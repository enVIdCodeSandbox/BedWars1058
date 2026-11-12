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

package com.andrei1058.bedwars.arena.tasks

import com.andrei1058.bedwars.BedWars
import com.andrei1058.bedwars.BedWars.Companion.nms
import com.andrei1058.bedwars.api.arena.GameState
import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.api.arena.NextEvent
import com.andrei1058.bedwars.api.arena.generator.IGenerator
import com.andrei1058.bedwars.api.arena.team.ITeam
import com.andrei1058.bedwars.api.configuration.ConfigPath
import com.andrei1058.bedwars.api.events.player.PlayerInvisibilityPotionEvent
import com.andrei1058.bedwars.api.language.Language
import com.andrei1058.bedwars.api.language.Language.Companion.getMsg
import com.andrei1058.bedwars.api.language.Messages
import com.andrei1058.bedwars.api.tasks.PlayingTask
import com.andrei1058.bedwars.arena.Arena
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitTask
import com.andrei1058.bedwars.api.util.unchecked

open class GamePlayingTask(private var arena: Arena) : Runnable, PlayingTask {
    private var task: BukkitTask
    private var beds_destroy_countdown: Int
    private var dragon_spawn_countdown: Int
    private var game_end_countdown: Int

    override fun getArena(): Arena {
        return arena
    }

    override fun getBukkitTask(): BukkitTask {
        return task
    }

    /**
     * Get task ID
     */
    override fun getTask(): Int {
        return task.getTaskId()
    }

    override fun getBedsDestroyCountdown(): Int {
        return beds_destroy_countdown
    }

    override fun getDragonSpawnCountdown(): Int {
        return dragon_spawn_countdown
    }

    override fun getGameEndCountdown(): Int {
        return game_end_countdown
    }

