package com.andrei1058.bedwars.features.arenamanagement.bedwars

import com.andrei1058.bedwars.BedWars
import com.andrei1058.bedwars.api.arena.GameState
import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.features.arenamanagement.ArenaManagementPlugin
import com.andrei1058.bedwars.features.arenamanagement.arena.LiveArena
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

/** Read-only view of the loaded arenas, straight from the BedWars1058 API. */
class BedWarsHook(private val plugin: ArenaManagementPlugin) {
    val bedWarsPlugin: Plugin? get() = plugin.host

    val isAvailable: Boolean get() = true

    fun connect(): Boolean = true

    /** All arenas BedWars1058 currently has loaded (i.e. enabled). */
    fun liveArenas(): List<LiveArena> {
        return try {
            ArrayList(BedWars.getAPI().getArenaUtil().getArenas()).mapNotNull { toLive(it) }
        } catch (t: Throwable) {
            plugin.logger.warning("Reading BedWars1058 arenas failed: ${t.javaClass.simpleName}: ${t.message}")
            emptyList()
        }
    }

    private fun toLive(arena: IArena): LiveArena? {
        val name = arena.getArenaName() ?: arena.getWorldName() ?: return null
        val display = arena.getDisplayName()?.takeIf { it.isNotBlank() } ?: name
        val group = arena.getGroup() ?: plugin.settings.defaultGroup
        val status = arena.getStatus()?.toString()?.lowercase() ?: "waiting"
        val players = arena.getPlayers()?.size ?: 0
        // getWorld() is null when the arena's world is not loaded (failed to load, or parked by MapLoader).
        // Disabling such an arena the usual way throws, so the menus need to know.
        val worldLoaded = arena.getWorld() != null
        return LiveArena(name, display, group, status, players, arena.getMaxPlayers(), worldLoaded)
    }

    /** True when the player is currently in (or spectating) an arena. */
    fun isInArena(player: Player): Boolean = BedWars.getAPI().getArenaUtil().getArenaByPlayer(player) != null

    /** Spectates a running arena. */
    fun spectate(player: Player, arenaName: String): Boolean {
        val arena: IArena? = BedWars.getAPI().getArenaUtil().getArenaByName(arenaName)
        if (arena == null || arena.getStatus() != GameState.playing) return false
        return try {
            arena.addSpectator(player, false, null)
        } catch (t: Throwable) {
            false
        }
    }
}
