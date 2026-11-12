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

package com.andrei1058.bedwars.api.server

import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.api.configuration.ConfigPath
import java.util.function.Consumer
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.entity.Item
import org.bukkit.plugin.Plugin
import org.bukkit.util.Vector
import com.andrei1058.bedwars.api.util.unchecked

abstract class RestoreAdapter(owner: Plugin) {
    private val plugin: Plugin

    /**
     * Get adapter owner.
     */
    open fun getOwner(): Plugin {
        return plugin
    }

    /**
     * Load the world.
     * Arenas will be initialized automatically based on WorldLoadEvent.
     */
    abstract fun onEnable(a: IArena)

    /**
     * Restore the world.
     * call new Arena when it's done.
     */
    abstract fun onRestart(a: IArena)

    /**
     * Unload the world.
     * This is usually used for /bw unloadArena name
     */
    abstract fun onDisable(a: IArena)

    /**
     * Load the world for setting it up.
     */
    abstract fun onSetupSessionStart(s: ISetupSession)

    /**
     * Unload the world.
     */
    abstract fun onSetupSessionClose(s: ISetupSession)

    /**
     * Remove lobby blocks.
     */
    open fun onLobbyRemoval(a: IArena) {
        this.foreachBlockInRegion(a.getConfig()!!.getArenaLoc(ConfigPath.ARENA_WAITING_POS1), a.getConfig()!!.getArenaLoc(ConfigPath.ARENA_WAITING_POS2), { block -> block.setType(Material.AIR) })
        Bukkit.getScheduler().runTaskLater(getOwner(), Runnable { clearItems(a.getWorld().unchecked()) }, 15L)
    }

    /**
     * Check if given world exists.
     */
    abstract fun isWorld(name: String): Boolean

    /**
     * Delete a world.
     */
    abstract fun deleteWorld(name: String)

    /**
     * Clone an arena world.
     */
    abstract fun cloneArena(name1: String, name2: String)

    /**
     * Get world container.
     */
    abstract fun getWorldsList(): MutableList<String>

    /**
     * Convert worlds if it is necessary before loading them.
     * Let them load on BedWars1058 main Thread, so they will be converted before getting loaded.
     */
    abstract fun convertWorlds()

    abstract fun getDisplayName(): String

    open fun foreachBlockInRegion(corner1: Location?, corner2: Location?, consumer: Consumer<Block>) {
        if (null == corner1 || null == corner2) {
            return
        }
        val min = Vector(Math.min(corner1.getBlockX(), corner2.getBlockX()), Math.min(corner1.getBlockY(), corner2.getBlockY()), Math.min(corner1.getBlockZ(), corner2.getBlockZ()))
        val max = Vector(Math.max(corner1.getBlockX(), corner2.getBlockX()), Math.max(corner1.getBlockY(), corner2.getBlockY()), Math.max(corner1.getBlockZ(), corner2.getBlockZ()))
        var x: Int = min.getBlockX()
        while (x < max.getBlockX()) {
            var y: Int = min.getBlockY()
            while (y < max.getBlockY()) {
                var z: Int = min.getBlockZ()
                while (z < max.getBlockZ()) {
                    consumer.accept(corner1.getWorld().getBlockAt(x, y, z))
                    z++
                }
                y++
            }
            x++
        }
    }

    open fun clearItems(world: World) {
        world.getEntities().forEach({ e ->
            if (e is Item) {
                e.remove()
            }
        })
    }

    init {
        this.plugin = owner
    }
}
