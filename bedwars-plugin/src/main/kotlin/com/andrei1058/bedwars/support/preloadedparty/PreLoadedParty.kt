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

package com.andrei1058.bedwars.support.preloadedparty

import com.andrei1058.bedwars.BedWars
import java.util.ArrayList
import java.util.concurrent.ConcurrentHashMap
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import com.andrei1058.bedwars.api.util.unchecked

open class PreLoadedParty(owner: String) {
    private var owner: String?
    private var members: MutableList<Player> = ArrayList()

    open fun addMember(player: Player) {
        members.add(player)
    }

    open fun teamUp() {
        if (this.owner == null) {
            return
        }
        val owner: Player? = Bukkit.getPlayer(this.owner)
        if (owner == null) {
            return
        }
        if (!owner.isOnline()) {
            return
        }
        for (player in members) {
            if (!player.getName().equals(this.owner, ignoreCase = true)) {
                BedWars.getParty().addMember(owner, player)
            }
        }
        preLoadedParties.remove(this.owner)
    }

    open fun clean() {
        preLoadedParties.remove(this.owner)
    }

    init {
        val plp: PreLoadedParty? = getPartyByOwner(owner)
        if (plp != null) {
            plp.clean()
        }
        this.owner = owner
        preLoadedParties.put(owner, this)
    }

    companion object {
        private var preLoadedParties: ConcurrentHashMap<String, PreLoadedParty> = ConcurrentHashMap()

        @JvmStatic
        fun getPartyByOwner(owner: String): PreLoadedParty {
            return preLoadedParties.getOrDefault(owner, null).unchecked()
        }

        @JvmStatic
        fun getPreLoadedParties(): ConcurrentHashMap<String, PreLoadedParty> {
            return preLoadedParties
        }
    }
}
