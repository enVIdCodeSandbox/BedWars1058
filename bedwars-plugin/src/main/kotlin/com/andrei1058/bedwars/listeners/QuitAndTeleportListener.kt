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

import com.andrei1058.bedwars.BedWars
import com.andrei1058.bedwars.BedWars.Companion.config
import com.andrei1058.bedwars.BedWars.Companion.getParty
import com.andrei1058.bedwars.BedWars.Companion.getRemoteDatabase
import com.andrei1058.bedwars.BedWars.Companion.getServerType
import com.andrei1058.bedwars.BedWars.Companion.plugin
import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.api.configuration.ConfigPath
import com.andrei1058.bedwars.api.language.Language
import com.andrei1058.bedwars.api.server.ServerType
import com.andrei1058.bedwars.arena.Arena
import com.andrei1058.bedwars.arena.LastHit
import com.andrei1058.bedwars.arena.SetupSession
import com.andrei1058.bedwars.arena.team.BedWarsTeam
import com.andrei1058.bedwars.commands.bedwars.subcmds.regular.CmdStats
import com.andrei1058.bedwars.sidebar.SidebarService
import java.util.UUID
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerQuitEvent
import com.andrei1058.bedwars.api.util.unchecked

open class QuitAndTeleportListener : Listener {
    @EventHandler
    open fun onLeave(e: PlayerQuitEvent) {
        val p = e.getPlayer()
        // Remove from arena
        val a: IArena? = Arena.getArenaByPlayer(p)
        if (a != null) {
            if (a.isPlayer(p)) {
                a.removePlayer(p, true)
            } else if (a.isSpectator(p)) {
                a.removeSpectator(p, true)
            }
        }
        //Save preferred language
        if (Language.getLangByPlayer().containsKey(p.getUniqueId())) {
            val u = p.getUniqueId()
            Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
                var iso = Language.getLangByPlayer().get(p.getUniqueId())!!.getIso()
                if (Language.isLanguageExist(iso.unchecked())) {
                    if (BedWars.config.getYml().getStringList(ConfigPath.GENERAL_CONFIGURATION_DISABLED_LANGUAGES).contains(iso)) {
                        iso = Language.getDefaultLanguage().getIso()
                    }
                    BedWars.getRemoteDatabase().setLanguage(u, iso.unchecked())
                }
                Language.getLangByPlayer().remove(p.getUniqueId())
            })
        }
        if (getServerType() != ServerType.SHARED) {
            e.setQuitMessage(null)
        }
        // Manage internal parties
        if (getParty().isInternal()) {
            if (getParty().hasParty(p)) {
                getParty().removeFromParty(p)
            }
        }
        // Check if was doing a setup and remove the session
        val ss: SetupSession? = SetupSession.getSession(p.getUniqueId())
        if (ss != null) {
            ss.cancel()
        }
        SidebarService.getInstance()!!.remove(e.getPlayer())
        BedWarsTeam.reSpawnInvulnerability.remove(e.getPlayer().getUniqueId())
        val lh: LastHit? = LastHit.getLastHit(p)
        if (lh != null) {
            lh.remove()
        }
        CmdStats.getStatsCoolDown().remove(e.getPlayer().getUniqueId())
    }

    /**
     * Handle players teleported outside.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    open fun onWorldChange(e: PlayerChangedWorldEvent) {
        // if player was teleported outside arena
        val arena: IArena? = Arena.getArenaByPlayer(e.getPlayer())
        if (null == arena) {
            return
        }
        if (e.getPlayer().getWorld().getName() == arena.getWorldName()) {
            return
        }
        if (arena.isPlayer(e.getPlayer())) {
            // it will teleport you to the lobby world or cached location
            arena.removePlayer(e.getPlayer(), false)
        }
        if (arena.isSpectator(e.getPlayer())) {
            // it will teleport you to the lobby world or cached location
            arena.removeSpectator(e.getPlayer(), false)
        }
    }
}
