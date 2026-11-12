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
import com.andrei1058.bedwars.api.arena.team.ITeam
import com.andrei1058.bedwars.api.configuration.ConfigPath
import com.andrei1058.bedwars.api.events.upgrades.UpgradeBuyEvent
import com.andrei1058.bedwars.api.language.Language
import com.andrei1058.bedwars.api.language.Messages
import com.andrei1058.bedwars.api.upgrades.MenuContent
import com.andrei1058.bedwars.api.upgrades.TeamUpgrade
import com.andrei1058.bedwars.api.upgrades.UpgradeAction
import com.andrei1058.bedwars.arena.Arena
import com.andrei1058.bedwars.configuration.Sounds
import com.andrei1058.bedwars.upgrades.UpgradesManager
import com.google.common.collect.ImmutableMap
import java.util.*
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import com.andrei1058.bedwars.api.util.unchecked

open class MenuUpgrade(private var name: String) : MenuContent, TeamUpgrade {
    private var tiers: MutableList<UpgradeTier> = LinkedList()

    override fun getDisplayItem(player: Player, team: ITeam): ItemStack {
        if (tiers.isEmpty()) {
            return ItemStack(Material.BEDROCK)
        }
        var tier: Int = -1
        if (team.getTeamUpgradeTiers().containsKey(getName())) {
            tier = team.getTeamUpgradeTiers().get(getName()).unchecked()
        }
        val highest: Boolean = getTiers().size == tier + 1 && team.getTeamUpgradeTiers().containsKey(getName())
        if (!highest) {
            tier += 1
        }
        val ut = getTiers().get(tier)
        val afford: Boolean = UpgradesManager.getMoney(player, ut.getCurrency()) >= ut.getCost()
        val i = ItemStack(tiers.get(tier).getDisplayItem())
        val im: ItemMeta? = i.getItemMeta()
        if (im == null) {
            return i
        }
        var color: String
        if (!highest) {
            if (afford) {
                color = Language.getMsg(player, Messages.FORMAT_UPGRADE_COLOR_CAN_AFFORD)
            } else {
                color = Language.getMsg(player, Messages.FORMAT_UPGRADE_COLOR_CANT_AFFORD)
            }
        } else {
            color = Language.getMsg(player, Messages.FORMAT_UPGRADE_COLOR_UNLOCKED)
        }
        im.setDisplayName(Language.getMsg(player, Messages.UPGRADES_UPGRADE_TIER_ITEM_NAME.replace("{name}", this.getName()!!.replace("upgrade-", "")).replace("{tier}", ut.getName())).replace("{color}", color))
        val lore: MutableList<String> = ArrayList()
        val currencyMsg = UpgradesManager.getCurrencyMsg(player, ut)
        for (s in Language.getList(player, Messages.UPGRADES_UPGRADE_TIER_ITEM_LORE.replace("{name}", this.getName()!!.replace("upgrade-", "")))) {
            if (s.contains("{tier_")) {
                // Get tier number from placeholder
                val result = s.replace(".*_([0-9]+)_.*".toRegex(), "\$1")
                var tierColor = Messages.FORMAT_UPGRADE_TIER_LOCKED
                if (Integer.valueOf(result) - 1 <= team.getTeamUpgradeTiers().getOrDefault(getName(), -1)) {
                    tierColor = Messages.FORMAT_UPGRADE_TIER_UNLOCKED
                }
                //get current tier. Note: placeholder number doesnt match array index.
                val upgradeTier = tiers.get(Integer.valueOf(result) - 1)
                lore.add(s.replace("{tier_" + result + "_cost}", upgradeTier.getCost().toString()).replace("{tier_" + result + "_currency}", currencyMsg).replace("{tier_" + result + "_color}", Language.getMsg(player, tierColor)))
            } else {
                lore.add(s.replace("{color}", color))
            }
        }
        if (highest) {
            lore.add(Language.getMsg(player, Messages.UPGRADES_LORE_REPLACEMENT_UNLOCKED).replace("{color}", color))
        } else if (afford) {
            lore.add(Language.getMsg(player, Messages.UPGRADES_LORE_REPLACEMENT_CLICK_TO_BUY).replace("{color}", color))
        } else {
            lore.add(Language.getMsg(player, Messages.UPGRADES_LORE_REPLACEMENT_INSUFFICIENT_MONEY).replace("{currency}", currencyMsg).replace("{color}", color))
        }
        im.setLore(lore)
        im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
        i.setItemMeta(im)
        return i
    }

