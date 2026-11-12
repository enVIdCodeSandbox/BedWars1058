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

package com.andrei1058.bedwars.api.entity

import com.andrei1058.bedwars.api.BedWars
import com.andrei1058.bedwars.api.arena.team.ITeam
import com.andrei1058.bedwars.api.events.player.PlayerKillEvent
import com.andrei1058.bedwars.api.language.Messages
import java.util.UUID
import org.bukkit.Bukkit
import org.bukkit.entity.LivingEntity
import com.andrei1058.bedwars.api.util.uninitialized

open class Despawnable {
    private var e: LivingEntity? = null
    private var team: ITeam? = null
    private var despawn: Int = 250
    private var namePath: String = uninitialized()
    private var deathRegularCause: PlayerKillEvent.PlayerKillCause = uninitialized()
    private var deathFinalCause: PlayerKillEvent.PlayerKillCause = uninitialized()
    private var uuid: UUID = uninitialized()

    constructor(e: LivingEntity?, team: ITeam, despawn: Int, namePath: String, deathFinalCause: PlayerKillEvent.PlayerKillCause, deathRegularCause: PlayerKillEvent.PlayerKillCause) {
        this.e = e
        if (e == null) {
            return
        }
        this.uuid = e.getUniqueId()
        this.team = team
        this.deathFinalCause = deathFinalCause
        this.deathRegularCause = deathRegularCause
        if (despawn != 0) {
            this.despawn = despawn
        }
        this.namePath = namePath
        if (api == null) {
            api = Bukkit.getServer().getServicesManager().getRegistration(BedWars::class.java).getProvider()
        }
        api!!.getVersionSupport().getDespawnablesList().put(uuid, this)
        this.setName()
    }

    open fun refresh() {
        if (e!!.isDead() || e == null || team == null || team!!.getArena() == null) {
            api!!.getVersionSupport().getDespawnablesList().remove(uuid)
            if (team!!.getArena() == null) {
                e!!.damage(e!!.getHealth() + 100)
            }
            return
        }
        setName()
        despawn--
        if (despawn == 0) {
            e!!.damage(e!!.getHealth() + 100)
            api!!.getVersionSupport().getDespawnablesList().remove(e!!.getUniqueId())
        }
    }

    private fun setName() {
        val percentuale: Int = ((e!!.getHealth() * 100) / e!!.getMaxHealth() / 10).toInt()
        var name = api!!.getDefaultLang().m(namePath).replace("{despawn}", despawn.toString()).replace("{health}", String(CharArray(percentuale)).replace("\u0000", api!!.getDefaultLang().m(Messages.FORMATTING_DESPAWNABLE_UTILITY_NPC_HEALTH)) + String(CharArray(10 - percentuale)).replace("\u0000", "§7" + api!!.getDefaultLang().m(Messages.FORMATTING_DESPAWNABLE_UTILITY_NPC_HEALTH)))
        if (team != null) {
            name = name.replace("{TeamColor}", team!!.getColor().chat().toString()).replace("{TeamName}", team!!.getDisplayName(api!!.getDefaultLang()))
        }
        e!!.setCustomName(name)
    }

    open fun getEntity(): LivingEntity? {
        return e
    }

    open fun getTeam(): ITeam? {
        return team
    }

    open fun getDespawn(): Int {
        return despawn
    }

    open fun getDeathFinalCause(): PlayerKillEvent.PlayerKillCause {
        return deathFinalCause
    }

    open fun getDeathRegularCause(): PlayerKillEvent.PlayerKillCause {
        return deathRegularCause
    }

    open fun destroy() {
        if (getEntity() != null) {
            getEntity()!!.damage(Integer.MAX_VALUE.toDouble())
        }
        team = null
        api!!.getVersionSupport().getDespawnablesList().remove(uuid)
    }

    override fun equals(obj: Any?): Boolean {
        if (obj is LivingEntity) {
            return (obj as LivingEntity).getUniqueId() == e!!.getUniqueId()
        }
        return false
    }

    companion object {
        private var api: BedWars? = null
    }
}
