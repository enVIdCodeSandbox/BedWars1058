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

import com.andrei1058.bedwars.BedWars.Companion.link
import com.andrei1058.bedwars.BedWars.Companion.mainCmd
import com.andrei1058.bedwars.BedWars.Companion.plugin
import com.andrei1058.bedwars.api.BedWars
import com.andrei1058.bedwars.api.arena.team.TeamColor
import com.andrei1058.bedwars.api.command.ParentCommand
import com.andrei1058.bedwars.api.command.SubCommand
import com.andrei1058.bedwars.api.configuration.ConfigPath
import com.andrei1058.bedwars.api.language.Language.Companion.getList
import com.andrei1058.bedwars.api.language.Messages
import com.andrei1058.bedwars.api.server.SetupType
import com.andrei1058.bedwars.arena.Arena
import com.andrei1058.bedwars.arena.Misc
import com.andrei1058.bedwars.arena.SetupSession
import java.util.Objects
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player

open class CmdList(parent: ParentCommand, name: String) : SubCommand(parent, name) {
    override fun execute(args: Array<String>, s: CommandSender): Boolean {
        if (s is ConsoleCommandSender) {
            return false
        }
        val p = s as Player
        if (SetupSession.isInSetupSession(p.getUniqueId())) {
            val ss = SetupSession.getSession(p.getUniqueId())
            Objects.requireNonNull(ss)!!.getConfig().reload()
            val waitingSpawn: Boolean = ss!!.getConfig().getYml().get("waiting.Loc") != null
            val pos1: Boolean = ss!!.getConfig().getYml().get("waiting.Pos1") != null
            val pos2: Boolean = ss!!.getConfig().getYml().get("waiting.Pos2") != null
            val pos: Boolean = pos1 && pos2
            val spawnNotSetNames = StringBuilder()
            val bedNotSet = StringBuilder()
            val shopNotSet = StringBuilder()
            val killDropsNotSet = StringBuilder()
            val upgradeNotSet = StringBuilder()
            val spawnNotSet = StringBuilder()
            val generatorNotSet = StringBuilder()
            var teams: Int = 0
            if (ss!!.getConfig().getYml().get("Team") != null) {
                for (team in ss!!.getConfig().getYml().getConfigurationSection("Team").getKeys(true)) {
                    if (ss!!.getConfig().getYml().get("Team." + team + ".Color") == null) {
                        continue
                    }
                    val color = TeamColor.getChatColor(ss!!.getConfig().getYml().getString("Team." + team + ".Color"))
                    if (ss!!.getConfig().getYml().get("Team." + team + ".Spawn") == null) {
                        spawnNotSet.append(color).append("▋")
                        spawnNotSetNames.append(color).append(team).append(" ")
                    }
                    if (ss!!.getConfig().getYml().get("Team." + team + ".Bed") == null) {
                        bedNotSet.append(color).append("▋")
                    }
                    if (ss!!.getConfig().getYml().get("Team." + team + ".Shop") == null) {
                        shopNotSet.append(color).append("▋")
                    }
                    if (ss!!.getConfig().getYml().get("Team." + team + "." + ConfigPath.ARENA_TEAM_KILL_DROPS_LOC) == null) {
                        killDropsNotSet.append(color).append("▋")
                    }
                    if (ss!!.getConfig().getYml().get("Team." + team + ".Upgrade") == null) {
                        upgradeNotSet.append(color).append("▋")
                    }
                    if (ss!!.getConfig().getYml().get("Team." + team + ".Iron") == null || ss!!.getConfig().getYml().get("Team." + team + ".Gold") == null) {
                        generatorNotSet.append(color).append("▋")
                    }
                    teams++
                }
            }
            var emGen: Int = 0
            var dmGen: Int = 0
            if (ss!!.getConfig().getYml().get("generator.Emerald") != null) {
                emGen = ss!!.getConfig().getYml().getStringList("generator.Emerald").size
            }
            if (ss!!.getConfig().getYml().get("generator.Diamond") != null) {
                dmGen = ss!!.getConfig().getYml().getStringList("generator.Diamond").size
            }
            var posMsg: String
            var group = ChatColor.RED.toString() + "(NOT SET)"
            if (pos1 && !pos2) {
                posMsg = ChatColor.RED.toString() + "(POS 2 NOT SET)"
            } else if (!pos1 && pos2) {
                posMsg = ChatColor.RED.toString() + "(POS 1 NOT SET)"
            } else if (pos1) {
                posMsg = ChatColor.GREEN.toString() + "(SET)"
            } else {
                posMsg = ChatColor.GRAY.toString() + "(NOT SET) " + ChatColor.ITALIC + "OPTIONAL"
            }
            val g2: String? = ss!!.getConfig().getYml().getString("group")
            if (g2 != null) {
                if (!g2.equals("default", ignoreCase = true)) {
                    group = ChatColor.GREEN.toString() + "(" + g2 + ")"
                }
            }
            val maxInTeam: Int = ss!!.getConfig().getInt("maxInTeam")
            val setWaitingSpawn = ss!!.dot() + (if (waitingSpawn) ChatColor.STRIKETHROUGH else "") + "setWaitingSpawn" + ChatColor.RESET + " " + (if (waitingSpawn) ChatColor.GREEN.toString() + "(SET)" else ChatColor.RED.toString() + "(NOT SET)")
            val waitingPos = ss!!.dot() + (if (pos) ChatColor.STRIKETHROUGH else "") + "waitingPos 1/2" + ChatColor.RESET + " " + posMsg
            val setSpawn = ss!!.dot() + (if ((spawnNotSet.length == 0)) ChatColor.STRIKETHROUGH else "") + "setSpawn <teamName>" + ChatColor.RESET + " " + (if ((spawnNotSet.length == 0)) ChatColor.GREEN.toString() + "(ALL SET)" else ChatColor.RED.toString() + "(Remaining: " + spawnNotSet + ChatColor.RED + ")")
            val setBed = ss!!.dot() + (if ((bedNotSet.toString().length == 0)) ChatColor.STRIKETHROUGH else "") + "setBed" + ChatColor.RESET + " " + (if ((bedNotSet.length == 0)) ChatColor.GREEN.toString() + "(ALL SET)" else ChatColor.RED.toString() + "(Remaining: " + bedNotSet + ChatColor.RED + ")")
            val setShop = ss!!.dot() + (if ((shopNotSet.toString().length == 0)) ChatColor.STRIKETHROUGH else "") + "setShop" + ChatColor.RESET + " " + (if ((shopNotSet.length == 0)) ChatColor.GREEN.toString() + "(ALL SET)" else ChatColor.RED.toString() + "(Remaining: " + shopNotSet + ChatColor.RED + ")")
            val setKillDrops = ss!!.dot() + (if ((killDropsNotSet.toString().length == 0)) ChatColor.STRIKETHROUGH else "") + "setKillDrops" + ChatColor.RESET + " " + (if ((shopNotSet.length == 0)) ChatColor.GREEN.toString() + "(ALL SET)" else ChatColor.RED.toString() + "(Remaining: " + killDropsNotSet + ChatColor.RED + ")")
            val setUpgrade = ss!!.dot() + (if ((upgradeNotSet.toString().length == 0)) ChatColor.STRIKETHROUGH else "") + "setUpgrade" + ChatColor.RESET + " " + (if ((upgradeNotSet.length == 0)) ChatColor.GREEN.toString() + "(ALL SET)" else ChatColor.RED.toString() + "(Remaining: " + upgradeNotSet + ChatColor.RED + ")")
            val addGenerator = ss!!.dot() + "addGenerator " + (if ((generatorNotSet.toString().length == 0)) "" else ChatColor.RED.toString() + "(Remaining: " + generatorNotSet + ChatColor.RED + ") ") + ChatColor.YELLOW + "(" + ChatColor.DARK_GREEN + "E" + emGen + " " + ChatColor.AQUA + "D" + dmGen + ChatColor.YELLOW + ")"
            val setSpectatorSpawn = ss!!.dot() + (if (ss!!.getConfig().getYml().get(ConfigPath.ARENA_SPEC_LOC) == null) "" else ChatColor.STRIKETHROUGH) + "setSpectSpawn" + ChatColor.RESET + " " + (if (ss!!.getConfig().getYml().get(ConfigPath.ARENA_SPEC_LOC) == null) ChatColor.RED.toString() + "(NOT SET)" else ChatColor.GRAY.toString() + "(SET)")
            s.sendMessage("")
            s.sendMessage(ChatColor.GRAY.toString() + "" + ChatColor.BOLD + com.andrei1058.bedwars.commands.bedwars.MainCommand.getDot() + ChatColor.GOLD + plugin.getDescription().getName() + " v" + plugin.getDescription().getVersion() + ChatColor.GRAY + '-' + " " + ChatColor.GREEN + ss!!.getWorldName() + " commands")
            p.spigot().sendMessage(Misc.msgHoverClick(setWaitingSpawn, ChatColor.WHITE.toString() + "Set the place where players have\n" + ChatColor.WHITE + "to wait before the game starts.", "/" + getParent().getName() + " setWaitingSpawn", if (ss!!.getSetupType() == SetupType.ASSISTED) ClickEvent.Action.RUN_COMMAND else ClickEvent.Action.SUGGEST_COMMAND))
            p.spigot().sendMessage(Misc.msgHoverClick(waitingPos, ChatColor.WHITE.toString() + "Make it so the waiting lobby will disappear at start.\n" + ChatColor.WHITE + "Select it as a world edit region.", "/" + getParent().getName() + " waitingPos ", ClickEvent.Action.SUGGEST_COMMAND))
            if (ss!!.getSetupType() == SetupType.ADVANCED) {
                p.spigot().sendMessage(Misc.msgHoverClick(setSpectatorSpawn, ChatColor.WHITE.toString() + "Set where to spawn spectators.", "/" + getParent().getName() + " setSpectSpawn", ClickEvent.Action.RUN_COMMAND))
            }
            p.spigot().sendMessage(Misc.msgHoverClick(ss!!.dot() + "autoCreateTeams " + ChatColor.YELLOW + "(auto detect)", ChatColor.WHITE.toString() + "Create teams based on islands colors.", "/" + getParent().getName() + " autoCreateTeams", ClickEvent.Action.SUGGEST_COMMAND))
            p.spigot().sendMessage(Misc.msgHoverClick(ss!!.dot() + "createTeam <name> <color> " + ChatColor.YELLOW + "(" + teams + " CREATED)", ChatColor.WHITE.toString() + "Create a team.", "/" + getParent().getName() + " createTeam ", ClickEvent.Action.SUGGEST_COMMAND))
            p.spigot().sendMessage(Misc.msgHoverClick(ss!!.dot() + "removeTeam <name>", ChatColor.WHITE.toString() + "Remove a team by name.", "/" + mainCmd + " removeTeam ", ClickEvent.Action.SUGGEST_COMMAND))
            p.spigot().sendMessage(Misc.msgHoverClick(setSpawn, ChatColor.WHITE.toString() + "Set a team spawn.\n" + ChatColor.WHITE + "Teams without a spawn set:\n" + spawnNotSetNames.toString(), "/" + getParent().getName() + " setSpawn ", ClickEvent.Action.SUGGEST_COMMAND))
            p.spigot().sendMessage(Misc.msgHoverClick(setBed, ChatColor.WHITE.toString() + "Set a team's bed location.\n" + ChatColor.WHITE + "You don't have to specify the team name.", "/" + getParent().getName() + " setBed", if (ss!!.getSetupType() == SetupType.ASSISTED) ClickEvent.Action.RUN_COMMAND else ClickEvent.Action.SUGGEST_COMMAND))
            p.spigot().sendMessage(Misc.msgHoverClick(setShop, ChatColor.WHITE.toString() + "Set a team's NPC.\n" + ChatColor.WHITE + "You don't have to specify the team name.\n" + ChatColor.WHITE + "It will be spawned only when the game starts.", "/" + getParent().getName() + " setShop", if (ss!!.getSetupType() == SetupType.ASSISTED) ClickEvent.Action.RUN_COMMAND else ClickEvent.Action.SUGGEST_COMMAND))
            p.spigot().sendMessage(Misc.msgHoverClick(setUpgrade, ChatColor.WHITE.toString() + "Set a team's upgrade NPC.\n" + ChatColor.WHITE + "You don't have to specify the team name.\n" + ChatColor.WHITE + "It will be spawned only when the game starts.", "/" + getParent().getName() + " setUpgrade", if (ss!!.getSetupType() == SetupType.ASSISTED) ClickEvent.Action.RUN_COMMAND else ClickEvent.Action.SUGGEST_COMMAND))
            if (ss!!.getSetupType() == SetupType.ADVANCED) {
                p.spigot().sendMessage(Misc.msgHoverClick(setKillDrops, ChatColor.WHITE.toString() + "Set a the location where to drop\n" + ChatColor.WHITE + "enemy items after you kill them.", "/" + getParent().getName() + " setKillDrops ", ClickEvent.Action.SUGGEST_COMMAND))
            }
            val genHover = (if (ss!!.getSetupType() == SetupType.ADVANCED) ChatColor.WHITE.toString() + "Add a generator spawn point.\n" + ChatColor.YELLOW + "/" + getParent().getName() + " addGenerator <Iron/ Gold/ Emerald, Diamond>" else ChatColor.WHITE.toString() + "Add a generator spawn point.\n" + ChatColor.YELLOW + "Stay in on a team island to set a team generator") + "\n" + ChatColor.WHITE + "Stay on a diamond block to set the diamond generator.\n" + ChatColor.WHITE + "Stay on a emerald block to set an emerald generator."
            p.spigot().sendMessage(Misc.msgHoverClick(addGenerator, genHover, "/" + getParent().getName() + " addGenerator ", if (ss!!.getSetupType() == SetupType.ASSISTED) ClickEvent.Action.RUN_COMMAND else ClickEvent.Action.SUGGEST_COMMAND))
            p.spigot().sendMessage(Misc.msgHoverClick(ss!!.dot() + "removeGenerator", genHover, "/" + getParent().getName() + " removeGenerator", if (ss!!.getSetupType() == SetupType.ASSISTED) ClickEvent.Action.RUN_COMMAND else ClickEvent.Action.SUGGEST_COMMAND))
            if (ss!!.getSetupType() == SetupType.ADVANCED) {
                p.spigot().sendMessage(Misc.msgHoverClick(ss!!.dot() + "setMaxInTeam <int> (IS SET TO " + maxInTeam + ")", ChatColor.WHITE.toString() + "Set the max team size.", "/" + mainCmd + " setMaxInTeam ", ClickEvent.Action.SUGGEST_COMMAND))
                p.spigot().sendMessage(Misc.msgHoverClick(ss!!.dot() + "arenaGroup " + group, ChatColor.WHITE.toString() + "Set the arena group.", "/" + mainCmd + " arenaGroup ", ClickEvent.Action.SUGGEST_COMMAND))
            } else {
                p.spigot().sendMessage(Misc.msgHoverClick(ss!!.dot() + "setType <type> " + group, ChatColor.WHITE.toString() + "Add the arena to a group.", "/" + getParent().getName() + " setType", ClickEvent.Action.RUN_COMMAND))
            }
            p.spigot().sendMessage(Misc.msgHoverClick(ss!!.dot() + "save", ChatColor.WHITE.toString() + "Save arena and go back to lobby", "/" + getParent().getName() + " save", ClickEvent.Action.SUGGEST_COMMAND))
        } else {
            val credits = TextComponent(ChatColor.BLUE.toString() + "" + ChatColor.BOLD + com.andrei1058.bedwars.commands.bedwars.MainCommand.getDot() + " " + ChatColor.GOLD + plugin.getName() + " " + ChatColor.GRAY + "v" + plugin.getDescription().getVersion() + " by andrei1058")
            credits.setClickEvent(ClickEvent(ClickEvent.Action.OPEN_URL, link))
            credits.setHoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, ComponentBuilder(ChatColor.GRAY.toString() + "Arenas: " + (if (Arena.getArenas().size == 0) ChatColor.RED.toString() + "0" else ChatColor.GREEN.toString() + "" + Arena.getArenas().size)).create()))
            (s as Player).spigot().sendMessage(credits)
            for (string in getList(s as Player, Messages.COMMAND_MAIN)) {
                s.sendMessage(string)
            }
        }
        return true
    }

    override fun getTabComplete(): MutableList<String>? {
        return null
    }

    override fun canSee(s: CommandSender, api: BedWars): Boolean {
        if (s is Player) {
            val p = s as Player
            if (Arena.isInArena(p)) {
                return false
            }
            if (SetupSession.isInSetupSession(p.getUniqueId())) {
                return false
            }
        }
        return hasPermission(s)
    }

    init {
        setPriority(11)
        showInList(true)
        setDisplayInfo(Misc.msgHoverClick("§6 ▪ §7/" + com.andrei1058.bedwars.commands.bedwars.MainCommand.getInstance().getName() + " " + getSubCommandName() + "         §8 - §e view player cmds", "§fView player commands.", "/" + getParent().getName() + " " + getSubCommandName(), ClickEvent.Action.RUN_COMMAND))
    }
}
