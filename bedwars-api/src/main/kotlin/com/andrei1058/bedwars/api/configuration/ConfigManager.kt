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

package com.andrei1058.bedwars.api.configuration

import java.io.File
import java.io.IOException
import java.util.ArrayList
import java.util.logging.Level
import java.util.stream.Collectors
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.Plugin
import com.andrei1058.bedwars.api.util.uninitialized

open class ConfigManager {
    private var yml: YamlConfiguration = uninitialized()
    private var config: File = uninitialized()
    private var name: String = uninitialized()
    private var firstTime: Boolean = false

    /**
     * Create a new configuration file.
     *
     * @param plugin config owner.
     * @param name   config name. Do not include .yml in it.
     */
    constructor(plugin: Plugin, name: String, dir: String) {
        val d = File(dir)
        if (!d.exists()) {
            if (!d.mkdirs()) {
                plugin.getLogger().log(Level.SEVERE, "Could not create " + d.getPath())
                return
            }
        }
        config = File(dir, name + ".yml")
        if (!config.exists()) {
            firstTime = true
            plugin.getLogger().log(Level.INFO, "Creating " + config.getPath())
            try {
                if (!config.createNewFile()) {
                    plugin.getLogger().log(Level.SEVERE, "Could not create " + config.getPath())
                    return
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        yml = YamlConfiguration.loadConfiguration(config)
        yml.options().copyDefaults(true)
        this.name = name
    }

    /**
     * Reload configuration.
     */
    open fun reload() {
        yml = YamlConfiguration.loadConfiguration(config)
    }

    /**
     * Convert a location to an arena location syntax
     */
    open fun stringLocationArenaFormat(loc: Location): String {
        return loc.getX().toString() + "," + loc.getY() + "," + loc.getZ() + "," + (loc.getYaw().toDouble()) + "," + (loc.getPitch().toDouble())
    }

    /**
     * Convert a location to a string for general use
     * Use {@link #stringLocationArenaFormat(Location)} for arena locations
     */
    open fun stringLocationConfigFormat(loc: Location): String {
        return loc.getX().toString() + "," + loc.getY() + "," + loc.getZ() + "," + (loc.getYaw().toDouble()) + "," + (loc.getPitch().toDouble()) + "," + loc.getWorld().getName()
    }

    /**
     * Save a general location to the config.
     * Use {@link #saveArenaLoc(String, Location)} for arena locations
     */
    open fun saveConfigLoc(path: String, loc: Location) {
        val data = loc.getX().toString() + "," + loc.getY() + "," + loc.getZ() + "," + (loc.getYaw().toDouble()) + "," + (loc.getPitch().toDouble()) + "," + loc.getWorld().getName()
        yml.set(path, data)
        save()
    }

    /**
     * Save a location for arena use
     */
    open fun saveArenaLoc(path: String, loc: Location) {
        val data = loc.getX().toString() + "," + loc.getY() + "," + loc.getZ() + "," + (loc.getYaw().toDouble()) + "," + (loc.getPitch().toDouble())
        yml.set(path, data)
        save()
    }

    /**
     * Get a general location
     * Use {@link #getArenaLoc(String)} for locations stored using {@link #saveArenaLoc(String, Location)}
     */
    open fun getConfigLoc(path: String): Location? {
        val d: String? = yml.getString(path)
        if (d == null) {
            return null
        }
        val data = d.replace("[", "").replace("]", "").split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        return Location(Bukkit.getWorld(data[5]), java.lang.Double.parseDouble(data[0]), java.lang.Double.parseDouble(data[1]), java.lang.Double.parseDouble(data[2]), java.lang.Float.parseFloat(data[3]), java.lang.Float.parseFloat(data[4]))
    }

    /**
     * Get a location for arena use
     * Use {@link #getConfigLoc(String)} (String)} for locations stored using {@link #saveConfigLoc(String, Location)} (String, Location)}
     */
    open fun getArenaLoc(path: String): Location? {
        val d: String? = yml.getString(path)
        if (d == null) {
            return null
        }
        val data = d.replace("[", "").replace("]", "").split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        return Location(Bukkit.getWorld(name), java.lang.Double.parseDouble(data[0]), java.lang.Double.parseDouble(data[1]), java.lang.Double.parseDouble(data[2]), java.lang.Float.parseFloat(data[3]), java.lang.Float.parseFloat(data[4]))
    }

    /**
     * Convert string to arena location syntax
     */
    open fun convertStringToArenaLocation(string: String): Location {
        val data = string.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        return Location(Bukkit.getWorld(name), java.lang.Double.parseDouble(data[0]), java.lang.Double.parseDouble(data[1]), java.lang.Double.parseDouble(data[2]), java.lang.Float.parseFloat(data[3]), java.lang.Float.parseFloat(data[4]))
    }

    /**
     * Get list of arena locations at given path
     */
    open fun getArenaLocations(path: String): MutableList<Location> {
        val l: MutableList<Location> = ArrayList()
        for (s in yml.getStringList(path)) {
            val loc: Location? = convertStringToArenaLocation(s)
            if (loc != null) {
                l.add(loc)
            }
        }
        return l
    }

    /**
     * Set data to config
     */
    open fun set(path: String, value: Any?) {
        yml.set(path, value)
        save()
    }

    /**
     * Get yml instance
     */
    open fun getYml(): YamlConfiguration {
        return yml
    }

    /**
     * Save config changes to file
     */
    open fun save() {
        try {
            yml.save(config)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * Get list of strings at given path
     *
     * @return a list of string with colors translated
     */
    open fun getList(path: String): MutableList<String> {
        return yml.getStringList(path).stream().map({ s -> s.replace("&", "§") }).collect(Collectors.toList())
    }

    /**
     * Get boolean at given path
     */
    open fun getBoolean(path: String): Boolean {
        return yml.getBoolean(path)
    }

    /**
     * Get Integer at given path
     */
    open fun getInt(path: String): Int {
        return yml.getInt(path)
    }

    open fun getDouble(path: String): Double {
        return yml.getDouble(path)
    }

    /**
     * Get string at given path
     */
    open fun getString(path: String): String {
        return yml.getString(path)
    }

    /**
     * Check if the config file was created for the first time
     * Can be used to add default values
     */
    open fun isFirstTime(): Boolean {
        return firstTime
    }

    /**
     * Compare two arena locations
     * Return true if same location
     */
    open fun compareArenaLoc(l1: Location, l2: Location): Boolean {
        return l1.getBlockX() == l2.getBlockX() && l1.getBlockZ() == l2.getBlockZ() && l1.getBlockY() == l2.getBlockY()
    }

    /**
     * Get config name
     */
    open fun getName(): String {
        return name
    }

    /**
     * Change internal name.
     */
    open fun setName(name: String) {
        this.name = name
    }
}
