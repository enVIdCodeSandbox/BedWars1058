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

package com.andrei1058.bedwars.arena.team

import com.andrei1058.bedwars.BedWars
import com.andrei1058.bedwars.BedWars.Companion.config
import com.andrei1058.bedwars.BedWars.Companion.generators
import com.andrei1058.bedwars.BedWars.Companion.nms
import com.andrei1058.bedwars.BedWars.Companion.plugin
import com.andrei1058.bedwars.BedWars.Companion.shop
import com.andrei1058.bedwars.api.arena.generator.GeneratorType
import com.andrei1058.bedwars.api.arena.generator.IGenerator
import com.andrei1058.bedwars.api.arena.team.ITeam
import com.andrei1058.bedwars.api.arena.team.TeamColor
import com.andrei1058.bedwars.api.arena.team.TeamEnchant
import com.andrei1058.bedwars.api.configuration.ConfigPath
import com.andrei1058.bedwars.api.events.player.PlayerFirstSpawnEvent
import com.andrei1058.bedwars.api.events.player.PlayerReSpawnEvent
import com.andrei1058.bedwars.api.language.Language
import com.andrei1058.bedwars.api.language.Language.Companion.getMsg
import com.andrei1058.bedwars.api.language.Messages
import com.andrei1058.bedwars.api.region.Cuboid
import com.andrei1058.bedwars.api.upgrades.EnemyBaseEnterTrap
import com.andrei1058.bedwars.arena.Arena
import com.andrei1058.bedwars.arena.OreGenerator
import com.andrei1058.bedwars.configuration.Sounds
import com.andrei1058.bedwars.shop.ShopCache
import com.andrei1058.bedwars.support.paper.TeleportManager
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.stream.Collectors
import org.bukkit.*
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.LeatherArmorMeta
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector
import com.andrei1058.bedwars.api.util.unchecked
import com.andrei1058.bedwars.api.util.uninitialized

open class BedWarsTeam : ITeam {
    private var members: MutableList<Player>? = ArrayList()
    private var color: TeamColor = uninitialized()
    private var spawn: Location? = null
    private var bed: Location? = null
    private var shop: Location? = null
    private var teamUpgrades: Location? = null
    //private IGenerator ironGenerator = null, goldGenerator = null, emeraldGenerator = null;
    private var name: String? = null
    private var arena: Arena? = null
    private var bedDestroyed: Boolean = false
    private var killDropsLoc: Vector? = null
    // team generators
    private var generators: MutableList<IGenerator> = ArrayList()
    // team upgrade name, tier
    private var teamUpgradeList: ConcurrentHashMap<String, Int> = ConcurrentHashMap()
    // Potion effects for teammates from the upgrades
    private var teamEffects: MutableList<PotionEffect>? = ArrayList()
    // Potion effects for teammates on base only
    private var base: MutableList<PotionEffect>? = ArrayList()
    // Enchantments for bows
    private var bowsEnchantments: MutableList<TeamEnchant>? = ArrayList()
    // Enchantments for swords
    private var swordsEnchantemnts: MutableList<TeamEnchant>? = ArrayList()
    // Enchantments for armors
    private var armorsEnchantemnts: MutableList<TeamEnchant>? = ArrayList()
    // Used for show/ hide bed hologram
    private var beds: HashMap<UUID, BedHolo> = HashMap()
    // Queued traps
    private var enemyBaseEnterTraps: LinkedList<EnemyBaseEnterTrap> = LinkedList()
    // Amount of dragons for Sudden Death phase
    private var dragons: Int = 1
    // Player cache, used for losers stats and rejoin
    private var membersCache: MutableList<Player>? = ArrayList()
    private var identity: UUID = uninitialized()

