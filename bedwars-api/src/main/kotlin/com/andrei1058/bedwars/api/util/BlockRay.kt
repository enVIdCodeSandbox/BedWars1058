package com.andrei1058.bedwars.api.util

import java.util.NoSuchElementException
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.util.NumberConversions
import org.bukkit.util.Vector

/*
Used for tracing blocks between two vectors.
Note: this could return the same blocks multiple times.
 */
open class BlockRay(world: World, src: Vector, dst: Vector, step: Double) : Iterator<Block> {
    private val world: World
    private val delta: Vector
    private val multiple: Double
    private val parts: Int
    private var consumed: Int = 0
    private val xOffset: Double
    private val yOffset: Double
    private val zOffset: Double
    private var lcx: Double
    private var lcy: Double
    private var lcz: Double
    private var currentBlock: Int
    private val blockQueue: Array<Block>

    override fun hasNext(): Boolean {
        return consumed <= parts || currentBlock >= 0
    }

    /**
     * This could return similar blocks multiple times.
     *
     * @return Current block at the ray's position.
     */
    override fun next(): Block {
        if (!hasNext()) {
            throw NoSuchElementException("No more blocks")
        }
        if (currentBlock < 0) {
            scan()
            currentBlock = 6
        }
        return blockQueue[currentBlock--]
    }

    private fun scan() {
        val cx: Double = (multiple * delta.getX() * consumed)
        val cy: Double = (multiple * delta.getY() * consumed)
        val cz: Double = (multiple * delta.getZ() * consumed)
        val lastXFloor: Int = NumberConversions.floor(xOffset + lcx)
        val lastYFloor: Int = NumberConversions.floor(yOffset + lcy)
        val lastZFloor: Int = NumberConversions.floor(zOffset + lcz)
        val currentXFloor: Int = NumberConversions.floor(xOffset + cx)
        val currentYFloor: Int = NumberConversions.floor(yOffset + cy)
        val currentZFloor: Int = NumberConversions.floor(zOffset + cz)
        // Get the X component separately
        blockQueue[0] = world.getBlockAt(currentXFloor, lastYFloor, lastZFloor)
        // Get the Y component separately
        blockQueue[1] = world.getBlockAt(lastXFloor, currentYFloor, lastZFloor)
        // Get the Z component separately
        blockQueue[2] = world.getBlockAt(lastXFloor, lastYFloor, currentZFloor)
        // Get the XY separately
        blockQueue[3] = world.getBlockAt(currentXFloor, currentYFloor, lastZFloor)
        // Get the XZ separately
        blockQueue[4] = world.getBlockAt(currentXFloor, lastYFloor, currentZFloor)
        // Get the ZY separately
        blockQueue[5] = world.getBlockAt(lastXFloor, currentYFloor, currentZFloor)
        // Get the resultant (cx, cy, cz) together
        blockQueue[6] = world.getBlockAt(currentXFloor, currentYFloor, currentZFloor)
        lcx = cx
        lcy = cy
        lcz = cz
        consumed += 1
    }

    init {
        // How much `pov` needs to change in order to reach the target
        this.delta = Vector(dst.getBlockX() - src.getBlockX(), dst.getBlockY() - src.getBlockY(), dst.getBlockZ() - src.getBlockZ())
        if (delta.lengthSquared() == 0.0) {
            throw IllegalArgumentException("The source vector is the same as the destination vector")
        }
        this.world = world
        // Exactly from the center.
        this.xOffset = src.getBlockX() + 0.5
        this.yOffset = src.getBlockY() + 0.5
        this.zOffset = src.getBlockZ() + 0.5
        // The last vector components (last offset)
        this.lcx = 0.0
        this.lcy = 0.0
        this.lcz = 0.0
        // How much one part is worth
        this.multiple = (1 / (delta.length() / step))
        // How many sections `delta` could be split up to
        this.parts = NumberConversions.ceil(delta.length() / step)
        // 6 -> Resultant
        this.blockQueue = arrayOfNulls<Block>(7) as Array<Block>
        // The current component (gets decremented whenever next() is called)
        this.currentBlock = 6
        scan()
    }
}
