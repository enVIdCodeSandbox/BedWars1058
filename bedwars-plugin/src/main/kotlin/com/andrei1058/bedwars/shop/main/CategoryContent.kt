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
import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.api.arena.shop.IBuyItem
import com.andrei1058.bedwars.api.arena.shop.ICategoryContent
import com.andrei1058.bedwars.api.arena.shop.IContentTier
import com.andrei1058.bedwars.api.configuration.ConfigPath
import com.andrei1058.bedwars.api.events.shop.ShopBuyEvent
import com.andrei1058.bedwars.api.language.Language
import com.andrei1058.bedwars.api.language.Language.Companion.getMsg
import com.andrei1058.bedwars.api.language.Messages
import com.andrei1058.bedwars.arena.Arena
import com.andrei1058.bedwars.configuration.Sounds
import com.andrei1058.bedwars.shop.ShopCache
import com.andrei1058.bedwars.shop.quickbuy.PlayerQuickBuyCache
import com.andrei1058.bedwars.shop.quickbuy.QuickBuyElement
import java.util.ArrayList
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import com.andrei1058.bedwars.api.util.unchecked
import com.andrei1058.bedwars.api.util.uninitialized

open class CategoryContent : ICategoryContent {
    private var slot: Int = 0
    private var loaded: Boolean = false
    private var contentTiers: MutableList<IContentTier> = ArrayList()
    private var contentName: String? = null
    private var itemNamePath: String? = null
    private var itemLorePath: String = uninitialized()
    private var identifier: String? = null
    private var permanent: Boolean = false
    private var downgradable: Boolean = false
    private var unbreakable: Boolean = false
    private var weight: Byte = 0
    private var father: ShopCategory = uninitialized()

    /**
     * Load a new category
     */
    constructor(path: String?, name: String?, categoryName: String?, yml: YamlConfiguration?, father: ShopCategory) {
        BedWars.debug("Loading CategoryContent " + path)
        this.contentName = name
        this.father = father
        if (path == null || name == null || categoryName == null || yml == null) {
            return
        }
        if (yml.get(path + "." + ConfigPath.SHOP_CATEGORY_CONTENT_CONTENT_SLOT) == null) {
            BedWars.plugin.getLogger().severe("Content slot not set at " + path)
            return
        }
        if (yml.get(path + "." + ConfigPath.SHOP_CATEGORY_CONTENT_CONTENT_TIERS) == null) {
            BedWars.plugin.getLogger().severe("No tiers set for " + path)
            return
        }
        if (yml.getConfigurationSection(path + "." + ConfigPath.SHOP_CATEGORY_CONTENT_CONTENT_TIERS).getKeys(false).isEmpty()) {
            BedWars.plugin.getLogger().severe("No tiers set for " + path)
            return
        }
        if (yml.get(path + "." + ConfigPath.SHOP_CATEGORY_CONTENT_CONTENT_TIERS + ".tier1") == null) {
            BedWars.plugin.getLogger().severe("tier1 not found for " + path)
            return
        }
        if (yml.get(path + "." + ConfigPath.SHOP_CATEGORY_CONTENT_IS_PERMANENT) != null) {
            permanent = yml.getBoolean(path + "." + ConfigPath.SHOP_CATEGORY_CONTENT_IS_PERMANENT)
        }
        if (yml.get(path + "." + ConfigPath.SHOP_CATEGORY_CONTENT_IS_DOWNGRADABLE) != null) {
            downgradable = yml.getBoolean(path + "." + ConfigPath.SHOP_CATEGORY_CONTENT_IS_DOWNGRADABLE)
        }
        if (yml.get(path + "." + ConfigPath.SHOP_CATEGORY_CONTENT_IS_UNBREAKABLE) != null) {
            unbreakable = yml.getBoolean(path + "." + ConfigPath.SHOP_CATEGORY_CONTENT_IS_UNBREAKABLE)
        }
        if (yml.get(path + "." + ConfigPath.SHOP_CATEGORY_CONTENT_WEIGHT) != null) {
            weight = yml.getInt(path + "." + ConfigPath.SHOP_CATEGORY_CONTENT_WEIGHT).toByte()
        }
        this.slot = yml.getInt(path + "." + ConfigPath.SHOP_CATEGORY_CONTENT_CONTENT_SLOT)
        var ctt: ContentTier
        for (s in yml.getConfigurationSection(path + "." + ConfigPath.SHOP_CATEGORY_CONTENT_CONTENT_TIERS).getKeys(false)) {
            ctt = ContentTier(path + "." + ConfigPath.SHOP_CATEGORY_CONTENT_CONTENT_TIERS + "." + s, s, path, yml)
            /*if (ctt.isLoaded())*/
            contentTiers.add(ctt)
        }
        itemNamePath = Messages.SHOP_CONTENT_TIER_ITEM_NAME.replace("%category%", categoryName).replace("%content%", contentName.unchecked())
        for (lang in Language.getLanguages()) {
            if (!lang.exists(itemNamePath.unchecked())) {
                lang.set(itemNamePath.unchecked(), "&cName not set")
            }
        }
        itemLorePath = Messages.SHOP_CONTENT_TIER_ITEM_LORE.replace("%category%", categoryName).replace("%content%", contentName.unchecked())
        for (lang in Language.getLanguages()) {
            if (!lang.exists(itemLorePath)) {
                lang.set(itemLorePath, "&cLore not set")
            }
        }
        identifier = path
        loaded = true
    }