    constructor(name: String, color: TeamColor, spawn: Location, bed: Location, shop: Location, teamUpgrades: Location, arena: Arena?) {
        if (arena == null) {
            return
        }
        this.name = name
        this.color = color
        this.spawn = spawn
        this.bed = bed
        this.arena = arena
        this.shop = shop
        this.teamUpgrades = teamUpgrades
        Language.saveIfNotExists(ConfigPath.TEAM_NAME_PATH.replace("{arena}", getArena()!!.getArenaName()).replace("{team}", getName().unchecked()), name)
        arena.getRegionsList()!!.add(Cuboid(spawn, arena.getConfig()!!.getInt(ConfigPath.ARENA_SPAWN_PROTECTION), true))
        val drops: Location? = getArena()!!.getConfig()!!.getArenaLoc("Team." + getName() + "." + ConfigPath.ARENA_TEAM_KILL_DROPS_LOC)
        if (drops != null) {
            setKillDropsLocation(drops)
        }
        this.identity = UUID.randomUUID()
    }

    override fun getSize(): Int {
        return members!!.size
    }

    /**
     * Add a new member to the team
     */
    override fun addPlayers(vararg players: Player) {
        if (players == null) {
            return
        }
        for (p in players) {
            if (p == null) {
                continue
            }
            members!!.removeIf({ player -> player.getUniqueId() == p.getUniqueId() })
            members!!.add(p)
            membersCache!!.removeIf({ player -> player.getUniqueId() == p.getUniqueId() })
            membersCache!!.add(p)
            BedHolo(p, getArena().unchecked())
        }
    }

    /**
     * first spawn
     */
    override fun firstSpawn(p: Player?) {
        if (p == null) {
            return
        }
        TeleportManager.teleportC(p, spawn.unchecked(), PlayerTeleportEvent.TeleportCause.PLUGIN)
        p.setGameMode(GameMode.SURVIVAL)
        p.setCanPickupItems(true)
        nms.setCollide(p, getArena(), true)
        sendDefaultInventory(p, true)
        Bukkit.getPluginManager().callEvent(PlayerFirstSpawnEvent(p, getArena().unchecked(), this))
    }

    /**
     * Spawn shopkeepers for target team (if enabled).
     */
    override fun spawnNPCs() {
        if (getMembers()!!.isEmpty() && getArena()!!.getConfig()!!.getBoolean(ConfigPath.ARENA_DISABLE_NPCS_FOR_EMPTY_TEAMS)) {
            return
        }
        Bukkit.getScheduler().runTaskLater(plugin, Runnable {
            nms.colorBed(this)
            nms.spawnShop(getArena()!!.getConfig()!!.getArenaLoc("Team." + getName() + ".Upgrade").unchecked(), (if (getArena()!!.getMaxInTeam() > 1) Messages.NPC_NAME_TEAM_UPGRADES else Messages.NPC_NAME_SOLO_UPGRADES), getArena()!!.getPlayers().unchecked(), getArena().unchecked())
            nms.spawnShop(getArena()!!.getConfig()!!.getArenaLoc("Team." + getName() + ".Shop").unchecked(), (if (getArena()!!.getMaxInTeam() > 1) Messages.NPC_NAME_TEAM_SHOP else Messages.NPC_NAME_SOLO_SHOP), getArena()!!.getPlayers().unchecked(), getArena().unchecked())
        }, 20L)
        val c1 = Cuboid(getArena()!!.getConfig()!!.getArenaLoc("Team." + getName() + ".Upgrade").unchecked(), getArena()!!.getConfig()!!.getInt(ConfigPath.ARENA_UPGRADES_PROTECTION), true)
        c1.setMinY(c1.getMinY() - 1)
        c1.setMaxY(c1.getMaxY() + 4)
        getArena()!!.getRegionsList()!!.add(c1)
        val c2 = Cuboid(getArena()!!.getConfig()!!.getArenaLoc("Team." + getName() + ".Shop").unchecked(), getArena()!!.getConfig()!!.getInt(ConfigPath.ARENA_SHOP_PROTECTION), true)
        c2.setMinY(c2.getMinY() - 1)
        c2.setMaxY(c2.getMaxY() + 4)
        getArena()!!.getRegionsList()!!.add(c2)
    }

