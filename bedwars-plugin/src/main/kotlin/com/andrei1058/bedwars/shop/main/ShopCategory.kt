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

package com.andrei1058.bedwars.shop.main

import com.andrei1058.bedwars.BedWars
import com.andrei1058.bedwars.BedWars.Companion.nms
import com.andrei1058.bedwars.api.configuration.ConfigPath
import com.andrei1058.bedwars.api.language.Language
import com.andrei1058.bedwars.api.language.Messages
import com.andrei1058.bedwars.shop.ShopCache
import com.andrei1058.bedwars.shop.ShopManager
import java.util.ArrayList
import java.util.UUID
import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import com.andrei1058.bedwars.api.util.uninitialized

open class ShopCategory {
    private var slot: Int = 0
    private var itemStack: ItemStack = uninitialized()
    private var itemNamePath: String = uninitialized()
    private var itemLorePath: String = uninitialized()
    private var invNamePath: String = uninitialized()
    private var loaded: Boolean = false
    private val categoryContentList: MutableList<CategoryContent> = ArrayList()
    private var name: String = uninitialized()

    /**
     * Load a shop category from the given path
     */
    constructor(path: String, yml: YamlConfiguration) {
        BedWars.debug("Loading shop category: " + path)
        this.name = path
        if (yml.get(path + ConfigPath.SHOP_CATEGORY_ITEM_MATERIAL) == null) {
            BedWars.plugin.getLogger().severe("Category material not set at: " + path)
            return
        }
        if (yml.get(path + ConfigPath.SHOP_CATEGORY_SLOT) == null) {
            BedWars.plugin.getLogger().severe("Category slot not set at: " + path)
            return
        }
        slot = yml.getInt(path + ConfigPath.SHOP_CATEGORY_SLOT)
        if (slot < 1 || slot > 8) {
            BedWars.plugin.getLogger().severe("Slot must be n > 1 and n < 9 at: " + path)
            return
        }
        for (sc in ShopManager.getShop().getCategoryList()) {
            if (sc.getSlot() == slot) {
                BedWars.plugin.getLogger().severe("Slot is already in use at: " + path)
                return
            }
        }
        itemStack = BedWars.nms.createItemStack(yml.getString(path + ConfigPath.SHOP_CATEGORY_ITEM_MATERIAL), if (yml.get(path + ConfigPath.SHOP_CATEGORY_ITEM_AMOUNT) == null) 1 else yml.getInt(path + ConfigPath.SHOP_CATEGORY_ITEM_AMOUNT), (if (yml.get(path + ConfigPath.SHOP_CATEGORY_ITEM_DATA) == null) 0 else yml.getInt(path + ConfigPath.SHOP_CATEGORY_ITEM_DATA)).toShort())
        if (yml.get(path + ConfigPath.SHOP_CATEGORY_ITEM_ENCHANTED) != null) {
            if (yml.getBoolean(path + ConfigPath.SHOP_CATEGORY_ITEM_ENCHANTED)) {
                itemStack = ShopManager.enchantItem(itemStack)
            }
        }
        // potion display color based on NBT tag
        if (yml.getString(path + ".category-item.potion-display") != null && !yml.getString(path + ".category-item.potion-display").isEmpty()) {
            itemStack = nms.setTag(itemStack, "Potion", yml.getString(path + ".category-item.potion-display"))
        }
        // 1.16+ custom color
        if (yml.getString(path + ".category-item.potion-color") != null && !yml.getString(path + ".category-item.potion-color").isEmpty()) {
            itemStack = nms.setTag(itemStack, "CustomPotionColor", yml.getString(path + ".category-item.potion-color"))
        }
        if (itemStack.getItemMeta() != null) {
            itemStack.setItemMeta(ShopManager.hideItemStuff(itemStack.getItemMeta()))
        }
        itemNamePath = Messages.SHOP_CATEGORY_ITEM_NAME.replace("%category%", path)
        itemLorePath = Messages.SHOP_CATEGORY_ITEM_LORE.replace("%category%", path)
        invNamePath = Messages.SHOP_CATEGORY_INVENTORY_NAME.replace("%category%", path)
        loaded = true
        var cc: CategoryContent
        for (s in yml.getConfigurationSection(path + "." + ConfigPath.SHOP_CATEGORY_CONTENT_PATH).getKeys(false)) {
            cc = CategoryContent(path + ConfigPath.SHOP_CATEGORY_CONTENT_PATH + "." + s, s, path, yml, this)
            if (cc.isLoaded()) {
                categoryContentList.add(cc)
                BedWars.debug("Adding CategoryContent: " + s + " to Shop Category: " + path)
            }
        }
    }

    open fun open(player: Player, index: ShopIndex, shopCache: ShopCache) {
        if (BedWars.nms.getTopInventory(player) == null) {
            return
        }
        ShopIndex.indexViewers.remove(player.getUniqueId())
        val inv = Bukkit.createInventory(null, index.getInvSize(), Language.getMsg(player, invNamePath))
        inv.setItem(index.getQuickBuyButton().getSlot(), index.getQuickBuyButton().getItemStack(player))
        for (sc in index.getCategoryList()) {
            inv.setItem(sc.getSlot(), sc.getItemStack(player))
        }
        index.addSeparator(player, inv)
        inv.setItem(getSlot() + 9, index.getSelectedItem(player))
        shopCache.setSelectedCategory(getSlot())
        for (cc in getCategoryContentList()) {
            inv.setItem(cc.getSlot(), cc.getItemStack(player, shopCache))
        }
        player.openInventory(inv)
        if (!categoryViewers.contains(player.getUniqueId())) {
            categoryViewers.add(player.getUniqueId())
        }
    }

    /**
     * Get the category preview item in player's language
     */
    open fun getItemStack(player: Player): ItemStack {
        val i = itemStack.clone()
        val im: ItemMeta? = i.getItemMeta()
        if (im != null) {
            im.setDisplayName(Language.getMsg(player, itemNamePath))
            im.setLore(Language.getList(player, itemLorePath))
            i.setItemMeta(im)
        }
        return i
    }

    /**
     * Check if category was loaded
     */
    open fun isLoaded(): Boolean {
        return loaded
    }

    /**
     * Get category slot in shop index
     */
    open fun getSlot(): Int {
        return slot
    }

    open fun getCategoryContentList(): MutableList<CategoryContent> {
        return categoryContentList
    }

    open fun getName(): String {
        return name
    }

    companion object {
        @JvmField var categoryViewers: MutableList<UUID> = ArrayList()

        /**Get a category content by identifier*/
        @JvmStatic
        fun getCategoryContent(identifier: String, shopIndex: ShopIndex): CategoryContent? {
            for (sc in shopIndex.getCategoryList()) {
                for (cc in sc.getCategoryContentList()) {
                    if (cc.getIdentifier() == identifier) {
                        return cc
                    }
                }
            }
            return null
        }

        @JvmStatic
        fun getCategoryViewers(): MutableList<UUID> {
            return ArrayList(categoryViewers)
        }
    }
}
