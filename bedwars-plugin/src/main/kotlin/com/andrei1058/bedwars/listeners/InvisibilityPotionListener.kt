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

import com.andrei1058.bedwars.BedWars.Companion.nms
import com.andrei1058.bedwars.BedWars.Companion.plugin
import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.api.arena.team.ITeam
import com.andrei1058.bedwars.api.events.player.PlayerInvisibilityPotionEvent
import com.andrei1058.bedwars.arena.Arena
import com.andrei1058.bedwars.sidebar.SidebarService
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import com.andrei1058.bedwars.api.util.unchecked

/**
 * This is used to hide and show player name tag above head when he drinks an invisibility
 * potion or when the potion is gone. It is required because it is related to scoreboards.
 */
open class InvisibilityPotionListener : Listener {
    @EventHandler
    open fun onPotion(e: PlayerInvisibilityPotionEvent) {
        if (e.getTeam() == null) {
            return
        }
        SidebarService.getInstance()!!.handleInvisibility(e.getTeam(), e.getPlayer(), e.getType() == PlayerInvisibilityPotionEvent.Type.ADDED)
    }

    @EventHandler
    open fun onDrink(e: PlayerItemConsumeEvent) {
        val a: IArena? = Arena.getArenaByPlayer(e.getPlayer())
        if (a == null) {
            return
        }
        if (e.getItem().getType() != Material.POTION) {
            return
        }
        // remove potion bottle
        Bukkit.getScheduler().runTaskLater(plugin, Runnable { nms.minusAmount(e.getPlayer(), ItemStack(Material.GLASS_BOTTLE), 1) }, 5L)
        if (nms.isInvisibilityPotion(e.getItem())) {
            Bukkit.getScheduler().runTaskLater(plugin, Runnable {
                for (pe in e.getPlayer().getActivePotionEffects()) {
                    if (pe.getType().toString().contains("INVISIBILITY")) {
                        // if is already invisible
                        if (a.getShowTime()!!.containsKey(e.getPlayer())) {
                            val t = a.getTeam(e.getPlayer())
                            // because potions do not hide armors
                            a.getShowTime()!!.replace(e.getPlayer(), pe.getDuration() / 20)
                            // call custom event
                            Bukkit.getPluginManager().callEvent(PlayerInvisibilityPotionEvent(PlayerInvisibilityPotionEvent.Type.ADDED, t.unchecked(), e.getPlayer(), t!!.getArena().unchecked()))
                            // increase invisibility timer
                            // keep trace of invisible players to send hide armor packet when required
                        } else {
                            // if not already invisible
                            val t = a.getTeam(e.getPlayer())
                            // because potions do not hide armors
                            a.getShowTime()!!.put(e.getPlayer(), pe.getDuration() / 20)
                            //
                            for (p1 in e.getPlayer().getWorld().getPlayers()) {
                                if (a.isSpectator(p1)) {
                                    // hide player armor to spectators
                                    nms.hideArmor(e.getPlayer(), p1)
                                } else if (t !== a.getTeam(p1)) {
                                    // hide player armor to other teams
                                    nms.hideArmor(e.getPlayer(), p1)
                                }
                            }
                            // call custom event
                            Bukkit.getPluginManager().callEvent(PlayerInvisibilityPotionEvent(PlayerInvisibilityPotionEvent.Type.ADDED, t.unchecked(), e.getPlayer(), t!!.getArena().unchecked()))
                            // keep trace of invisible players to send hide armor packet when required
                        }
                        break
                    }
                }
            }, 5L)
        }
    }
}
