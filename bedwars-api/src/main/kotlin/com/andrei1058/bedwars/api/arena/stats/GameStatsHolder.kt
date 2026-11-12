package com.andrei1058.bedwars.api.arena.stats

import com.andrei1058.bedwars.api.arena.IArena
import java.util.Optional
import java.util.UUID
import org.bukkit.entity.Player

interface GameStatsHolder {
    fun getArena(): IArena

    /**
     * Register statistic.
     * Throws a runtime exception if statistic is already registered.
     * @param statistic new statistic.
     */
    fun register(statistic: GameStatisticProvider<*>)

    /**
     * Initialize game session stats for given player.
     * @param player stats holder.
     */
    fun init(player: Player): PlayerGameStats

    /**
     * Remove player tracked data.
     * @param uuid holder.
     */
    fun unregisterPlayer(uuid: UUID)

    /**
     * Get existing or initialize statistic for given player.
     * @param holder player holder.
     * @return Existing or new statistic.
     */
    fun getCreate(holder: Player): PlayerGameStats

    /**
     * Get existing or initialize statistic for given player.
     * @param holder player holder.
     * @return Existing or new statistic.
     */
    fun get(holder: UUID): Optional<PlayerGameStats>

    /**
     * Get existing or initialize statistic for given player.
     * @param holder player holder.
     * @return Existing or new statistic.
     */
    fun get(holder: Player): Optional<PlayerGameStats> {
        return get(holder.getUniqueId())
    }

    /**
     * Get tracked players.
     *
     * @return Unmodifiable list of tracked players.
     */
    fun getTrackedPlayers(): MutableCollection<Optional<PlayerGameStats>>

    /**
     * @param statistic Order collection by given statistic.
     * @return top list.
     */
    fun getOrderedBy(statistic: DefaultStatistics): MutableCollection<Optional<PlayerGameStats>> {
        return getOrderedBy(statistic.toString())
    }

    /**
     * @param statistic Order collection by given statistic.
     * @return top list.
     */
    fun getOrderedBy(statistic: String): MutableList<Optional<PlayerGameStats>>

    /**
     * Check if given statistic is registered.
     */
    fun hasStatistic(orderBy: String): Boolean

    /**
     * @return unmodifiable list of registered game statistics.
     */
    fun getRegistered(): MutableList<String>

    /**
     * Get statistic provider.
     */
    fun getProvider(registered: String): GameStatisticProvider<*>?
}
