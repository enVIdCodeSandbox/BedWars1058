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
import com.andrei1058.bedwars.BedWars.Companion.autoscale
import com.andrei1058.bedwars.BedWars.Companion.config
import com.andrei1058.bedwars.BedWars.Companion.debug
import com.andrei1058.bedwars.BedWars.Companion.getAPI
import com.andrei1058.bedwars.BedWars.Companion.getChatSupport
import com.andrei1058.bedwars.BedWars.Companion.getEconomy
import com.andrei1058.bedwars.BedWars.Companion.getGeneratorsCfg
import com.andrei1058.bedwars.BedWars.Companion.getLevelSupport
import com.andrei1058.bedwars.BedWars.Companion.getLobbyWorld
import com.andrei1058.bedwars.BedWars.Companion.getParty
import com.andrei1058.bedwars.BedWars.Companion.getServerType
import com.andrei1058.bedwars.BedWars.Companion.isShuttingDown
import com.andrei1058.bedwars.BedWars.Companion.mainCmd
import com.andrei1058.bedwars.BedWars.Companion.nms
import com.andrei1058.bedwars.BedWars.Companion.plugin
import com.andrei1058.bedwars.BedWars.Companion.signs
import com.andrei1058.bedwars.api.arena.GameState
import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.api.arena.NextEvent
import com.andrei1058.bedwars.api.arena.generator.GeneratorType
import com.andrei1058.bedwars.api.arena.generator.IGenerator
import com.andrei1058.bedwars.api.arena.shop.ShopHolo
import com.andrei1058.bedwars.api.arena.stats.*
import com.andrei1058.bedwars.api.arena.team.ITeam
import com.andrei1058.bedwars.api.arena.team.ITeamAssigner
import com.andrei1058.bedwars.api.arena.team.TeamColor
import com.andrei1058.bedwars.api.configuration.ConfigPath
import com.andrei1058.bedwars.api.entity.Despawnable
import com.andrei1058.bedwars.api.events.gameplay.GameEndEvent
import com.andrei1058.bedwars.api.events.gameplay.GameStateChangeEvent
import com.andrei1058.bedwars.api.events.gameplay.NextEventChangeEvent
import com.andrei1058.bedwars.api.events.player.PlayerJoinArenaEvent
import com.andrei1058.bedwars.api.events.player.PlayerKillEvent
import com.andrei1058.bedwars.api.events.player.PlayerLeaveArenaEvent
import com.andrei1058.bedwars.api.events.player.PlayerReJoinEvent
import com.andrei1058.bedwars.api.events.server.ArenaDisableEvent
import com.andrei1058.bedwars.api.events.server.ArenaEnableEvent
import com.andrei1058.bedwars.api.events.server.ArenaRestartEvent
import com.andrei1058.bedwars.api.language.Language
import com.andrei1058.bedwars.api.language.Language.Companion.getDefaultLanguage
import com.andrei1058.bedwars.api.language.Language.Companion.getList
import com.andrei1058.bedwars.api.language.Language.Companion.getMsg
import com.andrei1058.bedwars.api.language.Language.Companion.getPlayerLanguage
import com.andrei1058.bedwars.api.language.Language.Companion.saveIfNotExists
import com.andrei1058.bedwars.api.language.Messages
import com.andrei1058.bedwars.api.region.Region
import com.andrei1058.bedwars.api.server.ServerType
import com.andrei1058.bedwars.api.sidebar.ISidebar
import com.andrei1058.bedwars.api.tasks.PlayingTask
import com.andrei1058.bedwars.api.tasks.RestartingTask
import com.andrei1058.bedwars.api.tasks.StartingTask
import com.andrei1058.bedwars.arena.stats.GameStatsManager
import com.andrei1058.bedwars.arena.stats.StatisticsOrdered
import com.andrei1058.bedwars.arena.tasks.GamePlayingTask
import com.andrei1058.bedwars.arena.tasks.GameRestartingTask
import com.andrei1058.bedwars.arena.tasks.GameStartingTask
import com.andrei1058.bedwars.arena.tasks.ReJoinTask
import com.andrei1058.bedwars.arena.team.BedWarsTeam
import com.andrei1058.bedwars.arena.upgrades.BaseListener.Companion.isOnABase
import com.andrei1058.bedwars.configuration.ArenaConfig
import com.andrei1058.bedwars.configuration.Sounds
import com.andrei1058.bedwars.levels.internal.InternalLevel
import com.andrei1058.bedwars.levels.internal.PerMinuteTask
import com.andrei1058.bedwars.listeners.blockstatus.BlockStatusListener
import com.andrei1058.bedwars.listeners.dropshandler.PlayerDrops
import com.andrei1058.bedwars.money.internal.MoneyPerMinuteTask
import com.andrei1058.bedwars.shop.ShopCache
import com.andrei1058.bedwars.sidebar.BwSidebar
import com.andrei1058.bedwars.sidebar.SidebarService
import com.andrei1058.bedwars.support.citizens.JoinNPC
import com.andrei1058.bedwars.support.paper.TeleportManager
import com.andrei1058.bedwars.support.papi.SupportPAPI
import com.andrei1058.bedwars.support.vault.WithEconomy
import com.andrei1058.bedwars.teamselector.teamselector.TeamSelectorAssigner
import java.io.File
import java.time.Instant
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Level
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.block.Sign
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitScheduler
import org.bukkit.util.Vector
import com.andrei1058.bedwars.api.util.unchecked
import com.andrei1058.bedwars.api.util.uninitialized

open class Arena : IArena {
    private var players: MutableList<Player>? = ArrayList()
    private var spectators: MutableList<Player>? = ArrayList()
    private var signs: MutableList<Block>? = ArrayList()
    private var status: GameState = GameState.restarting
    private var yml: YamlConfiguration? = null
    private var cm: ArenaConfig? = null
    private var minPlayers: Int = 2
    private var maxPlayers: Int = 10
    private var maxInTeam: Int = 1
    private var islandRadius: Int = 10
    @JvmField var upgradeDiamondsCount: Int = 0
    @JvmField var upgradeEmeraldsCount: Int = 0
    @JvmField var allowSpectate: Boolean = true
    private var world: World? = null
    private var group: String = "Default"
    private var arenaName: String = uninitialized()
    private var worldName: String? = null
    private var teams: MutableList<ITeam>? = ArrayList()
    private var placed: LinkedList<org.bukkit.util.Vector>? = LinkedList()
    private var nextEvents: MutableList<String>? = ArrayList()
    private var regionsList: MutableList<Region>? = ArrayList()
    private var renderDistance: Int = 0
    private val leaving: MutableList<Player> = ArrayList()
    /**
     * Current event, used at scoreboard
     */
    private var nextEvent: NextEvent? = NextEvent.DIAMOND_GENERATOR_TIER_II
    private var diamondTier: Int = 1
    private var emeraldTier: Int = 1
    /**
     * Players in respawn session
     */
    private var respawnSessions: ConcurrentHashMap<Player, Int>? = ConcurrentHashMap()
    /**
     * Invisibility for armor when you drink an invisibility potion
     */
    private var showTime: ConcurrentHashMap<Player, Int>? = ConcurrentHashMap()
    private val gameStats: GameStatsManager = GameStatsManager(this)
    /* ARENA TASKS */
    private var startingTask: StartingTask? = null
    private var playingTask: PlayingTask? = null
    private var restartingTask: RestartingTask? = null
    /* ARENA GENERATORS */
    private var oreGenerators: MutableList<IGenerator>? = ArrayList()
    private var perMinuteTask: PerMinuteTask? = null
    private var moneyperMinuteTask: MoneyPerMinuteTask? = null
    private var respawnLocation: Location? = null
    private var spectatorLocation: Location? = null
    private var waitingLocation: Location? = null
    private var yKillHeight: Int = 0
    private var startTime: Instant = uninitialized()
    private var teamAssigner: ITeamAssigner? = TeamSelectorAssigner()
    private var allowMapBreak: Boolean = false
    private var winner: ITeam? = null

    /**
     * Load an arena.
     * This will check if it was set up right.
     *
     * @param name - world name
     * @param p    - This will send messages to the player if something went wrong while loading the arena. Can be NULL.
     */
    constructor(name: String, p: Player?) {
        if (!Instances.enabled()) {
            for (mm in enableQueue) {
                if (mm.getArenaName().equals(name, ignoreCase = true)) {
                    plugin.getLogger().severe("Tried to load arena " + name + " but it is already in the enable queue.")
                    if (p != null) {
                        p.sendMessage(ChatColor.RED.toString() + "Tried to load arena " + name + " but it is already in the enable queue.")
                    }
                    return
                }
            }
            if (getArenaByName(name) != null) {
                plugin.getLogger().severe("Tried to load arena " + name + " but it is already enabled.")
                if (p != null) {
                    p.sendMessage(ChatColor.RED.toString() + "Tried to load arena " + name + " but it is already enabled.")
                }
                return
            }
        }
        this.arenaName = name
        this.worldName = Instances.pickWorldName(arenaName)
        cm = ArenaConfig(BedWars.plugin, name, plugin.getDataFolder().getPath() + "/Arenas")
        yml = cm!!.getYml()
        if (yml!!.get("Team") == null) {
            if (p != null) {
                p.sendMessage("You didn't set any team for arena: " + name)
            }
            plugin.getLogger().severe("You didn't set any team for arena: " + name)
            return
        }
        if (yml!!.getConfigurationSection("Team").getKeys(false).size < 2) {
            if (p != null) {
                p.sendMessage("§cYou must set at least 2 teams on: " + name)
            }
            plugin.getLogger().severe("You must set at least 2 teams on: " + name)
            return
        }
        maxInTeam = yml!!.getInt("maxInTeam")
        maxPlayers = yml!!.getConfigurationSection("Team").getKeys(false).size * maxInTeam
        minPlayers = yml!!.getInt("minPlayers")
        allowSpectate = yml!!.getBoolean("allowSpectate")
        islandRadius = yml!!.getInt(ConfigPath.ARENA_ISLAND_RADIUS)
        allowMapBreak = yml!!.getBoolean(ConfigPath.ARENA_ALLOW_MAP_BREAK)
        if (config.getYml().get("arenaGroups") != null) {
            if (config.getYml().getStringList("arenaGroups").contains(yml!!.getString("group"))) {
                group = yml!!.getString("group")
            }
        }
        if (!BedWars.getAPI().getRestoreAdapter().isWorld(name)) {
            if (p != null) {
                p.sendMessage(ChatColor.RED.toString() + "There isn't any map called " + name)
            }
            plugin.getLogger().log(Level.WARNING, "There isn't any map called " + name)
            return
        }
        var error: Boolean = false
        for (team in yml!!.getConfigurationSection("Team").getKeys(false)) {
            var colorS: String? = yml!!.getString("Team." + team + ".Color")
            if (colorS == null) {
                continue
            }
            colorS = colorS.uppercase()
            try {
                TeamColor.valueOf(colorS)
            } catch (e: Exception) {
                if (p != null) {
                    p.sendMessage("§cInvalid color at team: " + team + " in arena: " + name)
                }
                plugin.getLogger().severe("Invalid color at team: " + team + " in arena: " + name)
                error = true
            }
            for (stuff in Arrays.asList("Color", "Spawn", "Bed", "Shop", "Upgrade", "Iron", "Gold")) {
                if (yml!!.get("Team." + team + "." + stuff) == null) {
                    if (p != null) {
                        p.sendMessage("§c" + stuff + " not set for " + team + " team on: " + name)
                    }
                    plugin.getLogger().severe(stuff + " not set for " + team + " team on: " + name)
                    error = true
                }
            }
        }
        if (yml!!.get("generator.Diamond") == null) {
            if (p != null) {
                p.sendMessage("§cThere isn't set any Diamond generator on: " + name)
            }
            plugin.getLogger().severe("There isn't set any Diamond generator on: " + name)
        }
        if (yml!!.get("generator.Emerald") == null) {
            if (p != null) {
                p.sendMessage("§cThere isn't set any Emerald generator on: " + name)
            }
            plugin.getLogger().severe("There isn't set any Emerald generator on: " + name)
        }
        if (yml!!.get("waiting.Loc") == null) {
            if (p != null) {
                p.sendMessage("§cWaiting spawn not set on: " + name)
            }
            plugin.getLogger().severe("Waiting spawn not set on: " + name)
            return
        }
        if (error) {
            return
        }
        yKillHeight = yml!!.getInt(ConfigPath.ARENA_Y_LEVEL_KILL)
        addToEnableQueue(this)
        Language.saveIfNotExists(Messages.ARENA_DISPLAY_GROUP_PATH + getGroup().lowercase(), getGroup()[0].toString().uppercase() + group.substring(1).lowercase())
    }

