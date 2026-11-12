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

package com.andrei1058.bedwars.database

import com.andrei1058.bedwars.shop.quickbuy.QuickBuyElement
import com.andrei1058.bedwars.stats.PlayerStats
import java.util.HashMap
import java.util.UUID

interface Database {
    /**
     * Initialize database.
     */
    fun init()

    /**
     * Check if player has remote stats.
     */
    fun hasStats(uuid: UUID): Boolean

    /**
     * Create or replace stats for a player.
     */
    fun saveStats(stats: PlayerStats)

    fun fetchStats(uuid: UUID): PlayerStats

    /**
     * Set quick buy slot value.
     */
    @Deprecated("")
    fun setQuickBuySlot(uuid: UUID, shopPath: String, slot: Int)

    /**
     * Get quick buy slot value.
     */
    fun getQuickBuySlots(uuid: UUID, slot: Int): String

    /**
     * Get quick buy.
     * slot - identifier string
     */
    fun getQuickBuySlots(uuid: UUID, slot: IntArray): HashMap<Int, String>

    /**
     * Check if has quick buy.
     */
    fun hasQuickBuy(player: UUID): Boolean

    /**
     * Get a stats value for the given player.
     */
    fun getColumn(player: UUID, column: String): Int

    /**
     * Get a player level and xp.
     * <p>
     * args 0 is level.
     * args 1 is xp.
     * args 2 is display name.
     * args 3 next level cost.
     */
    fun getLevelData(player: UUID): Array<Any>

    /**
     * Set a player level data.
     */
    fun setLevelData(player: UUID, level: Int, xp: Int, displayName: String?, nextCost: Int)

    /**
     * Set a player language.
     */
    fun setLanguage(player: UUID, iso: String)

    /**
     * Get a player language.
     */
    fun getLanguage(player: UUID): String

    /**
     * @param updateSlots key is slot id and value is the element.
     */
    fun pushQuickBuyChanges(updateSlots: HashMap<Int, String>, uuid: UUID, elementList: MutableList<QuickBuyElement>)
}
