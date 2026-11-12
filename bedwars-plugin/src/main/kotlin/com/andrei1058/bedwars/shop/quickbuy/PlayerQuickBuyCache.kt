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

package com.andrei1058.bedwars.shop.quickbuy

import com.andrei1058.bedwars.BedWars
import com.andrei1058.bedwars.api.configuration.ConfigPath
import com.andrei1058.bedwars.api.language.Language
import com.andrei1058.bedwars.api.language.Messages
import com.andrei1058.bedwars.shop.ShopCache
import com.andrei1058.bedwars.shop.ShopManager
import com.andrei1058.bedwars.shop.main.CategoryContent
import java.util.ArrayList
import java.util.HashMap
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import com.andrei1058.bedwars.api.util.unchecked
import com.andrei1058.bedwars.api.util.uninitialized

open class PlayerQuickBuyCache {
    private val elements: MutableList<QuickBuyElement> = ArrayList()
    private var emptyItemNamePath: String = uninitialized()
    private var emptyItemLorePath: String = uninitialized()
    private var emptyItem: ItemStack = uninitialized()
    private var player: UUID? = null
    private var task: QuickBuyTask? = null
    private val updateSlots: HashMap<Int, String> = HashMap()

    constructor(player: Player?) {
        if (player == null) {
            return
        }
        this.player = player.getUniqueId()
        this.emptyItem = BedWars.nms.createItemStack(BedWars.shop.getYml().getString(ConfigPath.SHOP_SETTINGS_QUICK_BUY_EMPTY_MATERIAL), BedWars.shop.getYml().getInt(ConfigPath.SHOP_SETTINGS_QUICK_BUY_EMPTY_AMOUNT), BedWars.shop.getYml().getInt(ConfigPath.SHOP_SETTINGS_QUICK_BUY_EMPTY_DATA).toShort())
        if (BedWars.shop.getYml().getBoolean(ConfigPath.SHOP_SETTINGS_QUICK_BUY_EMPTY_ENCHANTED)) {
            this.emptyItem = ShopManager.enchantItem(emptyItem)
        }
        this.emptyItemNamePath = Messages.SHOP_QUICK_EMPTY_NAME
        this.emptyItemLorePath = Messages.SHOP_QUICK_EMPTY_LORE
        task = QuickBuyTask(player.getUniqueId())
        quickBuyCaches.put(this.player.unchecked(), this)
    }

    /**
     * Add the player's preferences to the given inventory.
     * This will also add the red empty item.
     */
    open fun addInInventory(inv: Inventory, shopCache: ShopCache) {
        val p = Bukkit.getPlayer(player)
        for (qbe in elements) {
            inv.setItem(qbe.getSlot(), qbe.getCategoryContent()!!.getItemStack(p, shopCache))
        }
        if (elements.size == 21) {
            return
        }
        val i = getEmptyItem(p)
        for (x in quickSlots) {
            if (inv.getItem(x) == null) {
                inv.setItem(x, i)
            }
        }
    }

    open fun destroy() {
        elements.clear()
        if (task != null) {
            task!!.cancel()
        }
        quickBuyCaches.remove(player)
        this.pushChangesToDB()
    }

    open fun setElement(slot: Int, cc: CategoryContent?) {
        elements.removeIf({ q -> q.getSlot() == slot })
        var element: String
        if (cc == null) {
            element = " "
        } else {
            addQuickElement(QuickBuyElement(cc.getIdentifier().unchecked(), slot))
            element = cc.getIdentifier().unchecked()
        }
        if (updateSlots.containsKey(slot)) {
            updateSlots.replace(slot, element)
        } else {
            updateSlots.put(slot, element)
        }
    }

    private fun getEmptyItem(player: Player): ItemStack {
        val i = emptyItem.clone()
        val im: ItemMeta? = i.getItemMeta()
        if (im != null) {
            im.setDisplayName(Language.getMsg(player, emptyItemNamePath))
            im.setLore(Language.getList(player, emptyItemLorePath))
            i.setItemMeta(im)
        }
        return i
    }

    /**
     * Check if as category content at quick buy
     */
    open fun hasCategoryContent(cc: CategoryContent): Boolean {
        for (q in getElements()) {
            if (q.getCategoryContent() === cc) {
                return true
            }
        }
        return false
    }

    open fun getElements(): MutableList<QuickBuyElement> {
        return elements
    }

    /**
     * Add a quick buy element
     */
    open fun addQuickElement(e: QuickBuyElement) {
        this.elements.add(e)
    }

    open fun pushChangesToDB() {
        Bukkit.getScheduler().runTaskAsynchronously(BedWars.plugin, Runnable { BedWars.getRemoteDatabase().pushQuickBuyChanges(updateSlots, this.player.unchecked(), elements) })
    }

    companion object {
        @JvmField var quickSlots: IntArray = intArrayOf(19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43)
        private val quickBuyCaches: ConcurrentHashMap<UUID, PlayerQuickBuyCache> = ConcurrentHashMap()

        /**
         * Get a Player Quick buy cache
         */
        @JvmStatic
        fun getQuickBuyCache(uuid: UUID): PlayerQuickBuyCache? {
            return quickBuyCaches.getOrDefault(uuid, null)
        }
    }
}
