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

package com.andrei1058.bedwars.api.events.gameplay

import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.api.arena.team.ITeam
import java.util.ArrayList
import java.util.UUID
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

open class GameEndEvent(arena: IArena, winners: MutableList<UUID>, losers: MutableList<UUID>, teamWinner: ITeam, aliveWinners: MutableList<UUID>) : Event() {
    private var winners: MutableList<UUID>
    private var losers: MutableList<UUID>
    private var aliveWinners: MutableList<UUID>
    private var teamWinner: ITeam
    private var arena: IArena

    /**
     * Get a list of winners including eliminated teammates
     */
    open fun getWinners(): MutableList<UUID> {
        return winners
    }

    /**
     * Get the winner team
     */
    open fun getTeamWinner(): ITeam {
        return teamWinner
    }

    /**
     * Get a list with people who played and didn't win.
     * This includes people who leaved the game etc.
     */
    open fun getLosers(): MutableList<UUID> {
        return losers
    }

    /**
     * Get the arena
     */
    open fun getArena(): IArena {
        return arena
    }

    /**
     * Get a list of winners.
     * Teammates killed by final kill excluded.
     */
    open fun getAliveWinners(): MutableList<UUID> {
        return aliveWinners
    }

    override fun getHandlers(): HandlerList {
        return HANDLERS
    }

    init {
        this.winners = ArrayList(winners)
        this.arena = arena
        this.losers = ArrayList(losers)
        this.teamWinner = teamWinner
        this.aliveWinners = ArrayList(aliveWinners)
    }

    companion object {
        private val HANDLERS: HandlerList = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return HANDLERS
        }
    }
}
