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

package com.andrei1058.bedwars.configuration

import com.andrei1058.bedwars.BedWars
import org.bukkit.entity.Player

object Permissions {
    @JvmField val PERMISSION_FORCESTART: String = BedWars.mainCmd + ".forcestart"
    @JvmField val PERMISSION_ALL: String = BedWars.mainCmd + ".*"
    @JvmField val PERMISSION_COMMAND_BYPASS: String = BedWars.mainCmd + ".cmd.bypass"
    @JvmField val PERMISSION_SHOUT_COMMAND: String = BedWars.mainCmd + ".shout"
    @JvmField val PERMISSION_SETUP_ARENA: String = BedWars.mainCmd + ".setup"
    @JvmField val PERMISSION_ARENA_GROUP: String = BedWars.mainCmd + ".groups"
    @JvmField val PERMISSION_BUILD: String = BedWars.mainCmd + ".build"
    @JvmField val PERMISSION_CLONE: String = BedWars.mainCmd + ".clone"
    @JvmField val PERMISSION_DEL_ARENA: String = BedWars.mainCmd + ".delete"
    @JvmField val PERMISSION_ARENA_ENABLE: String = BedWars.mainCmd + ".enableRotation"
    @JvmField val PERMISSION_ARENA_DISABLE: String = BedWars.mainCmd + ".disable"
    @JvmField val PERMISSION_NPC: String = BedWars.mainCmd + ".npc"
    @JvmField val PERMISSION_RELOAD: String = BedWars.mainCmd + ".reload"
    @JvmField val PERMISSION_REJOIN: String = BedWars.mainCmd + ".rejoin"
    @JvmField val PERMISSION_LEVEL: String = BedWars.mainCmd + ".level"
    @JvmField val PERMISSION_CHAT_COLOR: String = BedWars.mainCmd + ".chatcolor"
    @JvmField val PERMISSION_VIP: String = BedWars.mainCmd + ".vip"

    /**
     * Check if player has one of the given permissions.
     */
    @JvmStatic
    fun hasPermission(player: Player, vararg permissions: String): Boolean {
        for (permission in permissions) {
            if (player.hasPermission(permission)) {
                return true
            }
        }
        return false
    }

    /**
     * Check if player has all given permissions.
     */
    @JvmStatic
    fun hasPermissions(player: Player, vararg permissions: String): Boolean {
        for (permission in permissions) {
            if (!player.hasPermission(permission)) {
                return false
            }
        }
        return true
    }
}
