package com.andrei1058.bedwars.arena.upgrades

import com.andrei1058.bedwars.BedWars
import com.andrei1058.bedwars.BedWars.Companion.config
import com.andrei1058.bedwars.BedWars.Companion.plugin
import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.api.arena.team.ITeam
import com.andrei1058.bedwars.api.configuration.ConfigPath
import java.util.ArrayList
import java.util.Random
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import com.andrei1058.bedwars.api.util.uninitialized

open class HealPoolTask : BukkitRunnable {
    private var bwt: ITeam? = null
    private var maxX: Int = 0
    private var minX: Int = 0
    private var maxY: Int = 0
    private var minY: Int = 0
    private var maxZ: Int = 0
    private var minZ: Int = 0
    private var arena: IArena? = null
    private var r: Random = Random()
    private var l: Location = uninitialized()

    constructor(bwt: ITeam?) : super() {
        this.bwt = bwt
        if (bwt == null || bwt.getSpawn() == null) {
            removeForTeam(this.bwt)
            cancel()
            return
        }
        val radius: Int = bwt.getArena()!!.getConfig()!!.getInt(ConfigPath.ARENA_ISLAND_RADIUS)
        val teamspawn = bwt.getSpawn()
        this.maxX = (teamspawn!!.getBlockX() + radius)
        this.minX = (teamspawn!!.getBlockX() - radius)
        this.maxY = (teamspawn!!.getBlockY() + radius)
        this.minY = (teamspawn!!.getBlockY() - radius)
        this.maxZ = (teamspawn!!.getBlockZ() + radius)
        this.minZ = (teamspawn!!.getBlockZ() - radius)
        this.arena = bwt.getArena()
        this.runTaskTimerAsynchronously(plugin, 0, 80L)
        healPoolTasks!!.add(this)
    }

    override fun run() {
        //null checks
        if ((bwt == null) || (bwt!!.getSpawn() == null) || (arena == null)) {
            healPoolTasks!!.remove(this)
            return
        }
        for (x in minX..maxX) {
            for (y in minY..maxY) {
                for (z in minZ..maxZ) {
                    l = Location(arena!!.getWorld(), x + 0.5, y + 0.5, z + 0.5)
                    if (l.getBlock().getType() != Material.AIR) {
                        continue
                    }
                    val chance: Int = r.nextInt(9)
                    if (chance == 0) {
                        if (config.getBoolean(ConfigPath.GENERAL_CONFIGURATION_HEAL_POOL_SEEN_TEAM_ONLY)) {
                            for (p in bwt!!.getMembers()!!) {
                                BedWars.nms.playVillagerEffect(p, l)
                            }
                        } else {
                            for (p in arena!!.getPlayers()!!) {
                                BedWars.nms.playVillagerEffect(p, l)
                            }
                        }
                    }
                }
            }
        }
    }

    open fun getBwt(): ITeam? {
        return bwt
    }

    open fun getArena(): IArena? {
        return arena
    }

    companion object {
        private var healPoolTasks: MutableList<HealPoolTask>? = ArrayList()

        @JvmStatic
        fun exists(arena: IArena, bwt: ITeam): Boolean {
            if (healPoolTasks!!.isEmpty()) {
                return false
            }
            for (hpt in healPoolTasks!!) {
                if (hpt.getArena() === arena && hpt.getBwt() === bwt) {
                    return true
                }
            }
            return false
        }

        @JvmStatic
        fun removeForArena(a: IArena?) {
            if (healPoolTasks!!.isEmpty() || a == null) {
                return
            }
            for (hpt in healPoolTasks!!) {
                if (hpt == null) {
                    continue
                }
                if (hpt.getArena() == a) {
                    hpt.cancel()
                    healPoolTasks!!.remove(hpt)
                }
            }
        }

        @JvmStatic
        fun removeForArena(a: String?) {
            if (healPoolTasks == null || healPoolTasks!!.isEmpty() || (a == null)) {
                return
            }
            for (hpt in healPoolTasks) {
                if (hpt == null) {
                    continue
                }
                if (hpt.getArena()!!.getWorldName() == a) {
                    hpt.cancel()
                    healPoolTasks!!.remove(hpt)
                }
            }
        }

        @JvmStatic
        fun removeForTeam(team: ITeam?) {
            if (healPoolTasks == null || healPoolTasks!!.isEmpty() || (team == null)) {
                return
            }
            for (hpt in healPoolTasks) {
                if (hpt == null) {
                    continue
                }
                if (hpt.getBwt() == team) {
                    hpt.cancel()
                    healPoolTasks!!.remove(hpt)
                }
            }
        }
    }
}
