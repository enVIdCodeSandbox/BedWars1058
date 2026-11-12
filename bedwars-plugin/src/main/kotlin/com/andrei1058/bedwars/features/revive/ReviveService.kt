package com.andrei1058.bedwars.features.revive

import com.andrei1058.bedwars.api.arena.GameState
import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.api.arena.team.ITeam
import com.andrei1058.bedwars.api.configuration.ConfigPath
import com.andrei1058.bedwars.api.events.player.PlayerJoinArenaEvent
import com.andrei1058.bedwars.api.language.Language
import com.andrei1058.bedwars.features.revive.bed.BedRestoreResult
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.potion.PotionEffectType
import com.andrei1058.bedwars.api.language.Messages as BwMessages

/** A user-facing failure. [key] is a message key from config.yml. */
class ReviveFailure(val key: String, vararg val placeholders: Pair<String, String>) : Exception(key)

enum class ReviveKind { REVIVED, SWITCHED, BED_ONLY }

enum class BedOutcome { NOT_REQUESTED, NO_PERMISSION, ALREADY_INTACT, RESTORED, CREATED, NO_SNAPSHOT, FAILED }

class ReviveResult(val kind: ReviveKind, val bed: BedOutcome)

/**
 * Puts a player on a team of a running BedWars1058 game, whatever state the player and the team
 * are in: never in the game, dead, alive on another team; team full, team dead, bed gone.
 *
 * Behaviour here was checked against the BedWars1058 sources (Arena, BedWarsTeam, DamageDeathMove,
 * BwSidebar, ShopCacheListener).
 */
class ReviveService(private val plugin: BedWarsRevive) {

    private val api get() = plugin.api

    /**
     * Arena#addSpectator schedules tasks 1 and 5 ticks later that teleport the new spectator to the
     * spectator spot, make them invisible, hand out spectator items and flight. A player who was
     * just added has to be converted *after* those, or they would undo the conversion.
     */
    private val settleTicks = 7L

    /**
     * Reports its outcome through [done] exactly once (possibly a few ticks later, for players that
     * were not in the game yet).
     */
    fun revive(
        player: Player,
        arena: IArena,
        team: ITeam,
        wantBed: Boolean,
        mayRestoreBed: Boolean,
        maySwitch: Boolean,
        done: (ReviveResult?, ReviveFailure?) -> Unit
    ) {
        val alive: Boolean
        val outsider: Boolean
        val previousTeam: ITeam? = arena.getTeam(player)
        val bedWanted = wantBed && mayRestoreBed
        var bedOutcome = if (wantBed && !mayRestoreBed) BedOutcome.NO_PERMISSION else BedOutcome.NOT_REQUESTED

        try {
            if (arena.getStatus() != GameState.playing) throw ReviveFailure("arena-not-playing")
            if (arena.getTeams()!!.none { it === team }) throw ReviveFailure("unknown-team")

            val currentArena = api.getArenaUtil().getArenaByPlayer(player)
            if (currentArena != null && currentArena !== arena) throw ReviveFailure("in-other-arena")

            alive = currentArena != null && arena.isPlayer(player) && !arena.isSpectator(player)
            outsider = currentArena == null

            // Alive on this very team: the only thing left to do is the bed.
            if (alive && arena.getTeam(player) === team) {
                if (bedWanted && team.isBedDestroyed()) {
                    done(ReviveResult(ReviveKind.BED_ONLY, restoreBed(arena, team, force = false)), null)
                    return
                }
                throw ReviveFailure("already-on-team")
            }
            if (alive) {
                if (!maySwitch) throw ReviveFailure("switch-disabled")
                if (arena.isReSpawning(player)) throw ReviveFailure("respawning")
            }

            if (outsider) joinArenaAsSpectator(player, arena, team)
        } catch (failure: ReviveFailure) {
            done(null, failure)
            return
        } catch (t: Throwable) {
            plugin.logger.severe("Reviving ${player.name} onto ${team.getName()} in ${arena.getArenaName()} failed: $t")
            t.printStackTrace()
            done(null, ReviveFailure("internal-error"))
            return
        }

        val finish = Runnable {
            try {
                if (!player.isOnline || arena.getStatus() != GameState.playing) throw ReviveFailure("arena-not-playing")
                if (!alive && !arena.isSpectator(player)) throw ReviveFailure("internal-error")

                // ---- make sure they are a *player* (not a spectator) that belongs to no team
                if (!alive) spectatorToPlayer(player, arena)
                detachFromTeams(player, arena)

                // ---- a fresh joiner gets the same per-player state (shop, quick buy, tab list...)
                //      BedWars1058 creates when someone joins as a player.
                if (outsider) {
                    val join = PlayerJoinArenaEvent(arena, player, false)
                    plugin.server.pluginManager.callEvent(join)
                    if (join.isCancelled()) throw ReviveFailure("spectate-failed")
                }

                // ---- join the team (NO size check: full teams are allowed on purpose)
                val neverHadPlayers = team.getMembersCache()!!.isEmpty()
                team.addPlayers(player)
                if (neverHadPlayers && plugin.config.getBoolean("spawn-npcs-for-empty-team", true) &&
                    arena.getConfig()!!.getBoolean(ConfigPath.ARENA_DISABLE_NPCS_FOR_EMPTY_TEAMS)
                ) {
                    try {
                        team.spawnNPCs() // BedWars1058 skipped them at the start because the team was empty
                    } catch (t: Throwable) {
                        plugin.logger.warning("Could not spawn the NPCs of ${team.getName()}: $t")
                    }
                }
                restoreGeneratorsIfNeeded(team)
                team.firstSpawn(player)
                ensurePlayableState(player, arena)

                // ---- bed: needs a member on the team, so it is placed after joining
                if (bedWanted) {
                    bedOutcome =
                        if (team.isBedDestroyed()) restoreBed(arena, team, force = false) else BedOutcome.ALREADY_INTACT
                }

                // ---- a bed can't exist for an empty team: if this player was the last one, the old team dies
                if (previousTeam != null && previousTeam !== team) destroyBedIfEmpty(arena, previousTeam)

                // ---- the sidebar decides "YOU" when it is built, so give it after joining
                refreshSidebar(player, arena)
                debugState(player, arena)

                done(ReviveResult(if (alive) ReviveKind.SWITCHED else ReviveKind.REVIVED, bedOutcome), null)
            } catch (failure: ReviveFailure) {
                rollback(player, arena, outsider)
                done(null, failure)
            } catch (t: Throwable) {
                plugin.logger.severe("Reviving ${player.name} onto ${team.getName()} in ${arena.getArenaName()} failed: $t")
                t.printStackTrace()
                rollback(player, arena, outsider)
                done(null, ReviveFailure("internal-error"))
            }
        }

        if (outsider) plugin.server.scheduler.runTaskLater(plugin.host, finish, settleTicks) else finish.run()
    }

