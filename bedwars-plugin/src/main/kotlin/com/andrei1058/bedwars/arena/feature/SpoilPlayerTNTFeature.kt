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

package com.andrei1058.bedwars.arena.feature

import com.andrei1058.bedwars.BedWars
import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.api.configuration.ConfigPath
import com.andrei1058.bedwars.api.events.player.PlayerKillEvent
import com.andrei1058.bedwars.api.events.player.PlayerLeaveArenaEvent
import com.andrei1058.bedwars.arena.Arena
import com.andrei1058.bedwars.metrics.MetricsManager
import java.util.LinkedList
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerPickupItemEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffectType

open class SpoilPlayerTNTFeature private constructor() {
    private val playersWithTnt: LinkedList<Player> = LinkedList()

    open class ParticleTask : Runnable {
        override fun run() {
            for (player in instance!!.playersWithTnt) {
                if (player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                    return
                }
                BedWars.nms.playRedStoneDot(player)
            }
        }
    }

    open class TNTListener : Listener {
        @EventHandler
        open fun onDie(event: PlayerKillEvent) {
            instance!!.playersWithTnt.remove(event.getVictim())
        }

        @EventHandler
        open fun onLeave(event: PlayerLeaveArenaEvent) {
            instance!!.playersWithTnt.remove(event.getPlayer())
        }

        @EventHandler(ignoreCancelled = true)
        open fun onPickUp(event: PlayerPickupItemEvent) {
            if (event.getItem().getItemStack().getType() == Material.TNT) {
                val arena: IArena? = Arena.getArenaByPlayer(event.getPlayer())
                if (arena == null || !arena.isPlayer(event.getPlayer()) || arena.isSpectator(event.getPlayer())) {
                    return
                }
                if (instance!!.playersWithTnt.contains(event.getPlayer())) {
                    return
                }
                instance!!.playersWithTnt.add(event.getPlayer())
            }
        }

        @EventHandler(ignoreCancelled = true)
        open fun onDrop(event: PlayerDropItemEvent) {
            if (event.getItemDrop().getItemStack().getType() == Material.TNT) {
                val arena: IArena? = Arena.getArenaByPlayer(event.getPlayer())
                if (arena == null || !arena.isPlayer(event.getPlayer()) || arena.isSpectator(event.getPlayer())) {
                    return
                }
                if (!instance!!.playersWithTnt.contains(event.getPlayer())) {
                    return
                }
                if (event.getPlayer().getInventory().contains(Material.TNT)) {
                    return
                }
                instance!!.playersWithTnt.remove(event.getPlayer())
            }
        }

        @EventHandler(ignoreCancelled = true)
        open fun onPlace(event: BlockPlaceEvent) {
            val inHand = event.getItemInHand()
            val arena: IArena? = Arena.getArenaByPlayer(event.getPlayer())
            if (arena == null || !arena.isPlayer(event.getPlayer()) || arena.isSpectator(event.getPlayer())) {
                return
            }
            if (inHand.getType() == Material.TNT) {
                if (!instance!!.playersWithTnt.contains(event.getPlayer())) {
                    return
                }
                Bukkit.getScheduler().runTaskLater(BedWars.plugin, Runnable {
                    if (!event.getPlayer().getInventory().contains(Material.TNT)) {
                        instance!!.playersWithTnt.remove(event.getPlayer())
                    }
                }, 1L)
            }
        }

        @EventHandler(ignoreCancelled = true)
        open fun inventorySwitch(event: InventoryCloseEvent) {
            val player = event.getPlayer() as Player
            val arena: IArena? = Arena.getArenaByPlayer(player)
            // Ensure the player is in an arena, is not a spectator, and is indeed a player
            if (arena == null || !arena.isPlayer(player) || arena.isSpectator(player)) {
                return
            }
            val hasTnt: Boolean = player.getInventory().contains(Material.TNT)
            if (instance!!.playersWithTnt.contains(player)) {
                if (!hasTnt) {
                    instance!!.playersWithTnt.remove(player)
                }
            } else {
                if (hasTnt) {
                    instance!!.playersWithTnt.add(player)
                }
            }
        }
    }

    init {
        Bukkit.getPluginManager().registerEvents(TNTListener(), BedWars.plugin)
        Bukkit.getScheduler().runTaskTimer(BedWars.plugin, ParticleTask(), 20, 1L)
    }

    companion object {
        private var instance: SpoilPlayerTNTFeature? = null

        @JvmStatic
        fun init() {
            val enable = BedWars.config.getBoolean(ConfigPath.GENERAL_CONFIGURATION_PERFORMANCE_SPOIL_TNT_PLAYERS)
            if (enable) {
                if (instance == null) {
                    instance = SpoilPlayerTNTFeature()
                }
            }
            MetricsManager.appendPie("tnt_spoil_enable", { enable.toString() })
        }
    }
}
