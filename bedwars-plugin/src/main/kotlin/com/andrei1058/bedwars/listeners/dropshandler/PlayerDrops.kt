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

package com.andrei1058.bedwars.listeners.dropshandler

import com.andrei1058.bedwars.BedWars.Companion.nms
import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.api.arena.team.ITeam
import com.andrei1058.bedwars.api.configuration.ConfigPath
import com.andrei1058.bedwars.api.events.player.PlayerKillEvent
import com.andrei1058.bedwars.api.language.Language.Companion.getMsg
import com.andrei1058.bedwars.api.language.Messages
import java.util.HashMap
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector

object PlayerDrops {
    /**
     * if bedWars should handle drops behavior.
     *
     * @return true if event drops must be cleared.
     */
    @JvmStatic
    fun handlePlayerDrops(arena: IArena, victim: Player, killer: Player?, victimsTeam: ITeam?, killersTeam: ITeam, cause: PlayerKillEvent.PlayerKillCause, inventory: MutableList<ItemStack>): Boolean {
        if (arena.getConfig()!!.getBoolean(ConfigPath.ARENA_NORMAL_DEATH_DROPS)) {
            return false
        }
        if (cause == PlayerKillEvent.PlayerKillCause.PLAYER_PUSH || cause == PlayerKillEvent.PlayerKillCause.PLAYER_PUSH_FINAL) {
            // if died by fall damage drop items at location
            dropItems(victim, inventory)
            return true
        }
        if (killer == null) {
            // Death without a attacker drops items on the floor
            dropItems(victim, inventory)
            return true
        }
        if (cause.isDespawnable()) {
            // If killed by a ironGolem or silverFish drop on floor
            dropItems(victim, inventory)
            return true
        }
        if (cause.isPvpLogOut()) {
            // if is pvp log out drop at disconnect location
            dropItems(victim, inventory)
            return true
        }
        if (cause.isFinalKill()) {
            // if is final kill drop items at generator
            if (victimsTeam != null) {
                val dropsLocation = Location(victim.getWorld(), victimsTeam.getKillDropsLocation()!!.getBlockX().toDouble(), victimsTeam.getKillDropsLocation()!!.getY(), victimsTeam.getKillDropsLocation()!!.getZ())
                victim.getEnderChest().forEach({ item ->
                    if (item != null) {
                        victim.getWorld().dropItemNaturally(dropsLocation, item)
                    }
                })
                victim.getEnderChest().clear()
            }
        }
        if (victimsTeam != null && !(victimsTeam == killersTeam && victim == killer)) {
            // if final kill give items at kill drops location (team generator)
            if (victimsTeam.isBedDestroyed()) {
                for (i in inventory) {
                    if (i == null) {
                        continue
                    }
                    if (i.getType() == Material.AIR) {
                        continue
                    }
                    if (nms.isArmor(i) || nms.isBow(i) || nms.isSword(i) || nms.isTool(i)) {
                        continue
                    }
                    if (!nms.getShopUpgradeIdentifier(i).trim().isEmpty()) {
                        continue
                    }
                    if (arena.getTeam(killer) != null) {
                        val v = victimsTeam.getKillDropsLocation()
                        killer.getWorld().dropItemNaturally(Location(arena.getWorld(), v!!.getX(), v!!.getY(), v!!.getZ()), i)
                    }
                }
            } else {
                // add-to-inventory feature if receiver is not respawning
                if (!arena.isPlayer(killer)) {
                    return true
                }
                if (arena.isReSpawning(killer)) {
                    return true
                }
                val materialDrops: MutableMap<Material, Int> = HashMap()
                for (i in inventory) {
                    if (i == null) {
                        continue
                    }
                    if (i.getType() == Material.AIR) {
                        continue
                    }
                    if (i.getType() == Material.DIAMOND || i.getType() == Material.EMERALD || i.getType() == Material.IRON_INGOT || i.getType() == Material.GOLD_INGOT) {
                        // add to killer inventory
                        killer.getInventory().addItem(i)
                        // count items
                        if (materialDrops.containsKey(i.getType())) {
                            materialDrops.replace(i.getType(), materialDrops.get(i.getType())!! + i.getAmount())
                        } else {
                            materialDrops.put(i.getType(), i.getAmount())
                        }
                    }
                }
                for (entry in materialDrops.entries) {
                    var msg = ""
                    val amount: Int = entry.value
                    when (entry.key) {
                        Material.DIAMOND -> {
                            msg = getMsg(killer, Messages.PLAYER_DIE_REWARD_DIAMOND).replace("{meaning}", if (amount == 1) getMsg(killer, Messages.MEANING_DIAMOND_SINGULAR) else getMsg(killer, Messages.MEANING_DIAMOND_PLURAL))
                        }
                        Material.EMERALD -> {
                            msg = getMsg(killer, Messages.PLAYER_DIE_REWARD_EMERALD).replace("{meaning}", if (amount == 1) getMsg(killer, Messages.MEANING_EMERALD_SINGULAR) else getMsg(killer, Messages.MEANING_EMERALD_PLURAL))
                        }
                        Material.IRON_INGOT -> {
                            msg = getMsg(killer, Messages.PLAYER_DIE_REWARD_IRON).replace("{meaning}", if (amount == 1) getMsg(killer, Messages.MEANING_IRON_SINGULAR) else getMsg(killer, Messages.MEANING_IRON_PLURAL))
                        }
                        Material.GOLD_INGOT -> {
                            msg = getMsg(killer, Messages.PLAYER_DIE_REWARD_GOLD).replace("{meaning}", if (amount == 1) getMsg(killer, Messages.MEANING_GOLD_SINGULAR) else getMsg(killer, Messages.MEANING_GOLD_PLURAL))
                        }
                        else -> {}
                    }
                    killer.sendMessage(msg.replace("{amount}", amount.toString()))
                }
                materialDrops.clear()
            }
        }
        return true
    }

    @JvmStatic
    private fun dropItems(player: Player, inventory: MutableList<ItemStack>) {
        for (i in inventory) {
            if (i == null) {
                continue
            }
            if (i.getType() == Material.AIR) {
                continue
            }
            if (i.getType() == Material.DIAMOND || i.getType() == Material.EMERALD || i.getType() == Material.IRON_INGOT || i.getType() == Material.GOLD_INGOT) {
                player.getLocation().getWorld().dropItemNaturally(player.getLocation(), i)
            }
        }
    }
}
