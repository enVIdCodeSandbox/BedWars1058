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

package com.andrei1058.bedwars.api.region

import org.bukkit.Location

open class Cuboid(loc: Location, radius: Int, protect: Boolean) : Region {
    private var minX: Int
    private var maxX: Int
    private var minY: Int
    private var maxY: Int
    private var minZ: Int
    private var maxZ: Int
    private var protect: Boolean

    override fun isInRegion(l: Location): Boolean {
        return (l.getBlockX() <= maxX && l.getBlockX() >= minX) && (l.getY() <= maxY && l.getY() >= minY) && (l.getBlockZ() <= maxZ && l.getBlockZ() >= minZ)
    }

    override fun isProtected(): Boolean {
        return protect
    }

    open fun setMaxY(maxY: Int) {
        this.maxY = maxY
    }

    open fun setMinY(minY: Int) {
        this.minY = minY
    }

    open fun setProtect(protect: Boolean) {
        this.protect = protect
    }

    open fun getMaxY(): Int {
        return maxY
    }

    open fun getMinY(): Int {
        return minY
    }

    init {
        val l1 = loc.clone().subtract(radius.toDouble(), radius.toDouble(), radius.toDouble())
        val l2 = loc.clone().add(radius.toDouble(), radius.toDouble(), radius.toDouble())
        minX = Math.min(l1.getBlockX(), l2.getBlockX())
        maxX = Math.max(l1.getBlockX(), l2.getBlockX())
        minY = Math.min(l1.getBlockY(), l2.getBlockY())
        maxY = Math.max(l1.getBlockY(), l2.getBlockY())
        minZ = Math.min(l1.getBlockZ(), l2.getBlockZ())
        maxZ = Math.max(l1.getBlockZ(), l2.getBlockZ())
        this.protect = protect
    }
}
