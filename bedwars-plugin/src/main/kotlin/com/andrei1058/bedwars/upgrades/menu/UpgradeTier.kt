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

package com.andrei1058.bedwars.upgrades.menu

import com.andrei1058.bedwars.BedWars
import com.andrei1058.bedwars.api.language.Language
import com.andrei1058.bedwars.api.language.Messages
import com.andrei1058.bedwars.api.upgrades.UpgradeAction
import com.andrei1058.bedwars.upgrades.UpgradesManager
import com.andrei1058.bedwars.upgrades.upgradeaction.*
import java.util.ArrayList
import java.util.Collections
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffectType
import com.andrei1058.bedwars.api.util.unchecked

open class UpgradeTier(parentName: String, name: String, displayItem: ItemStack, cost: Int, currency: Material) {
    private var displayItem: ItemStack
    private var name: String
    private var upgradeActions: MutableList<UpgradeAction> = ArrayList()
    private var cost: Int
    private var currency: Material

    open fun getDisplayItem(): ItemStack {
        return displayItem
    }

    open fun getName(): String {
        return name
    }

    open fun getCost(): Int {
        return cost
    }

    open fun getCurrency(): Material {
        return currency
    }

    open fun getUpgradeActions(): MutableList<UpgradeAction> {
        return upgradeActions
    }

    init {
        this.displayItem = BedWars.nms.addCustomData(displayItem, "MCONT_" + parentName)
        this.name = name
        Language.saveIfNotExists(Messages.UPGRADES_UPGRADE_TIER_ITEM_NAME.replace("{name}", parentName.replace("upgrade-", "")).replace("{tier}", name), "&cName not set")
        Language.saveIfNotExists(Messages.UPGRADES_UPGRADE_TIER_ITEM_LORE.replace("{name}", parentName.replace("upgrade-", "")).replace("{tier}", name), Collections.singletonList("&cLore not set"))
        this.cost = cost
        this.currency = currency
        for (action in UpgradesManager.getConfiguration().getYml().getStringList(parentName + "." + name + ".receive")) {
            val type = action.trim().split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (type.size < 2) {
                continue
            }
            var data = type[1].trim().lowercase().split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            var ua: UpgradeAction? = null
            when (type[0].trim().lowercase()) {
                "enchant-item" -> {
                    if (data.size < 3) {
                        BedWars.plugin.getLogger().warning("Invalid " + type[0] + " at upgrades2: " + parentName + "." + name)
                        continue
                    }
                    val e: Enchantment? = Enchantment.getByName(data[0].uppercase())
                    if (e == null) {
                        BedWars.plugin.getLogger().warning("Invalid enchantment " + data[0].uppercase() + " at upgrades2: " + parentName + "." + name)
                        continue
                    }
                    var apply: EnchantItemAction.ApplyType? = null
                    when (data[2].lowercase()) {
                        "sword" -> {
                            apply = EnchantItemAction.ApplyType.SWORD
                        }
                        "armor" -> {
                            apply = EnchantItemAction.ApplyType.ARMOR
                        }
                        "bow" -> {
                            apply = EnchantItemAction.ApplyType.BOW
                        }
                        else -> {}
                    }
                    if (apply == null) {
                        BedWars.plugin.getLogger().warning("Invalid apply type " + data[2] + " at upgrades2: " + parentName + "." + name)
                        continue
                    }
                    var amplifier: Int = 1
                    try {
                        amplifier = Integer.parseInt(data[1])
                    } catch (ignored: Exception) {
                    }
                    ua = EnchantItemAction(e, amplifier, apply)
                }
                "player-effect" -> {
                    if (data.size < 4) {
                        BedWars.plugin.getLogger().warning("Invalid " + type[0] + " at upgrades2: " + parentName + "." + name)
                        continue
                    }
                    val pe: PotionEffectType? = PotionEffectType.getByName(data[0].uppercase())
                    if (pe == null) {
                        BedWars.plugin.getLogger().warning("Invalid potion effect " + data[0] + " at upgrades2: " + parentName + "." + name)
                        continue
                    }
                    var applyType: PlayerEffectAction.ApplyType? = null
                    when (data[3].lowercase()) {
                        "team" -> {
                            applyType = PlayerEffectAction.ApplyType.TEAM
                        }
                        "base" -> {
                            applyType = PlayerEffectAction.ApplyType.BASE
                        }
                        else -> {}
                    }
                    if (applyType == null) {
                        BedWars.plugin.getLogger().warning("Invalid apply type " + data[3] + " at upgrades2: " + parentName + "." + name)
                        continue
                    }
                    var amp: Int = 1
                    var time: Int = 0
                    try {
                        amp = Integer.parseInt(data[1])
                        time = Integer.parseInt(data[2])
                    } catch (ignored: Exception) {
                    }
                    ua = PlayerEffectAction(pe, amp, time, applyType)
                }
                "generator-edit" -> {
                    if (data.size < 4) {
                        BedWars.plugin.getLogger().warning("Invalid " + type[0] + " at upgrades2: " + parentName + "." + name)
                        continue
                    }
                    var genType: GeneratorEditAction.ApplyType? = null
                    when (data[0].lowercase()) {
                        "gold", "g" -> {
                            genType = GeneratorEditAction.ApplyType.GOLD
                        }
                        "iron", "i" -> {
                            genType = GeneratorEditAction.ApplyType.IRON
                        }
                        "emerald", "e" -> {
                            genType = GeneratorEditAction.ApplyType.EMERALD
                        }
                        else -> {}
                    }
                    if (genType == null) {
                        BedWars.plugin.getLogger().warning("Invalid generator type " + data[0] + " at upgrades2: " + parentName + "." + name)
                    }
                    var spawn: Int
                    var amount: Int
                    var limit: Int
                    try {
                        spawn = Integer.parseInt(data[1])
                        amount = Integer.parseInt(data[2])
                        limit = Integer.parseInt(data[3])
                    } catch (ex: Exception) {
                        BedWars.plugin.getLogger().warning("Invalid generator configuration " + data[0] + " at upgrades2: " + parentName + "." + name)
                        continue
                    }
                    ua = GeneratorEditAction(genType.unchecked(), amount, spawn, limit)
                }
                "dragon" -> {
                    if (data.size < 1) {
                        BedWars.plugin.getLogger().warning("Invalid " + type[0] + " at upgrades2: " + parentName + "." + name)
                        continue
                    }
                    var dragons: Int
                    try {
                        dragons = Integer.parseInt(data[0])
                    } catch (exc: Exception) {
                        BedWars.plugin.getLogger().warning("Invalid dragon amount at upgrades2: " + parentName + "." + name)
                        continue
                    }
                    ua = DragonAction(dragons)
                }
                "command" -> {
                    // once-as-console,command
                    if (data.size < 2) {
                        BedWars.plugin.getLogger().warning("Invalid " + type[0] + " at upgrades2: " + parentName + "." + name)
                        continue
                    }
                    var cmdType: DispatchCommand.CommandType
                    try {
                        cmdType = DispatchCommand.CommandType.valueOf(data[0].uppercase())
                    } catch (exception: Exception) {
                        BedWars.plugin.getLogger().warning("Invalid command type " + data[0] + " at upgrades2: " + parentName + "." + name)
                        continue
                    }
                    // we need data with spaces
                    data = type[1].split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    ua = DispatchCommand(cmdType, data[1])
                }
                else -> {}
            }
            if (ua != null) {
                upgradeActions.add(ua)
            }
        }
    }
}
