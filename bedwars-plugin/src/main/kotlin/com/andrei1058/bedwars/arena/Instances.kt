package com.andrei1058.bedwars.arena

import com.andrei1058.bedwars.BedWars
import com.andrei1058.bedwars.api.arena.GameState
import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.api.configuration.ConfigPath

/**
 * Helpers for running several simultaneous instances (games) of the same arena.
 * Every instance owns a separate world copy, named by {@link ArenaManager#generateGameID()}
 * except the first one which keeps the arena's own world name on non auto-scale servers.
 */
object Instances {
    const val ENABLED_PATH: String = "multi-instance.enabled"
    const val MAX_PER_ARENA_PATH: String = "multi-instance.max-per-arena"

    /**
     * @return true if more than one game of the same arena may exist at once.
     */
    @JvmStatic
    fun enabled(): Boolean {
        if (BedWars.autoscale) {
            return true
        }
        // legacy bungee runs a single arena per server
        if (BedWars.getServerType() == com.andrei1058.bedwars.api.server.ServerType.BUNGEE) {
            return false
        }
        return BedWars.config.getYml().getBoolean(ENABLED_PATH, true)
    }

    /**
     * @return max simultaneous instances of one arena.
     */
    @JvmStatic
    fun limit(): Int {
        if (BedWars.autoscale) {
            return BedWars.config.getInt(ConfigPath.GENERAL_CONFIGURATION_AUTO_SCALE_LIMIT)
        }
        return BedWars.config.getYml().getInt(MAX_PER_ARENA_PATH, 5)
    }

    /**
     * Pick the world name for a new instance of an arena.
     */
    @JvmStatic
    fun pickWorldName(arenaName: String): String {
        if (BedWars.autoscale) {
            return BedWars.arenaManager.generateGameID()
        }
        if (!enabled()) {
            return arenaName
        }
        for (a in Arena.getEnableQueue()) {
            if (a.getArenaName().equals(arenaName, ignoreCase = true)) {
                return BedWars.arenaManager.generateGameID()
            }
        }
        if (!Arena.getArenasByName(arenaName).isEmpty()) {
            return BedWars.arenaManager.generateGameID()
        }
        return arenaName
    }

    @JvmStatic
    fun isJoinable(a: IArena): Boolean {
        val s = a.getStatus()
        return (s == GameState.waiting || s == GameState.starting) && a.getPlayers().size() < a.getMaxPlayers()
    }

    /**
     * The instance a player should be sent to: the fullest joinable one,
     * otherwise the oldest one.
     */
    @JvmStatic
    fun best(instances: MutableList<IArena>): IArena? {
        var best: IArena? = null
        for (a in instances) {
            if (!isJoinable(a)) {
                continue
            }
            if (best == null || a.getPlayers().size() > best.getPlayers().size()) {
                best = a
            }
        }
        if (best != null) {
            return best
        }
        return if (instances.isEmpty()) null else instances.get(0)
    }
}
