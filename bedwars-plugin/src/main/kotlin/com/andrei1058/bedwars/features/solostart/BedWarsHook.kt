package com.andrei1058.bedwars.features.solostart

import com.andrei1058.bedwars.BedWars
import com.andrei1058.bedwars.api.arena.GameState
import com.andrei1058.bedwars.api.arena.IArena
import org.bukkit.entity.Player

/** The few things SoloControls asks of the game, straight from the BedWars1058 API. */
object BedWarsHook {

    fun arenaOf(p: Player): IArena? = BedWars.getAPI().getArenaUtil().getArenaByPlayer(p)

    fun isPlayer(arena: IArena, p: Player): Boolean = arena.isPlayer(p)

    fun isSpectator(arena: IArena, p: Player): Boolean = arena.isSpectator(p)

    fun arenaName(arena: IArena): String = arena.getArenaName() ?: arena.getWorldName() ?: "arena"

    /**
     * Starts [arena] after [seconds] regardless of how many players it has.
     * Returns null on success or a message explaining why it could not start.
     */
    fun forceStart(arena: IArena, seconds: Int): String? {
        when (arena.getStatus()) {
            GameState.playing -> return "The game is already running."
            GameState.restarting -> return "The arena is restarting."
            GameState.waiting -> arena.changeStatus(GameState.starting)
            else -> {}
        }
        val task = arena.getStartingTask() ?: return "BedWars did not create a starting task."
        task.setCountdown(seconds.coerceAtLeast(1))
        return null
    }

    /**
     * Ends the game immediately: the restarting state sends everyone back to the lobby and resets the map.
     * Returns null on success or a message explaining why it could not.
     */
    fun endGame(arena: IArena): String? {
        when (arena.getStatus()) {
            GameState.restarting -> return "The arena is already restarting."
            GameState.waiting, GameState.starting -> return "The game has not started yet."
            else -> {}
        }
        arena.changeStatus(GameState.restarting)
        return null
    }
}
