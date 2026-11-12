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

package com.andrei1058.bedwars.shop.listeners

import com.andrei1058.bedwars.BedWars
import com.andrei1058.bedwars.BedWars.Companion.nms
import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.api.arena.team.ITeam
import com.andrei1058.bedwars.api.configuration.ConfigPath
import com.andrei1058.bedwars.api.events.player.PlayerBedBugSpawnEvent
import com.andrei1058.bedwars.api.events.player.PlayerDreamDefenderSpawnEvent
import com.andrei1058.bedwars.arena.Arena
import com.andrei1058.bedwars.arena.Misc
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import com.andrei1058.bedwars.api.util.unchecked

open class SpecialsListener : Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    open fun onSpecialInteract(e: PlayerInteractEvent) {
        if (e.isCancelled()) {
            return
        }
        val p = e.getPlayer()
        val i: ItemStack? = e.getItem()
        if (i == null) {
            return
        }
        if (i.getType() == Material.AIR) {
            return
        }
        val a: IArena? = Arena.getArenaByPlayer(p)
        if (a == null) {
            return
        }
        if (a.getRespawnSessions()!!.containsKey(e.getPlayer())) {
            return
        }
        if (!a.isPlayer(p)) {
            return
        }
        val b: Block? = e.getClickedBlock()
        if (b == null) {
            return
        }
        val l = b.getLocation()
        if (BedWars.shop.getYml().getBoolean(ConfigPath.SHOP_SPECIAL_SILVERFISH_ENABLE)) {
            if (!Misc.isProjectile(Material.valueOf(BedWars.shop.getYml().getString(ConfigPath.SHOP_SPECIAL_SILVERFISH_MATERIAL)))) {
                if (i.getType() == Material.valueOf(BedWars.shop.getYml().getString(ConfigPath.SHOP_SPECIAL_SILVERFISH_MATERIAL)) && nms.itemStackDataCompare(i, BedWars.shop.getYml().getInt(ConfigPath.SHOP_SPECIAL_SILVERFISH_DATA).toShort())) {
                    e.setCancelled(true)
                    val playerTeam = a.getTeam(p)
                    val event = PlayerBedBugSpawnEvent(p, playerTeam.unchecked(), a)
                    nms.spawnSilverfish(l.add(0.0, 1.0, 0.0), playerTeam.unchecked(), BedWars.shop.getYml().getDouble(ConfigPath.SHOP_SPECIAL_SILVERFISH_SPEED), BedWars.shop.getYml().getDouble(ConfigPath.SHOP_SPECIAL_SILVERFISH_HEALTH), BedWars.shop.getInt(ConfigPath.SHOP_SPECIAL_SILVERFISH_DESPAWN), BedWars.shop.getYml().getDouble(ConfigPath.SHOP_SPECIAL_SILVERFISH_DAMAGE))
                    Bukkit.getPluginManager().callEvent(event)
                    if (!nms.isProjectile(i)) {
                        nms.minusAmount(p, i, 1)
                        p.updateInventory()
                    }
                }
            }
        }
        if (BedWars.shop.getYml().getBoolean(ConfigPath.SHOP_SPECIAL_IRON_GOLEM_ENABLE)) {
            if (!Misc.isProjectile(Material.valueOf(BedWars.shop.getYml().getString(ConfigPath.SHOP_SPECIAL_IRON_GOLEM_MATERIAL)))) {
                if (i.getType() == Material.valueOf(BedWars.shop.getYml().getString(ConfigPath.SHOP_SPECIAL_IRON_GOLEM_MATERIAL)) && nms.itemStackDataCompare(i, BedWars.shop.getYml().getInt(ConfigPath.SHOP_SPECIAL_IRON_GOLEM_DATA).toShort())) {
                    e.setCancelled(true)
                    val playerTeam = a.getTeam(p)
                    val event = PlayerDreamDefenderSpawnEvent(p, playerTeam.unchecked(), a)
                    nms.spawnIronGolem(l.add(0.0, 1.0, 0.0), playerTeam.unchecked(), BedWars.shop.getYml().getDouble(ConfigPath.SHOP_SPECIAL_IRON_GOLEM_SPEED), BedWars.shop.getYml().getDouble(ConfigPath.SHOP_SPECIAL_IRON_GOLEM_HEALTH), BedWars.shop.getInt(ConfigPath.SHOP_SPECIAL_IRON_GOLEM_DESPAWN))
                    Bukkit.getPluginManager().callEvent(event)
                    if (!nms.isProjectile(i)) {
                        nms.minusAmount(p, i, 1)
                        p.updateInventory()
                    }
                }
            }
        }
    }
}
