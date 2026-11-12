package com.andrei1058.bedwars.features.multigroup.integration

import com.andrei1058.bedwars.features.multigroup.bedwars.YamlTrees
import com.andrei1058.bedwars.features.multigroup.world.FileOps
import com.andrei1058.bedwars.features.FeatureManager
import com.andrei1058.bedwars.features.maploader.WorldLoaderPlugin
import com.andrei1058.bedwars.features.maploader.api.MapAliases
import org.bukkit.plugin.Plugin
import java.io.File
import java.nio.file.Path
import java.util.Locale
import java.util.function.Function
import java.util.logging.Logger

/**
 * Cooperation with the built-in MapLoader feature (the map library / just-in-time loader).
 *
 * When MapLoader is on, variant arenas are resolved to their base map inside MapLoader (its alias hook), so they are
 * parked, listed and loaded just-in-time exactly like every other arena. When it is off, MultiGroup loads variants itself.
 */
class MapLoaderBridge(private val owner: Plugin, private val log: Logger) {

    /** The MapLoader feature when it is switched on, otherwise null. */
    val plugin: WorldLoaderPlugin? get() = FeatureManager.find<WorldLoaderPlugin>()?.takeIf { FeatureManager.isActive(it) }

    @Volatile
    var aliasHookActive: Boolean = false
        private set

    /** Registers [resolver] (arena name -> base map name or null) with MapLoader's alias hook. */
    fun registerAliases(resolver: (String) -> String?): Boolean {
        if (plugin == null) return false
        MapAliases.registerResolver(RESOLVER_ID, Function<String, String?> { name -> resolver(name) })
        aliasHookActive = true
        return true
    }

    fun unregisterAliases() {
        if (!aliasHookActive) return
        aliasHookActive = false
        MapAliases.unregisterResolver(RESOLVER_ID)
    }

    /** True when MapLoader is enabled and has taken over BedWars1058's world loading. */
    fun isActive(): Boolean {
        val ml = plugin ?: return false
        return ml.isEnabled && ml.services != null
    }

    fun version(): String? = if (plugin != null) "(built in)" else null

    /** MapLoader's library folder (`storage.path`), from the running feature or from its config.yml. */
    fun libraryDir(): Path? {
        val ml = plugin ?: return null
        ml.services?.store?.storageDir?.let { return it }
        val cfg = File(ml.dataFolder, "config.yml")
        val tree = if (cfg.isFile) YamlTrees.load(cfg) else null
        val raw = ((tree?.get("storage") as? Map<*, *>)?.get("path") as? String) ?: "bedwars_worlds"
        val p = File(raw).toPath()
        return (if (p.isAbsolute) p else owner.server.worldContainer.toPath().resolve(p)).toAbsolutePath().normalize()
    }

    /** A map in MapLoader's library: world folder `<lib>/<name>` or `<lib>/<name>.zip` (case-insensitive). */
    fun findInLibrary(name: String): Path? {
        val dir = libraryDir()?.toFile() ?: return null
        val kids = dir.listFiles() ?: return null
        val lower = name.lowercase(Locale.ROOT)
        kids.firstOrNull { it.isDirectory && it.name.lowercase(Locale.ROOT) == lower && FileOps.isWorldFolder(it) }?.let { return it.toPath() }
        kids.firstOrNull { it.isFile && it.name.lowercase(Locale.ROOT) == "$lower.zip" }?.let { return it.toPath() }
        return null
    }

    companion object {
        const val NAME = "MapLoader"
        private const val RESOLVER_ID = "MultiGroup"
    }
}
