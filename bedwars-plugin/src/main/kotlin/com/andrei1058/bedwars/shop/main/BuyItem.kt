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
import com.andrei1058.bedwars.BedWars.Companion.plugin
import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.api.arena.shop.IBuyItem
import com.andrei1058.bedwars.api.arena.team.TeamEnchant
import com.andrei1058.bedwars.api.configuration.ConfigPath
import com.andrei1058.bedwars.configuration.Sounds
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import com.andrei1058.bedwars.api.util.unchecked
import com.andrei1058.bedwars.api.util.uninitialized

open class BuyItem : IBuyItem {
    private var itemStack: ItemStack = uninitialized()
    private var autoEquip: Boolean = false
    private var permanent: Boolean = false
    private var unbreakable: Boolean = false
    private var loaded: Boolean = false
    private var upgradeIdentifier: String = uninitialized()

    /**
     * Create a shop item
     */
    constructor(path: String, yml: YamlConfiguration, upgradeIdentifier: String, parent: ContentTier) {
        BedWars.debug("Loading BuyItems: " + path)
        this.upgradeIdentifier = upgradeIdentifier
        if (yml.get(path + ".material") == null) {
            BedWars.plugin.getLogger().severe("BuyItem: Material not set at " + path)
            return
        }
        itemStack = nms.createItemStack(yml.getString(path + ".material"), if (yml.get(path + ".amount") == null) 1 else yml.getInt(path + ".amount"), (if (yml.get(path + ".data") == null) 1 else yml.getInt(path + ".data")).toShort())
        if (yml.get(path + ".name") != null) {
            val im: ItemMeta? = itemStack.getItemMeta()
            if (im != null) {
                im.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&r" + yml.getString(path + ".name")))
                itemStack.setItemMeta(im)
            }
        }
        if (yml.get(path + ".enchants") != null && itemStack.getItemMeta() != null) {
            val imm: ItemMeta? = itemStack.getItemMeta()
            val enchant = yml.getString(path + ".enchants").split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            for (enc in enchant) {
                val stuff = enc.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                try {
                    Enchantment.getByName(stuff[0])
                } catch (ex: Exception) {
                    plugin.getLogger().severe("BuyItem: Invalid enchants " + stuff[0] + " at: " + path + ".enchants")
                    continue
                }
                var ieee: Int = 1
                if (stuff.size >= 2) {
                    try {
                        ieee = Integer.parseInt(stuff[1])
                    } catch (exx: Exception) {
                        plugin.getLogger().severe("BuyItem: Invalid int " + stuff[1] + " at: " + path + ".enchants")
                        continue
                    }
                }
                imm!!.addEnchant(Enchantment.getByName(stuff[0]), ieee, true)
            }
            itemStack.setItemMeta(imm)
        }
        if (yml.get(path + ".potion") != null && (itemStack.getType() == Material.POTION)) {
            // 1.16+ custom color
            if (yml.getString(path + ".potion-color") != null && !yml.getString(path + ".potion-color").isEmpty()) {
                itemStack = nms.setTag(itemStack, "CustomPotionColor", yml.getString(path + ".potion-color"))
            }
            val imm: PotionMeta? = itemStack.getItemMeta() as PotionMeta?
            if (imm != null) {
                val enchant = yml.getString(path + ".potion").split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                for (enc in enchant) {
                    val stuff = enc.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    try {
                        PotionEffectType.getByName(stuff[0].uppercase())
                    } catch (ex: Exception) {
                        plugin.getLogger().severe("BuyItem: Invalid potion effect " + stuff[0] + " at: " + path + ".potion")
                        continue
                    }
                    var duration: Int = 50
                    var amplifier: Int = 1
                    if (stuff.size >= 3) {
                        try {
                            duration = Integer.parseInt(stuff[1])
                        } catch (exx: Exception) {
                            plugin.getLogger().severe("BuyItem: Invalid int (duration) " + stuff[1] + " at: " + path + ".potion")
                            continue
                        }
                        try {
                            amplifier = Integer.parseInt(stuff[2])
                        } catch (exx: Exception) {
                            plugin.getLogger().severe("BuyItem: Invalid int (amplifier) " + stuff[2] + " at: " + path + ".potion")
                            continue
                        }
                    }
                    imm.addCustomEffect(PotionEffect(PotionEffectType.getByName(stuff[0].uppercase()), duration * 20, amplifier), true)
                }
                itemStack.setItemMeta(imm)
            }
            itemStack = nms.setTag(itemStack, "Potion", "minecraft:water")
            if (parent.getItemStack()!!.getType() == Material.POTION && imm != null && !imm.getCustomEffects().isEmpty()) {
                var parentItemStack = parent.getItemStack()
                if (parentItemStack!!.getItemMeta() != null) {
                    val potionMeta = parentItemStack!!.getItemMeta() as PotionMeta
                    for (potionEffect in imm.getCustomEffects()) {
                        potionMeta.addCustomEffect(potionEffect, true)
                    }
                    parentItemStack!!.setItemMeta(potionMeta)
                }
                parentItemStack = nms.setTag(parentItemStack.unchecked(), "Potion", "minecraft:water")
                parent.setItemStack(parentItemStack)
            }
        }
        if (yml.get(path + ".auto-equip") != null) {
            autoEquip = yml.getBoolean(path + ".auto-equip")
        }
        if (yml.get(upgradeIdentifier + "." + ConfigPath.SHOP_CATEGORY_CONTENT_IS_PERMANENT) != null) {
            permanent = yml.getBoolean(upgradeIdentifier + "." + ConfigPath.SHOP_CATEGORY_CONTENT_IS_PERMANENT)
        }
        if (yml.get(upgradeIdentifier + "." + ConfigPath.SHOP_CATEGORY_CONTENT_IS_UNBREAKABLE) != null) {
            unbreakable = yml.getBoolean(upgradeIdentifier + "." + ConfigPath.SHOP_CATEGORY_CONTENT_IS_UNBREAKABLE)
        }
        loaded = true
    }

