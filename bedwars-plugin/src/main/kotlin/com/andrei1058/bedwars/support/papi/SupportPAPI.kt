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

package com.andrei1058.bedwars.support.papi

import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.entity.Player

object SupportPAPI {
    private var supportPAPI: supp = noPAPI()

    interface supp {
        fun replace(p: Player, s: String): String

        fun replace(p: Player, strings: MutableList<String>): MutableList<String>
    }

    open class noPAPI : supp {
        override fun replace(p: Player, s: String): String {
            return s
        }

        override fun replace(p: Player, strings: MutableList<String>): MutableList<String> {
            return strings
        }
    }

    open class withPAPI : supp {
        override fun replace(p: Player, s: String): String {
            return PlaceholderAPI.setPlaceholders(p, s)
        }

        override fun replace(p: Player, strings: MutableList<String>): MutableList<String> {
            return PlaceholderAPI.setPlaceholders(p, strings)
        }
    }

    @JvmStatic
    fun getSupportPAPI(): supp {
        return supportPAPI
    }

    @JvmStatic
    fun setSupportPAPI(s: supp) {
        supportPAPI = s
    }
}
