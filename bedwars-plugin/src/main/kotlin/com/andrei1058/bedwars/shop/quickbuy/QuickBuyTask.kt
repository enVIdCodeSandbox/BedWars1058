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

package com.andrei1058.bedwars.shop.quickbuy

import com.andrei1058.bedwars.BedWars
import com.andrei1058.bedwars.api.configuration.ConfigPath
import com.andrei1058.bedwars.shop.ShopManager
import com.andrei1058.bedwars.shop.main.CategoryContent
import com.andrei1058.bedwars.shop.main.ShopCategory
import java.util.HashMap
import java.util.UUID
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable

open class QuickBuyTask(private var uuid: UUID) : BukkitRunnable() {
    override fun run() {
        if (Bukkit.getPlayer(uuid) == null) {
            cancel()
            return
        }
        if (Bukkit.getPlayer(uuid).isOnline()) {
            val cache: PlayerQuickBuyCache? = PlayerQuickBuyCache.getQuickBuyCache(uuid)
            if (cache == null) {
                cancel()
                return
            }
            if (!BedWars.getRemoteDatabase().hasQuickBuy(uuid)) {
                if (BedWars.shop.getYml().get(ConfigPath.SHOP_QUICK_DEFAULTS_PATH) != null) {
                    for (s in BedWars.shop.getYml().getConfigurationSection(ConfigPath.SHOP_QUICK_DEFAULTS_PATH).getKeys(false)) {
                        if (BedWars.shop.getYml().get(ConfigPath.SHOP_QUICK_DEFAULTS_PATH + "." + s + ".path") != null) {
                            if (BedWars.shop.getYml().get(ConfigPath.SHOP_QUICK_DEFAULTS_PATH + "." + s + ".slot") == null) {
                                continue
                            }
                            try {
                                Integer.valueOf(BedWars.shop.getYml().getString(ConfigPath.SHOP_QUICK_DEFAULTS_PATH + "." + s + ".slot"))
                            } catch (ex: Exception) {
                                BedWars.debug(BedWars.shop.getYml().getString(ConfigPath.SHOP_QUICK_DEFAULTS_PATH + "." + s + ".slot") + " must be an integer!")
                                continue
                            }
                            for (sc in ShopManager.getShop().getCategoryList()) {
                                for (cc in sc.getCategoryContentList()) {
                                    if (cc.getIdentifier() == BedWars.shop.getYml().getString(ConfigPath.SHOP_QUICK_DEFAULTS_PATH + "." + s + ".path")) {
                                        cache.setElement(Integer.parseInt(BedWars.shop.getYml().getString(ConfigPath.SHOP_QUICK_DEFAULTS_PATH + "." + s + ".slot")), cc)
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // slot, identifier
                val items: HashMap<Int, String>? = BedWars.getRemoteDatabase().getQuickBuySlots(uuid, PlayerQuickBuyCache.quickSlots)
                if (items == null) {
                    return
                }
                if (items.isEmpty()) {
                    return
                }
                for (entry in items.entries) {
                    if (entry.value.isEmpty()) {
                        continue
                    }
                    if (entry.value == " ") {
                        continue
                    }
                    val e = QuickBuyElement(entry.value, entry.key)
                    if (e.isLoaded()) {
                        cache.addQuickElement(e)
                    }
                }
            }
        }
    }

    @Throws(IllegalStateException::class)
    @Synchronized
    override fun cancel() {
        super.cancel()
    }

    init {
        this.runTaskLaterAsynchronously(BedWars.plugin, 20 * 7)
    }
}
