package com.andrei1058.bedwars.features.easysetup.infer

/** Read-only view of the blocks of a map. Out-of-range or unloaded space must read as AIR. */
fun interface BlockGrid {
    fun kind(x: Int, y: Int, z: Int): Kind
}
