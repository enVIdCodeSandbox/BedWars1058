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

package com.andrei1058.bedwars.shop

import com.andrei1058.bedwars.BedWars
import com.andrei1058.bedwars.arena.Arena
import com.andrei1058.bedwars.shop.main.CategoryContent
import com.andrei1058.bedwars.shop.main.ShopCategory
import java.util.*
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import com.andrei1058.bedwars.api.util.unchecked

open class ShopCache(private var player: UUID) {
    private var cachedItems: MutableList<CachedItem>? = LinkedList()
    private var selectedCategory: Int
    private var categoryWeight: HashMap<ShopCategory, Byte>? = HashMap()

    open fun getPlayer(): UUID {
        return player
    }

    open fun setSelectedCategory(slot: Int) {
        this.selectedCategory = slot
    }

    open fun getSelectedCategory(): Int {
        return selectedCategory
    }

    open fun getContentTier(identifier: String): Int {
        val ci: CachedItem? = getCachedItem(identifier)
        return if (ci == null) 1 else ci.getTier()
    }

    /**
     * Destroy data
     */
    open fun destroy() {
        shopCaches.remove(this)
        cachedItems!!.clear()
        cachedItems = null
        categoryWeight = null
    }

    /**
     * Used to give items on player respawn
     */
    open fun managePermanentsAndDowngradables(arena: Arena) {
        BedWars.debug("Restore permanents on death for: " + player)
        for (ci in cachedItems!!) {
            ci.manageDeath(arena)
        }
    }

    /**
     * Keep trace of shop items and player's tiers
     */
    open inner class CachedItem(@JvmField var cc: CategoryContent) {
        @JvmField var tier: Int = 1

        open fun getTier(): Int {
            return tier
        }

        open fun getCc(): CategoryContent {
            return cc
        }

        /**
         * Give permanents on death
         * and downgrade if necessary
         */
        open fun manageDeath(arena: Arena) {
            if (!cc.isPermanent()) {
                return
            }
            if (cc.isDowngradable() && tier > 1) {
                tier--
            }
            BedWars.debug("ShopCache Item Restore: " + cc.getIdentifier() + " for " + player)
            //noinspection ConstantConditions
            cc.giveItems(Bukkit.getPlayer(player), getShopCache(player).unchecked(), arena)
        }

        open fun upgrade(slot: Int) {
            tier++
            val p = Bukkit.getPlayer(player)
            for (i in p.getInventory().getContents()) {
                if (i == null) {
                    continue
                }
                if (i.getType() == Material.AIR) {
                    continue
                }
                if (BedWars.nms.getShopUpgradeIdentifier(i) == cc.getIdentifier()) {
                    p.getInventory().remove(i)
                }
            }
            updateItem(slot, p)
            p.updateInventory()
        }

        open fun updateItem(slot: Int, p: Player) {
            if (p.getOpenInventory() != null) {
                if (BedWars.nms.getTopInventory(p) != null) {
                    BedWars.nms.getTopInventory(p)!!.setItem(slot, cc.getItemStack(Bukkit.getPlayer(player), getShopCache(player).unchecked()))
                }
            }
        }

        init {
            cachedItems!!.add(this)
            BedWars.debug("New Cached item " + cc.getIdentifier() + " for player " + player)
        }
    }

    /**
     * Get a player's cached item
     */
    open fun getCachedItem(identifier: String): CachedItem? {
        for (ci in cachedItems!!) {
            if (ci.getCc().getIdentifier() == identifier) {
                return ci
            }
        }
        return null
    }

    /**
     * Check if the player has a cached item
     */
    open fun hasCachedItem(cc: CategoryContent): Boolean {
        for (ci in cachedItems!!) {
            if (ci.getCc() === cc) {
                return true
            }
        }
        return false
    }

    open fun getCachedItem(cc: CategoryContent): CachedItem? {
        for (ci in cachedItems!!) {
            if (ci.getCc() === cc) {
                return ci
            }
        }
        return null
    }

    /**
     * Upgrade cached item
     * Add it if not found
     */
    open fun upgradeCachedItem(cc: CategoryContent, slot: Int) {
        var ci: CachedItem? = getCachedItem(cc.getIdentifier().unchecked())
        if (ci == null) {
            ci = CachedItem(cc)
            ci.updateItem(slot, Bukkit.getPlayer(player))
        } else {
            if (cc.getContentTiers().size > ci.getTier()) {
                BedWars.debug("Cached item upgrade for " + cc.getIdentifier() + " player " + player)
                ci.upgrade(slot)
            }
        }
    }

    /**
     * Used for categories where you can't buy lower items
     * Ex. if you have bought diamond iron from it, you can't buy stone iron
     */
    open fun setCategoryWeight(sc: ShopCategory, weight: Byte) {
        if (categoryWeight!!.containsKey(sc)) {
            categoryWeight!!.replace(sc, weight)
        } else {
            categoryWeight!!.put(sc, weight)
        }
    }

    open fun getCategoryWeight(sc: ShopCategory): Byte {
        return categoryWeight!!.getOrDefault(sc, 0.toByte())
    }

    /**
     * Get permanent and non downgradable shop items.
     */
    open fun getCachedPermanents(): MutableList<CachedItem> {
        val ci: MutableList<CachedItem> = ArrayList()
        for (c in cachedItems!!) {
            if (c.getCc().isPermanent()) {
                ci.add(c)
            }
        }
        return ci
    }

    open fun getCachedItems(): MutableList<CachedItem>? {
        return cachedItems
    }

    init {
        this.selectedCategory = ShopManager.getShop().getQuickBuyButton().getSlot()
        shopCaches.add(this)
    }

    companion object {
        private var shopCaches: MutableList<ShopCache> = ArrayList()

        @JvmStatic
        fun getShopCache(player: UUID): ShopCache? {
            for (sc in ArrayList(shopCaches)) {
                if (sc.player == player) {
                    return sc
                }
            }
            return null
        }
    }
}
