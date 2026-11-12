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

package com.andrei1058.bedwars.api.events.upgrades

import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.api.arena.team.ITeam
import com.andrei1058.bedwars.api.upgrades.TeamUpgrade
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import com.andrei1058.bedwars.api.util.unchecked

open class UpgradeBuyEvent(private var teamUpgrade: TeamUpgrade, private var player: Player, private var team: ITeam) : Event(), Cancellable {
    private val arena: IArena
    private var cancelled: Boolean = false

    open fun getArena(): IArena {
        return arena
    }

    /**
     * Get who bought the upgrade
     */
    open fun getPlayer(): Player {
        return player
    }

    open fun getTeam(): ITeam {
        return team
    }

    open fun getTeamUpgrade(): TeamUpgrade {
        return teamUpgrade
    }

    override fun getHandlers(): HandlerList {
        return HANDLERS
    }

    override fun isCancelled(): Boolean {
        return cancelled
    }

    /**
     * Cancel event. Purchase will be cancelled.
     */
    override fun setCancelled(b: Boolean) {
        this.cancelled = b
    }

    init {
        this.arena = team.getArena().unchecked()
    }

    companion object {
        private val HANDLERS: HandlerList = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return HANDLERS
        }
    }
}
