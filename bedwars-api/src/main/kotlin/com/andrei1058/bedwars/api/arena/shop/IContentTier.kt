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

package com.andrei1058.bedwars.api.arena.shop

import org.bukkit.Material
import org.bukkit.inventory.ItemStack

interface IContentTier {
    /**
     * Get tier price
     */
    fun getPrice(): Int

    /**
     * Get tier currency.
     * {@link Material#AIR} for vault.
     */
    fun getCurrency(): Material

    /**
     * Set tier currency.
     * {@link Material#AIR} for vault.
     */
    fun setCurrency(currency: Material)

    /**
     * Set tier price.
     */
    fun setPrice(price: Int)

    /**
     * Set tier preview item.
     */
    fun setItemStack(itemStack: ItemStack)

    /**
     * Set list of items that you receive on buy.
     */
    fun setBuyItemsList(buyItemsList: MutableList<IBuyItem>)

    /**
     * Get item stack with name and lore in player's language
     */
    fun getItemStack(): ItemStack?

    /**
     * Get tier level
     */
    fun getValue(): Int

    /**
     * Get items
     */
    fun getBuyItemsList(): MutableList<IBuyItem>
}
