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

import com.andrei1058.bedwars.BedWars
import com.andrei1058.bedwars.api.arena.GameState
import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.api.command.ParentCommand
import com.andrei1058.bedwars.api.command.SubCommand
import com.andrei1058.bedwars.api.configuration.ConfigPath
import com.andrei1058.bedwars.api.language.Language.Companion.getMsg
import com.andrei1058.bedwars.api.language.Messages
import com.andrei1058.bedwars.arena.Arena
import com.andrei1058.bedwars.arena.SetupSession
import com.andrei1058.bedwars.commands.bedwars.MainCommand
import com.andrei1058.bedwars.configuration.Permissions
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player

open class CmdStart(parent: ParentCommand, name: String) : SubCommand(parent, name) {
    override fun execute(args: Array<String>, s: CommandSender): Boolean {
        if (s is ConsoleCommandSender) {
            return false
        }
        val p = s as Player
        val a: IArena? = Arena.getArenaByPlayer(p)
        if (a == null) {
            p.sendMessage(getMsg(p, Messages.COMMAND_FORCESTART_NOT_IN_GAME))
            return true
        }
        if (!a.isPlayer(p)) {
            p.sendMessage(getMsg(p, Messages.COMMAND_FORCESTART_NOT_IN_GAME))
            return true
        }
        if (!(p.hasPermission(Permissions.PERMISSION_ALL) || p.hasPermission(Permissions.PERMISSION_FORCESTART))) {
            p.sendMessage(getMsg(p, Messages.COMMAND_FORCESTART_NO_PERM))
            return true
        }
        if (a.getStatus() == GameState.playing) {
            return true
        }
        if (a.getStatus() == GameState.restarting) {
            return true
        }
        if (a.getStartingTask() == null) {
            if (args.size == 1 && args[0].equals("debug", ignoreCase = true) && s.isOp()) {
                a.changeStatus(GameState.starting)
                BedWars.debug = true
            } else {
                return true
            }
        }
        if (a.getStartingTask()!!.getCountdown() < 5) {
            return true
        }
        a.getStartingTask()!!.setCountdown(5)
        p.sendMessage(getMsg(p, Messages.COMMAND_FORCESTART_SUCCESS))
        return true
    }

    override fun getTabComplete(): MutableList<String>? {
        return null
    }

    override fun canSee(s: CommandSender, api: com.andrei1058.bedwars.api.BedWars): Boolean {
        if (s is ConsoleCommandSender) {
            return false
        }
        val p = s as Player
        val a: IArena? = Arena.getArenaByPlayer(p)
        if (a != null) {
            val status = a.getStatus()
            if (status == GameState.waiting || status == GameState.starting) {
                if (!a.isPlayer(p)) {
                    return false
                }
            } else {
                return false
            }
        } else {
            return false
        }
        if (SetupSession.isInSetupSession(p.getUniqueId())) {
            return false
        }
        return s.hasPermission(Permissions.PERMISSION_FORCESTART)
    }

    init {
        setPriority(15)
        showInList(true)
        setDisplayInfo(com.andrei1058.bedwars.commands.bedwars.MainCommand.createTC("§6 ▪ §7/" + MainCommand.getInstance().getName() + " " + getSubCommandName() + " §8 - §eforce start an arena", "/" + getParent().getName() + " " + getSubCommandName(), "§fForcestart an arena.\n§fPermission: §c" + Permissions.PERMISSION_FORCESTART))
    }
}