    // ------------------------------------------------------------------ steps

    /**
     * BedWars1058 only lets late joiners in as spectators. Passing a location makes Arena#addSpectator
     * skip the allow-spectate check and teleport the player there.
     */
    private fun joinArenaAsSpectator(player: Player, arena: IArena, team: ITeam) {
        if (!arena.addSpectator(player, false, team.getSpawn())) throw ReviveFailure("spectate-failed")
        if (!arena.isSpectator(player)) throw ReviveFailure("spectate-failed")
    }

    /** Undoes what BedWars1058's spectator mode did to the player and registers them as a player. */
    private fun spectatorToPlayer(player: Player, arena: IArena) {
        player.removePotionEffect(PotionEffectType.INVISIBILITY)
        arena.getSpectators()!!.remove(player)
        if (arena.isSpectator(player)) {
            plugin.logger.severe("Could not take ${player.name} out of the spectator list of ${arena.getArenaName()}.")
            throw ReviveFailure("internal-error")
        }
        if (!arena.getPlayers()!!.contains(player)) arena.getPlayers()!!.add(player)
        api.getArenaUtil().setArenaByPlayer(player, arena)
        resetPlayerState(player)
        syncVisibility(player, arena)
    }

    /**
     * Removes the player from every team's live member list AND its "ever been a member" history.
     * BedWars1058's sidebar marks a team as yours when `isMember || wasMember`, so leaving the
     * history behind shows (YOU) on the old team too.
     */
    @Suppress("DEPRECATION")
    private fun detachFromTeams(player: Player, arena: IArena) {
        for (t in arena.getTeams()!!) {
            if (t.isMember(player)) {
                t.getMembers()!!.remove(player)
                try {
                    t.destroyBedHolo(player)
                } catch (_: Throwable) {
                }
            }
            try {
                t.getMembersCache()!!.remove(player)
            } catch (_: Throwable) {
            }
            if (t.isMember(player) || t.wasMember(player.uniqueId)) {
                plugin.logger.warning(
                    "${player.name} still counts as a member of ${t.getName()} after being removed " +
                        "(isMember=${t.isMember(player)}, wasMember=${t.wasMember(player.uniqueId)})."
                )
            }
        }
    }

