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

package com.andrei1058.bedwars.commands.bedwars

import com.andrei1058.bedwars.BedWars
import com.andrei1058.bedwars.BedWars.Companion.config
import com.andrei1058.bedwars.BedWars.Companion.getAPI
import com.andrei1058.bedwars.BedWars.Companion.getServerType
import com.andrei1058.bedwars.BedWars.Companion.mainCmd
import com.andrei1058.bedwars.BedWars.Companion.plugin
import com.andrei1058.bedwars.api.command.ParentCommand
import com.andrei1058.bedwars.api.command.SubCommand
import com.andrei1058.bedwars.api.language.Language
import com.andrei1058.bedwars.api.language.Language.Companion.getMsg
import com.andrei1058.bedwars.api.language.Messages
import com.andrei1058.bedwars.api.server.ServerType
import com.andrei1058.bedwars.arena.SetupSession
import com.andrei1058.bedwars.commands.bedwars.subcmds.regular.*
import com.andrei1058.bedwars.commands.bedwars.subcmds.sensitive.Level
import com.andrei1058.bedwars.commands.bedwars.subcmds.sensitive.Reload
import com.andrei1058.bedwars.commands.bedwars.subcmds.sensitive.setup.*
import com.andrei1058.bedwars.playmenu.CmdPlay
import com.andrei1058.bedwars.support.citizens.JoinNPC
import java.util.ArrayList
import java.util.Arrays
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.command.defaults.BukkitCommand
import org.bukkit.entity.Player
import com.andrei1058.bedwars.api.util.uninitialized

open class MainCommand(name: String) : BukkitCommand(name), ParentCommand {
    override fun execute(s: CommandSender, st: String, args: Array<String>): Boolean {
        if (args.size == 0) {
            /* Set op commands*/
            if ((s.isOp() || s.hasPermission(BedWars.mainCmd + ".*"))) {
                if (s is Player) {
                    if (SetupSession.isInSetupSession((s as Player).getUniqueId())) {
                        Bukkit.dispatchCommand(s, getName() + " cmds")
                    } else {
                        s.sendMessage("")
                        s.sendMessage("§8§l" + dot + " §6" + plugin.getDescription().getName() + " v" + plugin.getDescription().getVersion() + " §7- §c Admin Commands")
                        s.sendMessage("")
                        sendSubCommands(s as Player)
                    }
                } else {
                    s.sendMessage("§f   bw safemode §eenable/ disable")
                }
            } else {
                if (s is ConsoleCommandSender) {
                    s.sendMessage("§fNo console commands available atm.")
                    return true
                }
                /* Send player commands */
                Bukkit.dispatchCommand(s, mainCmd + " cmds")
            }
            return true
        }
        var commandFound: Boolean = false
        for (sb in getSubCommands()) {
            if (sb.getSubCommandName().equals(args[0], ignoreCase = true)) {
                if (sb.hasPermission(s)) {
                    commandFound = sb.execute(Arrays.copyOfRange(args, 1, args.size), s)
                }
            }
        }
        if (!commandFound) {
            if (s is Player) {
                s.sendMessage(getMsg(s as Player, Messages.COMMAND_NOT_FOUND_OR_INSUFF_PERMS))
            } else {
                s.sendMessage(Language.getDefaultLanguage().m(Messages.COMMAND_NOT_FOUND_OR_INSUFF_PERMS))
            }
        }
        return true
    }

    override fun addSubCommand(subCommand: SubCommand) {
        subCommandList.add(subCommand)
    }

    override fun sendSubCommands(p: Player) {
        for (i in 0..20) {
            for (sb in getSubCommands()) {
                if (sb.getPriority() == i && sb.isShow() && sb.canSee(p, BedWars.getAPI())) {
                    p.spigot().sendMessage(sb.getDisplayInfo())
                }
            }
        }
    }

    @Throws(IllegalArgumentException::class)
    open fun tabComplete(s: CommandSender, alias: String, args: Array<String>, location: Location): MutableList<String>? {
        if (args.size == 1) {
            val sub: MutableList<String> = ArrayList()
            for (sb in getSubCommands()) {
                if (sb.canSee(s, BedWars.getAPI())) {
                    sub.add(sb.getSubCommandName())
                }
            }
            return sub
        } else if (args.size == 2) {
            if (hasSubCommand(args[0])) {
                if (getSubCommand(args[0])!!.canSee(s, BedWars.getAPI())) {
                    return getSubCommand(args[0])!!.getTabComplete()
                }
            }
        }
        return null
    }

    override fun getSubCommands(): MutableList<SubCommand> {
        return subCommandList
    }

    override fun hasSubCommand(name: String): Boolean {
        for (sc in getSubCommands()) {
            if (sc.getSubCommandName().equals(name, ignoreCase = true)) {
                return true
            }
        }
        return false
    }

    /**
     * Get sub-command by name
     */
    open fun getSubCommand(name: String): SubCommand? {
        for (sc in getSubCommands()) {
            if (sc.getSubCommandName().equals(name, ignoreCase = true)) {
                return sc
            }
        }
        return null
    }

