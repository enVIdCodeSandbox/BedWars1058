package com.andrei1058.bedwars.features

import com.andrei1058.bedwars.BedWars
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.logging.Level
import java.util.logging.LogRecord
import java.util.logging.Logger
import org.bukkit.Server
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.PluginCommand
import org.bukkit.command.TabExecutor
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.plugin.PluginDescriptionFile

/**
 * A built-in BedWars1058 feature (formerly a separate addon plugin).
 *
 * Every feature owns the folder `plugins/BedWars1058/Features/<id>/` with its own `config.yml` (and any other data
 * it keeps). The members mirror the ones a `Feature` offers (`config`, `logger`, `dataFolder`, `saveDefaultConfig()`
 * ...) but a feature is not a Bukkit plugin: listeners, tasks and commands belong to BedWars1058 itself ([host]).
 */
abstract class Feature(
    /** Folder name and key in `Features/features.yml`. */
    val id: String,
    /** Name shown in logs and in `/bw features`. */
    val displayName: String,
    /** Names of the standalone addon jars this feature replaces. */
    val legacyPluginNames: List<String> = emptyList(),
    /** Commands (declared in plugin.yml) that belong to this feature. */
    val commandNames: List<String> = emptyList()
) : TabExecutor {
    /** The BedWars1058 plugin. Use it wherever Bukkit asks for a `Plugin`. */
    val host: BedWars get() = BedWars.plugin
    val server: Server get() = host.server
    val name: String get() = displayName
    val description: PluginDescriptionFile get() = host.description

    /** `plugins/BedWars1058/Features/<id>` */
    val dataFolder: File by lazy { File(FeatureManager.featuresFolder, id) }

    val logger: Logger by lazy { FeatureLogger(host.logger, displayName) }

    /** True between a successful [onEnable] and [onDisable]. */
    var isEnabled: Boolean = false
        internal set

    private val listeners = ArrayList<Listener>()
    private var loadedConfig: FileConfiguration? = null
    private val configFile: File get() = File(dataFolder, "config.yml")

    /**
     * Early features are enabled before BedWars1058 enables itself (they prepare files or hooks the core reads while
     * starting) and are disabled after it. All other features start once the core is fully enabled.
     */
    open val early: Boolean get() = false

    /** Called at the very start of BedWars1058's onLoad, before it reads its configuration. */
    open fun onLoad() {}

    /** Called once BedWars1058 is fully enabled. */
    open fun onEnable() {}

    open fun onDisable() {}

    /** Default handler of the feature's commands, used when [onEnable] did not set another executor. */
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean = false

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): List<String>? = null

    // ------------------------------------------------------------------ configuration (own file per feature)

    val config: FileConfiguration
        get() {
            if (loadedConfig == null) reloadConfig()
            return loadedConfig!!
        }

    fun reloadConfig() {
        val yml = YamlConfiguration.loadConfiguration(configFile)
        getResource("config.yml")?.use { stream ->
            yml.setDefaults(YamlConfiguration.loadConfiguration(InputStreamReader(stream, StandardCharsets.UTF_8)))
        }
        loadedConfig = yml
    }

    fun saveConfig() {
        try {
            config.save(configFile)
        } catch (e: Exception) {
            logger.log(Level.SEVERE, "Could not save " + configFile, e)
        }
    }

    fun saveDefaultConfig() {
        if (!configFile.exists()) saveResource("config.yml", false)
    }

    /** A file bundled in the jar under `features/<id>/`. */
    fun getResource(path: String): InputStream? = host.getResource("features/$id/" + path.replace('\\', '/'))

    /** Copies a bundled file into [dataFolder]. */
    fun saveResource(path: String, replace: Boolean) {
        val clean = path.replace('\\', '/')
        val input = getResource(clean) ?: throw IllegalArgumentException("The embedded resource '$clean' cannot be found for feature $id")
        val out = File(dataFolder, clean)
        if (out.exists() && !replace) {
            input.close()
            return
        }
        out.parentFile?.mkdirs()
        input.use { i -> out.outputStream().use { o -> i.copyTo(o) } }
    }

    // ------------------------------------------------------------------ Bukkit glue

    /** Commands are declared in BedWars1058's plugin.yml. */
    fun getCommand(name: String): PluginCommand? = host.getCommand(name)

    /** Registers a listener that is removed again when the feature is disabled. */
    fun registerEvents(listener: Listener) {
        server.pluginManager.registerEvents(listener, host)
        listeners.add(listener)
    }

    /** Turns this feature off (what a standalone addon did with `disablePlugin(this)`). */
    fun disableSelf(reason: String? = null) {
        FeatureManager.disable(this, reason)
    }

    internal fun unregisterListeners() {
        for (l in listeners) HandlerList.unregisterAll(l)
        listeners.clear()
        if (this is Listener) HandlerList.unregisterAll(this)
    }
}

/** Logs through BedWars1058's logger with the feature name in front. */
private class FeatureLogger(parent: Logger, feature: String) : Logger("BedWars1058." + feature, null) {
    private val prefix = "[BedWars1058] [$feature] "

    init {
        setParent(parent)
        level = Level.ALL
    }

    override fun log(record: LogRecord) {
        record.message = prefix + record.message
        super.log(record)
    }
}
