package com.andrei1058.bedwars.arena.stats.defaults

import com.andrei1058.bedwars.api.arena.stats.GameStatistic
import com.andrei1058.bedwars.api.arena.stats.PlayerGameStats
import java.util.HashMap
import java.util.Optional
import java.util.UUID
import java.util.stream.Collectors
import org.bukkit.Bukkit
import org.bukkit.entity.Player

open class PlayerGameStatsContainer(player: Player) : PlayerGameStats {
    private val player: UUID
    private val username: String
    // last tracked display name
    private val lastDisplayName: String
    private val statsById: HashMap<String, Optional<GameStatistic<*>>> = HashMap()

    override fun getPlayer(): UUID {
        return player
    }

    override fun getDisplayPlayer(): String {
        val online: Player? = Bukkit.getPlayer(getPlayer())
        if (null == online) {
            return lastDisplayName
        }
        return online.getDisplayName()
    }

    override fun getUsername(): String {
        return username
    }

    override fun registerStatistic(id: String, defaultValue: GameStatistic<*>) {
        if (statsById.containsKey(id)) {
            throw RuntimeException("Statistic " + id + " already registered for player " + getPlayer())
        }
        statsById.put(id, Optional.of(defaultValue))
    }

    override fun getStatistic(id: String): Optional<GameStatistic<*>>? {
        return statsById.getOrDefault(id, Optional.empty())
    }

    override fun getRegistered(): MutableList<String> {
        return statsById.keys.stream().collect(Collectors.toUnmodifiableList())
    }

    init {
        this.player = player.getUniqueId()
        this.username = player.getName()
        this.lastDisplayName = player.getDisplayName()
    }
}
