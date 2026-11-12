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

package com.andrei1058.bedwars.arena.spectator

import com.andrei1058.bedwars.BedWars
import com.andrei1058.bedwars.BedWars.Companion.nms
import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.api.arena.team.ITeam
import com.andrei1058.bedwars.api.language.Language
import com.andrei1058.bedwars.api.language.Language.Companion.getList
import com.andrei1058.bedwars.api.language.Language.Companion.getMsg
import com.andrei1058.bedwars.api.language.Messages
import com.andrei1058.bedwars.arena.Arena
import java.util.ArrayList
import java.util.HashMap
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

object TeleporterGUI {
    //Don't remove "_" because it's used as a separator somewhere
    const val NBT_SPECTATOR_TELEPORTER_GUI_HEAD: String = "spectatorTeleporterGUIhead_"
    private var refresh: HashMap<Player, Inventory> = HashMap()

    /**
     * Refresh the Teleporter GUI for a player
     */
    @JvmStatic
    fun refreshInv(p: Player, inv: Inventory) {
        if (p.getOpenInventory() == null) {
            return
        }
        val arena: IArena? = Arena.getArenaByPlayer(p)
        if (arena == null) {
            p.closeInventory()
            return
        }
        val players = arena.getPlayers()
        var i: Int = 0
        while (i < inv.getSize()) {
            if (i < players!!.size) {
                inv.setItem(i, createHead(players!!.get(i), p))
            } else {
                inv.setItem(i, ItemStack(Material.AIR))
            }
            i++
        }
    }

    /**
     * Opens the Teleporter GUI to a Player
     */
    @JvmStatic
    fun openGUI(p: Player) {
        val arena: IArena? = Arena.getArenaByPlayer(p)
        if (arena == null) {
            return
        }
        val playerCount: Int = arena.getPlayers()!!.size
        var size: Int = if ((playerCount % 9) == 0) playerCount else (Math.ceil(playerCount / 9.0).toInt()) * 9
        if (size > 54) {
            size = 54
        }
        val inv = Bukkit.createInventory(p, size, getMsg(p, Messages.ARENA_SPECTATOR_TELEPORTER_GUI_NAME))
        refreshInv(p, inv)
        refresh.put(p, inv)
        p.openInventory(inv)
    }

    /**
     * Get a HashMap of players with Teleporter GUI opened
     */
    @JvmStatic
    fun getRefresh(): HashMap<Player, Inventory> {
        return refresh
    }

    /**
     * Refresh the Teleporter GUI for all players with it opened
     */
    @JvmStatic
    fun refreshAllGUIs() {
        for (e in HashMap(getRefresh()).entries) {
            refreshInv(e.key, e.value)
        }
    }

    /**
     * Create a player head
     */
    @JvmStatic
    private fun createHead(targetPlayer: Player, GUIholder: Player): ItemStack {
        val i = nms.getPlayerHead(targetPlayer, null)
        val im: ItemMeta? = i.getItemMeta()
        assert(im != null)
        val currentArena = Arena.getArenaByPlayer(targetPlayer)
        val targetPlayerTeam = currentArena.getTeam(targetPlayer)
        im!!.setDisplayName(getMsg(GUIholder, Messages.ARENA_SPECTATOR_TELEPORTER_GUI_HEAD_NAME).replace("{vPrefix}", BedWars.getChatSupport().getPrefix(targetPlayer)).replace("{vSuffix}", BedWars.getChatSupport().getSuffix(targetPlayer)).replace("{team}", targetPlayerTeam!!.getDisplayName(Language.getPlayerLanguage(GUIholder))).replace("{teamColor}", targetPlayerTeam!!.getColor().chat().toString()).replace("{player}", targetPlayer.getDisplayName()).replace("{playername}", targetPlayer.getName()))
        val lore: MutableList<String> = ArrayList()
        val health = ((targetPlayer.getHealth().toInt()) * 100 / targetPlayer.getHealthScale()).toString()
        for (s in getList(GUIholder, Messages.ARENA_SPECTATOR_TELEPORTER_GUI_HEAD_LORE)) {
            lore.add(s.replace("{health}", health).replace("{food}", targetPlayer.getFoodLevel().toString()))
        }
        im!!.setLore(lore)
        i.setItemMeta(im)
        return nms.addCustomData(i, NBT_SPECTATOR_TELEPORTER_GUI_HEAD + targetPlayer.getName())
    }

    /**
     * Remove a player from the refresh list and close gui
     */
    @JvmStatic
    fun closeGUI(p: Player) {
        if (getRefresh().containsKey(p)) {
            refresh.remove(p)
            p.closeInventory()
        }
    }
}
