package com.andrei1058.bedwars.arena.stats

import com.andrei1058.bedwars.api.arena.stats.*
import com.andrei1058.bedwars.api.events.player.PlayerBedBreakEvent
import com.andrei1058.bedwars.api.events.player.PlayerKillEvent
import java.util.Optional
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

/**
 * Used to increase default session stats.
 */
open class DefaultStatsHandler : Listener {
    @EventHandler(ignoreCancelled = true)
    open fun onBedWarsKill(event: PlayerKillEvent) {
        val statsHolder: GameStatsHolder? = event.getArena().getStatsHolder()
        if (null == statsHolder) {
            return
        }
        val killer: Player? = event.getKiller()
        val victim = event.getVictim()
        // INCREMENT KILLER STATS
        if ((killer != null && event.getVictimTeam() != event.getKillerTeam()) && victim != killer) {
            val killerStats = statsHolder.get(killer)
            killerStats.flatMap({ stats -> stats.getStatistic(DefaultStatistics.KILLS) }).ifPresent({ gameStatistic ->
                if (gameStatistic is Incrementable) {
                    (gameStatistic as Incrementable).increment()
                }
            })
            if (event.getCause().isFinalKill()) {
                killerStats.flatMap({ stats -> stats.getStatistic(DefaultStatistics.KILLS_FINAL) }).ifPresent({ gameStatistic ->
                    if (gameStatistic is Incrementable) {
                        (gameStatistic as Incrementable).increment()
                    }
                })
            }
        }
        // INCREMENT VICTIM STATS
        val victimStats = statsHolder.get(victim)
        victimStats.flatMap({ stats -> stats.getStatistic(DefaultStatistics.DEATHS) }).ifPresent({ gameStatistic ->
            if (gameStatistic is Incrementable) {
                (gameStatistic as Incrementable).increment()
            }
        })
        if (event.getCause().isFinalKill()) {
            victimStats.flatMap({ stats -> stats.getStatistic(DefaultStatistics.DEATHS_FINAL) }).ifPresent({ gameStatistic ->
                if (gameStatistic is Incrementable) {
                    (gameStatistic as Incrementable).increment()
                }
            })
        }
    }

    @EventHandler(ignoreCancelled = true)
    open fun onBedWarsBedBreak(event: PlayerBedBreakEvent) {
        if (null == event.getArena().getStatsHolder()) {
            return
        }
        event.getArena().getStatsHolder()!!.get(event.getPlayer()).flatMap({ stats -> stats.getStatistic(DefaultStatistics.BEDS_DESTROYED) }).ifPresent({ st ->
            if (st is Incrementable) {
                (st as Incrementable).increment()
            }
        })
    }
}
