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

import com.andrei1058.bedwars.api.arena.generator.GeneratorType
import com.andrei1058.bedwars.api.arena.generator.IGenerator
import com.andrei1058.bedwars.api.arena.team.ITeam
import com.andrei1058.bedwars.api.upgrades.UpgradeAction
import com.andrei1058.bedwars.arena.OreGenerator
import java.util.ArrayList
import java.util.stream.Collectors
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import com.andrei1058.bedwars.api.util.unchecked

open class GeneratorEditAction(private val type: ApplyType, private val amount: Int, private val delay: Int, private val limit: Int) : UpgradeAction {
    override fun onBuy(player: Player?, bwt: ITeam) {
        var generator: MutableList<IGenerator> = ArrayList()
        if (type == ApplyType.IRON) {
            generator = bwt.getGenerators().stream().filter({ g -> g.getType() == GeneratorType.IRON }).collect(Collectors.toList())
        } else if (type == ApplyType.GOLD) {
            generator = bwt.getGenerators().stream().filter({ g -> g.getType() == GeneratorType.GOLD }).collect(Collectors.toList())
        } else if (type == ApplyType.EMERALD) {
            if (!bwt.getArena()!!.getConfig()!!.getArenaLocations("Team." + bwt.getName() + ".Emerald").isEmpty()) {
                for (l in bwt.getArena()!!.getConfig()!!.getArenaLocations("Team." + bwt.getName() + ".Emerald")) {
                    val gen: IGenerator = OreGenerator(l, bwt.getArena().unchecked(), GeneratorType.CUSTOM, bwt)
                    gen.setOre(ItemStack(Material.EMERALD))
                    gen.setType(GeneratorType.EMERALD)
                    bwt.getGenerators().add(gen)
                    //bwt.getArena().getOreGenerators().add(gen);
                    generator.add(gen)
                }
            } else {
                val gen: IGenerator = OreGenerator(bwt.getGenerators().get(0).getLocation()!!.clone(), bwt.getArena().unchecked(), GeneratorType.CUSTOM, bwt)
                gen.setOre(ItemStack(Material.EMERALD))
                gen.setType(GeneratorType.EMERALD)
                bwt.getGenerators().add(gen)
                //bwt.getArena().getOreGenerators().add(gen);
                generator.add(gen)
            }
        }
        for (g in generator) {
            g.setAmount(amount)
            g.setDelay(delay)
            g.setSpawnLimit(limit)
        }
    }

    enum class ApplyType {
        IRON,
        GOLD,
        EMERALD;

    }
}
