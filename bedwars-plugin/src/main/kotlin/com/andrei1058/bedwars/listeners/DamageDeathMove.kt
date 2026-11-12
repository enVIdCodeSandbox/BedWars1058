/*
 * BedWars1058 - A bed wars mini-game.
 * Copyright (C) 2021 Andrei Dascălu
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Contact e-mail: andrew.dascalu@gmail.com
 */

package com.andrei1058.bedwars.listeners

import com.andrei1058.bedwars.BedWars
import com.andrei1058.bedwars.BedWars.Companion.config
import com.andrei1058.bedwars.BedWars.Companion.getAPI
import com.andrei1058.bedwars.BedWars.Companion.getLobbyWorld
import com.andrei1058.bedwars.BedWars.Companion.getServerType
import com.andrei1058.bedwars.BedWars.Companion.nms
import com.andrei1058.bedwars.BedWars.Companion.plugin
import com.andrei1058.bedwars.BedWars.Companion.shop
import com.andrei1058.bedwars.api.arena.GameState
import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.api.arena.generator.IGenerator
import com.andrei1058.bedwars.api.arena.shop.ShopHolo
import com.andrei1058.bedwars.api.arena.team.ITeam
import com.andrei1058.bedwars.api.configuration.ConfigPath
import com.andrei1058.bedwars.api.entity.Despawnable
import com.andrei1058.bedwars.api.events.player.PlayerInvisibilityPotionEvent
import com.andrei1058.bedwars.api.events.player.PlayerKillEvent
import com.andrei1058.bedwars.api.events.team.TeamEliminatedEvent
import com.andrei1058.bedwars.api.language.Language
import com.andrei1058.bedwars.api.language.Language.Companion.getMsg
import com.andrei1058.bedwars.api.language.Messages
import com.andrei1058.bedwars.api.server.ServerType
import com.andrei1058.bedwars.arena.Arena
import com.andrei1058.bedwars.arena.LastHit
import com.andrei1058.bedwars.arena.LastHit.Companion.getLastHit
import com.andrei1058.bedwars.arena.SetupSession
import com.andrei1058.bedwars.arena.team.BedWarsTeam
import com.andrei1058.bedwars.configuration.Sounds
import com.andrei1058.bedwars.listeners.dropshandler.PlayerDrops
import com.andrei1058.bedwars.support.paper.TeleportManager
import java.text.DecimalFormat
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.*
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.potion.PotionEffectType
import org.bukkit.projectiles.ProjectileSource
import org.bukkit.util.Vector
import com.andrei1058.bedwars.api.util.unchecked

open class DamageDeathMove : Listener {
    private val tntJumpBarycenterAlterationInY: Double
    private val tntJumpStrengthReductionConstant: Double
    private val tntJumpYAxisReductionConstant: Double
    private val tntDamageSelf: Double
    private val tntDamageTeammates: Double
    private val tntDamageOthers: Double

    @EventHandler
    open fun onDamage(e: EntityDamageEvent) {
        if (e.getEntity() is Player) {
            val p = e.getEntity() as Player
            val a: IArena? = Arena.getArenaByPlayer(p)
            if (a != null) {
                if (a.isSpectator(p)) {
                    e.setCancelled(true)
                    return
                }
                if (a.isReSpawning(p)) {
                    e.setCancelled(true)
                    return
                }
                if (a.getStatus() != GameState.playing) {
                    e.setCancelled(true)
                    return
                }
                //if (e.getCause() == EntityDamageEvent.DamageCause.FALL) {
                if (BedWarsTeam.reSpawnInvulnerability.containsKey(p.getUniqueId())) {
                    if (BedWarsTeam.reSpawnInvulnerability.get(p.getUniqueId())!! > System.currentTimeMillis()) {
                        e.setCancelled(true)
                    } else {
                        BedWarsTeam.reSpawnInvulnerability.remove(p.getUniqueId())
                    }
                }
                // todo why did I set this to 1? disabled for now
                /*if (e.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION) {
                    e.setDamage(1);
                    return;
                }*/
                //}
            }
        }
        if (BedWars.getServerType() == ServerType.MULTIARENA) {
            if (e.getEntity().getLocation().getWorld().getName().equals(BedWars.getLobbyWorld(), ignoreCase = true)) {
                e.setCancelled(true)
            }
        }
    }

