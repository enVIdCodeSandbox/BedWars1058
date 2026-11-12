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
import com.andrei1058.bedwars.api.configuration.ConfigPath
import com.andrei1058.bedwars.arena.Misc
import com.andrei1058.bedwars.arena.SetupSession
import com.andrei1058.bedwars.commands.Misc.removeArmorStand
import com.andrei1058.bedwars.configuration.Permissions
import com.andrei1058.bedwars.configuration.Sounds
import java.util.Objects
import net.md_5.bungee.api.chat.ClickEvent
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player
import com.andrei1058.bedwars.api.util.unchecked

open class RemoveTeam(parent: ParentCommand, name: String) : SubCommand(parent, name) {
    override fun execute(args: Array<String>, s: CommandSender): Boolean {
        if (s is ConsoleCommandSender) {
            return false
        }
        val p = s as Player
        val ss: SetupSession? = SetupSession.getSession(p.getUniqueId())
        if (ss == null) {
            //s.sendMessage("§c ▪ §7You're not in a setup session!");
            return false
        }
        if (args.size < 1) {
            p.sendMessage(ss.getPrefix() + ChatColor.RED + "Usage: /" + mainCmd + " removeTeam <teamName>")
            if (ss.getConfig().getYml().get("Team") != null) {
                p.sendMessage(ss.getPrefix() + "Available teams: ")
                for (team in Objects.requireNonNull(ss.getConfig().getYml().getConfigurationSection("Team")).getKeys(false)) {
                    p.spigot().sendMessage(Misc.msgHoverClick(ChatColor.GOLD.toString() + " " + '▪' + " " + TeamColor.getChatColor(team) + team, ChatColor.GRAY.toString() + "Remove " + TeamColor.getChatColor(team) + team + " " + ChatColor.GRAY + "(click to remove)", "/" + mainCmd + " removeTeam " + team, ClickEvent.Action.RUN_COMMAND))
                }
            }
        } else {
            if (ss.getConfig().getYml().get("Team." + args[0] + ".Color") == null) {
                p.sendMessage(ss.getPrefix() + "This team doesn't exist: " + args[0])
                com.andrei1058.bedwars.BedWars.nms.sendTitle(p, " ", ChatColor.RED.toString() + "Team not found: " + args[0], 5, 40, 5)
                Sounds.playSound(ConfigPath.SOUNDS_INSUFF_MONEY, p)
            } else {
                if (ss.getConfig().getYml().get("Team." + args[0] + ".Iron") != null) {
                    for (loc in ss.getConfig().getArenaLocations("Team." + args[0] + ".Iron")) {
                        removeArmorStand(null, loc, null)
                    }
                }
                if (ss.getConfig().getYml().get("Team." + args[0] + ".Gold") != null) {
                    for (loc in ss.getConfig().getArenaLocations("Team." + args[0] + ".Gold")) {
                        removeArmorStand(null, loc, null)
                    }
                }
                if (ss.getConfig().getYml().get("Team." + args[0] + ".Emerald") != null) {
                    for (loc in ss.getConfig().getArenaLocations("Team." + args[0] + ".Emerald")) {
                        removeArmorStand(null, loc, null)
                    }
                }
                if (ss.getConfig().getYml().get("Team." + args[0] + ".Shop") != null) {
                    removeArmorStand(null, ss.getConfig().getArenaLoc("Team." + args[0] + ".Shop").unchecked(), null)
                }
                if (ss.getConfig().getYml().get("Team." + args[0] + ".Upgrade") != null) {
                    removeArmorStand(null, ss.getConfig().getArenaLoc("Team." + args[0] + ".Upgrade").unchecked(), null)
                }
                if (ss.getConfig().getYml().get("Team." + args[0] + "." + ConfigPath.ARENA_TEAM_KILL_DROPS_LOC) != null) {
                    removeArmorStand(null, ss.getConfig().getArenaLoc("Team." + args[0] + "." + ConfigPath.ARENA_TEAM_KILL_DROPS_LOC).unchecked(), null)
                }
                p.sendMessage(ss.getPrefix() + "Team removed: " + ss.getTeamColor(args[0]) + args[0])
                com.andrei1058.bedwars.BedWars.nms.sendTitle(p, " ", ChatColor.GREEN.toString() + "Team removed: " + ss.getTeamColor(args[0]) + args[0], 5, 40, 5)
                Sounds.playSound(ConfigPath.SOUNDS_BOUGHT, p)
                ss.getConfig().set("Team." + args[0], null)
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
