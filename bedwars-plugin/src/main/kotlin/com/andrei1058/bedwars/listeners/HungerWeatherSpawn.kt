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

import com.andrei1058.bedwars.BedWars.Companion.config
import com.andrei1058.bedwars.BedWars.Companion.debug
import com.andrei1058.bedwars.BedWars.Companion.getServerType
import com.andrei1058.bedwars.BedWars.Companion.nms
import com.andrei1058.bedwars.BedWars.Companion.plugin
import com.andrei1058.bedwars.api.arena.GameState
import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.api.configuration.ConfigPath
import com.andrei1058.bedwars.api.server.ServerType
import com.andrei1058.bedwars.arena.Arena
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.entity.ItemSpawnEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.event.weather.WeatherChangeEvent

open class HungerWeatherSpawn : Listener {
    private val hungerWaiting: Boolean
    private val hungerIngame: Boolean

    @EventHandler
    open fun onFoodChange(e: FoodLevelChangeEvent) {
        if (e.isCancelled()) {
            return
        }
        val player = e.getEntity() as Player
        val arena: IArena? = Arena.getArenaByPlayer(player)
        // Dont cancel hunger for shared mode outside of arena
        if (arena == null && getServerType() == ServerType.SHARED) {
            return
        }
        // Cancel hunger in MULTIARENA lobby
        if (arena == null) {
            e.setCancelled(true)
            return
        }
        // Cancel hunger for spectators
        if (arena.isSpectator(player)) {
            e.setCancelled(true)
            return
        }
        when (arena.getStatus()) {
            GameState.waiting, GameState.starting, GameState.restarting -> {
                e.setCancelled(!hungerWaiting)
            }
            GameState.playing -> {
                e.setCancelled(!hungerIngame)
            }
            else -> {}
        }
    }

    @EventHandler
    open fun onWeatherChange(e: WeatherChangeEvent) {
        if (e.toWeatherState()) {
            if (getServerType() == ServerType.SHARED) {
                if (Arena.getArenaByIdentifier(e.getWorld().getName()) != null) {
                    e.setCancelled(true)
                }
            } else {
                e.setCancelled(true)
            }
        }
    }

    @EventHandler
    open fun onCreatureSpawn(e: CreatureSpawnEvent) {
        if (e.getSpawnReason() != CreatureSpawnEvent.SpawnReason.CUSTOM) {
            if (getServerType() != ServerType.BUNGEE) {
                if (Arena.getArenaByIdentifier(e.getEntity().getWorld().getName()) != null) {
                    e.setCancelled(true)
                }
            } else {
                e.setCancelled(true)
            }
        }
    }

    @EventHandler
    open fun onDrink(e: PlayerItemConsumeEvent) {
        val a: IArena? = Arena.getArenaByPlayer(e.getPlayer())
        if (a == null) {
            return
        }
        /* remove empty bottle */
        when (e.getItem().getType()) {
            Material.GLASS_BOTTLE -> {
                nms.minusAmount(e.getPlayer(), e.getItem(), 1)
            }
            Material.MILK_BUCKET -> {
                e.setCancelled(true)
                nms.minusAmount(e.getPlayer(), e.getItem(), 1)
                val task: Int = Bukkit.getScheduler().runTaskLater(plugin, Runnable {
                    Arena.magicMilk.remove(e.getPlayer().getUniqueId())
                    debug("PlayerItemConsumeEvent player " + e.getPlayer() + " was removed from magicMilk")
                }, 20 * 30L).getTaskId()
                Arena.magicMilk.put(e.getPlayer().getUniqueId(), task)
            }
            else -> {}
        }
    }

    @EventHandler
    open fun onItemSpawn(e: ItemSpawnEvent) {
        val l = e.getEntity().getLocation()
        val a: IArena? = Arena.getArenaByIdentifier(l.getWorld().getName())
        if (a == null) {
            return
        }
        if (a.getStatus() != GameState.playing) {
            e.setCancelled(true)
        }
    }

    init {
        hungerWaiting = config.getYml().getBoolean(ConfigPath.GENERAL_CONFIGURATION_HUNGER_WAITING)
        hungerIngame = config.getYml().getBoolean(ConfigPath.GENERAL_CONFIGURATION_HUNGER_INGAME)
    }
}
