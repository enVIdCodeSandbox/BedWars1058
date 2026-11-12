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
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

open class PlayerLeaveArenaEvent : Event {
    private var player: Player
    private var spectator: Boolean
    private var arena: IArena
    private var lastDamager: Player?

    /**
     * Called when a player leaves the arena.
     * This is called if the arena has ended as well.
     */
    constructor(p: Player, arena: IArena, lastDamager: Player?) : super() {
        this.player = p
        this.spectator = arena.isSpectator(p)
        this.arena = arena
        this.lastDamager = lastDamager
    }

    @Deprecated("")
    constructor(p: Player, arena: IArena) : this(p, arena, null)

    open fun getPlayer(): Player {
        return player
    }

    /**
     * Get the arena
     */
    open fun getArena(): IArena {
        return arena
    }

    /**
     * Check if the player was spectating.
     */
    open fun isSpectator(): Boolean {
        return spectator
    }

    /**
     * Get the player's last damager, might be null
     */
    open fun getLastDamager(): Player? {
        return lastDamager
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
