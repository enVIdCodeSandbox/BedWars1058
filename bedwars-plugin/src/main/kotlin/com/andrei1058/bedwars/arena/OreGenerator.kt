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

package com.andrei1058.bedwars.arena

import com.andrei1058.bedwars.BedWars
import com.andrei1058.bedwars.BedWars.Companion.debug
import com.andrei1058.bedwars.BedWars.Companion.getForCurrentVersion
import com.andrei1058.bedwars.BedWars.Companion.getGeneratorsCfg
import com.andrei1058.bedwars.BedWars.Companion.nms
import com.andrei1058.bedwars.BedWars.Companion.plugin
import com.andrei1058.bedwars.api.arena.GameState
import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.api.arena.generator.GeneratorType
import com.andrei1058.bedwars.api.arena.generator.IGenHolo
import com.andrei1058.bedwars.api.arena.generator.IGenerator
import com.andrei1058.bedwars.api.arena.team.ITeam
import com.andrei1058.bedwars.api.configuration.ConfigPath
import com.andrei1058.bedwars.api.events.gameplay.GeneratorUpgradeEvent
import com.andrei1058.bedwars.api.language.Language
import com.andrei1058.bedwars.api.language.Messages
import com.andrei1058.bedwars.api.region.Cuboid
import java.util.HashMap
import java.util.concurrent.ConcurrentLinkedDeque
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.*
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.util.EulerAngle
import org.bukkit.util.Vector
import com.andrei1058.bedwars.api.util.unchecked

open class OreGenerator(location: Location, arena: IArena, type: GeneratorType, bwt: ITeam?) : IGenerator {
    private var location: Location? = null
    private var delay: Int = 1
    private var upgradeStage: Int = 1
    private var lastSpawn: Int = 0
    private var spawnLimit: Int = 0
    private var amount: Int = 1
    private var arena: IArena?
    private var ore: ItemStack? = null
    private var type: GeneratorType
    private var rotate: Int = 0
    private var dropID: Int = 0
    private var bwt: ITeam?
    @JvmField var up: Boolean = true
    /**
     * Generator holograms per language <iso, holo></iso,>
     */
    private var armorStands: HashMap<String, IGenHolo>? = HashMap()
    private var item: ArmorStand? = null
    @JvmField var stack: Boolean = getGeneratorsCfg().getBoolean(ConfigPath.GENERATOR_STACK_ITEMS)

