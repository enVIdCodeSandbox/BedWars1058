package com.andrei1058.bedwars.features.easysetup.scan

import com.andrei1058.bedwars.features.easysetup.infer.BlockGrid
import com.andrei1058.bedwars.features.easysetup.infer.Kind

/** Adapts a chunk-snapshot view of the world to the version-independent detection algorithms. */
class WorldGrid(private val snap: WorldSnap) : BlockGrid {
    override fun kind(x: Int, y: Int, z: Int): Kind = Mats.kind(snap.type(x, y, z))
}
