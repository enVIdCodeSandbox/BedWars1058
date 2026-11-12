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

package com.andrei1058.bedwars.arena

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import com.andrei1058.bedwars.api.util.unchecked

open class LastHit(victim: Player, damager: Entity, time: Long) {
    private var victim: UUID
    private var damager: Entity?
    private var time: Long

    open fun setTime(time: Long) {
        this.time = time
    }

    open fun setDamager(damager: Entity?) {
        this.damager = damager
    }

    open fun getDamager(): Entity? {
        return damager
    }

    open fun getVictim(): UUID {
        return victim
    }

    open fun remove() {
        lastHit.remove(victim)
    }

    open fun getTime(): Long {
        return time
    }

    init {
        this.victim = victim.getUniqueId()
        this.damager = damager
        this.time = time
        lastHit.put(victim.getUniqueId(), this)
    }

    companion object {
        private var lastHit: ConcurrentHashMap<UUID, LastHit> = ConcurrentHashMap()

        @JvmStatic
        fun getLastHit(player: Player): LastHit {
            return lastHit.getOrDefault(player.getUniqueId(), null).unchecked()
        }
    }
}
