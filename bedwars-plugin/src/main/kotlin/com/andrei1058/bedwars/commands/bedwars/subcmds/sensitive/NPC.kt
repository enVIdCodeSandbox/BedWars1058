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
import com.andrei1058.bedwars.BedWars.Companion.mainCmd
import com.andrei1058.bedwars.api.command.ParentCommand
import com.andrei1058.bedwars.api.command.SubCommand
import com.andrei1058.bedwars.api.configuration.ConfigPath
import com.andrei1058.bedwars.arena.Arena
import com.andrei1058.bedwars.arena.Misc
import com.andrei1058.bedwars.arena.SetupSession
import com.andrei1058.bedwars.configuration.Permissions
import com.andrei1058.bedwars.support.citizens.JoinNPC
import com.google.common.base.Joiner
import java.util.ArrayList
import java.util.Arrays
import net.citizensnpcs.api.CitizensAPI
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.ClickEvent
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.util.BlockIterator

open class NPC(parent: ParentCommand, name: String) : SubCommand(parent, name) {
    //main usage
    private val MAIN_USAGE: MutableList<BaseComponent> = Arrays.asList(Misc.msgHoverClick("§f\n§c▪ §7Usage: §e/" + mainCmd + " " + getSubCommandName() + " add", "§fUse this command to create a join NPC.\n§fClick to see the syntax.", "/" + getParent().getName() + " " + getSubCommandName() + " add", ClickEvent.Action.RUN_COMMAND), Misc.msgHoverClick("§c▪ §7Usage: §e/" + mainCmd + " " + getSubCommandName() + " remove", "§fStay in front of a NPC in order to remove it.", "/" + getParent().getName() + " " + getSubCommandName() + " remove", ClickEvent.Action.SUGGEST_COMMAND))
    private val ADD_USAGE: MutableList<BaseComponent> = Arrays.asList(Misc.msgHoverClick("f\n§c▪ §7Usage: §e§o/" + getParent().getName() + " " + getSubCommandName() + " add <skin> <arenaGroup> <§7line1§9\\n§7line2§e>\n§7You can use §e{players} §7for the players count in this arena §7group.", "Click to use.", "/" + getParent().getName() + " " + getSubCommandName() + " add", ClickEvent.Action.SUGGEST_COMMAND))

