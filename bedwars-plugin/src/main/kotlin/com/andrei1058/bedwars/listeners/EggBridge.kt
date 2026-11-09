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
import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.api.events.gameplay.EggBridgeThrowEvent
import com.andrei1058.bedwars.api.server.ServerType
import com.andrei1058.bedwars.arena.Arena
import com.andrei1058.bedwars.arena.tasks.EggBridgeTask
import java.util.Collections
import java.util.HashMap
import org.bukkit.Bukkit
import org.bukkit.entity.Egg
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.event.player.PlayerEggThrowEvent

open class EggBridge : Listener {
    @EventHandler
    open fun onLaunch(event: ProjectileLaunchEvent) {
        if (BedWars.getServerType() == ServerType.MULTIARENA) {
            if (event.getEntity().getLocation().getWorld().getName().equals(BedWars.getLobbyWorld(), ignoreCase = true)) {
                event.setCancelled(true)
                return
            }
        }
        if (event.getEntity() is Egg) {
            val projectile = event.getEntity() as Egg
            if (projectile.getShooter() is Player) {
                val shooter = projectile.getShooter() as Player
                val arena: IArena? = Arena.getArenaByPlayer(shooter)
                if (arena != null) {
                    if (arena.isPlayer(shooter)) {
                        val throwEvent = EggBridgeThrowEvent(shooter, arena)
                        Bukkit.getPluginManager().callEvent(throwEvent)
                        if (throwEvent.isCancelled()) {
                            return
                        }
                        bridges.put(projectile, EggBridgeTask(shooter, projectile, arena.getTeam(shooter)!!.getColor()))
                    }
                }
            }
        }
    }

    @EventHandler
    open fun onHit(e: ProjectileHitEvent) {
        if (e.getEntity() is Egg) {
            // the egg is about to vanish; the bridge goes on along its path, through blocks
            val task: EggBridgeTask? = bridges.get(e.getEntity() as Egg)
            if (task != null) {
                task.onProjectileHit()
            }
        }
    }

    /** A bridge egg must not hatch a chicken where it lands. */
    @EventHandler
    open fun onEggHatch(e: PlayerEggThrowEvent) {
        if (bridges.containsKey(e.getEgg())) {
            e.setHatching(false)
        }
    }

    companion object {
        //Active eggBridges
        private var bridges: HashMap<Egg, EggBridgeTask> = HashMap()

        /**
         * Remove an egg from the active eggs list
         *
         * @since API 7
         */
        @JvmStatic
        fun removeEgg(e: Egg) {
            if (bridges.containsKey(e)) {
                if (bridges.get(e) != null) {
                    bridges.get(e)!!.cancel()
                }
                bridges.remove(e)
            }
        }

        /**
         * Get active egg bridges.
         * Modified  in api 11
         *
         * @since API 11
         */
        @JvmStatic
        fun getBridges(): MutableMap<Egg, EggBridgeTask> {
            return Collections.unmodifiableMap(bridges)
        }
    }
}