    /**
     * Rejoin a team
     */
    override fun reJoin(p: Player) {
        reJoin(p, BedWars.config.getInt(ConfigPath.GENERAL_CONFIGURATION_RE_SPAWN_COUNTDOWN))
    }

    override fun reJoin(p: Player, respawnTime: Int) {
        addPlayers(p)
        arena!!.startReSpawnSession(p, respawnTime)
    }

    /**
     * Gives the start inventory
     */
    override fun sendDefaultInventory(p: Player, clean: Boolean) {
        if (clean) {
            p.getInventory().clear()
        }
        val path: String = if (config.getYml().get(ConfigPath.GENERAL_CONFIGURATION_DEFAULT_ITEMS + "." + arena!!.getGroup()) == null) ConfigPath.GENERAL_CONFIGURATION_DEFAULT_ITEMS + ".Default" else ConfigPath.GENERAL_CONFIGURATION_DEFAULT_ITEMS + "." + arena!!.getGroup()
        for (s in config.getYml().getStringList(path)) {
            val parm = s.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (parm.size != 0) {
                try {
                    var i: ItemStack
                    if (parm.size > 1) {
                        try {
                            Integer.parseInt(parm[1])
                        } catch (ex: Exception) {
                            plugin.getLogger().severe(parm[1] + " is not an integer at: " + s + " (config)")
                            continue
                        }
                        i = ItemStack(Material.valueOf(parm[0]), Integer.parseInt(parm[1]))
                    } else {
                        i = ItemStack(Material.valueOf(parm[0]))
                    }
                    if (parm.size > 2) {
                        try {
                            Integer.parseInt(parm[2])
                        } catch (ex: Exception) {
                            plugin.getLogger().severe(parm[2] + " is not an integer at: " + s + " (config)")
                            continue
                        }
                        i.setAmount(Integer.parseInt(parm[2]))
                    }
                    val im = i.getItemMeta()
                    if (parm.size > 3) {
                        im.setDisplayName(ChatColor.translateAlternateColorCodes('&', parm[3]))
                    }
                    nms.setUnbreakable(im)
                    i.setItemMeta(im)
                    i = nms.addCustomData(i, "DEFAULT_ITEM")
                    if (BedWars.nms.isSword(i)) {
                        var hasSword: Boolean = false
                        for (item in p.getInventory().getContents()) {
                            if (item == null) {
                                continue
                            }
                            if (item.getType() == Material.AIR) {
                                continue
                            }
                            if (BedWars.nms.isSword(item)) {
                                hasSword = true
                                break
                            }
                        }
                        if (!hasSword) {
                            p.getInventory().addItem(i)
                        }
                    } else if (BedWars.nms.isBow(i)) {
                        var hasBow: Boolean = false
                        for (item in p.getInventory().getContents()) {
                            if (item == null) {
                                continue
                            }
                            if (item.getType() == Material.AIR) {
                                continue
                            }
                            if (BedWars.nms.isBow(item)) {
                                hasBow = true
                                break
                            }
                        }
                        if (!hasBow) {
                            p.getInventory().addItem(i)
                        }
                    } else {
                        p.getInventory().addItem(i)
                    }
                } catch (ignored: Exception) {
                }
            }
        }
        sendArmor(p)
    }

