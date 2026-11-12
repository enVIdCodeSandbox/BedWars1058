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

package com.andrei1058.bedwars.api.levels

import com.andrei1058.bedwars.api.events.player.PlayerXpGainEvent
import org.bukkit.entity.Player

interface Level {
    /**
     * @return current player level formatted as string.
     */
    fun getLevel(p: Player): String

    /**
     * @return current player level as number.
     */
    fun getPlayerLevel(p: Player): Int

    /**
     * Get required xp as string.
     * 2000 - 2k
     *
     * @return required xp for next level.
     */
    fun getRequiredXpFormatted(p: Player): String

    /**
     * @return current progress bar.
     */
    fun getProgressBar(p: Player): String

    /**
     * @return current xp.
     */
    fun getCurrentXp(p: Player): Int

    /**
     * @return current xp formatted.
     */
    fun getCurrentXpFormatted(p: Player): String

    /**
     * @return required xp
     */
    fun getRequiredXp(p: Player): Int

    /**
     * Add some xp to target player.
     */
    fun addXp(player: Player, xp: Int, source: PlayerXpGainEvent.XpSource)

    /**
     * Set player xp.
     */
    fun setXp(player: Player, currentXp: Int)

    /**
     * Set player level.
     */
    fun setLevel(player: Player, level: Int)
}
