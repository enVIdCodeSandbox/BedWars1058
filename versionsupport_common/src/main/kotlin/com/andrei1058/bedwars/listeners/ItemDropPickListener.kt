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

import com.andrei1058.bedwars.api.arena.GameState
import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.api.events.player.PlayerGeneratorCollectEvent
import com.andrei1058.bedwars.api.server.ServerType
import com.andrei1058.bedwars.support.version.common.VersionCommon.Companion.api
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Entity
import org.bukkit.entity.Item
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDropItemEvent
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerPickupArrowEvent
import org.bukkit.event.player.PlayerPickupItemEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import com.andrei1058.bedwars.api.util.unchecked

object ItemDropPickListener {
    // 1.11 or older
    open class PlayerDrop : Listener {
        @EventHandler
        open fun onDrop(e: PlayerDropItemEvent) {
            if (manageDrop(e.getPlayer(), e.getItemDrop())) {
                e.setCancelled(true)
            }
        }
    }

    // 1.11 or older
    open class PlayerPickup : Listener {
        @EventHandler
        open fun onDrop(e: PlayerPickupItemEvent) {
            if (managePickup(e.getItem(), e.getPlayer())) {
                e.setCancelled(true)
            }
        }
    }

    // 1.13 or newer
    open class EntityDrop : Listener {
        @EventHandler
        open fun onDrop(e: EntityDropItemEvent) {
            if (manageDrop(e.getEntity(), e.getItemDrop())) {
                e.setCancelled(true)
            }
        }
    }

    // 1.12 or newer
    open class EntityPickup : Listener {
        @EventHandler
        open fun onPickup(e: EntityPickupItemEvent) {
            if (managePickup(e.getItem(), e.getEntity())) {
                e.setCancelled(true)
            }
        }
    }

    // 1.9 or newer
    open class ArrowCollect : Listener {
        @EventHandler
        open fun onArrowPick(e: PlayerPickupArrowEvent) {
            if (api.getArenaUtil().isSpectating(e.getPlayer())) {
                e.setCancelled(true)
            }
        }
    }

    /**
     * @return true if event should be cancelled
     */
    @JvmStatic
    private fun managePickup(item: Item, player: LivingEntity): Boolean {
        if (player !is Player) {
            return false
        }
        if (api.getServerType() == ServerType.MULTIARENA) {
            //noinspection ConstantConditions
            if (player.getLocation().getWorld()!!.getName().equals(api.getLobbyWorld(), ignoreCase = true)) {
                return true
            }
        }
        val a: IArena? = api.getArenaUtil().getArenaByPlayer(player as Player)
        if (a == null) {
            return false
        }
        if (!a.isPlayer(player as Player)) {
            return true
        }
        if (a.getStatus() != GameState.playing) {
            return true
        }
        if (a.getRespawnSessions()!!.containsKey(player)) {
            return true
        }
        if (item.getItemStack().getType() == Material.ARROW) {
            item.setItemStack(api.getVersionSupport().createItemStack(item.getItemStack().getType().toString(), item.getItemStack().getAmount(), 0.toShort()))
            return false
        }
        if (item.getItemStack().getType().toString() == "BED") {
            item.remove()
            return true
        } else if (item.getItemStack().hasItemMeta()) {
            //noinspection ConstantConditions
            if (item.getItemStack().getItemMeta()!!.hasDisplayName()) {
                if (item.getItemStack().getItemMeta()!!.getDisplayName().contains("custom")) {
                    val material = item.getItemStack().getType()
                    val itemMeta = ItemStack(material).getItemMeta()
                    //Call ore pick up event
                    if (!api.getAFKUtil().isPlayerAFK((player as Player).getPlayer().unchecked())) {
                        val event = PlayerGeneratorCollectEvent(player as Player, item, a)
                        Bukkit.getPluginManager().callEvent(event)
                        if (event.isCancelled()) {
                            return true
                        } else {
                            item.getItemStack().setItemMeta(itemMeta)
                        }
                    } else {
                        //Cancel event if player is afk
                        return true
                    }
                }
            }
        }
        return false
    }

    /**
     * @return true to cancel the event.
     */
    @JvmStatic
    private fun manageDrop(player: Entity, item: Item): Boolean {
        if (player !is Player) {
            return false
        }
        if (api.getServerType() == ServerType.MULTIARENA) {
            //noinspection ConstantConditions
            if (player.getLocation().getWorld()!!.getName().equals(api.getLobbyWorld(), ignoreCase = true)) {
                return true
            }
        }
        val a: IArena? = api.getArenaUtil().getArenaByPlayer(player as Player)
        if (a == null) {
            return false
        }
        if (!a.isPlayer(player as Player)) {
            return true
        }
        if (a.getStatus() != GameState.playing) {
            return true
        } else {
            val i = item.getItemStack()
            if (i.getType() == Material.COMPASS) {
                return true
            }
        }
        return a.getRespawnSessions()!!.containsKey(player)
    }
}
