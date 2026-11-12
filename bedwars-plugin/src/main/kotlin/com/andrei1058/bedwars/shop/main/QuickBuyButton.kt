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

import com.andrei1058.bedwars.api.language.Language
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

open class QuickBuyButton(private var slot: Int, private var itemStack: ItemStack, private var namePath: String, private var lorePath: String) {
    /**
     * Get the quick buy button in the player's language
     */
    open fun getItemStack(player: Player): ItemStack {
        val i = itemStack.clone()
        val im: ItemMeta? = i.getItemMeta()
        if (im != null) {
            im.setDisplayName(Language.getMsg(player, namePath))
            im.setLore(Language.getList(player, lorePath))
            i.setItemMeta(im)
        }
        return i
    }

    /**
     * Get quick buy item slot
     */
    open fun getSlot(): Int {
        return slot
    }
}
