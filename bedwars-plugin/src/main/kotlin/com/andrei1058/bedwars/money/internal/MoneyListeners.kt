package com.andrei1058.bedwars.money.internal

import com.andrei1058.bedwars.BedWars
import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.api.arena.team.ITeam
import com.andrei1058.bedwars.api.events.gameplay.GameEndEvent
import com.andrei1058.bedwars.api.events.player.PlayerBedBreakEvent
import com.andrei1058.bedwars.api.events.player.PlayerKillEvent
import com.andrei1058.bedwars.api.language.Language
import com.andrei1058.bedwars.api.language.Messages
import com.andrei1058.bedwars.configuration.MoneyConfig
import java.util.UUID
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

open class MoneyListeners : Listener {
    /**
     * Create a new winner / loser money reward.
     */
    @EventHandler
    open fun onGameEnd(e: GameEndEvent) {
        for (p in e.getWinners()) {
            val player: Player? = Bukkit.getPlayer(p)
            if (player == null) {
                continue
            }
            val gameWin: Int = MoneyConfig.money.getInt("money-rewards.game-win")
            if (gameWin > 0) {
                BedWars.getEconomy().giveMoney(player, gameWin.toDouble())
                player.sendMessage(Language.getMsg(player, Messages.MONEY_REWARD_WIN).replace("{money}", gameWin.toString()))
            }
            val bwt: ITeam? = e.getArena().getExTeam(player.getUniqueId())
            val arena = e.getArena()
            if (bwt != null) {
                if (arena.getMaxInTeam() > 1) {
                    val teamMate: Int = MoneyConfig.money.getInt("money-rewards.per-teammate")
                    if (teamMate > 0) {
                        BedWars.getEconomy().giveMoney(player, teamMate.toDouble())
                        player.sendMessage(Language.getMsg(player, Messages.MONEY_REWARD_PER_TEAMMATE).replace("{money}", teamMate.toString()))
                    }
                }
            }
        }
        for (p in e.getLosers()) {
            val player: Player? = Bukkit.getPlayer(p)
            if (player == null) {
                continue
            }
            val bwt: ITeam? = e.getArena().getExTeam(player.getUniqueId())
            val arena = e.getArena()
            if (bwt != null) {
                if (arena.getMaxInTeam() > 1) {
                    val teamMate: Int = MoneyConfig.money.getInt("money-rewards.per-teammate")
                    if (teamMate > 0) {
                        BedWars.getEconomy().giveMoney(player, teamMate.toDouble())
                        player.sendMessage(Language.getMsg(player, Messages.MONEY_REWARD_PER_TEAMMATE).replace("{money}", teamMate.toString()))
                    }
                }
            }
        }
    }

    /**
     * Create a new bed destroyed money reward.
     */
    @EventHandler
    open fun onBreakBed(e: PlayerBedBreakEvent) {
        val player: Player? = e.getPlayer()
        if (player == null) {
            return
        }
        val bedDestroy: Int = MoneyConfig.money.getInt("money-rewards.bed-destroyed")
        if (bedDestroy > 0) {
            BedWars.getEconomy().giveMoney(player, bedDestroy.toDouble())
            player.sendMessage(Language.getMsg(player, Messages.MONEY_REWARD_BED_DESTROYED).replace("{money}", bedDestroy.toString()))
        }
    }

    /**
     * Create a kill money reward.
     */
    @EventHandler
    open fun onKill(e: PlayerKillEvent) {
        val player: Player? = e.getKiller()
        val victim = e.getVictim()
        if (player == null || victim == player) {
            return
        }
        val finalKill: Int = MoneyConfig.money.getInt("money-rewards.final-kill")
        val regularKill: Int = MoneyConfig.money.getInt("money-rewards.regular-kill")
        if (e.getCause().isFinalKill()) {
            if (finalKill > 0) {
                BedWars.getEconomy().giveMoney(player, finalKill.toDouble())
                player.sendMessage(Language.getMsg(player, Messages.MONEY_REWARD_FINAL_KILL).replace("{money}", finalKill.toString()))
            }
        } else {
            if (regularKill > 0) {
                BedWars.getEconomy().giveMoney(player, regularKill.toDouble())
                player.sendMessage(Language.getMsg(player, Messages.MONEY_REWARD_REGULAR_KILL).replace("{money}", regularKill.toString()))
            }
        }
    }
}