    override fun upgrade() {
        when (type) {
            GeneratorType.DIAMOND -> {
                upgradeStage++
                if (upgradeStage == 2) {
                    delay = getGeneratorsCfg().getInt(if (getGeneratorsCfg().getYml().get(arena!!.getGroup() + "." + ConfigPath.GENERATOR_DIAMOND_TIER_II_DELAY) == null) "Default." + ConfigPath.GENERATOR_DIAMOND_TIER_II_DELAY else arena!!.getGroup() + "." + ConfigPath.GENERATOR_DIAMOND_TIER_II_DELAY)
                    amount = getGeneratorsCfg().getInt(if (getGeneratorsCfg().getYml().get(arena!!.getGroup() + "." + ConfigPath.GENERATOR_DIAMOND_TIER_II_AMOUNT) == null) "Default." + ConfigPath.GENERATOR_DIAMOND_TIER_II_AMOUNT else arena!!.getGroup() + "." + ConfigPath.GENERATOR_DIAMOND_TIER_II_AMOUNT)
                    spawnLimit = getGeneratorsCfg().getInt(if (getGeneratorsCfg().getYml().get(arena!!.getGroup() + "." + ConfigPath.GENERATOR_DIAMOND_TIER_II_SPAWN_LIMIT) == null) "Default." + ConfigPath.GENERATOR_DIAMOND_TIER_II_SPAWN_LIMIT else arena!!.getGroup() + "." + ConfigPath.GENERATOR_DIAMOND_TIER_II_SPAWN_LIMIT)
                } else if (upgradeStage == 3) {
                    delay = getGeneratorsCfg().getInt(if (getGeneratorsCfg().getYml().get(arena!!.getGroup() + "." + ConfigPath.GENERATOR_DIAMOND_TIER_III_DELAY) == null) "Default." + ConfigPath.GENERATOR_DIAMOND_TIER_III_DELAY else arena!!.getGroup() + "." + ConfigPath.GENERATOR_DIAMOND_TIER_III_DELAY)
                    amount = getGeneratorsCfg().getInt(if (getGeneratorsCfg().getYml().get(arena!!.getGroup() + "." + ConfigPath.GENERATOR_DIAMOND_TIER_III_AMOUNT) == null) "Default." + ConfigPath.GENERATOR_DIAMOND_TIER_III_AMOUNT else arena!!.getGroup() + "." + ConfigPath.GENERATOR_DIAMOND_TIER_III_AMOUNT)
                    spawnLimit = getGeneratorsCfg().getInt(if (getGeneratorsCfg().getYml().get(arena!!.getGroup() + "." + ConfigPath.GENERATOR_DIAMOND_TIER_II_SPAWN_LIMIT) == null) "Default." + ConfigPath.GENERATOR_DIAMOND_TIER_III_SPAWN_LIMIT else arena!!.getGroup() + "." + ConfigPath.GENERATOR_DIAMOND_TIER_III_SPAWN_LIMIT)
                }
                ore = ItemStack(Material.DIAMOND)
                for (e in armorStands!!.values) {
                    e.setTierName(Language.getLang(e.getIso()).m(Messages.GENERATOR_HOLOGRAM_TIER).replace("{tier}", Language.getLang(e.getIso()).m(if (upgradeStage == 2) Messages.FORMATTING_GENERATOR_TIER2 else Messages.FORMATTING_GENERATOR_TIER3)))
                }
            }
            GeneratorType.EMERALD -> {
                upgradeStage++
                if (upgradeStage == 2) {
                    delay = getGeneratorsCfg().getInt(if (getGeneratorsCfg().getYml().get(arena!!.getGroup() + "." + ConfigPath.GENERATOR_EMERALD_TIER_II_DELAY) == null) "Default." + ConfigPath.GENERATOR_EMERALD_TIER_II_DELAY else arena!!.getGroup() + "." + ConfigPath.GENERATOR_EMERALD_TIER_II_DELAY)
                    amount = getGeneratorsCfg().getInt(if (getGeneratorsCfg().getYml().get(arena!!.getGroup() + "." + ConfigPath.GENERATOR_EMERALD_TIER_II_AMOUNT) == null) "Default." + ConfigPath.GENERATOR_EMERALD_TIER_II_AMOUNT else arena!!.getGroup() + "." + ConfigPath.GENERATOR_EMERALD_TIER_II_AMOUNT)
                    spawnLimit = getGeneratorsCfg().getInt(if (getGeneratorsCfg().getYml().get(arena!!.getGroup() + "." + ConfigPath.GENERATOR_EMERALD_TIER_II_SPAWN_LIMIT) == null) "Default." + ConfigPath.GENERATOR_EMERALD_TIER_II_SPAWN_LIMIT else arena!!.getGroup() + "." + ConfigPath.GENERATOR_EMERALD_TIER_II_SPAWN_LIMIT)
                } else if (upgradeStage == 3) {
                    delay = getGeneratorsCfg().getInt(if (getGeneratorsCfg().getYml().get(arena!!.getGroup() + "." + ConfigPath.GENERATOR_EMERALD_TIER_III_DELAY) == null) "Default." + ConfigPath.GENERATOR_EMERALD_TIER_III_DELAY else arena!!.getGroup() + "." + ConfigPath.GENERATOR_EMERALD_TIER_III_DELAY)
                    amount = getGeneratorsCfg().getInt(if (getGeneratorsCfg().getYml().get(arena!!.getGroup() + "." + ConfigPath.GENERATOR_EMERALD_TIER_III_AMOUNT) == null) "Default." + ConfigPath.GENERATOR_EMERALD_TIER_III_AMOUNT else arena!!.getGroup() + "." + ConfigPath.GENERATOR_EMERALD_TIER_III_AMOUNT)
                    spawnLimit = getGeneratorsCfg().getInt(if (getGeneratorsCfg().getYml().get(arena!!.getGroup() + "." + ConfigPath.GENERATOR_EMERALD_TIER_II_SPAWN_LIMIT) == null) "Default." + ConfigPath.GENERATOR_EMERALD_TIER_III_SPAWN_LIMIT else arena!!.getGroup() + "." + ConfigPath.GENERATOR_EMERALD_TIER_III_SPAWN_LIMIT)
                }
                ore = ItemStack(Material.EMERALD)
                for (e in armorStands!!.values) {
                    e.setTierName(Language.getLang(e.getIso()).m(Messages.GENERATOR_HOLOGRAM_TIER).replace("{tier}", Language.getLang(e.getIso()).m(if (upgradeStage == 2) Messages.FORMATTING_GENERATOR_TIER2 else Messages.FORMATTING_GENERATOR_TIER3)))
                }
            }
            else -> {}
        }
        Bukkit.getPluginManager().callEvent(GeneratorUpgradeEvent(this))
    }

