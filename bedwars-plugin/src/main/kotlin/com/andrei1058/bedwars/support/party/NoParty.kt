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

package com.andrei1058.bedwars.support.party

import com.andrei1058.bedwars.api.party.Party
import java.util.ArrayList
import org.bukkit.entity.Player

open class NoParty : Party {
    override fun hasParty(p: Player): Boolean {
        return false
    }

    override fun partySize(p: Player): Int {
        return 0
    }

    override fun isOwner(p: Player): Boolean {
        return false
    }

    override fun getMembers(owner: Player): MutableList<Player>? {
        return ArrayList()
    }

    override fun createParty(owner: Player, vararg members: Player) {
    }

    override fun addMember(owner: Player?, member: Player?) {
    }

    override fun removeFromParty(member: Player) {
    }

    override fun disband(owner: Player) {
    }

    override fun isMember(owner: Player, check: Player): Boolean {
        return false
    }

    override fun removePlayer(owner: Player, target: Player) {
    }

    override fun getOwner(member: Player): Player? {
        return null
    }

    override fun promote(owner: Player, target: Player) {
    }

    override fun isInternal(): Boolean {
        return false
    }
}
