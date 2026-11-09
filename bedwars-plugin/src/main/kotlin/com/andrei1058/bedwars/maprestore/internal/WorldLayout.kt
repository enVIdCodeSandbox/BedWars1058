package com.andrei1058.bedwars.maprestore.internal

import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.Locale
import org.apache.commons.io.FileUtils
import org.bukkit.Bukkit

/**
 * World storage differs between server versions.
 * <ul>
 *   <li>Up to 1.21.x a world is a folder in the server root: {@code <root>/<name>/region}.</li>
 *   <li>From 26.1 Paper stores every world, including the ones plugins create, inside the main level folder:
 *   {@code <level>/dimensions/minecraft/<name lower case>/region}. A map copied into the server root is ignored and
 *   the world is generated from scratch.</li>
 * </ul>
 * Maps are kept as plain folders / zips in the old layout; they are copied to {@link #worldDir(String)} to be played.
 */
object WorldLayout {
    private val DATA: Array<String> = arrayOf<String>("region", "entities", "poi")
    private val JUNK: Array<String> = arrayOf<String>("dimensions", "DIM-1", "DIM1", "playerdata", "players", "stats", "advancements", "level.dat", "level.dat_old", "level.dat_mcr", "uid.dat", "session.lock")

    /**
     * @return true on Minecraft 26.1+ ("26.3-R0.1-SNAPSHOT"; older versions start with "1.").
     */
    @JvmStatic
    fun isModernServer(): Boolean {
        val v = Bukkit.getBukkitVersion()
        var end: Int = 0
        while (end < v.length() && Character.isDigit(v[end])) {
            end++
        }
        if (end == 0) {
            return false
        }
        try {
            return Integer.parseInt(v.substring(0, end)) >= 26
        } catch (e: NumberFormatException) {
            return false
        }
    }

    /** Where the files of the world called {@code name} live while it is loaded. */
    @JvmStatic
    fun worldDir(name: String): File {
        if (!isModernServer()) {
            return File(Bukkit.getWorldContainer(), name)
        }
        return File(levelDirectory(), "dimensions/minecraft/" + name.toLowerCase(Locale.ENGLISH).replace(' ', '_'))
    }

    /** The folder of the main level ("world"). */
    @JvmStatic
    fun levelDirectory(): File {
        try {
            val path = Bukkit.getServer().javaClass.getMethod("getLevelDirectory").invoke(Bukkit.getServer())
            val f = (path as Path).toFile()
            return if (f.isAbsolute()) f else File(Bukkit.getWorldContainer(), f.getPath())
        } catch (t: Throwable) {
            return File(Bukkit.getWorldContainer(), Bukkit.getWorlds().get(0).getName())
        }
    }

    /**
     * Makes a freshly copied map usable.
     * On 26.1+ {@code dir} is the final dimension folder: data is moved up from {@code dimensions/minecraft/overworld}
     * when the map was saved in the new layout, and files that belong to a whole level are dropped.
     * On older servers a map saved in the new layout is converted back to the old one.
     */
    @JvmStatic
    fun normalize(dir: File) {
        if (!dir.isDirectory()) {
            return
        }
        try {
            val overworld = File(dir, "dimensions/minecraft/overworld")
            if (isModernServer()) {
                if (!File(dir, "region").isDirectory() && File(overworld, "region").isDirectory()) {
                    for (n in DATA) {
                        move(File(overworld, n), File(dir, n))
                    }
                }
                for (n in JUNK) {
                    val f = File(dir, n)
                    if (f.isDirectory()) {
                        FileUtils.deleteDirectory(f)
                    } else {
                        f.delete()
                    }
                }
            } else if (!File(dir, "region").isDirectory() && File(overworld, "region").isDirectory()) {
                for (n in DATA) {
                    move(File(overworld, n), File(dir, n))
                }
                val nether = File(dir, "dimensions/minecraft/the_nether")
                if (nether.isDirectory() && !File(dir, "DIM-1").exists()) {
                    move(nether, File(dir, "DIM-1"))
                }
                val end = File(dir, "dimensions/minecraft/the_end")
                if (end.isDirectory() && !File(dir, "DIM1").exists()) {
                    move(end, File(dir, "DIM1"))
                }
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }

    /**
     * 26.1+: copies the played world back into {@code <root>/<name>} (old layout) so it can be zipped as the map.
     * Used when a setup session ends.
     */
    @Throws(IOException::class)
    @JvmStatic
    fun exportToRoot(name: String) {
        if (!isModernServer()) {
            return
        }
        val from = worldDir(name)
        val to = File(Bukkit.getWorldContainer(), name)
        if (!File(from, "region").isDirectory()) {
            return
        }
        FileUtils.deleteDirectory(to)
        FileUtils.copyDirectory(from, to)
    }

    @Throws(IOException::class)
    @JvmStatic
    private fun move(from: File, to: File) {
        if (!from.exists() || to.exists()) {
            return
        }
        Files.move(from.toPath(), to.toPath())
    }
}
