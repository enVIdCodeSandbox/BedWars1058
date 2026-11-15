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

package com.andrei1058.bedwars.arena

import java.io.File
import java.text.SimpleDateFormat
import org.bukkit.Bukkit

open class ArenaManager {
    private var gid: Int = 0
    private var stamp: String = ""

    /**
     * Generates a world name that is unique for today, not loaded and not present on disk.
     */
    @Synchronized
    open fun generateGameID(): String {
        val now = SimpleDateFormat("'y'yy'm'MM'd'dd").format(System.currentTimeMillis())
        if (now != this.stamp) {
            this.stamp = now
            this.gid = 0
        }
        var id: String
        do {
            id = "bw_temp_" + this.stamp + "g" + gid++
        } while (Bukkit.getWorld(id) != null || File(Bukkit.getWorldContainer(), id).exists() || Arena.getArenaByIdentifier(id) != null)
        return id
    }
}
