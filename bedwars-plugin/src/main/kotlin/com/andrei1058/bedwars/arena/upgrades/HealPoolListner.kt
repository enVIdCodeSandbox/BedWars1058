package com.andrei1058.bedwars.arena.upgrades

import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.api.arena.team.ITeam
import com.andrei1058.bedwars.api.events.gameplay.GameEndEvent
import com.andrei1058.bedwars.api.events.player.PlayerLeaveArenaEvent
import com.andrei1058.bedwars.api.events.server.ArenaDisableEvent
import com.andrei1058.bedwars.api.events.team.TeamEliminatedEvent
import com.andrei1058.bedwars.api.events.upgrades.UpgradeBuyEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

open class HealPoolListner : Listener {
    @EventHandler
    open fun onTeamUpgrade(e: UpgradeBuyEvent) {
        if (e.getTeamUpgrade().getName()!!.contains("heal-pool")) {
            val a: IArena? = e.getArena()
            if (a == null) {
                return
            }
            val bwt: ITeam? = a.getTeam(e.getPlayer())
            if (bwt == null) {
                return
            }
            if (!HealPoolTask.exists(a, bwt)) {
                HealPoolTask(bwt)
            }
        }
    }

    @EventHandler
    open fun onDisable(e: ArenaDisableEvent) {
        HealPoolTask.removeForArena(e.getWorldName())
    }

    @EventHandler
    open fun onEnd(e: GameEndEvent) {
        HealPoolTask.removeForArena(e.getArena())
    }

    @EventHandler
    open fun teamDead(e: TeamEliminatedEvent) {
        HealPoolTask.removeForTeam(e.getTeam())
    }

    @EventHandler
    open fun LastLeave(event: PlayerLeaveArenaEvent) {
        if (event.getArena().getPlayers()!!.isEmpty()) {
            HealPoolTask.removeForArena(event.getArena())
        }
    }
}