    // show player health on bow hit
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    open fun onBowHit(e: EntityDamageByEntityEvent) {
        if (e.getEntity().getType() != EntityType.PLAYER) {
            return
        }
        if (e.getDamager() !is Projectile) {
            return
        }
        val projectile = e.getDamager() as Projectile
        if (projectile.getShooter() == null) {
            return
        }
        if (projectile.getShooter() !is Player) {
            return
        }
        val p = e.getEntity() as Player
        val damager = projectile.getShooter() as Player
        val a: IArena? = Arena.getArenaByPlayer(p)
        if (a == null) {
            return
        }
        if (a.getStatus() != GameState.playing) {
            return
        }
        // projectile hit message #696, #711
        val team = a.getTeam(p)
        val lang = Language.getPlayerLanguage(damager)
        if (lang.m(Messages.PLAYER_HIT_BOW).isEmpty()) {
            return
        }
        val message = lang.m(Messages.PLAYER_HIT_BOW).replace("{amount}", DecimalFormat("00.#").format((e.getEntity() as Player).getHealth() - e.getFinalDamage())).replace("{TeamColor}", team!!.getColor().chat().toString()).replace("{TeamName}", team!!.getDisplayName(lang)).replace("{PlayerName}", ChatColor.stripColor(p.getDisplayName()))
        damager.sendMessage(message)
    }