    /**
     * Use this method when the world was loaded successfully.
     */
    override fun init(world: World) {
        if (arenas.contains(this)) {
            return
        }
        if (!Instances.enabled() && getArenaByName(arenaName) != null) {
            return
        }
        removeFromEnableQueue(this)
        debug("Initialized arena " + getArenaName() + " with map " + world.getName())
        this.world = world
        this.worldName = world.getName()
        getConfig()!!.setName(worldName.unchecked())
        world.getEntities().stream().filter({ e -> e.getType() != EntityType.PLAYER }).filter({ e -> e.getType() != EntityType.PAINTING }).filter({ e -> e.getType() != EntityType.ITEM_FRAME }).forEach(Entity::remove)
        for (s in getConfig()!!.getList(ConfigPath.ARENA_GAME_RULES)) {
            val rule = s.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (rule.size == 2) {
                world.setGameRuleValue(rule[0], rule[1])
            }
        }
        world.setAutoSave(false)
        /* Clear setup armor-stands */
        for (e in world.getEntities()) {
            if (e.getType() == EntityType.ARMOR_STAND) {
                if (!(e as ArmorStand).isVisible()) {
                    e.remove()
                }
            }
        }
        //Create teams
        for (team in yml!!.getConfigurationSection("Team").getKeys(false)) {
            if (getTeam(team) != null) {
                BedWars.plugin.getLogger().severe("A team with name: " + team + " was already loaded for arena: " + getArenaName())
                continue
            }
            val bwt = BedWarsTeam(team, TeamColor.valueOf(yml!!.getString("Team." + team + ".Color").uppercase()), cm!!.getArenaLoc("Team." + team + ".Spawn").unchecked(), cm!!.getArenaLoc("Team." + team + ".Bed").unchecked(), cm!!.getArenaLoc("Team." + team + ".Shop").unchecked(), cm!!.getArenaLoc("Team." + team + ".Upgrade").unchecked(), this)
            teams!!.add(bwt)
            bwt.spawnGenerators()
        }
        //Load diamond/ emerald generators
        var location: Location?
        for (type in Arrays.asList("Diamond", "Emerald")) {
            if (yml!!.get("generator." + type) != null) {
                for (s in yml!!.getStringList("generator." + type)) {
                    location = cm!!.convertStringToArenaLocation(s)
                    if (location == null) {
                        plugin.getLogger().severe("Invalid location for " + type + " generator: " + s)
                        continue
                    }
                    oreGenerators!!.add(OreGenerator(location, this, GeneratorType.valueOf(type.uppercase()), null))
                }
            }
        }
        arenas.add(this)
        arenaByName.computeIfAbsent(getArenaName(), { k -> LinkedList() }).add(this)
        arenaByIdentifier.put(worldName.unchecked(), this)
        world.getWorldBorder().setCenter(cm!!.getArenaLoc("waiting.Loc"))
        world.getWorldBorder().setSize(yml!!.getInt("worldBorder").toDouble())
        /* Check if lobby removal is set */
        if (!getConfig()!!.getYml().isSet(ConfigPath.ARENA_WAITING_POS1) && getConfig()!!.getYml().isSet(ConfigPath.ARENA_WAITING_POS2)) {
            plugin.getLogger().severe("Lobby Pos1 isn't set! The arena's lobby won't be removed!")
        }
        if (getConfig()!!.getYml().isSet(ConfigPath.ARENA_WAITING_POS1) && !getConfig()!!.getYml().isSet(ConfigPath.ARENA_WAITING_POS2)) {
            plugin.getLogger().severe("Lobby Pos2 isn't set! The arena's lobby won't be removed!")
        }
        /* Register arena signs */
        registerSigns()
        //Call event
        Bukkit.getPluginManager().callEvent(ArenaEnableEvent(this))
        // Re Spawn Session Location
        respawnLocation = cm!!.getArenaLoc(ConfigPath.ARENA_SPEC_LOC)
        if (respawnLocation == null) {
            respawnLocation = cm!!.getArenaLoc("waiting.Loc")
        }
        if (respawnLocation == null) {
            respawnLocation = world.getSpawnLocation()
        }
        // Spectator location
        spectatorLocation = cm!!.getArenaLoc(ConfigPath.ARENA_SPEC_LOC)
        if (spectatorLocation == null) {
            spectatorLocation = cm!!.getArenaLoc("waiting.Loc")
        }
        if (spectatorLocation == null) {
            spectatorLocation = world.getSpawnLocation()
        }
        // Waiting location
        waitingLocation = cm!!.getArenaLoc("waiting.Loc")
        if (waitingLocation == null) {
            waitingLocation = world.getSpawnLocation()
        }
        changeStatus(GameState.waiting)
        //
        for (ne in NextEvent.values()) {
            nextEvents!!.add(ne.toString())
        }
        upgradeDiamondsCount = getGeneratorsCfg().getInt(if (getGeneratorsCfg().getYml().get(getGroup() + "." + ConfigPath.GENERATOR_DIAMOND_TIER_II_START) == null) "Default." + ConfigPath.GENERATOR_DIAMOND_TIER_II_START else getGroup() + "." + ConfigPath.GENERATOR_DIAMOND_TIER_II_START)
        upgradeEmeraldsCount = getGeneratorsCfg().getInt(if (getGeneratorsCfg().getYml().get(getGroup() + "." + ConfigPath.GENERATOR_EMERALD_TIER_II_START) == null) "Default." + ConfigPath.GENERATOR_EMERALD_TIER_II_START else getGroup() + "." + ConfigPath.GENERATOR_EMERALD_TIER_II_START)
        plugin.getLogger().info("Load done: " + getArenaName())
        // entity tracking range - player
        val yaml = YamlConfiguration.loadConfiguration(File("spigot.yml"))
        renderDistance = if (yaml.get("world-settings." + getWorldName() + ".entity-tracking-range.players") == null) yaml.getInt("world-settings.default.entity-tracking-range.players") else yaml.getInt("world-settings." + getWorldName() + ".entity-tracking-range.players")
    }

    /**
     * Add a player to the arena
     *
     * @param p              - Player to add.
     * @param skipOwnerCheck - True if you want to skip the party checking for this player. This
     * @return true if was added.
     */
    override fun addPlayer(p: Player?, skipOwnerCheck: Boolean): Boolean {
        if (p == null) {
            return false
        }
        debug("Player added: " + p.getName() + " arena: " + getArenaName())
        /* used for base enter/leave event */
        isOnABase.remove(p)
        //
        if (getArenaByPlayer(p) != null) {
            return false
        }
        if (getParty().hasParty(p)) {
            if (!skipOwnerCheck) {
                if (!getParty().isOwner(p)) {
                    p.sendMessage(getMsg(p, Messages.COMMAND_JOIN_DENIED_NOT_PARTY_LEADER))
                    return false
                }
                val partySize: Int = getParty().getMembers(p)!!.stream().filter(lambda10@{ member ->
                    val arena: IArena? = Arena.getArenaByPlayer(member)
                    if (arena == null) {
                        return@lambda10 true
                    }
                    arena.isSpectator(member)
                }).count().toInt()
                if (partySize > maxInTeam * getTeams()!!.size - getPlayers()!!.size) {
                    p.sendMessage(getMsg(p, Messages.COMMAND_JOIN_DENIED_PARTY_TOO_BIG))
                    return false
                }
                for (mem in getParty().getMembers(p)!!) {
                    if (mem === p) {
                        continue
                    }
                    val a: IArena? = Arena.getArenaByPlayer(mem)
                    if (a != null) {
                        /*if (a.isPlayer(mem)) {
                        a.removePlayer(mem, false);
                        } else */
                        if (a.isSpectator(mem)) {
                            a.removeSpectator(mem, false)
                        }
                    }
                    addPlayer(mem, true)
                }
            }
        }
        leaving.remove(p)
        if (status == GameState.waiting || (status == GameState.starting && (startingTask != null && startingTask!!.getCountdown() > 1))) {
            if (players!!.size >= maxPlayers && !isVip(p)) {
                val text = TextComponent(getMsg(p, Messages.COMMAND_JOIN_DENIED_IS_FULL))
                text.setClickEvent(ClickEvent(ClickEvent.Action.OPEN_URL, config.getYml().getString("storeLink")))
                p.spigot().sendMessage(text)
                return false
            } else if (players!!.size >= maxPlayers && isVip(p)) {
                var canJoin: Boolean = false
                for (on in ArrayList(players)) {
                    if (!isVip(on)) {
                        canJoin = true
                        removePlayer(on, false)
                        val vipKick = TextComponent(getMsg(p, Messages.ARENA_JOIN_VIP_KICK))
                        vipKick.setClickEvent(ClickEvent(ClickEvent.Action.OPEN_URL, config.getYml().getString("storeLink")))
                        p.spigot().sendMessage(vipKick)
                        break
                    }
                }
                if (!canJoin) {
                    p.sendMessage(getMsg(p, Messages.COMMAND_JOIN_DENIED_IS_FULL_OF_VIPS))
                    return false
                }
            }
            val ev = PlayerJoinArenaEvent(this, p, false)
            Bukkit.getPluginManager().callEvent(ev)
            if (ev.isCancelled()) {
                return false
            }
            //Remove from ReJoin
            val rejoin: ReJoin? = ReJoin.getPlayer(p)
            if (rejoin != null) {
                rejoin.destroy(true)
            }
            p.closeInventory()
            players!!.add(p)
            p.setFlying(false)
            p.setAllowFlight(false)
            p.setHealth(20.0)
            for (on in players!!) {
                on.sendMessage(getMsg(on, Messages.COMMAND_JOIN_PLAYER_JOIN_MSG).replace("{vPrefix}", getChatSupport().getPrefix(p)).replace("{vSuffix}", getChatSupport().getSuffix(p)).replace("{playername}", p.getName()).replace("{player}", p.getDisplayName()).replace("{on}", getPlayers()!!.size.toString()).replace("{max}", getMaxPlayers().toString()))
            }
            setArenaByPlayer(p, this)
            /* check if you can start the arena */
            var isStatusChange: Boolean = false
            if (status == GameState.waiting) {
                var teams: Int = 0
                var teammates: Int = 0
                for (on in getPlayers()!!) {
                    if (getParty().isOwner(on)) {
                        teams++
                    }
                    if (getParty().hasParty(on)) {
                        teammates++
                    }
                }
                if (minPlayers <= players!!.size && teams > 0 && players!!.size != teammates / teams) {
                    changeStatus(GameState.starting)
                    isStatusChange = true
                } else if (players!!.size >= minPlayers && teams == 0) {
                    changeStatus(GameState.starting)
                    isStatusChange = true
                }
            }
            //half full arena time shorten
            if (players!!.size >= getMaxPlayers() / 2 && players!!.size > minPlayers) {
                if (startingTask != null) {
                    if (Bukkit.getScheduler().isCurrentlyRunning(startingTask!!.getTask())) {
                        if (startingTask!!.getCountdown() > getConfig()!!.getInt(ConfigPath.GENERAL_CONFIGURATION_START_COUNTDOWN_HALF)) {
                            startingTask!!.setCountdown(BedWars.config.getInt(ConfigPath.GENERAL_CONFIGURATION_START_COUNTDOWN_HALF))
                        }
                    }
                }
            }
            /* save player inventory etc */
            if (getServerType() != ServerType.BUNGEE) {
                PlayerGoods(p, true)
                playerLocation.put(p, p.getLocation())
            }
            TeleportManager.teleportC(p, getWaitingLocation().unchecked(), PlayerTeleportEvent.TeleportCause.PLUGIN)
            if (!isStatusChange) {
                SidebarService.getInstance()!!.giveSidebar(p, this, false)
            }
            sendPreGameCommandItems(p)
            for (pf in p.getActivePotionEffects()) {
                p.removePotionEffect(pf.getType())
            }
        } else if (status == GameState.playing) {
            addSpectator(p, false, null)
            /* stop code if status playing*/
            return false
        }
        p.getInventory().setArmorContents(null)
        Bukkit.getScheduler().runTaskLater(plugin, Runnable {
            // bungee mode invisibility issues
            if (getServerType() == ServerType.BUNGEE) {
                //if (BedWars.nms.getVersion() == 7) {
                BedWars.nms.sendPlayerSpawnPackets(p, this)
                // fix invisibility issue
                //}
            }
            for (on in Bukkit.getOnlinePlayers()) {
                if (on == null) {
                    continue
                }
                if (on == p) {
                    continue
                }
                if (isPlayer(on)) {
                    BedWars.nms.spigotShowPlayer(p, on)
                    BedWars.nms.spigotShowPlayer(on, p)
                } else {
                    BedWars.nms.spigotHidePlayer(p, on)
                    BedWars.nms.spigotHidePlayer(on, p)
                }
            }
            if (getServerType() == ServerType.BUNGEE) {
                //if (BedWars.nms.getVersion() == 7) {
                BedWars.nms.sendPlayerSpawnPackets(p, this)
                // fix invisibility issue
                //}
            }
        }, 17L)
        if (getServerType() == ServerType.BUNGEE) {
            p.getEnderChest().clear()
        }
        if (getPlayers()!!.size >= getMaxPlayers()) {
            if (startingTask != null) {
                if (Bukkit.getScheduler().isCurrentlyRunning(startingTask!!.getTask())) {
                    if (startingTask!!.getCountdown() > BedWars.config.getInt(ConfigPath.GENERAL_CONFIGURATION_START_COUNTDOWN_SHORTENED)) {
                        startingTask!!.setCountdown(BedWars.config.getInt(ConfigPath.GENERAL_CONFIGURATION_START_COUNTDOWN_SHORTENED))
                    }
                }
            }
        }
        refreshSigns()
        JoinNPC.updateNPCs(getGroup())
        return true
    }

