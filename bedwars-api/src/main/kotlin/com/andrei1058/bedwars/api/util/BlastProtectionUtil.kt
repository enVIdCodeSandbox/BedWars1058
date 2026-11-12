package com.andrei1058.bedwars.api.util

import com.andrei1058.bedwars.api.BedWars
import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.api.configuration.ConfigPath
import com.andrei1058.bedwars.api.server.VersionSupport
import java.util.LinkedList
import java.util.concurrent.atomic.AtomicInteger
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.util.Vector

open class BlastProtectionUtil(private val versionSupport: VersionSupport, private val api: BedWars) {
    /**
     * Check if block is protected by blast-proof glass or an unbreakable block from a point of view
     * <p>
     * if pov is null, block is checked by {@link VersionSupport#isGlass(Material)} and {@link IArena#isBlockPlaced(Block)} instead.
     * Otherwise, a ray tracing is performed to check whether there is any glass block or an unbreakable block.
     * <p>
     * If arena is null, then glass only be checked.
     *
     * @param arena Arena instance.
     * @param pov   the point of view.
     * @param block the block instance.
     * @param step  how frequent to check the ray (0.25 - 0.5 recommended).
     * @return whether there's unbreakable block between the pov and the block
     */
    open fun isProtected(arena: IArena, pov: Location, block: Block, step: Double): Boolean {
        if (arena.isProtected(block.getLocation()) || arena.isTeamBed(block.getLocation())) {
            return true
        }
        val rayBlockedByGlass: Boolean = api.getConfigs().getMainConfig().getBoolean(ConfigPath.GENERAL_TNT_RAY_BLOCKED_BY_GLASS)
        // Trace blocks from pov to the block location
        val target = block.getLocation()
        val targetVectors: LinkedList<Vector> = LinkedList()
        val alteredRayStep: Double = 0.73
        // x
        var XrayRadius: Double = alteredRayStep * -1
        while (XrayRadius <= alteredRayStep) {
            // y
            var YrayRadius: Double = alteredRayStep * -1
            while (YrayRadius <= alteredRayStep) {
                // z
                var ZrayRadius: Double = alteredRayStep * -1
                while (ZrayRadius <= alteredRayStep) {
                    targetVectors.add(pov.clone().toVector().toBlockVector().add(Vector(XrayRadius, YrayRadius, ZrayRadius)))
                    ZrayRadius += alteredRayStep
                }
                YrayRadius += alteredRayStep
            }
            XrayRadius += alteredRayStep
        }
        val protectedTimes = AtomicInteger()
        val totalRays: Int = targetVectors.size
        targetVectors.forEach(lambda4@{ targetVector ->
            var ray: BlockRay
            try {
                ray = BlockRay(block.getWorld(), targetVector, target.toVector(), step)
            } catch (ignored: IllegalArgumentException) {
                return@lambda4
            }
            while (ray.hasNext()) {
                val nextBlock = ray.next()
                if (nextBlock.getType() == Material.AIR) {
                    continue
                }
                if (rayBlockedByGlass && versionSupport.isGlass(nextBlock.getType())) {
                    // If a block is a glass
                    protectedTimes.getAndIncrement()
                    return@lambda4
                }
                if (!arena.isBlockPlaced(nextBlock) && !arena.isAllowMapBreak()) {
                    protectedTimes.getAndIncrement()
                    return@lambda4
                }
            }
        })
        return totalRays - protectedTimes.get() < 6
    }
}
