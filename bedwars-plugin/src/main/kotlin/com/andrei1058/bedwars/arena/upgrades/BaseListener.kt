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

package com.andrei1058.bedwars.arena.upgrades

import com.andrei1058.bedwars.api.arena.GameState
import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.api.arena.team.ITeam
import com.andrei1058.bedwars.api.events.player.PlayerBaseEnterEvent
import com.andrei1058.bedwars.api.events.player.PlayerBaseLeaveEvent
import com.andrei1058.bedwars.api.events.player.PlayerLeaveArenaEvent
import com.andrei1058.bedwars.arena.Arena
import com.andrei1058.bedwars.arena.team.BedWarsTeam
import java.util.WeakHashMap
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.potion.PotionEffect
import com.andrei1058.bedwars.api.util.unchecked

open class BaseListener : Listener {
    @EventHandler(priority = EventPriority.HIGH)
    open fun onPlayerMove(e: PlayerMoveEvent) {
        val a: IArena? = Arena.getArenaByIdentifier(e.getPlayer().getWorld().getName())
        if (a == null) {
            return
        }
        if (a.getStatus() != GameState.playing) {
            return
        }
        val p = e.getPlayer()
        checkEvents(p, a)
    }

    @EventHandler
    open fun onTeleport(e: PlayerTeleportEvent) {
        val p = e.getPlayer()
        if (isOnABase.containsKey(p)) {
            val a: IArena? = Arena.getArenaByPlayer(p)
            if (a == null) {
                isOnABase.remove(p)
                return
            }
            checkEvents(p, a)
        }
    }

    @EventHandler
    open fun onPlayerDeath(e: PlayerDeathEvent) {
        val a: IArena? = Arena.getArenaByPlayer(e.getEntity())
        if (a == null) {
            return
        }
        checkEvents(e.getEntity(), a)
    }

    @EventHandler
    open fun onBaseEnter(e: PlayerBaseEnterEvent?) {
        if (e == null) {
            return
        }
        val team = e.getTeam()
        if (team.isMember(e.getPlayer())) {
            // Give base effects
            for (ef in team.getBaseEffects()!!) {
                e.getPlayer().addPotionEffect(ef, true)
            }
        } else {
            // Trigger trap
            if (!team.getActiveTraps().isEmpty()) {
                if (!team.isBedDestroyed()) {
                    team.getActiveTraps().get(0).trigger(team, e.getPlayer())
                    team.getActiveTraps().removeAt(0)
                }
            }
            /* Manage trap */
            /*if (team.isTrapActive()) {
                team.disableTrap();
                for (Player mem : team.getMembers()) {
                    if (team.isTrapTitle()) {
                        nms.sendTitle(mem, getMsg(mem, Messages.TRAP_ENEMY_BASE_ENTER_TITLE), null, 0, 50, 0);
                    }
                    if (team.isTrapSubtitle()) {
                        nms.sendTitle(mem, null, getMsg(mem, Messages.TRAP_ENEMY_BASE_ENTER_SUBTITLE), 0, 50, 0);
                    }
                    if (team.isTrapAction()) {
                        nms.playAction(mem, getMsg(mem, Messages.TRAP_ENEMY_BASE_ENTER_ACTION));
                    }
                    if (team.isTrapChat()) {
                        mem.sendMessage(getMsg(mem, Messages.TRAP_ENEMY_BASE_ENTER_CHAT));
                    }
                }
            }*/
        }
    }

    @EventHandler
    open fun onBaseLeave(e: PlayerBaseLeaveEvent?) {
        if (e == null) {
            return
        }
        val t = e.getTeam() as BedWarsTeam
        if (t.isMember(e.getPlayer())) {
            // Remove effects for members
            for (pef in e.getPlayer().getActivePotionEffects()) {
                for (pf in t.getBaseEffects()!!) {
                    if (pef.getType() === pf.getType()) {
                        e.getPlayer().removePotionEffect(pf.getType())
                    }
                }
            }
        }
    }

    @EventHandler
    open fun onArenaLeave(event: PlayerLeaveArenaEvent) {
        isOnABase.remove(event.getPlayer())
    }

    companion object {
        @JvmField var isOnABase: MutableMap<Player, ITeam> = WeakHashMap()

        /**
         * Check the Enter/ Leave events and call them
         */
        @JvmStatic
        private fun checkEvents(p: Player?, a: IArena?) {
            if (p == null || a == null) {
                return
            }
            if (a.isSpectator(p)) {
                return
            }
            if (a.isReSpawning(p)) {
                return
            }
            var notOnBase: Boolean = true
            for (bwt in a.getTeams()!!) {
                /* BaseEnterEvent */
                if (p.getLocation().distance(bwt.getBed()) <= a.getIslandRadius()) {
                    notOnBase = false
                    if (isOnABase.containsKey(p)) {
                        if (isOnABase.get(p) !== bwt) {
                            Bukkit.getPluginManager().callEvent(PlayerBaseLeaveEvent(p, isOnABase.get(p).unchecked()))
                            if (!Arena.magicMilk.containsKey(p.getUniqueId())) {
                                Bukkit.getPluginManager().callEvent(PlayerBaseEnterEvent(p, bwt))
                            }
                            isOnABase.replace(p, bwt)
                        }
                    } else {
                        if (!Arena.magicMilk.containsKey(p.getUniqueId())) {
                            Bukkit.getPluginManager().callEvent(PlayerBaseEnterEvent(p, bwt))
                            isOnABase.put(p, bwt)
                        }
                    }
                }
            }
            /* BaseLeaveEvent */
            if (notOnBase) {
                if (isOnABase.containsKey(p)) {
                    Bukkit.getPluginManager().callEvent(PlayerBaseLeaveEvent(p, isOnABase.get(p).unchecked()))
                    isOnABase.remove(p)
                }
            }
        }
    }
}
