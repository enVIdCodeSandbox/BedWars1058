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

import com.andrei1058.bedwars.api.BedWars
import com.andrei1058.bedwars.api.arena.GameState
import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.api.command.ParentCommand
import com.andrei1058.bedwars.api.command.SubCommand
import com.andrei1058.bedwars.arena.Arena
import com.andrei1058.bedwars.arena.Misc
import com.andrei1058.bedwars.arena.SetupSession
import com.andrei1058.bedwars.commands.bedwars.MainCommand
import java.util.ArrayList
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player

open class CmdStats(parent: ParentCommand, name: String) : SubCommand(parent, name) {
    override fun execute(args: Array<String>, s: CommandSender): Boolean {
        if (s is ConsoleCommandSender) {
            return false
        }
        val p = s as Player
        val a: IArena? = Arena.getArenaByPlayer(p)
        if (a != null) {
            if (!(a.getStatus() == GameState.starting || a.getStatus() == GameState.waiting)) {
                if (!a.isSpectator(p)) {
                    return false
                }
            }
        }
        if (statsCoolDown.containsKey(p.getUniqueId())) {
            if (System.currentTimeMillis() - 3000 >= statsCoolDown.get(p.getUniqueId())!!) {
                statsCoolDown.replace(p.getUniqueId(), System.currentTimeMillis())
            } else {
                //wait 3 seconds
                return true
            }
        } else {
            statsCoolDown.put(p.getUniqueId(), System.currentTimeMillis())
        }
        Misc.openStatsGUI(p)
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
        if (Arena.isInArena(p)) {
            return false
        }
        if (SetupSession.isInSetupSession(p.getUniqueId())) {
            return false
        }
        return hasPermission(s)
    }

    init {
        setPriority(16)
        showInList(false)
        setDisplayInfo(com.andrei1058.bedwars.commands.bedwars.MainCommand.createTC("§6 ▪ §7/" + MainCommand.getInstance().getName() + " " + getSubCommandName(), "/" + getParent().getName() + " " + getSubCommandName(), "§fOpens the stats GUI."))
    }

    companion object {
        private var statsCoolDown: ConcurrentHashMap<UUID, Long> = ConcurrentHashMap()

        @JvmStatic
        fun getStatsCoolDown(): ConcurrentHashMap<UUID, Long> {
            return statsCoolDown
        }
    }
}