    override fun defaultSword(p: Player, sword: Boolean) {
        if (!sword) {
            return
        }
        val path: String = if (config.getYml().get(ConfigPath.GENERAL_CONFIGURATION_DEFAULT_ITEMS + "." + arena!!.getGroup()) == null) ConfigPath.GENERAL_CONFIGURATION_DEFAULT_ITEMS + ".Default" else ConfigPath.GENERAL_CONFIGURATION_DEFAULT_ITEMS + "." + arena!!.getGroup()
        for (s in config.getYml().getStringList(path)) {
            val parm = s.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (parm.size != 0) {
                try {
                    var i: ItemStack
                    if (parm.size > 1) {
                        try {
                            Integer.parseInt(parm[1])
                        } catch (ex: Exception) {
                            plugin.getLogger().severe(parm[1] + " is not an integer at: " + s + " (config)")
                            continue
                        }
                        i = ItemStack(Material.valueOf(parm[0]), Integer.parseInt(parm[1]))
                    } else {
                        i = ItemStack(Material.valueOf(parm[0]))
                    }
                    if (parm.size > 2) {
                        try {
                            Integer.parseInt(parm[2])
                        } catch (ex: Exception) {
                            plugin.getLogger().severe(parm[2] + " is not an integer at: " + s + " (config)")
                            continue
                        }
                        i.setAmount(Integer.parseInt(parm[2]))
                    }
                    val im = i.getItemMeta()
                    if (parm.size > 3) {
                        im.setDisplayName(ChatColor.translateAlternateColorCodes('&', parm[3]))
                    }
                    nms.setUnbreakable(im)
                    i.setItemMeta(im)
                    i = nms.addCustomData(i, "DEFAULT_ITEM")
                    if (BedWars.nms.isSword(i)) {
                        p.getInventory().addItem(i)
                        break
                    }
                } catch (ignored: Exception) {
                }
            }
        }
    }

    /**
     * Spawn iron and gold generators
     */
    open fun spawnGenerators() {
        for (type in arrayOf<String>("Iron", "Gold")) {
            val gt = GeneratorType.valueOf(type.uppercase())
            var locs: MutableList<Location> = ArrayList()
            val o = getArena()!!.getConfig()!!.getYml().get("Team." + getName() + "." + type)
            if (o is String) {
                locs.add(getArena()!!.getConfig()!!.getArenaLoc("Team." + getName() + "." + type).unchecked())
            } else {
                locs = getArena()!!.getConfig()!!.getArenaLocations("Team." + getName() + "." + type)
            }
            for (loc in locs) {
                val gen: IGenerator = OreGenerator(loc, getArena().unchecked(), gt, this)
                //getArena().getOreGenerators().add(gen);
                generators.add(gen)
            }
        }
    }

