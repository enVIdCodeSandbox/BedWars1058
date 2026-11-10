package com.andrei1058.bedwars.listeners

import com.andrei1058.bedwars.BedWars
import com.andrei1058.bedwars.api.arena.GameState
import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.arena.Arena
import java.lang.reflect.Method
import java.util.HashMap
import org.bukkit.Bukkit
import org.bukkit.Effect
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.scheduler.BukkitTask

/**
 * Sponges are for soaking up water, not for building: a placed sponge disappears after a few seconds
 * ({@code sponge.despawn-seconds} in the main config) and can never be picked up again.
 */
open class SpongeListener : Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    open fun onPlace(e: BlockPlaceEvent) {
        if (!isSponge(e.getBlockPlaced().getType())) {
            return
        }
        val arena: IArena? = Arena.getArenaByPlayer(e.getPlayer())
        if (arena == null || arena.getStatus() != GameState.playing) {
            return
        }
        val block = e.getBlockPlaced()
        // a sponge placed on the spot of an earlier one starts its own countdown from zero
        stop(block)
        val c = Countdown(block, Math.max(1, BedWars.config.getYml().getInt("sponge.despawn-seconds", 3)) * 20)
        ACTIVE.put(key(block), c)
        c.start()
    }

    /** Burns while it waits to disappear, then removes the sponge. */
    class Countdown(@JvmField val block: Block, @JvmField val totalTicks: Int) : Runnable {
        @JvmField var elapsed: Int = 0
        @JvmField var task: BukkitTask? = null

        fun start() {
            task = Bukkit.getScheduler().runTaskTimer(BedWars.plugin, this, 0L, PERIOD)
        }

        fun cancel() {
            if (task != null) {
                task.cancel()
            }
        }

        override fun run() {
            val k = key(block)
            if (ACTIVE.get(k) !== this) {
                // replaced or stopped
                cancel()
                return
            }
            val a: IArena? = Arena.getArenaByIdentifier(block.getWorld().getName())
            if (a == null || !isSponge(block)) {
                ACTIVE.remove(k)
                cancel()
                return
            }
            if (elapsed >= totalTicks) {
                ACTIVE.remove(k)
                cancel()
                block.setType(Material.AIR)
                a.removePlacedBlock(block)
                return
            }
            flames(block, elapsed / (totalTicks.toDouble()))
            elapsed += PERIOD
        }

        companion object {
            const val PERIOD: Int = 3
        }
    }

    /** A placed sponge that gets broken is simply gone, it does not drop an item. */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    open fun onBreak(e: BlockBreakEvent) {
        val b = e.getBlock()
        if (!isSponge(b)) {
            return
        }
        val arena: IArena? = Arena.getArenaByIdentifier(b.getWorld().getName())
        if (arena == null || !arena.isBlockPlaced(b)) {
            return
        }
        e.setCancelled(true)
        stop(b)
        b.setType(Material.AIR)
        arena.removePlacedBlock(b)
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    open fun onExplode(e: EntityExplodeEvent) {
        val arena: IArena? = Arena.getArenaByIdentifier(e.getLocation().getWorld().getName())
        if (arena == null) {
            return
        }
        val it = e.blockList().iterator()
        while (it.hasNext()) {
            val b = it.next()
            if (isSponge(b) && arena.isBlockPlaced(b)) {
                it.remove()
                stop(b)
                b.setType(Material.AIR)
                arena.removePlacedBlock(b)
            }
        }
    }

    companion object {
        @JvmStatic
        private fun isSponge(b: Block): Boolean {
            val n = b.getType().name()
            return n == "SPONGE" || n == "WET_SPONGE"
        }

        @JvmStatic
        private fun isSponge(m: Material): Boolean {
            return m.name() == "SPONGE" || m.name() == "WET_SPONGE"
        }
        /** Sponges that are counting down, by block position. */
        private val ACTIVE: MutableMap<String, Countdown> = HashMap()

        @JvmStatic
        private fun key(b: Block): String {
            return b.getWorld().getName() + ":" + b.getX() + ":" + b.getY() + ":" + b.getZ()
        }

        @JvmStatic
        private fun stop(b: Block) {
            val c: Countdown? = ACTIVE.remove(key(b))
            if (c != null) {
                c.cancel()
            }
        }
        private var spawnParticle: Method? = null
        private lateinit var flame: Any
        private var particleLookup: Boolean = false

        /** Fire particles around the block; more of them the closer the sponge is to disappearing. */
        @JvmStatic
        private fun flames(b: Block, progress: Double) {
            val w = b.getWorld()
            val count: Int = 2 + ((progress * 6).toInt())
            if (!particleLookup) {
                particleLookup = true
                try {
                    val particle = Class.forName("org.bukkit.Particle")
                    flame = particle.getField("FLAME").get(null)
                    spawnParticle = World::class.java.getMethod("spawnParticle", particle, Location::class.java, Int::class.javaPrimitiveType, Double::class.javaPrimitiveType, Double::class.javaPrimitiveType, Double::class.javaPrimitiveType, Double::class.javaPrimitiveType)
                } catch (ignored: Throwable) {
                    // old server: the classic flame effect is used instead
                    spawnParticle = null
                }
            }
            val center = b.getLocation().add(0.5, 0.5, 0.5)
            for (i in 0 until count) {
                val at = center.clone().add((Math.random() - 0.5) * 1.2, (Math.random() - 0.5) * 1.2, (Math.random() - 0.5) * 1.2)
                if (spawnParticle != null) {
                    try {
                        spawnParticle.invoke(w, flame, at, 1, 0.0, 0.0, 0.0, 0.0)
                        continue
                    } catch (ignored: Throwable) {
                        spawnParticle = null
                    }
                }
                w.playEffect(at, Effect.MOBSPAWNER_FLAMES, 0)
            }
        }
    }
}
