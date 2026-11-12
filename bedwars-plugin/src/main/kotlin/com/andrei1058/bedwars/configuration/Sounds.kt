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
import com.andrei1058.bedwars.BedWars.Companion.plugin
import com.andrei1058.bedwars.api.arena.NextEvent
import com.andrei1058.bedwars.api.configuration.ConfigManager
import com.andrei1058.bedwars.api.configuration.ConfigPath.SOUNDS_BED_DESTROY
import com.andrei1058.bedwars.api.configuration.ConfigPath.SOUNDS_BED_DESTROY_OWN
import com.andrei1058.bedwars.api.configuration.ConfigPath.SOUNDS_BOUGHT
import com.andrei1058.bedwars.api.configuration.ConfigPath.SOUNDS_COUNTDOWN_TICK
import com.andrei1058.bedwars.api.configuration.ConfigPath.SOUNDS_COUNTDOWN_TICK_X
import com.andrei1058.bedwars.api.configuration.ConfigPath.SOUNDS_INSUFF_MONEY
import com.andrei1058.bedwars.api.configuration.ConfigPath.SOUNDS_KILL
import com.andrei1058.bedwars.api.configuration.ConfigPath.SOUND_GAME_START
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import com.andrei1058.bedwars.api.util.unchecked

object Sounds {
    private val sounds: ConfigManager = ConfigManager(plugin, "sounds", plugin.getDataFolder().getPath())