    override fun spawn() {
        if (arena!!.getStatus() != GameState.playing) {
            return
        }
        if (lastSpawn == 0) {
            lastSpawn = delay
            if (spawnLimit != 0) {
                var oreCount: Int = 0
                for (e in location!!.getWorld().getNearbyEntities(location, 3.0, 3.0, 3.0)) {
                    if (e.getType() == EntityType.DROPPED_ITEM) {
                        val i = e as Item
                        if (i.getItemStack().getType() == ore!!.getType()) {
                            oreCount++
                        }
                        if (oreCount >= spawnLimit) {
                            return
                        }
                    }
                }
                lastSpawn = delay
            }
            if (bwt == null) {
                dropItem(location.unchecked())
                return
            }
            if (bwt!!.getMembers()!!.size == 1) {
                dropItem(location.unchecked())
                return
            }
            if (plugin.getConfig().getBoolean(ConfigPath.GENERAL_CONFIGURATION_ENABLE_GEN_SPLIT)) {
                val players = location!!.getWorld().getNearbyEntities(location, 1.0, 1.0, 1.0).stream().filter({ entity -> entity.getType() == EntityType.PLAYER }).filter({ entity -> arena!!.isPlayer(entity as Player) }).toArray()
                if (players.size <= 1) {
                    dropItem(location.unchecked())
                    return
                }
                for (o in players) {
                    val player = o as Player
                    val item = ore!!.clone()
                    item.setAmount(amount)
                    player.playSound(player.getLocation(), BedWars.nms.getSound(BedWars.getForCurrentVersion("ITEM_PICKUP", "ENTITY_ITEM_PICKUP", "ENTITY_ITEM_PICKUP")), 0.6f, 1.3f)
                    val excess = player.getInventory().addItem(item).values
                    for (value in excess) {
                        dropItem(player.getLocation(), value.getAmount())
                    }
                }
                return
            } else {
                dropItem(location.unchecked())
                return
            }
        }
        lastSpawn--
        for (e in armorStands!!.values) {
            e.setTimerName(Language.getLang(e.getIso()).m(Messages.GENERATOR_HOLOGRAM_TIMER).replace("{seconds}", lastSpawn.toString()))
        }
    }

    private fun dropItem(location: Location, amount: Int) {
        for (temp in amount downTo 0 + 1) {
            val itemStack = ItemStack(ore)
            if (!stack) {
                val itemMeta = itemStack.getItemMeta()
                itemMeta.setDisplayName("custom" + dropID++)
                itemStack.setItemMeta(itemMeta)
            }
            val item = location.getWorld().dropItem(location, itemStack)
            item.setVelocity(Vector(0, 0, 0))
        }
    }

    /**
     * Drop item stack with ID
     */
    override fun dropItem(location: Location) {
        dropItem(location, amount)
    }

    override fun setOre(ore: ItemStack) {
        BedWars.debug("Changing ore for generator at " + location.toString() + " from " + this.ore + " to " + ore)
        this.ore = ore
    }

    /**
     * Get the arena assigned to this generator.
     */
    override fun getArena(): IArena? {
        return arena
    }

    override fun getLanguageHolograms(): HashMap<String, IGenHolo>? {
        return armorStands
    }