    /**
     * Respawn a member
     */
    override fun respawnMember(p: Player) {
        if (reSpawnInvulnerability.containsKey(p.getUniqueId())) {
            reSpawnInvulnerability.replace(p.getUniqueId(), System.currentTimeMillis() + config.getInt(ConfigPath.GENERAL_CONFIGURATION_RE_SPAWN_INVULNERABILITY))
        } else {
            reSpawnInvulnerability.put(p.getUniqueId(), System.currentTimeMillis() + config.getInt(ConfigPath.GENERAL_CONFIGURATION_RE_SPAWN_INVULNERABILITY))
        }
        TeleportManager.teleportC(p, getSpawn().unchecked(), PlayerTeleportEvent.TeleportCause.PLUGIN)
        p.setVelocity(Vector(0, 0, 0))
        p.removePotionEffect(PotionEffectType.INVISIBILITY)
        nms.setCollide(p, arena, true)
        p.setAllowFlight(false)
        p.setFlying(false)
        p.setHealth(20.0)
        Bukkit.getScheduler().runTaskLater(plugin, Runnable {
            //Fixes https://github.com/andrei1058/BedWars1058/issues/669
            getArena()!!.getRespawnSessions()!!.remove(p)
            for (inGame in arena!!.getPlayers()!!) {
                if (inGame == p) {
                    continue
                }
                BedWars.nms.spigotShowPlayer(p, inGame)
                BedWars.nms.spigotShowPlayer(inGame, p)
            }
            for (spectator in arena!!.getSpectators()!!) {
                BedWars.nms.spigotShowPlayer(p, spectator)
            }
        }, 8L)
        nms.sendTitle(p, getMsg(p, Messages.PLAYER_DIE_RESPAWNED_TITLE), "", 0, 20, 10)
        sendDefaultInventory(p, false)
        val sc: ShopCache? = ShopCache.getShopCache(p.getUniqueId())
        if (sc != null) {
            sc.managePermanentsAndDowngradables(getArena().unchecked())
        }
        p.setHealth(20.0)
        if (!getBaseEffects()!!.isEmpty()) {
            for (ef in getBaseEffects()!!) {
                p.addPotionEffect(ef, true)
            }
        }
        if (!getTeamEffects()!!.isEmpty()) {
            for (ef in getTeamEffects()!!) {
                p.addPotionEffect(ef, true)
            }
        }
        if (!getBowsEnchantments()!!.isEmpty()) {
            for (i in p.getInventory().getContents()) {
                if (i == null) {
                    continue
                }
                if (i.getType() == Material.BOW) {
                    val im = i.getItemMeta()
                    for (e in getBowsEnchantments()!!) {
                        im.addEnchant(e.getEnchantment(), e.getAmplifier(), true)
                    }
                    i.setItemMeta(im)
                }
                p.updateInventory()
            }
        }
        if (!getSwordsEnchantments()!!.isEmpty()) {
            for (i in p.getInventory().getContents()) {
                if (i == null) {
                    continue
                }
                if (nms.isSword(i)) {
                    val im = i.getItemMeta()
                    for (e in getSwordsEnchantments()!!) {
                        im.addEnchant(e.getEnchantment(), e.getAmplifier(), true)
                    }
                    i.setItemMeta(im)
                }
                p.updateInventory()
            }
        }
        if (!getArmorsEnchantments()!!.isEmpty()) {
            for (i in p.getInventory().getArmorContents()) {
                if (i == null) {
                    continue
                }
                if (nms.isArmor(i)) {
                    val im = i.getItemMeta()
                    for (e in getArmorsEnchantments()!!) {
                        im.addEnchant(e.getEnchantment(), e.getAmplifier(), true)
                    }
                    i.setItemMeta(im)
                }
                p.updateInventory()
            }
        }
        Bukkit.getPluginManager().callEvent(PlayerReSpawnEvent(p, getArena().unchecked(), this))
        nms.sendPlayerSpawnPackets(p, getArena())
        Bukkit.getScheduler().runTaskLater(plugin, Runnable {
            if (getArena() != null) {
                nms.sendPlayerSpawnPackets(p, getArena())
                // #274
                for (on in getArena()!!.getShowTime()!!.keys) {
                    BedWars.nms.hideArmor(on, p)
                }
            }
            //
        }, 10L)
        Sounds.playSound("player-re-spawn", p)
    }

    /**
     * Create a leather armor with team's color
     */
    private fun createArmor(material: Material): ItemStack {
        val i = ItemStack(material)
        val lam = i.getItemMeta() as LeatherArmorMeta
        lam.setColor(color.bukkitColor())
        nms.setUnbreakable(lam)
        i.setItemMeta(lam)
        return i
    }

    /**
     * Equip a player with default armor
     */
    override fun sendArmor(p: Player) {
        // Paper 26.x returns an AIR stack (never null) for an empty armor slot
        if (isEmptySlot(p.getInventory().getHelmet())) {
            p.getInventory().setHelmet(createArmor(Material.LEATHER_HELMET))
        }
        if (isEmptySlot(p.getInventory().getChestplate())) {
            p.getInventory().setChestplate(createArmor(Material.LEATHER_CHESTPLATE))
        }
        if (isEmptySlot(p.getInventory().getLeggings())) {
            p.getInventory().setLeggings(createArmor(Material.LEATHER_LEGGINGS))
        }
        if (isEmptySlot(p.getInventory().getBoots())) {
            p.getInventory().setBoots(createArmor(Material.LEATHER_BOOTS))
        }
    }

    override fun getIdentity(): UUID {
        return identity
    }

