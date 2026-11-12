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
import com.andrei1058.bedwars.api.upgrades.EnemyBaseEnterTrap
import com.andrei1058.bedwars.api.upgrades.MenuContent
import com.andrei1058.bedwars.api.upgrades.TeamUpgrade
import com.andrei1058.bedwars.api.upgrades.TrapAction
import com.andrei1058.bedwars.configuration.Sounds
import com.andrei1058.bedwars.upgrades.UpgradesManager
import com.andrei1058.bedwars.upgrades.trapaction.DisenchantAction
import com.andrei1058.bedwars.upgrades.trapaction.PlayerEffectAction
import com.andrei1058.bedwars.upgrades.trapaction.RemoveEffectAction
import java.util.ArrayList
import java.util.Collections
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.potion.PotionEffectType
import com.andrei1058.bedwars.api.util.unchecked

open class MenuBaseTrap(name: String, displayItem: ItemStack, cost: Int, currency: Material) : MenuContent, EnemyBaseEnterTrap, TeamUpgrade {
    private var displayItem: ItemStack
    private var name: String
    private var cost: Int
    private var currency: Material?
    private var trapActions: MutableList<TrapAction> = ArrayList()

    override fun getDisplayItem(player: Player, team: ITeam): ItemStack {
        var currency: Material? = this.currency
        if (this.currency == null) {
            var st: String? = UpgradesManager.getConfiguration().getYml().getString(team.getArena()!!.getGroup().lowercase() + "-upgrades-settings.trap-currency")
            if (st == null) {
                st = UpgradesManager.getConfiguration().getYml().getString("default-upgrades-settings.trap-currency")
            }
            currency = Material.valueOf(st.uppercase())
        }
        var cost: Int = this.cost
        if (cost == 0) {
            val multiplier: Int = team.getActiveTraps().size
            var incrementer: Int = UpgradesManager.getConfiguration().getYml().getInt(team.getArena()!!.getGroup().lowercase() + "-upgrades-settings.trap-increment-price")
            if (incrementer == 0) {
                incrementer = UpgradesManager.getConfiguration().getYml().getInt("default-upgrades-settings.trap-increment-price")
            }
            cost = UpgradesManager.getConfiguration().getYml().getInt(team.getArena()!!.getGroup().lowercase() + "-upgrades-settings.trap-start-price")
            if (cost == 0) {
                cost = UpgradesManager.getConfiguration().getYml().getInt("default-upgrades-settings.trap-start-price")
            }
            cost = cost + (multiplier * incrementer)
        }
        val i = displayItem.clone()
        val im: ItemMeta? = i.getItemMeta()
        if (im != null) {
            val afford: Boolean = UpgradesManager.getMoney(player, currency.unchecked()) >= cost
            var color: String
            if (afford) {
                color = Language.getMsg(player, Messages.FORMAT_UPGRADE_COLOR_CAN_AFFORD)
            } else {
                color = Language.getMsg(player, Messages.FORMAT_UPGRADE_COLOR_CANT_AFFORD)
            }
            im.setDisplayName(Language.getMsg(player, Messages.UPGRADES_BASE_TRAP_ITEM_NAME_PATH + name.replace("base-trap-", "")).replace("{color}", color))
            val lore = Language.getList(player, Messages.UPGRADES_BASE_TRAP_ITEM_LORE_PATH + name.replace("base-trap-", ""))
            val currencyMsg = UpgradesManager.getCurrencyMsg(player, cost, currency)
            lore.add(Language.getMsg(player, Messages.FORMAT_UPGRADE_TRAP_COST).replace("{cost}", cost.toString()).replace("{currency}", currencyMsg).replace("{currencyColor}", UpgradesManager.getCurrencyColor(currency.unchecked()).toString()))
            lore.add("")
            if (afford) {
                lore.add(Language.getMsg(player, Messages.UPGRADES_LORE_REPLACEMENT_CLICK_TO_BUY).replace("{color}", color))
            } else {
                lore.add(Language.getMsg(player, Messages.UPGRADES_LORE_REPLACEMENT_INSUFFICIENT_MONEY).replace("{currency}", currencyMsg).replace("{color}", color))
            }
            im.setLore(lore)
            im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
            i.setItemMeta(im)
        }
        return i
    }

