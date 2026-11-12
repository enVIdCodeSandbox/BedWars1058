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

package com.andrei1058.bedwars.arena.tasks

import com.andrei1058.bedwars.BedWars
import com.andrei1058.bedwars.api.arena.generator.IGenerator
import com.andrei1058.bedwars.api.arena.shop.ShopHolo
import com.andrei1058.bedwars.api.arena.team.ITeam
import com.andrei1058.bedwars.api.configuration.ConfigPath
import com.andrei1058.bedwars.api.server.ServerType
import com.andrei1058.bedwars.api.tasks.RestartingTask
import com.andrei1058.bedwars.arena.Arena
import com.andrei1058.bedwars.arena.Misc
import com.andrei1058.bedwars.configuration.Sounds
import com.andrei1058.bedwars.support.paper.TeleportManager
import java.util.ArrayList
import java.util.Random
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitTask
import com.andrei1058.bedwars.api.util.unchecked

open class GameRestartingTask(private var arena: Arena?) : Runnable, RestartingTask {
    private var restarting: Int = BedWars.config.getInt(ConfigPath.GENERAL_CONFIGURATION_RESTART) + 5
    private val task: BukkitTask

    /**
     * Get task ID
     */
    override fun getTask(): Int {
        return task.getTaskId()
    }

    override fun getRestarting(): Int {
        return restarting
    }

    override fun getArena(): Arena? {
        return arena
    }

    override fun getBukkitTask(): BukkitTask {
        return task
    }

    override fun run() {
        restarting--
        if (getArena()!!.getPlayers()!!.isEmpty() && restarting > 9) {
            restarting = 9
        }
        if (restarting == 7) {
            for (on in ArrayList(getArena()!!.getPlayers())) {
                getArena()!!.removePlayer(on, BedWars.getServerType() == ServerType.BUNGEE)
            }
            for (on in ArrayList(getArena()!!.getSpectators())) {
                getArena()!!.removeSpectator(on, BedWars.getServerType() == ServerType.BUNGEE)
            }
        } else if (restarting == 4) {
            ShopHolo.clearForArena(getArena().unchecked())
            for (e in getArena()!!.getWorld()!!.getEntities()) {
                if (e.getType() == EntityType.PLAYER) {
                    val p = e as Player
                    Misc.moveToLobbyOrKick(p, getArena(), true)
                    if (getArena()!!.isSpectator(p)) {
                        getArena()!!.removeSpectator(p, false)
                    }
                    if (getArena()!!.isPlayer(p)) {
                        getArena()!!.removePlayer(p, false)
                    }
                }
            }
            for (eg in getArena()!!.getOreGenerators()!!) {
                eg.disable()
            }
            for (t in getArena()!!.getTeams()!!) {
                for (eg in t.getGenerators()) {
                    eg.disable()
                }
            }
        } else if (restarting == 0) {
            getArena()!!.restart()
            task.cancel()
            arena = null
        }
    }

    override fun cancel() {
        task.cancel()
    }

    init {
        task = Bukkit.getScheduler().runTaskTimer(BedWars.plugin, this, 0, 20L)
        Sounds.playSound("game-end", arena!!.getPlayers().unchecked())
        Sounds.playSound("game-end", arena!!.getSpectators().unchecked())
        // teleport to alive players
        if (arena!!.getConfig()!!.getGameOverridableBoolean(ConfigPath.GENERAL_GAME_END_TELEPORT_ELIMINATED)) {
            if (!arena!!.getPlayers()!!.isEmpty()) {
                val r = Random()
                for (spectator in arena!!.getSpectators()!!) {
                    val target = arena!!.getPlayers()!!.get(r.nextInt(arena!!.getPlayers()!!.size))
                    val loc = target.getLocation().clone()
                    loc.setDirection(target.getLocation().getDirection().multiply(-1))
                    loc.add(0.0, 2.0, 0.0)
                    TeleportManager.teleportC(spectator, loc, PlayerTeleportEvent.TeleportCause.PLUGIN)
                }
            }
        }
        // show eliminated players
        if (arena!!.getConfig()!!.getGameOverridableBoolean(ConfigPath.GENERAL_GAME_END_SHOW_ELIMINATED)) {
            for (spectator in arena!!.getSpectators()!!) {
                val exTeam: ITeam? = arena!!.getExTeam(spectator.getUniqueId())
                if (null == exTeam) {
                    continue
                }
                spectator.removePotionEffect(PotionEffectType.INVISIBILITY)
                for (player in arena!!.getPlayers()!!) {
                    BedWars.nms.spigotShowPlayer(player, spectator)
                    BedWars.nms.spigotShowPlayer(spectator, player)
                }
            }
        }
    }
}