    /**
     * Creates a hologram on the team bed's per player
     */
    open inner class BedHolo(p: Player, arena: Arena) {
        @JvmField var a: ArmorStand = uninitialized()
        @JvmField var p: UUID
        @JvmField var arena: Arena
        @JvmField var hidden: Boolean = false
        @JvmField var bedDestroyed: Boolean = false

        open fun spawn() {
            if (!arena.getConfig()!!.getBoolean(ConfigPath.ARENA_USE_BED_HOLO)) {
                return
            }
            a = bed!!.getWorld().spawnEntity(bed!!.getBlock().getLocation().add(+0.5, 1.0, +0.5), EntityType.ARMOR_STAND) as ArmorStand
            a.setGravity(false)
            if (name != null) {
                if (isBedDestroyed()) {
                    a.setCustomName(getMsg(Bukkit.getPlayer(p), Messages.BED_HOLOGRAM_DESTROYED))
                    bedDestroyed = true
                } else {
                    a.setCustomName(getMsg(Bukkit.getPlayer(p), Messages.BED_HOLOGRAM_DEFEND))
                }
                a.setCustomNameVisible(true)
            }
            a.setRemoveWhenFarAway(false)
            a.setCanPickupItems(false)
            a.setArms(false)
            a.setBasePlate(false)
            a.setMarker(true)
            a.setVisible(false)
            for (p2 in arena.getWorld()!!.getPlayers()) {
                if (p !== p2.getUniqueId()) {
                    nms.hideEntity(a, p2)
                }
            }
        }

        open fun hide() {
            if (!arena.getConfig()!!.getBoolean(ConfigPath.ARENA_USE_BED_HOLO)) {
                return
            }
            if (bedDestroyed) {
                return
            }
            hidden = true
            a.remove()
        }

        open fun destroy() {
            if (!arena.getConfig()!!.getBoolean(ConfigPath.ARENA_USE_BED_HOLO)) {
                return
            }
            a.remove()
            beds.remove(p)
        }

        open fun show() {
            if (!arena.getConfig()!!.getBoolean(ConfigPath.ARENA_USE_BED_HOLO)) {
                return
            }
            hidden = false
            spawn()
        }

        open fun getArena(): Arena {
            return arena
        }

        open fun isHidden(): Boolean {
            return hidden
        }

        init {
            this.p = p.getUniqueId()
            this.arena = arena
            spawn()
            beds.put(p.getUniqueId(), this)
        }
    }

    /**
     * Used when someone buys a new potion effect with apply == members
     */
    override fun addTeamEffect(pef: PotionEffectType, amp: Int, duration: Int) {
        getTeamEffects()!!.add(PotionEffect(pef, duration, amp))
        for (p in getMembers()!!) {
            p.addPotionEffect(PotionEffect(pef, duration, amp), true)
        }
    }

    /**
     * Used when someone buys a new potion effect with apply == base
     */
    override fun addBaseEffect(pef: PotionEffectType, amp: Int, duration: Int) {
        getBaseEffects()!!.add(PotionEffect(pef, duration, amp))
        for (p in ArrayList(getMembers())) {
            if (p.getLocation().distance(getBed()) <= getArena()!!.getIslandRadius()) {
                for (e in getBaseEffects()!!) {
                    p.addPotionEffect(e, true)
                }
            }
        }
    }

    /**
     * Used when someone buys a bew enchantment with apply == bow
     */
    override fun addBowEnchantment(e: Enchantment, a: Int) {
        getBowsEnchantments()!!.add(Enchant(e, a))
        for (p in getMembers()!!) {
            for (i in p.getInventory().getContents()) {
                if (i == null) {
                    continue
                }
                if (i.getType() == Material.BOW) {
                    val im = i.getItemMeta()
                    im.addEnchant(e, a, true)
                    i.setItemMeta(im)
                }
            }
            p.updateInventory()
        }
    }

    /**
     * Used when someone buys a new enchantment with apply == sword
     */
    override fun addSwordEnchantment(e: Enchantment, a: Int) {
        getSwordsEnchantments()!!.add(Enchant(e, a))
        for (p in getMembers()!!) {
            for (i in p.getInventory().getContents()) {
                if (i == null) {
                    continue
                }
                if (nms.isSword(i) || nms.isAxe(i)) {
                    val im = i.getItemMeta()
                    im.addEnchant(e, a, true)
                    i.setItemMeta(im)
                }
            }
            p.updateInventory()
        }
    }

