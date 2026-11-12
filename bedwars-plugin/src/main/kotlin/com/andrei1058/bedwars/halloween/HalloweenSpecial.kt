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

package com.andrei1058.bedwars.halloween

import com.andrei1058.bedwars.BedWars
import com.andrei1058.bedwars.api.configuration.ConfigPath
import com.andrei1058.bedwars.halloween.shop.PumpkinContent
import com.andrei1058.bedwars.metrics.MetricsManager
import com.andrei1058.bedwars.shop.ShopManager
import com.andrei1058.bedwars.shop.main.ShopCategory
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import org.bukkit.Bukkit
import org.bukkit.ChatColor

open class HalloweenSpecial private constructor() {
    init {
        BedWars.plugin.getLogger().info(ChatColor.AQUA.toString() + "Loaded Halloween Special <3")
        // pumpkin hats
        Bukkit.getPluginManager().registerEvents(HalloweenListener(), BedWars.plugin)
        // pumpkin in shop
        val blockCategory: ShopCategory? = ShopManager.getShop().getCategoryList().stream().filter({ category -> category.getName() == "blocks-category" }).findFirst().orElse(null)
        if (blockCategory != null) {
            val content = PumpkinContent(blockCategory)
            if (content.isLoaded()) {
                blockCategory.getCategoryContentList().add(content)
            }
        }
    }

    companion object {
        private var INSTANCE: HalloweenSpecial? = null

        /**
         * Initialize Halloween Special.
         */
        @JvmStatic
        fun init() {
            val enable = BedWars.config.getBoolean(ConfigPath.GENERAL_CONFIGURATION_ENABLE_HALLOWEEN)
            if (enable) {
                if (INSTANCE == null && checkAvailabilityDate()) {
                    //noinspection InstantiationOfUtilityClass
                    INSTANCE = HalloweenSpecial()
                }
            }
            MetricsManager.appendPie("halloween_special_enable", { enable.toString() })
        }

        @JvmStatic
        fun checkAvailabilityDate(): Boolean {
            // check date
            val zone = ZoneId.of("Europe/Rome")
            val date = Date()
            val localDate = date.toInstant().atZone(zone).toLocalDate()
            val month: Int = localDate.getMonthValue()
            val day: Int = localDate.getDayOfMonth()
            // allowed between October 21 and November 1
            return (month == 10 && day > 21 || month == 11 && day < 2)
        }

        @JvmStatic
        fun getINSTANCE(): HalloweenSpecial? {
            return INSTANCE
        }
    }
}
