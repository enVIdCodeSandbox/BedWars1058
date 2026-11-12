package com.andrei1058.bedwars.support.party

import com.andrei1058.bedwars.api.party.Party
import de.simonsator.partyandfriends.api.pafplayers.OnlinePAFPlayer
import de.simonsator.partyandfriends.api.pafplayers.PAFPlayerManager
import de.simonsator.partyandfriends.api.party.PartyManager
import de.simonsator.partyandfriends.api.party.PlayerParty
import java.util.ArrayList
import org.bukkit.entity.Player

open class PAF : Party {
    //Party and Friends for Spigot Support by JT122406
    private fun getPAFParty(p: Player): PlayerParty? {
        if (PAFPlayerManager.getInstance() == null) {
            return null
        }
        return PartyManager.getInstance().getParty(PAFPlayerManager.getInstance().getPlayer(p))
    }

    override fun hasParty(p: Player): Boolean {
        return getPAFParty(p) != null
    }

    override fun partySize(p: Player): Int {
        val party: PlayerParty? = getPAFParty(p)
        if (party == null) {
            return 0
        }
        return party.getAllPlayers().size
    }

    override fun isOwner(p: Player): Boolean {
        val pafPlayer = PAFPlayerManager.getInstance().getPlayer(p)
        val party: PlayerParty? = PartyManager.getInstance().getParty(pafPlayer)
        if (party == null) {
            return false
        }
        return party.isLeader(pafPlayer)
    }

    override fun getMembers(owner: Player): MutableList<Player>? {
        val playerList: ArrayList<Player> = ArrayList()
        val party: PlayerParty? = getPAFParty(owner)
        if (party == null) {
            return playerList
        }
        for (players in party.getAllPlayers()) {
            playerList.add(players.getPlayer())
        }
        return playerList
    }

    override fun createParty(owner: Player, vararg members: Player) {
        val pafPlayer = PAFPlayerManager.getInstance().getPlayer(owner)
        val party = PartyManager.getInstance().createParty(pafPlayer)
        party.setPrivateState(false)
        for (p1 in members) {
            party.addPlayer(PAFPlayerManager.getInstance().getPlayer(p1))
        }
        party.setPrivateState(true)
    }

    override fun addMember(owner: Player, member: Player?) {
        val party = PAFPlayerManager.getInstance().getPlayer(owner).getParty()
        party.setPrivateState(false)
        party.addPlayer(PAFPlayerManager.getInstance().getPlayer(member))
        party.setPrivateState(true)
    }

    override fun removeFromParty(member: Player) {
        PAFPlayerManager.getInstance().getPlayer(member).getParty().leaveParty(PAFPlayerManager.getInstance().getPlayer(member))
    }

    override fun disband(owner: Player) {
        PartyManager.getInstance().deleteParty(PartyManager.getInstance().getParty(PAFPlayerManager.getInstance().getPlayer(owner)))
    }

    override fun isMember(owner: Player, check: Player): Boolean {
        val party: PlayerParty? = getPAFParty(owner)
        if (party == null) {
            return false
        }
        return party.isInParty(PAFPlayerManager.getInstance().getPlayer(check))
    }

    override fun removePlayer(owner: Player, target: Player) {
        getPAFParty(owner)!!.leaveParty(PAFPlayerManager.getInstance().getPlayer(target))
    }

    override fun getOwner(member: Player): Player? {
        return getPAFParty(member)!!.getLeader().getPlayer()
    }

    override fun promote(owner: Player, target: Player) {
        getPAFParty(owner)!!.setLeader(PAFPlayerManager.getInstance().getPlayer(target))
    }

    override fun isInternal(): Boolean {
        return false
    }
}
