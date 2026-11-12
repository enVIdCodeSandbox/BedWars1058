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

package com.andrei1058.bedwars.listeners

import com.andrei1058.bedwars.api.events.gameplay.GameStateChangeEvent
import com.andrei1058.bedwars.api.events.player.PlayerJoinArenaEvent
import com.andrei1058.bedwars.api.events.player.PlayerLeaveArenaEvent
import com.andrei1058.bedwars.api.events.server.ArenaDisableEvent
import com.andrei1058.bedwars.api.events.server.ArenaEnableEvent
import com.andrei1058.bedwars.arena.ArenaGUI
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

open class RefreshGUI : Listener {
    @EventHandler
    open fun onGameStateChange(e: GameStateChangeEvent?) {
        if (e == null) {
            return
        }
        val size: Int = e.getArena().getPlayers()!!.size
        for (p in Bukkit.getOnlinePlayers()) {
            ArenaGUI.refreshInv(p, e.getArena(), size)
        }
    }

    @EventHandler
    open fun onPlayerJoinArena(e: PlayerJoinArenaEvent?) {
        if (e == null) {
            return
        }
        var size: Int = e.getArena().getPlayers()!!.size
        if (!e.isSpectator()) {
            size++
        }
        for (p in Bukkit.getOnlinePlayers()) {
            ArenaGUI.refreshInv(p, e.getArena(), size)
        }
    }

    @EventHandler
    open fun onPlayerLeaveArena(e: PlayerLeaveArenaEvent?) {
        if (e == null) {
            return
        }
        var size: Int = e.getArena().getPlayers()!!.size
        if (!e.isSpectator()) {
            size--
        }
        for (p in Bukkit.getOnlinePlayers()) {
            ArenaGUI.refreshInv(p, e.getArena(), size)
        }
    }

    @EventHandler
    open fun onArenaEnable(e: ArenaEnableEvent?) {
        if (e == null) {
            return
        }
        for (p in Bukkit.getOnlinePlayers()) {
            ArenaGUI.refreshInv(p, e.getArena(), 0)
        }
    }

    @EventHandler
    open fun onArenaDisable(e: ArenaDisableEvent) {
        for (p in Bukkit.getOnlinePlayers()) {
            ArenaGUI.refreshInv(p, null, 0)
        }
    }
}
