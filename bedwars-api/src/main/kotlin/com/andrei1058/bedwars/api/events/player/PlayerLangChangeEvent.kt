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

open class PlayerLangChangeEvent(p: Player, private var oldLang: String, private var newLang: String) : Event() {
    private var cancelled: Boolean = false
    private var player: Player

    /**
     * Check if event is cancelled
     */
    open fun isCancelled(): Boolean {
        return cancelled
    }

    /**
     * Change value
     */
    open fun setCancelled(cancelled: Boolean) {
        this.cancelled = cancelled
    }

    /**
     * Get Player
     */
    open fun getPlayer(): Player {
        return player
    }

    /**
     * Get old Language iso
     */
    open fun getOldLang(): String {
        return oldLang
    }

    /**
     * Get new Language iso
     */
    open fun getNewLang(): String {
        return newLang
    }

    override fun getHandlers(): HandlerList {
        return HANDLERS
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