    override fun run() {
        when (getArena().getNextEvent()) {
            NextEvent.EMERALD_GENERATOR_TIER_II, NextEvent.EMERALD_GENERATOR_TIER_III, NextEvent.DIAMOND_GENERATOR_TIER_II, NextEvent.DIAMOND_GENERATOR_TIER_III -> {
                if (getArena().upgradeDiamondsCount > 0) {
                    getArena().upgradeDiamondsCount--
                    if (getArena().upgradeDiamondsCount == 0) {
                        getArena().updateNextEvent()
                    }
                }
                if (getArena().upgradeEmeraldsCount > 0) {
                    getArena().upgradeEmeraldsCount--
                    if (getArena().upgradeEmeraldsCount == 0) {
                        getArena().updateNextEvent()
                    }
                }
            }
            NextEvent.BEDS_DESTROY -> {
                beds_destroy_countdown--
                if (getBedsDestroyCountdown() == 0) {
                    for (p in getArena().getPlayers()!!) {
                        nms.sendTitle(p, getMsg(p, Messages.NEXT_EVENT_TITLE_ANNOUNCE_BEDS_DESTROYED), getMsg(p, Messages.NEXT_EVENT_SUBTITLE_ANNOUNCE_BEDS_DESTROYED), 0, 40, 10)
                        p.sendMessage(getMsg(p, Messages.NEXT_EVENT_CHAT_ANNOUNCE_BEDS_DESTROYED))
                    }
                    for (p in getArena().getSpectators()!!) {
                        nms.sendTitle(p, getMsg(p, Messages.NEXT_EVENT_TITLE_ANNOUNCE_BEDS_DESTROYED), getMsg(p, Messages.NEXT_EVENT_SUBTITLE_ANNOUNCE_BEDS_DESTROYED), 0, 40, 10)
                        p.sendMessage(getMsg(p, Messages.NEXT_EVENT_CHAT_ANNOUNCE_BEDS_DESTROYED))
                    }
                    for (t in getArena().getTeams()!!) {
                        t.setBedDestroyed(true)
                    }
                    getArena().updateNextEvent()
                }
            }
            NextEvent.ENDER_DRAGON -> {
                dragon_spawn_countdown--
                if (getDragonSpawnCountdown() == 0) {
                    for (p in getArena().getPlayers()!!) {
                        nms.sendTitle(p, getMsg(p, Messages.NEXT_EVENT_TITLE_ANNOUNCE_SUDDEN_DEATH), getMsg(p, Messages.NEXT_EVENT_SUBTITLE_ANNOUNCE_SUDDEN_DEATH), 0, 40, 10)
                        for (t in getArena().getTeams()!!) {
                            if (t.getMembers()!!.isEmpty()) {
                                continue
                            }
                            p.sendMessage(getMsg(p, Messages.NEXT_EVENT_CHAT_ANNOUNCE_SUDDEN_DEATH).replace("{TeamDragons}", t.getDragons().toString()).replace("{TeamColor}", t.getColor().chat().toString()).replace("{TeamName}", t.getDisplayName(Language.getPlayerLanguage(p))))
                        }
                    }
                    for (p in getArena().getSpectators()!!) {
                        nms.sendTitle(p, getMsg(p, Messages.NEXT_EVENT_TITLE_ANNOUNCE_SUDDEN_DEATH), getMsg(p, Messages.NEXT_EVENT_SUBTITLE_ANNOUNCE_SUDDEN_DEATH), 0, 40, 10)
                        for (t in getArena().getTeams()!!) {
                            if (t.getMembers()!!.isEmpty()) {
                                continue
                            }
                            p.sendMessage(getMsg(p, Messages.NEXT_EVENT_CHAT_ANNOUNCE_SUDDEN_DEATH).replace("{TeamDragons}", t.getDragons().toString()).replace("{TeamColor}", t.getColor().chat().toString()).replace("{TeamName}", t.getDisplayName(Language.getPlayerLanguage(p))))
                        }
                    }
                    getArena().updateNextEvent()
                    for (team in arena.getTeams()!!) {
                        for (o in team.getGenerators()) {
                            val l = o.getLocation()
                            for (y in 0 until 20) {
                                l!!.clone().subtract(0.0, y.toDouble(), 0.0).getBlock().setType(Material.AIR)
                            }
                        }
                    }
                    for (t in getArena().getTeams()!!) {
                        if (t.getMembers()!!.isEmpty()) {
                            continue
                        }
                        var x: Int = 0
                        while (x < t.getDragons()) {
                            nms.spawnDragon(getArena().getConfig()!!.getArenaLoc("waiting.Loc")!!.add(0.0, 10.0, 0.0), t)
                            x++
                        }
                    }
                }
            }
            NextEvent.GAME_END -> {
                game_end_countdown--
                if (getGameEndCountdown() == 0) {
                    getArena().checkWinner()
                    getArena().changeStatus(GameState.restarting)
                }
            }
            else -> {}
        }
        var distance: Int = 0
        for (t in getArena().getTeams()!!) {
            if (t.getSize() > 1) {
                for (p in t.getMembers()!!) {
                    for (p2 in t.getMembers()!!) {
                        if (p2 === p) {
                            continue
                        }
                        if (distance == 0) {
                            distance = p.getLocation().distance(p2.getLocation()).toInt()
                        } else if ((p.getLocation().distance(p2.getLocation()).toInt()) < distance) {
                            distance = p.getLocation().distance(p2.getLocation()).toInt()
                        }
                    }
                    nms.playAction(p, getMsg(p, Messages.FORMATTING_ACTION_BAR_TRACKING).replace("{team}", t.getColor().chat().toString() + t.getDisplayName(Language.getPlayerLanguage(p))).replace("{distance}", t.getColor().chat().toString() + distance).replace("&", "§"))
                }
            }
            // spawn items
            for (o in t.getGenerators()) {
                o.spawn()
            }
        }
        /* AFK SYSTEM FOR PLAYERS */
        var current: Int = 0
        for (p in getArena().getPlayers()!!) {
            if (Arena.afkCheck.get(p.getUniqueId()) == null) {
                Arena.afkCheck.put(p.getUniqueId(), current)
            } else {
                current = Arena.afkCheck.get(p.getUniqueId()).unchecked()
                current++
                Arena.afkCheck.replace(p.getUniqueId(), current)
                if (current == 45) {
                    BedWars.getAPI().getAFKUtil().setPlayerAFK(p, true)
                }
            }
        }
        /* RESPAWN SESSION */
        if (!getArena().getRespawnSessions()!!.isEmpty()) {
            for (e in getArena().getRespawnSessions()!!.entries) {
                if (e.value <= 0) {
                    val a: IArena? = Arena.getArenaByPlayer(e.key)
                    if (a == null) {
                        getArena().getRespawnSessions()!!.remove(e.key)
                        continue
                    }
                    val t: ITeam? = a.getTeam(e.key)
                    if (t == null) {
                        a.addSpectator(e.key, true, null)
                    } else {
                        t.respawnMember(e.key)
                        e.key.setAllowFlight(false)
                        e.key.setFlying(false)
                    }
                } else {
                    nms.sendTitle(e.key, getMsg(e.key, Messages.PLAYER_DIE_RESPAWN_TITLE).replace("{time}", e.value.toString()), getMsg(e.key, Messages.PLAYER_DIE_RESPAWN_SUBTITLE).replace("{time}", e.value.toString()), 0, 30, 10)
                    e.key.sendMessage(getMsg(e.key, Messages.PLAYER_DIE_RESPAWN_CHAT).replace("{time}", e.value.toString()))
                    getArena().getRespawnSessions()!!.replace(e.key, e.value - 1)
                }
            }
        }
        /* INVISIBILITY FOR ARMOR */
        if (!getArena().getShowTime()!!.isEmpty()) {
            for (e in getArena().getShowTime()!!.entries) {
                if (e.value <= 0) {
                    for (p in e.key.getWorld().getPlayers()) {
                        nms.showArmor(e.key, p)
                        //nms.showPlayer(e.getKey(), p);
                    }
                    e.key.removePotionEffect(PotionEffectType.INVISIBILITY)
                    getArena().getShowTime()!!.remove(e.key)
                    Bukkit.getPluginManager().callEvent(PlayerInvisibilityPotionEvent(PlayerInvisibilityPotionEvent.Type.REMOVED, getArena().getTeam(e.key).unchecked(), e.key, getArena()))
                } else {
                    getArena().getShowTime()!!.replace(e.key, e.value - 1)
                }
            }
        }
        /* SPAWN ITEMS */
        for (o in getArena().getOreGenerators()!!) {
            o.spawn()
        }
    }

    override fun cancel() {
        task.cancel()
    }

    init {
        this.beds_destroy_countdown = BedWars.config.getInt(ConfigPath.GENERAL_CONFIGURATION_BEDS_DESTROY_COUNTDOWN)
        this.dragon_spawn_countdown = BedWars.config.getInt(ConfigPath.GENERAL_CONFIGURATION_DRAGON_SPAWN_COUNTDOWN)
        this.game_end_countdown = BedWars.config.getInt(ConfigPath.GENERAL_CONFIGURATION_GAME_END_COUNTDOWN)
        this.task = Bukkit.getScheduler().runTaskTimer(BedWars.plugin, this, 0, 20L)
    }
}
