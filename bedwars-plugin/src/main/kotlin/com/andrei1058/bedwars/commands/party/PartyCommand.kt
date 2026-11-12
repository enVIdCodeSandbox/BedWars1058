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

package com.andrei1058.bedwars.commands.party

import com.andrei1058.bedwars.BedWars.Companion.getParty
import com.andrei1058.bedwars.api.language.Language.Companion.getList
import com.andrei1058.bedwars.api.language.Language.Companion.getMsg
import com.andrei1058.bedwars.api.language.Messages
import java.util.HashMap
import java.util.UUID
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.command.defaults.BukkitCommand
import org.bukkit.entity.Player
import com.andrei1058.bedwars.api.util.unchecked

open class PartyCommand(name: String) : BukkitCommand(name) {
    override fun execute(s: CommandSender, c: String, args: Array<String>): Boolean {
        if (s is ConsoleCommandSender) {
            return true
        }
        val p = s as Player
        if (args.size == 0 || args[0].equals("help", ignoreCase = true)) {
            sendPartyCmds(p)
            return true
        }
        when (args[0].lowercase()) {
            "invite" -> {
                if (args.size == 1) {
                    p.sendMessage(getMsg(p, Messages.COMMAND_PARTY_INVITE_USAGE))
                    return true
                }
                if (getParty().hasParty(p) && !getParty().isOwner(p)) {
                    p.sendMessage(getMsg(p, Messages.COMMAND_PARTY_INSUFFICIENT_PERMISSIONS))
                    return true
                }
                if (Bukkit.getPlayer(args[1]) != null && Bukkit.getPlayer(args[1]).isOnline()) {
                    if (p === Bukkit.getPlayer(args[1])) {
                        p.sendMessage(getMsg(p, Messages.COMMAND_PARTY_INVITE_DENIED_CANNOT_INVITE_YOURSELF))
                        return true
                    }
                    p.sendMessage(getMsg(p, Messages.COMMAND_PARTY_INVITE_SENT).replace("{playername}", p.getName()).replace("{player}", args[1]))
                    val tc = TextComponent(getMsg(p, Messages.COMMAND_PARTY_INVITE_SENT_TARGET_RECEIVE_MSG).replace("{player}", p.getName()))
                    tc.setClickEvent(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/party accept " + p.getName()))
                    Bukkit.getPlayer(args[1]).spigot().sendMessage(tc)
                    if (partySessionRequest.containsKey(p.getUniqueId())) {
                        partySessionRequest.replace(p.getUniqueId(), Bukkit.getPlayer(args[1]).getUniqueId())
                    } else {
                        partySessionRequest.put(p.getUniqueId(), Bukkit.getPlayer(args[1]).getUniqueId())
                    }
                } else {
                    p.sendMessage(getMsg(p, Messages.COMMAND_PARTY_INVITE_DENIED_PLAYER_OFFLINE).replace("{player}", args[1]))
                }
            }
            "accept" -> {
                if (args.size < 2) {
                    return true
                }
                if (getParty().hasParty(p)) {
                    p.sendMessage(getMsg(p, Messages.COMMAND_PARTY_ACCEPT_DENIED_ALREADY_IN_PARTY))
                    return true
                }
                if (Bukkit.getPlayer(args[1]) == null || !Bukkit.getPlayer(args[1]).isOnline()) {
                    p.sendMessage(getMsg(p, Messages.COMMAND_PARTY_INVITE_DENIED_PLAYER_OFFLINE).replace("{player}", args[1]))
                    return true
                }
                if (!partySessionRequest.containsKey(Bukkit.getPlayer(args[1]).getUniqueId())) {
                    p.sendMessage(getMsg(p, Messages.COMMAND_PARTY_ACCEPT_DENIED_NO_INVITE))
                    return true
                }
                if (partySessionRequest.get(Bukkit.getPlayer(args[1]).getUniqueId()) == p.getUniqueId()) {
                    partySessionRequest.remove(Bukkit.getPlayer(args[1]).getUniqueId())
                    if (getParty().hasParty(Bukkit.getPlayer(args[1]))) {
                        getParty().addMember(Bukkit.getPlayer(args[1]), p)
                        for (on in getParty().getMembers(Bukkit.getPlayer(args[1]))!!) {
                            on.sendMessage(getMsg(p, Messages.COMMAND_PARTY_ACCEPT_SUCCESS).replace("{playername}", p.getName()).replace("{player}", p.getDisplayName()))
                        }
                    } else {
                        getParty().createParty(Bukkit.getPlayer(args[1]), p)
                        for (on in getParty().getMembers(Bukkit.getPlayer(args[1]))!!) {
                            on.sendMessage(getMsg(p, Messages.COMMAND_PARTY_ACCEPT_SUCCESS).replace("{playername}", p.getName()).replace("{player}", p.getDisplayName()))
                        }
                    }
                } else {
                    p.sendMessage(getMsg(p, Messages.COMMAND_PARTY_ACCEPT_DENIED_NO_INVITE))
                }
            }
            "leave" -> {
                if (!getParty().hasParty(p)) {
                    p.sendMessage(getMsg(p, Messages.COMMAND_PARTY_GENERAL_DENIED_NOT_IN_PARTY))
                    return true
                }
                if (getParty().isOwner(p)) {
                    p.sendMessage(getMsg(p, Messages.COMMAND_PARTY_LEAVE_DENIED_IS_OWNER_NEEDS_DISBAND))
                    return true
                }
                getParty().removeFromParty(p)
            }
            "disband" -> {
                if (!getParty().hasParty(p)) {
                    p.sendMessage(getMsg(p, Messages.COMMAND_PARTY_GENERAL_DENIED_NOT_IN_PARTY))
                    return true
                }
                if (!getParty().isOwner(p)) {
                    p.sendMessage(getMsg(p, Messages.COMMAND_PARTY_INSUFFICIENT_PERMISSIONS))
                    return true
                }
                getParty().disband(p)
            }
            "remove" -> {
                if (args.size == 1) {
                    p.sendMessage(getMsg(p, Messages.COMMAND_PARTY_REMOVE_USAGE))
                    return true
                }
                if (getParty().hasParty(p) && !getParty().isOwner(p)) {
                    p.sendMessage(getMsg(p, Messages.COMMAND_PARTY_INSUFFICIENT_PERMISSIONS))
                    return true
                }
                val target: Player? = Bukkit.getPlayer(args[1])
                if (target == null) {
                    p.sendMessage(getMsg(p, Messages.COMMAND_PARTY_REMOVE_DENIED_TARGET_NOT_PARTY_MEMBER).replace("{player}", args[1]))
                    return true
                }
                if (!getParty().isMember(p, target)) {
                    p.sendMessage(getMsg(p, Messages.COMMAND_PARTY_REMOVE_DENIED_TARGET_NOT_PARTY_MEMBER).replace("{player}", args[1]))
                    return true
                }
                getParty().removePlayer(p, target)
            }
            "promote" -> {
                if (!getParty().hasParty(p)) {
                    p.sendMessage(getMsg(p, Messages.COMMAND_PARTY_GENERAL_DENIED_NOT_IN_PARTY))
                    return true
                } else if (!getParty().isOwner(p)) {
                    p.sendMessage(getMsg(p, Messages.COMMAND_PARTY_INSUFFICIENT_PERMISSIONS))
                    return true
                }
                if (args.size == 1) {
                    this.sendPartyCmds(p)
                    return true
                }
                val target1 = Bukkit.getPlayer(args[1])
                if (!getParty().isMember(p, target1)) {
                    p.sendMessage(getMsg(p, Messages.COMMAND_PARTY_REMOVE_DENIED_TARGET_NOT_PARTY_MEMBER).replace("{player}", args[1]))
                    return true
                }
                getParty().promote(p, target1)
                for (p1 in getParty().getMembers(p)!!) {
                    if (p1 == p) {
                        p1.sendMessage(getMsg(p1, Messages.COMMAND_PARTY_PROMOTE_SUCCESS).replace("{player}", args[1]))
                    } else if (p1 == target1) {
                        p1.sendMessage(getMsg(p1, Messages.COMMAND_PARTY_PROMOTE_OWNER))
                    } else {
                        p1.sendMessage(getMsg(p1, Messages.COMMAND_PARTY_PROMOTE_NEW_OWNER).replace("{player}", args[1]))
                    }
                }
            }
            "info", "list" -> {
                if (!getParty().hasParty(p)) {
                    p.sendMessage(getMsg(p, Messages.COMMAND_PARTY_GENERAL_DENIED_NOT_IN_PARTY))
                    return true
                }
                val owner = getParty().getOwner(p)
                p.sendMessage(getMsg(p, Messages.COMMAND_PARTY_INFO_OWNER).replace("{owner}", owner!!.getName()))
                p.sendMessage(getMsg(p, Messages.COMMAND_PARTY_INFO_PLAYERS))
                for (p1 in getParty().getMembers(owner.unchecked())!!) {
                    p.sendMessage(getMsg(p, Messages.COMMAND_PARTY_INFO_PLAYER).replace("{player}", p1.getName()))
                }
            }
            else -> {
                sendPartyCmds(p)
            }
        }
        return false
    }

    private fun sendPartyCmds(p: Player) {
        for (s in getList(p, Messages.COMMAND_PARTY_HELP)) {
            p.sendMessage(s)
        }
    }

    companion object {
        //owner, target
        private var partySessionRequest: HashMap<UUID, UUID> = HashMap()
    }
}
