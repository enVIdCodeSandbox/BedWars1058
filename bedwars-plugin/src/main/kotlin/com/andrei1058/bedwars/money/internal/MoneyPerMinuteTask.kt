package com.andrei1058.bedwars.money.internal

import com.andrei1058.bedwars.BedWars
import com.andrei1058.bedwars.api.language.Language
import com.andrei1058.bedwars.api.language.Messages
import com.andrei1058.bedwars.arena.Arena
import com.andrei1058.bedwars.configuration.MoneyConfig
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask

open class MoneyPerMinuteTask {
    private val money: Int = MoneyConfig.money.getInt("money-rewards.per-minute")
    private var task: BukkitTask? = null

    /**
     * Create a new per minute money reward.
     */
    constructor(arena: Arena?) {
        if (money < 1) {
            return
        }
        task = Bukkit.getScheduler().runTaskTimer(BedWars.plugin, Runnable lambda1@{
            if (null == arena) {
                this.cancel()
                return@lambda1
            }
            for (p in arena.getPlayers()!!) {
                BedWars.getEconomy().giveMoney(p, money.toDouble())
                p.sendMessage(Language.getMsg(p, Messages.MONEY_REWARD_PER_MINUTE).replace("{money}", money.toString()))
            }
        }, 60 * 20, 60 * 20)
    }

    /**
     * Cancel task.
     */
    open fun cancel() {
        if (task != null) {
            task!!.cancel()
        }
    }
}
