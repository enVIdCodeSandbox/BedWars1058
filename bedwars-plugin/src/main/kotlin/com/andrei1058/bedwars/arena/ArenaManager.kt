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

import java.text.SimpleDateFormat

open class ArenaManager {
    private var gid: Int = 0
    private var day: String = ""
    private var month: String = ""

    open fun generateGameID(): String {
        val y = SimpleDateFormat("yy")
        val m = SimpleDateFormat("MM")
        val d = SimpleDateFormat("dd")
        val m2 = m.format(System.currentTimeMillis())
        val d2 = d.format(System.currentTimeMillis())
        if (!(m2 == this.month || d2.equals(this.day, ignoreCase = true))) {
            this.month = m2
            this.day = d2
            this.gid = 0
        }
        return "bw_temp_y" + y.format(System.currentTimeMillis()) + "m" + this.month + "d" + this.day + "g" + gid++
    }
}
