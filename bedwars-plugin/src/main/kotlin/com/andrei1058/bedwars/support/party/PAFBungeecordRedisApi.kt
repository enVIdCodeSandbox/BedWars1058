package com.andrei1058.bedwars.support.party

import com.andrei1058.bedwars.api.party.Party
import de.simonsator.partyandfriends.spigot.api.pafplayers.PAFPlayer
import de.simonsator.partyandfriends.spigot.api.pafplayers.PAFPlayerManager
import de.simonsator.partyandfriends.spigot.api.party.PartyManager
import de.simonsator.partyandfriends.spigot.api.party.PlayerParty
import java.util.ArrayList
import org.bukkit.Bukkit
import org.bukkit.entity.Player

open class PAFBungeecordRedisApi : Party {
    private fun getPAFParty(p: Player): PlayerParty? {
        if (PAFPlayerManager.getInstance() == null) {
            return null
        }
        return PartyManager.getInstance().getParty(PAFPlayerManager.getInstance().getPlayer(p.getUniqueId()))
    }

    override fun hasParty(p: Player): Boolean {
        return getPAFParty(p) != null
    }

    override fun partySize(p: Player): Int {
        return getMembers(p)!!.size
    }

    override fun isOwner(p: Player): Boolean {
        val pafPlayer = PAFPlayerManager.getInstance().getPlayer(p.getUniqueId())
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
            val bukkitPlayer: Player? = Bukkit.getPlayer(players.getUniqueId())
            if (bukkitPlayer != null) {
                playerList.add(bukkitPlayer)
            }
        }
        return playerList
    }

    override fun createParty(owner: Player, vararg members: Player) {
    }

    override fun addMember(owner: Player, member: Player?) {
    }

    override fun removeFromParty(member: Player) {
    }

    override fun disband(owner: Player) {
    }

    override fun isMember(owner: Player, check: Player): Boolean {
        val party: PlayerParty? = getPAFParty(owner)
        if (party == null) {
            return false
        }
        return party.isInParty(PAFPlayerManager.getInstance().getPlayer(check.getUniqueId()))
    }

    override fun removePlayer(owner: Player, target: Player) {
    }

    override fun getOwner(member: Player): Player? {
        return Bukkit.getPlayer(getPAFParty(member)!!.getLeader().getUniqueId())
    }

    override fun promote(owner: Player, target: Player) {
    }

    override fun isInternal(): Boolean {
        return false
    }
}
