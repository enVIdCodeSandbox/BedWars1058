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

package com.andrei1058.bedwars.api.arena.team

import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.api.arena.generator.IGenerator
import com.andrei1058.bedwars.api.language.Language
import com.andrei1058.bedwars.api.upgrades.EnemyBaseEnterTrap
import java.util.LinkedList
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import org.bukkit.Location
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector

interface ITeam {
    /**
     * Runtime identifier.
     */
    fun getIdentity(): UUID

    /**
     * Get team color.
     */
    fun getColor(): TeamColor

    /**
     * Get team name.
     */
    fun getName(): String?

    /**
     * Get team display name.
     *
     * @param language get the display name in target language.
     */
    fun getDisplayName(language: Language): String

    /**
     * Check if is member.
     *
     * @param player target player.
     */
    fun isMember(player: Player?): Boolean

    /**
     * Get the team arena.
     */
    fun getArena(): IArena?

    /**
     * Get alive team members.
     */
    fun getMembers(): MutableList<Player>?

    /**
     * Restore lost default sword.
     *
     * @param p     target player.
     * @param value true to give the sword.
     */
    fun defaultSword(p: Player, value: Boolean)

    /**
     * Get bed location.
     */
    fun getBed(): Location?

    /**
     * Get list of team upgrades.
     * Upgrade identifier, tier.
     */
    fun getTeamUpgradeTiers(): ConcurrentHashMap<String, Int>

    /**
     * Get enchantments to be applied on bows.
     */
    fun getBowsEnchantments(): MutableList<TeamEnchant>?

    /**
     * Get enchantments to be applied on swords.
     */
    fun getSwordsEnchantments(): MutableList<TeamEnchant>?

    /**
     * Get enchantments to be applied on armors.
     */
    fun getArmorsEnchantments(): MutableList<TeamEnchant>?

    /**
     * Get team current size.
     */
    fun getSize(): Int

    /**
     * Add a new member to the team.
     * Be careful! This will cache players on this team until the game is finished
     * so you can retrieve who played here with {@link #wasMember(UUID)}.
     *
     * @param players players to be added.
     */
    fun addPlayers(vararg players: Player)

    /**
     * Spawn a player for the first spawn.
     *
     * @param p target player.
     */
    fun firstSpawn(p: Player?)

    /**
     * Spawn shopkeepers for target team (if enabled).
     */
    fun spawnNPCs()

    /**
     * Rejoin a team.
     *
     * @param p target player.
     */
    fun reJoin(p: Player)

    /**
     * Rejoin a team.
     *
     * @param p target player.
     * @param respawnTime the time until the player should respawn.
     */
    fun reJoin(p: Player, respawnTime: Int)

    /**
     * Gives the start inventory
     *
     * @param p     target player.
     * @param clear true to clear inventory.
     */
    fun sendDefaultInventory(p: Player, clear: Boolean)

    /**
     * Respawn a member. This is after respawn countdown.
     *
     * @param p target player.
     */
    fun respawnMember(p: Player)

    /**
     * Equip a player with default armor.
     *
     * @param p target player.
     */
    fun sendArmor(p: Player)

    /**
     * Used when someone buys a new potion effect with apply == members
     *
     * @param pef      effect.
     * @param amp      amplifier.
     * @param duration duration.
     */
    fun addTeamEffect(pef: PotionEffectType, amp: Int, duration: Int)

    /**
     * Used when someone buys a new potion effect with apply == base
     *
     * @param pef      effect.
     * @param amp      amplifier.
     * @param duration duration.
     */
    fun addBaseEffect(pef: PotionEffectType, amp: Int, duration: Int)

    /**
     * Get list of effects that you gen when you enter the base.
     */
    fun getBaseEffects(): MutableList<PotionEffect>?

    /**
     * Used when someone buys a bew enchantment with apply == bow.
     *
     * @param e enchant.
     * @param a amplifier.
     */
    fun addBowEnchantment(e: Enchantment, a: Int)

    /**
     * Used when someone buys a new enchantment with apply == sword.
     *
     * @param e enchant.
     * @param a amplifier.
     */
    fun addSwordEnchantment(e: Enchantment, a: Int)

    /**
     * Used when someone buys a new enchantment with apply == armor.
     *
     * @param e enchant.
     * @param a amplifier.
     */
    fun addArmorEnchantment(e: Enchantment, a: Int)

    /**
     * Check if target has played in this match.
     *
     * @param u player uuid.
     */
    fun wasMember(u: UUID?): Boolean

    /**
     * Check if team's bed was destroyed.
     *
     * @return true if players are no longer able to respawn.
     */
    fun isBedDestroyed(): Boolean

    /**
     * Get team spawn location.
     *
     * @return spawn point.
     */
    fun getSpawn(): Location?

    /**
     * Get shop keeper location.
     *
     * @return shop point.
     */
    fun getShop(): Location?

    /**
     * Get team upgrades location.
     *
     * @return upgrades point.
     */
    fun getTeamUpgrades(): Location?

    /**
     * This will also disable team generators if true.
     *
     * @param bedDestroyed team members will no longer be able to respawn if you set this to true.
     */
    fun setBedDestroyed(bedDestroyed: Boolean)

    /**
     * Get team iron generator.
     *
     * @return generator.
     */
    @Deprecated("")
    fun getIronGenerator(): IGenerator?

    /**
     * Get team gold generator.
     *
     * @return generator.
     */
    @Deprecated("")
    fun getGoldGenerator(): IGenerator?

    /**
     * Get team emerald generator.
     *
     * @return NULL if team does not have an emerald generator yet.
     */
    @Deprecated("")
    fun getEmeraldGenerator(): IGenerator?

    /**
     * Set a team emerald generator.
     *
     * @param emeraldGenerator generator.
     */
    @Deprecated("")
    fun setEmeraldGenerator(emeraldGenerator: IGenerator)

    /**
     * Get team generators.
     *
     * @return team generators.
     */
    fun getGenerators(): MutableList<IGenerator>

    /**
     * Get team dragons amount for the sudden death phase.
     */
    fun getDragons(): Int

    /**
     * Set a team dragons amount for the sudden death phase.
     */
    fun setDragons(amount: Int)

    @Deprecated("")
    fun getMembersCache(): MutableList<Player>?

    /**
     * Destroy team data when the arena restarts.
     * This must be called by BedWars1058 only.
     */
    fun destroyData()

    /**
     * Destroy bed hologram for player
     *
     * @param player target player.
     */
    @Deprecated("")
    fun destroyBedHolo(player: Player)

    /**
     * Get queued traps for a team.
     *
     * @return active traps list.
     */
    fun getActiveTraps(): LinkedList<EnemyBaseEnterTrap>

    /**
     * Get the location where enemy items are dropped after you kill him.
     *
     * @return x, y, z.
     */
    fun getKillDropsLocation(): Vector?

    /**
     * Change the location where to drop items after you kill an enemy.
     *
     * @param location x,y,z.
     */
    fun setKillDropsLocation(location: Vector?)

    /**
     * Check if bed is placed at given location.
     * Or partial.
     */
    fun isBed(location: Location): Boolean

    /**
     * What happens when one of team beds is destroyed at given location.
     */
    fun onBedDestroy(location: Location) {
        throw RuntimeException("Not implemented yet")
    }
}