    /**
     * Add a player as Spectator
     *
     * @param p            Player to be added
     * @param playerBefore True if the player has played in this arena before and he died so now should be a spectator.
     */
    override fun addSpectator(p: Player, playerBefore: Boolean, staffTeleport: Location?): Boolean {
        if (allowSpectate || playerBefore || staffTeleport != null) {
            debug("Spectator added: " + p.getName() + " arena: " + getArenaName())
            if (!playerBefore) {
                val ev = PlayerJoinArenaEvent(this, p, true)
                Bukkit.getPluginManager().callEvent(ev)
                if (ev.isCancelled()) {
                    return false
                }
            }
            //Remove from ReJoin
            val reJoin: ReJoin? = ReJoin.getPlayer(p)
            if (reJoin != null) {
                reJoin.destroy(true)
            }
            leaving.remove(p)
            p.closeInventory()
            spectators!!.add(p)
            players!!.remove(p)
            updateSpectatorCollideRule(p, false)
            if (!playerBefore) {
                /* save player inv etc if isn't saved yet*/
                if (getServerType() != ServerType.BUNGEE) {
                    PlayerGoods(p, true)
                    playerLocation.put(p, p.getLocation())
                }
                setArenaByPlayer(p, this)
            }
            SidebarService.getInstance()!!.giveSidebar(p, this, false)
            nms.setCollide(p, this, false)
            if (!playerBefore) {
                if (staffTeleport == null) {
                    TeleportManager.teleportC(p, getSpectatorLocation().unchecked(), PlayerTeleportEvent.TeleportCause.PLUGIN)
                } else {
                    TeleportManager.teleportC(p, staffTeleport, PlayerTeleportEvent.TeleportCause.PLUGIN)
                }
            }
            p.setGameMode(GameMode.ADVENTURE)
            Bukkit.getScheduler().runTaskLater(plugin, Runnable lambda18@{
                if (leaving.contains(p)) {
                    return@lambda18
                }
                p.setAllowFlight(true)
                p.setFlying(true)
            }, 5L)
            if (p.getPassenger() != null && p.getPassenger().getType() == EntityType.ARMOR_STAND) {
                p.getPassenger().remove()
            }
            Bukkit.getScheduler().runTask(plugin, Runnable lambda19@{
                if (leaving.contains(p)) {
                    return@lambda19
                }
                for (on in Bukkit.getOnlinePlayers()) {
                    if (on === p) {
                        continue
                    }
                    if (getSpectators()!!.contains(on)) {
                        BedWars.nms.spigotShowPlayer(p, on)
                        BedWars.nms.spigotShowPlayer(on, p)
                    } else if (getPlayers()!!.contains(on)) {
                        BedWars.nms.spigotHidePlayer(p, on)
                        BedWars.nms.spigotShowPlayer(on, p)
                    } else {
                        BedWars.nms.spigotHidePlayer(p, on)
                        BedWars.nms.spigotHidePlayer(on, p)
                    }
                }
                if (!playerBefore) {
                    if (staffTeleport == null) {
                        TeleportManager.teleportC(p, getSpectatorLocation().unchecked(), PlayerTeleportEvent.TeleportCause.PLUGIN)
                    } else {
                        TeleportManager.teleport(p, staffTeleport)
                    }
                } else {
                    TeleportManager.teleport(p, getSpectatorLocation().unchecked())
                }
                p.setAllowFlight(true)
                p.setFlying(true)
                /* Spectator items */
                sendSpectatorCommandItems(p)
                // make invisible because it is annoying whene there are many spectators around the map
                p.addPotionEffect(PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, false))
                p.getInventory().setArmorContents(null)
            })
            leaving.remove(p)
            p.sendMessage(getMsg(p, Messages.COMMAND_JOIN_SPECTATOR_MSG).replace("{arena}", this.getDisplayName()))
            /* update generator holograms for spectators */
            val iso = Language.getPlayerLanguage(p).getIso()
            for (o in getOreGenerators()!!) {
                o.updateHolograms(p, iso.unchecked())
            }
            for (t in getTeams()!!) {
                for (o in t.getGenerators()) {
                    o.updateHolograms(p, iso.unchecked())
                }
            }
            for (sh in ShopHolo.getShopHolo()) {
                if (sh.getA() === this) {
                    sh.updateForPlayer(p, iso.unchecked())
                }
            }
        } else {
            p.sendMessage(getMsg(p, Messages.COMMAND_JOIN_SPECTATOR_DENIED_MSG))
            return false
        }
        showTime!!.remove(p)
        refreshSigns()
        JoinNPC.updateNPCs(getGroup())
        return true
    }

    /**
     * Remove a player from the arena
     *
     * @param p          Player to be removed
     * @param disconnect True if the player was disconnected
     */
    override fun removePlayer(p: Player, disconnect: Boolean) {
        if (leaving.contains(p)) {
            return
        } else {
            leaving.add(p)
        }
        debug("Player removed: " + p.getName() + " arena: " + getArenaName())
        respawnSessions!!.remove(p)
        var team: ITeam? = null
        Arena.afkCheck.remove(p.getUniqueId())
        BedWars.getAPI().getAFKUtil().setPlayerAFK(p, false)
        if (status == GameState.playing) {
            for (t in getTeams()!!) {
                if (t.isMember(p)) {
                    team = t
                    t.getMembers()!!.remove(p)
                    //noinspection deprecation
                    t.destroyBedHolo(p)
                }
            }
        }
        var cacheList: MutableList<ShopCache.CachedItem> = ArrayList()
        if (ShopCache.getShopCache(p.getUniqueId()) != null) {
            //noinspection ConstantConditions
            cacheList = ShopCache.getShopCache(p.getUniqueId())!!.getCachedPermanents()
        }
        val lastHit: LastHit? = LastHit.getLastHit(p)
        var lastDamager: Player? = if ((lastHit == null)) null else if ((lastHit.getDamager() is Player)) lastHit.getDamager() as Player else null
        if (lastHit != null) {
            // accept damager in last 13 seconds only.
            if (lastHit.getTime() < System.currentTimeMillis() - 13_000) {
                lastDamager = null
            }
        }
        Bukkit.getPluginManager().callEvent(PlayerLeaveArenaEvent(p, this, lastDamager))
        //players.remove must be under call event in order to check if the player is a spectator or not
        players!!.remove(p)
        removeArenaByPlayer(p, this)
        for (pf in p.getActivePotionEffects()) {
            p.removePotionEffect(pf.getType())
        }
        if (p.getPassenger() != null && p.getPassenger().getType() == EntityType.ARMOR_STAND) {
            p.getPassenger().remove()
        }
        var teamuri: Boolean = false
        for (on in getPlayers()!!) {
            if (getParty().hasParty(on)) {
                teamuri = true
            }
        }
        if (status == GameState.starting && (maxInTeam > players!!.size && teamuri || players!!.size < minPlayers && !teamuri)) {
            changeStatus(GameState.waiting)
            for (on in players!!) {
                on.sendMessage(getMsg(on, Messages.ARENA_START_COUNTDOWN_STOPPED_INSUFF_PLAYERS_CHAT))
            }
        } else if (status == GameState.playing) {
            BedWars.debug("removePlayer debug1")
            var alive_teams: Int = 0
            for (t in getTeams()!!) {
                if (t == null) {
                    continue
                }
                if (!t.getMembers()!!.isEmpty()) {
                    alive_teams++
                }
            }
            if (alive_teams == 1 && !BedWars.isShuttingDown()) {
                checkWinner()
                Bukkit.getScheduler().runTaskLater(BedWars.plugin, Runnable { changeStatus(GameState.restarting) }, 10L)
                if (team != null) {
                    if (!team.isBedDestroyed()) {
                        for (p2 in this.getPlayers()!!) {
                            p2.sendMessage(getMsg(p2, Messages.TEAM_ELIMINATED_CHAT).replace("{TeamColor}", team.getColor().chat().toString()).replace("{TeamName}", team.getDisplayName(Language.getPlayerLanguage(p2))))
                        }
                        for (p2 in this.getSpectators()!!) {
                            p2.sendMessage(getMsg(p2, Messages.TEAM_ELIMINATED_CHAT).replace("{TeamColor}", team.getColor().chat().toString()).replace("{TeamName}", team.getDisplayName(Language.getPlayerLanguage(p2))))
                        }
                    }
                }
            } else if (alive_teams == 0 && !BedWars.isShuttingDown()) {
                Bukkit.getScheduler().runTaskLater(BedWars.plugin, Runnable { changeStatus(GameState.restarting) }, 10L)
            } else if (!BedWars.isShuttingDown()) {
                //ReJoin feature
                ReJoin(p, this, team, cacheList)
            }
            // pvp log out
            if (team != null) {
                val killerTeam: ITeam? = getTeam(lastDamager.unchecked())
                if (lastDamager != null && isPlayer(lastDamager) && killerTeam != null) {
                    var message: String
                    var cause: PlayerKillEvent.PlayerKillCause
                    if (team.isBedDestroyed()) {
                        cause = PlayerKillEvent.PlayerKillCause.PLAYER_DISCONNECT_FINAL
                        message = Messages.PLAYER_DIE_PVP_LOG_OUT_FINAL
                    } else {
                        message = Messages.PLAYER_DIE_PVP_LOG_OUT_REGULAR
                        cause = PlayerKillEvent.PlayerKillCause.PLAYER_DISCONNECT
                    }
                    val event = PlayerKillEvent(this, p, team, lastDamager, killerTeam, { player -> Language.getMsg(player, message) }, cause)
                    Bukkit.getPluginManager().callEvent(event)
                    if (null != event.getMessage()) {
                        for (inGame in getPlayers()!!) {
                            val lang = Language.getPlayerLanguage(inGame)
                            inGame.sendMessage(event.getMessage()!!.apply(inGame)!!.replace("{PlayerTeamName}", team.getDisplayName(lang)).replace("{PlayerColor}", team.getColor().chat().toString()).replace("{PlayerName}", p.getDisplayName()).replace("{KillerColor}", killerTeam.getColor().chat().toString()).replace("{KillerName}", lastDamager.getDisplayName()).replace("{KillerTeamName}", killerTeam.getDisplayName(lang)))
                        }
                    }
                    if (null != event.getMessage()) {
                        for (inGame in getSpectators()!!) {
                            val lang = Language.getPlayerLanguage(inGame)
                            inGame.sendMessage(event.getMessage()!!.apply(inGame)!!.replace("{PlayerTeamName}", team.getDisplayName(lang)).replace("{PlayerColor}", team.getColor().chat().toString()).replace("{PlayerName}", p.getDisplayName()).replace("{KillerColor}", killerTeam.getColor().chat().toString()).replace("{KillerName}", lastDamager.getDisplayName()).replace("{KillerTeamName}", killerTeam.getDisplayName(lang)))
                        }
                    }
                    PlayerDrops.handlePlayerDrops(this, p, lastDamager, team, killerTeam, cause, ArrayList(Arrays.asList(*p.getInventory().getContents())))
                }
            }
        }
        for (on in getPlayers()!!) {
            on.sendMessage(getMsg(on, Messages.COMMAND_LEAVE_MSG).replace("{vPrefix}", getChatSupport().getPrefix(p)).replace("{vSuffix}", getChatSupport().getSuffix(p)).replace("{playername}", p.getName()).replace("{player}", p.getDisplayName()))
        }
        for (on in getSpectators()!!) {
            on.sendMessage(getMsg(on, Messages.COMMAND_LEAVE_MSG).replace("{vPrefix}", getChatSupport().getPrefix(p)).replace("{playername}", p.getName()).replace("{player}", p.getDisplayName()))
        }
        if (getServerType() == ServerType.SHARED) {
            SidebarService.getInstance()!!.remove(p)
            this.sendToMainLobby(p)
        } else if (getServerType() == ServerType.BUNGEE) {
            Misc.moveToLobbyOrKick(p, this, true)
            return
        } else {
            this.sendToMainLobby(p)
        }
        /* restore player inventory */
        val pg: PlayerGoods? = PlayerGoods.getPlayerGoods(p)
        if (pg == null) {
            // if there is no previous backup of the inventory send lobby items if multi arena
            if (BedWars.getServerType() == ServerType.MULTIARENA) {
                // Send items
                Arena.sendLobbyCommandItems(p)
            }
        } else {
            pg.restore()
        }
        playerLocation.remove(p)
        for (pf in p.getActivePotionEffects()) {
            p.removePotionEffect(pf.getType())
        }
        if (!BedWars.isShuttingDown()) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, Runnable {
                for (on in Bukkit.getOnlinePlayers()) {
                    if (on == p) {
                        continue
                    }
                    if (getArenaByPlayer(on) == null) {
                        BedWars.nms.spigotShowPlayer(p, on)
                        BedWars.nms.spigotShowPlayer(on, p)
                    } else {
                        BedWars.nms.spigotHidePlayer(p, on)
                        BedWars.nms.spigotHidePlayer(on, p)
                    }
                }
                if (!disconnect) {
                    SidebarService.getInstance()!!.giveSidebar(p, null, false)
                }
            }, 5L)
        }
        /* Remove also the party */
        if (getParty().hasParty(p)) {
            if (getParty().isOwner(p)) {
                if (status != GameState.restarting) {
                    if (getParty().isInternal()) {
                        for (mem in ArrayList(getParty().getMembers(p))) {
                            mem.sendMessage(getMsg(mem, Messages.ARENA_LEAVE_PARTY_DISBANDED))
                        }
                    }
                    getParty().disband(p)
                    // prevent arena from staring with a single player
                    teamuri = false
                    for (on in getPlayers()!!) {
                        if (getParty().hasParty(on)) {
                            teamuri = true
                        }
                    }
                    if (status == GameState.starting && (maxInTeam > players!!.size && teamuri || players!!.size < minPlayers && !teamuri)) {
                        changeStatus(GameState.waiting)
                        for (on in players!!) {
                            on.sendMessage(getMsg(on, Messages.ARENA_START_COUNTDOWN_STOPPED_INSUFF_PLAYERS_CHAT))
                        }
                    }
                }
            }
        }
        p.setFlying(false)
        p.setAllowFlight(false)
        //Remove from ReJoin if game ended
        if (status == GameState.restarting) {
            if (ReJoin.exists(p)) {
                //noinspection ConstantConditions
                if (ReJoin.getPlayer(p)!!.getArena() === this) {
                    //noinspection ConstantConditions
                    ReJoin.getPlayer(p)!!.destroy(false)
                }
            }
        }
        //Remove from magic milk
        if (magicMilk.containsKey(p.getUniqueId())) {
            val taskId: Int = magicMilk.remove(p.getUniqueId()).unchecked()
            if (taskId > 0) {
                Bukkit.getScheduler().cancelTask(taskId)
            }
        }
        showTime!!.remove(p)
        refreshSigns()
        JoinNPC.updateNPCs(getGroup())
        // remove player from party if leaves and the owner is still in the arena while waiting or starting
        if (status == GameState.waiting || status == GameState.starting) {
            if (BedWars.getParty().hasParty(p) && !BedWars.getParty().isOwner(p)) {
                for (pl in BedWars.getParty().getMembers(p)!!) {
                    if (BedWars.getParty().isOwner(pl) && pl.getWorld().getName().equals(getArenaName(), ignoreCase = true)) {
                        BedWars.getParty().removeFromParty(p)
                        break
                    }
                }
            }
        }
        if (lastHit != null) {
            lastHit.remove()
        }
    }

    /**
     * Remove a spectator from the arena
     *
     * @param p          Player to be removed
     * @param disconnect True if the player was disconnected
     */
    override fun removeSpectator(p: Player, disconnect: Boolean) {
        debug("Spectator removed: " + p.getName() + " arena: " + getArenaName())
        if (leaving.contains(p)) {
            return
        } else {
            leaving.add(p)
        }
        Bukkit.getPluginManager().callEvent(PlayerLeaveArenaEvent(p, this, null))
        spectators!!.remove(p)
        removeArenaByPlayer(p, this)
        p.getInventory().clear()
        p.getInventory().setArmorContents(null)
        nms.setCollide(p, this, true)
        Arena.afkCheck.remove(p.getUniqueId())
        BedWars.getAPI().getAFKUtil().setPlayerAFK(p, false)
        if (getServerType() == ServerType.SHARED) {
            SidebarService.getInstance()!!.remove(p)
            this.sendToMainLobby(p)
        } else if (getServerType() == ServerType.MULTIARENA) {
            this.sendToMainLobby(p)
        }
        for (pf in p.getActivePotionEffects()) {
            p.removePotionEffect(pf.getType())
        }
        /* restore player inventory */
        val pg: PlayerGoods? = PlayerGoods.getPlayerGoods(p)
        if (pg == null) {
            // if there is no previous backup of the inventory send lobby items if multi arena
            if (BedWars.getServerType() == ServerType.MULTIARENA) {
                // Send items
                Arena.sendLobbyCommandItems(p)
            }
        } else {
            pg.restore()
        }
        if (getServerType() == ServerType.BUNGEE) {
            Misc.moveToLobbyOrKick(p, this, true)
            return
        }
        playerLocation.remove(p)
        if (!BedWars.isShuttingDown()) {
            Bukkit.getScheduler().runTask(plugin, Runnable {
                for (on in Bukkit.getOnlinePlayers()) {
                    if (on == p) {
                        continue
                    }
                    if (getArenaByPlayer(on) == null) {
                        BedWars.nms.spigotShowPlayer(p, on)
                        BedWars.nms.spigotShowPlayer(on, p)
                    } else {
                        BedWars.nms.spigotHidePlayer(p, on)
                        BedWars.nms.spigotHidePlayer(on, p)
                    }
                }
                if (!disconnect) {
                    SidebarService.getInstance()!!.giveSidebar(p, null, false)
                }
            })
        }
        /* Remove also the party */
        if (getParty().hasParty(p)) {
            if (getParty().isOwner(p)) {
                if (status != GameState.restarting) {
                    if (getParty().isInternal()) {
                        for (mem in ArrayList(getParty().getMembers(p))) {
                            mem.sendMessage(getMsg(mem, Messages.ARENA_LEAVE_PARTY_DISBANDED))
                        }
                    }
                    getParty().disband(p)
                }
            }
        }
        p.setFlying(false)
        p.setAllowFlight(false)
        //Remove from ReJoin if game ended
        if (ReJoin.exists(p)) {
            //noinspection ConstantConditions
            if (ReJoin.getPlayer(p)!!.getArena() === this) {
                //noinspection ConstantConditions
                ReJoin.getPlayer(p)!!.destroy(false)
            }
        }
        //Remove from magic milk
        if (magicMilk.containsKey(p.getUniqueId())) {
            val taskId: Int = magicMilk.get(p.getUniqueId()).unchecked()
            if (taskId > 0) {
                Bukkit.getScheduler().cancelTask(taskId)
            }
        }
        refreshSigns()
        JoinNPC.updateNPCs(getGroup())
    }

    /**
     * Rejoin an arena
     */
    override fun reJoin(p: Player): Boolean {
        val reJoin: ReJoin? = ReJoin.getPlayer(p)
        if (reJoin == null) {
            return false
        }
        if (reJoin.getArena() !== this) {
            return false
        }
        if (!reJoin.canReJoin()) {
            return false
        }
        if (reJoin.getTask() != null) {
            reJoin.getTask()!!.destroy()
        }
        val ev = PlayerReJoinEvent(p, this, BedWars.config.getInt(ConfigPath.GENERAL_CONFIGURATION_RE_SPAWN_COUNTDOWN))
        Bukkit.getPluginManager().callEvent(ev)
        if (ev.isCancelled()) {
            return false
        }
        for (on in Bukkit.getOnlinePlayers()) {
            if (on == p) {
                continue
            }
            if (!isInArena(on)) {
                BedWars.nms.spigotHidePlayer(on, p)
                BedWars.nms.spigotHidePlayer(p, on)
            }
        }
        p.closeInventory()
        players!!.add(p)
        for (on in players!!) {
            on.sendMessage(getMsg(on, Messages.COMMAND_REJOIN_PLAYER_RECONNECTED).replace("{playername}", p.getName()).replace("{player}", p.getDisplayName()).replace("{on}", getPlayers()!!.size.toString()).replace("{max}", getMaxPlayers().toString()))
        }
        for (on in spectators!!) {
            on.sendMessage(getMsg(on, Messages.COMMAND_REJOIN_PLAYER_RECONNECTED).replace("{playername}", p.getName()).replace("{player}", p.getDisplayName()).replace("{on}", getPlayers()!!.size.toString()).replace("{max}", getMaxPlayers().toString()))
        }
        setArenaByPlayer(p, this)
        /* save player inventory etc */
        if (BedWars.getServerType() != ServerType.BUNGEE) {
            //new PlayerGoods(p, true, true);
            playerLocation.put(p, p.getLocation())
            // no need to backup inventory because it's empty
        }
        TeleportManager.teleportC(p, getSpectatorLocation().unchecked(), PlayerTeleportEvent.TeleportCause.PLUGIN)
        p.getInventory().clear()
        //restore items before re-spawning in team
        var sc: ShopCache? = ShopCache.getShopCache(p.getUniqueId())
        if (sc != null) {
            sc.destroy()
        }
        sc = ShopCache(p.getUniqueId())
        for (ci in reJoin.getPermanentsAndNonDowngradables()) {
            sc.getCachedItems()!!.add(ci)
        }
        reJoin.getBwt()!!.reJoin(p, ev.getRespawnTime())
        reJoin.destroy(false)
        SidebarService.getInstance()!!.giveSidebar(p, this, true)
        return true
    }

    /**
     * Disable the arena.
     * This will automatically kick/ remove the people from the arena.
     */
    override fun disable() {
        for (p in ArrayList(players)) {
            removePlayer(p, false)
        }
        for (p in ArrayList(spectators)) {
            removeSpectator(p, false)
        }
        if (getRestartingTask() != null) {
            getRestartingTask()!!.cancel()
        }
        if (getStartingTask() != null) {
            getStartingTask()!!.cancel()
        }
        if (getPlayingTask() != null) {
            getPlayingTask()!!.cancel()
        }
        plugin.getLogger().log(Level.WARNING, "Disabling arena: " + getArenaName())
        for (inWorld in getWorld()!!.getPlayers()) {
            inWorld.kickPlayer("You're not supposed to be here.")
        }
        BedWars.getAPI().getRestoreAdapter().onDisable(this)
        Bukkit.getPluginManager().callEvent(ArenaDisableEvent(getArenaName(), getWorldName().unchecked()))
        destroyData()
    }

    /**
     * Restart the arena.
     */
    override fun restart() {
        if (getRestartingTask() != null) {
            getRestartingTask()!!.cancel()
        }
        if (getStartingTask() != null) {
            getStartingTask()!!.cancel()
        }
        if (getPlayingTask() != null) {
            getPlayingTask()!!.cancel()
        }
        if (null != moneyperMinuteTask) {
            moneyperMinuteTask!!.cancel()
        }
        if (null != perMinuteTask) {
            perMinuteTask!!.cancel()
        }
        plugin.getLogger().log(Level.FINE, "Restarting arena: " + getArenaName())
        Bukkit.getPluginManager().callEvent(ArenaRestartEvent(getArenaName(), getWorldName().unchecked()))
        for (inWorld in getWorld()!!.getPlayers()) {
            inWorld.kickPlayer("You're not supposed to be here.")
        }
        BedWars.getAPI().getRestoreAdapter().onRestart(this)
        destroyData()
    }

    /**
     * Get the arena world
     */
    override fun getWorld(): World? {
        return world
    }

    override fun getMaxInTeam(): Int {
        return maxInTeam
    }

    /**
     * Get the display status for an arena.
     * A message that can be used on signs etc.
     */
    override fun getDisplayStatus(lang: Language): String {
        var s = ""
        when (status) {
            GameState.waiting -> {
                s = lang.m(Messages.ARENA_STATUS_WAITING_NAME)
            }
            GameState.starting -> {
                s = lang.m(Messages.ARENA_STATUS_STARTING_NAME)
            }
            GameState.restarting -> {
                s = lang.m(Messages.ARENA_STATUS_RESTARTING_NAME)
            }
            GameState.playing -> {
                s = lang.m(Messages.ARENA_STATUS_PLAYING_NAME)
            }
            else -> {}
        }
        return s.replace("{full}", if (this.getPlayers()!!.size == this.getMaxPlayers()) lang.m(Messages.MEANING_FULL) else "")
    }

    override fun getDisplayGroup(player: Player): String {
        return getPlayerLanguage(player).m(Messages.ARENA_DISPLAY_GROUP_PATH + getGroup().lowercase())
    }

    override fun getDisplayGroup(language: Language): String {
        return language.m(Messages.ARENA_DISPLAY_GROUP_PATH + getGroup().lowercase())
    }

    /**
     * Get the players list
     */
    override fun getPlayers(): MutableList<Player>? {
        return players
    }

    /**
     * Get the max number of players that can play on this arena.
     */
    override fun getMaxPlayers(): Int {
        return maxPlayers
    }

    /**
     * Get the arena name as a message that can be used on signs etc.
     *
     * @return A string with - and _ replaced by a space.
     */
    override fun getDisplayName(): String {
        return if (getConfig()!!.getYml().getString(ConfigPath.ARENA_DISPLAY_NAME, (Character.toUpperCase(arenaName[0]).toString() + arenaName.substring(1)).replace("_", " ").replace("-", " ")).trim().isEmpty()) (Character.toUpperCase(arenaName[0]).toString() + arenaName.substring(1)).replace("_", " ").replace("-", " ") else getConfig()!!.getString(ConfigPath.ARENA_DISPLAY_NAME)
    }

    override fun setWorldName(name: String) {
        this.worldName = name
    }

    override fun getGroup(): String {
        return group
    }

    override fun getArenaName(): String? {
        return arenaName
    }

    override fun getTeams(): MutableList<ITeam>? {
        return teams
    }

    override fun getConfig(): ArenaConfig? {
        return cm
    }

    override fun addPlacedBlock(block: Block?) {
        if (block == null) {
            return
        }
        placed!!.add(org.bukkit.util.Vector(block.getX(), block.getY(), block.getZ()))
    }

    override fun removePlacedBlock(block: Block?) {
        if (block == null) {
            return
        }
        if (!isBlockPlaced(block)) {
            return
        }
        placed!!.remove(org.bukkit.util.Vector(block.getX(), block.getY(), block.getZ()))
    }

    override fun isBlockPlaced(block: Block): Boolean {
        for (v in getPlaced()!!) {
            if (v.getX() == block.getX().toDouble() && v.getY() == block.getY().toDouble() && v.getZ() == block.getZ().toDouble()) {
                return true
            }
        }
        return false
    }

    /**
     * Get a player kills count.
     *
     * @param player     Target player
     * @param finalKills True if you want to get the Final Kills. False for regular kills.
     */
    @Deprecated("")
    override fun getPlayerKills(player: Player?, finalKills: Boolean): Int {
        if (null == player || null == getStatsHolder()) {
            return 0
        }
        val st = getStatsHolder()!!.get(player).flatMap({ stats -> stats.getStatistic(if (finalKills) DefaultStatistics.KILLS_FINAL else DefaultStatistics.BEDS_DESTROYED) })
        if (st.isEmpty()) {
            return 0
        }
        val gs = st.get()
        return if (gs is Incrementable) (gs.getValue() as Int) else 0
    }

    /**
     * Get the player beds destroyed count
     *
     * @param player Target player
     */
    @Deprecated("")
    override fun getPlayerBedsDestroyed(player: Player?): Int {
        if (null == player || null == getStatsHolder()) {
            return 0
        }
        val st = getStatsHolder()!!.get(player).flatMap({ stats -> stats.getStatistic(DefaultStatistics.BEDS_DESTROYED) })
        if (st.isEmpty()) {
            return 0
        }
        val gs = st.get()
        return if (gs is Incrementable) (gs.getValue() as Int) else 0
    }

    /**
     * Get the join signs for this arena
     *
     * @return signs.
     */
    override fun getSigns(): MutableList<Block>? {
        return signs
    }

    /**
     * Get the island radius
     */
    override fun getIslandRadius(): Int {
        return islandRadius
    }

    //SETTER METHODS
    override fun setGroup(group: String) {
        this.group = group
    }

    /**
     * Set game status without starting stats.
     */
    override fun setStatus(status: GameState) {
        if (this.status != GameState.playing && status == GameState.playing) {
            startTime = Instant.now()
        }
        // if countdown cancelled
        if (this.status == GameState.starting && status == GameState.waiting) {
            for (player in getPlayers()!!) {
                val playerLang = Language.getPlayerLanguage(player)
                nms.sendTitle(player, playerLang.m(Messages.ARENA_STATUS_START_COUNTDOWN_CANCELLED_TITLE), playerLang.m(Messages.ARENA_STATUS_START_COUNTDOWN_CANCELLED_SUB_TITLE), 0, 40, 10)
            }
        }
        this.status = status
    }

    /**
     * Change game status starting tasks.
     */
    override fun changeStatus(status: GameState) {
        // prevent called twice #https://github.com/andrei1058/BedWars1058/issues/774
        if (status == this.status) {
            return
        }
        if (this.status != GameState.playing && status == GameState.playing) {
            startTime = Instant.now()
        }
        this.status = status
        Bukkit.getPluginManager().callEvent(GameStateChangeEvent(this, status, status))
        refreshSigns()
        if (status == GameState.playing) {
            for (p in players!!) {
                Arena.afkCheck.remove(p.getUniqueId())
                BedWars.getAPI().getAFKUtil().setPlayerAFK(p, false)
            }
            for (p in spectators!!) {
                Arena.afkCheck.remove(p.getUniqueId())
                BedWars.getAPI().getAFKUtil().setPlayerAFK(p, false)
            }
            // Initialize game stats
            getPlayers()!!.forEach(gameStats::init)
        }
        //Stop active tasks to prevent issues
        val bs = Bukkit.getScheduler()
        if (startingTask != null) {
            if (bs.isCurrentlyRunning(startingTask!!.getTask()) || bs.isQueued(startingTask!!.getTask())) {
                startingTask!!.cancel()
            }
        }
        startingTask = null
        if (playingTask != null) {
            if (bs.isCurrentlyRunning(playingTask!!.getTask()) || bs.isQueued(playingTask!!.getTask())) {
                playingTask!!.cancel()
            }
        }
        playingTask = null
        if (restartingTask != null) {
            if (bs.isCurrentlyRunning(restartingTask!!.getTask()) || bs.isQueued(restartingTask!!.getTask())) {
                restartingTask!!.cancel()
            }
        }
        restartingTask = null
        if (null != moneyperMinuteTask) {
            moneyperMinuteTask!!.cancel()
        }
        if (null != perMinuteTask) {
            perMinuteTask!!.cancel()
        }
        players!!.forEach({ c -> SidebarService.getInstance()!!.giveSidebar(c, this, false) })
        spectators!!.forEach({ c -> SidebarService.getInstance()!!.giveSidebar(c, this, false) })
        if (status == GameState.starting) {
            startingTask = GameStartingTask(this)
        } else if (status == GameState.playing) {
            if (BedWars.getLevelSupport() is InternalLevel) {
                perMinuteTask = PerMinuteTask(this)
            }
            if (BedWars.getEconomy() is WithEconomy) {
                moneyperMinuteTask = MoneyPerMinuteTask(this)
            }
            playingTask = GamePlayingTask(this)
        } else if (status == GameState.restarting) {
            restartingTask = GameRestartingTask(this)
        }
    }

    /**
     * Check if a player is playing.
     */
    override fun isPlayer(p: Player): Boolean {
        return players!!.contains(p)
    }

    /**
     * Check if a player is spectating.
     */
    override fun isSpectator(p: Player): Boolean {
        return spectators!!.contains(p)
    }

    override fun isSpectator(player: UUID): Boolean {
        for (p in getSpectators()!!) {
            if (p.getUniqueId() == player) {
                return true
            }
        }
        return false
    }

    override fun isReSpawning(player: UUID?): Boolean {
        if (player == null) {
            return false
        }
        for (reSpawnSession in respawnSessions!!.keys) {
            if (reSpawnSession.getUniqueId() == player) {
                return true
            }
        }
        return false
    }

    /**
     * Add a join sign for the arena.
     */
    override fun addSign(loc: Location?) {
        if (loc == null) {
            return
        }
        if (loc.getBlock().getType().toString().endsWith("_SIGN") || loc.getBlock().getType().toString().endsWith("_WALL_SIGN")) {
            signs!!.add(loc.getBlock())
            refreshSigns()
            BlockStatusListener.updateBlock(this)
        }
    }

    /**
     * Get game stage.
     */
    override fun getStatus(): GameState {
        return status
    }

    /**
     * Refresh signs.
     */
    @Synchronized
    override fun refreshSigns() {
        if (signs != null) {
            val shown: IArena? = getArenaByName(arenaName)
            if (shown != null && shown !== this) {
                shown.refreshSigns()
                return
            }
        }
        for (b in getSigns()!!) {
            if (b == null) {
                continue
            }
            if (!(b.getType().toString().endsWith("_SIGN") || b.getType().toString().endsWith("_WALL_SIGN"))) {
                continue
            }
            if (b.getState() !is Sign) {
                continue
            }
            val s: Sign? = b.getState() as Sign?
            if (s == null) {
                return
            }
            var line: Int = 0
            for (string in BedWars.signs.getList("format")) {
                if (string == null) {
                    continue
                }
                if (getPlayers() == null) {
                    continue
                }
                s.setLine(line, string.replace("[on]", getPlayers()!!.size.toString()).replace("[max]", getMaxPlayers().toString()).replace("[arena]", getDisplayName()).replace("[status]", getDisplayStatus(Language.getDefaultLanguage())).replace("[type]", getMaxInTeam().toString()))
                line++
            }
            try {
                s.update(true)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    /**
     * Get a list of spectators for this arena.
     */
    override fun getSpectators(): MutableList<Player>? {
        return spectators
    }

    /**
     * Add a kill point to the game stats.
     */
    @Deprecated("")
    override fun addPlayerKill(player: Player, finalKill: Boolean, victim: Player) {
    }

    /**
     * Add a destroyed bed point to the player temp stats.
     */
    @Deprecated("")
    override fun addPlayerBedDestroyed(player: Player) {
    }

    /**
     * This will give the pre-game command Items.
     * This will clear the inventory first.
     */
    override fun sendPreGameCommandItems(p: Player) {
        if (config.getYml().get(ConfigPath.GENERAL_CONFIGURATION_PRE_GAME_ITEMS_PATH) == null) {
            return
        }
        p.getInventory().clear()
        for (item in config.getYml().getConfigurationSection(ConfigPath.GENERAL_CONFIGURATION_PRE_GAME_ITEMS_PATH).getKeys(false)) {
            if (config.getYml().get(ConfigPath.GENERAL_CONFIGURATION_PRE_GAME_ITEMS_MATERIAL.replace("%path%", item)) == null) {
                BedWars.plugin.getLogger().severe(ConfigPath.GENERAL_CONFIGURATION_PRE_GAME_ITEMS_MATERIAL.replace("%path%", item) + " is not set!")
                continue
            }
            if (config.getYml().get(ConfigPath.GENERAL_CONFIGURATION_PRE_GAME_ITEMS_DATA.replace("%path%", item)) == null) {
                BedWars.plugin.getLogger().severe(ConfigPath.GENERAL_CONFIGURATION_PRE_GAME_ITEMS_DATA.replace("%path%", item) + " is not set!")
                continue
            }
            if (config.getYml().get(ConfigPath.GENERAL_CONFIGURATION_PRE_GAME_ITEMS_SLOT.replace("%path%", item)) == null) {
                BedWars.plugin.getLogger().severe(ConfigPath.GENERAL_CONFIGURATION_PRE_GAME_ITEMS_SLOT.replace("%path%", item) + " is not set!")
                continue
            }
            if (config.getYml().get(ConfigPath.GENERAL_CONFIGURATION_PRE_GAME_ITEMS_ENCHANTED.replace("%path%", item)) == null) {
                BedWars.plugin.getLogger().severe(ConfigPath.GENERAL_CONFIGURATION_PRE_GAME_ITEMS_ENCHANTED.replace("%path%", item) + " is not set!")
                continue
            }
            if (config.getYml().get(ConfigPath.GENERAL_CONFIGURATION_PRE_GAME_ITEMS_COMMAND.replace("%path%", item)) == null) {
                BedWars.plugin.getLogger().severe(ConfigPath.GENERAL_CONFIGURATION_PRE_GAME_ITEMS_COMMAND.replace("%path%", item) + " is not set!")
                continue
            }
            val i = Misc.createItem(Material.valueOf(config.getYml().getString(ConfigPath.GENERAL_CONFIGURATION_PRE_GAME_ITEMS_MATERIAL.replace("%path%", item))), config.getInt(ConfigPath.GENERAL_CONFIGURATION_PRE_GAME_ITEMS_DATA.replace("%path%", item)).toByte(), config.getBoolean(ConfigPath.GENERAL_CONFIGURATION_PRE_GAME_ITEMS_ENCHANTED.replace("%path%", item)), SupportPAPI.getSupportPAPI().replace(p, getMsg(p, Messages.GENERAL_CONFIGURATION_WAITING_ITEMS_NAME.replace("%path%", item))), SupportPAPI.getSupportPAPI().replace(p, getList(p, Messages.GENERAL_CONFIGURATION_WAITING_ITEMS_LORE.replace("%path%", item))), p, "RUNCOMMAND", config.getYml().getString(ConfigPath.GENERAL_CONFIGURATION_PRE_GAME_ITEMS_COMMAND.replace("%path%", item)))
            p.getInventory().setItem(config.getInt(ConfigPath.GENERAL_CONFIGURATION_PRE_GAME_ITEMS_SLOT.replace("%path%", item)), i)
        }
    }

    /**
     * This will give the spectator command Items.
     * This will clear the inventory first.
     */
    override fun sendSpectatorCommandItems(p: Player) {
        if (config.getYml().get(ConfigPath.GENERAL_CONFIGURATION_SPECTATOR_ITEMS_PATH) == null) {
            return
        }
        p.getInventory().clear()
        for (item in config.getYml().getConfigurationSection(ConfigPath.GENERAL_CONFIGURATION_SPECTATOR_ITEMS_PATH).getKeys(false)) {
            if (config.getYml().get(ConfigPath.GENERAL_CONFIGURATION_SPECTATOR_ITEMS_MATERIAL.replace("%path%", item)) == null) {
                BedWars.plugin.getLogger().severe(ConfigPath.GENERAL_CONFIGURATION_SPECTATOR_ITEMS_MATERIAL.replace("%path%", item) + " is not set!")
                continue
            }
            if (config.getYml().get(ConfigPath.GENERAL_CONFIGURATION_SPECTATOR_ITEMS_DATA.replace("%path%", item)) == null) {
                BedWars.plugin.getLogger().severe(ConfigPath.GENERAL_CONFIGURATION_SPECTATOR_ITEMS_DATA.replace("%path%", item) + " is not set!")
                continue
            }
            if (config.getYml().get(ConfigPath.GENERAL_CONFIGURATION_SPECTATOR_ITEMS_SLOT.replace("%path%", item)) == null) {
                BedWars.plugin.getLogger().severe(ConfigPath.GENERAL_CONFIGURATION_SPECTATOR_ITEMS_SLOT.replace("%path%", item) + " is not set!")
                continue
            }
            if (config.getYml().get(ConfigPath.GENERAL_CONFIGURATION_SPECTATOR_ITEMS_ENCHANTED.replace("%path%", item)) == null) {
                BedWars.plugin.getLogger().severe(ConfigPath.GENERAL_CONFIGURATION_SPECTATOR_ITEMS_ENCHANTED.replace("%path%", item) + " is not set!")
                continue
            }
            if (config.getYml().get(ConfigPath.GENERAL_CONFIGURATION_SPECTATOR_ITEMS_COMMAND.replace("%path%", item)) == null) {
                BedWars.plugin.getLogger().severe(ConfigPath.GENERAL_CONFIGURATION_SPECTATOR_ITEMS_COMMAND.replace("%path%", item) + " is not set!")
                continue
            }
            val i = Misc.createItem(Material.valueOf(config.getYml().getString(ConfigPath.GENERAL_CONFIGURATION_SPECTATOR_ITEMS_MATERIAL.replace("%path%", item))), config.getInt(ConfigPath.GENERAL_CONFIGURATION_SPECTATOR_ITEMS_DATA.replace("%path%", item)).toByte(), config.getBoolean(ConfigPath.GENERAL_CONFIGURATION_SPECTATOR_ITEMS_ENCHANTED.replace("%path%", item)), SupportPAPI.getSupportPAPI().replace(p, getMsg(p, Messages.GENERAL_CONFIGURATION_SPECTATOR_ITEMS_NAME.replace("%path%", item))), SupportPAPI.getSupportPAPI().replace(p, getList(p, Messages.GENERAL_CONFIGURATION_SPECTATOR_ITEMS_LORE.replace("%path%", item))), p, "RUNCOMMAND", config.getYml().getString(ConfigPath.GENERAL_CONFIGURATION_SPECTATOR_ITEMS_COMMAND.replace("%path%", item)))
            p.getInventory().setItem(config.getInt(ConfigPath.GENERAL_CONFIGURATION_SPECTATOR_ITEMS_SLOT.replace("%path%", item)), i)
        }
    }

    /**
     * Get team by player.
     * Make sure the player is in this arena first.
     */
    override fun getTeam(p: Player): ITeam? {
        if (null == getTeams()) {
            // arena data already destroyed
            return null
        }
        for (t in getTeams()!!) {
            if (t.isMember(p)) {
                return t
            }
        }
        return null
    }

    /**
     * Get ex team by player.
     * Check the team where he played before leaving or losing.
     */
    override fun getExTeam(p: UUID): ITeam? {
        if (null == getTeams()) {
            // arena data already destroyed
            return null
        }
        for (t in getTeams()!!) {
            if (t.wasMember(p)) {
                return t
            }
        }
        return null
    }

    /**
     * Get arena by player name.
     * Used to get the team for a player that has left the arena.
     * Make sure the player is in this arena first.
     */
    @Deprecated("")
    override fun getPlayerTeam(playerCache: String): ITeam? {
        for (t in getTeams()!!) {
            for (p in t.getMembersCache()!!) {
                if (p.getName() == playerCache) {
                    return t
                }
            }
        }
        return null
    }

    /**
     * Check winner. You can always do that.
     * It will manage the arena restart and the needed stuff.
     */
    override fun checkWinner() {
        if (status != GameState.restarting) {
            val max: Int = getTeams()!!.size
            var eliminated: Int = 0
            for (t in getTeams()!!) {
                if (t.getMembers()!!.isEmpty()) {
                    eliminated++
                } else {
                    winner = t
                }
            }
            if (max - eliminated == 1) {
                if (winner != null) {
                    if (!winner!!.getMembers()!!.isEmpty()) {
                        for (p in winner!!.getMembers()!!) {
                            if (!p.isOnline()) {
                                continue
                            }
                            p.getInventory().clear()
                        }
                    }
                    var winners = StringBuilder()
                    //noinspection deprecation
                    for (p in winner!!.getMembersCache()!!) {
                        if (!winners.toString().contains(p.getDisplayName())) {
                            winners.append(p.getDisplayName()).append(" ")
                        }
                    }
                    if (winners.toString().endsWith(" ")) {
                        winners = StringBuilder(winners.substring(0, winners.length - 1))
                    }
                    var topInChat: StatisticsOrdered? = null
                    if (null != getStatsHolder()) {
                        topInChat = StatisticsOrdered(this, getConfig()!!.getGameOverridableString(ConfigPath.GENERAL_GAME_END_CHAT_TOP_STATISTIC))
                        // hide stats row completely when placeholders cannot be replaced
                        if (getConfig()!!.getGameOverridableBoolean(ConfigPath.GENERAL_GAME_END_CHAT_TOP_HIDE_MISSING)) {
                            topInChat.setBoundsPolicy(StatisticsOrdered.BoundsPolicy.SKIP)
                        }
                    }
                    // this is assigned to scoreboards
                    val topInSidebar = StatisticsOrdered(this, getConfig()!!.getGameOverridableString(ConfigPath.GENERAL_GAME_END_SB_TOP_STATISTIC))
                    // hide stats row completely when placeholders cannot be replaced
                    if (getConfig()!!.getGameOverridableBoolean(ConfigPath.GENERAL_GAME_END_SB_TOP_HIDE_MISSING)) {
                        topInSidebar.setBoundsPolicy(StatisticsOrdered.BoundsPolicy.SKIP)
                    }
                    val receivers: MutableList<Player> = ArrayList(getPlayers()!!.size + getSpectators()!!.size)
                    receivers.addAll(getPlayers().unchecked())
                    receivers.addAll(getSpectators().unchecked())
                    if (null != topInChat) {
                        val statParser = topInChat.newParser()
                        for (receiver in receivers) {
                            val playerLang = Language.getPlayerLanguage(receiver)
                            val winnerTeamChat: String? = playerLang.m(Messages.GAME_END_TEAM_WON_CHAT)
                            // check if message disabled
                            if (null != winnerTeamChat && !winnerTeamChat.isBlank()) {
                                receiver.sendMessage(winnerTeamChat.replace("{TeamColor}", winner!!.getColor().chat().toString()).replace("{TeamName}", winner!!.getDisplayName(playerLang)))
                            }
                            if (winner!!.getMembers()!!.contains(receiver) || winner!!.wasMember(receiver.getUniqueId())) {
                                nms.sendTitle(receiver, getMsg(receiver, Messages.GAME_END_VICTORY_PLAYER_TITLE), null, 0, 70, 20)
                            } else {
                                nms.sendTitle(receiver, playerLang.m(Messages.GAME_END_GAME_OVER_PLAYER_TITLE), null, 0, 70, 20)
                            }
                            statParser.resetIndex()
                            // check if message is disabled
                            val topChat = getList(receiver, Messages.GAME_END_TOP_PLAYER_CHAT)
                            if (topChat.isEmpty() || topChat.size == 1 && topChat.get(0).isEmpty()) {
                                continue
                            }
                            for (s in topChat) {
                                var msg: String? = statParser.parseString(s, playerLang, playerLang.m(Messages.MEANING_NOBODY))
                                if (null == msg) {
                                    continue
                                }
                                msg = msg.replace("{winnerFormat}", if (getMaxInTeam() > 1) playerLang.m(Messages.FORMATTING_TEAM_WINNER_FORMAT).replace("{members}", winners.toString()) else playerLang.m(Messages.FORMATTING_SOLO_WINNER_FORMAT).replace("{members}", winners.toString())).replace("{TeamColor}", winner!!.getColor().chat().toString()).replace("{TeamName}", winner!!.getDisplayName(playerLang))
                                receiver.sendMessage(SupportPAPI.getSupportPAPI().replace(receiver, msg))
                            }
                            val sidebar = SidebarService.getInstance()!!.getSidebar(receiver)
                            if (sidebar is BwSidebar) {
                                (sidebar as BwSidebar).setTopStatistics(topInSidebar)
                            }
                        }
                    }
                }
                changeStatus(GameState.restarting)
                //Game end event
                var winners: MutableList<UUID> = ArrayList()
                val losers: MutableList<UUID> = ArrayList()
                val aliveWinners: MutableList<UUID> = ArrayList()
                for (p in getPlayers()!!) {
                    aliveWinners.add(p.getUniqueId())
                }
                if (winner != null) {
                    //noinspection deprecation
                    for (p in winner!!.getMembersCache()!!) {
                        winners.add(p.getUniqueId())
                    }
                }
                for (bwt in getTeams()!!) {
                    if (winner != null) {
                        if (bwt === winner) {
                            continue
                        }
                    }
                    //noinspection deprecation
                    for (p in bwt.getMembersCache()!!) {
                        losers.add(p.getUniqueId())
                    }
                }
                Bukkit.getPluginManager().callEvent(GameEndEvent(this, winners, losers, winner.unchecked(), aliveWinners))
            }
            if (players!!.isEmpty() && status != GameState.restarting) {
                changeStatus(GameState.restarting)
            }
        }
    }

    /**
     * Add a kill to the player temp stats.
     */
    @Deprecated("")
    override fun addPlayerDeath(player: Player) {
    }

    /**
     * Set next event for the arena.
     */
    override fun setNextEvent(nextEvent: NextEvent?) {
        if (this.nextEvent != null) {
            Sounds.playSound(this.nextEvent!!.getSoundPath(), getPlayers().unchecked())
            Sounds.playSound(this.nextEvent!!.getSoundPath(), getSpectators().unchecked())
        }
        Bukkit.getPluginManager().callEvent(NextEventChangeEvent(this, nextEvent.unchecked(), this.nextEvent.unchecked()))
        this.nextEvent = nextEvent
    }

    override fun updateNextEvent() {
        debug("---")
        debug("updateNextEvent called")
        if (nextEvent == NextEvent.EMERALD_GENERATOR_TIER_II && upgradeEmeraldsCount == 0) {
            // next diamond time < next emerald time
            val next: Int = getGeneratorsCfg().getInt(if (getGeneratorsCfg().getYml().get(getGroup() + "." + ConfigPath.GENERATOR_EMERALD_TIER_III_START) == null) "Default." + ConfigPath.GENERATOR_EMERALD_TIER_III_START else getGroup() + "." + ConfigPath.GENERATOR_EMERALD_TIER_III_START)
            if (upgradeDiamondsCount < next && diamondTier == 1) {
                setNextEvent(NextEvent.DIAMOND_GENERATOR_TIER_II)
            } else if (upgradeDiamondsCount < next && diamondTier == 2) {
                setNextEvent(NextEvent.DIAMOND_GENERATOR_TIER_III)
            } else {
                setNextEvent(NextEvent.EMERALD_GENERATOR_TIER_III)
            }
            upgradeEmeraldsCount = next
            emeraldTier = 2
            sendEmeraldsUpgradeMessages()
            for (o in getOreGenerators()!!) {
                if (o.getType() == GeneratorType.EMERALD && o.getBwt() == null) {
                    o.upgrade()
                }
            }
        } else if (nextEvent == NextEvent.DIAMOND_GENERATOR_TIER_II && upgradeDiamondsCount == 0) {
            val next: Int = getGeneratorsCfg().getInt(if (getGeneratorsCfg().getYml().get(getGroup() + "." + ConfigPath.GENERATOR_DIAMOND_TIER_III_START) == null) "Default." + ConfigPath.GENERATOR_DIAMOND_TIER_III_START else getGroup() + "." + ConfigPath.GENERATOR_DIAMOND_TIER_III_START)
            if (upgradeEmeraldsCount < next && emeraldTier == 1) {
                setNextEvent(NextEvent.EMERALD_GENERATOR_TIER_II)
            } else if (upgradeEmeraldsCount < next && emeraldTier == 2) {
                setNextEvent(NextEvent.EMERALD_GENERATOR_TIER_III)
            } else {
                setNextEvent(NextEvent.DIAMOND_GENERATOR_TIER_III)
            }
            upgradeDiamondsCount = next
            diamondTier = 2
            sendDiamondsUpgradeMessages()
            for (o in getOreGenerators()!!) {
                if (o.getType() == GeneratorType.DIAMOND && o.getBwt() == null) {
                    o.upgrade()
                }
            }
        } else if (nextEvent == NextEvent.EMERALD_GENERATOR_TIER_III && upgradeEmeraldsCount == 0) {
            emeraldTier = 3
            sendEmeraldsUpgradeMessages()
            if (diamondTier == 1 && upgradeDiamondsCount > 0) {
                setNextEvent(NextEvent.DIAMOND_GENERATOR_TIER_II)
            } else if (diamondTier == 2 && upgradeDiamondsCount > 0) {
                setNextEvent(NextEvent.DIAMOND_GENERATOR_TIER_III)
            } else {
                setNextEvent(NextEvent.BEDS_DESTROY)
            }
            for (o in getOreGenerators()!!) {
                if (o.getType() == GeneratorType.EMERALD && o.getBwt() == null) {
                    o.upgrade()
                }
            }
        } else if (nextEvent == NextEvent.DIAMOND_GENERATOR_TIER_III && upgradeDiamondsCount == 0) {
            diamondTier = 3
            sendDiamondsUpgradeMessages()
            if (emeraldTier == 1 && upgradeEmeraldsCount > 0) {
                setNextEvent(NextEvent.EMERALD_GENERATOR_TIER_II)
            } else if (emeraldTier == 2 && upgradeEmeraldsCount > 0) {
                setNextEvent(NextEvent.EMERALD_GENERATOR_TIER_III)
            } else {
                setNextEvent(NextEvent.BEDS_DESTROY)
            }
            for (o in getOreGenerators()!!) {
                if (o.getType() == GeneratorType.DIAMOND && o.getBwt() == null) {
                    o.upgrade()
                }
            }
        } else if (nextEvent == NextEvent.BEDS_DESTROY && getPlayingTask()!!.getBedsDestroyCountdown() == 0) {
            setNextEvent(NextEvent.ENDER_DRAGON)
        } else if (nextEvent == NextEvent.ENDER_DRAGON && getPlayingTask()!!.getDragonSpawnCountdown() == 0) {
            setNextEvent(NextEvent.GAME_END)
        }
        debug("---")
        debug(nextEvent.toString())
    }

    /**
     * Get next event.
     */
    override fun getNextEvent(): NextEvent? {
        return nextEvent
    }

    /**
     * Register join-signs for arena
     */
    private fun registerSigns() {
        if (getServerType() != ServerType.BUNGEE) {
            if (BedWars.signs.getYml().get("locations") != null) {
                for (st in BedWars.signs.getYml().getStringList("locations")) {
                    val data = st.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    if (data[0] == getArenaName()) {
                        var l: Location
                        try {
                            l = Location(Bukkit.getWorld(data[6]), java.lang.Double.parseDouble(data[1]), java.lang.Double.parseDouble(data[2]), java.lang.Double.parseDouble(data[3]))
                        } catch (e: Exception) {
                            //noinspection ImplicitArrayToString
                            plugin.getLogger().severe("Could not load sign at: " + data.toString())
                            continue
                        }
                        addSign(l)
                    }
                }
            }
        }
    }

    /**
     * Get a team by name
     */
    override fun getTeam(name: String): ITeam? {
        for (bwt in getTeams()!!) {
            if (bwt.getName() == name) {
                return bwt
            }
        }
        return null
    }

    /**
     * Get respawn sessions.
     */
    override fun getRespawnSessions(): ConcurrentHashMap<Player, Int>? {
        return respawnSessions
    }

    override fun updateSpectatorCollideRule(p: Player, collide: Boolean) {
    }

    /**
     * Get invisibility for armor
     */
    override fun getShowTime(): ConcurrentHashMap<Player, Int>? {
        return showTime
    }

    /**
     * Get instance of the starting task.
     */
    override fun getStartingTask(): StartingTask? {
        return startingTask
    }

    /**
     * Get instance of the playing task.
     */
    override fun getPlayingTask(): PlayingTask? {
        return playingTask
    }

    /**
     * Get instance of the game restarting task.
     */
    override fun getRestartingTask(): RestartingTask? {
        return restartingTask
    }

    /**
     * Get arena ore generators Ore Generators.
     */
    override fun getOreGenerators(): MutableList<IGenerator>? {
        return oreGenerators
    }

    /**
     * Get the list of next events to come.
     * Not ordered.
     */
    override fun getNextEvents(): MutableList<String> {
        return ArrayList(nextEvents)
    }

    /**
     * Get player deaths.
     */
    @Deprecated("")
    override fun getPlayerDeaths(player: Player?, finalDeaths: Boolean): Int {
        if (null == player || null == getStatsHolder()) {
            return 0
        }
        val st = getStatsHolder()!!.get(player).flatMap({ stats -> stats.getStatistic(if (finalDeaths) DefaultStatistics.DEATHS_FINAL else DefaultStatistics.DEATHS) })
        if (st.isEmpty()) {
            return 0
        }
        val gs = st.get()
        return if (gs is Incrementable) (gs.getValue() as Int) else 0
    }

    /**
     * Show upgrade announcement to players.
     * Change diamondTier value first.
     */
    override fun sendDiamondsUpgradeMessages() {
        for (p in getPlayers()!!) {
            p.sendMessage(getMsg(p, Messages.GENERATOR_UPGRADE_CHAT_ANNOUNCEMENT).replace("{generatorType}", getMsg(p, Messages.GENERATOR_HOLOGRAM_TYPE_DIAMOND)).replace("{tier}", getMsg(p, (if (diamondTier == 2) Messages.FORMATTING_GENERATOR_TIER2 else Messages.FORMATTING_GENERATOR_TIER3))))
        }
        for (p in getSpectators()!!) {
            p.sendMessage(getMsg(p, Messages.GENERATOR_UPGRADE_CHAT_ANNOUNCEMENT).replace("{generatorType}", getMsg(p, Messages.GENERATOR_HOLOGRAM_TYPE_DIAMOND)).replace("{tier}", getMsg(p, (if (diamondTier == 2) Messages.FORMATTING_GENERATOR_TIER2 else Messages.FORMATTING_GENERATOR_TIER3))))
        }
    }

    /**
     * Show upgrade announcement to players.
     * Change emeraldTier value first.
     */
    override fun sendEmeraldsUpgradeMessages() {
        for (p in getPlayers()!!) {
            p.sendMessage(getMsg(p, Messages.GENERATOR_UPGRADE_CHAT_ANNOUNCEMENT).replace("{generatorType}", getMsg(p, Messages.GENERATOR_HOLOGRAM_TYPE_EMERALD)).replace("{tier}", getMsg(p, (if (emeraldTier == 2) Messages.FORMATTING_GENERATOR_TIER2 else Messages.FORMATTING_GENERATOR_TIER3))))
        }
        for (p in getSpectators()!!) {
            p.sendMessage(getMsg(p, Messages.GENERATOR_UPGRADE_CHAT_ANNOUNCEMENT).replace("{generatorType}", getMsg(p, Messages.GENERATOR_HOLOGRAM_TYPE_EMERALD)).replace("{tier}", getMsg(p, (if (emeraldTier == 2) Messages.FORMATTING_GENERATOR_TIER2 else Messages.FORMATTING_GENERATOR_TIER3))))
        }
    }

    override fun getRegionsList(): MutableList<Region>? {
        return regionsList
    }

    override fun getPlaced(): LinkedList<Vector>? {
        return placed
    }
    private val fireballCooldowns: MutableMap<UUID, Long> = HashMap()

    override fun getFireballCooldowns(): MutableMap<UUID, Long> {
        return fireballCooldowns
    }

    override fun destroyData() {
        destroyReJoins()
        if (worldName != null) {
            arenaByIdentifier.remove(worldName)
        }
        arenas.remove(this)
        for (rjt in ReJoinTask.getReJoinTasks()) {
            if (rjt.getArena() === this) {
                rjt.destroy()
            }
        }
        for (despawnable in ArrayList(BedWars.nms.getDespawnablesList().values)) {
            if (despawnable.getTeam()!!.getArena() === this) {
                despawnable.destroy()
            }
        }
        unregisterInstance(this)
        arenaByPlayer.entries.removeIf({ entry -> entry.value === this })
        players = null
        spectators = null
        signs = null
        yml = null
        cm = null
        world = null
        for (og in oreGenerators!!) {
            og.destroyData()
        }
        isOnABase.entries.removeIf({ entry -> entry.value.getArena() == this })
        for (bwt in teams!!) {
            bwt.destroyData()
        }
        playerLocation.entries.removeIf({ e -> Objects.requireNonNull(e.value.getWorld()).getName().equals(worldName, ignoreCase = true) })
        teams = null
        placed = null
        nextEvents = null
        regionsList = null
        respawnSessions = null
        showTime = null
        startingTask = null
        playingTask = null
        restartingTask = null
        oreGenerators = null
        perMinuteTask = null
        moneyperMinuteTask = null
        leaving.clear()
        fireballCooldowns.clear()
    }

    override fun getUpgradeDiamondsCount(): Int {
        return upgradeDiamondsCount
    }

    override fun getUpgradeEmeraldsCount(): Int {
        return upgradeEmeraldsCount
    }

    override fun setAllowSpectate(allowSpectate: Boolean) {
        this.allowSpectate = allowSpectate
    }

    override fun isAllowSpectate(): Boolean {
        return allowSpectate
    }

    override fun getWorldName(): String? {
        return worldName
    }

    override fun getRenderDistance(): Int {
        return renderDistance
    }

    override fun getReSpawnLocation(): Location? {
        return respawnLocation
    }

    override fun getSpectatorLocation(): Location? {
        return spectatorLocation
    }

    override fun getWaitingLocation(): Location? {
        return waitingLocation
    }

    override fun startReSpawnSession(player: Player, seconds: Int): Boolean {
        if (respawnSessions!!.get(player) == null) {
            val arena: IArena? = Arena.getArenaByPlayer(player)
            if (arena == null) {
                return false
            }
            if (!arena.isPlayer(player)) {
                return false
            }
            player.getInventory().clear()
            if (seconds > 1) {
                // hide to others
                for (playing in arena.getPlayers()!!) {
                    if (playing == player) {
                        continue
                    }
                    BedWars.nms.spigotHidePlayer(player, playing)
                }
                TeleportManager.teleportC(player, getReSpawnLocation().unchecked(), PlayerTeleportEvent.TeleportCause.PLUGIN)
                player.setAllowFlight(true)
                player.setFlying(true)
                respawnSessions!!.put(player, seconds)
                Bukkit.getScheduler().runTaskLater(BedWars.plugin, Runnable {
                    player.setAllowFlight(true)
                    player.setFlying(true)
                    nms.setCollide(player, this, false)
                    // #274
                    for (invisible in getShowTime()!!.keys) {
                        BedWars.nms.hideArmor(invisible, player)
                    }
                    updateSpectatorCollideRule(player, false)
                    TeleportManager.teleportC(player, getReSpawnLocation().unchecked(), PlayerTeleportEvent.TeleportCause.PLUGIN)
                }, 10L)
            } else {
                val team = getTeam(player)
                team!!.respawnMember(player)
            }
            return true
        }
        return false
    }

    override fun isReSpawning(player: Player?): Boolean {
        return respawnSessions!!.containsKey(player.unchecked())
    }

    override fun equals(obj: Any?): Boolean {
        if (obj == null) {
            return false
        }
        if (obj is IArena) {
            return (obj as IArena).getWorldName() == this.getWorldName()
        }
        return false
    }

    private fun destroyReJoins() {
        val reJoins: MutableList<ReJoin> = ArrayList(ReJoin.getReJoinList())
        for (reJoin in reJoins) {
            if (reJoin.getArena() === this) {
                reJoin.destroy(true)
            }
        }
    }

    override fun isProtected(location: Location): Boolean {
        return Misc.isBuildProtected(location, this)
    }

    override fun abandonGame(player: Player?) {
        if (player == null) {
            return
        }
        val team: ITeam? = getTeams()!!.stream().filter({ team1 -> team1.wasMember(player.getUniqueId()) }).findFirst().orElse(null)
        if (team != null) {
            //noinspection deprecation
            team.getMembersCache()!!.removeIf({ cachedPlayer -> cachedPlayer.getUniqueId() == player.getUniqueId() })
            val rejoin: ReJoin? = ReJoin.getPlayer(player)
            if (rejoin != null) {
                rejoin.destroy(team.getMembers()!!.isEmpty())
            }
        }
    }

    override fun getYKillHeight(): Int {
        return yKillHeight
    }

    override fun getStartTime(): Instant {
        return startTime
    }

    override fun getTeamAssigner(): ITeamAssigner? {
        return teamAssigner
    }

    override fun getDiamondTier(): Int {
        return diamondTier
    }

    override fun getEmeraldTier(): Int {
        return emeraldTier
    }

    /** Sets the tier (1-3) the diamond generators are counted as. Used by the FastForward feature. */
    open fun setDiamondTier(tier: Int) {
        diamondTier = tier
    }

    /** Sets the tier (1-3) the emerald generators are counted as. Used by the FastForward feature. */
    open fun setEmeraldTier(tier: Int) {
        emeraldTier = tier
    }

    override fun getYHeightLimit(): Int {
        return getConfig()!!.getInt(ConfigPath.ARENA_CONFIGURATION_MAX_BUILD_Y)
    }

    override fun setTeamAssigner(teamAssigner: ITeamAssigner?) {
        if (teamAssigner == null) {
            this.teamAssigner = TeamSelectorAssigner()
            plugin.getLogger().info("Using Default team assigner on arena: " + this.getArenaName())
        } else {
            this.teamAssigner = teamAssigner
            plugin.getLogger().warning("Using " + teamAssigner.javaClass.getSimpleName() + " team assigner on arena: " + this.getArenaName())
        }
    }

    override fun getLeavingPlayers(): MutableList<Player> {
        return leaving
    }

    /**
     * Remove player from world.
     * Contains fall-backs.
     */
    private fun sendToMainLobby(player: Player) {
        if (BedWars.getServerType() == ServerType.SHARED) {
            val loc: Location? = playerLocation.get(player)
            if (loc == null) {
                TeleportManager.teleportC(player, Bukkit.getWorlds().get(0).getSpawnLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN)
                plugin.getLogger().log(Level.SEVERE, player.getName() + " was teleported to the main world because lobby location is not set!")
            } else {
                TeleportManager.teleportC(player, loc, PlayerTeleportEvent.TeleportCause.PLUGIN)
            }
        } else if (BedWars.getServerType() == ServerType.MULTIARENA) {
            if (BedWars.getLobbyWorld().isEmpty()) {
                TeleportManager.teleportC(player, Bukkit.getWorlds().get(0).getSpawnLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN)
                plugin.getLogger().log(Level.SEVERE, player.getName() + " was teleported to the main world because lobby location is not set!")
            } else {
                TeleportManager.teleportC(player, config.getConfigLoc("lobbyLoc").unchecked(), PlayerTeleportEvent.TeleportCause.PLUGIN)
            }
        }
    }

    override fun isAllowMapBreak(): Boolean {
        return allowMapBreak
    }

    override fun setAllowMapBreak(allowMapBreak: Boolean) {
        this.allowMapBreak = allowMapBreak
    }

    override fun getBedsTeam(location: Location): ITeam? {
        if (location.getWorld().getName() != this.worldName) {
            throw RuntimeException("Given location is not on this game world.")
        }
        if (!nms.isBed(location.getBlock().getType())) {
            return null
        }
        for (team in this.teams!!) {
            if (team.isBed(location)) {
                return team
            }
        }
        return null
    }

    override fun getWinner(): ITeam? {
        return winner
    }

    override fun isTeamBed(location: Location): Boolean {
        return null != getBedsTeam(location)
    }

    override fun getStatsHolder(): GameStatsHolder? {
        return gameStats
    }

    companion object {
        private val arenaByName: HashMap<String, LinkedList<IArena>> = HashMap()
        private val arenaByPlayer: HashMap<Player, IArena> = HashMap()
        private val arenaByIdentifier: HashMap<String, IArena> = HashMap()
        private val arenas: LinkedList<IArena> = LinkedList()
        private var gamesBeforeRestart: Int = config.getInt(ConfigPath.GENERAL_CONFIGURATION_BUNGEE_MODE_GAMES_BEFORE_RESTART)
        @JvmField var afkCheck: HashMap<UUID, Int> = HashMap()
        @JvmField var magicMilk: HashMap<UUID, Int> = HashMap()
        /**
         * Player location before joining.
         * The player is teleported to this location if the server is running in SHARED mode.
         */
        private val playerLocation: HashMap<Player, Location> = HashMap()
        private val enableQueue: LinkedList<IArena> = LinkedList()

        /**
         * Get an arena by arena name
         *
         * @param arenaName arena name
         */
        @JvmStatic
        fun getArenaByName(arenaName: String): IArena {
            return Instances.best(getArenasByName(arenaName)).unchecked()
        }

        /**
         * Get every running instance of an arena (oldest first).
         *
         * @param arenaName arena name
         */
        @JvmStatic
        fun getArenasByName(arenaName: String): MutableList<IArena> {
            val l: LinkedList<IArena>? = arenaByName.get(arenaName)
            return if (l == null) ArrayList() else ArrayList(l)
        }

        /**
         * Get an arena by world name
         *
         * @param worldName world name
         */
        @JvmStatic
        fun getArenaByIdentifier(worldName: String): IArena {
            return arenaByIdentifier.get(worldName).unchecked()
        }

        /**
         * Get an arena by a player. Spectator or Player.
         *
         * @param p Target player
         * @return The arena where the player is in. Can be NULL.
         */
        @JvmStatic
        fun getArenaByPlayer(p: Player): IArena {
            return arenaByPlayer.get(p).unchecked()
        }

        /**
         * Get an arenas list
         */
        @JvmStatic
        fun getArenas(): LinkedList<IArena> {
            return arenas
        }

        @JvmStatic
        fun setArenaByPlayer(p: Player, arena: IArena) {
            arenaByPlayer.put(p, arena)
            arena.refreshSigns()
            JoinNPC.updateNPCs(arena.getGroup())
        }

        @JvmStatic
        fun setArenaByName(arena: IArena) {
            val l = arenaByName.computeIfAbsent(arena.getArenaName(), { k -> LinkedList() })
            if (!l.contains(arena)) {
                l.add(arena)
            }
        }

        /**
         * Removes the inactive instances registered under the given arena name (all of them if every one is active).
         */
        @JvmStatic
        fun removeArenaByName(arena: String) {
            val key = arena.replace("_clone", "")
            val l: LinkedList<IArena>? = arenaByName.get(key)
            if (l == null) {
                return
            }
            // when every entry is active nothing is left to tell them apart, so the whole name is removed
            val removed: Boolean = l.removeIf({ x -> !arenas.contains(x) })
            if (!removed || l.isEmpty()) {
                arenaByName.remove(key)
            }
        }

        @JvmStatic
        private fun unregisterInstance(arena: Arena) {
            val l: LinkedList<IArena>? = arenaByName.get(arena.getArenaName())
            if (l == null) {
                return
            }
            l.remove(arena)
            if (l.isEmpty()) {
                arenaByName.remove(arena.getArenaName())
            }
        }

        @JvmStatic
        fun removeArenaByPlayer(p: Player, arena: IArena) {
            arenaByPlayer.remove(p)
            arena.refreshSigns()
            JoinNPC.updateNPCs(arena.getGroup())
        }

        /**
         * Check if a player has vip perms
         */
        @JvmStatic
        fun isVip(p: Player): Boolean {
            return p.hasPermission(mainCmd + ".*") || p.hasPermission(mainCmd + ".vip")
        }

        /**
         * This will give the lobby items to the player.
         * Not used in serverType BUNGEE.
         * This will clear the inventory first.
         */
        @JvmStatic
        fun sendLobbyCommandItems(p: Player) {
            if (config.getYml().get(ConfigPath.GENERAL_CONFIGURATION_LOBBY_ITEMS_PATH) == null) {
                return
            }
            if (!BedWars.config.getLobbyWorldName().equals(p.getWorld().getName(), ignoreCase = true)) {
                return
            }
            p.getInventory().clear()
            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, Runnable lambda98@{
                if (!BedWars.config.getLobbyWorldName().equals(p.getWorld().getName(), ignoreCase = true)) {
                    return@lambda98
                }
                for (item in config.getYml().getConfigurationSection(ConfigPath.GENERAL_CONFIGURATION_LOBBY_ITEMS_PATH).getKeys(false)) {
                    if (config.getYml().get(ConfigPath.GENERAL_CONFIGURATION_LOBBY_ITEMS_MATERIAL.replace("%path%", item)) == null) {
                        BedWars.plugin.getLogger().severe(ConfigPath.GENERAL_CONFIGURATION_LOBBY_ITEMS_MATERIAL.replace("%path%", item) + " is not set!")
                        continue
                    }
                    if (config.getYml().get(ConfigPath.GENERAL_CONFIGURATION_LOBBY_ITEMS_DATA.replace("%path%", item)) == null) {
                        BedWars.plugin.getLogger().severe(ConfigPath.GENERAL_CONFIGURATION_LOBBY_ITEMS_DATA.replace("%path%", item) + " is not set!")
                        continue
                    }
                    if (config.getYml().get(ConfigPath.GENERAL_CONFIGURATION_LOBBY_ITEMS_SLOT.replace("%path%", item)) == null) {
                        BedWars.plugin.getLogger().severe(ConfigPath.GENERAL_CONFIGURATION_LOBBY_ITEMS_SLOT.replace("%path%", item) + " is not set!")
                        continue
                    }
                    if (config.getYml().get(ConfigPath.GENERAL_CONFIGURATION_LOBBY_ITEMS_ENCHANTED.replace("%path%", item)) == null) {
                        BedWars.plugin.getLogger().severe(ConfigPath.GENERAL_CONFIGURATION_LOBBY_ITEMS_ENCHANTED.replace("%path%", item) + " is not set!")
                        continue
                    }
                    if (config.getYml().get(ConfigPath.GENERAL_CONFIGURATION_LOBBY_ITEMS_COMMAND.replace("%path%", item)) == null) {
                        BedWars.plugin.getLogger().severe(ConfigPath.GENERAL_CONFIGURATION_LOBBY_ITEMS_COMMAND.replace("%path%", item) + " is not set!")
                        continue
                    }
                    val i = Misc.createItem(Material.valueOf(config.getYml().getString(ConfigPath.GENERAL_CONFIGURATION_LOBBY_ITEMS_MATERIAL.replace("%path%", item))), config.getInt(ConfigPath.GENERAL_CONFIGURATION_LOBBY_ITEMS_DATA.replace("%path%", item)).toByte(), config.getBoolean(ConfigPath.GENERAL_CONFIGURATION_LOBBY_ITEMS_ENCHANTED.replace("%path%", item)), SupportPAPI.getSupportPAPI().replace(p, getMsg(p, Messages.GENERAL_CONFIGURATION_LOBBY_ITEMS_NAME.replace("%path%", item))), SupportPAPI.getSupportPAPI().replace(p, getList(p, Messages.GENERAL_CONFIGURATION_LOBBY_ITEMS_LORE.replace("%path%", item))), p, "RUNCOMMAND", config.getYml().getString(ConfigPath.GENERAL_CONFIGURATION_LOBBY_ITEMS_COMMAND.replace("%path%", item)))
                    p.getInventory().setItem(config.getInt(ConfigPath.GENERAL_CONFIGURATION_LOBBY_ITEMS_SLOT.replace("%path%", item)), i)
                }
            }, 15L)
        }

        /**
         * Check if a player is in the arena.
         *
         * @return true if is playing or spectating.
         */
        @JvmStatic
        fun isInArena(p: Player): Boolean {
            return arenaByPlayer.containsKey(p)
        }

        /**
         * Get arena by players list.
         */
        @JvmStatic
        fun getArenaByPlayer(): HashMap<Player, IArena> {
            return arenaByPlayer
        }

        /**
         * Get players count for a group
         */
        @JvmStatic
        fun getPlayers(group: String): Int {
            var i: Int = 0
            val groups = group.split("\\+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            for (g in groups) {
                for (a in getArenas()) {
                    if (a.getGroup().equals(g, ignoreCase = true)) {
                        i += a.getPlayers()!!.size
                    }
                }
            }
            return i
        }

        /**
         * Add a player to the most filled arena.
         * Check if is the party owner first.
         */
        @JvmStatic
        fun joinRandomArena(p: Player): Boolean {
            val arenas = getSorted(getArenas())
            val amount: Int = if (getParty().hasParty(p)) getParty().getMembers(p)!!.stream().filter(lambda102@{ member ->
                val arena: IArena? = Arena.getArenaByPlayer(member)
                if (arena == null) {
                    return@lambda102 true
                }
                arena.isSpectator(member)
            }).count().toInt() else 1
            for (a in arenas) {
                if (a.getPlayers()!!.size == a.getMaxPlayers()) {
                    continue
                }
                if (a.getMaxPlayers() - a.getPlayers()!!.size >= amount) {
                    if (a.addPlayer(p, false)) {
                        break
                    }
                }
            }
            return true
        }

        @JvmStatic
        fun getSorted(arenas: MutableList<IArena>): MutableList<IArena> {
            val sorted: MutableList<IArena> = ArrayList(arenas)
            sorted.sortWith(object : Comparator<IArena> {
                override fun compare(o1: IArena, o2: IArena): Int {
                    if (o1.getStatus() == GameState.starting && o2.getStatus() == GameState.starting) {
                        return Integer.compare(o2.getPlayers()!!.size, o1.getPlayers()!!.size)
                    } else if (o1.getStatus() == GameState.starting && o2.getStatus() != GameState.starting) {
                        return -1
                    } else if (o2.getStatus() == GameState.starting && o1.getStatus() != GameState.starting) {
                        return 1
                    } else if (o1.getStatus() == GameState.waiting && o2.getStatus() == GameState.waiting) {
                        return Integer.compare(o2.getPlayers()!!.size, o1.getPlayers()!!.size)
                    } else if (o1.getStatus() == GameState.waiting && o2.getStatus() != GameState.waiting) {
                        return -1
                    } else if (o2.getStatus() == GameState.waiting && o1.getStatus() != GameState.waiting) {
                        return 1
                    } else if (o1.getStatus() == GameState.playing && o2.getStatus() == GameState.playing) {
                        return 0
                    } else if (o1.getStatus() == GameState.playing && o2.getStatus() != GameState.playing) {
                        return -1
                    } else {
                        return 1
                    }
                }

                override fun equals(obj: Any?): Boolean {
                    return obj is IArena
                }
            })
            return sorted
        }

        /**
         * Add a player to the most filled arena from a group.
         */
        @JvmStatic
        fun joinRandomFromGroup(p: Player, group: String): Boolean {
            val arenas = getSorted(getArenas())
            val amount: Int = if (getParty().hasParty(p)) getParty().getMembers(p)!!.stream().filter(lambda104@{ member ->
                val arena: IArena? = Arena.getArenaByPlayer(member)
                if (arena == null) {
                    return@lambda104 true
                }
                arena.isSpectator(member)
            }).count().toInt() else 1
            val groups = group.split("\\+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            for (a in arenas) {
                if (a.getPlayers()!!.size == a.getMaxPlayers()) {
                    continue
                }
                for (g in groups) {
                    if (a.getGroup().equals(g, ignoreCase = true)) {
                        if (a.getMaxPlayers() - a.getPlayers()!!.size >= amount) {
                            if (a.addPlayer(p, false)) {
                                return true
                            }
                        }
                    }
                }
            }
            return false
        }

        @JvmStatic
        fun getGamesBeforeRestart(): Int {
            return gamesBeforeRestart
        }

        @JvmStatic
        fun setGamesBeforeRestart(gamesBeforeRestart: Int) {
            Arena.gamesBeforeRestart = gamesBeforeRestart
        }

        @JvmStatic
        fun getEnableQueue(): LinkedList<IArena> {
            return enableQueue
        }

        /**
         * Remove an arena from the enable queue.
         */
        @JvmStatic
        fun removeFromEnableQueue(a: IArena) {
            enableQueue.remove(a)
            // the adapter may load the world synchronously, which re-enters this method and empties the queue
            val next: IArena? = if (enableQueue.isEmpty()) null else enableQueue.get(0)
            if (next != null) {
                plugin.getLogger().info("Loading arena: " + next.getWorldName())
                BedWars.getAPI().getRestoreAdapter().onEnable(next)
            }
        }

        @JvmStatic
        fun addToEnableQueue(a: IArena) {
            enableQueue.add(a)
            plugin.getLogger().info("Arena " + a.getWorldName() + " was added to the enable queue.")
            if (enableQueue.size == 1) {
                BedWars.getAPI().getRestoreAdapter().onEnable(a)
                plugin.getLogger().info("Loading arena: " + a.getWorldName())
            }
        }

        // used for auto scale / multi instance conditions
        @JvmStatic
        fun canAutoScale(arenaName: String): Boolean {
            if (!Instances.enabled()) {
                return true
            }
            for (ar in Arena.getEnableQueue()) {
                if (ar.getArenaName().equals(arenaName, ignoreCase = true)) {
                    return false
                }
            }
            if (autoscale && Arena.getGamesBeforeRestart() != -1 && Arena.getArenas().size >= Arena.getGamesBeforeRestart()) {
                return false
            }
            var activeClones: Int = 0
            for (ar in Arena.getArenasByName(arenaName)) {
                // clone this arena only if there aren't available arena of the same kind
                if (Instances.isJoinable(ar)) {
                    return false
                }
                activeClones++
            }
            // check amount of active instances
            return Instances.limit() > activeClones
        }
    }
}
