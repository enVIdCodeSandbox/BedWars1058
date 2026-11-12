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

package com.andrei1058.bedwars.shop.defaultrestore

import com.andrei1058.bedwars.api.arena.GameState
import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.api.arena.team.ITeam
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
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerPickupItemEvent
import org.bukkit.inventory.ItemStack

// Used to restore default swords and bows if they are removed from the inventory and you remain with a less powerful weapon of the same kind. 1.12-.
object ShopItemRestoreListener {
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

    // 1.12 or newer
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
        open fun onDrop(e: EntityPickupItemEvent) {
            if (managePickup(e.getItem(), e.getEntity())) {
                e.setCancelled(true)
            }
        }
    }

    /**
     * Remove the default swords if the picked item is more powerful.
     *
     * @return true to cancel the event
     */
    @JvmStatic
    fun managePickup(item: Item, player: LivingEntity): Boolean {
        if (player !is Player) {
            return false
        }
        if (api.getArenaUtil().getArenaByPlayer(player as Player) == null) {
            return false
        }
        if (api.getArenaUtil().getArenaByPlayer(player as Player).getStatus() != GameState.playing) {
            return false
        }
        if (!api.getArenaUtil().getArenaByPlayer(player as Player).isPlayer(player as Player)) {
            return false
        }
        if (api.getVersionSupport().isSword(item.getItemStack())) {
            for (`is` in (player as Player).getInventory()) {
                if (`is` == null) {
                    continue
                }
                if (`is`.getType() == Material.AIR) {
                    continue
                }
                if (!api.getVersionSupport().isCustomBedWarsItem(`is`)) {
                    continue
                }
                if (api.getVersionSupport().getCustomData(`is`).equals("DEFAULT_ITEM", ignoreCase = true)) {
                    (player as Player).getInventory().remove(`is`)
                    (player as Player).updateInventory()
                    // function will only return false. default item should only be checked. access tools should be put in chests
                    return false
                }
            }
        }
        return false
    }

    /**
     * If the dropped sword/ bow is a default item and is more powerful
     * than the others in the inventory give it back.
     * <p>
     * If the player remains without a sword give it the swords from the default items.
     * If the player remains without a bow give it bows from the default items.
     *
     * @return true to cancel the event.
     */
    @JvmStatic
    private fun manageDrop(player: Entity, item: Item): Boolean {
        if (player !is Player) {
            return false
        }
        if (api.getArenaUtil().getArenaByPlayer(player as Player) == null) {
            return false
        }
        val a = api.getArenaUtil().getArenaByPlayer(player as Player)
        if (a.getStatus() != GameState.playing) {
            return false
        }
        if (!a.isPlayer(player as Player)) {
            return false
        }
        if (api.getVersionSupport().isCustomBedWarsItem(item.getItemStack()) && api.getVersionSupport().getCustomData(item.getItemStack()).equals("DEFAULT_ITEM", ignoreCase = true) && api.getVersionSupport().isSword(item.getItemStack())) {
            var hasSword: Boolean = false
            for (`is` in (player as Player).getInventory()) {
                if (`is` == null) {
                    continue
                }
                if (api.getVersionSupport().isSword(`is`)) {
                    if (api.getVersionSupport().getDamage(`is`) >= api.getVersionSupport().getDamage(item.getItemStack())) {
                        hasSword = true
                        break
                    }
                }
            }
            return !hasSword
        } else {
            var sword: Boolean = false
            for (`is` in (player as Player).getInventory()) {
                if (`is` == null) {
                    continue
                }
                if (api.getVersionSupport().isSword(`is`)) {
                    sword = true
                    break
                }
            }
            if (!sword) {
                a.getTeam(player as Player)!!.defaultSword(player as Player, true)
            }
        }
        return false
    }

    open class DefaultRestoreInvClose : Listener {
        /**
         * If the player moves a default sword or bow into another inventory
         * and he remains with a less powerful weapon restore the lost one.
         */
        @EventHandler
        open fun onInventoryClose(e: InventoryCloseEvent) {
            if (e.getInventory().getType() == InventoryType.PLAYER) {
                return
            }
            if (api.getArenaUtil().getArenaByPlayer(e.getPlayer() as Player) == null) {
                return
            }
            val a = api.getArenaUtil().getArenaByPlayer(e.getPlayer() as Player)
            if (a.getStatus() != GameState.playing) {
                return
            }
            if (!a.isPlayer(e.getPlayer() as Player)) {
                return
            }
            var sword: Boolean = false
            for (`is` in e.getPlayer().getInventory()) {
                if (`is` == null) {
                    continue
                }
                if (`is`.getType() == Material.AIR) {
                    continue
                }
                if (api.getVersionSupport().isSword(`is`)) {
                    sword = true
                }
            }
            if (!sword) {
                val team: ITeam? = a.getTeam(e.getPlayer() as Player)
                if (team != null && !a.isReSpawning(e.getPlayer() as Player)) {
                    team.defaultSword(e.getPlayer() as Player, true)
                }
            }
        }
    }
}
