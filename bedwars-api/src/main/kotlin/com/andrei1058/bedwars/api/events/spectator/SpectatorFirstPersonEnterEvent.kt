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
import java.util.ArrayList
import java.util.UUID
import java.util.function.Function
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

open class SpectatorFirstPersonEnterEvent(private val spectator: Player, private val target: Player, private val arena: IArena, private var title: Function<Player, String?>, subtitle: Function<Player, String?>) : Event(), Cancellable {
    private var cancelled: Boolean = false
    private var subTitle: Function<Player, String?>
    private var fadeIn: Int = 0
    private var stay: Int = 40
    private var fadeOut: Int = 10

    /**
     * Get the spectator
     */
    open fun getSpectator(): Player {
        return spectator
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
        return target
    }

    /**
     * Check if it is cancelled
     */
    override fun isCancelled(): Boolean {
        return cancelled
    }

    /**
     * Change value
     */
    override fun setCancelled(cancelled: Boolean) {
        this.cancelled = cancelled
    }

    open fun getSubTitle(): Function<Player, String?> {
        return subTitle
    }

    open fun getTitle(): Function<Player, String?> {
        return title
    }

    /**
     * Get first person enter title
     */
    open fun setTitle(title: Function<Player, String?>) {
        this.title = title
    }

    /**
     * Set first person enter title and subtitle. Leave "" for empty msg
     */
    open fun setSubTitle(subTitle: Function<Player, String?>) {
        this.subTitle = subTitle
    }

    open fun getStay(): Int {
        return stay
    }

    open fun getFadeOut(): Int {
        return fadeOut
    }

    open fun getFadeIn(): Int {
        return fadeIn
    }

    open fun setStay(stay: Int) {
        if (stay < 0) {
            return
        }
        this.stay = stay
    }

    open fun setFadeOut(fadeOut: Int) {
        if (fadeOut < 0) {
            return
        }
        this.fadeOut = fadeOut
    }

    open fun setFadeIn(fadeIn: Int) {
        if (fadeIn < 0) {
            return
        }
        this.fadeIn = fadeIn
    }

    override fun getHandlers(): HandlerList {
        return HANDLERS
    }

    init {
        this.subTitle = subtitle
        if (!spectatingInFirstPerson.contains(spectator.getUniqueId())) {
            spectatingInFirstPerson.add(spectator.getUniqueId())
        }
    }

    companion object {
        private val HANDLERS: HandlerList = HandlerList()
        // A list of all players spectating in first person
        private var spectatingInFirstPerson: MutableList<UUID> = ArrayList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return HANDLERS
        }
    }
}
