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

import com.alessiodp.parties.api.interfaces.PartiesAPI
import com.alessiodp.parties.api.interfaces.PartyPlayer
import com.andrei1058.bedwars.BedWars
import com.andrei1058.bedwars.api.configuration.ConfigPath
import com.andrei1058.bedwars.api.party.Party
import java.util.ArrayList
import java.util.Objects
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import com.andrei1058.bedwars.api.util.unchecked

open class PartiesAdapter : Party {
    //Support for Parties by AlessioDP
    private val api: PartiesAPI = com.alessiodp.parties.api.Parties.getApi()

    override fun hasParty(p: Player): Boolean {
        val pp: PartyPlayer? = api.getPartyPlayer(p.getUniqueId())
        return pp != null && pp.isInParty()
    }

    override fun partySize(p: Player): Int {
        if (hasParty(p)) {
            val partyPlayer: PartyPlayer? = api.getPartyPlayer(p.getUniqueId())
            if (partyPlayer != null) {
                if (partyPlayer.getPartyId() != null) {
                    val party: com.alessiodp.parties.api.interfaces.Party? = api.getParty(partyPlayer.getPartyId().unchecked())
                    if (null != party) {
                        return party.getOnlineMembers().size
                    }
                }
            }
        }
        return 0
    }

    override fun isOwner(p: Player): Boolean {
        val pp: PartyPlayer? = api.getPartyPlayer(p.getUniqueId())
        if (pp == null || pp.getPartyId() == null) {
            return false
        }
        return pp.getRank() >= requiredRankToSelect
    }

    override fun getMembers(p: Player): MutableList<Player>? {
        val players: ArrayList<Player> = ArrayList()
        if (hasParty(p)) {
            val pp: PartyPlayer? = api.getPartyPlayer(p.getUniqueId())
            if (null != pp) {
                if (pp.getPartyId() != null) {
                    val party = api.getParty(pp.getPartyId().unchecked())
                    for (member in party!!.getOnlineMembers()) {
                        players.add(Bukkit.getPlayer(member.getPlayerUUID()))
                    }
                }
            }
        }
        return players
    }

    override fun createParty(owner: Player, vararg members: Player) {
        //party creation handled on bungee side
        if (!api.isBungeeCordEnabled()) {
            val partyOwner: PartyPlayer? = api.getPartyPlayer(owner.getUniqueId())
            if (null != partyOwner && !partyOwner.isInParty()) {
                val created: Boolean = api.createParty(null, partyOwner)
                if (created && null != partyOwner.getPartyId()) {
                    val party: com.alessiodp.parties.api.interfaces.Party? = api.getParty(partyOwner.getPartyId().unchecked())
                    if (null != party) {
                        for (player1 in members) {
                            val partyPlayer: PartyPlayer? = api.getPartyPlayer(player1.getUniqueId())
                            if (null != partyPlayer && !partyPlayer.isInParty()) {
                                party.addMember(partyPlayer)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun addMember(owner: Player?, member: Player?) {
        //party operations handled on bungee server side
        if (!api.isBungeeCordEnabled()) {
            val partyPlayer: PartyPlayer? = api.getPartyPlayer(owner!!.getUniqueId())
            if (null != partyPlayer) {
                if (null != partyPlayer.getPartyId()) {
                    val party: com.alessiodp.parties.api.interfaces.Party? = api.getParty(partyPlayer.getPartyId().unchecked())
                    if (null != party) {
                        val partyMember: PartyPlayer? = api.getPartyPlayer(member!!.getUniqueId())
                        if (null != partyMember) {
                            party.addMember(partyMember)
                        }
                    }
                }
            }
        }
    }

    override fun removeFromParty(member: Player) {
        val partyMember: PartyPlayer? = api.getPartyPlayer(member.getUniqueId())
        if (null != partyMember) {
            if (null != partyMember.getPartyId()) {
                val party: com.alessiodp.parties.api.interfaces.Party? = api.getParty(partyMember.getPartyId().unchecked())
                if (null != party) {
                    party.removeMember(partyMember)
                }
            }
        }
    }

    override fun disband(owner: Player) {
        val partyMember: PartyPlayer? = api.getPartyPlayer(owner.getUniqueId())
        if (null != partyMember) {
            if (null != partyMember.getPartyId()) {
                val party: com.alessiodp.parties.api.interfaces.Party? = api.getParty(partyMember.getPartyId().unchecked())
                if (null != party) {
                    party.delete()
                }
            }
        }
    }

    override fun isMember(owner: Player, check: Player): Boolean {
        if (!hasParty(owner) || !hasParty(check)) {
            return false
        } else {
            return api.areInTheSameParty(owner.getUniqueId(), check.getUniqueId())
        }
    }

    override fun removePlayer(owner: Player, target: Player) {
        val player: PartyPlayer? = api.getPartyPlayer(owner.getUniqueId())
        if (null != player) {
            if (null != player.getPartyId()) {
                val party: com.alessiodp.parties.api.interfaces.Party? = api.getParty(player.getPartyId().unchecked())
                if (null != party) {
                    val targetPlayer: PartyPlayer? = api.getPartyPlayer(target.getUniqueId())
                    if (null != targetPlayer) {
                        party.removeMember(targetPlayer)
                    }
                }
            }
        }
    }

    override fun getOwner(member: Player): Player? {
        val partyPlayer = api.getPartyPlayer(member.getUniqueId())
        return Bukkit.getPlayer(Objects.requireNonNull(api.getParty(Objects.requireNonNull(partyPlayer)!!.getPartyId().unchecked()))!!.getLeader())
    }

    override fun promote(owner: Player, target: Player) {
    }

    override fun isInternal(): Boolean {
        return false
    }

    companion object {
        private val requiredRankToSelect: Int = BedWars.config.getInt(ConfigPath.GENERAL_ALESSIODP_PARTIES_RANK)
    }
}
