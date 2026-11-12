package com.andrei1058.bedwars.features.replay.bedwars

import com.andrei1058.bedwars.BedWars
import com.andrei1058.bedwars.api.arena.GameState
import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.api.arena.team.ITeam
import com.andrei1058.bedwars.api.arena.team.TeamColor
import com.andrei1058.bedwars.api.events.gameplay.GameEndEvent
import com.andrei1058.bedwars.api.events.gameplay.GameStateChangeEvent
import com.andrei1058.bedwars.api.events.player.PlayerBedBreakEvent
import com.andrei1058.bedwars.api.events.player.PlayerKillEvent
import com.andrei1058.bedwars.api.events.player.PlayerLeaveArenaEvent
import com.andrei1058.bedwars.api.events.player.PlayerReSpawnEvent
import com.andrei1058.bedwars.api.events.server.ArenaDisableEvent
import com.andrei1058.bedwars.api.events.server.ArenaRestartEvent
import com.andrei1058.bedwars.api.events.team.TeamEliminatedEvent
import com.andrei1058.bedwars.arena.Arena
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin
import java.util.logging.Level

/**
 * The replay feature lives inside BedWars1058, so it talks to the arenas, teams and events of the core directly.
 * [TeamRef] and [ArenaRef] only keep the rest of the feature independent of the core's class layout.
 */
class TeamRef(val handle: ITeam) {
    val name: String get() = handle.getName() ?: "?"
    val bedDestroyed: Boolean get() = handle.isBedDestroyed()
    val members: List<Player> get() = handle.getMembers()?.toList() ?: emptyList()

    /** Location of the team's bed (its head block), or null if it has none. */
    val bed: org.bukkit.Location? get() = handle.getBed()

    /** Team colour as 0xRRGGBB. */
    val rgb: Int
        get() {
            val color: TeamColor = handle.getColor() ?: return -1
            return runCatching { TeamColor.getColor(color).asRGB() }.getOrNull() ?: NAMED[color.toString().uppercase()] ?: -1
        }

    private companion object {
        val NAMED = mapOf(
            "RED" to 0xFF5555, "BLUE" to 0x5555FF, "GREEN" to 0x55FF55, "YELLOW" to 0xFFFF55,
            "AQUA" to 0x55FFFF, "WHITE" to 0xFFFFFF, "PINK" to 0xFF55FF, "GRAY" to 0xAAAAAA,
            "ORANGE" to 0xFFAA00, "PURPLE" to 0xAA00AA, "CYAN" to 0x00AAAA, "LIME" to 0x55FF55,
        )
    }
}

/** Thin handle on a BedWars arena. Two refs to the same arena are equal. */
class ArenaRef(val handle: IArena) {
    val name: String get() = handle.getArenaName()
    val worldName: String get() = handle.getWorldName() ?: name
    val group: String? get() = (handle.getGroup() as String?)
    val displayName: String get() = handle.getDisplayName() ?: name
    val status: String get() = (handle.getStatus() as GameState?)?.toString() ?: ""
    val world: World? get() = handle.getWorld()
    val players: List<Player> get() = handle.getPlayers()?.toList() ?: emptyList()
    val spectators: List<Player> get() = handle.getSpectators()?.toList() ?: emptyList()
    val teams: List<TeamRef> get() = handle.getTeams()?.map(::TeamRef) ?: emptyList()

    fun teamOf(p: Player): TeamRef? = (handle.getTeam(p) as ITeam?)?.let(::TeamRef)

    /** Dead players waiting to respawn stay in [players] but are hidden and parked elsewhere. */
    fun isRespawning(p: Player): Boolean = handle.isReSpawning(p)

    override fun equals(other: Any?) = other is ArenaRef && other.handle === handle
    override fun hashCode() = System.identityHashCode(handle)
}

interface BedWarsCallbacks {
    fun onStateChange(arena: ArenaRef, newState: String) {}
    fun onGameEnd(arena: ArenaRef, winnerTeam: String?) {}
    fun onKill(arena: ArenaRef, victim: Player, killer: Player?, finalKill: Boolean, message: String?) {}
    fun onBedBreak(arena: ArenaRef, breaker: Player, victimTeam: String) {}
    fun onTeamEliminated(arena: ArenaRef, team: String) {}
    fun onRespawn(arena: ArenaRef, player: Player) {}
    fun onLeave(arena: ArenaRef, player: Player) {}
    fun onArenaDisabled(arenaName: String, worldName: String?) {}
}

class BedWarsBridge(private val plugin: Plugin) {
    /** Set before [register]; the recorder needs the bridge and the bridge reports to the recorder. */
    lateinit var callbacks: BedWarsCallbacks

