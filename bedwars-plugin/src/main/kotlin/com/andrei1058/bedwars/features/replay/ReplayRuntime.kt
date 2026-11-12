package com.andrei1058.bedwars.features.replay

import com.andrei1058.bedwars.features.replay.bedwars.BedWarsBridge
import com.andrei1058.bedwars.features.replay.command.ReplayCommand
import com.andrei1058.bedwars.features.replay.config.PluginConfig
import com.andrei1058.bedwars.features.replay.config.StorageType
import com.andrei1058.bedwars.features.replay.gui.MenuListener
import com.andrei1058.bedwars.features.replay.gui.ReplayMenu
import com.andrei1058.bedwars.features.replay.gui.ReplayServices
import com.andrei1058.bedwars.features.replay.playback.LobbyBoardGuard
import com.andrei1058.bedwars.features.replay.playback.PlaybackController
import com.andrei1058.bedwars.features.replay.playback.PlaybackWorlds
import com.andrei1058.bedwars.features.replay.playback.SpectateListener
import com.andrei1058.bedwars.features.replay.record.PacketCapture
import com.andrei1058.bedwars.features.replay.record.RecordingManager
import com.andrei1058.bedwars.features.replay.record.ScoreboardRegistry
import com.andrei1058.bedwars.features.replay.settings.PlayerSettingsStore
import com.andrei1058.bedwars.features.replay.storage.FileReplayStorage
import com.andrei1058.bedwars.features.replay.storage.ReplayRepository
import com.andrei1058.bedwars.features.replay.storage.ReplayStorage
import com.andrei1058.bedwars.features.replay.storage.SqlReplayStorage
import com.andrei1058.bedwars.features.replay.util.ClientCompat
import com.github.retrooper.packetevents.PacketEvents
import org.bukkit.Bukkit
import org.bukkit.event.HandlerList
import org.bukkit.scheduler.BukkitTask
import java.io.File

/** Everything the replay feature runs while it is on. Only loaded when PacketEvents is installed. */
internal class ReplayRuntime(private val feature: ReplayFeature) {
    private val host = feature.host
    private val logger = feature.logger

    @Volatile
    lateinit var settings: PluginConfig
        private set
    lateinit var repository: ReplayRepository
        private set

    private var bridge: BedWarsBridge? = null
    private var recorder: RecordingManager? = null
    private var playback: PlaybackController? = null
    private var spectate: SpectateListener? = null
    private var playerSettingsStore: PlayerSettingsStore? = null
    private var capture: PacketCapture? = null
    private var menuListener: MenuListener? = null
    private var cleanupTask: BukkitTask? = null

    fun activeRecordings(): Int = recorder?.activeCount() ?: 0

    fun watchBlocks(blocks: Collection<org.bukkit.block.Block>) {
        recorder?.watchBlocks(blocks)
    }

    fun start() {
        settings = PluginConfig(feature.config)
        ClientCompat.mode = settings.legacyRows

        val bridge = BedWarsBridge(host)
        this.bridge = bridge
        LobbyBoardGuard.bridge = bridge

        val storage: ReplayStorage = try {
            when (settings.storageType) {
                StorageType.MYSQL -> SqlReplayStorage(settings.mysql)
                StorageType.FILE -> FileReplayStorage(File(feature.dataFolder, "replays"), logger)
            }
        } catch (t: Throwable) {
            logger.warning("Could not set up ${settings.storageType} storage (${t.message}); using local files instead.")
            FileReplayStorage(File(feature.dataFolder, "replays"), logger)
        }
        repository = ReplayRepository(host, storage) { settings }
        repository.init()

        PlaybackWorlds.cleanupLeftovers(host, settings.worldPrefix)

        val rec = RecordingManager(host, { settings }, repository, bridge)
        bridge.callbacks = rec
        bridge.register()
        rec.start()
        recorder = rec

        val pc = PacketCapture(rec, logger, settings.recordChat, settings.recordBlocks, settings.recordSounds, settings.recordTitles, settings.recordScoreboards)
        PacketEvents.getAPI().eventManager.registerListener(pc)
        capture = pc

        val playerSettings = PlayerSettingsStore(host) { settings }
        playerSettingsStore = playerSettings
        val play = PlaybackController(host, { settings }, repository, bridge, playerSettings)
        playback = play
        ReplayServices.controller = play
        ReplayServices.settings = playerSettings
        ReplayServices.repo = repository
        val menu = ReplayMenu({ settings }, repository, play)
        feature.registerEvents(play)
        val sl = SpectateListener(host, play)
        spectate = sl
        PacketEvents.getAPI().eventManager.registerListener(sl)
        val ml = MenuListener()
        menuListener = ml
        feature.registerEvents(ml)

        val cmd = ReplayCommand(feature, { menu }, play)
        for (name in feature.commandNames) {
            feature.getCommand(name)?.apply { setExecutor(cmd); tabCompleter = cmd }
        }

        scheduleCleanup()
    }

    private fun scheduleCleanup() {
        cleanupTask?.cancel()
        val period = settings.cleanupIntervalMinutes * 60L * 20L
        cleanupTask = Bukkit.getScheduler().runTaskTimer(host, Runnable { repository.cleanupExpired() }, 200L, period)
    }

    fun reloadSettings() {
        settings = PluginConfig(feature.config)
        ClientCompat.mode = settings.legacyRows
        scheduleCleanup()
    }

    /** Viewers first (so nobody loses their inventory), then recordings (saved synchronously). */
    fun stop() {
        cleanupTask?.cancel()
        playback?.shutdown()
        playerSettingsStore?.flushAll()
        spectate?.let { runCatching { PacketEvents.getAPI().eventManager.unregisterListener(it) } }
        capture?.let { runCatching { PacketEvents.getAPI().eventManager.unregisterListener(it) } }
        bridge?.unregister()
        recorder?.shutdown()
        LobbyBoardGuard.bridge = null
        ScoreboardRegistry.clear()
        if (::repository.isInitialized) repository.close()
    }
}
