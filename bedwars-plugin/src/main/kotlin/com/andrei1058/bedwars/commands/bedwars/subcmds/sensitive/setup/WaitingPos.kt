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
import com.andrei1058.bedwars.arena.Misc
import com.andrei1058.bedwars.arena.SetupSession
import com.andrei1058.bedwars.configuration.Permissions
import java.util.Arrays
import net.md_5.bungee.api.chat.ClickEvent
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player

open class WaitingPos(parent: ParentCommand, name: String) : SubCommand(parent, name) {
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
            p.sendMessage("§c▪ §7Usage: /" + mainCmd + " " + getSubCommandName() + " 1 or 2")
        } else {
            if (args[0].equals("1", ignoreCase = true) || args[0].equals("2", ignoreCase = true)) {
                p.sendMessage("§6 ▪ §7Pos " + args[0] + " set!")
                ss.getConfig().saveArenaLoc("waiting.Pos" + args[0], p.getLocation())
                ss.getConfig().reload()
                if (ss.getConfig().getYml().get("waiting.Pos1") == null) {
                    p.sendMessage("§c ▪ §7Set the remaining position:")
                    p.spigot().sendMessage(Misc.msgHoverClick("§c ▪ §7/" + BedWars.mainCmd + " waitingPos 1", "§dSet pos 1", "/" + getParent().getName() + " waitingPos 1", ClickEvent.Action.RUN_COMMAND))
                } else if (ss.getConfig().getYml().get("waiting.Pos2") == null) {
                    p.sendMessage("§c ▪ §7Set the remaining position:")
                    p.spigot().sendMessage(Misc.msgHoverClick("§c ▪ §7/" + BedWars.mainCmd + " waitingPos 2", "§dSet pos 2", "/" + getParent().getName() + " waitingPos 2", ClickEvent.Action.RUN_COMMAND))
                }
            } else {
                p.sendMessage("§c▪ §7Usage: /" + mainCmd + " " + getSubCommandName() + " 1 or 2")
            }
        }
        if (!((ss.getConfig().getYml().get("waiting.Pos1") == null || ss.getConfig().getYml().get("waiting.Pos2") == null))) {
            Bukkit.dispatchCommand(p, BedWars.mainCmd + " cmds")
            s.sendMessage("§6 ▪ §7Set teams spawn if you didn't!")
        }
        return true
    }

    override fun getTabComplete(): MutableList<String>? {
        return Arrays.asList("1", "2")
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
