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

open class PlayerKillEvent : Event {
    private val arena: IArena
    private val victim: Player
    private var victimTeam: ITeam?
    private val killer: Player?
    private var killerTeam: ITeam? = null
    private val cause: PlayerKillCause
    private var message: Function<Player, String?>?
    private var playSound: Boolean = true

    /**
     * Called when a Player got killed during the game.
     *
     * @param killer can be NULL.
     */
    @Deprecated("")
    constructor(arena: IArena, victim: Player, killer: Player?, message: Function<Player, String?>, cause: PlayerKillCause) : super() {
        victimTeam = arena.getTeam(victim)
        if (null == victimTeam) {
            victimTeam = arena.getExTeam(victim.getUniqueId())
        }
        if (null != killer) {
            killerTeam = arena.getTeam(killer)
            if (null == killerTeam) {
                killerTeam = arena.getExTeam(killer.getUniqueId())
            }
        }
        this.arena = arena
        this.victim = victim
        this.killer = killer
        this.message = message
        this.cause = cause
    }

    constructor(arena: IArena, victim: Player, victimTeam: ITeam?, killer: Player?, killerTeam: ITeam?, message: Function<Player, String?>?, cause: PlayerKillCause) : super() {
        this.arena = arena
        this.victim = victim
        this.killer = killer
        this.message = message
        this.cause = cause
        this.victimTeam = victimTeam
        this.killerTeam = killerTeam
    }

    enum class PlayerKillCause(@JvmField val finalKill: Boolean, @JvmField val despawnable: Boolean, @JvmField val pvpLogOut: Boolean) {
        UNKNOWN(false, false, false),
        UNKNOWN_FINAL_KILL(true, false, false),
        EXPLOSION(false, false, false),
        EXPLOSION_FINAL_KILL(true, false, false),
        VOID(false, false, false),
        VOID_FINAL_KILL(true, false, false),
        PVP(false, false, false),
        PVP_FINAL_KILL(true, false, false),
        PLAYER_SHOOT(false, false, false),
        PLAYER_SHOOT_FINAL_KILL(true, false, false),
        SILVERFISH(false, true, false),
        SILVERFISH_FINAL_KILL(true, true, false),
        IRON_GOLEM(false, true, false),
        IRON_GOLEM_FINAL_KILL(true, true, false),
        PLAYER_PUSH(false, false, false),
        /**
         * Corresponds to FALL on ground.
         */
        PLAYER_PUSH_FINAL(true, false, false),
        PLAYER_DISCONNECT(false, false, true),
        PLAYER_DISCONNECT_FINAL(true, false, true);

        fun isFinalKill(): Boolean {
            return finalKill
        }

        /**
         * @return true if killed by a player's ironGolem, silverfish etc.
         */
        fun isDespawnable(): Boolean {
            return despawnable
        }

        fun isPvpLogOut(): Boolean {
            return pvpLogOut
        }
    }

    /**
     * Killer can be NULL (void etc.)
     */
    open fun getKiller(): Player? {
        return killer
    }

    /**
     * Get kill chat message.
     */
    open fun getMessage(): Function<Player, String?>? {
        return message
    }

    /**
     * Set chat message.
     */
    open fun setMessage(message: Function<Player, String?>?) {
        this.message = message
    }

    /**
     * Get kill cause
     */
    open fun getCause(): PlayerKillCause {
        return cause
    }

    open fun getArena(): IArena {
        return arena
    }

    /**
     * Get the Player who died.
     */
    open fun getVictim(): Player {
        return victim
    }

    /**
     * Checks if the killer gets the kill sound
     */
    open fun playSound(): Boolean {
        return playSound
    }

    /**
     * Set if the killer should get the kill sound
     */
    open fun setPlaySound(playSound: Boolean) {
        this.playSound = playSound
    }

    open fun getKillerTeam(): ITeam? {
        return killerTeam
    }

    open fun getVictimTeam(): ITeam? {
        return victimTeam
    }

    open fun setKillerTeam(killerTeam: ITeam) {
        this.killerTeam = killerTeam
    }

    open fun setVictimTeam(victimTeam: ITeam) {
        this.victimTeam = victimTeam
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
