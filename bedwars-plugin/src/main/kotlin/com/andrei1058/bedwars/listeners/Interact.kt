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

package com.andrei1058.bedwars.listeners

import com.andrei1058.bedwars.BedWars
import com.andrei1058.bedwars.BedWars.Companion.config
import com.andrei1058.bedwars.BedWars.Companion.getAPI
import com.andrei1058.bedwars.BedWars.Companion.getLobbyWorld
import com.andrei1058.bedwars.BedWars.Companion.getServerType
import com.andrei1058.bedwars.BedWars.Companion.nms
import com.andrei1058.bedwars.BedWars.Companion.plugin
import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.api.arena.team.ITeam
import com.andrei1058.bedwars.api.configuration.ConfigPath
import com.andrei1058.bedwars.api.language.Language.Companion.getMsg
import com.andrei1058.bedwars.api.language.Messages
import com.andrei1058.bedwars.api.server.ServerType
import com.andrei1058.bedwars.arena.Arena
import com.andrei1058.bedwars.configuration.Sounds
import com.andrei1058.bedwars.shop.ShopCache
import com.andrei1058.bedwars.shop.listeners.InventoryListener
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.Sign
import org.bukkit.entity.EntityType
import org.bukkit.entity.Fireball
import org.bukkit.entity.ItemFrame
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.inventory.PrepareItemCraftEvent
import org.bukkit.event.player.PlayerArmorStandManipulateEvent
import org.bukkit.event.player.PlayerBedEnterEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.material.Openable
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.util.Vector

open class Interact : Listener {
    private val fireballSpeedMultiplier: Double
    private val fireballCooldown: Double
    private val fireballExplosionSize: Float

    @EventHandler
    open fun onItemCommand(e: PlayerInteractEvent?) {
        if (e == null) {
            return
        }
        val p = e.getPlayer()
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR) {
            val i = BedWars.nms.getItemInHand(p)
            if (!nms.isCustomBedWarsItem(i)) {
                return
            }
            val customData = nms.getCustomData(i).split("_".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (customData.size >= 2) {
                if (customData[0] == "RUNCOMMAND") {
                    e.setCancelled(true)
                    Bukkit.getScheduler().runTask(plugin, Runnable { Bukkit.dispatchCommand(p, customData[1]) })
                }
            }
        }
    }

