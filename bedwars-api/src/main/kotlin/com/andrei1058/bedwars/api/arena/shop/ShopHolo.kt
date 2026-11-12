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

package com.andrei1058.bedwars.api.arena.shop

import com.andrei1058.bedwars.api.BedWars
import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.api.language.Language
import java.util.ArrayList
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import com.andrei1058.bedwars.api.util.unchecked
import com.andrei1058.bedwars.api.util.uninitialized

open class ShopHolo {
    private var iso: String = uninitialized()
    private var a1: ArmorStand? = null
    private var a2: ArmorStand? = null
    private var l: Location? = null
    private var a: IArena = uninitialized()

    constructor(iso: String, a1: ArmorStand?, a2: ArmorStand?, l: Location, a: IArena) {
        this.l = l
        for (sh in getShopHolo()) {
            if (sh.l === l && sh.iso.equals(iso, ignoreCase = true)) {
                if (a1 != null) {
                    a1.remove()
                }
                if (a2 != null) {
                    a2.remove()
                }
                return
            }
        }
        this.a1 = a1
        this.a2 = a2
        this.iso = iso
        this.a = a
        if (a1 != null) {
            a1.setMarker(true)
        }
        if (a2 != null) {
            a2.setMarker(true)
        }
        shopHolo.add(this)
        if (api == null) {
            api = Bukkit.getServer().getServicesManager().getRegistration(BedWars::class.java).getProvider()
        }
    }

    open fun update() {
        if (l == null) {
            Bukkit.broadcastMessage("LOCATION IS NULL")
        }
        for (p2 in l!!.getWorld().getPlayers()) {
            if (Language.getPlayerLanguage(p2).getIso().equals(iso, ignoreCase = true)) {
                continue
            }
            if (a1 != null) {
                api!!.getVersionSupport().hideEntity(a1.unchecked(), p2)
            }
            if (a2 != null) {
                api!!.getVersionSupport().hideEntity(a2.unchecked(), p2)
            }
        }
    }

    open fun updateForPlayer(p: Player, lang: String) {
        if (lang.equals(iso, ignoreCase = true)) {
            return
        }
        if (a1 != null) {
            api!!.getVersionSupport().hideEntity(a1.unchecked(), p)
        }
        if (a2 != null) {
            api!!.getVersionSupport().hideEntity(a2.unchecked(), p)
        }
    }

    open fun getA(): IArena {
        return a
    }

    open fun getIso(): String {
        return iso
    }

    companion object {
        /**
         * Shop holograms per language <iso, holo></iso,>
         */
        private var shopHolo: MutableList<ShopHolo> = ArrayList()
        private var api: BedWars? = null

        @JvmStatic
        fun clearForArena(a: IArena) {
            for (sh in ArrayList(getShopHolo())) {
                if (sh.a === a) {
                    shopHolo.remove(sh)
                }
            }
        }

        @JvmStatic
        fun getShopHolo(): MutableList<ShopHolo> {
            return shopHolo
        }
    }
}
