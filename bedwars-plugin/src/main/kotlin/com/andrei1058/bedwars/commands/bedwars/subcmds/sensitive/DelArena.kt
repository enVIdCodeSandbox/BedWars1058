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
import com.andrei1058.bedwars.arena.Arena.Companion.getArenaByName
import com.andrei1058.bedwars.arena.Misc
import com.andrei1058.bedwars.arena.SetupSession
import com.andrei1058.bedwars.commands.bedwars.MainCommand
import com.andrei1058.bedwars.configuration.Permissions
import java.io.File
import java.util.ArrayList
import java.util.HashMap
import java.util.Objects
import net.md_5.bungee.api.chat.ClickEvent
import org.apache.commons.io.FileUtils
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player

open class DelArena(parent: ParentCommand, name: String) : SubCommand(parent, name) {
    override fun execute(args: Array<String>, s: CommandSender): Boolean {
        if (s is ConsoleCommandSender) {
            return false
        }
        val p = s as Player
        if (!MainCommand.isLobbySet(p)) {
            return true
        }
        if (args.size != 1) {
            p.sendMessage("§c▪ §7Usage: §o/" + MainCommand.getInstance().getName() + " delArena <mapName>")
            return true
        }
        if (!BedWars.getAPI().getRestoreAdapter().isWorld(args[0])) {
            p.sendMessage("§c▪ §7" + args[0] + " doesn't exist as a world folder!")
            return true
        }
        if (getArenaByName(args[0]) != null) {
            p.sendMessage("§c▪ §7Please disable it first!")
            return true
        }
        val ac = File(plugin.getDataFolder(), "/Arenas/" + args[0] + ".yml")
        if (!ac.exists()) {
            p.sendMessage("§c▪ §7This arena doesn't exist!")
            return true
        }
        if (delArenaConfirm.containsKey(p)) {
            if (System.currentTimeMillis() - 2000 <= delArenaConfirm.get(p)!!) {
                BedWars.getAPI().getRestoreAdapter().deleteWorld(args[0])
                FileUtils.deleteQuietly(ac)
                p.sendMessage("§c▪ §7" + args[0] + " was deleted!")
                return true
            }
            p.sendMessage("§6 ▪ §7Type again to confirm.")
            delArenaConfirm.replace(p, System.currentTimeMillis())
        } else {
            p.sendMessage("§6 ▪ §7Type again to confirm.")
            delArenaConfirm.put(p, System.currentTimeMillis())
        }
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
        setPriority(4)
        showInList(true)
        setPermission(Permissions.PERMISSION_DEL_ARENA)
        setDisplayInfo(Misc.msgHoverClick("§6 ▪ §7/" + MainCommand.getInstance().getName() + " " + getSubCommandName() + " §6<worldName>", "§fDelete a map and its configuration.", "/" + MainCommand.getInstance().getName() + " " + getSubCommandName(), ClickEvent.Action.SUGGEST_COMMAND))
    }

    companion object {
        private var delArenaConfirm: HashMap<Player, Long> = HashMap()
    }
}
