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
import com.andrei1058.bedwars.arena.Misc
import com.andrei1058.bedwars.arena.SetupSession
import com.andrei1058.bedwars.commands.Misc.removeArmorStand
import com.andrei1058.bedwars.configuration.Permissions
import com.andrei1058.bedwars.support.paper.TeleportManager
import java.util.ArrayList
import java.util.Objects
import net.md_5.bungee.api.chat.ClickEvent
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player
import com.andrei1058.bedwars.api.util.unchecked

open class SetSpawn(parent: ParentCommand, name: String) : SubCommand(parent, name) {
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
            p.sendMessage(ss.getPrefix() + ChatColor.RED + "Usage: /" + mainCmd + " setSpawn <team>")
            if (ss.getConfig().getYml().get("Team") != null) {
                for (team in Objects.requireNonNull(ss.getConfig().getYml().getConfigurationSection("Team")).getKeys(false)) {
                    if (ss.getConfig().getYml().get("Team." + team + ".Spawn") == null) {
                        p.spigot().sendMessage(Misc.msgHoverClick(ss.getPrefix() + "Set spawn for: " + ss.getTeamColor(team) + team + " " + ChatColor.getLastColors(ss.getPrefix()) + "(click to set)", ChatColor.WHITE.toString() + "Set spawn for " + ss.getTeamColor(team) + team, "/" + mainCmd + " setSpawn " + team, ClickEvent.Action.RUN_COMMAND))
                    }
                }
            }
        } else {
            if (ss.getConfig().getYml().get("Team." + args[0]) == null) {
                p.sendMessage(ss.getPrefix() + ChatColor.RED + "Could not find target team: " + ChatColor.RED + args[0])
                if (ss.getConfig().getYml().get("Team") != null) {
                    p.sendMessage(ss.getPrefix() + "Teams list: ")
                    for (team in Objects.requireNonNull(ss.getConfig().getYml().getConfigurationSection("Team")).getKeys(false)) {
                        p.spigot().sendMessage(Misc.msgHoverClick(ChatColor.GOLD.toString() + " " + '▪' + " " + ss.getTeamColor(team) + team + " " + ChatColor.getLastColors(ss.getPrefix()) + "(click to set)", ChatColor.WHITE.toString() + "Set spawn for " + ss.getTeamColor(team) + team, "/" + mainCmd + " setSpawn " + team, ClickEvent.Action.RUN_COMMAND))
                    }
                }
            } else {
                if (ss.getConfig().getYml().get("Team." + args[0] + ".Spawn") != null) {
                    removeArmorStand("spawn", ss.getConfig().getArenaLoc("Team." + args[0] + ".Spawn").unchecked(), ss.getConfig().getString("Team." + args[0] + ".Spawn"))
                }
                ss.getConfig().saveArenaLoc("Team." + args[0] + ".Spawn", p.getLocation())
                val teamm = ss.getTeamColor(args[0]).toString() + args[0]
                p.sendMessage(ChatColor.GOLD.toString() + " " + '▪' + " " + "Spawn set for: " + teamm)
                com.andrei1058.bedwars.commands.Misc.createArmorStand(teamm + " " + ChatColor.GOLD + "SPAWN SET", p.getLocation(), ss.getConfig().stringLocationArenaFormat(p.getLocation()))
                val radius: Int = ss.getConfig().getInt(ConfigPath.ARENA_ISLAND_RADIUS)
                val l = p.getLocation()
                for (x in -radius until radius) {
                    for (y in -radius until radius) {
                        for (z in -radius until radius) {
                            val b = l.clone().add(x.toDouble(), y.toDouble(), z.toDouble()).getBlock()
                            if (BedWars.nms.isBed(b.getType())) {
                                TeleportManager.teleport(p, b.getLocation())
                                Bukkit.dispatchCommand(p, getParent().getName() + " setBed " + args[0])
                                return true
                            }
                        }
                    }
                }
                if (ss.getConfig().getYml().get("Team") != null) {
                    val remainging = StringBuilder()
                    for (team in Objects.requireNonNull(ss.getConfig().getYml().getConfigurationSection("Team")).getKeys(false)) {
                        if (ss.getConfig().getYml().get("Team." + team + ".Spawn") == null) {
                            remainging.append(ss.getTeamColor(team)).append(team).append(" ")
                        }
                    }
                    if (remainging.toString().length > 0) {
                        p.sendMessage(ss.getPrefix() + "Remaining: " + remainging.toString())
                    }
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
