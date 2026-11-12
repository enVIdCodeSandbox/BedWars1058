/*
 * BedWars1058 - A bed wars mini-game.
 * Copyright (C) 2023 Andrei Dascălu
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

package com.andrei1058.bedwars.arena.mapreset.slime

import com.andrei1058.bedwars.api.BedWars
import com.andrei1058.bedwars.api.arena.GameState
import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.api.configuration.ConfigPath
import com.andrei1058.bedwars.api.server.ISetupSession
import com.andrei1058.bedwars.api.server.RestoreAdapter
import com.andrei1058.bedwars.api.server.ServerType
import com.andrei1058.bedwars.api.util.FileUtil
import com.andrei1058.bedwars.api.util.ZipFileUtil
import com.flowpowered.nbt.CompoundMap
import com.flowpowered.nbt.CompoundTag
import com.flowpowered.nbt.IntTag
import com.flowpowered.nbt.stream.NBTInputStream
import com.flowpowered.nbt.stream.NBTOutputStream
import com.infernalsuite.aswm.api.SlimePlugin
import com.infernalsuite.aswm.api.exceptions.*
import com.infernalsuite.aswm.api.loaders.SlimeLoader
import com.infernalsuite.aswm.api.world.SlimeWorld
import com.infernalsuite.aswm.api.world.properties.SlimeProperties
import com.infernalsuite.aswm.api.world.properties.SlimePropertyMap
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import java.util.logging.Level
import org.apache.commons.io.FileUtils
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.event.world.WorldInitEvent
import org.bukkit.event.world.WorldLoadEvent
import org.bukkit.plugin.Plugin
import com.andrei1058.bedwars.api.util.unchecked

open class SlimePaperAdapter(plugin: Plugin) : RestoreAdapter(plugin) {
    private val slime: SlimePlugin
    private val api: BedWars

    override fun onEnable(a: IArena) {
        if (api.getVersionSupport().getMainLevel().equals(a.getWorldName(), ignoreCase = true)) {
            if (!(api.getServerType() == ServerType.BUNGEE && api.getArenaUtil().getGamesBeforeRestart() == 1)) {
                FileUtil.setMainLevel("ignore_main_level", api.getVersionSupport())
                getOwner().getLogger().log(Level.SEVERE, "Cannot use level-name as arenas. Automatically creating a new void map for level-name.")
                getOwner().getLogger().log(Level.SEVERE, "The server is restarting...")
                Bukkit.getServer().spigot().restart()
                return
            }
        }
        Bukkit.getScheduler().runTaskAsynchronously(getOwner(), Runnable lambda1@{
            if (Bukkit.getWorld(a.getWorldName()) != null) {
                Bukkit.getScheduler().runTask(getOwner(), Runnable {
                    val w: World? = Bukkit.getWorld(a.getWorldName())
                    a.init(w.unchecked())
                })
                return@lambda1
            }
            val flat = slime.getLoader("file")
            val spawn = a.getConfig()!!.getString("waiting.Loc").split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val spm = getCreateProperties(java.lang.Double.parseDouble(spawn[0]).toInt(), java.lang.Double.parseDouble(spawn[1]).toInt(), java.lang.Double.parseDouble(spawn[2]).toInt())
            try {
                // Note that this method should be called asynchronously
                var world = slime.loadWorld(flat, a.getArenaName(), true, spm)
                if (api.getServerType() == ServerType.BUNGEE && api.isAutoScale()) {
                    world = world.clone(a.getWorldName())
                }
                // This method must be called synchronously
                val finalWorld = world
                Bukkit.getScheduler().runTask(getOwner(), Runnable lambda3@{
                    val loaded: SlimeWorld? = slime.loadWorld(finalWorld)
                    if (null == loaded) {
                        api.getArenaUtil().removeFromEnableQueue(a)
                        getOwner().getLogger().severe("Something wrong... removing arena " + a.getArenaName() + " from queue.")
                        return@lambda3
                    }
                    val w: World? = Bukkit.getWorld(loaded.getName())
                    if (w == null) {
                        api.getArenaUtil().removeFromEnableQueue(a)
                        getOwner().getLogger().severe("Something wrong... removing arena " + a.getArenaName() + " from queue.")
                        return@lambda3
                    }
                    Bukkit.getPluginManager().callEvent(WorldInitEvent(w))
                    Bukkit.getScheduler().runTask(getOwner(), Runnable { Bukkit.getPluginManager().callEvent(WorldLoadEvent(w)) })
                })
            } catch (ex: UnknownWorldException) {
                api.getArenaUtil().removeFromEnableQueue(a)
                ex.printStackTrace()
            } catch (ex: IOException) {
                api.getArenaUtil().removeFromEnableQueue(a)
                ex.printStackTrace()
            } catch (ex: CorruptedWorldException) {
                api.getArenaUtil().removeFromEnableQueue(a)
                ex.printStackTrace()
            } catch (ex: NewerFormatException) {
                api.getArenaUtil().removeFromEnableQueue(a)
                ex.printStackTrace()
            } catch (ex: WorldLockedException) {
                api.getArenaUtil().removeFromEnableQueue(a)
                ex.printStackTrace()
            } catch (thisShouldNotHappenSWM: ConcurrentModificationException) {
                // https://github.com/Grinderwolf/Slime-World-Manager/blob/develop/.docs/api/load-world.md
                thisShouldNotHappenSWM.printStackTrace()
                api.getArenaUtil().removeFromEnableQueue(a)
                getOwner().getLogger().severe("This is a AdvancedSlimePaper issue!")
                getOwner().getLogger().severe("I've submitted a bug report: https://github.com/Grinderwolf/Slime-World-Manager/issues/174")
                getOwner().getLogger().severe("Trying again to load arena: " + a.getArenaName())
                // hope not to get an overflow
                onEnable(a)
                // this should not happen since they say to use #load async
            }
        })
    }

    override fun onRestart(a: IArena) {
        if (api.getServerType() == ServerType.BUNGEE) {
            if (api.getArenaUtil().getGamesBeforeRestart() == 0) {
                if (api.getArenaUtil().getArenas().size == 1 && api.getArenaUtil().getArenas().get(0).getStatus() == GameState.restarting) {
                    getOwner().getLogger().info("Dispatching command: " + api.getConfigs().getMainConfig().getString(ConfigPath.GENERAL_CONFIGURATION_BUNGEE_OPTION_RESTART_CMD))
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), api.getConfigs().getMainConfig().getString(ConfigPath.GENERAL_CONFIGURATION_BUNGEE_OPTION_RESTART_CMD))
                }
            } else {
                if (api.getArenaUtil().getGamesBeforeRestart() != -1) {
                    api.getArenaUtil().setGamesBeforeRestart(api.getArenaUtil().getGamesBeforeRestart() - 1)
                }
                Bukkit.getScheduler().runTask(getOwner(), Runnable {
                    Bukkit.unloadWorld(a.getWorldName(), false)
                    if (api.getArenaUtil().canAutoScale(a.getArenaName())) {
                        Bukkit.getScheduler().runTaskLater(getOwner(), Runnable { api.getArenaUtil().loadArena(a.getArenaName(), null) }, 80L)
                    }
                })
            }
        } else {
            Bukkit.getScheduler().runTask(getOwner(), Runnable {
                Bukkit.unloadWorld(a.getWorldName(), false)
                Bukkit.getScheduler().runTaskLater(getOwner(), Runnable { api.getArenaUtil().loadArena(a.getArenaName(), null) }, 80L)
            })
        }
    }

    override fun onDisable(a: IArena) {
        if (api.isShuttingDown()) {
            Bukkit.unloadWorld(a.getWorldName(), false)
            return
        }
        Bukkit.getScheduler().runTask(getOwner(), Runnable { Bukkit.unloadWorld(a.getWorldName(), false) })
    }

    override fun onSetupSessionStart(s: ISetupSession) {
        Bukkit.getScheduler().runTaskAsynchronously(getOwner(), Runnable {
            val sLoader = slime.getLoader("file")
            var spawn = arrayOf<String>("0", "50", "0")
            if (s.getConfig().getYml().getString("waiting.Loc") != null) {
                spawn = s.getConfig().getString("waiting.Loc").split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            }
            val spm = getCreateProperties(java.lang.Double.parseDouble(spawn[0]).toInt(), java.lang.Double.parseDouble(spawn[1]).toInt(), java.lang.Double.parseDouble(spawn[2]).toInt())
            try {
                if (Bukkit.getWorld(s.getWorldName()) != null) {
                    Bukkit.getScheduler().runTask(getOwner(), Runnable { Bukkit.unloadWorld(s.getWorldName(), false) })
                }
                var world: SlimeWorld
                if (sLoader.worldExists(s.getWorldName())) {
                    world = slime.loadWorld(sLoader, s.getWorldName(), false, spm)
                    Bukkit.getScheduler().runTask(getOwner(), Runnable { s.getPlayer().sendMessage(ChatColor.GREEN.toString() + "Loading world from SlimeWorldManager container.") })
                } else {
                    if (File(Bukkit.getWorldContainer(), s.getWorldName() + "/level.dat").exists()) {
                        Bukkit.getScheduler().runTask(getOwner(), Runnable { s.getPlayer().sendMessage(ChatColor.GREEN.toString() + "Importing world to the SlimeWorldManager container.") })
                        slime.importWorld(File(Bukkit.getWorldContainer(), s.getWorldName()), s.getWorldName().lowercase(), sLoader)
                        world = slime.loadWorld(sLoader, s.getWorldName(), false, spm)
                    } else {
                        Bukkit.getScheduler().runTask(getOwner(), Runnable { s.getPlayer().sendMessage(ChatColor.GREEN.toString() + "Creating anew void map.") })
                        world = slime.createEmptyWorld(sLoader, s.getWorldName(), false, spm)
                    }
                }
                val sw: SlimeWorld? = world
                // This method must be called synchronously
                Bukkit.getScheduler().runTask(getOwner(), Runnable lambda7@{
                    if (null == sw) {
                        return@lambda7
                    }
                    val w: World? = Bukkit.getWorld(sw.getName())
                    if (w == null) {
                        return@lambda7
                    }
                    Bukkit.getPluginManager().callEvent(WorldInitEvent(w))
                    Bukkit.getScheduler().runTask(getOwner(), Runnable { Bukkit.getPluginManager().callEvent(WorldLoadEvent(w)) })
                    s.teleportPlayer()
                })
            } catch (ex: UnknownWorldException) {
                s.getPlayer().sendMessage(ChatColor.RED.toString() + "An error occurred! Please check console.")
                ex.printStackTrace()
                s.close()
            } catch (ex: IOException) {
                s.getPlayer().sendMessage(ChatColor.RED.toString() + "An error occurred! Please check console.")
                ex.printStackTrace()
                s.close()
            } catch (ex: CorruptedWorldException) {
                s.getPlayer().sendMessage(ChatColor.RED.toString() + "An error occurred! Please check console.")
                ex.printStackTrace()
                s.close()
            } catch (ex: NewerFormatException) {
                s.getPlayer().sendMessage(ChatColor.RED.toString() + "An error occurred! Please check console.")
                ex.printStackTrace()
                s.close()
            } catch (ex: WorldAlreadyExistsException) {
                s.getPlayer().sendMessage(ChatColor.RED.toString() + "An error occurred! Please check console.")
                ex.printStackTrace()
                s.close()
            } catch (ex: InvalidWorldException) {
                s.getPlayer().sendMessage(ChatColor.RED.toString() + "An error occurred! Please check console.")
                ex.printStackTrace()
                s.close()
            } catch (ex: WorldTooBigException) {
                s.getPlayer().sendMessage(ChatColor.RED.toString() + "An error occurred! Please check console.")
                ex.printStackTrace()
                s.close()
            } catch (ex: WorldLoadedException) {
                s.getPlayer().sendMessage(ChatColor.RED.toString() + "An error occurred! Please check console.")
                ex.printStackTrace()
                s.close()
            } catch (ex: WorldLockedException) {
                s.getPlayer().sendMessage(ChatColor.RED.toString() + "An error occurred! Please check console.")
                ex.printStackTrace()
                s.close()
            }
        })
    }

    override fun onSetupSessionClose(s: ISetupSession) {
        Objects.requireNonNull(Bukkit.getWorld(s.getWorldName())).save()
        Bukkit.getScheduler().runTask(getOwner(), Runnable { Bukkit.unloadWorld(s.getWorldName(), true) })
    }

    override fun isWorld(name: String): Boolean {
        try {
            return slime.getLoader("file").worldExists(name)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return false
    }

    override fun deleteWorld(name: String) {
        Bukkit.getScheduler().runTaskAsynchronously(getOwner(), Runnable {
            try {
                slime.getLoader("file").deleteWorld(name)
            } catch (e: UnknownWorldException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        })
    }

    override fun cloneArena(name1: String, name2: String) {
        Bukkit.getScheduler().runTaskAsynchronously(getOwner(), Runnable {
            try {
                // Note that this method should be called asynchronously
                val world = slime.loadWorld(slime.getLoader("file"), name1, true, getCreateProperties(0, 118, 0))
                world.clone(name2, slime.getLoader("file"))
            } catch (ex: UnknownWorldException) {
                ex.printStackTrace()
            } catch (ex: IOException) {
                ex.printStackTrace()
            } catch (ex: CorruptedWorldException) {
                ex.printStackTrace()
            } catch (ex: NewerFormatException) {
                ex.printStackTrace()
            } catch (ex: WorldLockedException) {
                ex.printStackTrace()
            } catch (ex: WorldAlreadyExistsException) {
                ex.printStackTrace()
            }
        })
    }

    override fun getWorldsList(): MutableList<String> {
        try {
            return slime.getLoader("file").listWorlds()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return ArrayList()
    }

    /**
     * Convert vanilla worlds to the slime format.
     */
    override fun convertWorlds() {
        val dir = File(getOwner().getDataFolder(), "/Arenas")
        var ff: File
        val sl = slime.getLoader("file")
        if (dir.exists()) {
            val fls: Array<File>? = dir.listFiles()
            if (fls != null) {
                for (fl in fls) {
                    if (fl.isFile()) {
                        if (fl.getName().endsWith(".yml")) {
                            val name = fl.getName().replace(".yml", "").lowercase()
                            ff = File(Bukkit.getWorldContainer(), fl.getName().replace(".yml", ""))
                            try {
                                if (!sl.worldExists(name)) {
                                    if (fl.getName() != name) {
                                        if (!fl.renameTo(File(dir, name + ".yml"))) {
                                            getOwner().getLogger().log(Level.WARNING, "Could not rename " + fl.getName() + ".yml to " + name + ".yml")
                                        }
                                    }
                                    val bc = File(getOwner().getDataFolder().toString() + "/Cache", ff.getName() + ".zip")
                                    if (ff.exists() && bc.exists()) {
                                        FileUtil.delete(ff)
                                        ZipFileUtil.unzipFileIntoDirectory(bc, File(Bukkit.getWorldContainer(), name))
                                    }
                                    // clean up world folder
                                    deleteWorldTrash(name)
                                    // check if level.dat is missing in world folder
                                    handleLevelDat(name)
                                    // start Slime conversion
                                    convertWorld(name, null)
                                }
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            }
        }
        Bukkit.getScheduler().runTaskAsynchronously(getOwner(), Runnable {
            val files: Array<File>? = Bukkit.getWorldContainer().listFiles()
            if (files != null) {
                for (f in files) {
                    if (f != null && f.isDirectory()) {
                        if (f.getName().contains("bw_temp_")) {
                            try {
                                FileUtils.deleteDirectory(f)
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            }
        })
    }

    override fun getDisplayName(): String {
        return "Advanced Slime Paper by InfernalSuite"
    }

    private fun convertWorld(name: String, player: Player?) {
        val worldFolder = File(Bukkit.getWorldContainer(), name)
        if (!worldFolder.exists() || !worldFolder.isDirectory()) {
            getOwner().getLogger().severe("Tried converting arena " + name + " to Slime format, but couldn't find any bukkit world folder.")
            return
        }
        // todo allow data loaders
        val sl = slime.getLoader("file")
        try {
            getOwner().getLogger().log(Level.INFO, "Converting " + name + " to the Slime format.")
            slime.importWorld(File(Bukkit.getWorldContainer(), name), name, sl)
        } catch (e: WorldAlreadyExistsException) {
            if (player != null) {
                player.sendMessage(ChatColor.RED.toString() + "Could not convert " + name + " to the Slime format.")
                player.sendMessage(ChatColor.RED.toString() + "Check the console for details.")
                val s: ISetupSession? = api.getSetupSession(player.getUniqueId())
                if (s != null) {
                    s.close()
                }
            }
            getOwner().getLogger().log(Level.WARNING, "Could not convert " + name + " to the Slime format.")
            e.printStackTrace()
        } catch (e: InvalidWorldException) {
            if (player != null) {
                player.sendMessage(ChatColor.RED.toString() + "Could not convert " + name + " to the Slime format.")
                player.sendMessage(ChatColor.RED.toString() + "Check the console for details.")
                val s: ISetupSession? = api.getSetupSession(player.getUniqueId())
                if (s != null) {
                    s.close()
                }
            }
            getOwner().getLogger().log(Level.WARNING, "Could not convert " + name + " to the Slime format.")
            e.printStackTrace()
        } catch (e: WorldLoadedException) {
            if (player != null) {
                player.sendMessage(ChatColor.RED.toString() + "Could not convert " + name + " to the Slime format.")
                player.sendMessage(ChatColor.RED.toString() + "Check the console for details.")
                val s: ISetupSession? = api.getSetupSession(player.getUniqueId())
                if (s != null) {
                    s.close()
                }
            }
            getOwner().getLogger().log(Level.WARNING, "Could not convert " + name + " to the Slime format.")
            e.printStackTrace()
        } catch (e: WorldTooBigException) {
            if (player != null) {
                player.sendMessage(ChatColor.RED.toString() + "Could not convert " + name + " to the Slime format.")
                player.sendMessage(ChatColor.RED.toString() + "Check the console for details.")
                val s: ISetupSession? = api.getSetupSession(player.getUniqueId())
                if (s != null) {
                    s.close()
                }
            }
            getOwner().getLogger().log(Level.WARNING, "Could not convert " + name + " to the Slime format.")
            e.printStackTrace()
        } catch (e: IOException) {
            if (player != null) {
                player.sendMessage(ChatColor.RED.toString() + "Could not convert " + name + " to the Slime format.")
                player.sendMessage(ChatColor.RED.toString() + "Check the console for details.")
                val s: ISetupSession? = api.getSetupSession(player.getUniqueId())
                if (s != null) {
                    s.close()
                }
            }
            getOwner().getLogger().log(Level.WARNING, "Could not convert " + name + " to the Slime format.")
            e.printStackTrace()
        }
    }

    private fun deleteWorldTrash(world: String) {
        for (f in arrayOf<File>(File(Bukkit.getWorldContainer(), world + "/level.dat_mcr"), File(Bukkit.getWorldContainer(), world + "/level.dat_old"), File(Bukkit.getWorldContainer(), world + "/session.lock"), File(Bukkit.getWorldContainer(), world + "/uid.dat"))) {
            if (f.exists()) {
                if (!f.delete()) {
                    getOwner().getLogger().warning("Could not delete: " + f.getPath())
                    getOwner().getLogger().warning("This may cause issues!")
                }
            }
        }
    }

    /**
     * Create level.dat in world folder before converting to Slime format.
     * References:
     * <a href="https://github.com/cijaaimee/Slime-World-Manager/blob/develop/slimeworldmanager-importer/src/main/java/com/grinderwolf/swm/importer/SWMImporter.java">Slime importer requirements</a>
     * <a href="https://wiki.vg/Map_Format#level.dat">level.dat NBT TAG</a>
     *
     * @param world folder name.
     */
    @Throws(IOException::class)
    private fun handleLevelDat(world: String) {
        val worldFolder = File(Bukkit.getWorldContainer(), world)
        if (!worldFolder.exists() || !worldFolder.isDirectory()) {
            return
        }
        val levelFile = File(worldFolder, "level.dat")
        if (levelFile.exists()) {
            return
        }
        // try detecting world version from region files
        val regionFolder = File(worldFolder, "region")
        val regionFiles: Array<File>? = regionFolder.listFiles()
        if (!regionFolder.exists() || null == regionFiles || !regionFolder.isDirectory()) {
            getOwner().getLogger().severe("Tried detecting world version, but it has no regions! (" + world + ")")
            return
        }
        val firstRegion = Arrays.stream(regionFiles).filter({ regionFile -> regionFile.isFile() && regionFile.getName().endsWith(".mca") }).findFirst()
        var dataVersion: Optional<Int> = Optional.empty()
        // try getting world version from NBT TAG
        if (firstRegion.isPresent()) {
            try {
                val inputStream = NBTInputStream(FileInputStream(firstRegion.get()))
                val tag = inputStream.readTag().getAsCompoundTag()
                inputStream.close()
                if (tag.isPresent()) {
                    val dataTag = tag.get().getAsCompoundTag("Chunk")
                    val version = dataTag.flatMap({ tagMap -> tagMap.getIntValue("DataVersion") })
                    if (version.isPresent()) {
                        dataVersion = version
                        Bukkit.getLogger().info("Detected world version from region file for level.dat creation: v" + version.get() + " (" + world + ")")
                    }
                }
            } catch (ignored: Exception) {
            }
        }
        val errorMessage = "Cannot create level.dat in " + worldFolder
        // create new level.dat file and write TAG
        if (!levelFile.createNewFile()) {
            getOwner().getLogger().severe(errorMessage)
            return
        }
        try {
            NBTOutputStream(FileOutputStream(levelFile)).use { outputStream ->
                val cm = CompoundMap()
                cm.put(IntTag("SpawnX", 0))
                cm.put(IntTag("SpawnY", 255))
                cm.put(IntTag("SpawnZ", 0))
                dataVersion.ifPresent({ integer -> cm.put(IntTag("DataVersion", integer)) })
                val dataTag = CompoundTag("Data", cm)
                val rootTag = CompoundMap()
                rootTag.put(dataTag)
                outputStream.writeTag(CompoundTag("", rootTag))
                outputStream.flush()
            }
        } catch (ignored: Exception) {
            try {
                // noinspection ResultOfMethodCallIgnored
                levelFile.delete()
                // clean up empty file
            } catch (ignored2: Exception) {
            }
            getOwner().getLogger().severe(errorMessage)
        }
    }

    private fun getCreateProperties(spawnX: Int, spawnY: Int, spawnZ: Int): SlimePropertyMap {
        val spm = SlimePropertyMap()
        spm.setValue(SlimeProperties.WORLD_TYPE, "flat")
        spm.setValue(SlimeProperties.SPAWN_X, spawnX)
        spm.setValue(SlimeProperties.SPAWN_Y, spawnY)
        spm.setValue(SlimeProperties.SPAWN_Z, spawnZ)
        spm.setValue(SlimeProperties.ALLOW_ANIMALS, false)
        spm.setValue(SlimeProperties.ALLOW_MONSTERS, false)
        spm.setValue(SlimeProperties.DIFFICULTY, "easy")
        spm.setValue(SlimeProperties.PVP, true)
        return spm
    }

    init {
        slime = Bukkit.getPluginManager().getPlugin("SlimeWorldManager") as SlimePlugin
        api = Objects.requireNonNull(Bukkit.getServer().getServicesManager().getRegistration(BedWars::class.java)).getProvider()
    }
}