    @EventHandler
    open fun onDamageByEntity(e: EntityDamageByEntityEvent) {
        if (e.getEntity() is Player) {
            val p = e.getEntity() as Player
            val a: IArena? = Arena.getArenaByPlayer(p)
            if (a != null) {
                if (a.getStatus() != GameState.playing) {
                    e.setCancelled(true)
                    return
                }
                if (a.isSpectator(p) || a.isReSpawning(p)) {
                    e.setCancelled(true)
                    return
                }
                var damager: Player? = null
                var projectile: Boolean = false
                if (e.getDamager() is Player) {
                    damager = e.getDamager() as Player
                } else if (e.getDamager() is Projectile) {
                    val shooter = (e.getDamager() as Projectile).getShooter()
                    if (shooter is Player) {
                        damager = shooter as Player
                    } else {
                        return
                    }
                    projectile = true
                } else if (e.getDamager() is Player) {
                    damager = e.getDamager() as Player
                    if (a.isReSpawning(damager)) {
                        e.setCancelled(true)
                        return
                    }
                } else if (e.getDamager() is TNTPrimed) {
                    val tnt = e.getDamager() as TNTPrimed
                    if (tnt.getSource() != null) {
                        if (tnt.getSource() is Player) {
                            damager = tnt.getSource() as Player
                            if (damager == p) {
                                if (tntDamageSelf > -1) {
                                    e.setDamage(tntDamageSelf)
                                }
                                // tnt jump. credits to feargames.it
                                val damaged = e.getEntity() as LivingEntity
                                val distance = damaged.getLocation().subtract(0.0, tntJumpBarycenterAlterationInY, 0.0).toVector().subtract(tnt.getLocation().toVector())
                                val direction = distance.clone().normalize()
                                val force: Double = ((tnt.getYield() * tnt.getYield()) / (tntJumpStrengthReductionConstant + distance.length()))
                                val resultingForce = direction.clone().multiply(force)
                                resultingForce.setY(resultingForce.getY() / (distance.length() + tntJumpYAxisReductionConstant))
                                damaged.setVelocity(resultingForce)
                            } else {
                                val currentTeam = a.getTeam(p)
                                val damagerTeam = a.getTeam(damager)
                                if (currentTeam == damagerTeam) {
                                    if (tntDamageTeammates > -1) {
                                        e.setDamage(tntDamageTeammates)
                                    }
                                } else {
                                    if (tntDamageOthers > -1) {
                                        e.setDamage(tntDamageOthers)
                                    }
                                }
                            }
                        } else {
                            return
                        }
                    }
                } else if ((e.getDamager() is Silverfish) || (e.getDamager() is IronGolem)) {
                    val lh: LastHit? = LastHit.getLastHit(p)
                    if (lh != null) {
                        lh.setDamager(e.getDamager())
                        lh.setTime(System.currentTimeMillis())
                    } else {
                        LastHit(p, e.getDamager(), System.currentTimeMillis())
                    }
                }
                if (damager != null) {
                    if (a.isSpectator(damager) || a.isReSpawning(damager.getUniqueId())) {
                        e.setCancelled(true)
                        return
                    }
                    if (a.getTeam(p) == a.getTeam(damager)) {
                        if (e.getDamager() !is TNTPrimed) {
                            e.setCancelled(true)
                        }
                        return
                    }
                    // protection after re-spawn
                    if (BedWarsTeam.reSpawnInvulnerability.containsKey(p.getUniqueId())) {
                        if (BedWarsTeam.reSpawnInvulnerability.get(p.getUniqueId())!! > System.currentTimeMillis()) {
                            e.setCancelled(true)
                            return
                        } else {
                            BedWarsTeam.reSpawnInvulnerability.remove(p.getUniqueId())
                        }
                    }
                    // but if the damageR is the re-spawning player remove protection
                    BedWarsTeam.reSpawnInvulnerability.remove(damager.getUniqueId())
                    val lh: LastHit? = LastHit.getLastHit(p)
                    if (lh != null) {
                        lh.setDamager(damager)
                        lh.setTime(System.currentTimeMillis())
                    } else {
                        LastHit(p, damager, System.currentTimeMillis())
                    }
                    // if player gets hit show him
                    if (a.getShowTime()!!.containsKey(p)) {
                        Bukkit.getScheduler().runTask(plugin, Runnable {
                            for (on in a.getWorld()!!.getPlayers()) {
                                BedWars.nms.showArmor(p, on)
                                //BedWars.nms.showPlayer(p, on);
                            }
                            a.getShowTime()!!.remove(p)
                            p.removePotionEffect(PotionEffectType.INVISIBILITY)
                            val team = a.getTeam(p)
                            p.sendMessage(getMsg(p, Messages.INTERACT_INVISIBILITY_REMOVED_DAMGE_TAKEN))
                            Bukkit.getPluginManager().callEvent(PlayerInvisibilityPotionEvent(PlayerInvisibilityPotionEvent.Type.REMOVED, team.unchecked(), p, a))
                        })
                    }
                    // #274
                    //
                }
            }
        } else if (nms.isDespawnable(e.getEntity())) {
            var damager: Player?
            if (e.getDamager() is Player) {
                damager = e.getDamager() as Player
            } else if (e.getDamager() is Projectile) {
                val proj = e.getDamager() as Projectile
                damager = proj.getShooter() as Player
            } else if (e.getDamager() is TNTPrimed) {
                val tnt = e.getDamager() as TNTPrimed
                if (tnt.getSource() is Player) {
                    damager = tnt.getSource() as Player
                } else {
                    return
                }
            } else {
                return
            }
            val a: IArena? = Arena.getArenaByPlayer(damager)
            if (a != null) {
                if (a.isPlayer(damager)) {
                    // do not hurt own mobs
                    if (a.getTeam(damager) === nms.getDespawnablesList().get(e.getEntity().getUniqueId())!!.getTeam()) {
                        e.setCancelled(true)
                    }
                } else {
                    e.setCancelled(true)
                }
            }
        }
        /*else if (e.getEntity() instanceof IronGolem) {
        Player damager;
        if (e.getDamager() instanceof Player) {
        damager = (Player) e.getDamager();
        } else if (e.getDamager() instanceof Projectile) {
        Projectile proj = (Projectile) e.getDamager();
        damager = (Player) proj.getShooter();
        } else {
        return;
        }
        Arena a = Arena.getArenaByPlayer(damager);
        if (a != null) {
        if (a.isPlayer(damager)) {
        if (nms.isDespawnable(e.getEntity())) {
        if (a.getTeam(damager) == ((OwnedByTeam) nms.getDespawnablesList().get(e.getEntity().getUniqueId())).getOwner()) {
        e.setCancelled(true);
        }
        }
        }
        }
        }*/
        if (BedWars.getServerType() == ServerType.MULTIARENA) {
            if (e.getEntity().getLocation().getWorld().getName().equals(BedWars.getLobbyWorld(), ignoreCase = true)) {
                e.setCancelled(true)
            }
        }
    }

