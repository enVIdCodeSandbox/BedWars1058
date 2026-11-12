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

package com.andrei1058.bedwars.api.server

import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.api.arena.team.ITeam
import com.andrei1058.bedwars.api.arena.team.TeamColor
import com.andrei1058.bedwars.api.entity.Despawnable
import com.andrei1058.bedwars.api.exceptions.InvalidEffectException
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.block.BlockState
import org.bukkit.command.Command
import org.bukkit.entity.Entity
import org.bukkit.entity.Fireball
import org.bukkit.entity.Player
import org.bukkit.entity.TNTPrimed
import org.bukkit.event.inventory.InventoryEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.plugin.Plugin
import org.bukkit.util.Vector
import com.andrei1058.bedwars.api.util.uninitialized

abstract class VersionSupport(private val plugin: Plugin, versionName: String) {
    private var eggBridge: Effect = uninitialized()

    protected open fun loadDefaultEffects() {
        try {
            setEggBridgeEffect("MOBSPAWNER_FLAMES")
        } catch (e: InvalidEffectException) {
            e.printStackTrace()
        }
    }

    /**
     * Register a new command as bukkit command
     */
    abstract fun registerCommand(name: String, clasa: Command)

    /**
     * Send title, subtitle. null for empty
     */
    abstract fun sendTitle(p: Player, title: String?, subtitle: String?, fadeIn: Int, stay: Int, fadeOut: Int)

    /**
     * Send action-bar message
     */
    abstract fun playAction(p: Player, text: String)

    /**
     * Check if bukkit command is registered
     */
    abstract fun isBukkitCommandRegistered(command: String): Boolean

    /**
     * Get in had item-stack
     */
    abstract fun getItemInHand(p: Player): ItemStack

    /**
     * Hide an entity
     */
    abstract fun hideEntity(e: Entity, p: Player)

    /**
     * Check if item-stack is armor
     */
    abstract fun isArmor(itemStack: ItemStack): Boolean

    /**
     * Check if item-stack is a tool
     */
    abstract fun isTool(itemStack: ItemStack): Boolean

    /**
     * Check if item-stack is sword
     */
    abstract fun isSword(itemStack: ItemStack): Boolean

    /**
     * Check if item-stack is axe
     */
    abstract fun isAxe(itemStack: ItemStack): Boolean

    /**
     * Check if item-stack is bow
     */
    abstract fun isBow(itemStack: ItemStack): Boolean

    /**
     * Check if itemstack is Projectile
     */
    abstract fun isProjectile(itemStack: ItemStack): Boolean

    /**
     * Check if itemstack is Invisibility Potion
     */
    abstract fun isInvisibilityPotion(itemStack: ItemStack): Boolean

    /**
     * Check if type is a Glass type material
     */
    open fun isGlass(type: Material): Boolean {
        return type != Material.AIR && (type == Material.GLASS || type.toString().contains("_GLASS"))
    }

    /**
     * Register custom entities
     */
    abstract fun registerEntities()

    /**
     * Spawn shop NPC
     */
    abstract fun spawnShop(loc: Location, name1: String, players: MutableList<Player>, arena: IArena)

    /**
     * Get item-stack damage amount
     */
    abstract fun getDamage(i: ItemStack): Double

    /**
     * Spawn silverfish for a team
     */
    abstract fun spawnSilverfish(loc: Location, team: ITeam, speed: Double, health: Double, despawn: Int, damage: Double)

    /**
     * Spawn a iron-golem for a team
     */
    abstract fun spawnIronGolem(loc: Location, team: ITeam, speed: Double, health: Double, despawn: Int)

    /**
     * Is despawnable entity
     */
    open fun isDespawnable(e: Entity): Boolean {
        return despawnables.get(e.getUniqueId()) != null
    }

    /**
     * Change item amount
     */
    abstract fun minusAmount(p: Player, i: ItemStack, amount: Int)

    /**
     * Set tnt source
     */
    abstract fun setSource(tnt: TNTPrimed, owner: Player)

    /**
     * Void damage with cause
     */
    abstract fun voidKill(p: Player)

    /**
     * Hide player armor to a player
     */
    abstract fun hideArmor(victim: Player, receiver: Player)

    /**
     * Show a player armor
     */
    abstract fun showArmor(victim: Player, receiver: Player)

