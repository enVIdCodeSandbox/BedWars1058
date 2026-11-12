package com.andrei1058.bedwars.features

import com.andrei1058.bedwars.BedWars
import java.io.File
import java.util.logging.Level
import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration

/**
 * Loads, enables and disables the built-in features.
 *
 * `plugins/BedWars1058/Features/features.yml` is the switchboard (one `true`/`false` per feature).
 * Each feature keeps its settings in its own `Features/<id>/config.yml`.
 */
object FeatureManager {
    private val registered = LinkedHashMap<String, Feature>()
    private val active = LinkedHashSet<Feature>()
    private val skipped = LinkedHashMap<String, String>()

    /**
     * True once BedWars1058 finished its own start-up (arenas, restore adapter, API). Early features wait for this
     * (or for the PluginEnableEvent that follows) before they hook into the core.
     */
    @JvmStatic
    @Volatile
    var coreReady: Boolean = false
        private set

    @JvmStatic
    val featuresFolder: File get() = File(BedWars.plugin.dataFolder, "Features")

    private val switchboardFile: File get() = File(featuresFolder, "features.yml")

    /** Every feature in registration order. */
    @JvmStatic
    fun all(): Collection<Feature> = registered.values

    @JvmStatic
    fun get(id: String): Feature? = registered[id.lowercase()]

    /** The enabled feature of type [T], or null when it is switched off. */
    inline fun <reified T : Feature> enabled(): T? = all().firstOrNull { it is T && it.isEnabled } as T?

    /** The feature of type [T] whether it is enabled or not. */
    inline fun <reified T : Feature> find(): T? = all().firstOrNull { it is T } as T?

    @JvmStatic
    fun isEnabled(id: String): Boolean = get(id)?.isEnabled == true

    /** Why a feature is not running, for `/bw features`. */
    @JvmStatic
    fun skipReason(id: String): String? = skipped[id.lowercase()]

    @JvmStatic
    fun register(feature: Feature) {
        registered[feature.id.lowercase()] = feature
    }

    /** Reads the switchboard and runs [Feature.onLoad]. Called from the plugin's onLoad. */
    @JvmStatic
    fun loadAll() {
        if (registered.isEmpty()) BuiltInFeatures.registerAll()
        featuresFolder.mkdirs()
        val yml = YamlConfiguration.loadConfiguration(switchboardFile)
        yml.options().header(
            "Built-in BedWars1058 features. Set one to false to switch it off (takes effect after a restart).\n" +
                "Each feature has its own settings in Features/<name>/config.yml"
        )
        for (f in registered.values) yml.addDefault(f.id, true)
        yml.options().copyDefaults(true)
        try {
            yml.save(switchboardFile)
        } catch (e: Exception) {
            BedWars.plugin.logger.log(Level.WARNING, "Could not save " + switchboardFile, e)
        }
        for (f in registered.values) {
            if (!yml.getBoolean(f.id, true)) {
                skipped[f.id] = "switched off in Features/features.yml"
                continue
            }
            // The same code running twice (built-in + old addon jar) would do everything double.
            val legacy = f.legacyPluginNames.firstOrNull { Bukkit.getPluginManager().getPlugin(it) != null }
            if (legacy != null) {
                skipped[f.id] = "the standalone addon $legacy is installed"
                BedWars.plugin.logger.warning(
                    "Feature '" + f.displayName + "' is built in now, but the addon jar " + legacy +
                        " is still installed. The built-in copy stays off; remove the jar to use it."
                )
                continue
            }
            migrateLegacyFolder(f)
            try {
                f.onLoad()
                active.add(f)
            } catch (t: Throwable) {
                skipped[f.id] = "failed while loading: $t"
                BedWars.plugin.logger.log(Level.SEVERE, "Feature '" + f.displayName + "' failed while loading", t)
            }
        }
    }

    /** True when the feature is switched on (loaded), even if its [Feature.onEnable] has not run yet. */
    @JvmStatic
    fun isActive(feature: Feature?): Boolean = feature != null && feature in active

    /** Enables the [Feature.early] features. Called at the start of the plugin's onEnable. */
    @JvmStatic
    fun enableEarly() {
        for (f in ArrayList(active)) if (f.early) enable(f)
    }

    /** Enables every other loaded feature. Called at the end of the plugin's onEnable. */
    @JvmStatic
    fun enableAll() {
        coreReady = true
        for (f in ArrayList(active)) if (!f.early) enable(f)
        for (f in registered.values) if (f !in active) for (c in f.commandNames) {
            val reason = skipped[f.id] ?: "not enabled"
            BedWars.plugin.getCommand(c)?.setExecutor { sender, _, _, _ ->
                sender.sendMessage("\u00a7cThe BedWars1058 feature '" + f.displayName + "' is off (" + reason + ").")
                true
            }
        }
        val on = active.filter { it.isEnabled }
        BedWars.plugin.logger.info("Built-in features enabled (" + on.size + "/" + registered.size + "): " + on.joinToString(", ") { it.displayName })
    }

    private fun enable(f: Feature) {
        try {
            f.isEnabled = true
            f.onEnable()
            // like a plugin, a feature handles its own commands unless it named another executor
            if (f.isEnabled) for (c in f.commandNames) {
                val cmd = BedWars.plugin.getCommand(c) ?: continue
                if (cmd.executor === BedWars.plugin) cmd.setExecutor(f)
                if (cmd.tabCompleter == null || cmd.tabCompleter === BedWars.plugin) cmd.tabCompleter = f
            }
        } catch (t: Throwable) {
            BedWars.plugin.logger.log(Level.SEVERE, "Feature '" + f.displayName + "' failed to enable", t)
            skipped[f.id] = "failed to enable: $t"
            disable(f)
        }
    }

    /** Disables the regular features. Called at the start of the plugin's onDisable. */
    @JvmStatic
    fun disableAll() {
        coreReady = false
        for (f in ArrayList(active).reversed()) if (!f.early) disable(f)
    }

    /** Disables the [Feature.early] features. Called at the end of the plugin's onDisable. */
    @JvmStatic
    fun disableEarly() {
        for (f in ArrayList(active).reversed()) disable(f)
    }

    @JvmStatic
    @JvmOverloads
    fun disable(feature: Feature, reason: String? = null) {
        if (!active.remove(feature)) return
        if (reason != null) skipped[feature.id] = reason
        val wasEnabled = feature.isEnabled
        feature.isEnabled = false
        try {
            if (wasEnabled) feature.onDisable()
        } catch (t: Throwable) {
            BedWars.plugin.logger.log(Level.WARNING, "Feature '" + feature.displayName + "' failed to disable cleanly", t)
        }
        feature.unregisterListeners()
        if (!skipped.containsKey(feature.id)) skipped[feature.id] = "disabled itself"
    }

    /** First start after switching from the addon jar: keep the settings the server already had. */
    private fun migrateLegacyFolder(f: Feature) {
        if (f.dataFolder.exists()) return
        val pluginsDir = BedWars.plugin.dataFolder.parentFile ?: return
        val old = f.legacyPluginNames.map { File(pluginsDir, it) }.firstOrNull { it.isDirectory } ?: return
        try {
            old.copyRecursively(f.dataFolder, overwrite = false)
            BedWars.plugin.logger.info("Feature '" + f.displayName + "': copied the settings of " + old.name + " to " + f.dataFolder.path)
        } catch (e: Exception) {
            BedWars.plugin.logger.log(Level.WARNING, "Could not copy " + old + " to " + f.dataFolder, e)
        }
    }
}
