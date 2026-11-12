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

package com.andrei1058.bedwars.stats

import com.andrei1058.bedwars.BedWars
import com.andrei1058.bedwars.api.arena.GameState
import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.api.arena.team.ITeam
import com.andrei1058.bedwars.api.events.gameplay.GameEndEvent
import com.andrei1058.bedwars.api.events.player.PlayerBedBreakEvent
import com.andrei1058.bedwars.api.events.player.PlayerKillEvent
import com.andrei1058.bedwars.api.events.player.PlayerLeaveArenaEvent
import com.andrei1058.bedwars.arena.Arena
import java.time.Instant
import java.util.UUID
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerLoginEvent
import org.bukkit.event.player.PlayerQuitEvent

open class StatsListener : Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    open fun onAsyncPreLoginEvent(event: AsyncPlayerPreLoginEvent) {
        if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) {
            // Do nothing if login fails
            return
        }
        val stats = BedWars.getRemoteDatabase().fetchStats(event.getUniqueId())
        stats.setName(event.getName())
        BedWars.getStatsManager().put(event.getUniqueId(), stats)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    open fun onPlayerLoginEvent(event: PlayerLoginEvent) {
        if (event.getResult() != PlayerLoginEvent.Result.ALLOWED) {
            // Prevent memory leak if login fails
            BedWars.getStatsManager().remove(event.getPlayer().getUniqueId())
        }
    }

    @EventHandler
    open fun onBedBreak(event: PlayerBedBreakEvent) {
        val stats = BedWars.getStatsManager().get(event.getPlayer().getUniqueId())
        //store beds destroyed
        stats.setBedsDestroyed(stats.getBedsDestroyed() + 1)
    }

    @EventHandler
    open fun onPlayerKill(event: PlayerKillEvent) {
        val victimStats = BedWars.getStatsManager().get(event.getVictim().getUniqueId())
        // If killer is not null and not equal to victim
        val killerStats: PlayerStats? = if (event.getVictim() != event.getKiller()) (if (event.getKiller() == null) null else BedWars.getStatsManager().getUnsafe(event.getKiller()!!.getUniqueId())) else null
        if (event.getCause().isFinalKill()) {
            //store final deaths
            victimStats.setFinalDeaths(victimStats.getFinalDeaths() + 1)
            //store losses
            victimStats.setLosses(victimStats.getLosses() + 1)
            //store games played
            victimStats.setGamesPlayed(victimStats.getGamesPlayed() + 1)
            //store final kills
            if (killerStats != null) {
                killerStats.setFinalKills(killerStats.getFinalKills() + 1)
            }
        } else {
            //store deaths
            victimStats.setDeaths(victimStats.getDeaths() + 1)
            //store kills
            if (killerStats != null) {
                killerStats.setKills(killerStats.getKills() + 1)
            }
        }
    }

    @EventHandler
    open fun onGameEnd(event: GameEndEvent) {
        for (uuid in event.getWinners()) {
            val player: Player? = Bukkit.getPlayer(uuid)
            if (player == null) {
                continue
            }
            if (!player.isOnline()) {
                continue
            }
            val stats = BedWars.getStatsManager().get(uuid)
            // the ones who abandoned are already removed from the winners list
            stats.setWins(stats.getWins() + 1)
            // for those who left games played are updated in arena leave listener
            val playerArena: IArena? = Arena.getArenaByPlayer(player)
            if (playerArena != null && playerArena == event.getArena()) {
                stats.setGamesPlayed(stats.getGamesPlayed() + 1)
            }
            // store wins even if is in another game because he assisted this team
            // store games played
            // give if he remained in this arena till the end even if was eliminated
        }
    }

    @EventHandler
    open fun onArenaLeave(event: PlayerLeaveArenaEvent) {
        val player = event.getPlayer()
        val team: ITeam? = event.getArena().getExTeam(player.getUniqueId())
        if (team == null) {
            // The player didn't play this game
            return
        }
        if (event.getArena().getStatus() == GameState.starting || event.getArena().getStatus() == GameState.waiting) {
            // Game didn't start
            return
        }
        val playerStats: PlayerStats? = BedWars.getStatsManager().getUnsafe(player.getUniqueId())
        // sometimes can be null due to scheduling delays
        if (playerStats == null) {
            return
        }
        // Update last play and first play (if required)
        val now = Instant.now()
        playerStats.setLastPlay(now)
        if (playerStats.getFirstPlay() == null) {
            playerStats.setFirstPlay(now)
        }
        //save or replace stats for player - run later because PlayerKillEvent is triggered after PlayerLeaveArenaEvent
        Bukkit.getScheduler().runTaskLaterAsynchronously(BedWars.plugin, Runnable { BedWars.getRemoteDatabase().saveStats(playerStats) }, 10L)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    open fun onQuit(event: PlayerQuitEvent) {
        BedWars.getStatsManager().remove(event.getPlayer().getUniqueId())
    }
}
