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

package com.andrei1058.bedwars.api.party

import org.bukkit.ChatColor
import org.bukkit.entity.Player

interface Party {
    fun hasParty(p: Player): Boolean

    fun partySize(p: Player): Int

    fun isOwner(p: Player): Boolean

    fun getMembers(owner: Player): MutableList<Player>?

    fun createParty(owner: Player, vararg members: Player)

    fun addMember(owner: Player?, member: Player?)

    fun removeFromParty(member: Player)

    fun disband(owner: Player)

    fun isMember(owner: Player, check: Player): Boolean

    fun removePlayer(owner: Player, target: Player)

    fun getOwner(member: Player): Player? {
        for (m in this.getMembers(member)!!) {
            if (isOwner(m)) {
                return m
            }
        }
        return null
    }

    fun promote(owner: Player, target: Player) {
        val msg = ChatColor.RED.toString() + "Not implemented! Contact an administrator"
        owner.sendMessage(msg)
        target.sendMessage(msg)
    }

    fun isInternal(): Boolean
}
