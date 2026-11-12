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

package com.andrei1058.bedwars.commands.bedwars.subcmds.sensitive

import com.andrei1058.bedwars.BedWars
import com.andrei1058.bedwars.api.command.ParentCommand
import com.andrei1058.bedwars.api.command.SubCommand
import com.andrei1058.bedwars.arena.Arena
import com.andrei1058.bedwars.arena.Arena.Companion.getArenaByName
import com.andrei1058.bedwars.arena.Misc
import com.andrei1058.bedwars.arena.SetupSession
import com.andrei1058.bedwars.commands.bedwars.MainCommand
import com.andrei1058.bedwars.configuration.Permissions
import net.md_5.bungee.api.chat.ClickEvent
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player

open class SetupArena(parent: ParentCommand, name: String) : SubCommand(parent, name) {
    override fun execute(args: Array<String>, s: CommandSender): Boolean {
        if (s is ConsoleCommandSender) {
            return false
        }
        val p = s as Player
        if (!MainCommand.isLobbySet(p)) {
            return true
        }
        if (args.size != 1) {
            p.sendMessage("§c▪ §7Usage: §o/" + getParent().getName() + " " + getSubCommandName() + " <mapName>")
            return true
        }
        if (args[0] != args[0].lowercase()) {
            p.sendMessage("§c▪ §c" + args[0] + ChatColor.GRAY + " mustn't contain capital letters! Rename your folder to: " + ChatColor.GREEN + args[0].lowercase())
            return true
        }
        if (args[0].contains("+")) {
            p.sendMessage("§c▪ §7" + args[0] + " mustn't contain this symbol: " + ChatColor.RED + "+")
            return true
        }
        //}
        if (getArenaByName(args[0]) != null && !BedWars.autoscale) {
            p.sendMessage("§c▪ §7Please disable it first!")
            return true
        }
        if (SetupSession.isInSetupSession(p.getUniqueId())) {
            p.sendMessage("§c ▪ §7You're already in a setup session!")
            return true
        }
        SetupSession(p, args[0])
        return true
    }

    override fun getTabComplete(): MutableList<String>? {
        return BedWars.getAPI().getRestoreAdapter().getWorldsList()
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
        setPriority(2)
        setDisplayInfo(Misc.msgHoverClick("§6 ▪ §7/" + com.andrei1058.bedwars.commands.bedwars.MainCommand.getInstance().getName() + " setupArena §6<worldName>", "§fCreate or edit an arena.\n'_' and '-' will not be displayed in the arena's name.", "/" + com.andrei1058.bedwars.commands.bedwars.MainCommand.getInstance().getName() + " setupArena ", ClickEvent.Action.SUGGEST_COMMAND))
        showInList(true)
        setPermission(Permissions.PERMISSION_SETUP_ARENA)
    }
}
