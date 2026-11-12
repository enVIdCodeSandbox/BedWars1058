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
import com.andrei1058.bedwars.api.events.shop.ShopOpenEvent
import com.andrei1058.bedwars.api.language.Language
import com.andrei1058.bedwars.arena.Arena
import com.andrei1058.bedwars.shop.ShopCache
import com.andrei1058.bedwars.shop.quickbuy.PlayerQuickBuyCache
import java.util.ArrayList
import java.util.UUID
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import com.andrei1058.bedwars.api.util.unchecked

open class ShopIndex(private var namePath: String, private var quickBuyButton: QuickBuyButton, private var separatorNamePath: String, private var separatorLorePath: String, @JvmField var separatorSelected: ItemStack, @JvmField var separatorStandard: ItemStack) {
    private var invSize: Int = 54
    private var categoryList: MutableList<ShopCategory> = ArrayList()

    /**
     * Open this shop to a player
     *
     * @param callEvent     true if you want to call the shop open event
     * @param quickBuyCache the player cache regarding his preferences
     * @param player        target player
     */
    open fun open(player: Player, quickBuyCache: PlayerQuickBuyCache?, callEvent: Boolean) {
        if (quickBuyCache == null) {
            return
        }
        if (callEvent) {
            val event = ShopOpenEvent(player, Arena.getArenaByPlayer(player))
            Bukkit.getPluginManager().callEvent(event)
            if (event.isCancelled()) {
                return
            }
        }
        val inv = Bukkit.createInventory(null, invSize, Language.getMsg(player, getNamePath()))
        inv.setItem(getQuickBuyButton().getSlot(), getQuickBuyButton().getItemStack(player))
        for (sc in getCategoryList()) {
            inv.setItem(sc.getSlot(), sc.getItemStack(player))
        }
        addSeparator(player, inv)
        inv.setItem(getQuickBuyButton().getSlot() + 9, getSelectedItem(player))
        //noinspection ConstantConditions
        ShopCache.getShopCache(player.getUniqueId())!!.setSelectedCategory(getQuickBuyButton().getSlot())
        quickBuyCache.addInInventory(inv, ShopCache.getShopCache(player.getUniqueId()).unchecked())
        player.openInventory(inv)
        if (!indexViewers.contains(player.getUniqueId())) {
            indexViewers.add(player.getUniqueId())
        }
    }

    /**
     * Add shop separator between categories and items
     */
    open fun addSeparator(player: Player, inv: Inventory) {
        val i = separatorStandard.clone()
        val im: ItemMeta? = i.getItemMeta()
        if (im != null) {
            im.setDisplayName(Language.getMsg(player, separatorNamePath))
            im.setLore(Language.getList(player, separatorLorePath))
            i.setItemMeta(im)
        }
        for (x in 9 until 18) {
            inv.setItem(x, i)
        }
    }

    /**
     * This is the item that indicates the selected category
     */
    open fun getSelectedItem(player: Player): ItemStack {
        val i = separatorSelected.clone()
        val im: ItemMeta? = i.getItemMeta()
        if (im != null) {
            im.setDisplayName(Language.getMsg(player, separatorNamePath))
            im.setLore(Language.getList(player, separatorLorePath))
            i.setItemMeta(im)
        }
        return i
    }

    /**
     * Add a shop category
     */
    open fun addShopCategory(sc: ShopCategory) {
        categoryList.add(sc)
        BedWars.debug("Adding shop category: " + sc + " at slot " + sc.getSlot())
    }

    /**
     * Get the inventory name path
     */
    open fun getNamePath(): String {
        return namePath
    }

    /**
     * Get the inventory size
     */
    open fun getInvSize(): Int {
        return invSize
    }

    /**
     * Get the shop's categories
     */
    open fun getCategoryList(): MutableList<ShopCategory> {
        return categoryList
    }

    /**
     * Get the quick buy button
     */
    open fun getQuickBuyButton(): QuickBuyButton {
        return quickBuyButton
    }

    companion object {
        @JvmField var indexViewers: MutableList<UUID> = ArrayList()

        @JvmStatic
        fun getIndexViewers(): MutableList<UUID> {
            return ArrayList(indexViewers)
        }
    }
}
