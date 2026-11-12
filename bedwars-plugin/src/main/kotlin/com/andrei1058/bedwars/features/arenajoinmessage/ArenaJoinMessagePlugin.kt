package com.andrei1058.bedwars.features.arenajoinmessage

import com.andrei1058.bedwars.api.BedWars
import com.andrei1058.bedwars.features.arenajoinmessage.command.AjmCommand
import com.andrei1058.bedwars.features.arenajoinmessage.listener.ArenaJoinListener
import com.andrei1058.bedwars.features.arenajoinmessage.notify.JoinNotifier
import com.andrei1058.bedwars.features.arenajoinmessage.settings.PlayerPrefs
import com.andrei1058.bedwars.features.arenajoinmessage.settings.PluginSettings
import org.bukkit.configuration.file.YamlConfiguration
import com.andrei1058.bedwars.features.Feature
import java.io.File
import java.util.logging.Level

class ArenaJoinMessagePlugin : Feature("arenajoinmessage", "ArenaJoinMessage", listOf("BedWars1058-ArenaJoinMessage"), listOf("ajm")) {

    /**
     * The BedWars1058 API, or null if BedWars1058 has not registered it (yet).
     * Looked up every time (a cheap map lookup) instead of cached, so reloading BedWars1058 with a plugin
     * manager can never leave us holding the API object of the old, disabled instance.
     */
    val bedWars: BedWars?
        get() = server.servicesManager.getRegistration(BedWars::class.java)?.provider

    // Volatile: replaced on reload while other code may be reading it.
    @Volatile
    lateinit var settings: PluginSettings
        private set
    lateinit var prefs: PlayerPrefs
        private set
    lateinit var notifier: JoinNotifier
        private set

    override fun onEnable() {
        saveDefaultConfig()
        settings = PluginSettings.load(config, logger)
        prefs = PlayerPrefs(this).also { it.load() }
        notifier = JoinNotifier(this)

        server.pluginManager.registerEvents(ArenaJoinListener(this), this.host)

        val command = getCommand("ajm")
        if (command != null) {
            val handler = AjmCommand(this)
            command.setExecutor(handler)
            command.tabCompleter = handler
        }

        // BedWars1058 normally registers its API while it enables (before us because of "depend"),
        // but check again once every plugin is up and say so loudly if it is still missing.
        server.scheduler.runTask(this.host, Runnable {
            val api = bedWars
            if (api == null) {
                logger.severe("BedWars1058 API not found. Is BedWars1058 installed and enabled? No messages will be sent.")
                return@Runnable
            }
            try {
                if (api.getServerType()?.name == "BUNGEE") {
                    logger.warning("BedWars1058 runs in BUNGEE mode: everyone on this server is inside the one arena, " +
                        "so nobody will receive join messages. This plugin is meant for MULTIARENA / SHARED servers.")
                }
            } catch (ignored: LinkageError) {
                // very old API without getServerType()
            }
        })

        logger.info("Enabled. Arena join messages are ${if (settings.enabled) "ON" else "OFF"}.")
    }

    override fun onDisable() {
        if (::notifier.isInitialized) notifier.shutdown()
    }

    /**
     * Re-reads config.yml and data.yml. Safe to call at any time.
     * Returns null on success, or a short error text. On an error the previous settings stay active.
     */
    fun reloadAll(): String? {
        saveDefaultConfig() // recreates config.yml if somebody deleted it

        // Bukkit's reloadConfig() silently swaps a broken file for an empty one, so check the file first.
        try {
            YamlConfiguration().load(File(dataFolder, "config.yml"))
        } catch (e: Exception) {
            val reason = (e.message ?: e.javaClass.simpleName).lineSequence().first().trim()
            logger.severe("config.yml could not be loaded, keeping the previous settings: $reason")
            return reason
        }

        reloadConfig()
        settings = try {
            PluginSettings.load(config, logger)
        } catch (e: Exception) {
            logger.log(Level.SEVERE, "config.yml has an invalid value, keeping the previous settings", e)
            return e.message ?: e.javaClass.simpleName
        }
        prefs.load()
        notifier.clearCooldowns()
        return null
    }
}