    /**
     * Spawn ender dragon
     */
    abstract fun spawnDragon(l: Location?, team: ITeam)

    /**
     * Color a bed 1.12+
     */
    abstract fun colorBed(team: ITeam)

    /**
     * Modify block blast resistance.
     */
    abstract fun registerTntWhitelist(endStoneBlast: Float, glassBlast: Float)

    /**
     * Egg bridge particles
     */
    open fun eggBridge(): Effect {
        return eggBridge
    }

    @Throws(InvalidEffectException::class)
    open fun setEggBridgeEffect(eggBridge: String) {
        try {
            this.eggBridge = Effect.valueOf(eggBridge)
        } catch (e: Exception) {
            throw InvalidEffectException(eggBridge)
        }
    }

    /**
     * Set block data
     * For 1.13 support
     */
    abstract fun setBlockTeamColor(block: Block, teamColor: TeamColor)

    /**
     * Disable collisions in 1.9+
     */
    abstract fun setCollide(p: Player, a: IArena?, value: Boolean)

    /**
     * Add custom data to an ItemStack
     */
    abstract fun addCustomData(i: ItemStack, data: String): ItemStack

    abstract fun setTag(itemStack: ItemStack, key: String, value: String): ItemStack

    /**
     * Get a custom item tag.
     *
     * @return null if not present.
     */
    abstract fun getTag(itemStack: ItemStack, key: String): String?

    /**
     * Check if an item has a BedWars1058 NBTTag
     */
    abstract fun isCustomBedWarsItem(i: ItemStack): Boolean

    /**
     * Get the NBTTag from a BedWars1058 item
     */
    abstract fun getCustomData(i: ItemStack): String

    /**
     * Color an item if possible with the team's color
     */
    abstract fun colourItem(itemStack: ItemStack?, bedWarsTeam: ITeam): ItemStack?

    abstract fun createItemStack(material: String, amount: Int, data: Short): ItemStack

    /**
     * Check if is a player head
     */
    open fun isPlayerHead(material: String, data: Int): Boolean {
        return material.equals("PLAYER_HEAD", ignoreCase = true)
    }

    /**
     * Get fireball material
     */
    abstract fun materialFireball(): Material

    /**
     * Player head material
     */
    abstract fun materialPlayerHead(): Material

    /**
     * Get snowball material
     */
    abstract fun materialSnowball(): Material

    /**
     * Get gold  helmet material
     */
    abstract fun materialGoldenHelmet(): Material

    /**
     * Get gold chest plate
     */
    abstract fun materialGoldenChestPlate(): Material

    /**
     * Get gold leggings
     */
    abstract fun materialGoldenLeggings(): Material

    /**
     * Get netherite  helmet material
     */
    abstract fun materialNetheriteHelmet(): Material

    /**
     * Get netherite chest plate
     */
    abstract fun materialNetheriteChestPlate(): Material

    /**
     * Get netherite leggings
     */
    abstract fun materialNetheriteLeggings(): Material

    /**
     * Get elytra - supports: 1.12.2+
     */
    abstract fun materialElytra(): Material?

    /**
     * Cake material
     */
    abstract fun materialCake(): Material

    /**
     * Crafting table material
     */
    abstract fun materialCraftingTable(): Material

    /**
     * Enchanting table material
     */
    abstract fun materialEnchantingTable(): Material

    /**
     * Check if bed
     */
    open fun isBed(material: Material): Boolean {
        return material.toString().contains("_BED")
    }

    /**
     * Item Data compare
     * This will always return true on versions major or equal 1.13
     */
    open fun itemStackDataCompare(i: ItemStack, data: Short): Boolean {
        return true
    }

    /**
     * Set block data
     * For versions before 1.13
     */
    open fun setJoinSignBackgroundBlockData(b: BlockState, data: Byte) {
    }

    /**
     * Change the block behind the join sign.
     */
    abstract fun setJoinSignBackground(b: BlockState, material: Material)

    /**
     * Wool material
     */
    abstract fun woolMaterial(): Material

    /**
     * Get an ItemStack identifier
     * will return null text if it does not have an identifier
     */
    abstract fun getShopUpgradeIdentifier(itemStack: ItemStack): String