    @EventHandler
    open fun onInventoryInteract(e: PlayerInteractEvent?) {
        if (e == null) {
            return
        }
        if (e.isCancelled()) {
            return
        }
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return
        }
        val b: Block? = e.getClickedBlock()
        if (b == null) {
            return
        }
        if ((BedWars.getServerType() == ServerType.MULTIARENA && b.getWorld().getName() == BedWars.getLobbyWorld() && !BreakPlace.isBuildSession(e.getPlayer())) || Arena.getArenaByPlayer(e.getPlayer()) != null) {
            if (b.getType() == nms.materialCraftingTable() && config.getBoolean(ConfigPath.GENERAL_CONFIGURATION_DISABLE_CRAFTING)) {
                e.setCancelled(true)
            } else if (b.getType() == nms.materialEnchantingTable() && config.getBoolean(ConfigPath.GENERAL_CONFIGURATION_DISABLE_ENCHANTING)) {
                e.setCancelled(true)
            } else if (b.getType() == Material.FURNACE && config.getBoolean(ConfigPath.GENERAL_CONFIGURATION_DISABLE_FURNACE)) {
                e.setCancelled(true)
            } else if (b.getType() == Material.BREWING_STAND && config.getBoolean(ConfigPath.GENERAL_CONFIGURATION_DISABLE_BREWING_STAND)) {
                e.setCancelled(true)
            } else if (b.getType() == Material.ANVIL && config.getBoolean(ConfigPath.GENERAL_CONFIGURATION_DISABLE_ANVIL)) {
                e.setCancelled(true)
            }
        }
    }

    @EventHandler
    open fun onInteract(e: PlayerInteractEvent?) {
        if (e == null) {
            return
        }
        val p = e.getPlayer()
        Arena.afkCheck.remove(p.getUniqueId())
        if (BedWars.getAPI().getAFKUtil().isPlayerAFK(e.getPlayer())) {
            BedWars.getAPI().getAFKUtil().setPlayerAFK(e.getPlayer(), false)
        }
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            val b: Block? = e.getClickedBlock()
            if (b == null) {
                return
            }
            if (b.getType() == Material.AIR) {
                return
            }
            val a: IArena? = Arena.getArenaByPlayer(p)
            if (a != null) {
                if (a.getRespawnSessions()!!.containsKey(e.getPlayer())) {
                    e.setCancelled(true)
                    return
                }
                if (nms.isBed(b.getType())) {
                    if (p.isSneaking()) {
                        val i: ItemStack? = nms.getItemInHand(p)
                        if (i == null) {
                            e.setCancelled(true)
                        } else if (i.getType() == Material.AIR) {
                            e.setCancelled(true)
                        }
                    } else {
                        e.setCancelled(true)
                    }
                    return
                }
                if (b.getType() == Material.CHEST) {
                    if (a.isSpectator(p) || a.getRespawnSessions()!!.containsKey(p)) {
                        e.setCancelled(true)
                        return
                    }
                    //make it so only team members can open chests while team is alive, and all when is eliminated
                    var owner: ITeam? = null
                    val isRad: Int = a.getConfig()!!.getInt(ConfigPath.ARENA_ISLAND_RADIUS)
                    for (t in a.getTeams()!!) {
                        if (t.getSpawn()!!.distance(e.getClickedBlock().getLocation()) <= isRad) {
                            owner = t
                        }
                    }
                    if (owner != null) {
                        if (!owner.isMember(p)) {
                            if (!(owner.getMembers()!!.isEmpty() && owner.isBedDestroyed())) {
                                e.setCancelled(true)
                                p.sendMessage(getMsg(p, Messages.INTERACT_CHEST_CANT_OPEN_TEAM_ELIMINATED))
                            }
                        }
                    }
                }
                if (a.isSpectator(p) || a.getRespawnSessions()!!.containsKey(p)) {
                    when (b.getType().toString()) {
                        "CHEST", "ENDER_CHEST", "ANVIL", "WORKBENCH", "HOPPER", "TRAPPED_CHEST", "CRAFTING_TABLE" -> {
                            e.setCancelled(true)
                        }
                        else -> {}
                    }
                    if (b.getState() is Openable) {
                        e.setCancelled(true)
                    }
                }
            }
            if (b.getState() is Sign) {
                for (a1 in Arena.getArenas()) {
                    if (a1.getSigns()!!.contains(b)) {
                        if (a1.addPlayer(p, false)) {
                            Sounds.playSound("join-allowed", p)
                        } else {
                            Sounds.playSound("join-denied", p)
                        }
                        return
                    }
                }
            }
        }
        //check hand
        val inHand: ItemStack? = e.getItem()
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR) {
            if (inHand == null) {
                return
            }
            val a: IArena? = Arena.getArenaByPlayer(p)
            if (a != null) {
                if (a.isPlayer(p)) {
                    if (inHand.getType() == nms.materialFireball()) {
                        e.setCancelled(true)
                        if (System.currentTimeMillis() - a.getFireballCooldowns().getOrDefault(p.getUniqueId(), 0L) > (fireballCooldown * 1000)) {
                            a.getFireballCooldowns().put(p.getUniqueId(), System.currentTimeMillis())
                            var fb = p.launchProjectile(Fireball::class.java)
                            val direction = p.getEyeLocation().getDirection()
                            fb = nms.setFireballDirection(fb, direction)
                            fb.setVelocity(fb.getDirection().multiply(fireballSpeedMultiplier))
                            //fb.setIsIncendiary(false); // apparently this on <12 makes the fireball not explode on hit. wtf bukkit?
                            fb.setYield(fireballExplosionSize)
                            fb.setMetadata("bw1058", FixedMetadataValue(plugin, "ceva"))
                            nms.minusAmount(p, inHand, 1)
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    open fun disableItemFrameRotation(e: PlayerInteractEntityEvent?) {
        if (e == null) {
            return
        }
        if (e.getRightClicked().getType() == EntityType.ITEM_FRAME) {
            if ((e.getRightClicked() as ItemFrame).getItem().getType() == Material.AIR) {
                //prevent from putting upgradable items in it
                val i: ItemStack? = nms.getItemInHand(e.getPlayer())
                if (i != null) {
                    if (i.getType() != Material.AIR) {
                        val sc: ShopCache? = ShopCache.getShopCache(e.getPlayer().getUniqueId())
                        if (sc != null) {
                            if (InventoryListener.shouldCancelMovement(i, sc)) {
                                e.setCancelled(true)
                            }
                        }
                    }
                }
                return
            }
            val a: IArena? = Arena.getArenaByIdentifier(e.getPlayer().getWorld().getName())
            if (a != null) {
                e.setCancelled(true)
            }
            if (BedWars.getServerType() == ServerType.MULTIARENA) {
                if (BedWars.getLobbyWorld() == e.getPlayer().getWorld().getName() && !BreakPlace.isBuildSession(e.getPlayer())) {
                    e.setCancelled(true)
                }
            }
        }
    }

    @EventHandler
    open fun onEntityInteract(e: PlayerInteractEntityEvent?) {
        if (e == null) {
            return
        }
        val a: IArena? = Arena.getArenaByPlayer(e.getPlayer())
        if (a == null) {
            return
        }
        val l = e.getRightClicked().getLocation()
        for (t in a.getTeams()!!) {
            val l2 = t.getShop()
            val l3 = t.getTeamUpgrades()
            if (l.getBlockX() == l2!!.getBlockX() && l.getBlockY() == l2!!.getBlockY() && l.getBlockZ() == l2!!.getBlockZ()) {
                e.setCancelled(true)
            } else if (l.getBlockX() == l3!!.getBlockX() && l.getBlockY() == l3!!.getBlockY() && l.getBlockZ() == l3!!.getBlockZ()) {
                e.setCancelled(true)
            }
        }
    }

    @EventHandler
    open fun onBedEnter(e: PlayerBedEnterEvent?) {
        if (e == null) {
            return
        }
        if (Arena.getArenaByPlayer(e.getPlayer()) != null) {
            e.setCancelled(true)
        }
    }

    @EventHandler
    open fun onArmorManipulate(e: PlayerArmorStandManipulateEvent?) {
        if (e == null) {
            return
        }
        if (e.isCancelled()) {
            return
        }
        //prevent from breaking generators
        if (Arena.getArenaByPlayer(e.getPlayer()) != null) {
            e.setCancelled(true)
        }
        //prevent from stealing from armor stands in lobby
        if (BedWars.getServerType() == ServerType.MULTIARENA && e.getPlayer().getLocation().getWorld().getName().equals(BedWars.getLobbyWorld(), ignoreCase = true) && !BreakPlace.isBuildSession(e.getPlayer())) {
            e.setCancelled(true)
        }
    }

    @EventHandler
    open fun onCrafting(e: PrepareItemCraftEvent?) {
        if (e == null) {
            return
        }
        if (Arena.getArenaByPlayer(BedWars.nms.getViewPlayer(e) as Player) != null) {
            if (config.getBoolean(ConfigPath.GENERAL_CONFIGURATION_DISABLE_CRAFTING)) {
                e.getInventory().setResult(ItemStack(Material.AIR))
            }
        }
    }

    init {
        this.fireballSpeedMultiplier = config.getYml().getDouble(ConfigPath.GENERAL_FIREBALL_SPEED_MULTIPLIER)
        this.fireballCooldown = config.getYml().getDouble(ConfigPath.GENERAL_FIREBALL_COOLDOWN)
        this.fireballExplosionSize = config.getYml().getDouble(ConfigPath.GENERAL_FIREBALL_EXPLOSION_SIZE).toFloat()
    }
}