    @EventHandler
    open fun onDeath(e: PlayerDeathEvent) {
        val victim = e.getEntity()
        var killer: Player? = e.getEntity().getKiller()
        var killersTeam: ITeam? = null
        val a: IArena? = Arena.getArenaByPlayer(victim)
        if ((BedWars.getServerType() == ServerType.MULTIARENA && BedWars.getLobbyWorld() == e.getEntity().getWorld().getName()) || a != null) {
            e.setDeathMessage(null)
        }
        if (a != null) {
            if (a.isSpectator(victim)) {
                victim.spigot().respawn()
                return
            }
            if (a.getStatus() != GameState.playing) {
                victim.spigot().respawn()
                return
            }
            val damageEvent: EntityDamageEvent? = e.getEntity().getLastDamageCause()
            val victimsTeam: ITeam? = a.getTeam(victim)
            if (a.getStatus() != GameState.playing) {
                victim.spigot().respawn()
                return
            }
            if (victimsTeam == null) {
                victim.spigot().respawn()
                return
            }
            BedWars.nms.clearArrowsFromPlayerBody(victim)
            var message: String = if (victimsTeam.isBedDestroyed()) Messages.PLAYER_DIE_UNKNOWN_REASON_FINAL_KILL else Messages.PLAYER_DIE_UNKNOWN_REASON_REGULAR
            var cause: PlayerKillEvent.PlayerKillCause = if (victimsTeam.isBedDestroyed()) PlayerKillEvent.PlayerKillCause.UNKNOWN_FINAL_KILL else PlayerKillEvent.PlayerKillCause.UNKNOWN
            if (damageEvent != null) {
                if (damageEvent.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
                    val lh: LastHit? = getLastHit(victim)
                    if (lh != null) {
                        if (lh.getTime() >= System.currentTimeMillis() - 15000) {
                            if (lh.getDamager() is Player) {
                                killer = lh.getDamager() as Player
                            }
                            if (killer != null && killer.getUniqueId() == victim.getUniqueId()) {
                                killer = null
                            }
                        }
                    }
                    if (killer == null) {
                        message = if (victimsTeam.isBedDestroyed()) Messages.PLAYER_DIE_EXPLOSION_WITHOUT_SOURCE_FINAL_KILL else Messages.PLAYER_DIE_EXPLOSION_WITHOUT_SOURCE_REGULAR
                    } else {
                        if (killer !== victim) {
                            message = if (victimsTeam.isBedDestroyed()) Messages.PLAYER_DIE_EXPLOSION_WITH_SOURCE_FINAL_KILL else Messages.PLAYER_DIE_EXPLOSION_WITH_SOURCE_REGULAR_KILL
                        } else {
                            message = if (victimsTeam.isBedDestroyed()) Messages.PLAYER_DIE_EXPLOSION_WITHOUT_SOURCE_FINAL_KILL else Messages.PLAYER_DIE_EXPLOSION_WITHOUT_SOURCE_REGULAR
                        }
                    }
                    cause = if (victimsTeam.isBedDestroyed()) PlayerKillEvent.PlayerKillCause.EXPLOSION_FINAL_KILL else PlayerKillEvent.PlayerKillCause.EXPLOSION
                } else if (damageEvent.getCause() == EntityDamageEvent.DamageCause.VOID) {
                    val lh: LastHit? = getLastHit(victim)
                    if (lh != null) {
                        if (lh.getTime() >= System.currentTimeMillis() - 15000) {
                            if (lh.getDamager() is Player) {
                                killer = lh.getDamager() as Player
                            }
                            if (killer != null && killer.getUniqueId() == victim.getUniqueId()) {
                                killer = null
                            }
                        }
                    }
                    if (killer == null) {
                        message = if (victimsTeam.isBedDestroyed()) Messages.PLAYER_DIE_VOID_FALL_FINAL_KILL else Messages.PLAYER_DIE_VOID_FALL_REGULAR_KILL
                    } else {
                        if (killer !== victim) {
                            message = if (victimsTeam.isBedDestroyed()) Messages.PLAYER_DIE_KNOCKED_IN_VOID_FINAL_KILL else Messages.PLAYER_DIE_KNOCKED_IN_VOID_REGULAR_KILL
                        } else {
                            message = if (victimsTeam.isBedDestroyed()) Messages.PLAYER_DIE_VOID_FALL_FINAL_KILL else Messages.PLAYER_DIE_VOID_FALL_REGULAR_KILL
                        }
                    }
                    cause = if (victimsTeam.isBedDestroyed()) PlayerKillEvent.PlayerKillCause.VOID_FINAL_KILL else PlayerKillEvent.PlayerKillCause.VOID
                } else if (damageEvent.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
                    if (killer == null) {
                        val lh: LastHit? = getLastHit(victim)
                        if (lh != null) {
                            if (lh.getTime() >= System.currentTimeMillis() - 15000) {
                                if (nms.isDespawnable(lh.getDamager().unchecked())) {
                                    val d = nms.getDespawnablesList().get(lh.getDamager()!!.getUniqueId())
                                    killersTeam = d!!.getTeam()
                                    message = if (d!!.getEntity()!!.getType() == EntityType.IRON_GOLEM) if (victimsTeam.isBedDestroyed()) Messages.PLAYER_DIE_IRON_GOLEM_FINAL_KILL else Messages.PLAYER_DIE_IRON_GOLEM_REGULAR else if (victimsTeam.isBedDestroyed()) Messages.PLAYER_DIE_DEBUG_FINAL_KILL else Messages.PLAYER_DIE_DEBUG_REGULAR
                                    cause = if (victimsTeam.isBedDestroyed()) d!!.getDeathFinalCause() else d!!.getDeathRegularCause()
                                }
                            }
                        }
                    } else {
                        message = if (victimsTeam.isBedDestroyed()) Messages.PLAYER_DIE_PVP_FINAL_KILL else Messages.PLAYER_DIE_PVP_REGULAR_KILL
                        cause = if (victimsTeam.isBedDestroyed()) PlayerKillEvent.PlayerKillCause.PVP_FINAL_KILL else PlayerKillEvent.PlayerKillCause.PVP
                    }
                } else if (damageEvent.getCause() == EntityDamageEvent.DamageCause.PROJECTILE) {
                    if (killer != null) {
                        message = if (victimsTeam.isBedDestroyed()) Messages.PLAYER_DIE_SHOOT_FINAL_KILL else Messages.PLAYER_DIE_SHOOT_REGULAR
                        cause = if (victimsTeam.isBedDestroyed()) PlayerKillEvent.PlayerKillCause.PLAYER_SHOOT_FINAL_KILL else PlayerKillEvent.PlayerKillCause.PLAYER_SHOOT
                    }
                } else if (damageEvent.getCause() == EntityDamageEvent.DamageCause.FALL) {
                    val lh: LastHit? = getLastHit(victim)
                    if (lh != null) {
                        // check if kicked off in the last 10 seconds
                        if (lh.getTime() >= System.currentTimeMillis() - 10000) {
                            if (lh.getDamager() is Player) {
                                killer = lh.getDamager() as Player
                            }
                            if (killer != null && killer.getUniqueId() == victim.getUniqueId()) {
                                killer = null
                            }
                            if (killer != null) {
                                if (killer !== victim) {
                                    message = if (victimsTeam.isBedDestroyed()) Messages.PLAYER_DIE_KNOCKED_BY_FINAL_KILL else Messages.PLAYER_DIE_KNOCKED_BY_REGULAR_KILL
                                } else {
                                    message = if (victimsTeam.isBedDestroyed()) Messages.PLAYER_DIE_VOID_FALL_FINAL_KILL else Messages.PLAYER_DIE_VOID_FALL_REGULAR_KILL
                                }
                            }
                            cause = if (victimsTeam.isBedDestroyed()) PlayerKillEvent.PlayerKillCause.PLAYER_PUSH_FINAL else PlayerKillEvent.PlayerKillCause.PLAYER_PUSH
                        }
                    }
                }
            }
            if (killer != null) {
                killersTeam = a.getTeam(killer)
            }
            val finalMessage = message
            val playerKillEvent = PlayerKillEvent(a, victim, victimsTeam, killer, killersTeam, { player -> Language.getMsg(player, finalMessage) }, cause)
            Bukkit.getPluginManager().callEvent(playerKillEvent)
            if (killer != null && playerKillEvent.playSound()) {
                Sounds.playSound(ConfigPath.SOUNDS_KILL, killer)
            }
            if (null != playerKillEvent.getMessage()) {
                for (on in a.getPlayers()!!) {
                    val lang = Language.getPlayerLanguage(on)
                    on.sendMessage(playerKillEvent.getMessage()!!.apply(on)!!.replace("{PlayerColor}", victimsTeam.getColor().chat().toString()).replace("{PlayerName}", victim.getDisplayName()).replace("{PlayerNameUnformatted}", victim.getName()).replace("{PlayerTeamName}", victimsTeam.getDisplayName(lang)).replace("{KillerColor}", if (killersTeam == null) "" else killersTeam.getColor().chat().toString()).replace("{KillerName}", if (killer == null) "" else killer.getDisplayName()).replace("{KillerNameUnformatted}", if (killer == null) "" else killer.getName()).replace("{KillerTeamName}", if (killersTeam == null) "" else killersTeam.getDisplayName(lang)))
                }
            }
            if (null != playerKillEvent.getMessage()) {
                for (on in a.getSpectators()!!) {
                    val lang = Language.getPlayerLanguage(on)
                    on.sendMessage(playerKillEvent.getMessage()!!.apply(on)!!.replace("{PlayerColor}", victimsTeam.getColor().chat().toString()).replace("{PlayerName}", victim.getDisplayName()).replace("{PlayerNameUnformatted}", victim.getName()).replace("{KillerColor}", if (killersTeam == null) "" else killersTeam.getColor().chat().toString()).replace("{PlayerTeamName}", victimsTeam.getDisplayName(lang)).replace("{KillerName}", if (killer == null) "" else killer.getDisplayName()).replace("{KillerNameUnformatted}", if (killer == null) "" else killer.getName()).replace("{KillerTeamName}", if (killersTeam == null) "" else killersTeam.getDisplayName(lang)))
                }
            }
            // handle drops
            if (PlayerDrops.handlePlayerDrops(a, victim, killer, victimsTeam, killersTeam.unchecked(), cause, e.getDrops())) {
                e.getDrops().clear()
            }
            // send respawn packet
            Bukkit.getScheduler().runTaskLater(plugin, Runnable { victim.spigot().respawn() }, 3L)
            // reset last damager
            val lastHit: LastHit? = LastHit.getLastHit(victim)
            if (lastHit != null) {
                lastHit.setDamager(null)
            }
            if (victimsTeam.isBedDestroyed() && victimsTeam.getSize() == 1 && a.getConfig()!!.getBoolean(ConfigPath.ARENA_DISABLE_GENERATOR_FOR_EMPTY_TEAMS)) {
                for (g in victimsTeam.getGenerators()) {
                    g.disable()
                }
                victimsTeam.getGenerators().clear()
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    open fun onRespawn(e: PlayerRespawnEvent) {
        val a: IArena? = Arena.getArenaByPlayer(e.getPlayer())
        if (a == null) {
            val ss: SetupSession? = SetupSession.getSession(e.getPlayer().getUniqueId())
            if (ss != null) {
                e.setRespawnLocation(e.getPlayer().getWorld().getSpawnLocation())
            }
        } else {
            if (a.isSpectator(e.getPlayer())) {
                e.setRespawnLocation(a.getSpectatorLocation())
                val iso = Language.getPlayerLanguage(e.getPlayer()).getIso()
                for (o in a.getOreGenerators()!!) {
                    o.updateHolograms(e.getPlayer(), iso.unchecked())
                }
                for (t in a.getTeams()!!) {
                    for (o in t.getGenerators()) {
                        o.updateHolograms(e.getPlayer(), iso.unchecked())
                    }
                }
                for (sh in ShopHolo.getShopHolo()) {
                    if (sh.getA() === a) {
                        sh.updateForPlayer(e.getPlayer(), iso.unchecked())
                    }
                }
                a.sendSpectatorCommandItems(e.getPlayer())
                return
            }
            val t: ITeam? = a.getTeam(e.getPlayer())
            if (t == null) {
                e.setRespawnLocation(a.getReSpawnLocation())
                plugin.getLogger().severe(e.getPlayer().getName() + " re-spawn error on " + a.getArenaName() + "[" + a.getWorldName() + "] because the team was NULL and he was not spectating!")
                plugin.getLogger().severe("This is caused by one of your plugins: remove or configure any re-spawn related plugins.")
                a.removePlayer(e.getPlayer(), false)
                a.removeSpectator(e.getPlayer(), false)
                return
            }
            if (t.isBedDestroyed()) {
                e.setRespawnLocation(a.getSpectatorLocation())
                a.addSpectator(e.getPlayer(), true, null)
                t.getMembers()!!.remove(e.getPlayer())
                e.getPlayer().sendMessage(getMsg(e.getPlayer(), Messages.PLAYER_DIE_ELIMINATED_CHAT))
                if (t.getMembers()!!.isEmpty()) {
                    Bukkit.getPluginManager().callEvent(TeamEliminatedEvent(a, t))
                    for (p in a.getWorld()!!.getPlayers()) {
                        p.sendMessage(getMsg(p, Messages.TEAM_ELIMINATED_CHAT).replace("{TeamColor}", t.getColor().chat().toString()).replace("{TeamName}", t.getDisplayName(Language.getPlayerLanguage(p))))
                    }
                    Bukkit.getScheduler().runTaskLater(plugin, a::checkWinner, 40L)
                }
            } else {
                //respawn session
                val respawnTime: Int = config.getInt(ConfigPath.GENERAL_CONFIGURATION_RE_SPAWN_COUNTDOWN)
                if (respawnTime > 1) {
                    e.setRespawnLocation(a.getReSpawnLocation())
                    a.startReSpawnSession(e.getPlayer(), respawnTime)
                } else {
                    // instant respawn configuration
                    e.setRespawnLocation(t.getSpawn())
                    t.respawnMember(e.getPlayer())
                }
            }
        }
    }

    @EventHandler
    open fun onMove(e: PlayerMoveEvent) {
        if (Arena.isInArena(e.getPlayer())) {
            val a = Arena.getArenaByPlayer(e.getPlayer())
            // todo check on x y z change... not head rotation because this is really spammy
            if (e.getFrom().getChunk().getX() != e.getTo().getChunk().getX() || e.getFrom().getChunk().getZ() != e.getTo().getChunk().getZ() || e.getFrom().getChunk().getWorld() != e.getTo().getChunk().getWorld()) {
                /* update armor-stands hidden by nms */
                val iso = Language.getPlayerLanguage(e.getPlayer()).getIso()
                for (o in a.getOreGenerators()!!) {
                    o.updateHolograms(e.getPlayer(), iso.unchecked())
                }
                for (t in a.getTeams()!!) {
                    for (o in t.getGenerators()) {
                        o.updateHolograms(e.getPlayer(), iso.unchecked())
                    }
                }
                for (sh in ShopHolo.getShopHolo()) {
                    if (sh.getA() === a) {
                        sh.updateForPlayer(e.getPlayer(), iso.unchecked())
                    }
                }
                // hide armor for those with invisibility potions
                if (!a.getShowTime()!!.isEmpty()) {
                    // generic hide packets
                    for (entry in a.getShowTime()!!.entries) {
                        if (entry.value > 1) {
                            BedWars.nms.hideArmor(entry.key, e.getPlayer())
                        }
                    }
                    // if the moving player has invisible armor
                    if (a.getShowTime()!!.containsKey(e.getPlayer())) {
                        for (p in a.getPlayers()!!) {
                            nms.hideArmor(e.getPlayer(), p)
                        }
                    }
                    if (a.getShowTime()!!.containsKey(e.getPlayer())) {
                        for (p in a.getSpectators()!!) {
                            nms.hideArmor(e.getPlayer(), p)
                        }
                    }
                }
            }
            if (a.isSpectator(e.getPlayer()) || a.isReSpawning(e.getPlayer())) {
                if (e.getTo().getY() < 0) {
                    TeleportManager.teleportC(e.getPlayer(), (if (a.isSpectator(e.getPlayer())) a.getSpectatorLocation() else a.getReSpawnLocation()).unchecked(), PlayerTeleportEvent.TeleportCause.PLUGIN)
                    e.getPlayer().setAllowFlight(true)
                    e.getPlayer().setFlying(true)
                    // how to remove fall velocity?
                }
            } else {
                if (a.getStatus() == GameState.playing) {
                    if (e.getPlayer().getLocation().getBlockY() <= a.getYKillHeight()) {
                        nms.voidKill(e.getPlayer())
                    }
                    for (t in a.getTeams()!!) {
                        if (e.getPlayer().getLocation().distance(t.getBed()) < 4) {
                            if (t.isMember(e.getPlayer()) && (t is BedWarsTeam)) {
                                if ((t as BedWarsTeam).getBedHolo(e.getPlayer()) == null) {
                                    continue
                                }
                                if (!(t as BedWarsTeam).getBedHolo(e.getPlayer()).isHidden()) {
                                    (t as BedWarsTeam).getBedHolo(e.getPlayer()).hide()
                                }
                            }
                        } else {
                            if (t.isMember(e.getPlayer()) && (t is BedWarsTeam)) {
                                if ((t as BedWarsTeam).getBedHolo(e.getPlayer()) == null) {
                                    continue
                                }
                                if ((t as BedWarsTeam).getBedHolo(e.getPlayer()).isHidden()) {
                                    (t as BedWarsTeam).getBedHolo(e.getPlayer()).show()
                                }
                            }
                        }
                    }
                    if (e.getFrom() !== e.getTo()) {
                        Arena.afkCheck.remove(e.getPlayer().getUniqueId())
                        BedWars.getAPI().getAFKUtil().setPlayerAFK(e.getPlayer(), false)
                    }
                } else {
                    if (e.getPlayer().getLocation().getBlockY() <= 0) {
                        val bwt: ITeam? = a.getTeam(e.getPlayer())
                        if (bwt != null) {
                            TeleportManager.teleport(e.getPlayer(), bwt.getSpawn().unchecked())
                        } else {
                            TeleportManager.teleport(e.getPlayer(), a.getSpectatorLocation().unchecked())
                        }
                    }
                }
            }
        } else {
            if (config.getBoolean(ConfigPath.LOBBY_VOID_TELEPORT_ENABLED) && e.getPlayer().getWorld().getName().equals(config.getLobbyWorldName(), ignoreCase = true) && BedWars.getServerType() == ServerType.MULTIARENA) {
                if (e.getTo().getY() < config.getInt(ConfigPath.LOBBY_VOID_TELEPORT_HEIGHT)) {
                    TeleportManager.teleportC(e.getPlayer(), config.getConfigLoc("lobbyLoc").unchecked(), PlayerTeleportEvent.TeleportCause.PLUGIN)
                }
            }
        }
    }

    @EventHandler
    open fun onProjHit(e: ProjectileHitEvent) {
        val proj: Projectile? = e.getEntity()
        if (proj == null) {
            return
        }
        if (e.getEntity().getShooter() is Player) {
            val a: IArena? = Arena.getArenaByPlayer(e.getEntity().getShooter() as Player)
            if (a != null) {
                if (!a.isPlayer(e.getEntity().getShooter() as Player)) {
                    return
                }
                var utility = ""
                if (proj is Snowball) {
                    utility = "silverfish"
                }
                if (!utility.isEmpty()) {
                    spawnUtility(utility, e.getEntity().getLocation(), a.getTeam(e.getEntity().getShooter() as Player).unchecked(), e.getEntity().getShooter() as Player)
                }
            }
        }
    }

    @EventHandler
    open fun onItemFrameDamage(e: EntityDamageByEntityEvent) {
        if (e.getEntity().getType() == EntityType.ITEM_FRAME) {
            val a: IArena? = Arena.getArenaByIdentifier(e.getEntity().getWorld().getName())
            if (a != null) {
                e.setCancelled(true)
            }
            if (BedWars.getServerType() == ServerType.MULTIARENA) {
                if (BedWars.getLobbyWorld() == e.getEntity().getWorld().getName()) {
                    e.setCancelled(true)
                }
            }
        }
    }

    @EventHandler
    open fun onEntityDeath(e: EntityDeathEvent) {
        if (Arena.getArenaByIdentifier(e.getEntity().getLocation().getWorld().getName()) != null) {
            if (e.getEntityType() == EntityType.IRON_GOLEM || e.getEntityType() == EntityType.SILVERFISH) {
                e.getDrops().clear()
                e.setDroppedExp(0)
            }
        }
        // clean if necessary
        nms.getDespawnablesList().remove(e.getEntity().getUniqueId())
    }

    @EventHandler
    open fun onEat(e: PlayerItemConsumeEvent) {
        if (e.getItem().getType() == nms.materialCake()) {
            if (Arena.getArenaByIdentifier(e.getPlayer().getWorld().getName()) != null) {
                e.setCancelled(true)
            }
        }
    }

    init {
        this.tntJumpBarycenterAlterationInY = config.getYml().getDouble(ConfigPath.GENERAL_TNT_JUMP_BARYCENTER_IN_Y)
        this.tntJumpStrengthReductionConstant = config.getYml().getDouble(ConfigPath.GENERAL_TNT_JUMP_STRENGTH_REDUCTION)
        this.tntJumpYAxisReductionConstant = config.getYml().getDouble(ConfigPath.GENERAL_TNT_JUMP_Y_REDUCTION)
        this.tntDamageSelf = config.getYml().getDouble(ConfigPath.GENERAL_TNT_JUMP_DAMAGE_SELF)
        this.tntDamageTeammates = config.getYml().getDouble(ConfigPath.GENERAL_TNT_JUMP_DAMAGE_TEAMMATES)
        this.tntDamageOthers = config.getYml().getDouble(ConfigPath.GENERAL_TNT_JUMP_DAMAGE_OTHERS)
    }

    companion object {
        @JvmStatic
        private fun spawnUtility(s: String, loc: Location, t: ITeam, p: Player) {
            if ("silverfish".equals(s, ignoreCase = true)) {
                nms.spawnSilverfish(loc, t, shop.getYml().getDouble(ConfigPath.SHOP_SPECIAL_SILVERFISH_SPEED), shop.getYml().getDouble(ConfigPath.SHOP_SPECIAL_SILVERFISH_HEALTH), shop.getInt(ConfigPath.SHOP_SPECIAL_SILVERFISH_DESPAWN), BedWars.shop.getYml().getDouble(ConfigPath.SHOP_SPECIAL_SILVERFISH_DAMAGE))
            }
        }
    }
}
