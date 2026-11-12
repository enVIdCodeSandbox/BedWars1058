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

import com.andrei1058.bedwars.BedWars
import com.andrei1058.bedwars.BedWars.Companion.mainCmd
import com.andrei1058.bedwars.api.command.ParentCommand
import com.andrei1058.bedwars.api.command.SubCommand
import com.andrei1058.bedwars.api.configuration.ConfigPath
import com.andrei1058.bedwars.api.server.SetupType
import com.andrei1058.bedwars.arena.Misc
import com.andrei1058.bedwars.arena.SetupSession
import com.andrei1058.bedwars.commands.Misc.createArmorStand
import com.andrei1058.bedwars.commands.Misc.removeArmorStand
import com.andrei1058.bedwars.configuration.Permissions
import com.andrei1058.bedwars.configuration.Sounds
import java.util.ArrayList
import java.util.Objects
import net.md_5.bungee.api.chat.ClickEvent
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player
import com.andrei1058.bedwars.api.util.unchecked

open class SetBed(parent: ParentCommand, name: String) : SubCommand(parent, name) {
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
        if (args.size == 0) {
            val foundTeam = ss.getNearestTeam()
            if (foundTeam.isEmpty()) {
                p.sendMessage("")
                p.sendMessage(ss.getPrefix() + ChatColor.RED + "Could not find any nearby team.")
                p.spigot().sendMessage(Misc.msgHoverClick(ss.getPrefix() + "Make sure you set the team's spawn first!", ChatColor.WHITE.toString() + "Set a team bed.", "/" + getParent().getName() + " " + getSubCommandName() + " ", ClickEvent.Action.SUGGEST_COMMAND))
                p.spigot().sendMessage(Misc.msgHoverClick(ss.getPrefix() + "Or if you set the spawn and it wasn't found automatically try using: /bw " + getSubCommandName() + " <team>", "Add a team bed.", "/" + getParent().getName() + " " + getSubCommandName() + " ", ClickEvent.Action.SUGGEST_COMMAND))
                com.andrei1058.bedwars.BedWars.nms.sendTitle(p, " ", ChatColor.RED.toString() + "Could not find any nearby team.", 5, 60, 5)
                Sounds.playSound(ConfigPath.SOUNDS_INSUFF_MONEY, p)
                ss.displayAvailableTeams()
            } else {
                Bukkit.dispatchCommand(s, getParent().getName() + " " + getSubCommandName() + " " + foundTeam)
            }
        } else {
            if (!(BedWars.nms.isBed(p.getLocation().clone().add(0.0, -0.5, 0.0).getBlock().getType()) || BedWars.nms.isBed(p.getLocation().clone().add(0.0, 0.5, 0.0).getBlock().getType()) || BedWars.nms.isBed(p.getLocation().clone().getBlock().getType()))) {
                p.sendMessage(ss.getPrefix() + ChatColor.RED + "You must stay on a bed while using this command!")
                com.andrei1058.bedwars.BedWars.nms.sendTitle(p, " ", ChatColor.RED.toString() + "You must stay on a bed.", 5, 40, 5)
                Sounds.playSound(ConfigPath.SOUNDS_INSUFF_MONEY, p)
                return true
            }
            if (ss.getConfig().getYml().get("Team." + args[0]) == null) {
                p.sendMessage(ss.getPrefix() + ChatColor.RED + "This team doesn't exist!")
                if (ss.getConfig().getYml().get("Team") != null) {
                    p.sendMessage(ss.getPrefix() + "Available teams: ")
                    for (team in Objects.requireNonNull(ss.getConfig().getYml().getConfigurationSection("Team")).getKeys(false)) {
                        p.spigot().sendMessage(Misc.msgHoverClick(ChatColor.GOLD.toString() + " " + '▪' + " " + ss.getTeamColor(team) + team, ChatColor.WHITE.toString() + "Set bed for " + ss.getTeamColor(team) + team, "/" + mainCmd + " setBed " + team, ClickEvent.Action.RUN_COMMAND))
                    }
                }
            } else {
                val team = ss.getTeamColor(args[0]).toString() + args[0]
                if (ss.getConfig().getYml().get("Team." + args[0] + ".Bed") != null) {
                    removeArmorStand("bed", ss.getConfig().getArenaLoc("Team." + args[0] + ".Bed").unchecked(), null)
                }
                createArmorStand(team + " " + ChatColor.GOLD + "BED SET", p.getLocation().add(0.5, 0.0, 0.5), null)
                ss.getConfig().saveArenaLoc("Team." + args[0] + ".Bed", p.getLocation())
                p.sendMessage(ss.getPrefix() + "Bed set for: " + team)
                com.andrei1058.bedwars.BedWars.nms.sendTitle(p, " ", ChatColor.GREEN.toString() + "Bed set for: " + team, 5, 40, 5)
                Sounds.playSound(ConfigPath.SOUNDS_BOUGHT, p)
                if (ss.getSetupType() == SetupType.ASSISTED) {
                    Bukkit.dispatchCommand(p, getParent().getName())
                }
            }
        }
        return true
    }

    override fun getTabComplete(): MutableList<String>? {
        return ArrayList()
    }

    override fun canSee(s: CommandSender, api: com.andrei1058.bedwars.api.BedWars): Boolean {
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
