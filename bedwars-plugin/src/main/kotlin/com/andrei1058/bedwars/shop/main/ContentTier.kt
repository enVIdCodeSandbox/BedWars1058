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
import com.andrei1058.bedwars.api.arena.shop.IBuyItem
import com.andrei1058.bedwars.api.arena.shop.IContentTier
import com.andrei1058.bedwars.api.configuration.ConfigPath
import com.andrei1058.bedwars.shop.ShopManager
import java.util.ArrayList
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.inventory.ItemStack
import com.andrei1058.bedwars.api.util.unchecked
import com.andrei1058.bedwars.api.util.uninitialized

open class ContentTier : IContentTier {
    private var value: Int = 0
    private var price: Int = 0
    private var itemStack: ItemStack? = null
    private var currency: Material = uninitialized()
    private var buyItemsList: MutableList<IBuyItem> = ArrayList()
    private var loaded: Boolean = false

    /**
     * Create a content tier for a category content
     */
    constructor(path: String, tierName: String, identifier: String, yml: YamlConfiguration) {
        BedWars.debug("Loading content tier" + path)
        if (yml.get(path + ConfigPath.SHOP_CONTENT_TIER_ITEM_MATERIAL) == null) {
            BedWars.plugin.getLogger().severe("tier-item material not set at " + path)
            return
        }
        try {
            value = Integer.parseInt(tierName.replace("tier", ""))
        } catch (e: Exception) {
            BedWars.plugin.getLogger().severe(path + " doesn't end with a number. It's not recognized as a tier!")
            return
        }
        if (yml.get(path + ConfigPath.SHOP_CONTENT_TIER_SETTINGS_COST) == null) {
            BedWars.plugin.getLogger().severe("Cost not set for " + path)
            return
        }
        price = yml.getInt(path + ConfigPath.SHOP_CONTENT_TIER_SETTINGS_COST)
        if (yml.get(path + ConfigPath.SHOP_CONTENT_TIER_SETTINGS_CURRENCY) == null) {
            BedWars.plugin.getLogger().severe("Currency not set for " + path)
            return
        }
        if (yml.getString(path + ConfigPath.SHOP_CONTENT_TIER_SETTINGS_CURRENCY).isEmpty()) {
            BedWars.plugin.getLogger().severe("Invalid currency at " + path)
            return
        }
        when (yml.getString(path + ConfigPath.SHOP_CONTENT_TIER_SETTINGS_CURRENCY).lowercase()) {
            "iron", "gold", "diamond", "vault", "emerald" -> {
                currency = CategoryContent.getCurrency(yml.getString(path + ConfigPath.SHOP_CONTENT_TIER_SETTINGS_CURRENCY).lowercase())
            }
            else -> {
                BedWars.plugin.getLogger().severe("Invalid currency at " + path)
                currency = Material.IRON_INGOT
            }
        }
        itemStack = BedWars.nms.createItemStack(yml.getString(path + ConfigPath.SHOP_CONTENT_TIER_ITEM_MATERIAL), if (yml.get(path + ConfigPath.SHOP_CONTENT_TIER_ITEM_AMOUNT) == null) 1 else yml.getInt(path + ConfigPath.SHOP_CONTENT_TIER_ITEM_AMOUNT), (if (yml.get(path + ConfigPath.SHOP_CONTENT_TIER_ITEM_DATA) == null) 0 else yml.getInt(path + ConfigPath.SHOP_CONTENT_TIER_ITEM_DATA)).toShort())
        if (yml.get(path + ConfigPath.SHOP_CONTENT_TIER_ITEM_ENCHANTED) != null) {
            if (yml.getBoolean(path + ConfigPath.SHOP_CONTENT_TIER_ITEM_ENCHANTED)) {
                itemStack = ShopManager.enchantItem(itemStack.unchecked())
            }
        }
        // potion display color based on NBT tag
        if (yml.getString(path + ".tier-item.potion-display") != null && !yml.getString(path + ".tier-item.potion-display").isEmpty()) {
            itemStack = nms.setTag(itemStack.unchecked(), "Potion", yml.getString(path + ".tier-item.potion-display"))
        }
        // 1.16+ custom color
        if (yml.getString(path + ".tier-item.potion-color") != null && !yml.getString(path + ".tier-item.potion-color").isEmpty()) {
            itemStack = nms.setTag(itemStack.unchecked(), "CustomPotionColor", yml.getString(path + ".tier-item.potion-color"))
        }
        if (itemStack != null) {
            itemStack!!.setItemMeta(ShopManager.hideItemStuff(itemStack!!.getItemMeta()))
        }
        var bi: IBuyItem
        if (yml.get(path + "." + ConfigPath.SHOP_CONTENT_BUY_ITEMS_PATH) != null) {
            for (s in yml.getConfigurationSection(path + "." + ConfigPath.SHOP_CONTENT_BUY_ITEMS_PATH).getKeys(false)) {
                bi = BuyItem(path + "." + ConfigPath.SHOP_CONTENT_BUY_ITEMS_PATH + "." + s, yml, identifier, this)
                if (bi.isLoaded()) {
                    buyItemsList.add(bi)
                }
            }
        }
        if (yml.get(path + "." + ConfigPath.SHOP_CONTENT_BUY_CMDS_PATH) != null) {
            bi = BuyCommand(path + "." + ConfigPath.SHOP_CONTENT_BUY_CMDS_PATH, yml, identifier)
            if (bi.isLoaded()) {
                buyItemsList.add(bi)
            }
        }
        if (buyItemsList.isEmpty()) {
            Bukkit.getLogger().warning("Loaded 0 buy content for: " + path)
        }
        loaded = true
    }

    /**
     * Get tier price
     */
    override fun getPrice(): Int {
        return price
    }

    /**
     * Get tier currency
     */
    override fun getCurrency(): Material {
        return currency
    }

    /**
     * Set tier currency.
     */
    override fun setCurrency(currency: Material) {
        this.currency = currency
    }

    /**
     * Set tier price.
     */
    override fun setPrice(price: Int) {
        this.price = price
    }

    /**
     * Set tier preview item.
     */
    override fun setItemStack(itemStack: ItemStack) {
        this.itemStack = itemStack
    }

    /**
     * Set list of items that you receive on buy.
     */
    override fun setBuyItemsList(buyItemsList: MutableList<IBuyItem>) {
        this.buyItemsList = buyItemsList
    }

    /**
     * Get item stack with name and lore in player's language
     */
    override fun getItemStack(): ItemStack? {
        return itemStack
    }

    /**
     * Get tier level
     */
    override fun getValue(): Int {
        return value
    }

    /**
     * Check if tier is loaded
     */
    open fun isLoaded(): Boolean {
        return loaded
    }

    /**
     * Get items
     */
    override fun getBuyItemsList(): MutableList<IBuyItem> {
        return buyItemsList
    }
}
