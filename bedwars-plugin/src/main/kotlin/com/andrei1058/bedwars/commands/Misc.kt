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

package com.andrei1058.bedwars.commands

import com.andrei1058.bedwars.BedWars
import com.andrei1058.bedwars.arena.SetupSession
import com.andrei1058.bedwars.support.paper.TeleportManager
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.metadata.FixedMetadataValue

object Misc {
    /**
     * This is used to spawn armorStands during the setup
     * so the player knows what he set
     *
     * @since api v6
     */
    @JvmStatic
    fun createArmorStand(name: String, location: Location, configLoc: String?) {
        val a = location.getWorld().spawnEntity(location.getBlock().getLocation().add(0.5, 2.0, 0.5), EntityType.ARMOR_STAND) as ArmorStand
        a.setVisible(false)
        a.setMarker(true)
        a.setGravity(false)
        a.setCustomNameVisible(true)
        a.setCustomName(name)
        a.setMetadata("bw1058-setup", FixedMetadataValue(BedWars.plugin, "hologram"))
        if (configLoc != null) {
            a.setMetadata("bw1058-loc", FixedMetadataValue(BedWars.plugin, configLoc))
        }
    }

    /**
     * Remove an armor stand
     */
    @JvmStatic
    fun removeArmorStand(contains: String?, location: Location, configLoc: String?) {
        for (e in location.getWorld().getNearbyEntities(location, 1.0, 3.0, 1.0)) {
            if (e.hasMetadata("bw1058-setup")) {
                if (e.hasMetadata("bw1058-loc")) {
                    if (e.getMetadata("bw1058-loc").get(0).asString().equals(configLoc, ignoreCase = true)) {
                        if (contains != null) {
                            if (!contains.isEmpty()) {
                                if (ChatColor.stripColor(e.getCustomName()).contains(contains)) {
                                    e.remove()
                                    return
                                }
                            }
                        }
                        e.remove()
                    }
                } else {
                    e.remove()
                }
                continue
            }
            if (e.getType() == EntityType.ARMOR_STAND) {
                if (!(e as ArmorStand).isVisible()) {
                    if (contains != null && e.getCustomName().contains(contains)) {
                        e.remove()
                    }
                }
            }
        }
    }

    /**
     * Find and set generators
     */
    @JvmStatic
    fun autoSetGen(p: Player, command: String, setupSession: SetupSession, type: Material) {
        if (type == Material.EMERALD_BLOCK) {
            if (setupSession.isAutoCreatedEmerald()) {
                return
            }
            setupSession.setAutoCreatedEmerald(true)
        } else {
            if (setupSession.isAutoCreatedDiamond()) {
                return
            }
            setupSession.setAutoCreatedDiamond(true)
        }
        detectGenerators(p.getLocation().add(0.0, -1.0, 0.0).getBlock().getLocation(), setupSession)
        Bukkit.getScheduler().runTaskLater(BedWars.plugin, Runnable {
            for (l in setupSession.getSkipAutoCreateGen()) {
                Bukkit.getScheduler().runTaskLater(BedWars.plugin, Runnable {
                    TeleportManager.teleport(p, l)
                    Bukkit.dispatchCommand(p, command + (if (l.add(0.0, -1.0, 0.0).getBlock().getType() == Material.EMERALD_BLOCK) "emerald" else "diamond"))
                }, 20)
            }
        }, 20)
    }

    /**
     * @param origin block location under player
     */
    @JvmStatic
    private fun detectGenerators(origin: Location, setupSession: SetupSession) {
        var origin = origin
        origin = origin.getBlock().getLocation()
        setupSession.addSkipAutoCreateGen(origin)
        val target = origin.getBlock().getType()
        val layout_z_minus = origin.clone().add(0.0, 1.0, -1.0).getBlock().getType()
        val layout_z_plus = origin.clone().add(0.0, 1.0, 1.0).getBlock().getType()
        val layout_x_minus = origin.clone().add(-1.0, 1.0, 0.0).getBlock().getType()
        val layout_x_plus = origin.clone().add(1.0, 1.0, 0.0).getBlock().getType()
        val layout_x_plus_z_plus = origin.clone().add(1.0, 1.0, 1.0).getBlock().getType()
        val layout_x_plus_z_minus = origin.clone().add(1.0, 1.0, -1.0).getBlock().getType()
        val layout_x_minus_z_plus = origin.clone().add(-1.0, 1.0, 1.0).getBlock().getType()
        val layout_x_minus_z_minus = origin.clone().add(-1.0, 1.0, -1.0).getBlock().getType()
        val path = "generator." + (if (target == Material.DIAMOND_BLOCK) "Diamond" else "Emerald")
        if (layout_z_minus == Material.AIR || layout_z_plus == Material.AIR || layout_x_minus == Material.AIR || layout_x_plus == Material.AIR || layout_x_plus_z_plus == Material.AIR || layout_x_plus_z_minus == Material.AIR || layout_x_minus_z_plus == Material.AIR || layout_x_minus_z_minus == Material.AIR) {
            //It's better not to use it
            return
        }
        val locations = setupSession.getConfig().getArenaLocations(path)
        for (x in -150 until 150) {
            for (z in -150 until 150) {
                val b = origin.clone().add(x.toDouble(), 0.0, z.toDouble()).getBlock()
                if (b.getX() == origin.getBlockX() && b.getY() == origin.getBlockY() && b.getZ() == origin.getBlockZ()) {
                    continue
                }
                val l = b.getLocation().clone().add(0.0, 1.0, 0.0)
                for (location in locations) {
                    setupSession.getConfig().compareArenaLoc(location, b.getLocation().add(0.0, 1.0, 0.0))
                }
                if (b.getType() == target) {
                    if (layout_z_minus == l.clone().add(0.0, 0.0, -1.0).getBlock().getType() && layout_z_plus == l.clone().add(0.0, 0.0, 1.0).getBlock().getType() && layout_x_minus == l.clone().add(-1.0, 0.0, 0.0).getBlock().getType() && layout_x_plus == l.clone().add(1.0, 0.0, 0.0).getBlock().getType() && layout_x_plus_z_minus == l.clone().add(1.0, 0.0, -1.0).getBlock().getType() && layout_x_plus_z_plus == l.clone().add(1.0, 0.0, 1.0).getBlock().getType() && layout_x_minus_z_plus == l.clone().add(-1.0, 0.0, 1.0).getBlock().getType() && layout_x_minus_z_minus == l.clone().add(-1.0, 0.0, -1.0).getBlock().getType()) {
                        if (!setupSession.getSkipAutoCreateGen().contains(l)) {
                            setupSession.addSkipAutoCreateGen(l)
                            detectGenerators(b.getLocation(), setupSession)
                        }
                    }
                }
            }
        }
    }
}
