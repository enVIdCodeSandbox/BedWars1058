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
import com.andrei1058.bedwars.api.arena.generator.GeneratorType
import com.andrei1058.bedwars.api.arena.generator.IGenerator
import com.andrei1058.bedwars.api.arena.team.ITeam
import com.andrei1058.bedwars.api.configuration.ConfigPath
import com.andrei1058.bedwars.api.language.Language
import com.andrei1058.bedwars.api.language.Language.Companion.getList
import com.andrei1058.bedwars.api.language.Language.Companion.getMsg
import com.andrei1058.bedwars.api.language.Messages
import com.andrei1058.bedwars.api.tasks.StartingTask
import com.andrei1058.bedwars.arena.Arena
import com.andrei1058.bedwars.arena.team.BedWarsTeam
import com.andrei1058.bedwars.arena.team.LegacyTeamAssigner
import com.andrei1058.bedwars.configuration.Sounds
import com.andrei1058.bedwars.support.papi.SupportPAPI
import java.util.ArrayList
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask
import com.andrei1058.bedwars.api.util.unchecked

open class GameStartingTask(arena: Arena) : Runnable, StartingTask {
    private var countdown: Int
    private val arena: IArena
    private val task: BukkitTask

    /**
     * Get countdown value
     */
    override fun getCountdown(): Int {
        return countdown
    }

    override fun setCountdown(countdown: Int) {
        this.countdown = countdown
    }

    /**
     * Get arena
     */
    override fun getArena(): IArena {
        return arena
    }

    /**
     * Get task ID
     */
    override fun getTask(): Int {
        return task.getTaskId()
    }

    override fun getBukkitTask(): BukkitTask {
        return task
    }

    override fun run() {
        if (countdown == 0) {
            if (BedWars.config.getBoolean(ConfigPath.GENERAL_CONFIGURATION_EXPERIMENTAL_TEAM_ASSIGNER)) {
                getArena().getTeamAssigner()!!.assignTeams(getArena())
            } else {
                LegacyTeamAssigner.assignTeams(getArena())
            }
            //Disable generators for empty teams if required
            for (team in getArena().getTeams()!!) {
                nms.colorBed(team)
                if (team.getMembers()!!.isEmpty()) {
                    team.setBedDestroyed(true)
                    if (getArena().getConfig()!!.getBoolean(ConfigPath.ARENA_DISABLE_GENERATOR_FOR_EMPTY_TEAMS)) {
                        for (gen in team.getGenerators()) {
                            gen.disable()
                        }
                    }
                }
            }
            Bukkit.getScheduler().runTaskLater(BedWars.plugin, Runnable {
                //Enable diamond/ emerald generators
                for (og in getArena().getOreGenerators()!!) {
                    if (og.getType() == GeneratorType.EMERALD || og.getType() == GeneratorType.DIAMOND) {
                        og.enableRotation()
                    }
                }
            }, 60L)
            //Spawn players
            spawnPlayers()
            //Lobby removal
            BedWars.getAPI().getRestoreAdapter().onLobbyRemoval(arena)
            task.cancel()
            getArena().changeStatus(GameState.playing)
            // Check if emerald should be first based on time
            if (getArena().getUpgradeDiamondsCount() < getArena().getUpgradeEmeraldsCount()) {
                getArena().setNextEvent(NextEvent.DIAMOND_GENERATOR_TIER_II)
            } else {
                getArena().setNextEvent(NextEvent.EMERALD_GENERATOR_TIER_II)
            }
            //Spawn shopkeepers
            for (bwt in getArena().getTeams()!!) {
                bwt.spawnNPCs()
            }
            return
            //Color bed block if possible
            //Destroy bed if team is empty
            //Spawn shops and upgrades
        }
        //Send countdown
        if (getCountdown() % 10 == 0 || getCountdown() <= 5) {
            if (getCountdown() < 5) {
                Sounds.playSound(ConfigPath.SOUNDS_COUNTDOWN_TICK_X + getCountdown(), getArena().getPlayers().unchecked())
            } else {
                Sounds.playSound(ConfigPath.SOUNDS_COUNTDOWN_TICK.unchecked(), getArena().getPlayers().unchecked())
            }
            for (player in getArena().getPlayers()!!) {
                val playerLang = Language.getPlayerLanguage(player)
                val titleSubtitle = Language.getCountDownTitle(playerLang, getCountdown())
                nms.sendTitle(player, titleSubtitle[0], legacyDigits(titleSubtitle[1]), 0, 20, 10)
                player.sendMessage(getMsg(player, Messages.ARENA_STATUS_START_COUNTDOWN_CHAT).replace("{time}", getCountdown().toString()))
            }
        }
        countdown--
    }

    /**
     * The 1.8 font has no circled digits (the default countdown uses them for 5 to 1): show plain digits there.
     */
    private fun legacyDigits(text: String?): String {
        if (nms.getVersion() > 0 || text == null) {
            return text!!
        }
        return text.replace("\u2776", "1").replace("\u2777", "2").replace("\u2778", "3").replace("\u2779", "4").replace("\u277A", "5")
    }

    //Spawn players
    private fun spawnPlayers() {
        for (bwt in getArena().getTeams()!!) {
            for (p in ArrayList(bwt.getMembers())) {
                BedWarsTeam.reSpawnInvulnerability.put(p.getUniqueId(), System.currentTimeMillis() + 2000L)
                bwt.firstSpawn(p)
                Sounds.playSound(ConfigPath.SOUND_GAME_START, p)
                nms.sendTitle(p, getMsg(p, Messages.ARENA_STATUS_START_PLAYER_TITLE), null, 0, 30, 10)
                for (tut in getList(p, Messages.ARENA_STATUS_START_PLAYER_TUTORIAL)) {
                    p.sendMessage(SupportPAPI.getSupportPAPI().replace(p, tut))
                }
            }
        }
    }

    override fun cancel() {
        task.cancel()
    }

    init {
        this.arena = arena
        countdown = BedWars.config.getInt(ConfigPath.GENERAL_CONFIGURATION_START_COUNTDOWN_REGULAR)
        task = Bukkit.getScheduler().runTaskTimer(BedWars.plugin, this, 0, 20L)
    }
}
