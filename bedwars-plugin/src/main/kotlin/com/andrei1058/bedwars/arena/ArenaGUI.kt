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

package com.andrei1058.bedwars.arena

import com.andrei1058.bedwars.BedWars
import com.andrei1058.bedwars.api.arena.GameState
import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.api.configuration.ConfigPath
import com.andrei1058.bedwars.api.language.Language
import com.andrei1058.bedwars.api.language.Messages
import com.andrei1058.bedwars.configuration.Sounds
import com.andrei1058.bedwars.listeners.arenaselector.ArenaSelectorListener
import java.util.ArrayList
import java.util.HashMap
import java.util.UUID
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

object ArenaGUI {
    //private static HashMap<Player, Object[]> refresh = new HashMap<>();
    private var yml: YamlConfiguration = BedWars.config.getYml()
    private var antiCalledTwice: HashMap<UUID, Long> = HashMap()

    //Object[0] = inventory, Object[1] = group
    @JvmStatic
    fun refreshInv(p: Player?, arena: IArena?, players: Int) {
        if (p == null) {
            return
        }
        if (p.getOpenInventory() == null) {
            return
        }
        if (BedWars.nms.getTopInventory(p)!!.getHolder() !is ArenaSelectorHolder) {
            return
        }
        val ash = (BedWars.nms.getTopInventory(p)!!.getHolder() as ArenaSelectorHolder)
        var arenas: MutableList<IArena>
        if (ash.getGroup().equals("default", ignoreCase = true)) {
            arenas = ArrayList(Arena.getArenas())
        } else {
            arenas = ArrayList()
            // several groups can be combined with a plus, like in /bw join
            val groups = ash.getGroup().split("\\+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            for (a in Arena.getArenas()) {
                for (group in groups) {
                    if (a.getGroup().equals(group, ignoreCase = true)) {
                        arenas.add(a)
                        break
                    }
                }
            }
        }
        arenas = Arena.getSorted(arenas)
        var arenaKey: Int = 0
        for (slot in getUsedSlots()) {
            var i: ItemStack
            BedWars.nms.getTopInventory(p)!!.setItem(slot, ItemStack(Material.AIR))
            if (arenaKey >= arenas.size) {
                continue
            }
            var status: String
            when (arenas.get(arenaKey).getStatus()) {
                GameState.waiting -> {
                    status = "waiting"
                }
                GameState.playing -> {
                    status = "playing"
                }
                GameState.starting -> {
                    status = "starting"
                }
                else -> {
                    continue
                }
            }
            i = BedWars.nms.createItemStack(yml.getString(ConfigPath.GENERAL_CONFIGURATION_ARENA_SELECTOR_STATUS_MATERIAL.replace("%path%", status)), 1, yml.getInt(ConfigPath.GENERAL_CONFIGURATION_ARENA_SELECTOR_STATUS_DATA.replace("%path%", status)).toShort())
            if (yml.getBoolean(ConfigPath.GENERAL_CONFIGURATION_ARENA_SELECTOR_STATUS_ENCHANTED.replace("%path%", status))) {
                val im = i.getItemMeta()
                im.addEnchant(Enchantment.LURE, 1, true)
                im.addItemFlags(ItemFlag.HIDE_ENCHANTS)
                i.setItemMeta(im)
            }
            val im = i.getItemMeta()
            im.setDisplayName(Language.getMsg(p, Messages.ARENA_GUI_ARENA_CONTENT_NAME).replace("{name}", arenas.get(arenaKey).getDisplayName()).replace("{map_name}", arenas.get(arenaKey).getArenaName()))
            val lore: MutableList<String> = ArrayList()
            for (s in Language.getList(p, Messages.ARENA_GUI_ARENA_CONTENT_LORE)) {
                if (!(s.contains("{group}") && arenas.get(arenaKey).getGroup().equals("default", ignoreCase = true))) {
                    lore.add(s.replace("{on}", (if (arena != null) if (arena === arenas.get(arenaKey)) players else arenas.get(arenaKey).getPlayers()!!.size else arenas.get(arenaKey).getPlayers()!!.size).toString()).replace("{max}", arenas.get(arenaKey).getMaxPlayers().toString()).replace("{status}", arenas.get(arenaKey).getDisplayStatus(Language.getPlayerLanguage(p))).replace("{group}", arenas.get(arenaKey).getDisplayGroup(p)))
                }
            }
            im.setLore(lore)
            i.setItemMeta(im)
            i = BedWars.nms.addCustomData(i, ArenaSelectorListener.ARENA_SELECTOR_GUI_IDENTIFIER + arenas.get(arenaKey).getWorldName())
            BedWars.nms.getTopInventory(p)!!.setItem(slot, i)
            arenaKey++
        }
        p.updateInventory()
    }

