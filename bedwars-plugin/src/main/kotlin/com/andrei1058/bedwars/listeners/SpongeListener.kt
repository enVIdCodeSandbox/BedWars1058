package com.andrei1058.bedwars.listeners

import com.andrei1058.bedwars.BedWars
import com.andrei1058.bedwars.api.arena.GameState
import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.arena.Arena
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityExplodeEvent

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
        val worldName = block.getWorld().getName()
        val ticks: Long = Math.max(1, BedWars.config.getYml().getInt("sponge.despawn-seconds", 3)) * 20L
        Bukkit.getScheduler().runTaskLater(BedWars.plugin, Runnable lambda1@{
            val a: IArena? = Arena.getArenaByIdentifier(worldName)
            if (a == null || !isSponge(block)) {
                return@lambda1
            }
            block.setType(Material.AIR)
            a.removePlacedBlock(block)
        }, ticks)
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
    }
}
