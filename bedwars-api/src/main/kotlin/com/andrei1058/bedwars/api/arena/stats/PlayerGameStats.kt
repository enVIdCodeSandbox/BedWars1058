package com.andrei1058.bedwars.api.arena.stats

import java.util.Optional
import java.util.UUID

/**
 * Player stats container for a game.
 */
interface PlayerGameStats {
    fun getPlayer(): UUID

    /**
     * @return player display name.
     */
    fun getDisplayPlayer(): String

    /**
     * @return player username.
     */
    fun getUsername(): String

    fun registerStatistic(id: String, defaultValue: GameStatistic<*>)

    fun getStatistic(id: String): Optional<GameStatistic<*>>?

    fun getStatistic(id: DefaultStatistics): Optional<GameStatistic<*>>? {
        return getStatistic(id.toString())
    }

    /**
     * List of registered statistics.
     */
    fun getRegistered(): MutableList<String>
}
