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

package com.andrei1058.bedwars.commands.bedwars.subcmds.sensitive.setup

import com.andrei1058.bedwars.BedWars.Companion.mainCmd
import com.andrei1058.bedwars.api.BedWars
import com.andrei1058.bedwars.api.arena.team.TeamColor
import com.andrei1058.bedwars.api.command.ParentCommand
import com.andrei1058.bedwars.api.command.SubCommand
import com.andrei1058.bedwars.api.server.SetupType
import com.andrei1058.bedwars.arena.SetupSession
import com.andrei1058.bedwars.configuration.Permissions
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player

open class CreateTeam(parent: ParentCommand, name: String) : SubCommand(parent, name) {
    override fun execute(args: Array<String>, s: CommandSender): Boolean {
        if (s is ConsoleCommandSender) {
            return false
        }
        val p = s as Player
        val ss: SetupSession? = SetupSession.getSession(p.getUniqueId())
        if (ss == null) {
            s.sendMessage("§c ▪ §7You're not in a setup session!")
            return true
        }
        if (args.size < 2) {
            p.sendMessage("§c▪ §7Usage: /" + mainCmd + " createTeam §o<name> §o<color>")
            var colors = StringBuilder("§7")
            for (t in TeamColor.values()) {
                colors.append(t.chat()).append(t).append(ChatColor.GRAY).append(", ")
            }
            colors = StringBuilder(colors.substring(0, colors.toString().length - 2) + ChatColor.GRAY + ".")
            p.sendMessage("§6 ▪ §7Available colors: " + colors)
        } else {
            var y: Boolean = true
            for (t in TeamColor.values()) {
                if (t.toString().equals(args[1], ignoreCase = true)) {
                    y = false
                }
            }
            if (y) {
                p.sendMessage("§c▪ §7Invalid color!")
                var colors = StringBuilder("§7")
                for (t in TeamColor.values()) {
                    colors.append(t.chat()).append(t).append(ChatColor.GRAY).append(", ")
                }
                colors = StringBuilder(colors.substring(0, colors.toString().length - 2) + ChatColor.GRAY + ".")
                p.sendMessage("§6 ▪ §7Available colors: " + colors)
            } else {
                if (ss.getConfig().getYml().get("Team." + args[0] + ".Color") != null) {
                    p.sendMessage("§c▪ §7" + args[0] + " team already exists!")
                    return true
                }
                ss.getConfig().set("Team." + args[0] + ".Color", args[1].uppercase())
                p.sendMessage("§6 ▪ §7" + TeamColor.getChatColor(args[1]) + args[0] + " §7created!")
                if (ss.getSetupType() == SetupType.ASSISTED) {
                    ss.getConfig().reload()
                    val teams: Int = ss.getConfig().getYml().getConfigurationSection("Team").getKeys(false).size
                    var max: Int = 1
                    if (teams == 4) {
                        max = 2
                    }
                    ss.getConfig().set("maxInTeam", max)
                }
            }
        }
        return true
    }

    override fun getTabComplete(): MutableList<String>? {
        return null
    }

    override fun canSee(s: CommandSender, api: BedWars): Boolean {
        if (s is ConsoleCommandSender) {
            return false
        }
        val p = s as Player
        if (!SetupSession.isInSetupSession(p.getUniqueId())) {
            return false
        }
        return hasPermission(s)
    }

    init {
        setArenaSetupCommand(true)
        setPermission(Permissions.PERMISSION_SETUP_ARENA)
    }
}