    init {
        setAliases(Arrays.asList("bedwars", "bedwars1058"))
        instance = this
        CmdJoin(this, "join")
        CmdLeave(this, "leave")
        CmdLang(this, "lang")
        CmdTeleporter(this, "teleporter")
        if (getServerType() != ServerType.BUNGEE) {
            CmdGUI(this, "gui")
            CmdPlay(this, "play")
        }
        CmdStats(this, "stats")
        CmdStart(this, "forceStart")
        CmdStart(this, "start")
        if (BedWars.getServerType() != ServerType.BUNGEE) {
            //priority 1
            com.andrei1058.bedwars.commands.bedwars.subcmds.sensitive.SetLobby(this, "setLobby")
        }
        //priority 2
        com.andrei1058.bedwars.commands.bedwars.subcmds.sensitive.SetupArena(this, "setupArena")
        //priority 3
        com.andrei1058.bedwars.commands.bedwars.subcmds.sensitive.ArenaList(this, "arenaList")
        //priority 4
        com.andrei1058.bedwars.commands.bedwars.subcmds.sensitive.DelArena(this, "delArena")
        //priority 5
        com.andrei1058.bedwars.commands.bedwars.subcmds.sensitive.EnableArena(this, "enableArena")
        //priority 6
        com.andrei1058.bedwars.commands.bedwars.subcmds.sensitive.DisableArena(this, "disableArena")
        //priority 7
        com.andrei1058.bedwars.commands.bedwars.subcmds.sensitive.CloneArena(this, "cloneArena")
        //priority 8
        com.andrei1058.bedwars.commands.bedwars.subcmds.sensitive.ArenaGroup(this, "arenaGroup")
        //priority 9
        com.andrei1058.bedwars.commands.bedwars.subcmds.sensitive.Build(this, "build")
        Level(this, "level")
        //priority 11
        Reload(this, "reload")
        //priority 20
        CmdList(this, "cmds")
        /* Arena setup commands (in world) */
        AutoCreateTeams(this, "autoCreateTeams")
        SetWaitingSpawn(this, "setWaitingSpawn")
        SetSpectatorPos(this, "setSpectSpawn")
        CreateTeam(this, "createTeam")
        com.andrei1058.bedwars.commands.bedwars.subcmds.sensitive.setup.WaitingPos(this, "waitingPos")
        com.andrei1058.bedwars.commands.bedwars.subcmds.sensitive.setup.RemoveTeam(this, "removeTeam")
        com.andrei1058.bedwars.commands.bedwars.subcmds.sensitive.setup.SetMaxInTeam(this, "setMaxInTeam")
        com.andrei1058.bedwars.commands.bedwars.subcmds.sensitive.setup.SetBuildHeight(this, "setMaxBuildHeight")
        com.andrei1058.bedwars.commands.bedwars.subcmds.sensitive.setup.SetSpawn(this, "setSpawn")
        com.andrei1058.bedwars.commands.bedwars.subcmds.sensitive.setup.SetBed(this, "setBed")
        SetShop(this, "setShop")
        com.andrei1058.bedwars.commands.bedwars.subcmds.sensitive.setup.SetUpgrade(this, "setUpgrade")
        com.andrei1058.bedwars.commands.bedwars.subcmds.sensitive.setup.AddGenerator(this, "addGenerator")
        RemoveGenerator(this, "removeGenerator")
        com.andrei1058.bedwars.commands.bedwars.subcmds.sensitive.setup.SetType(this, "setType")
        com.andrei1058.bedwars.commands.bedwars.subcmds.sensitive.setup.Save(this, "save")
        if (JoinNPC.isCitizensSupport() && BedWars.getServerType() != ServerType.BUNGEE) {
            com.andrei1058.bedwars.commands.bedwars.subcmds.sensitive.NPC(this, "npc")
        }
        CmdTpStaff(this, "tp")
        CmdUpgrades(this, "upgradesmenu")
        SetKillDropsLoc(this, "setKillDrops")
    }

    companion object {
        /* SubCommands ArenaList */
        private var subCommandList: MutableList<SubCommand> = ArrayList()
        /* MainCommand instance*/
        private var instance: MainCommand = uninitialized()
        /* Dot char */
        @JvmField var dot: Char = 254.toChar()

        @JvmStatic
        fun isArenaGroup(`var`: String): Boolean {
            if (config.getYml().get("arenaGroups") != null) {
                return config.getYml().getStringList("arenaGroups").contains(`var`)
            }
            return `var`.equals("default", ignoreCase = true)
        }

        @JvmStatic
        fun createTC(text: String, suggest: String, shot_text: String): TextComponent {
            val tx = TextComponent(text)
            tx.setClickEvent(ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, suggest))
            tx.setHoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, ComponentBuilder(shot_text).create()))
            return tx
        }

        /**
         * Get instance
         */
        @JvmStatic
        fun getInstance(): MainCommand {
            return instance
        }

        /**
         * Check if lobby location is set, else send a error message to the player
         */
        @JvmStatic
        fun isLobbySet(p: Player?): Boolean {
            if (BedWars.getServerType() == ServerType.BUNGEE) {
                return true
            }
            if (config.getLobbyWorldName().isEmpty()) {
                if (p != null) {
                    p.sendMessage("§c▪ §7You have to set the lobby location first!")
                }
                return false
            }
            return true
        }

        /**
         * Get a dot symbol
         */
        @JvmStatic
        fun getDot(): Char {
            return dot
        }
    }
}
