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

package com.andrei1058.bedwars.upgrades.menu

import com.andrei1058.bedwars.BedWars
import com.andrei1058.bedwars.api.arena.team.ITeam
import com.andrei1058.bedwars.api.language.Language
import com.andrei1058.bedwars.api.language.Messages
import com.andrei1058.bedwars.api.upgrades.MenuContent
import com.andrei1058.bedwars.upgrades.UpgradesManager
import java.util.Collections
import java.util.HashMap
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import com.andrei1058.bedwars.api.util.unchecked

open class MenuCategory(private var name: String, displayItem: ItemStack) : MenuContent {
    private var displayItem: ItemStack
    private var menuContentBySlot: HashMap<Int, MenuContent> = HashMap()

    /**
     * Add content to a menu.
     *
     * @param content content instance.
     * @param slot    where to put the content in the menu.
     * @return false if te given slot is in use.
     */
    open fun addContent(content: MenuContent, slot: Int): Boolean {
        if (menuContentBySlot.get(slot) != null) {
            return false
        }
        menuContentBySlot.put(slot, content)
        return true
    }

    override fun getDisplayItem(player: Player, team: ITeam): ItemStack {
        val i = ItemStack(displayItem)
        val im: ItemMeta? = i.getItemMeta()
        if (im != null) {
            im.setDisplayName(Language.getMsg(player, Messages.UPGRADES_CATEGORY_ITEM_NAME_PATH + name.replace("category-", "")))
            val lore = Language.getList(player, Messages.UPGRADES_CATEGORY_ITEM_LORE_PATH + name.replace("category-", ""))
            if (name.equals("traps", ignoreCase = true)) {
                var queueLimit: Int = UpgradesManager.getConfiguration().getInt(team.getArena()!!.getGroup().lowercase() + "-upgrades-settings.trap-queue-limit")
                if (queueLimit == 0) {
                    queueLimit = UpgradesManager.getConfiguration().getInt("default-upgrades-settings.trap-queue-limit")
                }
                if (queueLimit == team.getActiveTraps().size) {
                    lore.add("")
                    lore.add(Language.getMsg(player, Messages.UPGRADES_TRAP_QUEUE_LIMIT))
                }
            }
            im.setLore(lore)
            i.setItemMeta(im)
        }
        return i
    }

    override fun getName(): String? {
        return name
    }

    override fun onClick(player: Player, clickType: ClickType, team: ITeam?) {
        if (name.equals("category-traps", ignoreCase = true)) {
            var queueLimit: Int = UpgradesManager.getConfiguration().getInt(team!!.getArena()!!.getGroup().lowercase() + "-upgrades-settings.trap-queue-limit")
            if (queueLimit == 0) {
                queueLimit = UpgradesManager.getConfiguration().getInt("default-upgrades-settings.trap-queue-limit")
            }
            if (queueLimit <= team!!.getActiveTraps().size) {
                player.sendMessage(Language.getMsg(player, Messages.UPGRADES_TRAP_QUEUE_LIMIT))
                return
            }
        }
        val inv = Bukkit.createInventory(null, 45, Language.getMsg(player, Messages.UPGRADES_CATEGORY_GUI_NAME_PATH + name.replace("category-", "")))
        for (entry in menuContentBySlot.entries) {
            inv.setItem(entry.key, entry.value.getDisplayItem(player, team.unchecked()))
        }
        player.openInventory(inv)
        UpgradesManager.setWatchingUpgrades(player.getUniqueId())
    }

    init {
        this.displayItem = BedWars.nms.addCustomData(displayItem, "MCONT_" + name)
        Language.saveIfNotExists(Messages.UPGRADES_CATEGORY_GUI_NAME_PATH + name.replace("category-", ""), "&8" + name)
        Language.saveIfNotExists(Messages.UPGRADES_CATEGORY_ITEM_NAME_PATH + name.replace("category-", ""), "&cName not set")
        Language.saveIfNotExists(Messages.UPGRADES_CATEGORY_ITEM_LORE_PATH + name.replace("category-", ""), Collections.singletonList("&cLore not set"))
    }
}
