package com.andrei1058.bedwars.features.replay.bedwars

import com.andrei1058.bedwars.BedWars
import com.andrei1058.bedwars.features.FeatureManager
import com.andrei1058.bedwars.features.maploader.WorldLoaderPlugin
import com.andrei1058.bedwars.features.maploader.api.MapAliases
import com.andrei1058.bedwars.features.multigroup.MultiGroupPlugin
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import java.io.File
import java.util.Locale

/** Where the pristine copy of an arena's map can be copied from. */
sealed class MapSource(val file: File, val origin: String) {
    class Folder(file: File, origin: String) : MapSource(file, origin)
    class Zip(file: File, origin: String) : MapSource(file, origin)

    override fun toString() = "$origin: ${file.path}"
}

/**
 * Finds the map of an arena, whatever plugin keeps it:
 *  1. BedWars1058-MapLoader's library (`storage.path`, a world folder or a zip per map),
 *  2. BedWars1058's own pristine copies (`plugins/BedWars1058/Cache/<map>.zip`),
 *  3. a plain world folder in the server root (very old setups).
 *
 * Variant arenas made by the MultiGroup feature play another arena's map, so the arena name is first translated to the
 * name of its base map. MapLoader and MultiGroup are features of this plugin, so they are asked directly.
 *
 * All methods are safe to call from the main thread; none touches a world.
 */
class MapTemplates(private val owner: Plugin) {

    /** Names an arena's map may be stored under, most specific first. */
    fun candidates(arena: String, group: String?): List<String> {
        val out = LinkedHashSet<String>()
        multiGroupBase(arena)?.let { out += it }
        mapLoaderAlias(arena)?.let { out += it }
        out += arena
        // Last resort when the variant is gone from MultiGroup's configuration: its default name is "<map>-<group>".
        if (!group.isNullOrBlank()) {
            val suffix = "-" + group.lowercase(Locale.ROOT)
            if (arena.lowercase(Locale.ROOT).endsWith(suffix) && arena.length > suffix.length) {
                out += arena.substring(0, arena.length - suffix.length)
            }
        }
        return out.toList()
    }

    /** The first map found for [arena], or null if none of the known places has it. */
    fun find(arena: String, group: String?): MapSource? {
        val names = candidates(arena, group)
        for (n in names) {
            libraryDir()?.let { dir -> findIn(dir, n, "MapLoader library")?.let { return it } }
            findIn(bedWarsCache(), n, "BedWars cache")?.let { return it }
            rootFolder(n)?.let { return it }
        }
        return null
    }

    // ------------------------------------------------------------------ name resolution

    /** The MultiGroup feature knows which map each of its variant arenas plays. */
    private fun multiGroupBase(arena: String): String? {
        val mg = FeatureManager.enabled<MultiGroupPlugin>() ?: return null
        return mg.registry.baseOf(arena)?.takeIf { it.isNotBlank() }
    }

    /** MapLoader's alias hook, for whatever other feature registered one. */
    private fun mapLoaderAlias(arena: String): String? {
        if (FeatureManager.enabled<WorldLoaderPlugin>() == null) return null
        val r = MapAliases.resolve(arena)
        return r?.takeIf { it.isNotBlank() && !it.equals(arena, ignoreCase = true) }
    }

    // ------------------------------------------------------------------ places

    /** MapLoader's library folder, read from the running feature or, failing that, from its config.yml. */
    private fun libraryDir(): File? {
        val ml = FeatureManager.find<WorldLoaderPlugin>()?.takeIf { FeatureManager.isActive(it) } ?: return null
        ml.services?.store?.storageDir?.let { return it.toFile() }
        val raw = try {
            org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(File(ml.dataFolder, "config.yml"))
                .getString("storage.path")
        } catch (_: Throwable) { null } ?: "bedwars_worlds"
        val f = File(raw)
        return if (f.isAbsolute) f else File(Bukkit.getWorldContainer(), raw)
    }

    private fun bedWarsCache(): File = File(BedWars.plugin.dataFolder, "Cache")

    private fun findIn(dir: File, name: String, origin: String): MapSource? {
        val kids = dir.listFiles() ?: return null
        val lower = name.lowercase(Locale.ROOT)
        kids.firstOrNull { it.isDirectory && it.name.lowercase(Locale.ROOT) == lower && looksLikeWorld(it) }
            ?.let { return MapSource.Folder(it, origin) }
        kids.firstOrNull { it.isFile && it.name.lowercase(Locale.ROOT) == "$lower.zip" }
            ?.let { return MapSource.Zip(it, origin) }
        return null
    }

    /** A world folder in the server root that no plugin created for a match and that is not one of ours. */
    private fun rootFolder(name: String): MapSource? {
        val f = File(Bukkit.getWorldContainer(), name)
        if (!f.isDirectory || !looksLikeWorld(f)) return null
        if (Bukkit.getWorld(name) != null) return null // a live arena: not pristine
        if (FOREIGN_AND_OWN.any { File(f, it).exists() }) return null
        return MapSource.Folder(f, "server root")
    }

    private fun looksLikeWorld(dir: File) =
        File(dir, "region").isDirectory || File(dir, "level.dat").isFile || File(dir, "dimensions").isDirectory

    companion object {
        private val FOREIGN_AND_OWN = com.andrei1058.bedwars.features.replay.util.WorldLayout.FOREIGN_MARKERS + com.andrei1058.bedwars.features.replay.util.WorldLayout.MARKER
    }
}