    @JvmStatic
    fun openGui(p: Player, group: String) {
        if (preventCalledTwice(p)) {
            return
        }
        updateCalledTwice(p)
        var size: Int = BedWars.config.getYml().getInt(ConfigPath.GENERAL_CONFIGURATION_ARENA_SELECTOR_SETTINGS_SIZE)
        if (size % 9 != 0) {
            size = 27
        }
        if (size > 54) {
            size = 54
        }
        val ash = ArenaSelectorHolder(group)
        val inv = Bukkit.createInventory(ash, size, Language.getMsg(p, Messages.ARENA_GUI_INV_NAME))
        val skippedSlotMaterial = BedWars.config.getString(ConfigPath.GENERAL_CONFIGURATION_ARENA_SELECTOR_STATUS_MATERIAL.replace("%path%", "skipped-slot"))
        if (!skippedSlotMaterial.equals("none", ignoreCase = true) && !skippedSlotMaterial.equals("air", ignoreCase = true)) {
            var i = BedWars.nms.createItemStack(skippedSlotMaterial, 1, BedWars.config.getInt(ConfigPath.GENERAL_CONFIGURATION_ARENA_SELECTOR_STATUS_DATA.replace("%path%", "skipped-slot")).toByte().toShort())
            i = BedWars.nms.addCustomData(i, "RUNCOMMAND_bw join random")
            val im: ItemMeta? = i.getItemMeta()
            assert(im != null)
            im!!.setDisplayName(ChatColor.translateAlternateColorCodes('&', Language.getMsg(p, Messages.ARENA_GUI_SKIPPED_ITEM_NAME).replace("\\{serverIp}".toRegex(), BedWars.config.getString(ConfigPath.GENERAL_CONFIG_PLACEHOLDERS_REPLACEMENTS_SERVER_IP)).replace("\\{poweredBy}".toRegex(), BedWars.config.getString(ConfigPath.GENERAL_CONFIG_PLACEHOLDERS_REPLACEMENTS_POWERED_BY))))
            val lore: MutableList<String> = ArrayList()
            for (line0 in Language.getList(p, Messages.ARENA_GUI_SKIPPED_ITEM_LORE)) {
                var line = line0
                line = line.replace("\\{serverIp}".toRegex(), BedWars.config.getString(ConfigPath.GENERAL_CONFIG_PLACEHOLDERS_REPLACEMENTS_SERVER_IP)).replace("\\{poweredBy}".toRegex(), BedWars.config.getString(ConfigPath.GENERAL_CONFIG_PLACEHOLDERS_REPLACEMENTS_POWERED_BY))
                lore.add(line)
            }
            if (lore.size > 0) {
                im!!.setLore(lore)
            }
            im!!.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
            i.setItemMeta(im)
            val used = getUsedSlots()
            var x: Int = 0
            while (x < inv.getSize()) {
                if (used.contains(x)) {
                    x++
                    continue
                }
                inv.setItem(x, i)
                x++
            }
        }
        p.openInventory(inv)
        refreshInv(p, null, 0)
        //refresh.put(p, new Object[]{inv, group});
        Sounds.playSound("arena-selector-open", p)
    }

    open class ArenaSelectorHolder(@JvmField var group: String) : InventoryHolder {
        open fun getGroup(): String {
            return group
        }

        override fun getInventory(): Inventory? {
            return null
        }
    }

    @JvmStatic
    private fun getUsedSlots(): MutableList<Int> {
        val ls: MutableList<Int> = ArrayList()
        for (useSlot in BedWars.config.getString(ConfigPath.GENERAL_CONFIGURATION_ARENA_SELECTOR_SETTINGS_USE_SLOTS).split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
            try {
                val slot: Int = Integer.parseInt(useSlot)
                ls.add(slot)
            } catch (ignored: Exception) {
            }
        }
        return ls
    }

    @JvmStatic
    private fun preventCalledTwice(player: Player): Boolean {
        return antiCalledTwice.getOrDefault(player.getUniqueId(), 0L) > System.currentTimeMillis()
    }

    @JvmStatic
    private fun updateCalledTwice(player: Player) {
        if (antiCalledTwice.containsKey(player.getUniqueId())) {
            antiCalledTwice.replace(player.getUniqueId(), System.currentTimeMillis() + 2000)
        } else {
            antiCalledTwice.put(player.getUniqueId(), System.currentTimeMillis() + 2000)
        }
    }
}
