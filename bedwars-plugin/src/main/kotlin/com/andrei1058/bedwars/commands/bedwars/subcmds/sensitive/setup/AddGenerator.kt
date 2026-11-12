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

import com.andrei1058.bedwars.api.BedWars
import com.andrei1058.bedwars.api.command.ParentCommand
import com.andrei1058.bedwars.api.command.SubCommand
import com.andrei1058.bedwars.api.configuration.ConfigPath
import com.andrei1058.bedwars.api.server.SetupType
import com.andrei1058.bedwars.arena.Misc
import com.andrei1058.bedwars.arena.SetupSession
import com.andrei1058.bedwars.commands.Misc.createArmorStand
import com.andrei1058.bedwars.configuration.Permissions
import com.andrei1058.bedwars.configuration.Sounds
import java.util.ArrayList
import java.util.Arrays
import net.md_5.bungee.api.chat.ClickEvent
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player

open class AddGenerator(parent: ParentCommand, name: String) : SubCommand(parent, name) {
    override fun execute(args: Array<String>, s: CommandSender): Boolean {
        if (s is ConsoleCommandSender) {
            return false
        }
        val p = s as Player
        val ss: SetupSession? = SetupSession.getSession(p.getUniqueId())
        if (ss == null) {
            //s.sendMessage(ChatColor.RED + "You're not in a setup session!");
            return false
        }
        if (args.size == 0 && ss.getSetupType() == SetupType.ASSISTED) {
            var team = ss.getNearestTeam()
            if (team.isEmpty()) {
                // save emerald or diamond generator if is standing on a block of this type
                if (p.getLocation().add(0.0, -1.0, 0.0).getBlock().getType() == Material.DIAMOND_BLOCK) {
                    Bukkit.dispatchCommand(p, getParent().getName() + " " + getSubCommandName() + " diamond")
                    return true
                } else if (p.getLocation().add(0.0, -1.0, 0.0).getBlock().getType() == Material.EMERALD_BLOCK) {
                    Bukkit.dispatchCommand(p, getParent().getName() + " " + getSubCommandName() + " emerald")
                    return true
                }
                // else send usage message
                p.sendMessage(ss.getPrefix() + ChatColor.RED + "Could not find any nearby team.")
                p.spigot().sendMessage(Misc.msgHoverClick(ss.getPrefix() + "Make sure you set the team's spawn first!", ChatColor.WHITE.toString() + "Set a team spawn.", "/" + getParent().getName() + " " + getSubCommandName() + " ", ClickEvent.Action.SUGGEST_COMMAND))
                p.spigot().sendMessage(Misc.msgHoverClick(ss.getPrefix() + "Or if you set the spawn and it wasn't found automatically try using: /bw addGenerator <team>", "Add a team generator.", "/" + getParent().getName() + " " + getSubCommandName() + " ", ClickEvent.Action.SUGGEST_COMMAND))
                p.spigot().sendMessage(Misc.msgHoverClick(ss.getPrefix() + "Other use: /bw addGenerator <emerald/ diamond>", "Add an emerald/ diamond generator.", "/" + getParent().getName() + " " + getSubCommandName() + " ", ClickEvent.Action.SUGGEST_COMMAND))
                com.andrei1058.bedwars.BedWars.nms.sendTitle(p, " ", ChatColor.RED.toString() + "Could not find any nearby team.", 5, 60, 5)
                Sounds.playSound(ConfigPath.SOUNDS_INSUFF_MONEY, p)
                return true
            }
            // save team generators
            saveTeamGen(p.getLocation(), team, ss, "Iron")
            saveTeamGen(p.getLocation(), team, ss, "Gold")
            saveTeamGen(p.getLocation(), team, ss, "Emerald")
            createArmorStand(ChatColor.GOLD.toString() + "Generator set for team: " + ss.getTeamColor(team) + team, p.getLocation(), ss.getConfig().stringLocationArenaFormat(p.getLocation()))
            p.sendMessage(ss.getPrefix() + "Generator set for team: " + ss.getTeamColor(team) + team)
            Bukkit.dispatchCommand(p, getParent().getName())
            com.andrei1058.bedwars.BedWars.nms.sendTitle(p, " ", ChatColor.GREEN.toString() + "Generator set for team: " + ss.getTeamColor(team) + team, 5, 60, 5)
            Sounds.playSound(ConfigPath.SOUNDS_BOUGHT, p)
            return true
        } else if (args.size == 1 && (args[0].equals("diamond", ignoreCase = true) || args[0].equals("emerald", ignoreCase = true))) {
            // add emerald or diamond generator to the list if it was not added yet
            val locations = ss.getConfig().getArenaLocations("generator." + args[0].substring(0, 1).uppercase() + args[0].substring(1).lowercase())
            for (l in locations) {
                if (ss.getConfig().compareArenaLoc(l, p.getLocation())) {
                    p.sendMessage(ss.getPrefix() + ChatColor.RED + "This generator was already set!")
                    com.andrei1058.bedwars.BedWars.nms.sendTitle(p, " ", ChatColor.RED.toString() + "This generator was already set!", 5, 30, 5)
                    Sounds.playSound(ConfigPath.SOUNDS_INSUFF_MONEY, p)
                    return true
                }
            }
            var gen = args[0].substring(0, 1).uppercase() + args[0].substring(1).lowercase()
            var saved: ArrayList<String>
            if (ss.getConfig().getYml().get("generator." + gen) == null) {
                saved = ArrayList()
            } else {
                saved = ss.getConfig().getYml().getStringList("generator." + gen) as ArrayList<String>
            }
            saved.add(ss.getConfig().stringLocationArenaFormat(p.getLocation()))
            ss.getConfig().set("generator." + gen, saved)
            p.sendMessage(ss.getPrefix() + gen + " generator was added!")
            createArmorStand(ChatColor.GOLD.toString() + gen + " SET", p.getLocation(), ss.getConfig().stringLocationArenaFormat(p.getLocation()))
            if (ss.getSetupType() == SetupType.ASSISTED) {
                Bukkit.dispatchCommand(p, getParent().getName())
            }
            com.andrei1058.bedwars.BedWars.nms.sendTitle(p, " ", ChatColor.GOLD.toString() + gen + ChatColor.GREEN + " generator added!", 5, 60, 5)
            Sounds.playSound(ConfigPath.SOUNDS_BOUGHT, p)
            return true
        } else if (args.size >= 1 && (args[0].equals("iron", ignoreCase = true) || args[0].equals("gold", ignoreCase = true) || args[0].equals("upgrade", ignoreCase = true)) && ss.getSetupType() == SetupType.ADVANCED) {
            var team: String
            if (args.size == 1) {
                team = ss.getNearestTeam()
            } else {
                team = args[1]
                if (ss.getConfig().getYml().get("Team." + team + ".Color") == null) {
                    p.sendMessage(ss.getPrefix() + ChatColor.RED + "Could not find team: " + team)
                    p.sendMessage(ss.getPrefix() + "Use: /bw createTeam if you want to create one.")
                    ss.displayAvailableTeams()
                    com.andrei1058.bedwars.BedWars.nms.sendTitle(p, " ", ChatColor.RED.toString() + "Could not find any nearby team.", 5, 60, 5)
                    Sounds.playSound(ConfigPath.SOUNDS_INSUFF_MONEY, p)
                    return true
                }
            }
            // find nearest team to set the generator else send usage msg
            if (team.isEmpty()) {
                p.sendMessage(ss.getPrefix() + ChatColor.RED + "Could not find any nearby team.")
                p.sendMessage(ss.getPrefix() + "Try using: /bw addGenerator <iron/ gold/ upgrade> <team>")
                return true
            }
            var gen = args[0].substring(0, 1).uppercase() + args[0].substring(1).lowercase()
            if (gen.equals("upgrade", ignoreCase = true)) {
                gen = "Emerald"
            }
            createArmorStand(ChatColor.GOLD.toString() + gen + " generator added for team: " + ss.getTeamColor(team) + team, p.getLocation(), ss.getConfig().stringLocationArenaFormat(p.getLocation()))
            p.sendMessage(ss.getPrefix() + gen + " generator added for team: " + ss.getTeamColor(team) + team)
            saveTeamGen(p.getLocation(), team, ss, gen)
            com.andrei1058.bedwars.BedWars.nms.sendTitle(p, " ", ChatColor.GOLD.toString() + gen + ChatColor.GREEN + " generator for " + ss.getTeamColor(team) + team + ChatColor.GREEN + " was added!", 5, 60, 5)
            Sounds.playSound(ConfigPath.SOUNDS_BOUGHT, p)
            return true
        } else if (args.size == 1 && ss.getSetupType() == SetupType.ASSISTED) {
            var team = args[0]
            if (ss.getConfig().getYml().get("Team." + team + ".Color") == null) {
                p.sendMessage(ss.getPrefix() + "Could not find team: " + ChatColor.RED + team)
                p.sendMessage(ss.getPrefix() + "Use: /bw createTeam if you want to create one.")
                ss.displayAvailableTeams()
                com.andrei1058.bedwars.BedWars.nms.sendTitle(p, " ", "Could not find team: " + ChatColor.RED + team, 5, 40, 5)
                Sounds.playSound(ConfigPath.SOUNDS_INSUFF_MONEY, p)
                return true
            }
            saveTeamGen(p.getLocation(), team, ss, "Iron")
            saveTeamGen(p.getLocation(), team, ss, "Gold")
            saveTeamGen(p.getLocation(), team, ss, "Emerald")
            createArmorStand(ChatColor.GOLD.toString() + "Generator set for team: " + ss.getTeamColor(team) + team, p.getLocation(), ss.getConfig().stringLocationArenaFormat(p.getLocation()))
            p.sendMessage(ss.getPrefix() + "Generator set for team: " + ss.getTeamColor(team) + team)
            Bukkit.dispatchCommand(p, getParent().getName())
            com.andrei1058.bedwars.BedWars.nms.sendTitle(p, " ", ChatColor.GREEN.toString() + "Generator set for team: " + ss.getTeamColor(team) + team, 5, 60, 5)
            Sounds.playSound(ConfigPath.SOUNDS_BOUGHT, p)
            return true
        }
        if (ss.getSetupType() == SetupType.ASSISTED) {
            p.spigot().sendMessage(Misc.msgHoverClick(ss.getPrefix() + "/bw addGenerator (detect team automatically)", "Add a team generator.", "/" + getParent().getName() + " " + getSubCommandName() + " ", ClickEvent.Action.SUGGEST_COMMAND))
            p.spigot().sendMessage(Misc.msgHoverClick(ss.getPrefix() + "/bw addGenerator <team>", "Add a team generator.", "/" + getParent().getName() + " " + getSubCommandName() + " ", ClickEvent.Action.SUGGEST_COMMAND))
        }
        if (ss.getSetupType() == SetupType.ADVANCED) {
            p.spigot().sendMessage(Misc.msgHoverClick(ss.getPrefix() + "/bw addGenerator <iron/ gold/ upgrade>", "Add a team generator.\nThe team will be detected automatically.", "/" + getParent().getName() + " " + getSubCommandName() + " ", ClickEvent.Action.SUGGEST_COMMAND))
            p.spigot().sendMessage(Misc.msgHoverClick(ss.getPrefix() + "/bw addGenerator <iron/ gold/ upgrade> <team>", "Add a team generator.", "/" + getParent().getName() + " " + getSubCommandName() + " ", ClickEvent.Action.SUGGEST_COMMAND))
        }
        p.spigot().sendMessage(Misc.msgHoverClick(ss.getPrefix() + "/bw addGenerator <emerald/ diamond>", "Add an emerald/ diamond generator.", "/" + getParent().getName() + " " + getSubCommandName() + " ", ClickEvent.Action.SUGGEST_COMMAND))
        return true
    }

    override fun getTabComplete(): MutableList<String>? {
        return Arrays.asList("Diamond", "Emerald", "Iron", "Gold", "Upgrade")
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

    companion object {
        /**
         * Save team generator.
         *
         * @param l    location.
         * @param t    team.
         * @param ss   setup session.
         * @param type Iron/ Gold.
         */
        @JvmStatic
        private fun saveTeamGen(l: Location, t: String, ss: SetupSession, type: String) {
            val o: Any? = ss.getConfig().getYml().get("Team." + t + "." + type)
            var locs: MutableList<String>
            if (o == null) {
                locs = ArrayList()
            } else if (o is String) {
                locs = ArrayList()
                locs.add(o as String)
            } else {
                locs = ss.getConfig().getList("Team." + t + "." + type)
            }
            locs.add(ss.getConfig().stringLocationArenaFormat(l))
            ss.getConfig().set("Team." + t + "." + type, locs)
        }
    }
}
