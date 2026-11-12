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

package com.andrei1058.bedwars.support.citizens

import com.andrei1058.bedwars.BedWars
import com.andrei1058.bedwars.api.command.SubCommand
import com.andrei1058.bedwars.api.configuration.ConfigPath
import com.andrei1058.bedwars.arena.Arena
import com.andrei1058.bedwars.arena.Misc
import com.andrei1058.bedwars.commands.bedwars.MainCommand
import com.andrei1058.bedwars.commands.bedwars.subcmds.sensitive.NPC.Companion.createArmorStand
import java.util.Arrays
import java.util.HashMap
import net.citizensnpcs.api.CitizensAPI
import net.citizensnpcs.api.npc.NPC
import net.citizensnpcs.npc.skin.SkinnableEntity
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.event.player.PlayerTeleportEvent

object JoinNPC {
    private var citizensSupport: Boolean = false
    /* Here are stored NPC holograms without colors and placeholders translated used for refresh*/
    @JvmField var npcs_holos: HashMap<ArmorStand, MutableList<String>> = HashMap()
    /* Here are stored all the NPCs*/
    @JvmField var npcs: HashMap<Int, String> = HashMap()

    /**
     * Check if Citizens is loaded correctly
     */
    @JvmStatic
    fun isCitizensSupport(): Boolean {
        return citizensSupport
    }

    /**
     * Set Citizens support
     */
    @JvmStatic
    fun setCitizensSupport(citizensSupport: Boolean) {
        JoinNPC.citizensSupport = citizensSupport
        val bw: MainCommand? = MainCommand.getInstance()
        if (bw == null) {
            return
        }
        if (citizensSupport) {
            //add npc subCommand
            if (bw.isRegistered()) {
                var registered: Boolean = false
                for (sc in bw.getSubCommands()) {
                    if (sc.getSubCommandName().equals("npc", ignoreCase = true)) {
                        registered = true
                        break
                    }
                }
                if (!registered) {
                    com.andrei1058.bedwars.commands.bedwars.subcmds.sensitive.NPC(bw, "npc")
                }
            }
        } else {
            //remove npc subCommand
            if (bw.isRegistered()) {
                bw.getSubCommands().removeIf({ sc -> sc.getSubCommandName().equals("npc", ignoreCase = true) })
            }
        }
    }

    /**
     * Spawn a join-NPC
     *
     * @param group Arena Group
     * @param l     Location where to be spawned
     * @param name  Display name
     * @param skin  A player name to get his skin
     */
    @JvmStatic
    fun spawnNPC(l: Location, name: String, group: String, skin: String, spawnExisting: NPC?): NPC? {
        if (!isCitizensSupport()) {
            return null
        }
        var npc: NPC
        if (spawnExisting == null) {
            npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, "")
        } else {
            npc = spawnExisting
        }
        if (!npc.isSpawned()) {
            npc.spawn(l)
        }
        if (npc.getEntity() is SkinnableEntity) {
            (npc.getEntity() as SkinnableEntity).setSkinName(skin)
        }
        npc.setProtected(true)
        npc.setName("")
        val separator = "\\\\n"
        val nume = name.split(Regex(separator)).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (e in l.getWorld().getNearbyEntities(l, 1.0, 3.0, 1.0)) {
            if (e.getType() == EntityType.ARMOR_STAND) {
                e.remove()
            }
        }
        if (nume.size >= 2) {
            val a = createArmorStand(l.clone().add(0.0, 0.05, 0.0))
            a.setMarker(false)
            a.setCustomNameVisible(true)
            a.setCustomName(ChatColor.translateAlternateColorCodes('&', nume[0]).replace("{players}", Arena.getPlayers(group).toString()))
            npcs.put(npc.getId(), group)
            val a2 = createArmorStand(l.clone().subtract(0.0, 0.25, 0.0))
            a2.setMarker(false)
            a2.setCustomName(ChatColor.translateAlternateColorCodes('&', nume[1].replace("{players}", Arena.getPlayers(group).toString())))
            a2.setCustomNameVisible(true)
            npcs_holos.put(a, Arrays.asList(group, nume[0]))
            npcs_holos.put(a2, Arrays.asList(group, nume[1]))
        } else if (nume.size == 1) {
            npcs.put(npc.getId(), group)
            val a2 = createArmorStand(l.clone().subtract(0.0, 0.25, 0.0))
            a2.setMarker(false)
            a2.setCustomName(ChatColor.translateAlternateColorCodes('&', nume[0]).replace("{players}", Arena.getPlayers(group).toString()))
            a2.setCustomNameVisible(true)
            npcs_holos.put(a2, Arrays.asList(group, nume[0]))
        }
        npc.teleport(l, PlayerTeleportEvent.TeleportCause.PLUGIN)
        npc.setName("")
        return npc
    }

    /**
     * Spawn all the CmdJoin-NPCs
     */
    @JvmStatic
    fun spawnNPCs() {
        if (!isCitizensSupport()) {
            return
        }
        if (BedWars.config.getYml().get(ConfigPath.GENERAL_CONFIGURATION_NPC_LOC_STORAGE) != null) {
            for (s in BedWars.config.getYml().getStringList(ConfigPath.GENERAL_CONFIGURATION_NPC_LOC_STORAGE)) {
                val data = s.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                if (data.size < 10) {
                    continue
                }
                if (!Misc.isNumber(data[0])) {
                    continue
                }
                if (!Misc.isNumber(data[1])) {
                    continue
                }
                if (!Misc.isNumber(data[2])) {
                    continue
                }
                if (!Misc.isNumber(data[3])) {
                    continue
                }
                if (!Misc.isNumber(data[4])) {
                    continue
                }
                if (Misc.isNumber(data[5])) {
                    continue
                }
                if (Misc.isNumber(data[6])) {
                    continue
                }
                if (Misc.isNumber(data[7])) {
                    continue
                }
                if (Misc.isNumber(data[8])) {
                    continue
                }
                if (!Misc.isNumber(data[9])) {
                    continue
                }
                val l = Location(Bukkit.getWorld(data[5]), java.lang.Double.parseDouble(data[0]), java.lang.Double.parseDouble(data[1]), java.lang.Double.parseDouble(data[2]), java.lang.Float.parseFloat(data[3]), java.lang.Float.parseFloat(data[4]))
                val skin = data[6]
                val name = data[7]
                val group = data[8]
                val id: Int = Integer.parseInt(data[9])
                val npc: net.citizensnpcs.api.npc.NPC? = CitizensAPI.getNPCRegistry().getById(id)
                if (npc == null) {
                    BedWars.plugin.getLogger().severe("Invalid npc id: " + id)
                    continue
                }
                spawnNPC(l, name, group, skin, npc)
            }
        }
    }

    /**
     * Update CmdJoin NPCs for a group
     *
     * @param group arena group
     */
    @JvmStatic
    fun updateNPCs(group: String) {
        val x = Arena.getPlayers(group).toString()
        for (e in npcs_holos.entries) {
            if (e.value.get(0).equals(group, ignoreCase = true)) {
                if (e.key != null) {
                    if (!e.key.isDead()) {
                        e.key.setCustomName(ChatColor.translateAlternateColorCodes('&', e.value.get(1).replace("{players}", x)))
                    }
                }
            }
        }
    }
}
