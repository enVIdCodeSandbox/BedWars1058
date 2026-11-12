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
import com.andrei1058.bedwars.api.arena.GameState
import com.andrei1058.bedwars.api.events.gameplay.GameStateChangeEvent
import com.andrei1058.bedwars.api.events.player.PlayerJoinArenaEvent
import com.andrei1058.bedwars.api.events.player.PlayerKillEvent
import com.andrei1058.bedwars.api.events.player.PlayerXpGainEvent
import com.andrei1058.bedwars.api.events.server.ArenaDisableEvent
import com.andrei1058.bedwars.api.events.server.ArenaEnableEvent
import com.andrei1058.bedwars.api.events.server.ArenaRestartEvent
import com.andrei1058.bedwars.arena.Misc
import com.andrei1058.bedwars.levels.internal.PlayerLevel
import org.bukkit.*
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.world.WorldLoadEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue

open class HalloweenListener : Listener {
    private val ambienceSound: Sound
    private val ghastSound: Sound

    @EventHandler
    open fun onCreatureSpawn(e: CreatureSpawnEvent) {
        if (e.isCancelled()) {
            return
        }
        val entity = e.getEntity()
        if (entity.getType() == EntityType.ARMOR_STAND) {
            return
        }
        entity.getEquipment().setHelmet(ItemStack(Material.PUMPKIN))
    }

    @EventHandler(priority = EventPriority.MONITOR)
    open fun onWorldLoad(e: WorldLoadEvent) {
        // check if it is time to disable this special
        if (HalloweenSpecial.getINSTANCE() != null) {
            if (!HalloweenSpecial.checkAvailabilityDate()) {
                CreatureSpawnEvent.getHandlerList().unregister(this)
            }
        }
    }

    @EventHandler
    open fun onPlayerDie(e: PlayerKillEvent) {
        if (e.getKiller() != null) {
            val location = e.getVictim().getLocation().add(0.0, 1.0, 0.0)
            if (location.getBlock().getType() == Material.AIR) {
                location.getWorld().playSound(location, ghastSound, 2f, 1f)
                if (!Misc.isBuildProtected(location, e.getArena())) {
                    location.getBlock().setType(Material.valueOf(BedWars.getForCurrentVersion("WEB", "WEB", "COBWEB")))
                    e.getArena().addPlacedBlock(location.getBlock())
                    location.getBlock().setMetadata("give-bw-exp", FixedMetadataValue(BedWars.plugin, "ok"))
                    val remover: CobWebRemover? = CobWebRemover.getByArena(e.getArena())
                    if (remover != null) {
                        remover.addCobWeb(location.getBlock())
                    }
                }
            }
        }
    }

    @EventHandler
    open fun onBlockBreak(e: BlockBreakEvent) {
        if (e.isCancelled()) {
            return
        }
        if (e.getBlock().hasMetadata("give-bw-exp")) {
            val level: PlayerLevel? = PlayerLevel.getLevelByPlayer(e.getPlayer().getUniqueId())
            if (level != null) {
                e.getBlock().getDrops().clear()
                level.addXp(5, PlayerXpGainEvent.XpSource.OTHER)
                e.getPlayer().sendMessage(ChatColor.GOLD.toString() + "+5 xp!")
            }
        }
    }

    @EventHandler
    open fun onJoin(e: PlayerJoinArenaEvent) {
        if (!e.isSpectator()) {
            Bukkit.getScheduler().runTaskLater(BedWars.plugin, Runnable { e.getPlayer().getWorld().playSound(e.getPlayer().getLocation(), ambienceSound, 3f, 1f) }, 20L)
        }
    }

    @EventHandler
    open fun onGameStateChange(e: GameStateChangeEvent) {
        if (e.getNewState() == GameState.restarting) {
            val remover: CobWebRemover? = CobWebRemover.getByArena(e.getArena())
            if (remover != null) {
                remover.destroy()
            }
        }
    }

    @EventHandler
    open fun onRestart(e: ArenaRestartEvent) {
        val remover: CobWebRemover? = CobWebRemover.getByArenaWorld(e.getWorldName())
        if (remover != null) {
            remover.destroy()
        }
    }

    @EventHandler
    open fun onDisable(e: ArenaDisableEvent) {
        val remover: CobWebRemover? = CobWebRemover.getByArenaWorld(e.getWorldName())
        if (remover != null) {
            remover.destroy()
        }
    }

    @EventHandler
    open fun onEnable(e: ArenaEnableEvent) {
        CobWebRemover(e.getArena())
    }

    init {
        ambienceSound = BedWars.nms.getSound(BedWars.getForCurrentVersion("AMBIENCE_CAVE", "AMBIENT_CAVE", "AMBIENT_CAVE"))
        ghastSound = BedWars.nms.getSound(BedWars.getForCurrentVersion("GHAST_SCREAM2", "ENTITY_GHAST_SCREAM", "ENTITY_GHAST_SCREAM"))
    }
}
