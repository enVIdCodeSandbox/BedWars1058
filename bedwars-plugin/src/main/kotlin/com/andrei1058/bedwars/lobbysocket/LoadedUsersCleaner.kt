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

package com.andrei1058.bedwars.lobbysocket

import com.andrei1058.bedwars.support.preloadedparty.PreLoadedParty
import java.util.LinkedList
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer

open class LoadedUsersCleaner : Runnable {
    private val toRemove: MutableList<LoadedUser> = LinkedList()

    override fun run() {
        for (lu in LoadedUser.getLoaded().values) {
            if (lu.isTimedOut()) {
                toRemove.add(lu)
            }
        }
        if (!toRemove.isEmpty()) {
            toRemove.forEach({ c ->
                val op: OfflinePlayer? = Bukkit.getOfflinePlayer(c.getUuid())
                if (op != null && op.getName() != null) {
                    val plp: PreLoadedParty? = PreLoadedParty.getPartyByOwner(op.getName())
                    if (plp != null) {
                        plp.clean()
                    }
                }
                c.destroy("Removed by cleaner task.")
            })
            toRemove.clear()
        }
    }
}