    open inner class HoloGram(@JvmField var iso: String) : IGenHolo {
        @JvmField var tier: ArmorStand
        @JvmField var timer: ArmorStand
        @JvmField var name: ArmorStand

        override fun updateForAll() {
            for (p2 in timer.getWorld().getPlayers()) {
                updateForPlayer(p2, Language.getPlayerLanguage(p2).getIso().unchecked())
            }
        }

        override fun updateForPlayer(p: Player, lang: String) {
            if (lang.equals(iso, ignoreCase = true)) {
                return
            }
            nms.hideEntity(tier, p)
            nms.hideEntity(timer, p)
            nms.hideEntity(name, p)
        }

        override fun setTierName(name: String) {
            if (tier.isDead()) {
                return
            }
            tier.setCustomName(name)
        }

        override fun getIso(): String {
            return iso
        }

        override fun setTimerName(name: String) {
            if (timer.isDead()) {
                return
            }
            timer.setCustomName(name)
        }

        override fun destroy() {
            tier.remove()
            timer.remove()
            name.remove()
        }

        init {
            this.tier = createArmorStand(Language.getLang(iso).m(Messages.GENERATOR_HOLOGRAM_TIER).replace("{tier}", Language.getLang(iso).m(Messages.FORMATTING_GENERATOR_TIER1)), location!!.clone().add(0.0, 3.0, 0.0))
            this.timer = createArmorStand(Language.getLang(iso).m(Messages.GENERATOR_HOLOGRAM_TIMER).replace("{seconds}", lastSpawn.toString()), location!!.clone().add(0.0, 2.4, 0.0))
            this.name = createArmorStand(Language.getLang(iso).m(if (getOre()!!.getType() == Material.DIAMOND) Messages.GENERATOR_HOLOGRAM_TYPE_DIAMOND else Messages.GENERATOR_HOLOGRAM_TYPE_EMERALD), location!!.clone().add(0.0, 2.7, 0.0))
        }
    }

    override fun rotate() {
        if (up) {
            if (rotate >= 540) {
                up = false
            }
            if (rotate > 500) {
                item!!.setHeadPose(EulerAngle(0.0, Math.toRadians((run { rotate += 1; rotate }).toDouble()), 0.0))
            } else if (rotate > 470) {
                item!!.setHeadPose(EulerAngle(0.0, Math.toRadians((run { rotate += 2; rotate }).toDouble()), 0.0))
                /*item.teleport(item.getLocation().add(0, 0.005D, 0));*/
            } else if (rotate > 450) {
                item!!.setHeadPose(EulerAngle(0.0, Math.toRadians((run { rotate += 3; rotate }).toDouble()), 0.0))
                /*item.teleport(item.getLocation().add(0, 0.001D, 0));*/
            } else {
                item!!.setHeadPose(EulerAngle(0.0, Math.toRadians((run { rotate += 4; rotate }).toDouble()), 0.0))
                /*item.teleport(item.getLocation().add(0, 0.002D, 0));*/
            }
        } else {
            if (rotate <= 0) {
                up = true
            }
            if (rotate > 120) {
                item!!.setHeadPose(EulerAngle(0.0, Math.toRadians((run { rotate -= 4; rotate }).toDouble()), 0.0))
                /*item.teleport(item.getLocation().subtract(0, 0.002D, 0));*/
            } else if (rotate > 90) {
                item!!.setHeadPose(EulerAngle(0.0, Math.toRadians((run { rotate -= 3; rotate }).toDouble()), 0.0))
                /*item.teleport(item.getLocation().add(0, 0.001D, 0));*/
            } else if (rotate > 70) {
                item!!.setHeadPose(EulerAngle(0.0, Math.toRadians((run { rotate -= 2; rotate }).toDouble()), 0.0))
                /*item.teleport(item.getLocation().add(0, 0.005D, 0));*/
            } else {
                item!!.setHeadPose(EulerAngle(0.0, Math.toRadians((run { rotate -= 1; rotate }).toDouble()), 0.0))
            }
        }
    }

    override fun setDelay(delay: Int) {
        this.delay = delay
    }

    override fun setAmount(amount: Int) {
        this.amount = amount
    }

    override fun getLocation(): Location? {
        return location
    }

    override fun getOre(): ItemStack? {
        return ore
    }

    override fun disable() {
        if (getOre()!!.getType() == Material.EMERALD || getOre()!!.getType() == Material.DIAMOND) {
            rotation.remove(this)
            for (a in armorStands!!.values) {
                a.destroy()
            }
        }
        armorStands!!.clear()
    }

    override fun updateHolograms(p: Player, iso: String) {
        for (h in armorStands!!.values) {
            h.updateForPlayer(p, iso)
        }
    }

