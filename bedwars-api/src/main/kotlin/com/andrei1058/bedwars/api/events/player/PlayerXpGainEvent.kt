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

import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

open class PlayerXpGainEvent(private var player: Player, private var amount: Int, private var xpSource: XpSource) : Event() {
    /**
     * Get the player that have received new xp.
     */
    open fun getPlayer(): Player {
        return player
    }

    /**
     * Get the amount of xp received.
     */
    open fun getAmount(): Int {
        return amount
    }

    /**
     * Get xp source
     */
    open fun getXpSource(): XpSource {
        return xpSource
    }

    /**
     * Lets you know why did the player received new xp.
     */
    enum class XpSource {
        PER_MINUTE,
        PER_TEAMMATE,
        GAME_WIN,
        BED_DESTROYED,
        FINAL_KILL,
        REGULAR_KILL,
        OTHER;

    }

    override fun getHandlers(): HandlerList {
        return HANDLERS
    }

    companion object {
        private val HANDLERS: HandlerList = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return HANDLERS
        }
    }
}
