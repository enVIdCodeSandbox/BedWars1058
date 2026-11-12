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
import com.andrei1058.bedwars.api.arena.team.TeamColor
import com.andrei1058.bedwars.api.command.ParentCommand
import com.andrei1058.bedwars.api.command.SubCommand
import com.andrei1058.bedwars.api.server.SetupType
import com.andrei1058.bedwars.arena.Misc
import com.andrei1058.bedwars.arena.SetupSession
import com.andrei1058.bedwars.configuration.Permissions
import java.util.ArrayList
import java.util.HashMap
import net.md_5.bungee.api.chat.ClickEvent
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player

open class AutoCreateTeams(parent: ParentCommand, name: String) : SubCommand(parent, name) {
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
        if (ss.getSetupType() == SetupType.ASSISTED) {
            if (is13Higher()) {
                if (timeOut.containsKey(p) && timeOut.get(p)!! >= System.currentTimeMillis() && teamsFound13.containsKey(p)) {
                    for (tf in teamsFound13.get(p)!!) {
                        Bukkit.dispatchCommand(s, BedWars.mainCmd + " createTeam " + TeamColor.enName(tf) + " " + TeamColor.enName(tf))
                    }
                    if (ss.getConfig().getYml().get("waiting.Pos1") == null) {
                        s.sendMessage("")
                        s.sendMessage("§6§lWAITING LOBBY REMOVAL:")
                        s.sendMessage("§fIf you'd like the lobby to disappear when the game starts,")
                        s.sendMessage("§fplease use the following commands like a world edit selection.")
                        p.spigot().sendMessage(Misc.msgHoverClick("§c ▪ §7/" + BedWars.mainCmd + " waitingPos 1", "§dSet pos 1", "/" + getParent().getName() + " waitingPos 1", ClickEvent.Action.RUN_COMMAND))
                        p.spigot().sendMessage(Misc.msgHoverClick("§c ▪ §7/" + BedWars.mainCmd + " waitingPos 2", "§dSet pos 2", "/" + getParent().getName() + " waitingPos 2", ClickEvent.Action.RUN_COMMAND))
                        s.sendMessage("")
                        s.sendMessage("§7This step is OPTIONAL. If you wan to skip it do §6/" + BedWars.mainCmd)
                    }
                    return true
                }
                val found: MutableList<String> = ArrayList()
                val w = p.getWorld()
                if (ss.getConfig().getYml().get("Team") == null) {
                    p.sendMessage("§6 ▪ §7Searching for teams. This may cause lag.")
                    for (x in -200 until 200) {
                        for (y in 50 until 130) {
                            for (z in -200 until 200) {
                                val b = Location(w, x.toDouble(), y.toDouble(), z.toDouble()).getBlock()
                                if (b.getType().toString().contains("_WOOL")) {
                                    if (!found.contains(b.getType().toString())) {
                                        var count: Int = 0
                                        for (x1 in -2 until 2) {
                                            for (y1 in -2 until 2) {
                                                for (z1 in -2 until 2) {
                                                    val b2 = Location(w, x.toDouble(), y.toDouble(), z.toDouble()).getBlock()
                                                    if (b2.getType() == b.getType()) {
                                                        count++
                                                    }
                                                }
                                            }
                                        }
                                        if (count >= 5) {
                                            if (!TeamColor.enName(b.getType().toString()).isEmpty()) {
                                                if (ss.getConfig().getYml().get("Team." + TeamColor.enName(b.getType().toString())) == null) {
                                                    found.add(b.getType().toString())
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (found.isEmpty()) {
                    p.sendMessage("§6 ▪ §7No new teams were found.\n§6 ▪ §7Manually create teams with: §6/" + BedWars.mainCmd + " createTeam")
                } else {
                    if (timeOut.containsKey(p)) {
                        p.sendMessage("§c ▪ §7Time out. Type again to search for teams.")
                        timeOut.remove(p)
                        return true
                    } else {
                        timeOut.put(p, System.currentTimeMillis() + 16000)
                    }
                    if (teamsFound13.containsKey(p)) {
                        teamsFound13.replace(p, found)
                    } else {
                        teamsFound13.put(p, found)
                    }
                    p.sendMessage("§6§lNEW TEAMS FOUND:")
                    for (tf in found) {
                        val name = TeamColor.enName(tf)
                        p.sendMessage("§f ▪ " + TeamColor.getChatColor(name) + name.replace("_", " "))
                    }
                    p.spigot().sendMessage(Misc.msgHoverClick("§6 ▪ §7§lClick here to create found teams.", "§fClick to create found teams!", "/" + getParent().getName() + " " + getSubCommandName(), ClickEvent.Action.RUN_COMMAND))
                }
            } else {
                if (timeOut.containsKey(p) && timeOut.get(p)!! >= System.currentTimeMillis() && teamsFoundOld.containsKey(p)) {
                    for (tf in teamsFoundOld.get(p)!!) {
                        Bukkit.dispatchCommand(s, BedWars.mainCmd + " createTeam " + TeamColor.enName(tf) + " " + TeamColor.enName(tf))
                    }
                    if (ss.getConfig().getYml().get("waiting.Pos1") == null) {
                        s.sendMessage("")
                        s.sendMessage("§6§lWAITING LOBBY REMOVAL:")
                        s.sendMessage("§fIf you'd like the lobby to disappear when the game starts,")
                        s.sendMessage("§fplease use the following commands like a world edit selection.")
                        p.spigot().sendMessage(Misc.msgHoverClick("§c ▪ §7/" + BedWars.mainCmd + " waitingPos 1", "§dSet pos 1", "/" + getParent().getName() + " waitingPos 1", ClickEvent.Action.RUN_COMMAND))
                        p.spigot().sendMessage(Misc.msgHoverClick("§c ▪ §7/" + BedWars.mainCmd + " waitingPos 2", "§dSet pos 2", "/" + getParent().getName() + " waitingPos 2", ClickEvent.Action.RUN_COMMAND))
                        s.sendMessage("")
                        s.sendMessage("§7This step is OPTIONAL. If you wan to skip it do §6/" + BedWars.mainCmd)
                    }
                    return true
                }
                val found: MutableList<Byte> = ArrayList()
                val w = p.getWorld()
                if (ss.getConfig().getYml().get("Team") == null) {
                    p.sendMessage("§6 ▪ §7Searching for teams. This may cause lag.")
                    for (x in -200 until 200) {
                        for (y in 50 until 130) {
                            for (z in -200 until 200) {
                                val b = Location(w, x.toDouble(), y.toDouble(), z.toDouble()).getBlock()
                                if (b.getType() == Material.valueOf("WOOL")) {
                                    if (!found.contains(b.getData())) {
                                        var count: Int = 0
                                        for (x1 in -2 until 2) {
                                            for (y1 in -2 until 2) {
                                                for (z1 in -2 until 2) {
                                                    val b2 = Location(w, x.toDouble(), y.toDouble(), z.toDouble()).getBlock()
                                                    if (b2.getType() == b.getType()) {
                                                        if (b.getData() == b2.getData()) {
                                                            count++
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        if (count >= 5) {
                                            if (!TeamColor.enName(b.getData()).isEmpty()) {
                                                if (ss.getConfig().getYml().get("Team." + TeamColor.enName(b.getData())) == null) {
                                                    found.add(b.getData())
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (found.isEmpty()) {
                    p.sendMessage("§6 ▪ §7No new teams were found.\n§6 ▪ §7Manually create teams with: §6/" + BedWars.mainCmd + " createTeam")
                } else {
                    if (timeOut.containsKey(p)) {
                        p.sendMessage("§c ▪ §7Time out. Type again to search for teams.")
                        timeOut.remove(p)
                        return true
                    } else {
                        timeOut.put(p, System.currentTimeMillis() + 16000)
                    }
                    if (teamsFoundOld.containsKey(p)) {
                        teamsFoundOld.replace(p, found)
                    } else {
                        teamsFoundOld.put(p, found)
                    }
                    p.sendMessage("§6§lNEW TEAMS FOUND:")
                    for (tf in found) {
                        val name = TeamColor.enName(tf)
                        p.sendMessage("§f ▪ " + TeamColor.getChatColor(name) + name.replace('_', ' '))
                    }
                    p.spigot().sendMessage(Misc.msgHoverClick("§6 ▪ §7§lClick here to create found teams.", "§fClick to create found teams!", "/" + getParent().getName() + " " + getSubCommandName(), ClickEvent.Action.RUN_COMMAND))
                }
            }
        } else {
            return false
        }
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
        if (!SetupSession.isInSetupSession(p.getUniqueId())) {
            return false
        }
        return hasPermission(s)
    }

    init {
        setArenaSetupCommand(true)
        setPermission(Permissions.PERMISSION_SETUP_ARENA)
    }

    companion object {
        private var timeOut: HashMap<Player, Long> = HashMap()
        private var teamsFoundOld: HashMap<Player, MutableList<Byte>> = HashMap()
        private var teamsFound13: HashMap<Player, MutableList<String>> = HashMap()

        /**
         * Check if server version is 1.13 or higher
         */
        @JvmStatic
        fun is13Higher(): Boolean {
            return !(BedWars.getServerVersion() == "v1_8_R3" || BedWars.getServerVersion() == "v1_12_R1")
        }
    }
}