    /**
     * Used when someone buys a new enchantment with apply == armor
     */
    override fun addArmorEnchantment(e: Enchantment, a: Int) {
        getArmorsEnchantments()!!.add(Enchant(e, a))
        for (p in getMembers()!!) {
            for (i in p.getInventory().getArmorContents()) {
                if (i == null) {
                    continue
                }
                if (nms.isArmor(i)) {
                    val im = i.getItemMeta()
                    im.addEnchant(e, a, true)
                    i.setItemMeta(im)
                }
            }
            p.updateInventory()
        }
        // #274
        Bukkit.getScheduler().runTaskLater(BedWars.plugin, Runnable {
            for (m in getMembers()!!) {
                if (m.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                    for (p in getArena()!!.getPlayers()!!) {
                        BedWars.nms.hideArmor(m, p)
                    }
                    for (p in getArena()!!.getSpectators()!!) {
                        BedWars.nms.hideArmor(m, p)
                    }
                }
            }
        }, 20L)
    }

    /**
     * Enchantments for bows, swords and armors from the team upgrades
     */
    open class Enchant(@JvmField var enchantment: Enchantment, @JvmField var amplifier: Int) : TeamEnchant {
        override fun getEnchantment(): Enchantment {
            return enchantment
        }

        override fun getAmplifier(): Int {
            return amplifier
        }
    }

    override fun isMember(u: Player?): Boolean {
        if (u == null) {
            return false
        }
        return members!!.contains(u)
    }

    /**
     * Getter, setter etc.
     */
    override fun wasMember(u: UUID?): Boolean {
        if (u == null) {
            return false
        }
        for (p in membersCache!!) {
            if (p.getUniqueId() == u) {
                return true
            }
        }
        return false
    }

    override fun isBedDestroyed(): Boolean {
        return bedDestroyed
    }

    override fun getSpawn(): Location? {
        return spawn
    }

    override fun getShop(): Location? {
        return shop
    }

    override fun getTeamUpgrades(): Location? {
        return teamUpgrades
    }

    override fun getName(): String? {
        return name
    }

    override fun getDisplayName(language: Language): String {
        val m: String? = language.m(ConfigPath.TEAM_NAME_PATH.replace("{arena}", getArena()!!.getArenaName()).replace("{team}", getName().unchecked()))
        return (m ?: getName()).unchecked()
    }

    override fun getColor(): TeamColor {
        return color
    }

    override fun getMembers(): MutableList<Player>? {
        return members
    }

    override fun getBed(): Location? {
        return bed
    }

    override fun getTeamUpgradeTiers(): ConcurrentHashMap<String, Int> {
        return teamUpgradeList
    }

    open fun getBedHolo(p: Player): BedHolo {
        return beds.get(p.getUniqueId()).unchecked()
    }

    /**
     * Destroy the bed for a team.
     */
    override fun setBedDestroyed(bedDestroyed: Boolean) {
        this.bedDestroyed = bedDestroyed
        if (!bedDestroyed) {
            if (!getBed()!!.getBlock().getType().toString().contains("BED")) {
                BedWars.plugin.getLogger().severe("Bed not set for team: " + getName() + " in arena: " + getArena()!!.getArenaName())
                return
            }
            nms.colorBed(this)
        } else {
            bed!!.getBlock().setType(Material.AIR)
        }
        for (bh in beds.values) {
            bh.hide()
            bh.show()
        }
    }

    @Deprecated("")
    override fun getIronGenerator(): IGenerator? {
        val gens = generators.stream().filter({ f -> f.getType() == GeneratorType.IRON }).toArray() as Array<IGenerator>
        if (gens.size == 0) {
            return null
        }
        return gens[0]
    }

    @Deprecated("")
    override fun getGoldGenerator(): IGenerator? {
        val gens = generators.stream().filter({ f -> f.getType() == GeneratorType.GOLD }).toArray() as Array<IGenerator>
        if (gens.size == 0) {
            return null
        }
        return gens[0]
    }

    @Deprecated("")
    override fun getEmeraldGenerator(): IGenerator? {
        val gens = generators.stream().filter({ f -> f.getType() == GeneratorType.EMERALD }).toArray() as Array<IGenerator>
        if (gens.size == 0) {
            return null
        }
        return gens[0]
    }

