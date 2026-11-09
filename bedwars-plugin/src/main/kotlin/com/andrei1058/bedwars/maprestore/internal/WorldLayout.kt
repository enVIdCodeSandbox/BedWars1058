package com.andrei1058.bedwars.maprestore.internal

import java.io.File
import java.io.IOException
import java.nio.file.Files
import org.bukkit.Bukkit

/**
 * Minecraft 26.1 moved the overworld from the world root into dimensions/minecraft/overworld
 * (and DIM-1 / DIM1 into dimensions/minecraft/the_nether / the_end). A map saved in the other layout
 * would be loaded as a freshly generated vanilla world, so it is converted before the world is created.
 */
object WorldLayout {
    private val DATA: Array<String> = arrayOf<String>("region", "entities", "poi")
    private val EXTRA: Array<Array<String>> = arrayOf<Array<String>>(arrayOf<String>("DIM-1", "the_nether"), arrayOf<String>("DIM1", "the_end"))

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

    @JvmStatic
    fun normalize(world: File) {
        if (!world.isDirectory()) {
            return
        }
        val modern: Boolean = isModernServer()
        val overworld = File(world, "dimensions/minecraft/overworld")
        try {
            if (modern) {
                if (File(world, "region").isDirectory() && !File(overworld, "region").isDirectory()) {
                    overworld.mkdirs()
                    for (n in DATA) {
                        move(File(world, n), File(overworld, n))
                    }
                }
                for (e in EXTRA) {
                    val target = File(world, "dimensions/minecraft/" + e[1])
                    if (File(world, e[0]).isDirectory() && !File(target, "region").isDirectory()) {
                        target.getParentFile().mkdirs()
                        move(File(world, e[0]), target)
                    }
                }
            } else {
                if (File(overworld, "region").isDirectory() && !File(world, "region").isDirectory()) {
                    for (n in DATA) {
                        move(File(overworld, n), File(world, n))
                    }
                }
                for (e in EXTRA) {
                    val source = File(world, "dimensions/minecraft/" + e[1])
                    if (source.isDirectory() && !File(world, e[0]).exists()) {
                        move(source, File(world, e[0]))
                    }
                }
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
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