    /**
     * Check if object created properly
     */
    override fun isLoaded(): Boolean {
        return loaded
    }

    /**
     * Give to a player
     */
    override fun give(player: Player, arena: IArena) {
        give(player, arena, null)
    }

    /**
     * Give to a player. [shopName] is used when the item has no name of its own (so it is not left with its vanilla
     * one, like "Horse Spawn Egg").
     */
    open fun give(player: Player, arena: IArena, shopName: String?) {
        var i = itemStack.clone()
        if (shopName != null && !shopName.isEmpty()) {
            val named: ItemMeta? = i.getItemMeta()
            if (named != null && !named.hasDisplayName()) {
                named.setDisplayName(shopName)
                i.setItemMeta(named)
            }
        }
        BedWars.debug("Giving BuyItem: " + getUpgradeIdentifier() + " to: " + player.getName())
        if (autoEquip && nms.isArmor(itemStack)) {
            val m = i.getType()
            val im: ItemMeta? = i.getItemMeta()
            // idk dadea erori
            if (arena.getTeam(player) == null) {
                BedWars.debug("Could not give BuyItem to " + player.getName() + " - TEAM IS NULL")
                return
            }
            if (im != null) {
                for (e in arena.getTeam(player)!!.getArmorsEnchantments()!!) {
                    im.addEnchant(e.getEnchantment(), e.getAmplifier(), true)
                }
                if (permanent) {
                    nms.setUnbreakable(im)
                }
                i.setItemMeta(im)
            }
            if (m == Material.LEATHER_HELMET || m == Material.CHAINMAIL_HELMET || m == Material.IRON_HELMET || m == Material.DIAMOND_HELMET || m == nms.materialGoldenHelmet() || m == nms.materialNetheriteHelmet()) {
                if (permanent) {
                    i = nms.setShopUpgradeIdentifier(i, upgradeIdentifier)
                }
                player.getInventory().setHelmet(i)
            } else if (m == Material.LEATHER_CHESTPLATE || m == Material.CHAINMAIL_CHESTPLATE || m == Material.IRON_CHESTPLATE || m == Material.DIAMOND_CHESTPLATE || m == nms.materialGoldenChestPlate() || m == nms.materialNetheriteChestPlate() || m == nms.materialElytra()) {
                if (permanent) {
                    i = nms.setShopUpgradeIdentifier(i, upgradeIdentifier)
                }
                player.getInventory().setChestplate(i)
            } else if (m == Material.LEATHER_LEGGINGS || m == Material.CHAINMAIL_LEGGINGS || m == Material.IRON_LEGGINGS || m == Material.DIAMOND_LEGGINGS || m == nms.materialGoldenLeggings() || m == nms.materialNetheriteLeggings()) {
                if (permanent) {
                    i = nms.setShopUpgradeIdentifier(i, upgradeIdentifier)
                }
                player.getInventory().setLeggings(i)
            } else {
                if (permanent) {
                    i = nms.setShopUpgradeIdentifier(i, upgradeIdentifier)
                }
                player.getInventory().setBoots(i)
            }
            player.updateInventory()
            Sounds.playSound("shop-auto-equip", player)
            Bukkit.getScheduler().runTaskLater(plugin, Runnable {
                // #274
                if (player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                    for (p in arena.getPlayers()!!) {
                        BedWars.nms.hideArmor(player, p)
                    }
                }
                //
            }, 20L)
            return
        } else {
            val im: ItemMeta? = i.getItemMeta()
            i = nms.colourItem(i, arena.getTeam(player).unchecked()).unchecked()
            if (im != null) {
                if (permanent) {
                    nms.setUnbreakable(im)
                }
                if (unbreakable) {
                    nms.setUnbreakable(im)
                }
                if (i.getType() == Material.BOW) {
                    if (permanent) {
                        nms.setUnbreakable(im)
                    }
                    for (e in arena.getTeam(player)!!.getBowsEnchantments()!!) {
                        im.addEnchant(e.getEnchantment(), e.getAmplifier(), true)
                    }
                } else if (nms.isSword(i) || nms.isAxe(i)) {
                    for (e in arena.getTeam(player)!!.getSwordsEnchantments()!!) {
                        im.addEnchant(e.getEnchantment(), e.getAmplifier(), true)
                    }
                }
                i.setItemMeta(im)
            }
            if (permanent) {
                i = nms.setShopUpgradeIdentifier(i, upgradeIdentifier)
            }
        }
        //Remove swords with lower damage
        if (BedWars.nms.isSword(i)) {
            for (itm in player.getInventory().getContents()) {
                if (itm == null) {
                    continue
                }
                if (itm.getType() == Material.AIR) {
                    continue
                }
                if (!BedWars.nms.isSword(itm)) {
                    continue
                }
                if (itm === i) {
                    continue
                }
                if (nms.isCustomBedWarsItem(itm) && nms.getCustomData(itm) == "DEFAULT_ITEM") {
                    if (BedWars.nms.getDamage(itm) <= BedWars.nms.getDamage(i)) {
                        player.getInventory().remove(itm)
                    }
                }
            }
        }
        //
        player.getInventory().addItem(i)
        player.updateInventory()
    }

    /**
     * Get upgrade identifier.
     * Used to remove old tier items.
     */
    override fun getUpgradeIdentifier(): String? {
        return upgradeIdentifier
    }

    override fun getItemStack(): ItemStack? {
        return itemStack
    }

    override fun setItemStack(itemStack: ItemStack) {
        this.itemStack = itemStack
    }

    override fun isAutoEquip(): Boolean {
        return autoEquip
    }

    override fun setAutoEquip(autoEquip: Boolean) {
        this.autoEquip = autoEquip
    }

    override fun isPermanent(): Boolean {
        return permanent
    }

    override fun setPermanent(permanent: Boolean) {
        this.permanent = permanent
    }

    override fun isUnbreakable(): Boolean {
        return unbreakable
    }

    override fun setUnbreakable(unbreakable: Boolean) {
        this.unbreakable = unbreakable
    }
}
