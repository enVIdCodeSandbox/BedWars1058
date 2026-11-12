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

package com.andrei1058.bedwars.arena.team

import com.andrei1058.bedwars.BedWars.Companion.getParty
import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.api.arena.team.ITeam
import com.andrei1058.bedwars.api.events.gameplay.TeamAssignEvent
import com.andrei1058.bedwars.arena.Arena
import java.util.ArrayList
import java.util.Collections
import org.bukkit.Bukkit
import org.bukkit.entity.Player

object LegacyTeamAssigner {
    @JvmStatic
    fun assignTeams(arena: IArena) {
        //Check who is having parties
        val skip: MutableList<Player> = ArrayList()
        val owners: MutableList<Player> = ArrayList()
        for (p in arena.getPlayers()!!) {
            if (getParty().hasParty(p) && getParty().isOwner(p)) {
                owners.add(p)
            }
        }
        //Mix teams order
        Collections.shuffle(arena.getTeams())
        //Team-up parties
        for (p in arena.getPlayers()!!) {
            if (owners.contains(p)) {
                for (t in arena.getTeams()!!) {
                    if (skip.contains(p)) {
                        continue
                    }
                    if (t.getSize() + getParty().partySize(p) <= arena.getMaxInTeam()) {
                        skip.add(p)
                        p.closeInventory()
                        val e = TeamAssignEvent(p, t, arena)
                        Bukkit.getPluginManager().callEvent(e)
                        if (!e.isCancelled()) {
                            t.addPlayers(p)
                        }
                        for (mem in getParty().getMembers(p)!!) {
                            if (mem !== p) {
                                val ia: IArena? = Arena.getArenaByPlayer(mem)
                                if (ia == null) {
                                    continue
                                } else if (ia != arena) {
                                    continue
                                }
                                val ee = TeamAssignEvent(p, t, arena)
                                Bukkit.getPluginManager().callEvent(ee)
                                if (!e.isCancelled()) {
                                    t.addPlayers(mem)
                                }
                                skip.add(mem)
                                mem.closeInventory()
                            }
                        }
                    }
                }
            }
        }
        //Give a team to players without a party
        for (p in arena.getPlayers()!!) {
            if (skip.contains(p)) {
                continue
            }
            var addhere = arena.getTeams()!!.get(0)
            for (t in arena.getTeams()!!) {
                if (t.getMembers()!!.size < arena.getMaxInTeam() && t.getMembers()!!.size < addhere.getMembers()!!.size) {
                    addhere = t
                }
            }
            val e = TeamAssignEvent(p, addhere, arena)
            Bukkit.getPluginManager().callEvent(e)
            if (!e.isCancelled()) {
                addhere.addPlayers(p)
            }
            p.closeInventory()
        }
    }
}
