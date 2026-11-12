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
import com.andrei1058.bedwars.api.configuration.ConfigPath
import com.andrei1058.bedwars.api.language.Language.Companion.getMsg
import com.andrei1058.bedwars.api.language.Messages
import com.andrei1058.bedwars.arena.Arena
import com.andrei1058.bedwars.arena.Misc
import com.andrei1058.bedwars.configuration.Sounds
import java.util.ArrayList
import net.citizensnpcs.api.CitizensAPI
import net.citizensnpcs.api.event.NPCRemoveEvent
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEntityEvent
import com.andrei1058.bedwars.api.util.unchecked

open class CitizensListener : Listener {
    @EventHandler
    open fun removeNPC(e: NPCRemoveEvent?) {
        if (e == null) {
            return
        }
        if (e.getNPC() == null) {
            return
        }
        if (e.getNPC().getEntity() == null) {
            return
        }
        val locations = BedWars.config.getYml().getStringList(ConfigPath.GENERAL_CONFIGURATION_NPC_LOC_STORAGE)
        var removed: Boolean = false
        if (JoinNPC.npcs.containsKey(e.getNPC().getId())) {
            JoinNPC.npcs.remove(e.getNPC().getId())
            removed = true
        }
        for (s in ArrayList(locations)) {
            val data = s.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (data.size >= 10) {
                if (Misc.isNumber(data[9])) {
                    if (Integer.parseInt(data[9]) == e.getNPC().getId()) {
                        locations.remove(s)
                        removed = true
                    }
                }
            }
        }
        for (e2 in e.getNPC().getEntity().getNearbyEntities(0.0, 3.0, 0.0)) {
            if (e2.getType() == EntityType.ARMOR_STAND) {
                e2.remove()
            }
        }
        if (removed) {
            BedWars.config.set(ConfigPath.GENERAL_CONFIGURATION_NPC_LOC_STORAGE, locations)
        }
    }

    @EventHandler
    open fun onNPCInteract(e: PlayerInteractEntityEvent) {
        if (!JoinNPC.isCitizensSupport()) {
            return
        }
        if (e.getPlayer().isSneaking()) {
            return
        }
        if (!e.getRightClicked().hasMetadata("NPC")) {
            return
        }
        val npc: net.citizensnpcs.api.npc.NPC? = CitizensAPI.getNPCRegistry().getNPC(e.getRightClicked())
        if (npc == null) {
            return
        }
        if (JoinNPC.npcs.containsKey(npc.getId())) {
            if (!Arena.joinRandomFromGroup(e.getPlayer(), JoinNPC.npcs.get(npc.getId()).unchecked())) {
                e.getPlayer().sendMessage(getMsg(e.getPlayer(), Messages.COMMAND_JOIN_NO_EMPTY_FOUND))
                Sounds.playSound("join-denied", e.getPlayer())
            } else {
                Sounds.playSound("join-allowed", e.getPlayer())
            }
        }
    }
}
