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

package com.andrei1058.bedwars.configuration

import com.andrei1058.bedwars.BedWars
import com.andrei1058.bedwars.api.configuration.ConfigManager
import com.andrei1058.bedwars.api.configuration.ConfigPath
import com.andrei1058.bedwars.api.configuration.GameMainOverridable
import java.lang.reflect.Field
import java.util.ArrayList
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.Plugin

open class ArenaConfig(plugin: Plugin, name: String, dir: String) : ConfigManager(plugin, name, dir) {
    private var cachedGameOverridables: MutableList<String> = ArrayList()

    private fun getGameOverridables(): MutableList<String> {
        val paths: MutableList<String> = ArrayList()
        for (field in ConfigPath::class.java.getDeclaredFields()) {
            if (field.isAnnotationPresent(GameMainOverridable::class.java)) {
                try {
                    val value = field.get(field)
                    if (value is String) {
                        paths.add(value as String)
                    }
                } catch (ignored: IllegalAccessException) {
                }
            }
        }
        return paths
    }

    open fun isGameOverridable(path: String): Boolean {
        return cachedGameOverridables.contains(path)
    }

    open fun getGameOverridableValue(path: String): Any {
        if (!isGameOverridable(path)) {
            throw RuntimeException("Given path is not game-overridable: " + path)
        }
        val value: Any? = getYml().get(path, null)
        if (null == value) {
            return BedWars.config.getYml().get(path)
        }
        return value
    }

    open fun getGameOverridableBoolean(path: String): Boolean {
        val value = getGameOverridableValue(path)
        return if (value is Boolean) value as Boolean else false
    }

    open fun getGameOverridableString(path: String): String {
        val value = getGameOverridableValue(path)
        return if (value is String) value as String else "invalid"
    }

    init {
        val yml = getYml()
        yml.options().header(plugin.getName() + " arena configuration file.\n" + "Documentation here: https://gitlab.com/andrei1058/BedWars1058/wikis/configuration/Arena-Configuration")
        yml.addDefault("group", "Default")
        yml.addDefault(ConfigPath.ARENA_DISPLAY_NAME, "")
        yml.addDefault("minPlayers", 2)
        yml.addDefault("maxInTeam", 1)
        yml.addDefault("allowSpectate", true)
        yml.addDefault(ConfigPath.ARENA_SPAWN_PROTECTION, 5)
        yml.addDefault(ConfigPath.ARENA_SHOP_PROTECTION, 1)
        yml.addDefault(ConfigPath.ARENA_UPGRADES_PROTECTION, 1)
        yml.addDefault(ConfigPath.ARENA_GENERATOR_PROTECTION, 1)
        yml.addDefault(ConfigPath.ARENA_ISLAND_RADIUS, 17)
        yml.addDefault("worldBorder", 300)
        yml.addDefault(ConfigPath.ARENA_Y_LEVEL_KILL, -1)
        //yml.addDefault("disableGeneratorsOnOrphanIslands", false);
        yml.addDefault(ConfigPath.ARENA_CONFIGURATION_MAX_BUILD_Y, 180)
        yml.addDefault(ConfigPath.ARENA_DISABLE_GENERATOR_FOR_EMPTY_TEAMS, false)
        yml.addDefault(ConfigPath.ARENA_DISABLE_NPCS_FOR_EMPTY_TEAMS, true)
        yml.addDefault(ConfigPath.ARENA_NORMAL_DEATH_DROPS, false)
        yml.addDefault(ConfigPath.ARENA_USE_BED_HOLO, true)
        yml.addDefault(ConfigPath.ARENA_ALLOW_MAP_BREAK, false)
        val rules: ArrayList<String> = ArrayList()
        rules.add("doDaylightCycle:false")
        rules.add("announceAdvancements:false")
        rules.add("doInsomnia:false")
        rules.add("doImmediateRespawn:true")
        rules.add("doWeatherCycle:false")
        rules.add("doFireTick:false")
        yml.addDefault(ConfigPath.ARENA_GAME_RULES, rules)
        yml.options().copyDefaults(true)
        save()
        //convert old configuration
        if (yml.get("spawnProtection") != null) {
            set(ConfigPath.ARENA_SPAWN_PROTECTION, yml.getInt("spawnProtection"))
            set("spawnProtection", null)
        }
        if (yml.get("shopProtection") != null) {
            set(ConfigPath.ARENA_SHOP_PROTECTION, yml.getInt("shopProtection"))
            set("shopProtection", null)
        }
        if (yml.get("upgradesProtection") != null) {
            set(ConfigPath.ARENA_UPGRADES_PROTECTION, yml.getInt("upgradesProtection"))
            set("upgradesProtection", null)
        }
        if (yml.get("islandRadius") != null) {
            set(ConfigPath.ARENA_ISLAND_RADIUS, yml.getInt("islandRadius"))
        }
        if (yml.get("voidKill") != null) {
            set("voidKill", null)
        }
        set(ConfigPath.GENERAL_CONFIGURATION_ENABLE_GEN_SPLIT, null)
        cachedGameOverridables = getGameOverridables()
    }
}
