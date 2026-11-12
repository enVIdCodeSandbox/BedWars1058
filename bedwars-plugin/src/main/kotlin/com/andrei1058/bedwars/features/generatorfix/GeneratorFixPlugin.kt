package com.andrei1058.bedwars.features.generatorfix

import com.andrei1058.bedwars.features.Feature
import com.andrei1058.bedwars.features.FeatureManager

/**
 * Keeps the blocks below every team generator when sudden death starts.
 *
 * Without this feature the game sets the 20 blocks below each team generator to air the moment the dragons
 * spawn, which leaves a hole the resources fall through. While the feature is on, the game task simply skips that
 * step (see GamePlayingTask), so nothing has to be saved and restored any more.
 */
class GeneratorFixPlugin : Feature("generatorfix", "GeneratorFix", listOf("BedWars1058-GeneratorFix"), listOf()) {

    var debug: Boolean = false
        private set

    override fun onEnable() {
        saveDefaultConfig()
        reloadConfig()
        keepBase = config.getBoolean("keep-generator-base", true)
        debug = config.getBoolean("debug", false)
        logger.info(if (keepBase) "Generator bases are kept intact at sudden death." else "keep-generator-base is false: generator bases are removed at sudden death.")
    }

    override fun onDisable() {
        keepBase = false
    }

    companion object {
        @Volatile
        private var keepBase = false

        /** Asked by the game task right before it would clear the blocks below the team generators. */
        @JvmStatic
        fun keepsGeneratorBase(arenaName: String?): Boolean {
            val f = FeatureManager.enabled<GeneratorFixPlugin>() ?: return false
            if (!keepBase) return false
            if (f.debug) f.logger.info("Sudden death in '$arenaName': generator bases left untouched")
            return true
        }
    }
}
