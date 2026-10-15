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

package com.andrei1058.bedwars.commands.rejoin

import com.andrei1058.bedwars.api.language.Language
import com.andrei1058.bedwars.api.language.Messages
import com.andrei1058.bedwars.arena.ReJoin
import com.andrei1058.bedwars.configuration.Permissions
import com.andrei1058.bedwars.configuration.Sounds
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.command.defaults.BukkitCommand
import org.bukkit.entity.Player

open class RejoinCommand(name: String) : BukkitCommand(name) {
    override fun execute(s: CommandSender, st: String, args: Array<String>): Boolean {
        if (s is ConsoleCommandSender) {
            s.sendMessage("This command is for players!")
            return true
        }
        attemptRejoin(s as Player)
        return true
    }

    companion object {
        /**
         * Send a player back to the game he left, if he can.
         * Shared by the /rejoin command and the reconnect button of the play menu.
         */
        @JvmStatic
        fun attemptRejoin(p: Player) {
            if (!p.hasPermission(Permissions.PERMISSION_REJOIN)) {
                p.sendMessage(Language.getMsg(p, Messages.COMMAND_NOT_FOUND_OR_INSUFF_PERMS))
                return
            }
            val rj: ReJoin? = ReJoin.getPlayer(p)
            if (rj == null) {
                p.sendMessage(Language.getMsg(p, Messages.REJOIN_NO_ARENA))
                Sounds.playSound("rejoin-denied", p)
                return
            }
            if (!rj.canReJoin()) {
                p.sendMessage(Language.getMsg(p, Messages.REJOIN_DENIED))
                Sounds.playSound("rejoin-denied", p)
                return
            }
            p.sendMessage(Language.getMsg(p, Messages.REJOIN_ALLOWED).replace("{arena}", rj.getArena()!!.getDisplayName()))
            Sounds.playSound("rejoin-allowed", p)
            rj.reJoin(p)
        }
    }
}
