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

package com.andrei1058.bedwars.listeners.joinhandler

import com.andrei1058.bedwars.BedWars
import com.andrei1058.bedwars.BedWars.Companion.config
import com.andrei1058.bedwars.BedWars.Companion.mainCmd
import com.andrei1058.bedwars.BedWars.Companion.nms
import com.andrei1058.bedwars.BedWars.Companion.plugin
import com.andrei1058.bedwars.arena.Arena
import com.andrei1058.bedwars.arena.ReJoin
import com.andrei1058.bedwars.sidebar.SidebarService
import com.andrei1058.bedwars.support.paper.TeleportManager
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

open class JoinListenerMultiArena : Listener {
    @EventHandler(priority = EventPriority.HIGH)
    open fun onJoin(e: PlayerJoinEvent) {
        e.setJoinMessage(null)
        val p = e.getPlayer()
        p.getInventory().setArmorContents(null)
        JoinHandlerCommon.displayCustomerDetails(p)
        // Show commands if player is op and there is no set arenas
        if (p.isOp()) {
            if (Arena.getArenas().isEmpty()) {
                p.performCommand(mainCmd)
            }
        }
        val reJoin: ReJoin? = ReJoin.getPlayer(p)
        Bukkit.getScheduler().runTaskLater(plugin, Runnable lambda1@{
            // Players from lobby will remain visible
            for (online in Bukkit.getOnlinePlayers()) {
                if (Arena.isInArena(online)) {
                    BedWars.nms.spigotHidePlayer(online, p)
                    BedWars.nms.spigotHidePlayer(p, online)
                } else {
                    BedWars.nms.spigotShowPlayer(online, p)
                    BedWars.nms.spigotShowPlayer(p, online)
                }
            }
            // To prevent invisibility issues handle ReJoin after sending invisibility packets
            if (reJoin != null) {
                if (reJoin.canReJoin()) {
                    reJoin.reJoin(p)
                    return@lambda1
                }
                reJoin.destroy(false)
            }
            // Hide new player to players and spectators, and vice versa
        }, 14L)
        if (reJoin != null && reJoin.canReJoin()) {
            return
        }
        // Teleport to lobby location
        val lobbyLocation: Location? = config.getConfigLoc("lobbyLoc")
        if (lobbyLocation != null && lobbyLocation.getWorld() != null) {
            TeleportManager.teleport(p, lobbyLocation)
        }
        // Send items
        Arena.sendLobbyCommandItems(p)
        SidebarService.getInstance()!!.giveSidebar(p, null, true)
        p.setHealthScale(p.getMaxHealth())
        p.setExp(0f)
        p.setHealthScale(20.0)
        p.setFoodLevel(20)
    }
}
