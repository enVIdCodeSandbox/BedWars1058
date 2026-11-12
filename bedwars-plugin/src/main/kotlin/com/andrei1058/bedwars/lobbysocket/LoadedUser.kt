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

import com.andrei1058.bedwars.BedWars
import com.andrei1058.bedwars.api.configuration.ConfigPath
import com.andrei1058.bedwars.api.language.Language
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import org.bukkit.Bukkit
import com.andrei1058.bedwars.api.util.unchecked
import com.andrei1058.bedwars.api.util.uninitialized

open class LoadedUser {
    private var uuid: UUID = uninitialized()
    private var partyOwnerOrSpectateTarget: String? = null
    private var toleranceTime: Long = 0L
    private var arenaIdentifier: String = uninitialized()
    private var language: Language? = null

    constructor(uuid: String, arenaIdentifier: String, langIso: String, partyOwnerOrSpectateTarget: String?) {
        if (Bukkit.getWorld(arenaIdentifier) == null) {
            return
        }
        this.arenaIdentifier = arenaIdentifier
        this.uuid = UUID.fromString(uuid)
        if (partyOwnerOrSpectateTarget != null) {
            if (!partyOwnerOrSpectateTarget.isEmpty()) {
                this.partyOwnerOrSpectateTarget = partyOwnerOrSpectateTarget
            }
        }
        this.toleranceTime = System.currentTimeMillis() + waitSeconds
        val l: Language? = Language.getLang(langIso)
        if (l != null) {
            language = l
        }
        loaded.put(this.uuid, this)
    }

    open fun isTimedOut(): Boolean {
        return System.currentTimeMillis() > this.toleranceTime
    }

    open fun getUuid(): UUID {
        return uuid
    }

    open fun getArenaIdentifier(): String {
        return arenaIdentifier
    }

    open fun destroy(reason: String) {
        BedWars.debug("Destroyed PreLoaded User: " + uuid + " Reason: " + reason + ". Tolerance: " + waitSeconds)
        loaded.remove(uuid)
    }

    open fun getLanguage(): Language? {
        return language
    }

    // if arena is started is used as staff teleport target
    open fun getPartyOwnerOrSpectateTarget(): String? {
        return partyOwnerOrSpectateTarget
    }

    companion object {
        private val waitSeconds: Long = BedWars.config.getYml().getLong(ConfigPath.GENERAL_CONFIGURATION_BUNGEE_OPTION_BWP_TIME_OUT)
        private val loaded: ConcurrentHashMap<UUID, LoadedUser> = ConcurrentHashMap()

        @JvmStatic
        fun isPreLoaded(uuid: UUID): Boolean {
            return loaded.containsKey(uuid)
        }

        @JvmStatic
        fun getPreLoaded(uuid: UUID): LoadedUser {
            return loaded.get(uuid).unchecked()
        }

        @JvmStatic
        fun getLoaded(): ConcurrentHashMap<UUID, LoadedUser> {
            return loaded
        }
    }
}