    /**
     * Puts a team's bed back, independent of any player. With [force] the bed is re-placed even if
     * it still stands (repairs a damaged/overbuilt bed); otherwise an intact bed is left alone.
     * The blocks are placed first: ITeam#setBedDestroyed(false) refuses to run (and logs an error)
     * when there is no bed block, and recolours the bed and refreshes the holograms when there is.
     */
    fun restoreBed(arena: IArena, team: ITeam, force: Boolean): BedOutcome {
        if (arena.getStatus() != GameState.playing) throw ReviveFailure("arena-not-playing")
        if (team.getMembers()!!.isEmpty()) throw ReviveFailure("bed-team-empty", "team" to team.getName()!!)
        if (!force && !team.isBedDestroyed()) return BedOutcome.ALREADY_INTACT
        val result = plugin.beds.restore(arena, team)
        return when (result) {
            BedRestoreResult.RESTORED, BedRestoreResult.CREATED -> {
                team.setBedDestroyed(false)
                try {
                    api.getScoreboardManager().refreshPlaceholders(arena)
                } catch (_: Throwable) {
                }
                if (result == BedRestoreResult.CREATED) BedOutcome.CREATED else BedOutcome.RESTORED
            }
            BedRestoreResult.NO_SNAPSHOT -> BedOutcome.NO_SNAPSHOT
            BedRestoreResult.FAILED -> BedOutcome.FAILED
        }
    }

    /** An empty team must not keep a bed: remove it and mark it destroyed so the game agrees. */
    private fun destroyBedIfEmpty(arena: IArena, team: ITeam) {
        if (team.getMembers()!!.isNotEmpty() || team.isBedDestroyed()) return
        try {
            plugin.beds.remove(team)
            team.setBedDestroyed(true)
            api.getScoreboardManager().refreshPlaceholders(arena)
        } catch (t: Throwable) {
            plugin.logger.warning("Could not destroy the bed of the emptied team ${team.getName()}: $t")
        }
    }

    /**
     * Turns an alive player into a dead spectator, exactly as if they had died with no bed -
     * whether or not their team's bed is intact. Mirrors BedWars1058's own final-death handling
     * (DamageDeathMove#onRespawn): spectator mode, removal from the live member list only, and
     * the elimination announcement + winner check when the team has nobody left.
     */
    fun eliminate(player: Player, arena: IArena) {
        if (arena.getStatus() != GameState.playing) throw ReviveFailure("arena-not-playing")
        if (api.getArenaUtil().getArenaByPlayer(player) !== arena || !arena.isPlayer(player) || arena.isSpectator(player)) {
            throw ReviveFailure("target-not-alive")
        }
        val team = arena.getTeam(player)
        try {
            arena.getRespawnSessions()!!.remove(player)
            if (team != null) {
                team.getMembers()!!.remove(player) // NOT the history list: dead players stay on it, like a normal death
                try {
                    @Suppress("DEPRECATION")
                    team.destroyBedHolo(player)
                } catch (_: Throwable) {
                }
            }
            player.inventory.clear()
            player.inventory.setArmorContents(null)
            if (!arena.addSpectator(player, true, null) || !arena.isSpectator(player)) {
                throw ReviveFailure("spectate-failed")
            }
        } catch (failure: ReviveFailure) {
            undoEliminate(player, arena, team)
            throw failure
        } catch (t: Throwable) {
            plugin.logger.severe("Eliminating ${player.name} in ${arena.getArenaName()} failed: $t")
            t.printStackTrace()
            undoEliminate(player, arena, team)
            throw ReviveFailure("internal-error")
        }

        try {
            player.sendMessage(Language.getMsg(player, BwMessages.PLAYER_DIE_ELIMINATED_CHAT))
            if (team != null && team.getMembers()!!.isEmpty()) {
                destroyBedIfEmpty(arena, team) // "died without a bed", whether or not it had one
                announceTeamEliminated(arena, team)
            }
        } catch (t: Throwable) {
            plugin.debug("eliminate announce: $t")
        }
    }

    private fun undoEliminate(player: Player, arena: IArena, team: ITeam?) {
        if (arena.isSpectator(player)) return
        if (!arena.getPlayers()!!.contains(player)) arena.getPlayers()!!.add(player)
        if (team != null && !team.getMembers()!!.contains(player)) team.getMembers()!!.add(player)
    }

    private fun announceTeamEliminated(arena: IArena, team: ITeam) {
        if (team.isBedDestroyed() && arena.getConfig()!!.getBoolean(ConfigPath.ARENA_DISABLE_GENERATOR_FOR_EMPTY_TEAMS)) {
            for (g in team.getGenerators()) g.disable()
            team.getGenerators().clear()
        }
        plugin.server.pluginManager.callEvent(com.andrei1058.bedwars.api.events.team.TeamEliminatedEvent(arena, team))
        for (p in arena.getWorld()!!.players) {
            p.sendMessage(
                Language.getMsg(p, BwMessages.TEAM_ELIMINATED_CHAT)
                    .replace("{TeamColor}", team.getColor().chat().toString())
                    .replace("{TeamName}", team.getDisplayName(api.getPlayerLanguage(p)))
            )
        }
        // BedWars1058 waits 2 seconds so the message is read before the game may end.
        plugin.server.scheduler.runTaskLater(plugin.host, Runnable { arena.checkWinner() }, 40L)
    }

