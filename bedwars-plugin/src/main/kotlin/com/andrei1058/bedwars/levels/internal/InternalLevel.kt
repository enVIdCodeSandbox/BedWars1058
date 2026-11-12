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

package com.andrei1058.bedwars.levels.internal

import com.andrei1058.bedwars.api.events.player.PlayerXpGainEvent
import com.andrei1058.bedwars.api.levels.Level
import org.bukkit.entity.Player

open class InternalLevel : Level {
    override fun getLevel(p: Player): String {
        return PlayerLevel.getLevelByPlayer(p.getUniqueId()).getLevelName()
    }

    override fun getPlayerLevel(p: Player): Int {
        return PlayerLevel.getLevelByPlayer(p.getUniqueId()).getPlayerLevel()
    }

    override fun getRequiredXpFormatted(p: Player): String {
        return PlayerLevel.getLevelByPlayer(p.getUniqueId()).getFormattedRequiredXp()
    }

    override fun getProgressBar(p: Player): String {
        return PlayerLevel.getLevelByPlayer(p.getUniqueId()).getProgress()
    }

    override fun getCurrentXp(p: Player): Int {
        return PlayerLevel.getLevelByPlayer(p.getUniqueId()).getCurrentXp()
    }

    override fun getCurrentXpFormatted(p: Player): String {
        return PlayerLevel.getLevelByPlayer(p.getUniqueId()).getFormattedCurrentXp()
    }

    override fun getRequiredXp(p: Player): Int {
        return PlayerLevel.getLevelByPlayer(p.getUniqueId()).getNextLevelCost()
    }

    override fun addXp(player: Player, xp: Int, source: PlayerXpGainEvent.XpSource) {
        PlayerLevel.getLevelByPlayer(player.getUniqueId()).addXp(xp, source)
    }

    override fun setXp(player: Player, currentXp: Int) {
        PlayerLevel.getLevelByPlayer(player.getUniqueId()).setXp(currentXp)
    }

    override fun setLevel(player: Player, level: Int) {
        PlayerLevel.getLevelByPlayer(player.getUniqueId()).setLevel(level)
    }
}