    @Deprecated("")
    override fun setEmeraldGenerator(emeraldGenerator: IGenerator) {
        generators.add(emeraldGenerator)
    }

    override fun getGenerators(): MutableList<IGenerator> {
        return generators
    }

    override fun getBaseEffects(): MutableList<PotionEffect>? {
        return base
    }

    open fun getTeamEffects(): MutableList<PotionEffect>? {
        return teamEffects
    }

    override fun getBowsEnchantments(): MutableList<TeamEnchant>? {
        return bowsEnchantments
    }

    override fun getSwordsEnchantments(): MutableList<TeamEnchant>? {
        return swordsEnchantemnts
    }

    override fun getArmorsEnchantments(): MutableList<TeamEnchant>? {
        return armorsEnchantemnts
    }

    override fun getArena(): Arena? {
        return arena
    }

    override fun getDragons(): Int {
        return dragons
    }

    override fun setDragons(amount: Int) {
        this.dragons = amount
    }

    override fun getMembersCache(): MutableList<Player>? {
        return membersCache
    }

    open fun getBeds(): HashMap<UUID, BedHolo> {
        return beds
    }

    override fun destroyData() {
        members = null
        spawn = null
        bed = null
        shop = null
        teamUpgrades = null
        for (ig in ArrayList(generators)) {
            ig.destroyData()
        }
        arena = null
        teamEffects = null
        base = null
        bowsEnchantments = null
        swordsEnchantemnts = null
        armorsEnchantemnts = null
        enemyBaseEnterTraps.clear()
        membersCache = null
    }

    override fun destroyBedHolo(player: Player) {
        if (getBeds().get(player.getUniqueId()) != null) {
            getBeds().get(player.getUniqueId())!!.destroy()
        }
    }

    override fun getActiveTraps(): LinkedList<EnemyBaseEnterTrap> {
        return enemyBaseEnterTraps
    }

    override fun getKillDropsLocation(): Vector? {
        if (killDropsLoc == null) {
            val gen = generators.stream().filter({ p -> (p.getType() == GeneratorType.IRON || p.getType() == GeneratorType.GOLD) }).collect(Collectors.toList())
            if (gen.isEmpty()) {
                return Vector(getSpawn()!!.getX(), getSpawn()!!.getY(), getSpawn()!!.getZ())
            }
            return Vector(gen.get(0).getLocation()!!.getX(), gen.get(0).getLocation()!!.getY(), gen.get(0).getLocation()!!.getZ())
        }
        return killDropsLoc
    }

    override fun setKillDropsLocation(loc: Vector?) {
        if (loc == null) {
            this.killDropsLoc = null
            return
        }
        this.killDropsLoc = Vector(loc.getBlockX() + 0.5, loc.getBlockY().toDouble(), loc.getBlockZ() + 0.5)
    }

    override fun isBed(location: Location): Boolean {
        var x: Int = location.getBlockX() - 1
        while (x < location.getBlockX() + 1) {
            var z: Int = location.getBlockZ() - 1
            while (z < location.getBlockZ() + 1) {
                if (getBed()!!.getBlockX() == x && getBed()!!.getBlockY() == location.getBlockY() && getBed()!!.getBlockZ() == z) {
                    return true
                }
                z++
            }
            x++
        }
        return false
    }

    open fun setKillDropsLocation(loc: Location?) {
        if (loc == null) {
            this.killDropsLoc = null
            return
        }
        this.killDropsLoc = Vector(loc.getBlockX() + 0.5, loc.getBlockY().toDouble(), loc.getBlockZ() + 0.5)
    }

    companion object {
        // Fall invulnerability when teammates respawn
        @JvmField var reSpawnInvulnerability: HashMap<UUID, Long> = HashMap()

        @JvmStatic
        private fun isEmptySlot(item: ItemStack?): Boolean {
            return item == null || item.getType() == Material.AIR || item.getAmount() <= 0
        }
    }
}
