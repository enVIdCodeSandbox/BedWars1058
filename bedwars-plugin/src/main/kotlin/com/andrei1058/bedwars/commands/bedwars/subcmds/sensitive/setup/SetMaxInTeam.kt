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
import com.andrei1058.bedwars.arena.SetupSession
import com.andrei1058.bedwars.configuration.Permissions
import java.util.Arrays
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player

open class SetMaxInTeam(parent: ParentCommand, name: String) : SubCommand(parent, name) {
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
            p.sendMessage("§c▪ §7Usage: /" + mainCmd + " setMaxInTeam <int>")
        } else {
            try {
                Integer.parseInt(args[0])
            } catch (ex: Exception) {
                p.sendMessage("§c▪ §7Usage: /" + mainCmd + " setMaxInTeam <int>")
                return true
            }
            ss.getConfig().set("maxInTeam", Integer.valueOf(args[0]))
            p.sendMessage("§6 ▪ §7Max in team set!")
        }
        return true
    }

    override fun getTabComplete(): MutableList<String>? {
        return Arrays.asList("1", "2", "4")
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