    /**
     * Set an upgrade identifier
     */
    abstract fun setShopUpgradeIdentifier(itemStack: ItemStack, identifier: String): ItemStack

    /**
     * Get player head with skin.
     *
     * @param copyTagFrom will copy nbt tag from this item.
     */
    abstract fun getPlayerHead(player: Player, copyTagFrom: ItemStack?): ItemStack

    /**
     * This will send the player spawn packet after a player re-spawn.
     * <p>
     * Show the target player to players and spectators in the arena.
     */
    abstract fun sendPlayerSpawnPackets(player: Player?, arena: IArena?)

    /**
     * Get inventory name.
     */
    abstract fun getInventoryName(e: InventoryEvent): String

    /**
     * Get the top inventory of the inventory currently opened by the given player.
     * <p>
     * {@code InventoryView} became an interface in 1.21, so code compiled against older
     * server APIs must not call its methods directly. Newer version support modules
     * override this and are compiled against the newer API.
     *
     * @return the top inventory or null when none is available.
     */
    open fun getTopInventory(player: Player?): org.bukkit.inventory.Inventory? {
        if (player == null) {
            return null
        }
        val view: org.bukkit.inventory.InventoryView? = player.getOpenInventory()
        return if (view == null) null else view.getTopInventory()
    }

    /**
     * Get the top inventory of the view related to an inventory event.
     *
     * @see #getTopInventory(Player)
     */
    open fun getTopInventory(e: InventoryEvent?): org.bukkit.inventory.Inventory? {
        return e!!.getView().getTopInventory()
    }

    /**
     * Get the player (viewer) of the view related to an inventory event.
     *
     * @see #getTopInventory(Player)
     */
    open fun getViewPlayer(e: InventoryEvent): org.bukkit.entity.HumanEntity {
        return e.getView().getPlayer()
    }

    /**
     * Get a sound by its enum-like name, ex: {@code ENTITY_ITEM_PICKUP}.
     * <p>
     * {@code Sound} stopped being an enum in 1.21.3, so calling {@code Sound.valueOf}
     * from code compiled against older APIs is not safe.
     *
     * @throws IllegalArgumentException if there is no such sound.
     */
    open fun getSound(name: String): Sound {
        return Sound.valueOf(name)
    }

    /**
     * Make item unbreakable.
     */
    abstract fun setUnbreakable(itemMeta: ItemMeta)

    /**
     * Get list of entities that are going to despawn based on a timer.
     */
    open fun getDespawnablesList(): ConcurrentHashMap<UUID, Despawnable> {
        return despawnables
    }

    abstract fun getVersion(): Int

    open fun getPlugin(): Plugin {
        return plugin
    }

    abstract fun registerVersionListeners()

    /**
     * Get main level name.
     */
    abstract fun getMainLevel(): String

    open fun getCompressedAngle(value: Float): Byte {
        return ((value * 256.0f) / 360.0f).toInt().toByte()
    }

    open fun spigotShowPlayer(victim: Player, receiver: Player) {
        receiver.showPlayer(victim)
    }

    open fun spigotHidePlayer(victim: Player, receiver: Player) {
        receiver.hidePlayer(victim)
    }

    /**
     * Make fireball go straight.
     *
     * @param fireball fireball instance;
     * @param vector   fireball direction to normalize.
     * @return modified fireball.
     */
    abstract fun setFireballDirection(fireball: Fireball, vector: Vector): Fireball

    abstract fun playRedStoneDot(player: Player)

    abstract fun clearArrowsFromPlayerBody(player: Player)

    abstract fun placeTowerBlocks(b: Block, a: IArena, color: TeamColor, x: Int, y: Int, z: Int)

    abstract fun placeLadder(b: Block, x: Int, y: Int, z: Int, a: IArena, ladderdata: Int)

    abstract fun playVillagerEffect(player: Player, location: Location)

    init {
        name2 = versionName
    }

    companion object {
        private var name2: String = uninitialized()
        @JvmField var PLUGIN_TAG_GENERIC_KEY: String = "BedWars1058"
        @JvmField var PLUGIN_TAG_TIER_KEY: String = "tierIdentifier"
        private val despawnables: ConcurrentHashMap<UUID, Despawnable> = ConcurrentHashMap()

        @JvmStatic
        fun getName(): String {
            return name2
        }
    }
}
