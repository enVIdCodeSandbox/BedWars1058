package com.andrei1058.bedwars.features.replay

import com.andrei1058.bedwars.BedWars
import java.io.File

/**
 * What the rest of the replay code needs to know about its host: where its files are and whether it is still running.
 * Replay is a feature of BedWars1058, so tasks and listeners belong to the BedWars1058 plugin; this object says whether
 * the feature itself is still alive (the plugin may be enabled while the feature has been switched off or is stopping).
 */
object ReplayState {
    lateinit var feature: ReplayFeature

    @Volatile
    var alive: Boolean = false

    /** False while the server (or the feature) is shutting down: work is then done directly instead of scheduled. */
    val running: Boolean get() = alive && BedWars.plugin.isEnabled

    /** `plugins/BedWars1058/Features/replay` */
    val dataFolder: File get() = feature.dataFolder
}