    override fun enableRotation() {
        //if (getType() == GeneratorType.EMERALD || getType() == GeneratorType.DIAMOND) {
        rotation.add(this)
        for (lan in Language.getLanguages()) {
            val h: IGenHolo? = armorStands!!.get(lan.getIso().unchecked())
            if (h == null) {
                armorStands!!.put(lan.getIso().unchecked(), HoloGram(lan.getIso().unchecked()))
            }
        }
        for (hg in armorStands!!.values) {
            hg.updateForAll()
        }
        item = createArmorStand(null, location!!.clone().add(0.0, 0.5, 0.0))
        item!!.setHelmet(ItemStack(if (type == GeneratorType.DIAMOND) Material.DIAMOND_BLOCK else Material.EMERALD_BLOCK))
    }

    override fun setSpawnLimit(value: Int) {
        this.spawnLimit = value
    }

    private fun loadDefaults() {
        when (type) {
            GeneratorType.GOLD -> {
                delay = getGeneratorsCfg().getInt(if (getGeneratorsCfg().getYml().get(arena!!.getGroup() + "." + ConfigPath.GENERATOR_GOLD_DELAY) == null) "Default." + ConfigPath.GENERATOR_GOLD_DELAY else arena!!.getGroup() + "." + ConfigPath.GENERATOR_GOLD_DELAY)
                ore = ItemStack(Material.GOLD_INGOT)
                amount = getGeneratorsCfg().getInt(if (getGeneratorsCfg().getYml().get(arena!!.getGroup() + "." + ConfigPath.GENERATOR_GOLD_AMOUNT) == null) "Default." + ConfigPath.GENERATOR_GOLD_AMOUNT else arena!!.getGroup() + "." + ConfigPath.GENERATOR_GOLD_AMOUNT)
                spawnLimit = getGeneratorsCfg().getInt(if (getGeneratorsCfg().getYml().get(arena!!.getGroup() + "." + ConfigPath.GENERATOR_GOLD_SPAWN_LIMIT) == null) "Default." + ConfigPath.GENERATOR_GOLD_SPAWN_LIMIT else arena!!.getGroup() + "." + ConfigPath.GENERATOR_GOLD_SPAWN_LIMIT)
            }
            GeneratorType.IRON -> {
                delay = getGeneratorsCfg().getInt(if (getGeneratorsCfg().getYml().get(arena!!.getGroup() + "." + ConfigPath.GENERATOR_IRON_DELAY) == null) "Default." + ConfigPath.GENERATOR_IRON_DELAY else arena!!.getGroup() + "." + ConfigPath.GENERATOR_IRON_DELAY)
                amount = getGeneratorsCfg().getInt(if (getGeneratorsCfg().getYml().get(arena!!.getGroup() + "." + ConfigPath.GENERATOR_IRON_AMOUNT) == null) "Default." + ConfigPath.GENERATOR_IRON_AMOUNT else arena!!.getGroup() + "." + ConfigPath.GENERATOR_IRON_AMOUNT)
                ore = ItemStack(Material.IRON_INGOT)
                spawnLimit = getGeneratorsCfg().getInt(if (getGeneratorsCfg().getYml().get(arena!!.getGroup() + "." + ConfigPath.GENERATOR_IRON_SPAWN_LIMIT) == null) "Default." + ConfigPath.GENERATOR_IRON_SPAWN_LIMIT else arena!!.getGroup() + "." + ConfigPath.GENERATOR_IRON_SPAWN_LIMIT)
            }
            GeneratorType.DIAMOND -> {
                delay = getGeneratorsCfg().getInt(if (getGeneratorsCfg().getYml().get(arena!!.getGroup() + "." + ConfigPath.GENERATOR_DIAMOND_TIER_I_DELAY) == null) "Default." + ConfigPath.GENERATOR_DIAMOND_TIER_I_DELAY else arena!!.getGroup() + "." + ConfigPath.GENERATOR_DIAMOND_TIER_I_DELAY)
                amount = getGeneratorsCfg().getInt(if (getGeneratorsCfg().getYml().get(arena!!.getGroup() + "." + ConfigPath.GENERATOR_DIAMOND_TIER_I_AMOUNT) == null) "Default." + ConfigPath.GENERATOR_DIAMOND_TIER_I_AMOUNT else arena!!.getGroup() + "." + ConfigPath.GENERATOR_DIAMOND_TIER_I_AMOUNT)
                spawnLimit = getGeneratorsCfg().getInt(if (getGeneratorsCfg().getYml().get(arena!!.getGroup() + "." + ConfigPath.GENERATOR_DIAMOND_TIER_I_SPAWN_LIMIT) == null) "Default." + ConfigPath.GENERATOR_DIAMOND_TIER_I_SPAWN_LIMIT else arena!!.getGroup() + "." + ConfigPath.GENERATOR_DIAMOND_TIER_I_SPAWN_LIMIT)
                ore = ItemStack(Material.DIAMOND)
            }
            GeneratorType.EMERALD -> {
                delay = getGeneratorsCfg().getInt(if (getGeneratorsCfg().getYml().get(arena!!.getGroup() + "." + ConfigPath.GENERATOR_EMERALD_TIER_I_DELAY) == null) "Default." + ConfigPath.GENERATOR_EMERALD_TIER_I_DELAY else arena!!.getGroup() + "." + ConfigPath.GENERATOR_EMERALD_TIER_I_DELAY)
                amount = getGeneratorsCfg().getInt(if (getGeneratorsCfg().getYml().get(arena!!.getGroup() + "." + ConfigPath.GENERATOR_EMERALD_TIER_I_AMOUNT) == null) "Default." + ConfigPath.GENERATOR_EMERALD_TIER_I_AMOUNT else arena!!.getGroup() + "." + ConfigPath.GENERATOR_EMERALD_TIER_I_AMOUNT)
                spawnLimit = getGeneratorsCfg().getInt(if (getGeneratorsCfg().getYml().get(arena!!.getGroup() + "." + ConfigPath.GENERATOR_EMERALD_TIER_I_SPAWN_LIMIT) == null) "Default." + ConfigPath.GENERATOR_EMERALD_TIER_I_SPAWN_LIMIT else arena!!.getGroup() + "." + ConfigPath.GENERATOR_EMERALD_TIER_I_SPAWN_LIMIT)
                ore = ItemStack(Material.EMERALD)
            }
            else -> {}
        }
        lastSpawn = delay
    }

