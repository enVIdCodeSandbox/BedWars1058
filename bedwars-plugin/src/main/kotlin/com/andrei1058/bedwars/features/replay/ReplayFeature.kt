package com.andrei1058.bedwars.features.replay

import com.andrei1058.bedwars.features.Feature
import com.andrei1058.bedwars.features.FeatureManager
import com.andrei1058.bedwars.features.replay.config.PluginConfig
import com.andrei1058.bedwars.features.replay.storage.ReplayRepository

/**
 * Hypixel-style automatic match replays: every match is recorded and can be watched back in a private copy of the map.
 *
 * This class only checks the one requirement (the PacketEvents plugin) and hands over to [ReplayRuntime]. Everything
 * that touches PacketEvents classes lives in the runtime, so a server without PacketEvents can still load this class and
 * simply keeps the feature off.
 */
class ReplayFeature : Feature(
    "replay", "Replay",
    listOf("BedWars1058-Replay"),
    listOf("replay", "replaysjoin", "replaytransfer"),
) {
    @Volatile
    private var runtime: ReplayRuntime? = null

    val settings: PluginConfig get() = runtime!!.settings
    val repository: ReplayRepository get() = runtime!!.repository

    /** The recordings that are running right now (0 when the feature is off). */
    fun activeRecordings(): Int = runtime?.activeRecordings() ?: 0

    override fun onEnable() {
        val pe = server.pluginManager.getPlugin("packetevents")
        if (pe == null || !pe.isEnabled) {
            logger.warning("The replay feature needs the PacketEvents plugin (https://github.com/retrooper/packetevents). " +
                "Install it and restart; replays stay off until then.")
            disableSelf("the PacketEvents plugin is not installed")
            return
        }
        saveDefaultConfig()
        ReplayState.feature = this
        val rt = ReplayRuntime(this)
        try {
            rt.start()
        } catch (t: Throwable) {
            rt.stop()
            throw t
        }
        runtime = rt
        ReplayState.alive = true
        logger.info("Enabled (${rt.settings.storageType.name.lowercase()} storage).")
    }

    override fun onDisable() {
        ReplayState.alive = false
        runtime?.stop()
        runtime = null
    }

    companion object {
        /**
         * For the other features: tell the recorder that [blocks] are about to change without a Bukkit event, so the
         * replay shows the change. Call it right before changing them. Does nothing when replays are off.
         */
        @JvmStatic
        fun blocksChanging(blocks: Collection<org.bukkit.block.Block>) {
            FeatureManager.enabled<ReplayFeature>()?.runtime?.watchBlocks(blocks)
        }

        @JvmStatic
        fun blockChanging(block: org.bukkit.block.Block) = blocksChanging(listOf(block))
    }

    /** Re-reads Features/replay/config.yml. Storage type changes need a restart. */
    fun reloadSettings() {
        reloadConfig()
        runtime?.reloadSettings()
    }
}
