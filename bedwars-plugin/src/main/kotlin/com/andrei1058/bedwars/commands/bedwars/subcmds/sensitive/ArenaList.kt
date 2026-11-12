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

import com.andrei1058.bedwars.BedWars.Companion.plugin
import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.api.command.ParentCommand
import com.andrei1058.bedwars.api.command.SubCommand
import com.andrei1058.bedwars.api.language.Language
import com.andrei1058.bedwars.arena.Arena
import com.andrei1058.bedwars.arena.Misc
import com.andrei1058.bedwars.arena.SetupSession
import com.andrei1058.bedwars.commands.bedwars.MainCommand
import java.io.File
import java.util.ArrayList
import java.util.Objects
import net.md_5.bungee.api.chat.ClickEvent
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player

open class ArenaList(parent: ParentCommand, name: String) : SubCommand(parent, name) {
    override fun execute(args: Array<String>, s: CommandSender): Boolean {
        if (s is ConsoleCommandSender) {
            return false
        }
        val p = s as Player
        var page: Int = 1
        if (args.size >= 1) {
            try {
                page = Integer.parseInt(args[0])
                if (page < 0) {
                    page = 1
                }
            } catch (ignored: Exception) {
            }
        }
        var start: Int = (page - 1) * ARENAS_PER_PAGE
        val arenas: MutableList<IArena> = ArrayList(Arena.getArenas())
        if (arenas.size <= start) {
            page = 1
            start = 0
        }
        p.sendMessage(color(" \n&1|| &3" + plugin.getName() + "&7 Instantiated games: \n "))
        if (arenas.isEmpty()) {
            p.sendMessage(ChatColor.RED.toString() + "No arenas to display.")
            return true
        }
        val limit: Int = Math.min(arenas.size, start + ARENAS_PER_PAGE)
        arenas.subList(start, limit).forEach({ arena ->
            val gameState = arena.getDisplayStatus(Language.getPlayerLanguage(p))
            val msg = color("ID: &e" + arena.getWorldName() + " &fG: &e" + arena.getDisplayGroup(p) + " &fP: &e" + (arena.getPlayers()!!.size + arena.getSpectators()!!.size) + " &fS: " + gameState + " &fWl: &e" + (Bukkit.getWorld(arena.getWorldName()) != null))
            p.sendMessage(msg)
        })
        p.sendMessage(" ")
        if (arenas.size > ARENAS_PER_PAGE * page) {
            p.sendMessage(ChatColor.GRAY.toString() + "Type /" + ChatColor.GREEN + MainCommand.getInstance().getName() + " arenaList " + ++page + ChatColor.GRAY + " for next page.")
        }
        return true
    }

    override fun getTabComplete(): MutableList<String>? {
        return null
    }

    private fun getArenas(): MutableList<String> {
        val arene: ArrayList<String> = ArrayList()
        val dir = File(plugin.getDataFolder(), "/Arenas")
        if (dir.exists()) {
            for (f in Objects.requireNonNull(dir.listFiles())) {
                if (f.isFile()) {
                    if (f.getName().contains(".yml")) {
                        arene.add(f.getName().replace(".yml", ""))
                    }
                }
            }
        }
        return arene
    }

    override fun canSee(s: CommandSender, api: com.andrei1058.bedwars.api.BedWars): Boolean {
        if (s is Player) {
            val p = s as Player
            if (Arena.isInArena(p)) {
                return false
            }
            if (SetupSession.isInSetupSession(p.getUniqueId())) {
                return false
            }
        }
        return hasPermission(s)
    }

    init {
        setPriority(3)
        showInList(true)
        setDisplayInfo(Misc.msgHoverClick("§6 ▪ §7/" + com.andrei1058.bedwars.commands.bedwars.MainCommand.getInstance().getName() + " " + getSubCommandName() + (if ((getArenas().size == 0)) " §c(0 set)" else " §a(" + getArenas().size + " set)"), "§fShow available arenas", "/" + MainCommand.getInstance().getName() + " " + getSubCommandName(), ClickEvent.Action.RUN_COMMAND))
    }

    companion object {
        private const val ARENAS_PER_PAGE: Int = 10

        @JvmStatic
        private fun color(msg: String): String {
            return ChatColor.translateAlternateColorCodes('&', msg)
        }
    }
}
