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

package com.andrei1058.bedwars.halloween

import com.andrei1058.bedwars.BedWars
import com.andrei1058.bedwars.api.arena.IArena
import java.util.LinkedHashMap
import java.util.LinkedList
import java.util.Optional
import org.bukkit.Bukkit
import org.bukkit.block.Block
import com.andrei1058.bedwars.api.util.unchecked

open class CobWebRemover(arena: IArena) {
    private var taskId: Int
    private var arena: IArena
    private var cobWebs: LinkedHashMap<Block, Long> = LinkedHashMap()

    open fun addCobWeb(block: Block) {
        cobWebs.put(block, System.currentTimeMillis() + 7500L)
    }

    open fun getTaskId(): Int {
        return taskId
    }

    open fun getArena(): IArena {
        return arena
    }

    open fun destroy() {
        Bukkit.getScheduler().cancelTask(getTaskId())
        taskByArena.remove(arena)
    }

    open inner class RemovalTask : Runnable {
        @JvmField val toBeRemoved: LinkedList<Block> = LinkedList()

        override fun run() {
            val currentTime: Long = System.currentTimeMillis()
            cobWebs.forEach({ key, value ->
                if (value <= currentTime) {
                    toBeRemoved.add(key)
                    if (key.getType().toString().contains("WEB")) {
                        key.breakNaturally()
                    }
                }
            })
            toBeRemoved.forEach({ block -> cobWebs.remove(block) })
            toBeRemoved.clear()
        }
    }

    init {
        taskByArena.remove(arena)
        taskByArena.put(arena, this)
        this.arena = arena
        taskId = Bukkit.getScheduler().runTaskTimer(BedWars.plugin, RemovalTask(), 20L, 20L).getTaskId()
    }

    companion object {
        private val taskByArena: LinkedHashMap<IArena, CobWebRemover> = LinkedHashMap()

        @JvmStatic
        fun getByArena(arena: IArena): CobWebRemover {
            return taskByArena.get(arena).unchecked()
        }

        @JvmStatic
        fun getByArenaWorld(world: String): CobWebRemover {
            val entry = taskByArena.entries.stream().filter({ arena -> arena.key.getWorldName() == world }).findFirst()
            return entry.map { it.value }.orElse(null)
        }
    }
}
