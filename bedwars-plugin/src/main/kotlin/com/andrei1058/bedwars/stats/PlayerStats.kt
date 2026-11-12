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

import java.time.Instant
import java.util.UUID
import com.andrei1058.bedwars.api.util.uninitialized

open class PlayerStats(private val uuid: UUID) {
    private var name: String = uninitialized()
    private var firstPlay: Instant = uninitialized()
    private var lastPlay: Instant = uninitialized()
    private var wins: Int = 0
    private var kills: Int = 0
    private var finalKills: Int = 0
    private var totalKills: Int = 0
    private var losses: Int = 0
    private var deaths: Int = 0
    private var finalDeaths: Int = 0
    private var bedsDestroyed: Int = 0
    private var gamesPlayed: Int = 0

    open fun getName(): String {
        return name
    }

    open fun setName(name: String) {
        this.name = name
    }

    open fun getUuid(): UUID {
        return uuid
    }

    open fun getFirstPlay(): Instant {
        return firstPlay
    }

    open fun setFirstPlay(firstPlay: Instant) {
        this.firstPlay = firstPlay
    }

    open fun getLastPlay(): Instant {
        return lastPlay
    }

    open fun setLastPlay(lastPlay: Instant) {
        this.lastPlay = lastPlay
    }

    open fun getWins(): Int {
        return wins
    }

    open fun setWins(wins: Int) {
        this.wins = wins
    }

    open fun getKills(): Int {
        return kills
    }

    open fun setKills(kills: Int) {
        this.kills = kills
        this.totalKills = kills + finalKills
    }

    open fun getFinalKills(): Int {
        return finalKills
    }

    open fun setFinalKills(finalKills: Int) {
        this.finalKills = finalKills
        this.totalKills = kills + finalKills
    }

    open fun getLosses(): Int {
        return losses
    }

    open fun setLosses(losses: Int) {
        this.losses = losses
    }

    open fun getDeaths(): Int {
        return deaths
    }

    open fun setDeaths(deaths: Int) {
        this.deaths = deaths
    }

    open fun getFinalDeaths(): Int {
        return finalDeaths
    }

    open fun setFinalDeaths(finalDeaths: Int) {
        this.finalDeaths = finalDeaths
    }

    open fun getBedsDestroyed(): Int {
        return bedsDestroyed
    }

    open fun setBedsDestroyed(bedsDestroyed: Int) {
        this.bedsDestroyed = bedsDestroyed
    }

    open fun getGamesPlayed(): Int {
        return gamesPlayed
    }

    open fun setGamesPlayed(gamePlayed: Int) {
        this.gamesPlayed = gamePlayed
    }

    open fun getTotalKills(): Int {
        return totalKills
    }
}