    open fun execute(player: Player, shopCache: ShopCache, slot: Int) {
        var ct: IContentTier
        //check weight
        if (shopCache.getCategoryWeight(father) > weight) {
            return
        }
        if (shopCache.getContentTier(getIdentifier().unchecked()) > contentTiers.size) {
            Bukkit.getLogger().severe("Wrong tier order at: " + getIdentifier())
            return
        }
        //check if can re-buy
        if (shopCache.getContentTier(getIdentifier().unchecked()) == contentTiers.size) {
            if (isPermanent() && shopCache.hasCachedItem(this)) {
                player.sendMessage(getMsg(player, Messages.SHOP_ALREADY_BOUGHT))
                Sounds.playSound(ConfigPath.SOUNDS_INSUFF_MONEY, player)
                return
            }
            //current tier
            ct = contentTiers.get(shopCache.getContentTier(getIdentifier().unchecked()) - 1)
        } else {
            if (!shopCache.hasCachedItem(this)) {
                ct = contentTiers.get(0)
            } else {
                ct = contentTiers.get(shopCache.getContentTier(getIdentifier().unchecked()))
            }
        }
        //check money
        val money: Int = calculateMoney(player, ct.getCurrency())
        if (money < ct.getPrice()) {
            player.sendMessage(getMsg(player, Messages.SHOP_INSUFFICIENT_MONEY).replace("{currency}", getMsg(player, getCurrencyMsgPath(ct))).replace("{amount}", (ct.getPrice() - money).toString()))
            Sounds.playSound(ConfigPath.SOUNDS_INSUFF_MONEY, player)
            return
        }
        var event: ShopBuyEvent
        //call shop buy event
        Bukkit.getPluginManager().callEvent(ShopBuyEvent(player, Arena.getArenaByPlayer(player), this).also { event = it })
        if (event.isCancelled()) {
            return
        }
        //take money
        takeMoney(player, ct.getCurrency(), ct.getPrice())
        //upgrade if possible
        shopCache.upgradeCachedItem(this, slot)
        //give items
        giveItems(player, shopCache, Arena.getArenaByPlayer(player))
        //play sound
        Sounds.playSound(ConfigPath.SOUNDS_BOUGHT, player)
        //send purchase msg
        if (itemNamePath == null || Language.getPlayerLanguage(player).getYml().get(itemNamePath) == null) {
            val displayItem = ct.getItemStack()
            if (displayItem!!.getItemMeta() != null && displayItem!!.getItemMeta().hasDisplayName()) {
                player.sendMessage(getMsg(player, Messages.SHOP_NEW_PURCHASE).replace("{item}", displayItem!!.getItemMeta().getDisplayName()))
            }
        } else {
            player.sendMessage(getMsg(player, Messages.SHOP_NEW_PURCHASE).replace("{item}", ChatColor.stripColor(getMsg(player, itemNamePath.unchecked()))).replace("{color}", "").replace("{tier}", ""))
        }
        shopCache.setCategoryWeight(father, weight)
    }