    /**
     * A team that was wiped out with `disable-generator-for-empty-teams` loses its generators
     * (DamageDeathMove clears the list). Rebuild them for the player that revives it.
     */
    private fun restoreGeneratorsIfNeeded(team: ITeam) {
        if (team.getGenerators().isNotEmpty()) return
        try {
            (team as? com.andrei1058.bedwars.arena.team.BedWarsTeam)?.spawnGenerators()
        } catch (t: Throwable) {
            plugin.debug("spawnGenerators unavailable: $t")
        }
    }

    /**
     * Makes sure everything the shops and upgrade menus need exists for a player that is now on a team:
     * BedWars1058's ShopCache + PlayerQuickBuyCache (normally created by its own join listeners; the item
     * shop silently refuses to open without them) and entity collision (spectators have it switched off,
     * and a non-colliding player can't click the shop NPCs).
     */
    private fun ensurePlayableState(player: Player, arena: IArena) {
        try {
            api.getVersionSupport().setCollide(player, arena, true)
        } catch (t: Throwable) {
            plugin.debug("setCollide: $t")
        }
        try {
            if (com.andrei1058.bedwars.shop.ShopCache.getShopCache(player.uniqueId) == null) {
                plugin.logger.warning("${player.name} had no shop cache after joining; creating it.")
                com.andrei1058.bedwars.shop.ShopCache(player.uniqueId)
            }
            if (com.andrei1058.bedwars.shop.quickbuy.PlayerQuickBuyCache.getQuickBuyCache(player.uniqueId) == null) {
                plugin.logger.warning("${player.name} had no quick-buy cache after joining; creating it.")
                com.andrei1058.bedwars.shop.quickbuy.PlayerQuickBuyCache(player)
            }
        } catch (t: Throwable) {
            plugin.logger.warning("Could not verify the shop state of ${player.name}: $t")
        }
    }

    private fun resetPlayerState(player: Player) {
        try {
            player.gameMode = GameMode.SURVIVAL
            player.allowFlight = false
            player.isFlying = false
            player.fireTicks = 0
            player.fallDistance = 0f
            player.foodLevel = 20
            @Suppress("DEPRECATION")
            player.health = minOf(player.maxHealth, 20.0)
        } catch (t: Throwable) {
            plugin.debug("resetPlayerState: $t")
        }
    }

    /**
     * BedWars1058's argument order is (victim, receiver): `spigotShowPlayer(a, b)` makes **b see a**.
     * A new player must be seen by (and see) everyone playing; spectators see them but are hidden from them.
     */
    private fun syncVisibility(player: Player, arena: IArena) {
        try {
            val vs = api.getVersionSupport()
            for (other in arena.getPlayers()!!) {
                if (other == player) continue
                vs.spigotShowPlayer(player, other) // other sees player
                vs.spigotShowPlayer(other, player) // player sees other
            }
            for (spectator in arena.getSpectators()!!) {
                if (spectator == player) continue
                vs.spigotShowPlayer(player, spectator) // spectator sees player
                vs.spigotHidePlayer(spectator, player) // player doesn't see spectator
            }
        } catch (t: Throwable) {
            plugin.debug("syncVisibility: $t")
        }
    }

    private fun refreshSidebar(player: Player, arena: IArena) {
        try {
            api.getScoreboardManager().giveSidebar(player, arena, false)
            api.getScoreboardManager().refreshPlaceholders(arena)
        } catch (t: Throwable) {
            plugin.debug("refreshSidebar: $t")
        }
    }

    private fun debugState(player: Player, arena: IArena) {
        if (!plugin.config.getBoolean("debug", false)) return
        plugin.debug(
            "${player.name}: team=${arena.getTeam(player)?.getName()} " +
                arena.getTeams()!!.joinToString { "${it.getName()}[member=${it.isMember(player)},was=${it.wasMember(player.uniqueId)}]" }
        )
    }

    /** Best effort: don't leave a half-converted fresh joiner stuck inside the arena. */
    private fun rollback(player: Player, arena: IArena, joinedAsOutsider: Boolean) {
        if (!joinedAsOutsider) return
        try {
            if (arena.isSpectator(player)) arena.removeSpectator(player, false)
            else if (arena.isPlayer(player)) arena.removePlayer(player, false)
        } catch (t: Throwable) {
            plugin.logger.warning("Rollback for ${player.name} failed: $t")
        }
    }
}
