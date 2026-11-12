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
import com.andrei1058.bedwars.api.command.ParentCommand
import com.andrei1058.bedwars.api.command.SubCommand
import com.andrei1058.bedwars.api.configuration.ConfigPath
import com.andrei1058.bedwars.api.server.SetupType
import com.andrei1058.bedwars.arena.Misc
import com.andrei1058.bedwars.arena.SetupSession
import com.andrei1058.bedwars.commands.Misc.createArmorStand
import com.andrei1058.bedwars.commands.Misc.removeArmorStand
import com.andrei1058.bedwars.configuration.ArenaConfig
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

open class SetKillDropsLoc(parent: ParentCommand, name: String) : SubCommand(parent, name) {
    override fun execute(args: Array<String>, s: CommandSender): Boolean {
        if (s is ConsoleCommandSender) {
            return false
        }
        val p = s as Player
        val ss: SetupSession? = SetupSession.getSession(p.getUniqueId())
        if (ss == null) {
            //s.sendMessage(ss.getPrefix()"§c ▪ §7You're not in a setup session!");
            return false
        }
        val arena = ss.getConfig()
        if (args.size < 1) {
            var foundTeam = ""
            var distance: Double = 100.0
            if (ss.getConfig().getYml().getConfigurationSection("Team") == null) {
                p.sendMessage(ss.getPrefix() + "Please create teams first!")
                com.andrei1058.bedwars.BedWars.nms.sendTitle(p, " ", ChatColor.RED.toString() + "Please create teams first!", 5, 40, 5)
                Sounds.playSound(ConfigPath.SOUNDS_INSUFF_MONEY, p)
                return true
            }
            for (team in ss.getConfig().getYml().getConfigurationSection("Team").getKeys(false)) {
                if (ss.getConfig().getYml().get("Team." + team + ".Spawn") == null) {
                    continue
                }
                val dis: Double = ss.getConfig().getArenaLoc("Team." + team + ".Spawn")!!.distance(p.getLocation())
                if (dis <= ss.getConfig().getInt(ConfigPath.ARENA_ISLAND_RADIUS)) {
                    if (dis < distance) {
                        distance = dis
                        foundTeam = team
                    }
                }
            }
            if (!foundTeam.isEmpty()) {
                if (ss.getConfig().getYml().get("Team." + foundTeam + "." + ConfigPath.ARENA_TEAM_KILL_DROPS_LOC) != null) {
                    removeArmorStand("Kill drops", ss.getConfig().getArenaLoc("Team." + foundTeam + "." + ConfigPath.ARENA_TEAM_KILL_DROPS_LOC).unchecked(), null)
                }
                arena.set("Team." + foundTeam + "." + ConfigPath.ARENA_TEAM_KILL_DROPS_LOC, arena.stringLocationArenaFormat(p.getLocation()))
                val team = ss.getTeamColor(foundTeam).toString() + foundTeam
                p.sendMessage(ss.getPrefix() + "Kill drops set for team: " + team)
                createArmorStand(ChatColor.GOLD.toString() + "Kill drops " + team, p.getLocation(), null)
                com.andrei1058.bedwars.BedWars.nms.sendTitle(p, " ", ChatColor.GREEN.toString() + "Kill drops set for team: " + team, 5, 40, 5)
                Sounds.playSound(ConfigPath.SOUNDS_BOUGHT, p)
                if (ss.getSetupType() == SetupType.ASSISTED) {
                    Bukkit.dispatchCommand(p, getParent().getName())
                }
                return true
            }
            p.sendMessage(ss.getPrefix() + ChatColor.RED + "Usage: /" + mainCmd + " setKillDrops <teamName>")
            return true
        }
        var foundTeam = ss.getNearestTeam()
        if (foundTeam.isEmpty()) {
            p.sendMessage("")
            p.sendMessage(ss.getPrefix() + ChatColor.RED + "Could not find any nearby team.")
            p.spigot().sendMessage(Misc.msgHoverClick(ss.getPrefix() + "Make sure you set the team's spawn first!", ChatColor.WHITE.toString() + "Set a team spawn.", "/" + getParent().getName() + " " + getSubCommandName() + " ", ClickEvent.Action.SUGGEST_COMMAND))
            p.spigot().sendMessage(Misc.msgHoverClick(ss.getPrefix() + "Or if you set the spawn and it wasn't found automatically try using: /bw " + getSubCommandName() + " <team>", "Set kill drops location for a team.", "/" + getParent().getName() + " " + getSubCommandName() + " ", ClickEvent.Action.SUGGEST_COMMAND))
            com.andrei1058.bedwars.BedWars.nms.sendTitle(p, " ", ChatColor.RED.toString() + "Could not find any nearby team.", 5, 60, 5)
            Sounds.playSound(ConfigPath.SOUNDS_INSUFF_MONEY, p)
            return true
        }
        if (args.size == 1) {
            if (arena.getYml().get("Team." + args[0]) != null) {
                foundTeam = args[0]
            } else {
                p.sendMessage(ss.getPrefix() + ChatColor.RED + "This team doesn't exist!")
                if (arena.getYml().get("Team") != null) {
                    p.sendMessage(ss.getPrefix() + "Available teams: ")
                    for (team in Objects.requireNonNull(arena.getYml().getConfigurationSection("Team")).getKeys(false)) {
                        p.spigot().sendMessage(Misc.msgHoverClick(ChatColor.GOLD.toString() + " " + '▪' + " " + "Kill drops " + ss.getTeamColor(team) + team + " " + ChatColor.getLastColors(ss.getPrefix()) + "(click to set)", ChatColor.WHITE.toString() + "Set Kill drops for " + ss.getTeamColor(team) + team, "/" + mainCmd + " setKillDrops " + team, ClickEvent.Action.RUN_COMMAND))
                    }
                }
                return true
            }
        }
        arena.set("Team." + foundTeam + "." + ConfigPath.ARENA_TEAM_KILL_DROPS_LOC, arena.stringLocationArenaFormat(p.getLocation()))
        p.sendMessage(ss.getPrefix() + "Kill drops set for: " + ss.getTeamColor(foundTeam) + foundTeam)
        if (ss.getSetupType() == SetupType.ASSISTED) {
            Bukkit.dispatchCommand(p, getParent().getName())
        }
        return true
    }

    override fun getTabComplete(): MutableList<String>? {
        return ArrayList()
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
