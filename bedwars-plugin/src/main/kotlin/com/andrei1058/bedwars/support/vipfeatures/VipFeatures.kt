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

package com.andrei1058.bedwars.support.vipfeatures

import com.andrei1058.bedwars.api.arena.GameState
import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.arena.Arena
import com.andrei1058.vipfeatures.api.MiniGame
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

open class VipFeatures(plugin: Plugin) : MiniGame(plugin) {
    override fun isPlaying(p: Player): Boolean {
        val a: IArena? = Arena.getArenaByPlayer(p)
        if (a != null) {
            return !(a.getStatus() == GameState.waiting || a.getStatus() == GameState.starting)
        }
        return false
    }

    override fun hasBoosters(): Boolean {
        return false
    }

    override fun getDisplayName(): String {
        return "BedWars1058"
    }
}