    override fun onClick(player: Player, clickType: ClickType, team: ITeam?) {
        var queueLimit: Int = UpgradesManager.getConfiguration().getInt(team!!.getArena()!!.getGroup().lowercase() + "-upgrades-settings.trap-queue-limit")
        if (queueLimit == 0) {
            queueLimit = UpgradesManager.getConfiguration().getInt("default-upgrades-settings.trap-queue-limit")
        }
        if (queueLimit <= team!!.getActiveTraps().size) {
            player.sendMessage(Language.getMsg(player, Messages.UPGRADES_TRAP_QUEUE_LIMIT))
            return
        }
        var currency: Material? = this.currency
        if (this.currency == null) {
            var st: String? = UpgradesManager.getConfiguration().getYml().getString(team!!.getArena()!!.getGroup().lowercase() + "-upgrades-settings.trap-currency")
            if (st == null) {
                st = UpgradesManager.getConfiguration().getYml().getString("default-upgrades-settings.trap-currency")
            }
            currency = Material.valueOf(st.uppercase())
        }
        var cost: Int = this.cost
        if (cost == 0) {
            val multiplier: Int = team!!.getActiveTraps().size
            var incrementer: Int = UpgradesManager.getConfiguration().getYml().getInt(team!!.getArena()!!.getGroup().lowercase() + "-upgrades-settings.trap-increment-price")
            if (incrementer == 0) {
                incrementer = UpgradesManager.getConfiguration().getYml().getInt("default-upgrades-settings.trap-increment-price")
            }
            cost = UpgradesManager.getConfiguration().getYml().getInt(team!!.getArena()!!.getGroup().lowercase() + "-upgrades-settings.trap-start-price")
            if (cost == 0) {
                cost = UpgradesManager.getConfiguration().getYml().getInt("default-upgrades-settings.trap-start-price")
            }
            cost = cost + (multiplier * incrementer)
        }
        val money: Int = UpgradesManager.getMoney(player, currency.unchecked())
        if (money < cost) {
            Sounds.playSound(ConfigPath.SOUNDS_INSUFF_MONEY, player)
            player.sendMessage(Language.getMsg(player, Messages.SHOP_INSUFFICIENT_MONEY).replace("{currency}", UpgradesManager.getCurrencyMsg(player, cost, currency)).replace("{amount}", (cost - money).toString()))
            player.closeInventory()
            return
        }
        val event: UpgradeBuyEvent
        Bukkit.getPluginManager().callEvent(UpgradeBuyEvent(this, player, team.unchecked()).also { event = it })
        if (event.isCancelled()) {
            return
        }
        if (currency == Material.AIR) {
            BedWars.getEconomy().buyAction(player, money.toDouble())
        } else {
            BedWars.getAPI().getShopUtil().takeMoney(player, currency.unchecked(), cost)
        }
        Sounds.playSound(ConfigPath.SOUNDS_BOUGHT, player)
        team!!.getActiveTraps().add(this)
        // when a new trap is bought check for enemies on the island #646
        for (arenaPlayer in team!!.getArena()!!.getPlayers()!!) {
            if (team!!.isMember(arenaPlayer)) {
                continue
            }
            if (team!!.getArena()!!.isReSpawning(arenaPlayer)) {
                continue
            }
            if (arenaPlayer.getLocation().distance(team!!.getBed()) <= team!!.getArena()!!.getIslandRadius()) {
                team!!.getActiveTraps().removeAt(0).trigger(team.unchecked(), arenaPlayer)
                break
            }
        }
        for (p1 in team!!.getMembers()!!) {
            p1.sendMessage(Language.getMsg(p1, Messages.UPGRADES_UPGRADE_BOUGHT_CHAT).replace("{playername}", player.getName()).replace("{player}", player.getDisplayName()).replace("{upgradeName}", ChatColor.stripColor(Language.getMsg(p1, Messages.UPGRADES_BASE_TRAP_ITEM_NAME_PATH + getName()!!.replace("base-trap-", "")).replace("{color}", ""))))
        }
        UpgradesManager.getMenuForArena(team!!.getArena().unchecked()).open(player)
    }