    /**
     * Add tier items to player inventory
     */
    open fun giveItems(player: Player, shopCache: ShopCache, arena: IArena) {
        val tier = contentTiers.get(shopCache.getContentTier(getIdentifier().unchecked()) - 1)
        var shopName: String? = null
        if (itemNamePath != null && Language.getPlayerLanguage(player).getYml().get(itemNamePath) != null) {
            shopName = getMsg(player, itemNamePath).replace("{color}", ChatColor.GREEN.toString()).replace("{tier}", getRomanNumber(tier.getValue())).trim()
        }
        for (bi in tier.getBuyItemsList()) {
            if (bi is BuyItem) {
                (bi as BuyItem).give(player, arena, shopName)
            } else {
                bi.give(player, arena)
        }
    }

    override fun getSlot(): Int {
        return slot
    }

    override fun getItemStack(player: Player): ItemStack? {
        val sc: ShopCache? = ShopCache.getShopCache(player.getUniqueId())
        return if (sc == null) null else getItemStack(player, sc)
    }

    override fun hasQuick(player: Player): Boolean {
        val pqbc: PlayerQuickBuyCache? = PlayerQuickBuyCache.getQuickBuyCache(player.getUniqueId())
        return pqbc != null && hasQuick(pqbc)
    }

    open fun getItemStack(player: Player, shopCache: ShopCache): ItemStack {
        var ct: IContentTier
        if (shopCache.getContentTier(identifier.unchecked()) == contentTiers.size) {
            ct = contentTiers.get(contentTiers.size - 1)
        } else {
            if (shopCache.hasCachedItem(this)) {
                ct = contentTiers.get(shopCache.getContentTier(identifier.unchecked()))
            } else {
                ct = contentTiers.get(shopCache.getContentTier(identifier.unchecked()) - 1)
            }
        }
        val i = ct.getItemStack()
        var im: ItemMeta? = i!!.getItemMeta()
        if (im != null) {
            im = i!!.getItemMeta().clone()
            val canAfford: Boolean = calculateMoney(player, ct.getCurrency()) >= ct.getPrice()
            val qbc: PlayerQuickBuyCache? = PlayerQuickBuyCache.getQuickBuyCache(player.getUniqueId())
            val hasQuick: Boolean = qbc != null && hasQuick(qbc)
            val color = getMsg(player, if (canAfford) Messages.SHOP_CAN_BUY_COLOR else Messages.SHOP_CANT_BUY_COLOR)
            val translatedCurrency = getMsg(player, getCurrencyMsgPath(ct))
            val cColor = getCurrencyColor(ct.getCurrency())
            val tierI: Int = ct.getValue()
            val tier = getRomanNumber(tierI)
            var buyStatus: String
            if (isPermanent() && shopCache.hasCachedItem(this) && shopCache.getCachedItem(this)!!.getTier() == getContentTiers().size) {
                if (!(nms.isArmor(i.unchecked()))) {
                    //ARMOR
                    buyStatus = getMsg(player, Messages.SHOP_LORE_STATUS_MAXED)
                } else {
                    buyStatus = getMsg(player, Messages.SHOP_LORE_STATUS_ARMOR)
                }
            } else if (!canAfford) {
                buyStatus = getMsg(player, Messages.SHOP_LORE_STATUS_CANT_AFFORD).replace("{currency}", translatedCurrency)
            } else {
                buyStatus = getMsg(player, Messages.SHOP_LORE_STATUS_CAN_BUY)
            }
            im.setDisplayName(getMsg(player, itemNamePath.unchecked()).replace("{color}", color).replace("{tier}", tier))
            val lore: MutableList<String> = ArrayList()
            for (s0 in Language.getList(player, itemLorePath)) {
                var s = s0
                if (s.contains("{quick_buy}")) {
                    if (hasQuick) {
                        if (ShopIndex.getIndexViewers().contains(player.getUniqueId())) {
                            s = getMsg(player, Messages.SHOP_LORE_QUICK_REMOVE)
                        } else {
                            continue
                        }
                    } else {
                        s = getMsg(player, Messages.SHOP_LORE_QUICK_ADD)
                    }
                }
                s = s.replace("{tier}", tier).replace("{color}", color).replace("{cost}", cColor.toString() + ct.getPrice().toString()).replace("{currency}", cColor.toString() + translatedCurrency).replace("{buy_status}", buyStatus)
                lore.add(s)
            }
            im.setLore(lore)
            i!!.setItemMeta(im)
        }
        return i.unchecked()
    }

    open fun hasQuick(c: PlayerQuickBuyCache): Boolean {
        for (q in c.getElements()) {
            if (q.getCategoryContent() === this) {
                return true
            }
        }
        return false
    }

    open fun setLoaded(loaded: Boolean) {
        this.loaded = loaded
    }

    /**
     * Check if category content was loaded
     */
    open fun isLoaded(): Boolean {
        return loaded
    }

    override fun isPermanent(): Boolean {
        return permanent
    }

    override fun isDowngradable(): Boolean {
        return downgradable
    }

    override fun getIdentifier(): String? {
        return identifier
    }

    override fun getContentTiers(): MutableList<IContentTier> {
        return contentTiers
    }

    companion object {
        /**
         * Get player's money amount
         */
        @JvmStatic
        fun calculateMoney(player: Player, currency: Material): Int {
            if (currency == Material.AIR) {
                return BedWars.getEconomy().getMoney(player).toInt()
            }
            var amount: Int = 0
            for (`is` in player.getInventory().getContents()) {
                if (`is` == null) {
                    continue
                }
                if (`is`.getType() == currency) {
                    amount += `is`.getAmount()
                }
            }
            return amount
        }

        /**
         * Get currency as material
         */
        @JvmStatic
        fun getCurrency(currency: String): Material {
            var material: Material
            when (currency) {
                "gold" -> {
                    material = Material.GOLD_INGOT
                }
                "diamond" -> {
                    material = Material.DIAMOND
                }
                "emerald" -> {
                    material = Material.EMERALD
                }
                "vault" -> {
                    material = Material.AIR
                }
                else -> {
                    material = Material.IRON_INGOT
                }
            }
            return material
        }

        @JvmStatic
        fun getCurrencyColor(currency: Material): ChatColor {
            var c = ChatColor.DARK_GREEN
            if (currency.toString().lowercase().contains("diamond")) {
                c = ChatColor.AQUA
            } else if (currency.toString().lowercase().contains("gold")) {
                c = ChatColor.GOLD
            } else if (currency.toString().lowercase().contains("iron")) {
                c = ChatColor.WHITE
            }
            return c
        }

        /**
         * Cet currency path
         */
        @JvmStatic
        fun getCurrencyMsgPath(contentTier: IContentTier): String {
            var c: String
            if (contentTier.getCurrency().toString().lowercase().contains("iron")) {
                c = if (contentTier.getPrice() == 1) Messages.MEANING_IRON_SINGULAR else Messages.MEANING_IRON_PLURAL
            } else if (contentTier.getCurrency().toString().lowercase().contains("gold")) {
                c = if (contentTier.getPrice() == 1) Messages.MEANING_GOLD_SINGULAR else Messages.MEANING_GOLD_PLURAL
            } else if (contentTier.getCurrency().toString().lowercase().contains("emerald")) {
                c = if (contentTier.getPrice() == 1) Messages.MEANING_EMERALD_SINGULAR else Messages.MEANING_EMERALD_PLURAL
            } else if (contentTier.getCurrency().toString().lowercase().contains("diamond")) {
                c = if (contentTier.getPrice() == 1) Messages.MEANING_DIAMOND_SINGULAR else Messages.MEANING_DIAMOND_PLURAL
            } else {
                c = if (contentTier.getPrice() == 1) Messages.MEANING_VAULT_SINGULAR else Messages.MEANING_VAULT_PLURAL
            }
            return c
        }

        /**
         * Get the roman number for an integer
         */
        @JvmStatic
        fun getRomanNumber(n: Int): String {
            var s: String
            when (n) {
                1 -> {
                    s = "I"
                }
                2 -> {
                    s = "II"
                }
                3 -> {
                    s = "III"
                }
                4 -> {
                    s = "IV"
                }
                5 -> {
                    s = "V"
                }
                6 -> {
                    s = "VI"
                }
                7 -> {
                    s = "VII"
                }
                8 -> {
                    s = "VIII"
                }
                9 -> {
                    s = "IX"
                }
                10 -> {
                    s = "X"
                }
                else -> {
                    s = n.toString()
                }
            }
            return s
        }

        /**
         * Take money from player on buy
         */
        @JvmStatic
        fun takeMoney(player: Player, currency: Material, amount: Int) {
            if (currency == Material.AIR) {
                if (!BedWars.getEconomy().isEconomy()) {
                    player.sendMessage("§4§lERROR: This requires Vault Support! Please install Vault plugin!")
                    return
                }
                BedWars.getEconomy().buyAction(player, amount.toDouble())
                return
            }
            var cost: Int = amount
            for (i in player.getInventory().getContents()) {
                if (i == null) {
                    continue
                }
                if (i.getType() == currency) {
                    if (i.getAmount() < cost) {
                        cost -= i.getAmount()
                        nms.minusAmount(player, i, i.getAmount())
                        player.updateInventory()
                    } else {
                        nms.minusAmount(player, i, cost)
                        player.updateInventory()
                        break
                    }
                }
            }
        }
    }
}
