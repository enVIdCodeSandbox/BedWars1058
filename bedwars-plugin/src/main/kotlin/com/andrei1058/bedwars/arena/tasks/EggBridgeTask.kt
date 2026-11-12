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
import com.andrei1058.bedwars.BedWars.Companion.nms
import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.api.arena.team.TeamColor
import com.andrei1058.bedwars.api.events.gameplay.EggBridgeBuildEvent
import com.andrei1058.bedwars.arena.Arena
import com.andrei1058.bedwars.arena.Misc
import com.andrei1058.bedwars.configuration.Sounds
import com.andrei1058.bedwars.listeners.EggBridge
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Egg
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask
import com.andrei1058.bedwars.api.util.uninitialized

open class EggBridgeTask : Runnable {
    private var projectile: Egg = uninitialized()
    private var teamColor: TeamColor = uninitialized()
    private var player: Player = uninitialized()
    private var arena: IArena = uninitialized()
    private var task: BukkitTask = uninitialized()

    constructor(player: Player, projectile: Egg, teamColor: TeamColor) {
        val a: IArena? = Arena.getArenaByPlayer(player)
        if (a == null) {
            return
        }
        this.arena = a
        this.projectile = projectile
        this.teamColor = teamColor
        this.player = player
        task = Bukkit.getScheduler().runTaskTimer(BedWars.plugin, this, 0, 1)
    }

    open fun getTeamColor(): TeamColor {
        return teamColor
    }

    open fun getProjectile(): Egg {
        return projectile
    }

    open fun getPlayer(): Player {
        return player
    }

    open fun getArena(): IArena {
        return arena
    }

    override fun run() {
        val loc = getProjectile().getLocation()
        if (getProjectile().isDead() || !arena.isPlayer(getPlayer()) || getPlayer().getLocation().distance(getProjectile().getLocation()) > 27 || getPlayer().getLocation().getY() - getProjectile().getLocation().getY() > 9) {
            EggBridge.removeEgg(projectile)
            return
        }
        if (getPlayer().getLocation().distance(loc) > 4.0) {
            val b2 = loc.clone().subtract(0.0, 2.0, 0.0).getBlock()
            if (!Misc.isBuildProtected(b2.getLocation(), getArena())) {
                if (b2.getType() == Material.AIR) {
                    b2.setType(nms.woolMaterial())
                    nms.setBlockTeamColor(b2, getTeamColor())
                    getArena().addPlacedBlock(b2)
                    Bukkit.getPluginManager().callEvent(EggBridgeBuildEvent(getTeamColor(), getArena(), b2))
                    loc.getWorld().playEffect(b2.getLocation(), nms.eggBridge(), 3)
                    Sounds.playSound("egg-bridge-block", getPlayer())
                }
            }
            val b3 = loc.clone().subtract(1.0, 2.0, 0.0).getBlock()
            if (!Misc.isBuildProtected(b3.getLocation(), getArena())) {
                if (b3.getType() == Material.AIR) {
                    b3.setType(nms.woolMaterial())
                    nms.setBlockTeamColor(b3, getTeamColor())
                    getArena().addPlacedBlock(b3)
                    Bukkit.getPluginManager().callEvent(EggBridgeBuildEvent(getTeamColor(), getArena(), b3))
                    loc.getWorld().playEffect(b3.getLocation(), nms.eggBridge(), 3)
                    Sounds.playSound("egg-bridge-block", getPlayer())
                }
            }
            val b4 = loc.clone().subtract(0.0, 2.0, 1.0).getBlock()
            if (!Misc.isBuildProtected(b4.getLocation(), getArena())) {
                if (b4.getType() == Material.AIR) {
                    b4.setType(nms.woolMaterial())
                    nms.setBlockTeamColor(b4, getTeamColor())
                    getArena().addPlacedBlock(b4)
                    Bukkit.getPluginManager().callEvent(EggBridgeBuildEvent(getTeamColor(), getArena(), b4))
                    loc.getWorld().playEffect(b4.getLocation(), nms.eggBridge(), 3)
                    Sounds.playSound("egg-bridge-block", getPlayer())
                }
            }
        }
    }

    open fun cancel() {
        task.cancel()
    }
}
