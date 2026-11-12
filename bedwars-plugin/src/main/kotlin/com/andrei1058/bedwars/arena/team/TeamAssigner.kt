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

import com.andrei1058.bedwars.BedWars
import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.api.arena.team.ITeam
import com.andrei1058.bedwars.api.arena.team.ITeamAssigner
import com.andrei1058.bedwars.api.events.gameplay.TeamAssignEvent
import java.util.ArrayList
import java.util.Comparator
import java.util.LinkedList
import org.bukkit.Bukkit
import org.bukkit.entity.Player

open class TeamAssigner : ITeamAssigner {
    private val skip: LinkedList<Player> = LinkedList()

    override fun assignTeams(arena: IArena) {
        // team up parties first
        if (arena.getPlayers()!!.size > arena.getMaxInTeam() && arena.getMaxInTeam() > 1) {
            val teams: LinkedList<MutableList<Player>> = LinkedList()
            var members: MutableList<Player>?
            for (player in arena.getPlayers()!!) {
                members = BedWars.getParty().getMembers(player)
                if (members == null) {
                    continue
                }
                members = ArrayList(members)
                if (members.isEmpty()) {
                    continue
                }
                members.removeIf({ member -> !arena.isPlayer(member) })
                if (members.isEmpty()) {
                    continue
                }
                teams.add(members)
            }
            if (!teams.isEmpty()) {
                for (team in arena.getTeams()!!) {
                    // sort
                    teams.sortWith(Comparator.comparingInt { it.size })
                    if (teams.get(0).isEmpty()) {
                        break
                    }
                    var i: Int = 0
                    while (i < arena.getMaxInTeam() && team.getMembers()!!.size < arena.getMaxInTeam()) {
                        if (teams.get(0).size > i) {
                            val toAdd = teams.get(0).removeAt(0)
                            val e = TeamAssignEvent(toAdd, team, arena)
                            Bukkit.getPluginManager().callEvent(e)
                            if (!e.isCancelled()) {
                                toAdd.closeInventory()
                                team.addPlayers(toAdd)
                                skip.add(toAdd)
                            }
                        } else {
                            break
                        }
                        i++
                    }
                }
            }
            // prioritize bigger teams
        }
        for (remaining in arena.getPlayers()!!) {
            if (skip.contains(remaining)) {
                continue
            }
            for (team in arena.getTeams()!!) {
                if (team.getMembers()!!.size < arena.getMaxInTeam()) {
                    val e = TeamAssignEvent(remaining, team, arena)
                    Bukkit.getPluginManager().callEvent(e)
                    if (!e.isCancelled()) {
                        remaining.closeInventory()
                        team.addPlayers(remaining)
                    }
                    break
                }
            }
        }
    }
}
