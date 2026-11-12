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
import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.api.command.ParentCommand
import com.andrei1058.bedwars.api.command.SubCommand
import com.andrei1058.bedwars.api.server.ServerType
import com.andrei1058.bedwars.arena.Arena
import com.andrei1058.bedwars.arena.Misc
import com.andrei1058.bedwars.arena.SetupSession
import com.andrei1058.bedwars.commands.bedwars.MainCommand
import java.util.HashMap
import java.util.UUID
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player

open class CmdLeave(parent: ParentCommand, name: String) : SubCommand(parent, name) {
    override fun execute(args: Array<String>, s: CommandSender): Boolean {
        if (s is ConsoleCommandSender) {
            return false
        }
        val p = s as Player
        if (cancel(p.getUniqueId())) {
            return true
        }
        update(p.getUniqueId())
        val a: IArena? = Arena.getArenaByPlayer(p)
        Misc.moveToLobbyOrKick(p, a, a != null && a.isSpectator(p.getUniqueId()))
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
        if (BedWars.getServerType() == ServerType.SHARED && !Arena.isInArena(p)) {
            return false
        }
        if (SetupSession.isInSetupSession(p.getUniqueId())) {
            return false
        }
        return hasPermission(s)
    }

    init {
        setPriority(20)
        showInList(false)
        setDisplayInfo(com.andrei1058.bedwars.commands.bedwars.MainCommand.createTC("§6 ▪ §7/" + MainCommand.getInstance().getName() + " leave", "/" + getParent().getName() + " " + getSubCommandName(), "§fLeave an arena."))
    }

    companion object {
        private var delay: HashMap<UUID, Long> = HashMap()

        @JvmStatic
        private fun cancel(player: UUID): Boolean {
            return delay.getOrDefault(player, 0L) > System.currentTimeMillis()
        }

        @JvmStatic
        private fun update(player: UUID) {
            if (delay.containsKey(player)) {
                delay.replace(player, System.currentTimeMillis() + 2500L)
                return
            }
            delay.put(player, System.currentTimeMillis() + 2500L)
        }
    }
}