    private var listener: Listener? = null

    /** BedWars1058 is this plugin: there is always an API to talk to. */
    fun detect(): Boolean = true

    /** Subscribes to the BedWars events (removed again by [unregister]). */
    fun register() {
        val l = Events()
        listener = l
        plugin.server.pluginManager.registerEvents(l, plugin)
    }

    fun unregister() {
        listener?.let { HandlerList.unregisterAll(it) }
        listener = null
    }

    private inline fun guarded(what: String, block: () -> Unit) {
        try {
            block()
        } catch (t: Throwable) {
            plugin.logger.log(Level.WARNING, "[Replay] Error handling $what", t)
        }
    }

    private inner class Events : Listener {
        @EventHandler(priority = EventPriority.MONITOR)
        fun onState(e: GameStateChangeEvent) = guarded("GameStateChangeEvent") {
            callbacks.onStateChange(ArenaRef(e.getArena()), e.getNewState().toString())
        }

        @EventHandler(priority = EventPriority.MONITOR)
        fun onEnd(e: GameEndEvent) = guarded("GameEndEvent") {
            val winner = (e.getTeamWinner() as ITeam?)?.let { TeamRef(it).name }
            callbacks.onGameEnd(ArenaRef(e.getArena()), winner)
        }

        @EventHandler(priority = EventPriority.MONITOR)
        fun onKill(e: PlayerKillEvent) = guarded("PlayerKillEvent") {
            val victim = e.getVictim()
            val killer = e.getKiller()
            val finalKill = e.getCause().isFinalKill()
            val message = try {
                e.getMessage()?.apply(killer ?: victim)
            } catch (_: Throwable) { null }
            callbacks.onKill(ArenaRef(e.getArena()), victim, killer, finalKill, message)
        }

        @EventHandler(priority = EventPriority.MONITOR)
        fun onBed(e: PlayerBedBreakEvent) = guarded("PlayerBedBreakEvent") {
            val victim = (e.getVictimTeam() as ITeam?)?.let { TeamRef(it).name } ?: "?"
            callbacks.onBedBreak(ArenaRef(e.getArena()), e.getPlayer(), victim)
        }

        @EventHandler(priority = EventPriority.MONITOR)
        fun onEliminated(e: TeamEliminatedEvent) = guarded("TeamEliminatedEvent") {
            callbacks.onTeamEliminated(ArenaRef(e.getArena()), TeamRef(e.getTeam()).name)
        }

        @EventHandler(priority = EventPriority.MONITOR)
        fun onRespawn(e: PlayerReSpawnEvent) = guarded("PlayerReSpawnEvent") {
            callbacks.onRespawn(ArenaRef(e.getArena()), e.getPlayer())
        }

        @EventHandler(priority = EventPriority.MONITOR)
        fun onLeave(e: PlayerLeaveArenaEvent) = guarded("PlayerLeaveArenaEvent") {
            callbacks.onLeave(ArenaRef(e.getArena()), e.getPlayer())
        }

        // Several instances of one arena share its name (auto-scale clones, arenas MapLoader re-creates), so the world
        // name is what tells them apart.
        @EventHandler(priority = EventPriority.MONITOR)
        fun onDisable(e: ArenaDisableEvent) = guarded("ArenaDisableEvent") {
            callbacks.onArenaDisabled(e.getArenaName(), e.getWorldName())
        }

        @EventHandler(priority = EventPriority.MONITOR)
        fun onRestart(e: ArenaRestartEvent) = guarded("ArenaRestartEvent") {
            callbacks.onArenaDisabled(e.getArenaName(), e.getWorldName())
        }
    }

    /**
     * Asks BedWars to draw its own lobby sidebar for [player] again (removes the old one first so it is rebuilt from
     * scratch). Returns true if the sidebar service was called.
     */
    fun refreshLobbySidebar(player: Player): Boolean {
        return try {
            val mgr = BedWars.getAPI().getScoreboardManager()
            mgr.remove(player)
            mgr.giveSidebar(player, null, true)
            true
        } catch (t: Throwable) {
            plugin.logger.warning("[Replay] BedWars sidebar refresh failed: " + (t.cause ?: t))
            false
        }
    }

    fun arenaOf(player: Player): ArenaRef? {
        val a: IArena? = Arena.getArenaByPlayer(player)
        return a?.let(::ArenaRef)
    }

    fun arenas(): List<ArenaRef> = Arena.getArenas().map(::ArenaRef)

    /** Finds the pristine map of an arena in MapLoader's library, BedWars' own cache or the server root. */
    val templates by lazy { MapTemplates(plugin) }
}
