package com.andrei1058.bedwars.api.util

import com.andrei1058.bedwars.api.server.VersionSupport
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.Properties

object FileUtil {
    @JvmStatic
    fun delete(file: File) {
        if (file.isDirectory()) {
            //noinspection ConstantConditions
            for (subfile in file.listFiles()) {
                delete(subfile)
            }
        } else {
            //noinspection ResultOfMethodCallIgnored
            file.delete()
        }
    }

    @JvmStatic
    fun setMainLevel(worldName: String, vs: VersionSupport) {
        val properties = Properties()
        try {
            FileInputStream("server.properties").use { `in` ->
                properties.load(`in`)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        properties.setProperty("level-name", worldName)
        properties.setProperty("generator-settings", if (vs.getVersion() > 5) "minecraft:air;minecraft:air;minecraft:air" else "1;0;1")
        properties.setProperty("allow-nether", "false")
        properties.setProperty("level-type", "flat")
        properties.setProperty("generate-structures", "false")
        properties.setProperty("spawn-monsters", "false")
        properties.setProperty("max-world-size", "1000")
        properties.setProperty("spawn-animals", "false")
        try {
            FileOutputStream("server.properties").use { out ->
                properties.store(out, null)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
