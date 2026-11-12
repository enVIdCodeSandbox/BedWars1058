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

package com.andrei1058.bedwars.api.arena.generator

import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.api.arena.team.ITeam
import java.util.HashMap
import org.bukkit.Location
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

interface IGenerator {
    /**
     * Get holograms associated to this generator.
     * Language iso, Hologram instance.
     */
    fun getLanguageHolograms(): HashMap<String, IGenHolo>?

    /**
     * Disable a generator and destroy its data.
     */
    fun disable()

    /**
     * Manage what to do when the generator upgrade is called from {@link com.andrei1058.bedwars.api.arena.IArena#updateNextEvent}
     */
    fun upgrade()

    /**
     * This will attempt to spawn the items every second.
     */
    fun spawn()

    /**
     * This will drop the item at a given location.
     *
     * @param location You can customize this location in order to drop items near a player if it's a base generator with multiple teammates.
     */
    fun dropItem(location: Location)

    /**
     * Change the item that this generator will spawn.
     */
    fun setOre(ore: ItemStack)

    /**
     * Get the arena assigned to this generator.
     */
    fun getArena(): IArena?

    /**
     * This method is called every tick to manage the block rotation.
     */
    fun rotate()

    /**
     * Change item spawn delay. In seconds.
     */
    fun setDelay(delay: Int)

    /**
     * Set how many items should the generator spawn at once.
     */
    fun setAmount(amount: Int)

    /**
     * Get the generator location.
     */
    fun getLocation(): Location?

    /**
     * Get generator ore.
     */
    fun getOre(): ItemStack?

    /**
     * This will hide generator holograms with a different iso.
     *
     * @param iso player language iso.
     */
    fun updateHolograms(p: Player, iso: String)

    /**
     * Enable generator rotation.
     * Make sure it has a helmet set.
     * DIAMOND and EMERALD generator types will get
     * the rotation activated when the arena starts.
     * If you want to have a different rotating type you should call this manually at {@link com.andrei1058.bedwars.api.events.gameplay.GameStateChangeEvent}
     */
    fun enableRotation()

    /**
     * This is the limit when the generator will stop spawning new items until they are collected.
     */
    fun setSpawnLimit(value: Int)

    /**
     * Get the team assigned to this generator.
     *
     * @return null if this is not a team generator.
     */
    fun getBwt(): ITeam?

    /**
     * Get generator hologram holder (armor stand) containing the rotating item.
     *
     * @return null if there is no rotating item.
     */
    fun getHologramHolder(): ArmorStand?

    /**
     * Get generator type.
     */
    fun getType(): GeneratorType

    /**
     * Get the amount of items that are dropped once.
     */
    fun getAmount(): Int

    /**
     * Get spawn rate delay.
     */
    fun getDelay(): Int

    /**
     * Get seconds before next item spawn.
     */
    fun getNextSpawn(): Int

    /**
     * Get the spawn limit of the generators.
     * If there is this amount of items dropped near the generator
     * it will stop spawning new items.
     */
    fun getSpawnLimit(): Int

    /**
     * Set the remaining time till the next item spawn.
     */
    fun setNextSpawn(nextSpawn: Int)

    /**
     * Should the dropped items be stacked?
     */
    fun setStack(stack: Boolean)

    /**
     * Check if the dropped items can be stacked.
     */
    fun isStack(): Boolean

    /**
     * Set generator type.
     * This may break things.
     */
    fun setType(type: GeneratorType)

    /**
     * This only must be called by the arena instance when it restarts.
     * Do never call it unless you have a custom arena.
     * Manage your data destroy.
     */
    fun destroyData()
}
