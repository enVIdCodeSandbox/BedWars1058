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

package com.andrei1058.bedwars.api.events.player

import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.api.arena.team.ITeam
import java.util.function.Function
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

open class PlayerBedBreakEvent(p: Player, private val playerTeam: ITeam, private val victimTeam: ITeam, private val arena: IArena, private var message: Function<Player, String?>, private var title: Function<Player, String?>, private var subTitle: Function<Player, String?>) : Event() {
    private val player: Player

    /**
     * Get the player team.
     */
    open fun getPlayerTeam(): ITeam {
        return playerTeam
    }

    /**
     * Get the team who got the bed destroyed.
     */
    open fun getVictimTeam(): ITeam {
        return victimTeam
    }

    open fun getArena(): IArena {
        return arena
    }

    open fun getPlayer(): Player {
        return player
    }

    /**
     * Get chat message.
     */
    open fun getMessage(): Function<Player, String?> {
        return message
    }

    /**
     * Set chat message.
     */
    open fun setMessage(message: Function<Player, String?>) {
        this.message = message
    }

    override fun getHandlers(): HandlerList {
        return HANDLERS
    }

    open fun getTitle(): Function<Player, String?> {
        return title
    }

    open fun setTitle(title: Function<Player, String?>) {
        this.title = title
    }

    open fun getSubTitle(): Function<Player, String?> {
        return subTitle
    }

    open fun setSubTitle(subTitle: Function<Player, String?>) {
        this.subTitle = subTitle
    }

    init {
        this.player = p
    }

    companion object {
        private val HANDLERS: HandlerList = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return HANDLERS
        }
    }
}
