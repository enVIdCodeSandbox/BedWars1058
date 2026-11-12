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
import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.api.command.ParentCommand
import com.andrei1058.bedwars.api.command.SubCommand
import com.andrei1058.bedwars.arena.Arena
import com.andrei1058.bedwars.arena.Misc
import com.andrei1058.bedwars.arena.SetupSession
import com.andrei1058.bedwars.commands.bedwars.MainCommand
import com.andrei1058.bedwars.configuration.Permissions
import java.io.File
import java.util.ArrayList
import java.util.Objects
import net.md_5.bungee.api.chat.ClickEvent
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player

open class EnableArena(parent: ParentCommand, name: String) : SubCommand(parent, name) {
    override fun execute(args: Array<String>, s: CommandSender): Boolean {
        if (s is ConsoleCommandSender) {
            return false
        }
        val p = s as Player
        if (!MainCommand.isLobbySet(p)) {
            return true
        }
        if (args.size != 1) {
            p.sendMessage("§c▪ §7Usage: §o/" + getParent().getName() + " enableRotation <mapName>")
            return true
        }
        if (!BedWars.getAPI().getRestoreAdapter().isWorld(args[0])) {
            p.sendMessage("§c▪ §7" + args[0] + " doesn't exist!")
            return true
        }
        for (mm in Arena.getEnableQueue()) {
            if (mm.getArenaName().equals(args[0], ignoreCase = true)) {
                p.sendMessage("§c▪ §7This arena is already in the enable queue!")
                return true
            }
        }
        val aa: IArena? = Arena.getArenaByName(args[0])
        if (aa != null) {
            p.sendMessage("§c▪ §7This arena is already enabled!")
            return true
        }
        p.sendMessage("§6 ▪ §7Enabling arena...")
        Arena(args[0], p)
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
        setDisplayInfo(Misc.msgHoverClick("§6 ▪ §7/" + getParent().getName() + " " + getSubCommandName() + " §6<worldName>", "§fEnable an arena.", "/" + getParent().getName() + " " + getSubCommandName() + " ", ClickEvent.Action.SUGGEST_COMMAND))
        showInList(true)
        setPriority(5)
        setPermission(Permissions.PERMISSION_ARENA_ENABLE)
    }
}
