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

package com.andrei1058.bedwars.commands.bedwars.subcmds.regular

import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.api.arena.team.ITeam
import com.andrei1058.bedwars.api.command.ParentCommand
import com.andrei1058.bedwars.api.command.SubCommand
import com.andrei1058.bedwars.arena.Arena
import com.andrei1058.bedwars.upgrades.UpgradesManager
import java.util.ArrayList
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

open class CmdUpgrades(parent: ParentCommand, name: String) : SubCommand(parent, name) {
    override fun execute(args: Array<String>, s: CommandSender): Boolean {
        if (s !is Player) {
            return false
        }
        val a: IArena? = Arena.getArenaByPlayer(s as Player)
        if (a == null) {
            return false
        }
        if (!a.isPlayer(s as Player)) {
            return false
        }
        val t = a.getTeam(s as Player)
        if (t!!.getTeamUpgrades()!!.distance((s as Player).getLocation()) < 4) {
            UpgradesManager.getMenuForArena(a).open(s as Player)
            return true
        }
        return false
    }

    override fun getTabComplete(): MutableList<String>? {
        return ArrayList()
    }

    init {
        showInList(false)
    }
}
