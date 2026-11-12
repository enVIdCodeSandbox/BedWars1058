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

package com.andrei1058.bedwars.commands.bedwars.subcmds.sensitive

import com.andrei1058.bedwars.BedWars
import com.andrei1058.bedwars.BedWars.Companion.plugin
import com.andrei1058.bedwars.api.command.ParentCommand
import com.andrei1058.bedwars.api.command.SubCommand
import com.andrei1058.bedwars.arena.Arena
import com.andrei1058.bedwars.arena.Misc
import com.andrei1058.bedwars.arena.SetupSession
import com.andrei1058.bedwars.commands.bedwars.MainCommand
import com.andrei1058.bedwars.configuration.Permissions
import java.io.File
import java.io.IOException
import java.util.ArrayList
import java.util.Objects
import net.md_5.bungee.api.chat.ClickEvent
import org.apache.commons.io.FileUtils
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player

open class CloneArena(parent: ParentCommand, name: String) : SubCommand(parent, name) {
    override fun execute(args: Array<String>, s: CommandSender): Boolean {
        if (s is ConsoleCommandSender) {
            return false
        }
        val p = s as Player
        if (!MainCommand.isLobbySet(p)) {
            return true
        }
        if (args.size != 2) {
            p.sendMessage("§c▪ §7Usage: §o/" + getParent().getName() + " " + getSubCommandName() + " <mapName> <newArena>")
            return true
        }
        if (!BedWars.getAPI().getRestoreAdapter().isWorld(args[0])) {
            p.sendMessage("§c▪ §7" + args[0] + " doesn't exist!")
            return true
        }
        val yml1 = File(plugin.getDataFolder(), "/Arenas/" + args[0] + ".yml")
        val yml2 = File(plugin.getDataFolder(), "/Arenas/" + args[1] + ".yml")
        if (!yml1.exists()) {
            p.sendMessage("§c▪ §7" + args[0] + " doesn't exist!")
            return true
        }
        if (BedWars.getAPI().getRestoreAdapter().isWorld(args[1]) && yml2.exists()) {
            p.sendMessage("§c▪ §7" + args[1] + " already exist!")
            return true
        }
        if (args[1].contains("+")) {
            p.sendMessage("§c▪ §7" + args[1] + " mustn't contain this symbol: " + ChatColor.RED + "+")
            return true
        }
        if (Arena.getArenaByName(args[0]) != null) {
            p.sendMessage("§c▪ §7Please disable " + args[0] + " first!")
            return true
        }
        BedWars.getAPI().getRestoreAdapter().cloneArena(args[0], args[1])
        if (yml1.exists()) {
            try {
                FileUtils.copyFile(yml1, yml2, true)
            } catch (e: IOException) {
                e.printStackTrace()
                p.sendMessage("§c▪ §7An error occurred while copying the map's config. Check the console.")
            }
        }
        p.sendMessage("§6 ▪ §7Done :D.")
        return true
    }

    override fun getTabComplete(): MutableList<String>? {
        val tab: MutableList<String> = ArrayList()
        val dir = File(plugin.getDataFolder(), "/Arenas")
        if (dir.exists()) {
            val fls = dir.listFiles()
            for (fl in Objects.requireNonNull(fls)) {
                if (fl.isFile()) {
                    if (fl.getName().contains(".yml")) {
                        tab.add(fl.getName().replace(".yml", ""))
                    }
                }
            }
        }
        return tab
    }

    override fun canSee(s: CommandSender, api: com.andrei1058.bedwars.api.BedWars): Boolean {
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
        setPriority(7)
        showInList(true)
        setPermission(Permissions.PERMISSION_CLONE)
        setDisplayInfo(Misc.msgHoverClick("§6 ▪ §7/" + getParent().getName() + " " + getSubCommandName() + " §6<worldName> <newName>", "§fClone an existing arena.", "/" + getParent().getName() + " " + getSubCommandName(), ClickEvent.Action.SUGGEST_COMMAND))
    }
}
