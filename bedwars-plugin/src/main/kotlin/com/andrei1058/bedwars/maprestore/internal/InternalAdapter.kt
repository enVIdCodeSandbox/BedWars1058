/*
 * BedWars1058 - A bed wars mini-game.
 * Copyright (C) 2021 Andrei Dascălu
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Contact e-mail: andrew.dascalu@gmail.com
 */

package com.andrei1058.bedwars.maprestore.internal

import com.andrei1058.bedwars.BedWars
import com.andrei1058.bedwars.BedWars.Companion.config
import com.andrei1058.bedwars.BedWars.Companion.plugin
import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.api.configuration.ConfigPath
import com.andrei1058.bedwars.api.server.ISetupSession
import com.andrei1058.bedwars.api.server.RestoreAdapter
import com.andrei1058.bedwars.api.server.ServerType
import com.andrei1058.bedwars.api.util.FileUtil
import com.andrei1058.bedwars.api.util.ZipFileUtil
import com.andrei1058.bedwars.arena.Arena
import com.andrei1058.bedwars.arena.VoidChunkGenerator
import com.andrei1058.bedwars.maprestore.internal.files.WorldZipper
import java.io.File
import java.io.IOException
import java.util.ArrayList
import java.util.Objects
import org.apache.commons.io.FileUtils
import org.bukkit.*
import org.bukkit.plugin.Plugin
import com.andrei1058.bedwars.api.util.unchecked

open class InternalAdapter(plugin: Plugin) : RestoreAdapter(plugin) {
    override fun onEnable(a: IArena) {
        Bukkit.getScheduler().runTask(getOwner(), Runnable lambda1@{
            if (Bukkit.getWorld(a.getWorldName()) != null) {
                Bukkit.getScheduler().runTask(getOwner(), Runnable {
                    val w: World? = Bukkit.getWorld(a.getWorldName())
                    a.init(w.unchecked())
                })
                return@lambda1
            }
            Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
                val bf = File(backupFolder, a.getArenaName() + ".zip")
                val af = File(Bukkit.getWorldContainer(), a.getArenaName())
                // an extra game of an arena runs in its own copy of the map
                val instance: Boolean = a.getWorldName() != a.getArenaName()
                var hadBackup: Boolean
                synchronized(ZIP_LOCK) {
                    hadBackup = bf.exists()
                    if (!hadBackup) {
                        WorldZipper(a.getArenaName(), true)
                    }
                }
                if (bf.exists() && (hadBackup || instance)) {
                    if (instance) {
                        FileUtil.delete(File(Bukkit.getWorldContainer(), a.getWorldName()))
                    } else {
                        FileUtil.delete(af)
                    }
                    try {
                        ZipFileUtil.unzipFileIntoDirectory(bf, File(Bukkit.getWorldContainer(), a.getWorldName()))
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
                WorldLayout.normalize(File(Bukkit.getWorldContainer(), a.getWorldName().unchecked()))
                deleteWorldTrash(a.getWorldName().unchecked())
                Bukkit.getScheduler().runTask(plugin, Runnable {
                    val wc = WorldCreator(a.getWorldName())
                    wc.generateStructures(false)
                    wc.generator(VoidChunkGenerator())
                    val w: World? = Bukkit.createWorld(wc)
                    if (w == null) {
                        throw IllegalStateException("World should be null")
                    }
                    w.setKeepSpawnInMemory(true)
                    w.setAutoSave(false)
                })
            })
        })
    }