    override fun getName(): String? {
        return name
    }

    override fun getTierCount(): Int {
        return trapActions.size
    }

    override fun getNameMsgPath(): String {
        return Messages.UPGRADES_BASE_TRAP_ITEM_NAME_PATH + name.replace("base-trap-", "")
    }

    override fun getLoreMsgPath(): String {
        return Messages.UPGRADES_BASE_TRAP_ITEM_LORE_PATH + name.replace("base-trap-", "")
    }

    override fun getItemStack(): ItemStack {
        return displayItem
    }

    override fun trigger(trapTeam: ITeam, player: Player) {
        var sound: Sound? = null
        if (UpgradesManager.getConfiguration().getYml().get(name + ".sound") != null) {
            try {
                sound = BedWars.nms.getSound(UpgradesManager.getConfiguration().getYml().getString(name + ".sound"))
            } catch (ignored: Exception) {
            }
        }
        if (!Sounds.playSound(sound.unchecked(), trapTeam.getMembers().unchecked())) {
            Sounds.playSound("trap-sound", trapTeam.getMembers().unchecked())
        }
        val enemyTeam = trapTeam.getArena()!!.getTeam(player)
        trapActions.forEach({ t -> t.onTrigger(player, enemyTeam.unchecked(), trapTeam) })
        if (UpgradesManager.getConfiguration().getBoolean(name + ".custom-announce")) {
            val name2 = name.replace("base-trap-", "")
            val color: String = if (trapTeam.getArena()!!.getTeam(player) == null) "" else trapTeam.getArena()!!.getTeam(player)!!.getColor().chat().toString()
            for (p in trapTeam.getMembers()!!) {
                val trapName = ChatColor.stripColor(Language.getMsg(p, getNameMsgPath())).replace("{color}", "")
                val enemy: String = if (trapTeam.getArena()!!.getTeam(player) == null) "NULL" else trapTeam.getArena()!!.getTeam(player)!!.getDisplayName(Language.getPlayerLanguage(p))
                p.sendMessage(Language.getMsg(p, Messages.UPGRADES_TRAP_CUSTOM_MSG + name2).replace("{trap}", trapName).replace("{player}", player.getName()).replace("{team}", enemy).replace("{color}", color))
                BedWars.nms.sendTitle(p, Language.getMsg(p, Messages.UPGRADES_TRAP_CUSTOM_TITLE + name2).replace("{trap}", trapName).replace("{player}", player.getName()).replace("{team}", enemy).replace("{color}", color), Language.getMsg(p, Messages.UPGRADES_TRAP_CUSTOM_SUBTITLE + name2).replace("{trap}", trapName).replace("{player}", player.getName()).replace("{team}", enemy).replace("{color}", color), 15, 35, 10)
            }
        } else {
            for (p in trapTeam.getMembers()!!) {
                val trapName = ChatColor.stripColor(Language.getMsg(p, getNameMsgPath())).replace("{color}", "")
                p.sendMessage(Language.getMsg(p, Messages.UPGRADES_TRAP_DEFAULT_MSG).replace("{trap}", trapName))
                BedWars.nms.sendTitle(p, Language.getMsg(p, Messages.UPGRADES_TRAP_DEFAULT_TITLE).replace("{trap}", trapName), Language.getMsg(p, Messages.UPGRADES_TRAP_DEFAULT_SUBTITLE).replace("{trap}", trapName), 15, 35, 10)
            }
        }
    }

