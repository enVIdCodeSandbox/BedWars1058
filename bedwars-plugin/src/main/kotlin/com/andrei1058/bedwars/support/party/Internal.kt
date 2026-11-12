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
import com.andrei1058.bedwars.party.PartyService
import java.util.ArrayList
import java.util.UUID
import org.bukkit.entity.Player

/**
 * Built-in party provider. The rules live in {@link PartyService}; this adapter only exposes them through the
 * {@link Party} API the arenas use.
 */
open class Internal : Party {
    override fun hasParty(p: Player): Boolean {
        return PartyService.partyOf(p) != null
    }

    override fun partySize(p: Player): Int {
        val party: PartyService.Party? = PartyService.partyOf(p)
        return if (party == null) 0 else party.members.size
    }

    override fun isOwner(p: Player): Boolean {
        return PartyService.isLeader(p)
    }

    override fun getMembers(owner: Player): MutableList<Player>? {
        val party: PartyService.Party? = PartyService.partyOf(owner)
        return if (party == null) null else ArrayList(party.online())
    }

    override fun createParty(owner: Player, vararg members: Player) {
        PartyService.create(owner, *members)
    }

    override fun addMember(owner: Player, member: Player?) {
        val party: PartyService.Party? = PartyService.partyOf(owner)
        if (party != null && member != null) {
            PartyService.join(party, member, false)
        }
    }

    override fun removeFromParty(member: Player) {
        PartyService.leave(member, true)
    }

    override fun disband(owner: Player) {
        val party: PartyService.Party? = PartyService.partyOf(owner)
        if (party != null) {
            PartyService.disbandParty(party)
        }
    }

    override fun isMember(owner: Player, check: Player): Boolean {
        val party: PartyService.Party? = PartyService.partyOf(owner)
        return party != null && party.getMembers().contains(check.getUniqueId())
    }

    override fun removePlayer(owner: Player, target: Player) {
        PartyService.kick(owner, target.getName())
    }

    override fun getOwner(member: Player): Player? {
        val party: PartyService.Party? = PartyService.partyOf(member)
        if (party == null) {
            return null
        }
        val leader = party.getLeader()
        return org.bukkit.Bukkit.getPlayer(leader)
    }

    override fun promote(owner: Player, target: Player) {
        PartyService.transfer(owner, target.getName())
    }

    override fun isInternal(): Boolean {
        return true
    }
}
