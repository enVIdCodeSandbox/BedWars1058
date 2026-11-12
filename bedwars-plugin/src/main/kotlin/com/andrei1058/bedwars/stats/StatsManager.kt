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

package com.andrei1058.bedwars.stats

import com.andrei1058.bedwars.BedWars
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import org.bukkit.Bukkit

open class StatsManager {
    private val stats: MutableMap<UUID, PlayerStats> = ConcurrentHashMap()

    open fun remove(uuid: UUID) {
        stats.remove(uuid)
    }

    open fun put(uuid: UUID, playerStats: PlayerStats) {
        stats.put(uuid, playerStats)
    }

    open fun get(uuid: UUID): PlayerStats {
        val playerStats: PlayerStats? = stats.get(uuid)
        if (playerStats == null) {
            throw IllegalStateException("Trying to get stats data of an unloaded player!")
        }
        return playerStats
    }

    open fun getUnsafe(uuid: UUID): PlayerStats? {
        return stats.get(uuid)
    }

    /**
     * Register listeners related to stats cache.
     */
    private fun registerListeners() {
        Bukkit.getPluginManager().registerEvents(StatsListener(), BedWars.plugin)
    }

    init {
        registerListeners()
    }
}
