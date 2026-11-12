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
import com.andrei1058.bedwars.api.arena.shop.ICategoryContent
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

open class ShopBuyEvent : Event, Cancellable {
    private val buyer: Player
    private val arena: IArena?
    private val categoryContent: ICategoryContent
    private var cancelled: Boolean = false

    /**
     * Triggered when a player buys from the shop
     *
     * @deprecated Use {@link #ShopBuyEvent(Player, IArena, ICategoryContent)}
     */
    @Deprecated("")
    constructor(buyer: Player, categoryContent: ICategoryContent) : super() {
        this.categoryContent = categoryContent
        this.buyer = buyer
        this.arena = null
    }

    /**
     * Triggered when a player buys from the shop
     */
    constructor(buyer: Player, arena: IArena, categoryContent: ICategoryContent) : super() {
        this.categoryContent = categoryContent
        this.buyer = buyer
        this.arena = arena
    }

    open fun getArena(): IArena? {
        return arena
    }

    /**
     * Get the buyer
     */
    open fun getBuyer(): Player {
        return buyer
    }

    /**
     * Get the shop category content bought.
     */
    open fun getCategoryContent(): ICategoryContent {
        return categoryContent
    }

    override fun getHandlers(): HandlerList {
        return HANDLERS
    }

    override fun isCancelled(): Boolean {
        return cancelled
    }

    override fun setCancelled(cancelled: Boolean) {
        this.cancelled = cancelled
    }

    companion object {
        private val HANDLERS: HandlerList = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return HANDLERS
        }
    }
}
