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

package com.andrei1058.bedwars.arena

import com.andrei1058.bedwars.BedWars
import com.andrei1058.bedwars.BedWars.Companion.plugin
import java.util.*
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import com.andrei1058.bedwars.api.util.unchecked
import com.andrei1058.bedwars.api.util.uninitialized

/**
 * This is where player stuff are stored so he can have them back after a game
 */
open class PlayerGoods {
    private var uuid: UUID? = null
    private var level: Int = 0
    private var foodLevel: Int = 0
    private var health: Double = 0.0
    private var healthscale: Double = 0.0
    private var exp: Float = 0f
    private var items: HashMap<ItemStack, Int>? = HashMap()
    private var potions: MutableList<PotionEffect>? = ArrayList()
    private var armor: Array<ItemStack>? = null
    private var enderchest: HashMap<ItemStack, Int>? = HashMap()
    private var gamemode: GameMode = uninitialized()
    private var allowFlight: Boolean = false
    private var flying: Boolean = false
    private var displayName: String = uninitialized()
    private var tabName: String = uninitialized()

    constructor(p: Player, prepare: Boolean) : this(p, prepare, false)

    constructor(p: Player, prepare: Boolean, rejoin: Boolean) {
        if (hasGoods(p)) {
            plugin.getLogger().severe(p.getName() + " is already having a PlayerGoods vault :|")
            return
        }
        this.uuid = p.getUniqueId()
        this.level = p.getLevel()
        this.exp = p.getExp()
        this.health = p.getHealth()
        this.healthscale = p.getHealthScale()
        this.foodLevel = p.getFoodLevel()
        playerGoods.put(p.getUniqueId(), this)
        var x: Int = 0
        for (i in p.getInventory()) {
            if (i != null) {
                if (i.getType() != Material.AIR) {
                    items!!.put(i, x)
                }
            }
            x++
        }
        for (ef in p.getActivePotionEffects()) {
            potions!!.add(ef)
            if (prepare) {
                p.removePotionEffect(ef.getType())
            }
        }
        armor = p.getInventory().getArmorContents()
        if (!rejoin) {
            var x2: Int = 0
            for (i in p.getEnderChest()) {
                if (i != null) {
                    if (i.getType() != Material.AIR) {
                        enderchest!!.put(i, x2)
                    }
                }
                x2++
            }
        }
        this.gamemode = p.getGameMode()
        this.allowFlight = p.getAllowFlight()
        this.flying = p.isFlying()
        this.tabName = p.getPlayerListName()
        this.displayName = p.getDisplayName()
        /* prepare for arena */
        if (prepare) {
            p.setExp(0f)
            p.setLevel(0)
            p.setHealthScale(20.0)
            p.setHealth(20.0)
            p.setFoodLevel(20)
            p.getInventory().clear()
            p.getInventory().setArmorContents(null)
            if (!rejoin) {
                p.getEnderChest().clear()
            }
            p.setGameMode(GameMode.SURVIVAL)
            p.setAllowFlight(false)
            p.setFlying(false)
        }
    }

    /**
     * restore player
     */
    open fun restore() {
        val player: Player? = Bukkit.getPlayer(uuid)
        if (player == null) {
            return
        }
        playerGoods.remove(player.getUniqueId())
        for (pf in player.getActivePotionEffects()) {
            player.removePotionEffect(pf.getType())
        }
        player.getInventory().clear()
        player.getInventory().setArmorContents(null)
        player.setLevel(level)
        player.setExp(exp)
        player.setHealthScale(healthscale)
        try {
            player.setHealth(health)
        } catch (e: Exception) {
            BedWars.plugin.getLogger().severe("Something went wrong when restoring player health: " + health + ". Giving default of: 20")
            player.setHealth(20.0)
        }
        player.setFoodLevel(foodLevel)
        if (!items!!.isEmpty()) {
            for (entry in items!!.entries) {
                player.getInventory().setItem(entry.value, entry.key)
            }
            player.updateInventory()
            items!!.clear()
        }
        if (!potions!!.isEmpty()) {
            for (pe in potions!!) {
                player.addPotionEffect(pe)
            }
            potions!!.clear()
        }
        player.getEnderChest().clear()
        if (!enderchest!!.isEmpty()) {
            for (entry in enderchest!!.entries) {
                player.getEnderChest().setItem(entry.value, entry.key)
            }
            enderchest!!.clear()
        }
        player.getInventory().setArmorContents(armor)
        player.setGameMode(gamemode)
        player.setAllowFlight(allowFlight)
        player.setFlying(flying)
        if (displayName != player.getDisplayName()) {
            player.setDisplayName(displayName)
        }
        if (tabName != player.getPlayerListName()) {
            player.setPlayerListName(tabName)
        }
        uuid = null
        items = null
        potions = null
        armor = null
        enderchest = null
    }

    companion object {
        /**
         * a list where you can get PlayerGoods by player
         */
        private var playerGoods: HashMap<UUID, PlayerGoods> = HashMap()

        /**
         * check if a player has a vault
         */
        @JvmStatic
        fun hasGoods(p: Player): Boolean {
            return playerGoods.containsKey(p.getUniqueId())
        }

        /**
         * get a player vault
         */
        @JvmStatic
        fun getPlayerGoods(p: Player): PlayerGoods {
            return playerGoods.get(p.getUniqueId()).unchecked()
        }
    }
}