    init {
        this.displayItem = BedWars.nms.addCustomData(displayItem, "MCONT_" + name)
        this.name = name
        val nPath = name.replace("base-trap-", "")
        Language.saveIfNotExists(Messages.UPGRADES_BASE_TRAP_ITEM_NAME_PATH + nPath, "&cName not set")
        Language.saveIfNotExists(Messages.UPGRADES_BASE_TRAP_ITEM_LORE_PATH + nPath, Collections.singletonList("&cLore not set"))
        if (UpgradesManager.getConfiguration().getBoolean(name + ".custom-announce")) {
            Language.saveIfNotExists(Messages.UPGRADES_TRAP_CUSTOM_MSG + nPath, "Edit path: " + Messages.UPGRADES_TRAP_CUSTOM_MSG + nPath)
            Language.saveIfNotExists(Messages.UPGRADES_TRAP_CUSTOM_TITLE + nPath, "Edit path: " + Messages.UPGRADES_TRAP_CUSTOM_TITLE + nPath)
            Language.saveIfNotExists(Messages.UPGRADES_TRAP_CUSTOM_SUBTITLE + nPath, "Edit path: " + Messages.UPGRADES_TRAP_CUSTOM_SUBTITLE + nPath)
        }
        this.cost = cost
        this.currency = currency
        for (action in UpgradesManager.getConfiguration().getYml().getStringList(name + ".receive")) {
            val type = action.trim().split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (type.size < 2) {
                continue
            }
            val data = type[1].trim().lowercase().split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            var ua: TrapAction? = null
            when (type[0].trim().lowercase()) {
                "player-effect" -> {
                    if (data.size < 4) {
                        BedWars.plugin.getLogger().warning("Invalid " + type[0] + " at upgrades2: " + name)
                        continue
                    }
                    val pe: PotionEffectType? = PotionEffectType.getByName(data[0].uppercase())
                    if (pe == null) {
                        BedWars.plugin.getLogger().warning("Invalid potion effect " + data[0] + " at upgrades2: " + name)
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
                        "enemy", "enemies" -> {
                            applyType = PlayerEffectAction.ApplyType.ENEMY
                        }
                        else -> {}
                    }
                    if (applyType == null) {
                        BedWars.plugin.getLogger().warning("Invalid apply type " + data[3] + " at upgrades2: " + name)
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
                "disenchant-item" -> {
                    if (data.size < 2) {
                        BedWars.plugin.getLogger().warning("Invalid " + type[0] + " at upgrades2: " + name)
                        continue
                    }
                    val re: Enchantment? = Enchantment.getByName(data[0].uppercase())
                    if (re == null) {
                        BedWars.plugin.getLogger().warning("Invalid enchantment " + data[0] + " at upgrades2: " + name)
                        continue
                    }
                    var da: DisenchantAction.ApplyType? = null
                    when (data[1].lowercase()) {
                        "sword" -> {
                            da = DisenchantAction.ApplyType.SWORD
                        }
                        "armor" -> {
                            da = DisenchantAction.ApplyType.ARMOR
                        }
                        "bow" -> {
                            da = DisenchantAction.ApplyType.BOW
                        }
                        else -> {}
                    }
                    if (da == null) {
                        BedWars.plugin.getLogger().warning("Invalid apply type " + data[3] + " at upgrades2: " + name)
                        continue
                    }
                    ua = DisenchantAction(re, da)
                }
                "remove-effect" -> {
                    if (data.size < 1) {
                        BedWars.plugin.getLogger().warning("Invalid " + type[0] + " at upgrades2: " + name)
                        continue
                    }
                    val pet: PotionEffectType? = PotionEffectType.getByName(data[0].uppercase())
                    if (pet == null) {
                        BedWars.plugin.getLogger().warning("Invalid potion effect " + data[0] + " at upgrades2: " + name)
                        continue
                    }
                    ua = RemoveEffectAction(pet)
                }
                else -> {}
            }
            if (ua != null) {
                trapActions.add(ua)
            }
        }
    }
}
