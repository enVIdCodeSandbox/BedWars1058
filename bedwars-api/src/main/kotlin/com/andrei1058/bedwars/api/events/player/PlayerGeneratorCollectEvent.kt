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
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import org.bukkit.inventory.ItemStack

open class PlayerGeneratorCollectEvent(private val player: Player, private val item: Item, private val arena: IArena) : Event() {
    private var cancelled: Boolean = false

    open fun getArena(): IArena {
        return arena
    }

    /**
     * Get the player
     */
    open fun getPlayer(): Player {
        return player
    }

    /**
     * Get the item entity involved
     */
    open fun getItem(): Item {
        return item
    }

    /**
     * Get the itemStack involved
     */
    open fun getItemStack(): ItemStack {
        return item.getItemStack()
    }

    /**
     * Cancel this event
     */
    open fun isCancelled(): Boolean {
        return cancelled
    }

    open fun setCancelled(cancelled: Boolean) {
        this.cancelled = cancelled
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
