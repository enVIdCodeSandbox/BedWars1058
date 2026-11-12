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

package com.andrei1058.bedwars.upgrades.trapaction

import com.andrei1058.bedwars.api.arena.team.ITeam
import com.andrei1058.bedwars.api.upgrades.TrapAction
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

open class PlayerEffectAction(private var potionEffectType: PotionEffectType, private var amplifier: Int, private var duration: Int, private var type: ApplyType) : TrapAction {
    override fun getName(): String {
        return "player-effect"
    }

    override fun onTrigger(player: Player, playerTeam: ITeam, targetTeam: ITeam) {
        if (type == ApplyType.TEAM) {
            for (p in targetTeam.getMembers()!!) {
                p.addPotionEffect(PotionEffect(potionEffectType, duration, amplifier), true)
            }
        } else if (type == ApplyType.BASE) {
            for (p in targetTeam.getMembers()!!) {
                if (p.getLocation().distance(targetTeam.getBed()) <= targetTeam.getArena()!!.getIslandRadius()) {
                    p.addPotionEffect(PotionEffect(potionEffectType, duration, amplifier), true)
                }
            }
        } else if (type == ApplyType.ENEMY) {
            player.addPotionEffect(PotionEffect(potionEffectType, duration, amplifier), true)
        }
    }

    enum class ApplyType {
        TEAM,
        BASE,
        ENEMY;

    }

    init {
        if (duration < 0) {
            this.duration *= -1
        }
        /*if (type == ApplyType.ENEMY_BASE_ENTER && duration <= 0){
        this.duration = 20;
        }*/
        if (duration == 0) {
            this.duration = Integer.MAX_VALUE
        } else {
            this.duration *= 20
        }
    }
}
