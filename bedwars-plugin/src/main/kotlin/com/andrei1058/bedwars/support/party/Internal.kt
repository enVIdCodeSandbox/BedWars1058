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

import com.andrei1058.bedwars.api.language.Language.Companion.getMsg
import com.andrei1058.bedwars.api.language.Messages
import com.andrei1058.bedwars.api.party.Party
import java.util.ArrayList
import java.util.Collections
import org.bukkit.entity.Player
import org.jetbrains.annotations.Contract

open class Internal : Party {
    override fun hasParty(p: Player): Boolean {
        for (party in getParites()) {
            if (party.members.contains(p)) {
                return true
            }
        }
        return false
    }

    override fun partySize(p: Player): Int {
        for (party in getParites()) {
            if (party.members.contains(p)) {
                return party.members.size
            }
        }
        return 0
    }

    override fun isOwner(p: Player): Boolean {
        for (party in getParites()) {
            if (party.members.contains(p)) {
                if (party.owner === p) {
                    return true
                }
            }
        }
        return false
    }

    override fun getMembers(owner: Player): MutableList<Player>? {
        for (party in getParites()) {
            if (party.members.contains(owner)) {
                return party.members
            }
        }
        return null
    }

    override fun createParty(owner: Player, vararg members: Player) {
        val p = Party(owner)
        p.addMember(owner)
        for (mem in members) {
            p.addMember(mem)
        }
    }

    override fun addMember(owner: Player?, member: Player?) {
        if (owner == null || member == null) {
            return
        }
        val p: Internal.Party? = getParty(owner)
        if (p == null) {
            return
        }
        p.addMember(member)
    }

    override fun removeFromParty(member: Player) {
        for (p in ArrayList(getParites())) {
            if (p.owner === member) {
                disband(member)
            } else if (p.members.contains(member)) {
                for (mem in p.members) {
                    mem.sendMessage(getMsg(mem, Messages.COMMAND_PARTY_LEAVE_SUCCESS).replace("{playername}", member.getName()).replace("{player}", member.getDisplayName()))
                }
                p.members.remove(member)
                if (p.members.isEmpty() || p.members.size == 1) {
                    disband(p.owner)
                    parites.remove(p)
                }
                return
            }
        }
    }

    override fun disband(owner: Player) {
        val pa: Internal.Party? = getParty(owner)
        if (pa == null) {
            return
        }
        for (p in pa.members) {
            p.sendMessage(getMsg(p, Messages.COMMAND_PARTY_DISBAND_SUCCESS))
        }
        pa.members.clear()
        Internal.parites.remove(pa)
    }

    override fun isMember(owner: Player, check: Player): Boolean {
        for (p in parites) {
            if (p.owner === owner) {
                if (p.members.contains(check)) {
                    return true
                }
            }
        }
        return false
    }

    override fun removePlayer(owner: Player, target: Player) {
        val p: Party? = getParty(owner)
        if (p != null) {
            if (p.members.contains(target)) {
                for (mem in p.members) {
                    mem.sendMessage(getMsg(mem, Messages.COMMAND_PARTY_REMOVE_SUCCESS).replace("{player}", target.getName()))
                }
                p.members.remove(owner)
                if (p.members.isEmpty() || p.members.size == 1) {
                    disband(p.owner)
                    parites.remove(p)
                }
            }
        }
    }

    override fun getOwner(member: Player): Player? {
        for (party in Internal.getParites()) {
            if (party.members.contains(member)) {
                return party.owner
            }
        }
        return null
    }

    override fun promote(owner: Player, target: Player) {
        val p: Party? = getParty(owner)
        if (p != null) {
            p.owner = target
        }
    }

    override fun isInternal(): Boolean {
        return true
    }

    private fun getParty(owner: Player): Party? {
        for (p in getParites()) {
            if (p.getOwner() === owner) {
                return p
            }
        }
        return null
    }

    open class Party(p: Player) {
        @JvmField var members: MutableList<Player> = ArrayList()
        @JvmField var owner: Player

        open fun getOwner(): Player {
            return owner
        }

        open fun addMember(p: Player) {
            members.add(p)
        }

        init {
            owner = p
            Internal.parites.add(this)
        }
    }

    companion object {
        private var parites: MutableList<Internal.Party> = ArrayList()

        @Contract(pure = true)
        @JvmStatic
        fun getParites(): MutableList<Party> {
            return Collections.unmodifiableList(parites)
        }
    }
}
