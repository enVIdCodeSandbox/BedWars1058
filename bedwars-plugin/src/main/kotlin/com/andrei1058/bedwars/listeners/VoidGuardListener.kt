package com.andrei1058.bedwars.listeners

import com.andrei1058.bedwars.BedWars
import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.arena.Arena
import java.io.PrintWriter
import java.io.StringWriter
import java.util.HashSet
import org.bukkit.Chunk
import org.bukkit.ChunkSnapshot
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.WorldBorder
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.event.world.WorldInitEvent

/**
 * Two jobs for arena worlds. (1) Chunks that lie completely outside the world border (many maps have one) hold no part
 * of the map, so whatever terrain they contain is cleared as they load. (2) Diagnostic: arena maps must be created with
 * a void chunk generator. When a world that belongs to a queued arena is created without one (vanilla terrain would
 * show up around the map), this logs which code created it.
 *
 * Original description: Diagnostic only: arena maps must be created with a void chunk generator. When a world that belongs to a queued
 * arena is created without one (vanilla terrain would show up around the map), this logs which code created it.
 * Nothing is changed in the world.
 */
open class VoidGuardListener : Listener {
    private val warned: MutableSet<String> = HashSet()

    private fun isArenaWorld(world: World): Boolean {
        val name = world.getName()
        if (Arena.getArenaByIdentifier(name) != null) {
            return true
        }
        for (a in Arena.getEnableQueue()) {
            if (name.equals(a.getWorldName(), ignoreCase = true) || name.equals(a.getArenaName(), ignoreCase = true)) {
                return true
            }
        }
        return false
    }

    /**
     * Clears chunks that are entirely outside the world border (with one chunk of margin). Only applies when a real
     * border is set; the default border is so large that it never matches.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    open fun onChunkLoad(e: ChunkLoadEvent) {
        if (!BedWars.config.getYml().getBoolean("void-guard.clear-outside-border", true)) {
            return
        }
        val world = e.getWorld()
        var border: WorldBorder
        try {
            border = world.getWorldBorder()
        } catch (t: Throwable) {
            return
        }
        val size: Double = border.getSize()
        if (size >= 1.0E6 || !isArenaWorld(world)) {
            return
        }
        val c = border.getCenter()
        val half: Double = size / 2.0 + 16.0
        val chunk = e.getChunk()
        val minX: Int = chunk.getX() shl 4
        val minZ: Int = chunk.getZ() shl 4
        val outside: Boolean = minX > c.getX() + half || minX + 15 < c.getX() - half || minZ > c.getZ() + half || minZ + 15 < c.getZ() - half
        if (!outside) {
            return
        }
        // next tick: clearing inside the load event could interfere with whatever is loading the chunk
        BedWars.plugin.getServer().getScheduler().runTask(BedWars.plugin, Runnable { clear(world, chunk) })
    }

    private fun clear(world: World, chunk: Chunk) {
        if (!chunk.isLoaded()) {
            return
        }
        val snap = chunk.getChunkSnapshot()
        val bottom: Int = minHeight(world)
        val top: Int = world.getMaxHeight()
        for (x in 0 until 16) {
            for (z in 0 until 16) {
                for (y in bottom until top) {
                    val m = snap.getBlockType(x, y, z)
                    if (m != Material.AIR) {
                        chunk.getBlock(x, y, z).setType(Material.AIR, false)
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    open fun onWorldInit(e: WorldInitEvent) {
        val world = e.getWorld()
        if (world.getGenerator() != null) {
            return
        }
        val name = world.getName()
        var arenaWorld: Boolean = false
        for (a in Arena.getEnableQueue()) {
            if (name.equals(a.getWorldName(), ignoreCase = true) || name.equals(a.getArenaName(), ignoreCase = true)) {
                arenaWorld = true
                break
            }
        }
        if (!arenaWorld || !warned.add(name.lowercase())) {
            return
        }
        val trace = StringWriter()
        Throwable("world created here").printStackTrace(PrintWriter(trace))
        BedWars.plugin.getLogger().warning("The world '" + name + "' of an arena is being created WITHOUT a void chunk generator, " + "so vanilla terrain will generate around the map. Created by:\n" + trace)
    }

    companion object {
        @JvmStatic
        private fun minHeight(world: World): Int {
            try {
                return World::class.java.getMethod("getMinHeight").invoke(world) as Int
            } catch (t: Throwable) {
                return 0
            }
        }
    }
}
