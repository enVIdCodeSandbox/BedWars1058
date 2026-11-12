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

package com.andrei1058.bedwars.halloween.shop

import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.api.arena.shop.IBuyItem
import com.andrei1058.bedwars.api.arena.shop.IContentTier
import com.andrei1058.bedwars.api.language.Language.Companion.getMsg
import com.andrei1058.bedwars.api.language.Messages
import com.andrei1058.bedwars.shop.ShopCache
import com.andrei1058.bedwars.shop.main.CategoryContent
import com.andrei1058.bedwars.shop.main.ShopCategory
import java.util.Arrays
import java.util.Collections
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import com.andrei1058.bedwars.api.util.unchecked

open class PumpkinContent : CategoryContent {
    private var slot: Int = 0

    constructor(father: ShopCategory) : super(null, null, null, null, father) {
        var foundSlot: Int = -1
        for (i in 19 until 26) {
            val finalI: Int = i
            if (father.getCategoryContentList().stream().noneMatch({ categoryContent -> categoryContent.getSlot() == finalI })) {
                foundSlot = i
                break
            }
        }
        if (foundSlot == -1) {
            for (i in 28 until 35) {
                val finalI: Int = i
                if (father.getCategoryContentList().stream().noneMatch({ categoryContent -> categoryContent.getSlot() == finalI })) {
                    foundSlot = i
                    break
                }
            }
        }
        if (foundSlot == -1) {
            for (i in 37 until 44) {
                val finalI: Int = i
                if (father.getCategoryContentList().stream().noneMatch({ categoryContent -> categoryContent.getSlot() == finalI })) {
                    foundSlot = i
                    break
                }
            }
        }
        this.slot = foundSlot
        setLoaded(slot != -1)
        if (!isLoaded()) {
            return
        }
        val pumpkinTier = OneTier()
        getContentTiers().add(pumpkinTier)
    }

    override fun getIdentifier(): String? {
        return "halloween-special-pumpkin"
    }

    override fun getSlot(): Int {
        return slot
    }

    override fun isPermanent(): Boolean {
        return false
    }

    override fun getItemStack(player: Player): ItemStack? {
        val tier = getContentTiers().get(0)
        val pumpkin = tier.getItemStack()
        val canAfford: Boolean = calculateMoney(player, tier.getCurrency()) >= tier.getPrice()
        val translatedCurrency = getMsg(player, getCurrencyMsgPath(tier))
        var buyStatus: String
        if (!canAfford) {
            buyStatus = getMsg(player, Messages.SHOP_LORE_STATUS_CANT_AFFORD).replace("{currency}", translatedCurrency)
        } else {
            buyStatus = getMsg(player, Messages.SHOP_LORE_STATUS_CAN_BUY)
        }
        val cColor = getCurrencyColor(tier.getCurrency())
        pumpkin!!.setAmount(12)
        val itemMeta = pumpkin!!.getItemMeta()
        itemMeta.setDisplayName(ChatColor.GOLD.toString() + "" + ChatColor.BOLD + "Happy Halloween!")
        itemMeta.setLore(Arrays.asList("", cColor.toString() + tier.getPrice().toString() + " " + cColor + translatedCurrency, " ", buyStatus))
        pumpkin!!.setItemMeta(itemMeta)
        return pumpkin
    }

    override fun getItemStack(player: Player, shopCache: ShopCache): ItemStack {
        return getItemStack(player).unchecked()
    }

    open class OneTier : IContentTier {
        override fun getPrice(): Int {
            return 4
        }

        override fun getCurrency(): Material {
            return Material.IRON_INGOT
        }

        override fun setCurrency(currency: Material) {
        }

        override fun setPrice(price: Int) {
        }

        override fun setItemStack(itemStack: ItemStack) {
        }

        override fun setBuyItemsList(buyItemsList: MutableList<IBuyItem>) {
        }

        override fun getItemStack(): ItemStack? {
            return ItemStack(Material.PUMPKIN, 12)
        }

        override fun getValue(): Int {
            return 4
        }

        override fun getBuyItemsList(): MutableList<IBuyItem> {
            return Collections.singletonList(FinalItem())
        }
    }

    open class FinalItem : IBuyItem {
        override fun isLoaded(): Boolean {
            return true
        }

        override fun give(player: Player, arena: IArena) {
            player.getInventory().addItem(ItemStack(Material.PUMPKIN, 12))
        }

        override fun getUpgradeIdentifier(): String? {
            return null
        }

        override fun getItemStack(): ItemStack? {
            return null
        }

        override fun setItemStack(itemStack: ItemStack) {
        }

        override fun isAutoEquip(): Boolean {
            return false
        }

        override fun setAutoEquip(autoEquip: Boolean) {
        }

        override fun isPermanent(): Boolean {
            return false
        }

        override fun setPermanent(permanent: Boolean) {
        }

        override fun isUnbreakable(): Boolean {
            return false
        }

        override fun setUnbreakable(unbreakable: Boolean) {
        }
    }
}