    override fun getBwt(): ITeam? {
        return bwt
    }

    override fun getHologramHolder(): ArmorStand? {
        return item
    }

    override fun getType(): GeneratorType {
        return type
    }

    override fun getAmount(): Int {
        return amount
    }

    override fun getDelay(): Int {
        return delay
    }

    override fun getNextSpawn(): Int {
        return lastSpawn
    }

    override fun getSpawnLimit(): Int {
        return spawnLimit
    }

    override fun setNextSpawn(nextSpawn: Int) {
        this.lastSpawn = nextSpawn
    }

    override fun setStack(stack: Boolean) {
        this.stack = stack
    }

    override fun isStack(): Boolean {
        return stack
    }

    override fun setType(type: GeneratorType) {
        this.type = type
    }

    override fun destroyData() {
        rotation.remove(this)
        location = null
        arena = null
        ore = null
        bwt = null
        armorStands = null
        item = null
    }

    init {
        if (type == GeneratorType.EMERALD || type == GeneratorType.DIAMOND) {
            this.location = Location(location.getWorld(), location.getBlockX() + 0.5, location.getBlockY() + 1.3, location.getBlockZ() + 0.5)
        } else {
            this.location = location.add(0.0, 1.3, 0.0)
        }
        this.arena = arena
        this.bwt = bwt
        this.type = type
        loadDefaults()
        BedWars.debug("Initializing new generator at: " + location + " - " + type + " - " + (if (bwt == null) "NOTEAM" else bwt.getName()))
        val c = Cuboid(location, getArena()!!.getConfig()!!.getInt(ConfigPath.ARENA_GENERATOR_PROTECTION), true)
        c.setMaxY(c.getMaxY() + 5)
        c.setMinY(c.getMinY() - 2)
        arena.getRegionsList()!!.add(c)
    }

    companion object {
        private val rotation: ConcurrentLinkedDeque<OreGenerator> = ConcurrentLinkedDeque()

        @JvmStatic
        fun getRotation(): ConcurrentLinkedDeque<OreGenerator> {
            return rotation
        }

        @JvmStatic
        private fun createArmorStand(name: String?, l: Location): ArmorStand {
            val a = l.getWorld().spawnEntity(l, EntityType.ARMOR_STAND) as ArmorStand
            a.setGravity(false)
            if (name != null) {
                a.setCustomName(name)
                a.setCustomNameVisible(true)
            }
            a.setRemoveWhenFarAway(false)
            a.setVisible(false)
            a.setCanPickupItems(false)
            a.setArms(false)
            a.setBasePlate(false)
            a.setMarker(true)
            return a
        }
    }
}