    @JvmStatic
    fun init() {
        val yml = sounds.getYml()
        addDefSound("game-end", BedWars.getForCurrentVersion("AMBIENCE_THUNDER", "ENTITY_LIGHTNING_THUNDER", "ITEM_TRIDENT_THUNDER"))
        addDefSound("rejoin-denied", BedWars.getForCurrentVersion("VILLAGER_NO", "ENTITY_VILLAGER_NO", "ENTITY_VILLAGER_NO"))
        addDefSound("rejoin-allowed", BedWars.getForCurrentVersion("SLIME_WALK", "ENTITY_SLIME_JUMP", "ENTITY_SLIME_JUMP"))
        addDefSound("spectate-denied", BedWars.getForCurrentVersion("VILLAGER_NO", "ENTITY_VILLAGER_NO", "ENTITY_VILLAGER_NO"))
        addDefSound("spectate-allowed", BedWars.getForCurrentVersion("SLIME_WALK", "ENTITY_SLIME_JUMP", "ENTITY_SLIME_JUMP"))
        addDefSound("join-denied", BedWars.getForCurrentVersion("VILLAGER_NO", "ENTITY_VILLAGER_NO", "ENTITY_VILLAGER_NO"))
        addDefSound("join-allowed", BedWars.getForCurrentVersion("SLIME_WALK", "ENTITY_SLIME_JUMP", "ENTITY_SLIME_JUMP"))
        addDefSound("spectator-gui-click", BedWars.getForCurrentVersion("SLIME_WALK", "ENTITY_SLIME_JUMP", "ENTITY_SLIME_JUMP"))
        addDefSound(SOUNDS_COUNTDOWN_TICK, BedWars.getForCurrentVersion("CHICKEN_EGG_POP", "ENTITY_CHICKEN_EGG", "ENTITY_CHICKEN_EGG"))
        addDefSound(SOUNDS_COUNTDOWN_TICK_X + "5", BedWars.getForCurrentVersion("CHICKEN_EGG_POP", "ENTITY_CHICKEN_EGG", "ENTITY_CHICKEN_EGG"))
        addDefSound(SOUNDS_COUNTDOWN_TICK_X + "4", BedWars.getForCurrentVersion("CHICKEN_EGG_POP", "ENTITY_CHICKEN_EGG", "ENTITY_CHICKEN_EGG"))
        addDefSound(SOUNDS_COUNTDOWN_TICK_X + "3", BedWars.getForCurrentVersion("CHICKEN_EGG_POP", "ENTITY_CHICKEN_EGG", "ENTITY_CHICKEN_EGG"))
        addDefSound(SOUNDS_COUNTDOWN_TICK_X + "2", BedWars.getForCurrentVersion("CHICKEN_EGG_POP", "ENTITY_CHICKEN_EGG", "ENTITY_CHICKEN_EGG"))
        addDefSound(SOUNDS_COUNTDOWN_TICK_X + "1", BedWars.getForCurrentVersion("CHICKEN_EGG_POP", "ENTITY_CHICKEN_EGG", "ENTITY_CHICKEN_EGG"))
        addDefSound(SOUND_GAME_START, BedWars.getForCurrentVersion("SLIME_ATTACK", "BLOCK_SLIME_FALL", "BLOCK_SLIME_BLOCK_FALL"))
        addDefSound(SOUNDS_KILL, BedWars.getForCurrentVersion("ORB_PICKUP", "ENTITY_EXPERIENCE_ORB_PICKUP", "ENTITY_EXPERIENCE_ORB_PICKUP"))
        addDefSound(SOUNDS_BED_DESTROY, BedWars.getForCurrentVersion("ENDERDRAGON_GROWL", "ENTITY_ENDERDRAGON_GROWL", "ENTITY_ENDER_DRAGON_GROWL"))
        addDefSound(SOUNDS_BED_DESTROY_OWN, BedWars.getForCurrentVersion("WITHER_DEATH", "ENTITY_WITHER_DEATH", "ENTITY_WITHER_DEATH"))
        addDefSound(SOUNDS_INSUFF_MONEY, BedWars.getForCurrentVersion("VILLAGER_NO", "ENTITY_VILLAGER_NO", "ENTITY_VILLAGER_NO"))
        addDefSound(SOUNDS_BOUGHT, BedWars.getForCurrentVersion("VILLAGER_YES", "ENTITY_VILLAGER_YES", "ENTITY_VILLAGER_YES"))
        addDefSound(NextEvent.BEDS_DESTROY.getSoundPath(), BedWars.getForCurrentVersion("ENDERDRAGON_GROWL", "ENTITY_ENDERDRAGON_GROWL", "ENTITY_ENDER_DRAGON_GROWL"))
        addDefSound(NextEvent.DIAMOND_GENERATOR_TIER_II.getSoundPath(), BedWars.getForCurrentVersion("LEVEL_UP", "ENTITY_PLAYER_LEVELUP", "ENTITY_PLAYER_LEVELUP"))
        addDefSound(NextEvent.DIAMOND_GENERATOR_TIER_III.getSoundPath(), BedWars.getForCurrentVersion("LEVEL_UP", "ENTITY_PLAYER_LEVELUP", "ENTITY_PLAYER_LEVELUP"))
        addDefSound(NextEvent.EMERALD_GENERATOR_TIER_II.getSoundPath(), BedWars.getForCurrentVersion("GHAST_MOAN", "ENTITY_GHAST_WARN", "ENTITY_GHAST_WARN"))
        addDefSound(NextEvent.EMERALD_GENERATOR_TIER_III.getSoundPath(), BedWars.getForCurrentVersion("GHAST_MOAN", "ENTITY_GHAST_WARN", "ENTITY_GHAST_WARN"))
        addDefSound(NextEvent.ENDER_DRAGON.getSoundPath(), BedWars.getForCurrentVersion("ENDERDRAGON_WINGS", "ENTITY_ENDERDRAGON_FLAP", "ENTITY_ENDER_DRAGON_FLAP"))
        addDefSound("player-re-spawn", BedWars.getForCurrentVersion("SLIME_ATTACK", "BLOCK_SLIME_FALL", "BLOCK_SLIME_BLOCK_FALL"))
        addDefSound("arena-selector-open", BedWars.getForCurrentVersion("CHICKEN_EGG_POP", "ENTITY_CHICKEN_EGG", "ENTITY_CHICKEN_EGG"))
        addDefSound("stats-gui-open", BedWars.getForCurrentVersion("CHICKEN_EGG_POP", "ENTITY_CHICKEN_EGG", "ENTITY_CHICKEN_EGG"))
        addDefSound("trap-sound", BedWars.getForCurrentVersion("ENDERMAN_TELEPORT", "ENDERMAN_TELEPORT", "ENTITY_ENDERMAN_TELEPORT"))
        addDefSound("shop-auto-equip", BedWars.getForCurrentVersion("HORSE_ARMOR", "ITEM_ARMOR_EQUIP_GENERIC", "ITEM_ARMOR_EQUIP_GENERIC"))
        addDefSound("egg-bridge-block", BedWars.getForCurrentVersion("CHICKEN_EGG_POP", "ENTITY_CHICKEN_EGG", "ENTITY_CHICKEN_EGG"))
        addDefSound("ender-pearl-landed", BedWars.getForCurrentVersion("ENDERMAN_TELEPORT", "ENTITY_ENDERMEN_TELEPORT", "ENTITY_ENDERMAN_TELEPORT"))
        addDefSound("pop-up-tower-build", BedWars.getForCurrentVersion("CHICKEN_EGG_POP", "ENTITY_CHICKEN_EGG", "ENTITY_CHICKEN_EGG"))
        yml.options().copyDefaults(true)
        // remove old paths
        yml.set("bought", null)
        yml.set("insufficient-money", null)
        yml.set("player-kill", null)
        yml.set("countdown", null)
        sounds.save()
    }