    override fun onRestart(a: IArena) {
        Bukkit.getScheduler().runTask(getOwner(), Runnable {
            if (BedWars.getServerType() == ServerType.BUNGEE) {
                if (Arena.getGamesBeforeRestart() == 0) {
                    if (Arena.getArenas().isEmpty()) {
                        plugin.getLogger().info("Dispatching command: " + config.getString(ConfigPath.GENERAL_CONFIGURATION_BUNGEE_OPTION_RESTART_CMD))
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), config.getString(ConfigPath.GENERAL_CONFIGURATION_BUNGEE_OPTION_RESTART_CMD))
                    }
                } else {
                    if (Arena.getGamesBeforeRestart() != -1) {
                        Arena.setGamesBeforeRestart(Arena.getGamesBeforeRestart() - 1)
                    }
                    Bukkit.unloadWorld(a.getWorldName(), false)
                    if (Arena.canAutoScale(a.getArenaName())) {
                        Bukkit.getScheduler().runTaskLater(plugin, Runnable { Arena(a.getArenaName(), null) }, 80L)
                    }
                }
            } else {
                Bukkit.unloadWorld(a.getWorldName(), false)
                // with multiple instances another game of this arena may already be waiting for players
                if (Arena.canAutoScale(a.getArenaName())) {
                    Bukkit.getScheduler().runTaskLater(plugin, Runnable { Arena(a.getArenaName(), null) }, 80L)
                }
            }
            if (a.getWorldName() != a.getArenaName()) {
                deleteWorld(a.getWorldName().unchecked())
            }
        })
    }

    override fun onDisable(a: IArena) {
        if (BedWars.isShuttingDown()) {
            Bukkit.unloadWorld(a.getWorldName(), false)
            if (a.getWorldName() != a.getArenaName()) {
                try {
                    FileUtils.deleteDirectory(File(Bukkit.getWorldContainer(), a.getWorldName()))
                } catch (ignored: IOException) {
                }
            }
            return
        }
        Bukkit.getScheduler().runTask(getOwner(), Runnable {
            Bukkit.unloadWorld(a.getWorldName(), false)
            if (a.getWorldName() != a.getArenaName()) {
                deleteWorld(a.getWorldName())
            }
        })
    }

    override fun onSetupSessionStart(s: ISetupSession) {
        Bukkit.getScheduler().runTaskAsynchronously(getOwner(), Runnable {
            val bf = File(backupFolder, s.getWorldName() + ".zip")
            val af = File(Bukkit.getWorldContainer(), s.getWorldName())
            if (bf.exists()) {
                FileUtil.delete(af)
                try {
                    ZipFileUtil.unzipFileIntoDirectory(bf, File(Bukkit.getWorldContainer(), s.getWorldName()))
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            WorldLayout.normalize(af)
            val wc = WorldCreator(s.getWorldName())
            wc.generator(VoidChunkGenerator())
            wc.generateStructures(false)
            Bukkit.getScheduler().runTask(getOwner(), Runnable lambda8@{
                try {
                    val level = File(Bukkit.getWorldContainer(), s.getWorldName() + "/region")
                    if (level.exists()) {
                        s.getPlayer().sendMessage(ChatColor.GREEN.toString() + "Loading " + s.getWorldName() + " from Bukkit worlds container.")
                        deleteWorldTrash(s.getWorldName())
                        val w = Bukkit.createWorld(wc)
                        w.setKeepSpawnInMemory(true)
                    } else {
                        try {
                            s.getPlayer().sendMessage(ChatColor.GREEN.toString() + "Creating a new void map: " + s.getWorldName())
                            val w = Bukkit.createWorld(wc)
                            w.setKeepSpawnInMemory(true)
                            Bukkit.getScheduler().runTaskLater(plugin, s::teleportPlayer, 20L)
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                            s.close()
                        }
                        return@lambda8
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    s.close()
                    return@lambda8
                }
                Bukkit.getScheduler().runTaskLater(plugin, s::teleportPlayer, 20L)
            })
        })
    }

    override fun onSetupSessionClose(s: ISetupSession) {
        Bukkit.getScheduler().runTask(getOwner(), Runnable {
            Bukkit.getWorld(s.getWorldName()).save()
            Bukkit.unloadWorld(s.getWorldName(), true)
            Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable { WorldZipper(s.getWorldName(), true) })
        })
    }

    override fun isWorld(name: String): Boolean {
        return File(Bukkit.getWorldContainer(), name + "/region").exists()
    }

    override fun deleteWorld(name: String) {
        Bukkit.getScheduler().runTaskAsynchronously(getOwner(), Runnable {
            try {
                FileUtils.deleteDirectory(File(Bukkit.getWorldContainer(), name))
            } catch (e: IOException) {
                e.printStackTrace()
            }
        })
    }

    override fun cloneArena(name1: String, name2: String) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            try {
                FileUtils.copyDirectory(File(Bukkit.getWorldContainer(), name1), File(Bukkit.getWorldContainer(), name2))
                deleteWorldTrash(name2)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        })
    }

    override fun getWorldsList(): MutableList<String> {
        val worlds: MutableList<String> = ArrayList()
        val dir = Bukkit.getWorldContainer()
        if (dir.exists()) {
            val fls = dir.listFiles()
            for (fl in Objects.requireNonNull(fls)) {
                if (fl.isDirectory()) {
                    val dat = File(fl.getName() + "/region")
                    if (dat.exists() && !fl.getName().startsWith("bw_temp")) {
                        worlds.add(fl.getName())
                    }
                }
            }
        }
        return worlds
    }

    override fun convertWorlds() {
        val dir = File(plugin.getDataFolder(), "/Arenas")
        if (dir.exists()) {
            val files: MutableList<File>? = ArrayList()
            val fls = dir.listFiles()
            for (fl in Objects.requireNonNull(fls)) {
                if (fl.isFile()) {
                    if (fl.getName().contains(".yml")) {
                        files!!.add(fl)
                    }
                }
            }
            // lowerCase arena names - new 1.14 standard
            var folder: File
            var newName: File
            val toRemove: MutableList<File> = ArrayList()
            val toAdd: MutableList<File> = ArrayList()
            for (file in files!!) {
                if (file.getName() != file.getName().lowercase()) {
                    newName = File(dir.getPath() + "/" + file.getName().lowercase())
                    if (!file.renameTo(newName)) {
                        toRemove.add(file)
                        BedWars.plugin.getLogger().severe("Could not rename " + file.getName() + " to " + file.getName().lowercase() + "! Please do it manually!")
                    } else {
                        toAdd.add(newName)
                        toRemove.add(file)
                    }
                    folder = File(plugin.getServer().getWorldContainer(), file.getName().replace(".yml", ""))
                    if (folder.exists()) {
                        if (folder.getName() != folder.getName().lowercase()) {
                            if (!folder.renameTo(File(plugin.getServer().getWorldContainer().getPath() + "/" + folder.getName().lowercase()))) {
                                BedWars.plugin.getLogger().severe("Could not rename " + folder.getName() + " folder to " + folder.getName().lowercase() + "! Please do it manually!")
                                toRemove.add(file)
                                return
                            }
                        }
                    }
                }
            }
            for (f in toRemove) {
                files!!.remove(f)
            }
            files!!.addAll(toAdd)
        }
        Bukkit.getScheduler().runTaskAsynchronously(getOwner(), Runnable {
            val files: Array<File>? = Bukkit.getWorldContainer().listFiles()
            if (files != null) {
                for (f in files) {
                    if (f != null && f.isDirectory()) {
                        if (f.getName().contains("bw_temp_")) {
                            deleteWorld(f.getName())
                        }
                    }
                }
            }
        })
    }

    override fun getDisplayName(): String {
        return "Internal Restore Adapter"
    }

    private fun deleteWorldTrash(world: String) {
        for (f in arrayOf<File>(File(Bukkit.getWorldContainer(), world + "/level.dat"), File(Bukkit.getWorldContainer(), world + "/level.dat_mcr"), File(Bukkit.getWorldContainer(), world + "/level.dat_old"), File(Bukkit.getWorldContainer(), world + "/session.lock"), File(Bukkit.getWorldContainer(), world + "/uid.dat"))) {
            if (f.exists()) {
                if (!f.delete()) {
                    getOwner().getLogger().warning("Could not delete: " + f.getPath())
                    getOwner().getLogger().warning("This may cause issues!")
                }
            }
        }
    }

    companion object {
        private val ZIP_LOCK: Any = Any()
        @JvmField var backupFolder: File = File(BedWars.plugin.getDataFolder().toString() + "/Cache")
    }
}