    override fun onClick(player: Player, clickType: ClickType, team: ITeam?) {
        var tier: Int = -1
        if (team!!.getTeamUpgradeTiers().containsKey(getName())) {
            tier = team!!.getTeamUpgradeTiers().get(getName()).unchecked()
        }
        var ut: UpgradeTier
        if (getTiers().size - 1 > tier) {
            ut = getTiers().get(tier + 1)
            val money: Int = UpgradesManager.getMoney(player, ut.getCurrency())
            if (money < ut.getCost()) {
                Sounds.playSound(ConfigPath.SOUNDS_INSUFF_MONEY, player)
                player.sendMessage(Language.getMsg(player, Messages.SHOP_INSUFFICIENT_MONEY).replace("{currency}", UpgradesManager.getCurrencyMsg(player, ut)).replace("{amount}", (ut.getCost() - money).toString()))
                player.closeInventory()
                return
            }
            val event: UpgradeBuyEvent
            Bukkit.getPluginManager().callEvent(UpgradeBuyEvent(this, player, team.unchecked()).also { event = it })
            if (event.isCancelled()) {
                return
            }
            if (ut.getCurrency() == Material.AIR) {
                BedWars.getEconomy().buyAction(player, ut.getCost().toDouble())
            } else {
                BedWars.getAPI().getShopUtil().takeMoney(player, ut.getCurrency(), ut.getCost())
            }
            if (team!!.getTeamUpgradeTiers().containsKey(getName())) {
                team!!.getTeamUpgradeTiers().replace(getName().unchecked(), team!!.getTeamUpgradeTiers().get(getName())!! + 1)
            } else {
                team!!.getTeamUpgradeTiers().put(getName().unchecked(), 0)
            }
            Sounds.playSound(ConfigPath.SOUNDS_BOUGHT, player)
            for (a in ut.getUpgradeActions()) {
                a.onBuy(player, team.unchecked())
            }
            for (p1 in team!!.getMembers()!!) {
                p1.sendMessage(Language.getMsg(p1, Messages.UPGRADES_UPGRADE_BOUGHT_CHAT).replace("{playername}", player.getName()).replace("{player}", player.getDisplayName()).replace("{upgradeName}", ChatColor.stripColor(Language.getMsg(p1, Messages.UPGRADES_UPGRADE_TIER_ITEM_NAME.replace("{name}", getName()!!.replace("upgrade-", "")).replace("{tier}", ut.getName())))).replace("{color}", ""))
            }
            val menuContentBySlot = UpgradesManager.getMenuForArena(Arena.getArenaByPlayer(player)).getMenuContentBySlot()
            val inv = BedWars.nms.getTopInventory(player)
            for (entry in menuContentBySlot.entries) {
                inv!!.setItem(entry.key, entry.value.getDisplayItem(player, team.unchecked()))
            }
        }
    }

    /**
     * Load a upgrade element tiers.
     *
     * @param upgradeTier tier.
     * @return false if something went wrong.
     */
    open fun addTier(upgradeTier: UpgradeTier): Boolean {
        for (ut in tiers) {
            if (ut.getName().equals(upgradeTier.getName(), ignoreCase = true)) {
                return false
            }
        }
        tiers.add(upgradeTier)
        return true
    }

    override fun getName(): String? {
        return name
    }

    override fun getTierCount(): Int {
        return tiers.size
    }

    /**
     * @return tiers list.
     */
    open fun getTiers(): MutableList<UpgradeTier> {
        return Collections.unmodifiableList(tiers)
    }
}