    @JvmStatic
    private fun getSound(path: String): Sound {
        try {
            return BedWars.nms.getSound(sounds.getString(path + ".sound"))
        } catch (ex: Exception) {
            return BedWars.nms.getSound(BedWars.getForCurrentVersion("AMBIENCE_THUNDER", "ENTITY_LIGHTNING_THUNDER", "ITEM_TRIDENT_THUNDER"))
        }
    }

    @JvmStatic
    fun playSound(path: String?, players: MutableList<Player>) {
        if (path.equals("none", ignoreCase = true)) {
            return
        }
        val sound: Sound? = getSound(path.unchecked())
        val volume: Int = getSounds().getInt(path + ".volume")
        val pitch: Int = getSounds().getInt(path + ".pitch")
        if (sound != null) {
            players.forEach({ p -> p.playSound(p.getLocation(), sound, volume.toFloat(), pitch.toFloat()) })
        }
    }

    /**
     * @return true if sound is valid and it was played.
     */
    @JvmStatic
    fun playSound(sound: Sound?, players: MutableList<Player>): Boolean {
        if (sound == null) {
            return false
        }
        players.forEach({ p -> p.playSound(p.getLocation(), sound, 1f, 1f) })
        return true
    }

    @JvmStatic
    fun playSound(path: String?, player: Player) {
        val sound: Sound? = getSound(path.unchecked())
        val volume: Float = getSounds().getYml().getDouble(path + ".volume").toFloat()
        val pitch: Float = getSounds().getYml().getDouble(path + ".pitch").toFloat()
        if (sound != null) {
            player.playSound(player.getLocation(), sound, volume, pitch)
        }
    }

    @JvmStatic
    fun getSounds(): ConfigManager {
        return sounds
    }

    @JvmStatic
    private fun addDefSound(path: String, value: String) {
        // convert old paths
        if (getSounds().getYml().get(path) != null && getSounds().getYml().get(path + ".volume") == null) {
            val temp = getSounds().getYml().getString(path)
            getSounds().getYml().set(path, null)
            getSounds().getYml().set(path + ".sound", temp)
        }
        getSounds().getYml().addDefault(path + ".sound", value)
        getSounds().getYml().addDefault(path + ".volume", 1)
        getSounds().getYml().addDefault(path + ".pitch", 1)
    }

    @JvmStatic
    fun playsoundArea(path: String, location: Location, x: Float, y: Float) {
        val sound: Sound? = getSound(path)
        if (sound != null) {
            location.getWorld().playSound(location, sound, x, y)
        }
    }
}
