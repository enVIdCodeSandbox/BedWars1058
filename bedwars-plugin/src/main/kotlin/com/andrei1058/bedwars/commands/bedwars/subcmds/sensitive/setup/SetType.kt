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
import com.andrei1058.bedwars.BedWars.Companion.config
import com.andrei1058.bedwars.api.command.ParentCommand
import com.andrei1058.bedwars.api.command.SubCommand
import com.andrei1058.bedwars.api.configuration.ConfigPath
import com.andrei1058.bedwars.api.server.SetupType
import com.andrei1058.bedwars.arena.Misc
import com.andrei1058.bedwars.arena.SetupSession
import com.andrei1058.bedwars.configuration.Permissions
import java.util.Arrays
import net.md_5.bungee.api.chat.ClickEvent
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player

open class SetType(parent: ParentCommand, name: String) : SubCommand(parent, name) {
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
        if (args.size == 0) {
            sendUsage(p)
        } else {
            if (!available.contains(args[0])) {
                sendUsage(p)
                return true
            }
            val groups = BedWars.config.getYml().getStringList(ConfigPath.GENERAL_CONFIGURATION_ARENA_GROUPS)
            val input = args[0].substring(0, 1).uppercase() + args[0].substring(1).lowercase()
            if (!groups.contains(input)) {
                groups.add(input)
                BedWars.config.set(ConfigPath.GENERAL_CONFIGURATION_ARENA_GROUPS, groups)
            }
            if (input == "Solo") {
                ss.getConfig().set("maxInTeam", 1)
            } else if (input.equals("Doubles", ignoreCase = true)) {
                ss.getConfig().set("maxInTeam", 2)
            } else if (input.equals("3v3v3v3", ignoreCase = true)) {
                ss.getConfig().set("maxInTeam", 3)
            } else if (input.equals("4v4v4v4", ignoreCase = true)) {
                ss.getConfig().set("maxInTeam", 4)
            }
            ss.getConfig().set("group", input)
            p.sendMessage("§6 ▪ §7Arena group changed to: §d" + input)
            if (ss.getSetupType() == SetupType.ASSISTED) {
                Bukkit.dispatchCommand(p, getParent().getName())
            }
        }
        return true
    }

    override fun getTabComplete(): MutableList<String>? {
        val groups = BedWars.config.getYml().getStringList(ConfigPath.GENERAL_CONFIGURATION_ARENA_GROUPS)
        available.forEach({ available ->
            if (!groups.contains(available)) {
                groups.add(available)
            }
        })
        return config.getYml().getStringList(ConfigPath.GENERAL_CONFIGURATION_ARENA_GROUPS)
    }

    private fun sendUsage(p: Player) {
        p.sendMessage("§9 ▪ §7Usage: " + getParent().getName() + " " + getSubCommandName() + " <type>")
        p.sendMessage("§9Available types: ")
        for (st in available) {
            p.spigot().sendMessage(Misc.msgHoverClick("§1 ▪ §e" + st + " §7(click to set)", "§dClick to make the arena " + st, "/" + getParent().getName() + " " + getSubCommandName() + " " + st, ClickEvent.Action.RUN_COMMAND))
        }
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

    companion object {
        private val available: MutableList<String> = Arrays.asList("Solo", "Doubles", "3v3v3v3", "4v4v4v4")
    }
}
