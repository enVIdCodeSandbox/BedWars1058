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

package com.andrei1058.bedwars.commands.bedwars.subcmds.regular

import com.andrei1058.bedwars.BedWars
import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.api.command.ParentCommand
import com.andrei1058.bedwars.api.command.SubCommand
import com.andrei1058.bedwars.api.configuration.ConfigPath
import com.andrei1058.bedwars.api.language.Language.Companion.getMsg
import com.andrei1058.bedwars.api.language.Messages
import com.andrei1058.bedwars.arena.Arena
import com.andrei1058.bedwars.arena.SetupSession
import com.andrei1058.bedwars.configuration.Sounds
import java.util.ArrayList
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player

open class CmdJoin(parent: ParentCommand, name: String) : SubCommand(parent, name) {
    override fun execute(args: Array<String>, s: CommandSender): Boolean {
        if (s is ConsoleCommandSender) {
            return false
        }
        val p = s as Player
        if (args.size < 1) {
            s.sendMessage(getMsg(p, Messages.COMMAND_JOIN_USAGE))
            return true
        }
        if (args[0].equals("random", ignoreCase = true)) {
            if (!Arena.joinRandomArena(p)) {
                s.sendMessage(getMsg(p, Messages.COMMAND_JOIN_NO_EMPTY_FOUND))
                Sounds.playSound("join-denied", p)
            } else {
                Sounds.playSound("join-allowed", p)
            }
            return true
        }
        if (com.andrei1058.bedwars.commands.bedwars.MainCommand.isArenaGroup(args[0]) || args[0].contains("+")) {
            if (!Arena.joinRandomFromGroup(p, args[0])) {
                s.sendMessage(getMsg(p, Messages.COMMAND_JOIN_NO_EMPTY_FOUND))
                Sounds.playSound("join-denied", p)
            } else {
                Sounds.playSound("join-allowed", p)
            }
            return true
        } else if (Arena.getArenaByName(args[0]) != null) {
            if (Arena.getArenaByName(args[0]).addPlayer(p, false)) {
                Sounds.playSound("join-allowed", p)
            } else {
                Sounds.playSound("join-denied", p)
            }
            return true
        } else if (Arena.getArenaByIdentifier(args[0]) != null) {
            if (Arena.getArenaByIdentifier(args[0]).addPlayer(p, false)) {
                Sounds.playSound("join-allowed", p)
            } else {
                Sounds.playSound("join-denied", p)
            }
            return true
        }
        s.sendMessage(getMsg(p, Messages.COMMAND_JOIN_GROUP_OR_ARENA_NOT_FOUND).replace("{name}", args[0]))
        return true
    }

    override fun getTabComplete(): MutableList<String>? {
        val tab: MutableList<String> = ArrayList(BedWars.config.getYml().getStringList(ConfigPath.GENERAL_CONFIGURATION_ARENA_GROUPS))
        for (arena in Arena.getArenas()) {
            tab.add(arena.getArenaName())
        }
        return tab
    }

    override fun canSee(s: CommandSender, api: com.andrei1058.bedwars.api.BedWars): Boolean {
        if (s is ConsoleCommandSender) {
            return false
        }
        val p = s as Player
        if (Arena.isInArena(p)) {
            return false
        }
        if (SetupSession.isInSetupSession(p.getUniqueId())) {
            return false
        }
        return hasPermission(s)
    }

    init {
        setPriority(19)
        showInList(false)
        setDisplayInfo(com.andrei1058.bedwars.commands.bedwars.MainCommand.createTC("§6 ▪ §7/" + com.andrei1058.bedwars.commands.bedwars.MainCommand.getInstance().getName() + " join §e<random/ arena/ groupName>", "/" + getParent().getName() + " " + getSubCommandName(), "§fJoin an arena by name or by group.\n§f/bw join random - join random arena."))
    }
}
