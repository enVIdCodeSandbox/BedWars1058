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
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.entity.Egg
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask
import com.andrei1058.bedwars.api.util.uninitialized
import org.bukkit.util.Vector

open class EggBridgeTask : Runnable {
    private var projectile: Egg = uninitialized()
    private var teamColor: TeamColor = uninitialized()
    private var player: Player = uninitialized()
    private var arena: IArena = uninitialized()
    private var task: BukkitTask = uninitialized()
    /** Where the egg was on the previous tick. */
    private var last: Vector = uninitialized()
    /** Once the egg hit something it is simulated by hand, so that it flies on through blocks. */
    private var virtual: Boolean = false
    private var virtualVelocity: Vector = uninitialized()
    private var ticks: Int = 0
    private var finished: Boolean = false

    constructor(player: Player, projectile: Egg, teamColor: TeamColor) {
        val a: IArena? = Arena.getArenaByPlayer(player)
        if (a == null) {
            return
        }
        this.arena = a
        this.projectile = projectile
        this.teamColor = teamColor
        this.player = player
        this.last = projectile.getLocation().toVector()
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

    /**
     * The egg hit a block or an entity (and is about to disappear): carry on along its path as if nothing was in
     * the way.
     */
    open fun onProjectileHit() {
        if (virtual || finished) {
            return
        }
        virtual = true
        virtualVelocity = projectile.getVelocity().clone()
    }

    override fun run() {
        if (finished) {
            return
        }
        if (!arena.isPlayer(getPlayer()) || ++ticks > MAX_TICKS) {
            finish()
            return
        }
        val world = getPlayer().getWorld()
        var now: Vector
        if (virtual) {
            // same physics as a thrown egg: move, then drag and gravity
            now = last.clone().add(virtualVelocity)
            virtualVelocity.multiply(0.99).setY(virtualVelocity.getY() - 0.03)
        } else {
            if (getProjectile().isDead()) {
                // gone without a hit event (removed by something else): follow the last known motion
                onProjectileHit()
                if (virtualVelocity.lengthSquared() < 0.0001) {
                    finish()
                    return
                }
                return
            }
            now = getProjectile().getLocation().toVector()
        }
        val playerLoc = getPlayer().getLocation()
        val dx: Double = now.getX() - playerLoc.getX()
        val dz: Double = now.getZ() - playerLoc.getZ()
        if (Math.sqrt(dx * dx + dz * dz) > MAX_DISTANCE || now.getY() < getMinHeight(world)) {
            finish()
            return
        }
        // walk the segment last -> now so that a fast egg does not skip blocks
        val delta = now.clone().subtract(last)
        val length: Double = delta.length()
        val steps: Int = Math.max(1, Math.ceil(length / STEP).toInt())
        for (i in 1..steps) {
            val point = last.clone().add(delta.clone().multiply((i.toDouble()) / steps))
            val at = Location(world, point.getX(), point.getY(), point.getZ())
            // keep a few blocks free around the thrower, the bridge starts in front of them
            val px: Double = at.getX() - playerLoc.getX()
            val py: Double = at.getY() - playerLoc.getY()
            val pz: Double = at.getZ() - playerLoc.getZ()
            if (px * px + py * py + pz * pz <= 16.0) {
                continue
            }
            placeAround(at)
        }
        last = now
    }

    private fun placeAround(loc: Location) {
        place(loc.clone().subtract(0.0, 2.0, 0.0).getBlock())
        place(loc.clone().subtract(1.0, 2.0, 0.0).getBlock())
        place(loc.clone().subtract(0.0, 2.0, 1.0).getBlock())
    }

    /** Places one bridge block if the spot is free and not protected; otherwise it is skipped and the bridge goes on. */
    private fun place(b: Block) {
        if (b.getType() != Material.AIR) {
            return
        }
        if (Misc.isBuildProtected(b.getLocation(), getArena())) {
            return
        }
        b.setType(nms.woolMaterial())
        nms.setBlockTeamColor(b, getTeamColor())
        getArena().addPlacedBlock(b)
        Bukkit.getPluginManager().callEvent(EggBridgeBuildEvent(getTeamColor(), getArena(), b))
        b.getWorld().playEffect(b.getLocation(), nms.eggBridge(), 3)
        Sounds.playSound("egg-bridge-block", getPlayer())
    }

    private fun finish() {
        finished = true
        EggBridge.removeEgg(projectile)
        cancel()
    }

    open fun cancel() {
        finished = true
        if (task != null) {
            task.cancel()
        }
    }

    companion object {
        /** Longest horizontal distance from the thrower the bridge may reach. */
        private const val MAX_DISTANCE: Double = 30.0
        /** The bridge ends after this many ticks. */
        private const val MAX_TICKS: Int = 100
        /** Blocks are placed at least this close along the path, so a fast egg leaves no gaps. */
        private const val STEP: Double = 0.5

        @JvmStatic
        private fun getMinHeight(world: World): Int {
            try {
                return World::class.java.getMethod("getMinHeight").invoke(world) as Int
            } catch (ignored: Throwable) {
                return 0
            }
        }
    }
}
