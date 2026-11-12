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

package com.andrei1058.bedwars.levels.internal

import com.andrei1058.bedwars.BedWars
import com.andrei1058.bedwars.api.arena.team.ITeam
import com.andrei1058.bedwars.api.events.gameplay.GameEndEvent
import com.andrei1058.bedwars.api.events.player.PlayerBedBreakEvent
import com.andrei1058.bedwars.api.events.player.PlayerKillEvent
import com.andrei1058.bedwars.api.events.player.PlayerLeaveArenaEvent
import com.andrei1058.bedwars.api.events.player.PlayerXpGainEvent
import com.andrei1058.bedwars.api.language.Language
import com.andrei1058.bedwars.api.language.Messages
import com.andrei1058.bedwars.configuration.LevelsConfig
import java.util.UUID
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

open class LevelListeners : Listener {
    //create new level data on player join
    @EventHandler(priority = EventPriority.HIGH)
    open fun onPlayerJoin(e: PlayerJoinEvent) {
        val u = e.getPlayer().getUniqueId()
        // create empty level first
        PlayerLevel(u, 1, 0)
        Bukkit.getScheduler().runTaskAsynchronously(BedWars.plugin, Runnable {
            //if (PlayerLevel.getLevelByPlayer(e.getPlayer().getUniqueId()) != null) return;
            val levelData = BedWars.getRemoteDatabase().getLevelData(u)
            PlayerLevel.getLevelByPlayer(u).lazyLoad(levelData[0] as Int, levelData[1] as Int)
            //new PlayerLevel(e.getPlayer().getUniqueId(), (Integer)levelData[0], (Integer)levelData[1]);
            //Bukkit.broadcastMessage("LAZY LOAD");
        })
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    open fun onPlayerQuit(e: PlayerQuitEvent) {
        val u = e.getPlayer().getUniqueId()
        Bukkit.getScheduler().runTaskAsynchronously(BedWars.plugin, Runnable {
            val pl = PlayerLevel.getLevelByPlayer(u)
            pl.destroy()
        })
    }

    @EventHandler
    open fun onGameEnd(e: GameEndEvent) {
        for (p in e.getWinners()) {
            if (PlayerLevel.getLevelByPlayer(p) != null) {
                val p1: Player? = Bukkit.getPlayer(p)
                if (p1 == null) {
                    continue
                }
                val xpAmount: Int = LevelsConfig.levels.getInt("xp-rewards.game-win")
                if (xpAmount > 0) {
                    PlayerLevel.getLevelByPlayer(p).addXp(xpAmount, PlayerXpGainEvent.XpSource.GAME_WIN)
                    p1.sendMessage(Language.getMsg(p1, Messages.XP_REWARD_WIN).replace("{xp}", xpAmount.toString()))
                }
                val bwt: ITeam? = e.getArena().getExTeam(p1.getUniqueId())
                if (bwt != null) {
                    //noinspection deprecation
                    if (bwt.getMembersCache()!!.size > 1) {
                        val xpAmountPerTmt: Int = LevelsConfig.levels.getInt("xp-rewards.per-teammate")
                        if (xpAmountPerTmt > 0) {
                            //noinspection deprecation
                            val tr: Int = xpAmountPerTmt * bwt.getMembersCache()!!.size
                            PlayerLevel.getLevelByPlayer(p).addXp(tr, PlayerXpGainEvent.XpSource.PER_TEAMMATE)
                            p1.sendMessage(Language.getMsg(p1, "xp-reward-per-teammate").replace("{xp}", tr.toString()))
                        }
                    }
                }
            }
        }
        for (p in e.getLosers()) {
            if (PlayerLevel.getLevelByPlayer(p) != null) {
                val p1: Player? = Bukkit.getPlayer(p)
                if (p1 == null) {
                    continue
                }
                val bwt: ITeam? = e.getArena().getExTeam(p1.getUniqueId())
                if (bwt != null) {
                    //noinspection deprecation
                    if (bwt.getMembersCache()!!.size > 1) {
                        val xpAmountPerTmt: Int = LevelsConfig.levels.getInt("xp-rewards.per-teammate")
                        if (xpAmountPerTmt > 0) {
                            //noinspection deprecation
                            val tr: Int = LevelsConfig.levels.getInt("xp-rewards.per-teammate") * bwt.getMembersCache()!!.size
                            PlayerLevel.getLevelByPlayer(p).addXp(tr, PlayerXpGainEvent.XpSource.PER_TEAMMATE)
                            p1.sendMessage(Language.getMsg(p1, Messages.XP_REWARD_PER_TEAMMATE).replace("{xp}", tr.toString()))
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    open fun onArenaLeave(e: PlayerLeaveArenaEvent) {
        val u = e.getPlayer().getUniqueId()
        Bukkit.getScheduler().runTaskAsynchronously(BedWars.plugin, Runnable {
            val pl: PlayerLevel? = PlayerLevel.getLevelByPlayer(u)
            if (pl != null) {
                pl.updateDatabase()
            }
        })
    }

    @EventHandler
    open fun onBreakBed(e: PlayerBedBreakEvent) {
        val player: Player? = e.getPlayer()
        if (player == null) {
            return
        }
        val bedDestroy: Int = LevelsConfig.levels.getInt("xp-rewards.bed-destroyed")
        if (bedDestroy > 0) {
            PlayerLevel.getLevelByPlayer(player.getUniqueId()).addXp(bedDestroy, PlayerXpGainEvent.XpSource.BED_DESTROYED)
            player.sendMessage(Language.getMsg(player, Messages.XP_REWARD_BED_DESTROY).replace("{xp}", bedDestroy.toString()))
        }
    }

    @EventHandler
    open fun onKill(e: PlayerKillEvent) {
        val player: Player? = e.getKiller()
        val victim = e.getVictim()
        if (player == null || victim == player) {
            return
        }
        val finalKill: Int = LevelsConfig.levels.getInt("xp-rewards.final-kill")
        val regularKill: Int = LevelsConfig.levels.getInt("xp-rewards.regular-kill")
        if (e.getCause().isFinalKill()) {
            if (finalKill > 0) {
                PlayerLevel.getLevelByPlayer(player.getUniqueId()).addXp(finalKill, PlayerXpGainEvent.XpSource.FINAL_KILL)
                player.sendMessage(Language.getMsg(player, Messages.XP_REWARD_FINAL_KILL).replace("{xp}", finalKill.toString()))
            }
        } else {
            if (regularKill > 0) {
                PlayerLevel.getLevelByPlayer(player.getUniqueId()).addXp(regularKill, PlayerXpGainEvent.XpSource.REGULAR_KILL)
                player.sendMessage(Language.getMsg(player, Messages.XP_REWARD_REGULAR_KILL).replace("{xp}", regularKill.toString()))
            }
        }
    }

    init {
        instance = this
    }

    companion object {
        @JvmField var instance: LevelListeners? = null
    }
}
