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

package com.andrei1058.bedwars.arena.despawnables

import com.andrei1058.bedwars.BedWars
import com.andrei1058.bedwars.api.arena.GameState
import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.arena.Arena
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityTargetLivingEntityEvent

open class TargetListener : Listener {
    @EventHandler
    open fun onTarget(e: EntityTargetLivingEntityEvent) {
        if (e.getTarget() !is Player) {
            return
        }
        val arena: IArena? = Arena.getArenaByIdentifier(e.getEntity().getWorld().getName())
        val p = e.getTarget() as Player
        if (arena == null) {
            return
        }
        if (!arena.isPlayer(p)) {
            e.setCancelled(true)
            return
        }
        if (arena.getStatus() != GameState.playing) {
            e.setCancelled(true)
            return
        }
        if (BedWars.nms.isDespawnable(e.getEntity())) {
            if (arena.getTeam(p) === BedWars.nms.getDespawnablesList().get(e.getEntity().getUniqueId())!!.getTeam()) {
                e.setCancelled(true)
            }
        }
    }
}
