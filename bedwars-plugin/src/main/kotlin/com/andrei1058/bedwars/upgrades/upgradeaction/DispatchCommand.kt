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

package com.andrei1058.bedwars.upgrades.upgradeaction

import com.andrei1058.bedwars.api.arena.team.ITeam
import com.andrei1058.bedwars.api.language.Language
import com.andrei1058.bedwars.api.upgrades.UpgradeAction
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import com.andrei1058.bedwars.api.util.unchecked

open class DispatchCommand(private val commandType: CommandType, private val command: String) : UpgradeAction {
    enum class CommandType {
        ONCE_AS_CONSOLE,
        FOREACH_MEMBER_AS_CONSOLE,
        FOREACH_MEMBER_AS_PLAYER;

        fun dispatch(team: ITeam, command: String) {
            var command = command
            when (this) {
                DispatchCommand.CommandType.ONCE_AS_CONSOLE -> {
                    if (command.startsWith("/")) {
                        command = command.replaceFirst("/".toRegex(), "")
                    }
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command)
                }
                DispatchCommand.CommandType.FOREACH_MEMBER_AS_CONSOLE -> {
                    if (command.startsWith("/")) {
                        command = command.replaceFirst("/".toRegex(), "")
                    }
                    for (player in team.getMembers()!!) {
                        val playerName = player.getName()
                        val playerUUID = player.getUniqueId().toString()
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("{player}", playerName).replace("{player_uuid}", playerUUID))
                    }
                }
                DispatchCommand.CommandType.FOREACH_MEMBER_AS_PLAYER -> {
                    if (!command.startsWith("/")) {
                        command = "/" + command
                    }
                    for (player in team.getMembers()!!) {
                        val playerName = player.getName()
                        val playerUUID = player.getUniqueId().toString()
                        player.chat(command.replace("{player}", playerName).replace("{player_uuid}", playerUUID))
                    }
                }
                else -> {}
            }
        }
    }

    override fun onBuy(player: Player?, team: ITeam) {
        val buyerName: String = if (player == null) "null" else player.getName()
        val buyerUUID: String = if (player == null) "null" else player.getUniqueId().toString()
        val teamName = team.getName()
        val teamDisplay = team.getDisplayName(Language.getDefaultLanguage())
        val teamColor = team.getColor().chat().toString()
        val arenaIdentifier = team.getArena()!!.getArenaName()
        val arenaWorld = team.getArena()!!.getWorldName()
        val arenaDisplay = team.getArena()!!.getDisplayName()
        val arenaGroup = team.getArena()!!.getGroup()
        commandType.dispatch(team, command.replace("{buyer}", buyerName).replace("{buyer_uuid}", buyerUUID).replace("{team}", teamName.unchecked()).replace("{team_display}", teamDisplay).replace("{team_color}", teamColor).replace("{arena}", arenaIdentifier).replace("{arena_world}", arenaWorld.unchecked()).replace("{arena_display}", arenaDisplay).replace("{arena_group}", arenaGroup))
    }
}
