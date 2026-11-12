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

import com.andrei1058.bedwars.api.command.ParentCommand
import com.andrei1058.bedwars.api.command.SubCommand
import com.andrei1058.bedwars.api.configuration.ConfigPath
import com.andrei1058.bedwars.api.server.SetupType
import com.andrei1058.bedwars.arena.SetupSession
import com.andrei1058.bedwars.commands.Misc
import com.andrei1058.bedwars.configuration.Permissions
import com.andrei1058.bedwars.configuration.Sounds
import java.util.ArrayList
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

open class RemoveGenerator(parent: ParentCommand, name: String) : SubCommand(parent, name) {
    override fun execute(args: Array<String>, s: CommandSender): Boolean {
        if (s !is Player) {
            return false
        }
        val p = s as Player
        val ss: SetupSession? = SetupSession.getSession(p.getUniqueId())
        if (ss == null) {
            return false
        }
        if (args.size == 0) {
            val toRemove = arrayOf<String>("", "", "")
            var nearest: Location? = null
            if (ss.getConfig().getYml().get("Team") != null) {
                for (team in ss.getConfig().getYml().getConfigurationSection("Team").getKeys(false)) {
                    for (type in arrayOf<String>("Iron", "Gold", "Emerald")) {
                        if (ss.getConfig().getYml().get("Team." + team + "." + type) != null) {
                            for (loc in ss.getConfig().getList("Team." + team + "." + type)) {
                                val loc2: Location? = ss.getConfig().convertStringToArenaLocation(loc)
                                if (loc2 != null) {
                                    if (p.getLocation().distance(loc2) <= 2) {
                                        if (nearest != null) {
                                            if (p.getLocation().distance(nearest) > p.getLocation().distance(loc2)) {
                                                nearest = loc2
                                                toRemove[0] = type
                                                toRemove[1] = loc
                                                toRemove[2] = team
                                            }
                                        } else {
                                            nearest = loc2
                                            toRemove[0] = type
                                            toRemove[1] = loc
                                            toRemove[2] = team
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (ss.getConfig().getYml().get("generator") != null) {
                for (type in arrayOf<String>("Emerald", "Diamond")) {
                    if (ss.getConfig().getYml().get("generator." + type) != null) {
                        for (loc in ss.getConfig().getList("generator." + type)) {
                            val loc2: Location? = ss.getConfig().convertStringToArenaLocation(loc)
                            if (loc2 == null) {
                                continue
                            }
                            if (p.getLocation().distance(loc2) <= 2) {
                                if (nearest != null) {
                                    if (p.getLocation().distance(nearest) > p.getLocation().distance(loc2)) {
                                        nearest = loc2
                                        toRemove[0] = type
                                        toRemove[1] = loc
                                        toRemove[2] = ""
                                    }
                                } else {
                                    nearest = loc2
                                    toRemove[0] = type
                                    toRemove[1] = loc
                                    toRemove[2] = ""
                                }
                            }
                        }
                    }
                }
            }
            if (nearest == null) {
                p.sendMessage(ss.getPrefix() + "Could not find any nearby generator (Range 2x2).")
                p.sendMessage(ss.getPrefix() + "You mast stand close to the generator hologram's you want to remove.")
                com.andrei1058.bedwars.BedWars.nms.sendTitle(p, " ", ChatColor.RED.toString() + "Could not find any nearby generator.", 5, 40, 5)
                Sounds.playSound(ConfigPath.SOUNDS_INSUFF_MONEY, p)
                return true
            }
            if (toRemove[2].isEmpty()) {
                val list = ss.getConfig().getList("generator." + toRemove[0])
                list.remove(toRemove[1])
                ss.getConfig().set("generator." + toRemove[0], list)
                p.sendMessage(ss.getPrefix() + "Removed " + toRemove[0] + " generator at location: X:" + nearest.getBlockX() + " Y:" + nearest.getBlockY() + " Z:" + nearest.getZ())
                com.andrei1058.bedwars.BedWars.nms.sendTitle(p, " ", ChatColor.GREEN.toString() + toRemove[0] + " generator removed.", 5, 40, 5)
                Sounds.playSound(ConfigPath.SOUNDS_BOUGHT, p)
                Misc.removeArmorStand(toRemove[0], nearest, toRemove[1])
                return true
            }
            if (ss.getSetupType() == SetupType.ASSISTED) {
                ss.getConfig().set("Team." + toRemove[2] + ".Emerald", ArrayList<Any>())
                ss.getConfig().set("Team." + toRemove[2] + ".Iron", ArrayList<Any>())
                ss.getConfig().set("Team." + toRemove[2] + ".Gold", ArrayList<Any>())
                com.andrei1058.bedwars.BedWars.nms.sendTitle(p, " ", ss.getTeamColor(toRemove[2]).toString() + toRemove[2] + " generator was removed.", 5, 40, 5)
                Sounds.playSound(ConfigPath.SOUNDS_BOUGHT, p)
                Misc.removeArmorStand(null, nearest, toRemove[1])
                p.sendMessage(ss.getPrefix() + ss.getTeamColor(toRemove[2]) + toRemove[2] + ChatColor.getLastColors(ss.getPrefix()) + " generators were removed!")
                return true
            }
            val list = ss.getConfig().getList("Team." + toRemove[2] + "." + toRemove[0])
            list.remove(toRemove[1])
            ss.getConfig().set("Team." + toRemove[2] + "." + toRemove[0], list)
            p.sendMessage(ss.getPrefix() + "Removed " + ss.getTeamColor(toRemove[2]) + toRemove[2] + " " + ChatColor.getLastColors(ss.getPrefix()) + toRemove[0] + " generator at location: X:" + nearest.getBlockX() + " Y:" + nearest.getBlockY() + " Z:" + nearest.getZ())
            com.andrei1058.bedwars.BedWars.nms.sendTitle(p, " ", ss.getTeamColor(toRemove[2]).toString() + toRemove[2] + " " + ChatColor.GREEN + toRemove[0] + " generator removed.", 5, 40, 5)
            Sounds.playSound(ConfigPath.SOUNDS_BOUGHT, p)
            Misc.removeArmorStand(toRemove[0], nearest, toRemove[1])
            return true
        }
        return true
    }

    override fun getTabComplete(): MutableList<String>? {
        return ArrayList()
    }

    init {
        setArenaSetupCommand(true)
        setPermission(Permissions.PERMISSION_SETUP_ARENA)
    }
}
