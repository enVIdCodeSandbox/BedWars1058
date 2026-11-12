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

package com.andrei1058.bedwars.commands.shout

import com.andrei1058.bedwars.BedWars
import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.api.configuration.ConfigPath
import com.andrei1058.bedwars.api.language.Language
import com.andrei1058.bedwars.api.language.Messages
import com.andrei1058.bedwars.arena.Arena
import java.util.HashMap
import java.util.UUID
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.command.defaults.BukkitCommand
import org.bukkit.entity.Player

open class ShoutCommand(name: String) : BukkitCommand(name) {
    override fun execute(s: CommandSender, st: String, args: Array<String>): Boolean {
        if (s is ConsoleCommandSender) {
            return true
        }
        val p = s as Player
        val a: IArena? = Arena.getArenaByPlayer(p)
        if (a == null || a.isSpectator(p)) {
            p.sendMessage(Language.getMsg(p, Messages.COMMAND_NOT_FOUND_OR_INSUFF_PERMS))
            return true
        }
        val sb = StringBuilder()
        for (ar in args) {
            sb.append(ar).append(" ")
        }
        p.chat("!" + sb.toString())
        return false
    }

    companion object {
        private var shoutCooldown: HashMap<UUID, Long> = HashMap()

        @JvmStatic
        fun updateShout(player: Player) {
            if (player.hasPermission("bw.shout.bypass")) {
                return
            }
            if (shoutCooldown.containsKey(player.getUniqueId())) {
                shoutCooldown.replace(player.getUniqueId(), System.currentTimeMillis() + (BedWars.config.getInt(ConfigPath.GENERAL_CONFIGURATION_SHOUT_COOLDOWN) * 1000L))
            } else {
                shoutCooldown.put(player.getUniqueId(), System.currentTimeMillis() + (BedWars.config.getInt(ConfigPath.GENERAL_CONFIGURATION_SHOUT_COOLDOWN) * 1000L))
            }
        }

        @JvmStatic
        fun isShoutCooldown(player: Player): Boolean {
            if (player.hasPermission("bw.shout.bypass")) {
                return false
            }
            if (!shoutCooldown.containsKey(player.getUniqueId())) {
                return false
            }
            return shoutCooldown.get(player.getUniqueId())!! > System.currentTimeMillis()
        }

        @JvmStatic
        fun getShoutCooldown(p: Player): Double {
            return ((shoutCooldown.get(p.getUniqueId())!! - System.currentTimeMillis()) / 1000f).toDouble()
        }

        @JvmStatic
        fun isShout(p: Player): Boolean {
            if (!shoutCooldown.containsKey(p.getUniqueId())) {
                return false
            }
            return shoutCooldown.get(p.getUniqueId())!! + 1000 > System.currentTimeMillis()
        }
    }
}