    override fun execute(args: Array<String>, s: CommandSender): Boolean {
        if (s is ConsoleCommandSender) {
            return false
        }
        if (!JoinNPC.isCitizensSupport()) {
            return false
        }
        val p = s as Player
        if (args.size < 1) {
            for (bc in MAIN_USAGE) {
                p.spigot().sendMessage(bc)
            }
            return true
        }
        if (args[0].equals("add", ignoreCase = true)) {
            if (args.size < 4) {
                for (bc in ADD_USAGE) {
                    p.spigot().sendMessage(bc)
                }
                return true
            }
            var npcs: MutableList<String>
            if (BedWars.config.getYml().get(ConfigPath.GENERAL_CONFIGURATION_NPC_LOC_STORAGE) != null) {
                npcs = BedWars.config.getYml().getStringList(ConfigPath.GENERAL_CONFIGURATION_NPC_LOC_STORAGE)
            } else {
                npcs = ArrayList()
            }
            val name = Joiner.on(" ").join(args).replace(args[0] + " " + args[1] + " " + args[2] + " ", "")
            val npc: net.citizensnpcs.api.npc.NPC? = JoinNPC.spawnNPC(p.getLocation(), name, args[2], args[1], null)
            assert(npc != null)
            npcs.add(BedWars.config.stringLocationConfigFormat(p.getLocation()) + "," + args[1] + "," + name + "," + args[2] + "," + npc!!.getId())
            val NPC_SET = "§a§c▪ §bNPC: %name% §bwas set!"
            p.sendMessage(NPC_SET.replace("%name%", name.replace("&", "§").replace("\\\\n", " ")))
            p.sendMessage("§a§c▪ §bTarget groups: " + ChatColor.GOLD + args[2])
            BedWars.config.set(ConfigPath.GENERAL_CONFIGURATION_NPC_LOC_STORAGE, npcs)
        } else if (args[0].equals("remove", ignoreCase = true)) {
            val e = p.getNearbyEntities(4.0, 4.0, 4.0)
            val NO_NPCS = "§c▪ §bThere isn't any NPC nearby."
            if (e.isEmpty()) {
                p.sendMessage(NO_NPCS)
                return true
            }
            if (BedWars.config.getYml().get(ConfigPath.GENERAL_CONFIGURATION_NPC_LOC_STORAGE) == null) {
                val NO_SET = "§c▪ §bThere isn't any NPC set yet!"
                p.sendMessage(NO_SET)
                return true
            }
            val npc: net.citizensnpcs.api.npc.NPC? = getTarget(p)
            if (npc == null) {
                p.sendMessage(NO_NPCS)
                return true
            }
            val locations = BedWars.config.getYml().getStringList(ConfigPath.GENERAL_CONFIGURATION_NPC_LOC_STORAGE)
            for (id in JoinNPC.npcs.keys) {
                if (id == npc.getId()) {
                    for (loc in BedWars.config.getYml().getStringList(ConfigPath.GENERAL_CONFIGURATION_NPC_LOC_STORAGE)) {
                        if (loc.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[4].equals(npc.getId().toString(), ignoreCase = true)) {
                            locations.remove(loc)
                        }
                    }
                }
            }
            JoinNPC.npcs.remove(npc.getId())
            for (e2 in npc.getEntity().getNearbyEntities(0.0, 3.0, 0.0)) {
                if (e2.getType() == EntityType.ARMOR_STAND) {
                    e2.remove()
                }
            }
            BedWars.config.set(ConfigPath.GENERAL_CONFIGURATION_NPC_LOC_STORAGE, locations)
            npc.destroy()
            val NPC_REMOVED = "§c▪ §bThe target NPC was removed!"
            p.sendMessage(NPC_REMOVED)
        } else {
            for (bc in MAIN_USAGE) {
                p.spigot().sendMessage(bc)
            }
        }
        return true
    }

    override fun getTabComplete(): MutableList<String>? {
        return Arrays.asList("remove", "add")
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
        showInList(true)
        setPriority(12)
        setPermission(Permissions.PERMISSION_NPC)
        setDisplayInfo(Misc.msgHoverClick("§6 ▪ §7/" + getParent().getName() + " " + getSubCommandName() + "         §8   - §ecreate a join NPC", "§fCreate a join NPC  \n§fClick for more details.", "/" + getParent().getName() + " " + getSubCommandName(), ClickEvent.Action.RUN_COMMAND))
    }

    companion object {
        /**
         * Create an armor-stand hologram
         */
        @JvmStatic
        fun createArmorStand(loc: Location): ArmorStand {
            val a = loc.getWorld().spawn(loc, ArmorStand::class.java)
            a.setGravity(false)
            a.setVisible(false)
            a.setCustomNameVisible(false)
            a.setMarker(true)
            return a
        }

        /**
         * Get target NPC
         */
        @JvmStatic
        fun getTarget(player: Player): net.citizensnpcs.api.npc.NPC? {
            val iterator = BlockIterator(player.getWorld(), player.getLocation().toVector(), player.getEyeLocation().getDirection(), 0.0, 100)
            while (iterator.hasNext()) {
                val item = iterator.next()
                for (entity in player.getNearbyEntities(100.0, 100.0, 100.0)) {
                    val acc: Int = 2
                    for (x in -acc until acc) {
                        for (z in -acc until acc) {
                            for (y in -acc until acc) {
                                if (entity.getLocation().getBlock().getRelative(x, y, z) == item) {
                                    if (entity.hasMetadata("NPC")) {
                                        val npc: net.citizensnpcs.api.npc.NPC? = CitizensAPI.getNPCRegistry().getNPC(entity)
                                        if (npc != null) {
                                            return npc
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return null
        }
    }
}
