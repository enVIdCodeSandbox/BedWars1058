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

package com.andrei1058.bedwars.support.vipfeatures

import com.andrei1058.bedwars.BedWars
import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.api.arena.team.ITeam
import com.andrei1058.bedwars.api.events.player.PlayerJoinArenaEvent
import com.andrei1058.bedwars.api.server.ServerType
import com.andrei1058.vipfeatures.api.IVipFeatures
import com.andrei1058.vipfeatures.api.event.BlockChangeEvent
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.util.Vector

open class VipListeners(private val api: IVipFeatures) : Listener {
    @EventHandler
    open fun onServerJoin(e: PlayerJoinEvent) {
        if (BedWars.getServerType() == ServerType.MULTIARENA) {
            Bukkit.getScheduler().runTaskLater(BedWars.plugin, Runnable { api.givePlayerItemStack(e.getPlayer()) }, 10L)
        }
    }

    @EventHandler
    open fun onArenaJoin(e: PlayerJoinArenaEvent) {
        Bukkit.getScheduler().runTaskLater(BedWars.plugin, Runnable { api.givePlayerItemStack(e.getPlayer()) }, 10L)
    }

    @EventHandler
    open fun onBockChange(e: BlockChangeEvent) {
        if (BedWars.getAPI().getArenaUtil().getArenaByName(e.getLocation().getWorld().getName()) != null) {
            val a = BedWars.getAPI().getArenaUtil().getArenaByName(e.getLocation().getWorld().getName())
            for (t in a.getTeams()!!) {
                for (x in -1 until 2) {
                    for (z in -1 until 2) {
                        if (e.getLocation().getBlockX() == t.getBed()!!.getBlockX() && e.getLocation().getBlockY() == t.getBed()!!.getBlockY() && e.getLocation().getBlockZ() == t.getBed()!!.getBlockZ()) {
                            if (BedWars.nms.isBed(t.getBed()!!.clone().add(x.toDouble(), 0.0, z.toDouble()).getBlock().getType())) {
                                e.setCancelled(true)
                            }
                            return
                        }
                    }
                }
            }
            a.getPlaced()!!.add(Vector(e.getLocation().getBlockX(), e.getLocation().getBlockY(), e.getLocation().getBlockZ()))
        }
    }
}
