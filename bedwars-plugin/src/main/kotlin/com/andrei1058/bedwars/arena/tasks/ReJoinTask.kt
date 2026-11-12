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

package com.andrei1058.bedwars.arena.tasks

import com.andrei1058.bedwars.BedWars
import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.api.arena.team.ITeam
import com.andrei1058.bedwars.api.configuration.ConfigPath
import java.util.ArrayList
import java.util.Collections
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitTask
import org.jetbrains.annotations.Contract

open class ReJoinTask(private val arena: IArena?, private val bedWarsTeam: ITeam?) : Runnable {
    private val task: BukkitTask

    override fun run() {
        if (arena == null) {
            destroy()
            return
        }
        if (bedWarsTeam == null) {
            destroy()
            return
        }
        if (bedWarsTeam.getMembers() == null) {
            destroy()
            return
        }
        if (bedWarsTeam.getMembers()!!.isEmpty()) {
            bedWarsTeam.setBedDestroyed(true)
            destroy()
        }
    }

    /**
     * Get arena
     */
    open fun getArena(): IArena? {
        return arena
    }

    /**
     * Destroy task
     */
    open fun destroy() {
        reJoinTasks.remove(this)
        task.cancel()
    }

    open fun cancel() {
        task.cancel()
    }

    init {
        task = Bukkit.getScheduler().runTaskLater(BedWars.plugin, this, BedWars.config.getInt(ConfigPath.GENERAL_CONFIGURATION_REJOIN_TIME) * 20L)
    }

    companion object {
        private val reJoinTasks: MutableList<ReJoinTask> = ArrayList()

        /**
         * Get tasks list
         */
        @Contract(pure = true)
        @JvmStatic
        fun getReJoinTasks(): MutableCollection<ReJoinTask> {
            return Collections.unmodifiableCollection(reJoinTasks)
        }
    }
}
