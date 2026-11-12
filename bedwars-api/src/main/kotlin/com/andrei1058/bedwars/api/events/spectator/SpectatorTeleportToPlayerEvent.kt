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

package com.andrei1058.bedwars.api.events.spectator

import com.andrei1058.bedwars.api.arena.IArena
import java.util.UUID
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

open class SpectatorTeleportToPlayerEvent(spectator: Player, target: Player, arena: IArena) : Event() {
    private var spectator: UUID
    private var target: UUID
    private var arena: IArena
    private var cancelled: Boolean = false

    /**
     * Get the spectator
     */
    open fun getSpectator(): Player {
        return Bukkit.getPlayer(spectator)
    }

    /**
     * Get the arena
     */
    open fun getArena(): IArena {
        return arena
    }

    /**
     * Get the target player
     */
    open fun getTarget(): Player {
        return Bukkit.getPlayer(target)
    }

    /**
     * Check if it is cancelled
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

    init {
        this.spectator = spectator.getUniqueId()
        this.target = target.getUniqueId()
        this.arena = arena
    }

    companion object {
        private val HANDLERS: HandlerList = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return HANDLERS
        }
    }
}
