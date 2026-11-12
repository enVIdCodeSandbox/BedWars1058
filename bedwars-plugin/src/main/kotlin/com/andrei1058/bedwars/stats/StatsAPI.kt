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

import com.andrei1058.bedwars.api.BedWars
import java.sql.Timestamp
import java.time.Instant
import java.util.UUID

open class StatsAPI private constructor() : BedWars.IStats {
    private fun getData(uuid: UUID): PlayerStats {
        var stats: PlayerStats? = com.andrei1058.bedwars.BedWars.getStatsManager().getUnsafe(uuid)
        if (stats == null) {
            stats = com.andrei1058.bedwars.BedWars.getRemoteDatabase().fetchStats(uuid)
        }
        return stats
    }

    override fun getPlayerFirstPlay(p: UUID): Timestamp? {
        val firstPlay: Instant? = getData(p).getFirstPlay()
        if (firstPlay == null) {
            return null
        }
        return Timestamp.from(firstPlay)
    }

    override fun getPlayerLastPlay(p: UUID): Timestamp? {
        val lastPlay: Instant? = getData(p).getLastPlay()
        if (lastPlay == null) {
            return null
        }
        return Timestamp.from(lastPlay)
    }

    override fun getPlayerWins(p: UUID): Int {
        return getData(p).getWins()
    }

    override fun getPlayerKills(p: UUID): Int {
        return getData(p).getKills()
    }

    override fun getPlayerTotalKills(p: UUID): Int {
        return getData(p).getTotalKills()
    }

    override fun getPlayerFinalKills(p: UUID): Int {
        return getData(p).getFinalKills()
    }

    override fun getPlayerLoses(p: UUID): Int {
        return getData(p).getLosses()
    }

    override fun getPlayerDeaths(p: UUID): Int {
        return getData(p).getDeaths()
    }

    override fun getPlayerFinalDeaths(p: UUID): Int {
        return getData(p).getFinalDeaths()
    }

    override fun getPlayerBedsDestroyed(p: UUID): Int {
        return getData(p).getBedsDestroyed()
    }

    override fun getPlayerGamesPlayed(p: UUID): Int {
        return getData(p).getGamesPlayed()
    }

    companion object {
        private var instance: StatsAPI? = null

        @JvmStatic
        fun getInstance(): StatsAPI? {
            if (instance == null) {
                instance = StatsAPI()
            }
            return instance
        }
    }
}
