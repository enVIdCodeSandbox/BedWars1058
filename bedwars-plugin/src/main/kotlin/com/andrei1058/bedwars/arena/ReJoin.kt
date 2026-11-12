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

package com.andrei1058.bedwars.arena

import com.andrei1058.bedwars.BedWars
import com.andrei1058.bedwars.api.arena.GameState
import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.api.arena.team.ITeam
import com.andrei1058.bedwars.api.configuration.ConfigPath
import com.andrei1058.bedwars.api.language.Language
import com.andrei1058.bedwars.api.language.Language.Companion.getMsg
import com.andrei1058.bedwars.api.language.Messages
import com.andrei1058.bedwars.arena.tasks.ReJoinTask
import com.andrei1058.bedwars.configuration.Sounds
import com.andrei1058.bedwars.lobbysocket.ArenaSocket
import com.andrei1058.bedwars.shop.ShopCache
import com.google.gson.JsonObject
import java.util.ArrayList
import java.util.Collections
import java.util.UUID
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.entity.Player
import com.andrei1058.bedwars.api.util.uninitialized

open class ReJoin {
    private var player: UUID = uninitialized()
    private var arena: IArena? = null
    private var bwt: ITeam? = null
    private var task: ReJoinTask? = null
    private val permanentsAndNonDowngradables: ArrayList<ShopCache.CachedItem> = ArrayList()

    /**
     * Make rejoin possible for a player
     */
    constructor(player: Player, arena: IArena, bwt: ITeam?, cachedArmor: MutableList<ShopCache.CachedItem>) {
        val rj: ReJoin? = getPlayer(player)
        if (rj != null) {
            rj.destroy(true)
        }
        if (bwt == null) {
            return
        }
        if (bwt.isBedDestroyed()) {
            return
        }
        this.bwt = bwt
        this.player = player.getUniqueId()
        this.arena = arena
        reJoinList.add(this)
        BedWars.debug("Created ReJoin for " + player.getName() + " " + player.getUniqueId() + " at " + arena.getArenaName())
        if (bwt.getMembers()!!.isEmpty()) {
            task = ReJoinTask(arena, bwt)
        }
        this.permanentsAndNonDowngradables.addAll(cachedArmor)
        if (BedWars.autoscale) {
            val json = JsonObject()
            json.addProperty("type", "RC")
            json.addProperty("uuid", player.getUniqueId().toString())
            json.addProperty("arena_id", arena.getWorldName())
            json.addProperty("server", BedWars.config.getString(ConfigPath.GENERAL_CONFIGURATION_BUNGEE_OPTION_SERVER_ID))
            ArenaSocket.sendMessage(json.toString())
        }
    }

    /**
     * Check if can reJoin
     */
    open fun canReJoin(): Boolean {
        BedWars.debug("ReJoin canReJoin  check.")
        if (arena == null) {
            BedWars.debug("ReJoin canReJoin arena is null " + player.toString())
            destroy(true)
            return false
        }
        if (arena!!.getStatus() == GameState.restarting) {
            BedWars.debug("ReJoin canReJoin status is restarting " + player.toString())
            destroy(true)
            return false
        }
        if (bwt == null) {
            BedWars.debug("ReJoin canReJoin bwt is null " + player.toString())
            destroy(true)
            return false
        }
        if (bwt!!.isBedDestroyed()) {
            BedWars.debug("ReJoin canReJoin bed is destroyed " + player.toString())
            destroy(false)
            return false
        }
        return true
    }

    /**
     * Make a player re-join the arena
     */
    open fun reJoin(player: Player): Boolean {
        Sounds.playSound("rejoin-allowed", player)
        player.sendMessage(Language.getMsg(player, Messages.REJOIN_ALLOWED).replace("{arena}", getArena()!!.getDisplayName()))
        if (player.getGameMode() != GameMode.SURVIVAL) {
            Bukkit.getScheduler().runTaskLater(BedWars.plugin, Runnable {
                player.setGameMode(GameMode.SURVIVAL)
                player.setAllowFlight(true)
                player.setFlying(true)
            }, 20L)
        }
        return arena!!.reJoin(player)
    }

    /**
     * Destroy data and rejoin possibility
     */
    open fun destroy(destroyTeam: Boolean) {
        BedWars.debug("ReJoin destroy for " + player.toString())
        reJoinList.remove(this)
        val json = JsonObject()
        json.addProperty("type", "RD")
        json.addProperty("uuid", player.toString())
        json.addProperty("server", BedWars.config.getString(ConfigPath.GENERAL_CONFIGURATION_BUNGEE_OPTION_SERVER_ID))
        ArenaSocket.sendMessage(json.toString())
        if (bwt != null && destroyTeam && bwt!!.getMembers()!!.isEmpty()) {
            bwt!!.setBedDestroyed(true)
            if (bwt != null) {
                for (p2 in arena!!.getPlayers()!!) {
                    p2.sendMessage(getMsg(p2, Messages.TEAM_ELIMINATED_CHAT).replace("{TeamColor}", bwt!!.getColor().chat().toString()).replace("{TeamName}", bwt!!.getDisplayName(Language.getPlayerLanguage(p2))))
                }
                for (p2 in arena!!.getSpectators()!!) {
                    p2.sendMessage(getMsg(p2, Messages.TEAM_ELIMINATED_CHAT).replace("{TeamColor}", bwt!!.getColor().chat().toString()).replace("{TeamName}", bwt!!.getDisplayName(Language.getPlayerLanguage(p2))))
                }
            }
            arena!!.checkWinner()
        }
    }

    /**
     * Get Player
     */
    open fun getPlayer(): UUID {
        return player
    }

    /**
     * Get player team
     */
    open fun getBwt(): ITeam? {
        return bwt
    }

    /**
     * Get arena
     */
    open fun getArena(): IArena? {
        return arena
    }

    open fun getTask(): ReJoinTask? {
        return task
    }

    open fun getPl(): UUID {
        return player
    }

    open fun getPermanentsAndNonDowngradables(): MutableList<ShopCache.CachedItem> {
        return permanentsAndNonDowngradables
    }

    override fun equals(o: Any?): Boolean {
        if (o == null) {
            return false
        }
        if (o !is ReJoin) {
            return false
        }
        val reJoin = o as ReJoin
        return reJoin.getPl() == getPl()
    }

    companion object {
        private val reJoinList: MutableList<ReJoin> = ArrayList()

        /**
         * Check if a player has stored data
         */
        @JvmStatic
        fun exists(pl: Player): Boolean {
            BedWars.debug("ReJoin exists check " + pl.getUniqueId())
            for (rj in getReJoinList()) {
                BedWars.debug("ReJoin exists check list scroll: " + rj.getPl().toString())
                if (rj.getPl() == pl.getUniqueId()) {
                    return true
                }
            }
            return false
        }

        /**
         * Get a player ReJoin
         */
        @JvmStatic
        fun getPlayer(player: Player): ReJoin? {
            BedWars.debug("ReJoin getPlayer " + player.getUniqueId())
            for (rj in getReJoinList()) {
                if (rj.getPl() == player.getUniqueId()) {
                    return rj
                }
            }
            return null
        }

        @JvmStatic
        fun getReJoinList(): MutableList<ReJoin> {
            return Collections.unmodifiableList(reJoinList)
        }
    }
}
