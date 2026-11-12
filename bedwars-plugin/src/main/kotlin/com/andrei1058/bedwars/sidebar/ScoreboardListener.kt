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

package com.andrei1058.bedwars.sidebar

import com.andrei1058.bedwars.BedWars
import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.api.events.player.*
import com.andrei1058.bedwars.api.server.ServerType
import com.andrei1058.bedwars.arena.Arena
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityRegainHealthEvent
import org.bukkit.event.player.PlayerJoinEvent

open class ScoreboardListener : Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    open fun onPlayerDamage(e: EntityDamageEvent) {
        if (e.getEntity() !is Player) {
            return
        }
        val player = e.getEntity() as Player
        val arena: IArena? = Arena.getArenaByPlayer(player)
        if (arena == null) {
            return
        }
        val health: Int = Math.ceil((player.getHealth() - e.getFinalDamage())).toInt()
        SidebarService.getInstance()!!.refreshHealth(arena, player, health)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    open fun onRegain(e: EntityRegainHealthEvent) {
        if (e.getEntity() !is Player) {
            return
        }
        val player = e.getEntity() as Player
        val arena: IArena? = Arena.getArenaByPlayer(player)
        if (arena == null) {
            return
        }
        val health: Int = Math.ceil(player.getHealth() + e.getAmount()).toInt()
        SidebarService.getInstance()!!.refreshHealth(arena, player, health)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    open fun onReSpawn(e: PlayerReSpawnEvent) {
        val arena = e.getArena()
        SidebarService.getInstance()!!.refreshHealth(arena, e.getPlayer(), Math.ceil(e.getPlayer().getHealth()).toInt())
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    open fun reJoin(e: PlayerReJoinEvent) {
        // re-add player to scoreboard tab list
        SidebarService.getInstance()!!.handleReJoin(e.getArena(), e.getPlayer())
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    open fun arenaJoin(e: PlayerJoinArenaEvent) {
        // add player to scoreboard tab list
        SidebarService.getInstance()!!.handleJoin(e.getArena(), e.getPlayer(), e.isSpectator())
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    open fun serverJoin(e: PlayerJoinEvent) {
        if (BedWars.getServerType() == ServerType.MULTIARENA || BedWars.getServerType() == ServerType.SHARED) {
            // add player to scoreboard tab list
            SidebarService.getInstance()!!.applyLobbyTab(e.getPlayer())
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    open fun arenaLeave(e: PlayerLeaveArenaEvent) {
        if (BedWars.getServerType() == ServerType.MULTIARENA || BedWars.getServerType() == ServerType.SHARED) {
            // add player to scoreboard tab list
            SidebarService.getInstance()!!.applyLobbyTab(e.getPlayer())
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    open fun onBedDestroy(e: PlayerBedBreakEvent) {
        // refresh placeholders in case placeholders refresh is disabled
        SidebarService.getInstance()!!.refreshPlaceholders(e.getArena())
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    open fun onFinalKill(e: PlayerKillEvent) {
        if (!e.getCause().isFinalKill()) {
            return
        }
        // refresh placeholders in case placeholders refresh is disabled
        SidebarService.getInstance()!!.refreshPlaceholders(e.getArena())
    }
}
