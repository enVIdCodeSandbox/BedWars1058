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

package com.andrei1058.bedwars.support.papi

import com.andrei1058.bedwars.BedWars
import com.andrei1058.bedwars.api.arena.GameState
import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.api.arena.team.ITeam
import com.andrei1058.bedwars.api.language.Language
import com.andrei1058.bedwars.api.language.Language.Companion.getMsg
import com.andrei1058.bedwars.api.language.Messages
import com.andrei1058.bedwars.arena.Arena
import com.andrei1058.bedwars.commands.shout.ShoutCommand
import com.andrei1058.bedwars.stats.PlayerStats
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.Instant
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.entity.Player
import com.andrei1058.bedwars.api.util.unchecked

open class PAPISupport : PlaceholderExpansion() {
    override fun getIdentifier(): String {
        return "bw1058"
    }

    override fun getAuthor(): String {
        return "andrei1058"
    }

    override fun getVersion(): String {
        return BedWars.plugin.getDescription().getVersion()
    }

    override fun persist(): Boolean {
        return true
    }

    override fun onPlaceholderRequest(player: Player?, s: String): String? {
        if (s.startsWith("arena_status_")) {
            val a: IArena? = Arena.getArenaByName(s.replace("arena_status_", ""))
            if (a == null) {
                return if (player == null) Language.getDefaultLanguage().m(Messages.ARENA_STATUS_RESTARTING_NAME) else Language.getMsg(player, Messages.ARENA_STATUS_RESTARTING_NAME)
            }
            return a.getDisplayStatus(Language.getDefaultLanguage())
        }
        if (s.startsWith("arena_count_")) {
            var players: Int = 0
            val arenas = s.replace("arena_count_", "").split("\\+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            for (arena in arenas) {
                for (a in Arena.getArenasByName(arena)) {
                    players += a.getPlayers()!!.size
                }
            }
            return players.toString()
        }
        if (s.startsWith("arena_instances_")) {
            return Arena.getArenasByName(s.replace("arena_instances_", "")).size.toString()
        }
        if (s.startsWith("group_count_")) {
            return Arena.getPlayers(s.replace("group_count_", "")).toString()
        }
        if (s.startsWith("arena_group_")) {
            val a: String? = s.replace("arena_group_", "")
            val arena: IArena? = Arena.getArenaByName(a.unchecked())
            if (arena != null) {
                return arena.getGroup()
            }
            return "-"
        }
        /* Player required placeholders */
        if (player == null) {
            return null
        }
        // stats placeholders
        if (s.startsWith("stats_")) {
            val targetedStat = s.replaceFirst("stats_".toRegex(), "")
            if (targetedStat.isEmpty() || targetedStat.isBlank()) {
                return null
            }
            val stats: PlayerStats? = BedWars.getStatsManager().getUnsafe(player.getUniqueId())
            if (stats == null) {
                return null
            }
            when (targetedStat) {
                "firstplay" -> {
                    val firstPlay: Instant? = stats.getFirstPlay()
                    return SimpleDateFormat(getMsg(player, Messages.FORMATTING_STATS_DATE_FORMAT)).format(if (firstPlay != null) Timestamp.from(firstPlay) else null)
                }
                "lastplay" -> {
                    val lastPlay: Instant? = stats.getLastPlay()
                    return SimpleDateFormat(getMsg(player, Messages.FORMATTING_STATS_DATE_FORMAT)).format(if (lastPlay != null) Timestamp.from(lastPlay) else null)
                }
                "total_kills" -> {
                    return stats.getTotalKills().toString()
                }
                "kills" -> {
                    return stats.getKills().toString()
                }
                "wins" -> {
                    return stats.getWins().toString()
                }
                "finalkills" -> {
                    return stats.getFinalKills().toString()
                }
                "deaths" -> {
                    return stats.getDeaths().toString()
                }
                "losses" -> {
                    return stats.getLosses().toString()
                }
                "finaldeaths" -> {
                    return stats.getFinalDeaths().toString()
                }
                "bedsdestroyed" -> {
                    return stats.getBedsDestroyed().toString()
                }
                "gamesplayed" -> {
                    return stats.getGamesPlayed().toString()
                }
                else -> {}
            }
        }
        // other placeholders
        var response = ""
        val a: IArena? = Arena.getArenaByPlayer(player)
        when (s) {
            "current_online" -> {
                response = Arena.getArenaByPlayer().size.toString()
            }
            "current_arenas" -> {
                response = Arena.getArenas().size.toString()
            }
            "current_playing" -> {
                if (a != null) {
                    response = a.getPlayers()!!.size.toString()
                }
            }
            "player_team_color" -> {
                if (a != null && a.isPlayer(player) && a.getStatus() == GameState.playing) {
                    val team: ITeam? = a.getTeam(player)
                    if (team != null) {
                        response += team.getColor().chat().toString()
                    }
                }
            }
            "player_team" -> {
                if (a != null) {
                    if (ShoutCommand.isShout(player)) {
                        response += Language.getMsg(player, Messages.FORMAT_PAPI_PLAYER_TEAM_SHOUT)
                    }
                    if (a.isPlayer(player)) {
                        if (a.getStatus() == GameState.playing) {
                            val bwt: ITeam? = a.getTeam(player)
                            if (bwt != null) {
                                response += Language.getMsg(player, Messages.FORMAT_PAPI_PLAYER_TEAM_TEAM).replace("{TeamName}", bwt.getDisplayName(Language.getPlayerLanguage(player))).replace("{TeamColor}", bwt.getColor().chat().toString())
                            }
                        }
                    } else {
                        response += Language.getMsg(player, Messages.FORMAT_PAPI_PLAYER_TEAM_SPECTATOR)
                    }
                }
            }
            "player_level" -> {
                response = BedWars.getLevelSupport().getLevel(player)
            }
            "player_level_raw" -> {
                response = BedWars.getLevelSupport().getPlayerLevel(player).toString()
            }
            "player_progress" -> {
                response = BedWars.getLevelSupport().getProgressBar(player)
            }
            "player_xp_formatted" -> {
                response = BedWars.getLevelSupport().getCurrentXpFormatted(player)
            }
            "player_xp" -> {
                response = BedWars.getLevelSupport().getCurrentXp(player).toString()
            }
            "player_rerq_xp_formatted" -> {
                response = BedWars.getLevelSupport().getRequiredXpFormatted(player)
            }
            "player_rerq_xp" -> {
                response = BedWars.getLevelSupport().getRequiredXp(player).toString()
            }
            "player_status" -> {
                if (a != null) {
                    when (a.getStatus()) {
                        GameState.waiting, GameState.starting -> {
                            response = "WAITING"
                        }
                        GameState.playing -> {
                            if (a.isPlayer(player)) {
                                response = "PLAYING"
                            } else if (a.isSpectator(player)) {
                                response = "SPECTATING"
                            } else {
                                // this shouldnt happen
                                response = "IN_GAME_BUT_NOT"
                            }
                        }
                        GameState.restarting -> {
                            response = "RESTARTING"
                        }
                        else -> {}
                    }
                } else {
                    response = "NONE"
                }
            }
            "current_arena_group" -> {
                if (a != null) {
                    response = a.getGroup()
                }
            }
            "elapsed_time" -> {
                if (a != null) {
                    val startTime: Instant? = a.getStartTime()
                    if (null != startTime) {
                        val time = Duration.ofMillis(Instant.now().minusMillis(startTime.toEpochMilli()).toEpochMilli())
                        if (time.toHours() == 0L) {
                            response = String.format("%02d:%02d", time.toMinutes(), time.toSeconds())
                        } else {
                            response = String.format("%02d:%02d:%02d", time.toHours(), time.toMinutes(), time.toSeconds())
                        }
                    } else {
                        response = ""
                    }
                }
            }
            else -> {}
        }
        return response
    }
}
