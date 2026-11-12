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

package com.andrei1058.bedwars.api.events.shop

import com.andrei1058.bedwars.api.arena.IArena
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

open class ShopOpenEvent : Event {
    private var player: Player
    private val arena: IArena?
    private var cancelled: Boolean = false

    /**
     * Triggered when the shop NPS is clicked.
     *
     * @deprecated Use {@link #ShopOpenEvent(Player, IArena)}
     */
    @Deprecated("")
    constructor(player: Player) : super() {
        this.player = player
        this.arena = null
    }

    /**
     * Triggered when the shop NPS is clicked.
     */
    constructor(player: Player, arena: IArena) : super() {
        this.player = player
        this.arena = arena
    }

    open fun getArena(): IArena? {
        return arena
    }

    /**
     * Get the player who opened the shop.
     */
    open fun getPlayer(): Player {
        return player
    }

    open fun setCancelled(cancelled: Boolean) {
        this.cancelled = cancelled
    }

    open fun isCancelled(): Boolean {
        return cancelled
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
