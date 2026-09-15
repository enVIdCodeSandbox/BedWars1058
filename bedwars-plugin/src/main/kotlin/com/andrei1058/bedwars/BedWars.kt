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

package com.andrei1058.bedwars

import com.andrei1058.bedwars.features.FeatureManager
import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.api.configuration.ConfigManager
import com.andrei1058.bedwars.api.configuration.ConfigPath
import com.andrei1058.bedwars.api.language.Language
import com.andrei1058.bedwars.api.levels.Level
import com.andrei1058.bedwars.api.party.Party
import com.andrei1058.bedwars.api.server.RestoreAdapter
import com.andrei1058.bedwars.api.server.ServerType
import com.andrei1058.bedwars.api.server.VersionSupport
import com.andrei1058.bedwars.arena.Arena
import com.andrei1058.bedwars.arena.ArenaManager
import com.andrei1058.bedwars.arena.VoidChunkGenerator
import com.andrei1058.bedwars.arena.despawnables.TargetListener
import com.andrei1058.bedwars.arena.feature.SpoilPlayerTNTFeature
import com.andrei1058.bedwars.arena.spectator.SpectatorListeners
import com.andrei1058.bedwars.arena.stats.DefaultStatsHandler
import com.andrei1058.bedwars.arena.tasks.OneTick
import com.andrei1058.bedwars.arena.tasks.Refresh
import com.andrei1058.bedwars.arena.upgrades.BaseListener
import com.andrei1058.bedwars.arena.upgrades.HealPoolListner
import com.andrei1058.bedwars.commands.bedwars.MainCommand
import com.andrei1058.bedwars.commands.leave.LeaveCommand
import com.andrei1058.bedwars.commands.party.PartyCommand
import com.andrei1058.bedwars.commands.rejoin.RejoinCommand
import com.andrei1058.bedwars.commands.shout.ShoutCommand
import com.andrei1058.bedwars.configuration.*
import com.andrei1058.bedwars.database.Database
import com.andrei1058.bedwars.database.SQLite
import com.andrei1058.bedwars.halloween.HalloweenSpecial
import com.andrei1058.bedwars.language.*
import com.andrei1058.bedwars.levels.internal.InternalLevel
import com.andrei1058.bedwars.levels.internal.LevelListeners
import com.andrei1058.bedwars.listeners.*
import com.andrei1058.bedwars.listeners.arenaselector.ArenaSelectorListener
import com.andrei1058.bedwars.listeners.blockstatus.BlockStatusListener
import com.andrei1058.bedwars.listeners.chat.ChatAFK
import com.andrei1058.bedwars.listeners.chat.ChatFormatting
import com.andrei1058.bedwars.listeners.joinhandler.*
import com.andrei1058.bedwars.lobbysocket.ArenaSocket
import com.andrei1058.bedwars.lobbysocket.LoadedUsersCleaner
import com.andrei1058.bedwars.lobbysocket.SendTask
import com.andrei1058.bedwars.maprestore.internal.InternalAdapter
import com.andrei1058.bedwars.metrics.MetricsManager
import com.andrei1058.bedwars.money.internal.MoneyListeners
import com.andrei1058.bedwars.shop.ShopManager
import com.andrei1058.bedwars.sidebar.*
import com.andrei1058.bedwars.stats.StatsManager
import com.andrei1058.bedwars.support.citizens.CitizensListener
import com.andrei1058.bedwars.support.citizens.JoinNPC
import com.andrei1058.bedwars.support.papi.PAPISupport
import com.andrei1058.bedwars.support.papi.SupportPAPI
import com.andrei1058.bedwars.support.party.NoParty
import com.andrei1058.bedwars.support.party.PAF
import com.andrei1058.bedwars.support.party.PAFBungeecordRedisApi
import com.andrei1058.bedwars.support.party.PartiesAdapter
import com.andrei1058.bedwars.support.preloadedparty.PrePartyListener
import com.andrei1058.bedwars.support.vault.*
import com.andrei1058.bedwars.support.version.ServerVersionResolver
import com.andrei1058.bedwars.support.vipfeatures.VipFeatures
import com.andrei1058.bedwars.support.vipfeatures.VipListeners
import com.andrei1058.bedwars.teamselector.TeamSelectorModule
import com.andrei1058.vipfeatures.api.IVipFeatures
import com.andrei1058.vipfeatures.api.MiniGameAlreadyRegistered
import java.io.File
import java.lang.reflect.Constructor
import java.lang.reflect.InvocationTargetException
import java.util.*
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.WorldCreator
import org.bukkit.entity.Entity
import org.bukkit.entity.Monster
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.generator.ChunkGenerator
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.PluginDescriptionFile
import org.bukkit.plugin.RegisteredServiceProvider
import org.bukkit.plugin.ServicePriority
import org.bukkit.plugin.java.JavaPlugin
import com.andrei1058.bedwars.api.util.uninitialized

open class BedWars : JavaPlugin() {
    private var serverSoftwareSupport: Boolean = true

    override fun onLoad() {
        //Spigot support
        if (!ServerVersionResolver.isSpigotCompatible()) {
            this.getLogger().severe("I can't run on your server software. Please check:")
            this.getLogger().severe("https://gitlab.com/andrei1058/BedWars1058/wikis/compatibility")
            serverSoftwareSupport = false
            return
        }
        isPaper = ServerVersionResolver.detectPaper()
        plugin = this
        // Built-in features load first: some prepare files this plugin reads while it starts.
        FeatureManager.loadAll()
        //noinspection rawtypes
        var supp: Class<*>
        try {
            supp = Class.forName(ServerVersionResolver.getSupportClassName(version))
        } catch (e: ClassNotFoundException) {
            serverSoftwareSupport = false
            this.getLogger().severe("I can't run on your version: " + version)
            return
        } catch (e: LinkageError) {
            serverSoftwareSupport = false
            this.getLogger().severe("I can't run on your version: " + version)
            return
        }
        api = API()
        Bukkit.getServicesManager().register(com.andrei1058.bedwars.api.BedWars::class.java, api, this, ServicePriority.Highest)
        try {
            //noinspection unchecked
            nms = supp.getConstructor(Class.forName("org.bukkit.plugin.Plugin"), String::class.java).newInstance(this, version) as VersionSupport
        } catch (e: InstantiationException) {
            e.printStackTrace()
            serverSoftwareSupport = false
            this.getLogger().severe("Could not load support for server version: " + version)
            return
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
            serverSoftwareSupport = false
            this.getLogger().severe("Could not load support for server version: " + version)
            return
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
            serverSoftwareSupport = false
            this.getLogger().severe("Could not load support for server version: " + version)
            return
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
            serverSoftwareSupport = false
            this.getLogger().severe("Could not load support for server version: " + version)
            return
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
            serverSoftwareSupport = false
            this.getLogger().severe("Could not load support for server version: " + version)
            return
        } catch (e: LinkageError) {
            e.printStackTrace()
            serverSoftwareSupport = false
            this.getLogger().severe("Could not load support for server version: " + version)
            return
        }
        this.getLogger().info("Loading support for paper/spigot: " + version)
        // Setup languages
        English()
        Romanian()
        Italian()
        Polish()
        Spanish()
        Russian()
        Bangla()
        Persian()
        Hindi()
        Indonesia()
        Portuguese()
        SimplifiedChinese()
        Turkish()
        BedWars.config = MainConfig(this, "config")
        generators = GeneratorsConfig(this, "generators", this.getDataFolder().getPath())
        // Initialize signs config after the main config
        if (getServerType() != ServerType.BUNGEE) {
            signs = SignsConfig(this, "signs", this.getDataFolder().getPath())
        }
    }

    override fun onEnable() {
        if (!serverSoftwareSupport) {
            Bukkit.getPluginManager().disablePlugin(this)
            return
        }
        FeatureManager.enableEarly()
        nms.registerVersionListeners()
        if (!this.handleWorldAdapter()) {
            api.setRestoreAdapter(InternalAdapter(this))
            getLogger().info("Using internal world restore system.")
        }
        /* Register commands */
        nms.registerCommand(mainCmd, MainCommand(mainCmd))
        // newer versions do not seem to like delayed registration of commands
        if (nms.getVersion() >= 9) {
            this.registerDelayedCommands()
        } else {
            Bukkit.getScheduler().runTaskLater(this, this::registerDelayedCommands, 20L)
        }
        /* Setup plugin messaging channel */
        Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord")
        // define logger
        val out = plugin.getLogger()
        /* Check if lobby location is set. Required for non Bungee servers */
        if (BedWars.config.getLobbyWorldName().isEmpty() && serverType != ServerType.BUNGEE) {
            out.log(java.util.logging.Level.WARNING, "Lobby location is not set!")
        }
        /* Load lobby world if not main level
         * when the server finishes loading. */
        if (getServerType() == ServerType.MULTIARENA) {
            Bukkit.getScheduler().runTaskLater(this, Runnable {
                if (!BedWars.config.getLobbyWorldName().isEmpty()) {
                    if (Bukkit.getWorld(BedWars.config.getLobbyWorldName()) == null && File(Bukkit.getWorldContainer(), BedWars.config.getLobbyWorldName() + "/level.dat").exists()) {
                        if (!BedWars.config.getLobbyWorldName().equals(Bukkit.getServer().getWorlds().get(0).getName(), ignoreCase = true)) {
                            Bukkit.getScheduler().runTaskLater(this, Runnable {
                                Bukkit.createWorld(WorldCreator(BedWars.config.getLobbyWorldName()))
                                if (Bukkit.getWorld(BedWars.config.getLobbyWorldName()) != null) {
                                    Bukkit.getScheduler().runTaskLater(plugin, Runnable { Objects.requireNonNull(Bukkit.getWorld(BedWars.config.getLobbyWorldName())).getEntities().stream().filter({ e -> e is Monster }).forEach(Entity::remove) }, 20L)
                                }
                            }, 100L)
                        }
                    }
                    val l: Location? = BedWars.config.getConfigLoc("lobbyLoc")
                    if (l != null) {
                        val w: World? = Bukkit.getWorld(BedWars.config.getLobbyWorldName())
                        if (w != null) {
                            w.setSpawnLocation(l.getBlockX(), l.getBlockY(), l.getBlockZ())
                        }
                    }
                }
            }, 1L)
        }
        // Register events
        registerEvents(EnderPearlLanded(), QuitAndTeleportListener(), BreakPlace(), DamageDeathMove(), Inventory(), Interact(), RefreshGUI(), HungerWeatherSpawn(), CmdProcess(), FireballListener(), EggBridge(), SpectatorListeners(), BaseListener(), TargetListener(), LangListener(), Warnings(this), ChatAFK(), GameEndListener(), DefaultStatsHandler())
        if (BedWars.config.getBoolean(ConfigPath.GENERAL_CONFIGURATION_HEAL_POOL_ENABLE)) {
            registerEvents(HealPoolListner())
        }
        if (getServerType() == ServerType.BUNGEE) {
            if (autoscale) {
                //registerEvents(new ArenaListeners());
                ArenaSocket.lobbies.addAll(BedWars.config.getList(ConfigPath.GENERAL_CONFIGURATION_BUNGEE_OPTION_LOBBY_SERVERS))
                SendTask()
                registerEvents(AutoscaleListener(), PrePartyListener(), JoinListenerBungee())
                Bukkit.getScheduler().runTaskTimerAsynchronously(this, LoadedUsersCleaner(), 60L, 60L)
            } else {
                registerEvents(ServerPingListener(), JoinListenerBungeeLegacy())
            }
        } else if (getServerType() == ServerType.MULTIARENA || getServerType() == ServerType.SHARED) {
            registerEvents(ArenaSelectorListener(), BlockStatusListener())
            if (getServerType() == ServerType.MULTIARENA) {
                registerEvents(JoinListenerMultiArena())
            } else {
                registerEvents(JoinListenerShared())
            }
        }
        registerEvents(WorldLoadListener())
        if (!(getServerType() == ServerType.BUNGEE && autoscale)) {
            registerEvents(JoinHandlerCommon())
        }
        // Register setup-holograms fix
        registerEvents(ChunkLoad())
        registerEvents(InvisibilityPotionListener())
        /* Load join signs. */
        loadArenasAndSigns()
        statsManager = StatsManager()
        /* Party support */
        Bukkit.getScheduler().runTaskLater(this, Runnable {
            if (BedWars.config.getYml().getBoolean(ConfigPath.GENERAL_CONFIGURATION_ALLOW_PARTIES)) {
                if (getServer().getPluginManager().isPluginEnabled("Parties")) {
                    out.info("Hook into Parties (by AlessioDP) support!")
                    party = PartiesAdapter()
                } else if (Bukkit.getServer().getPluginManager().isPluginEnabled("PartyAndFriends")) {
                    out.info("Hook into Party and Friends for Spigot (by Simonsator) support!")
                    party = PAF()
                } else if (Bukkit.getServer().getPluginManager().isPluginEnabled("Spigot-Party-API-PAF")) {
                    out.info("Hook into Spigot Party API for Party and Friends Extended (by Simonsator) support!")
                    party = PAFBungeecordRedisApi()
                }
                if (party is NoParty) {
                    party = com.andrei1058.bedwars.support.party.Internal()
                    out.info("Loading internal Party system. /party")
                }
            } else {
                party = NoParty()
            }
        }, 10L)
        /* Levels support */
        setLevelAdapter(InternalLevel())
        /* Register tasks */
        Bukkit.getScheduler().runTaskTimer(this, Refresh(), 20L, 20L)
        if (BedWars.config.getBoolean(ConfigPath.GENERAL_CONFIGURATION_PERFORMANCE_ROTATE_GEN)) {
            //new OneTick().runTaskTimer(this, 120, 1);
            Bukkit.getScheduler().runTaskTimer(this, OneTick(), 120, 1)
        }
        /* Register NMS entities */
        nms.registerEntities()
        /* Database support */
        if (BedWars.config.getBoolean("database.enable")) {
            val mySQL = com.andrei1058.bedwars.database.MySQL()
            val time: Long = System.currentTimeMillis()
            if (!mySQL.connect()) {
                this.getLogger().severe("Could not connect to database! Please verify your credentials and make sure that the server IP is whitelisted in MySQL.")
                remoteDatabase = SQLite()
            } else {
                remoteDatabase = mySQL
            }
            if (System.currentTimeMillis() - time >= 5000) {
                this.getLogger().severe("It took " + ((System.currentTimeMillis() - time) / 1000) + " ms to establish a database connection!\n" + "Using this remote connection is not recommended!")
            }
            remoteDatabase.init()
        } else {
            remoteDatabase = SQLite()
            remoteDatabase.init()
        }
        /* Citizens support */
        Bukkit.getScheduler().runTaskLater(plugin, Runnable {
            if (this.getServer().getPluginManager().getPlugin("Citizens") != null) {
                JoinNPC.setCitizensSupport(true)
                out.info("Hook into Citizens support. /bw npc")
                registerEvents(CitizensListener())
            }
            //spawn NPCs
            try {
                JoinNPC.spawnNPCs()
            } catch (e: Exception) {
                this.getLogger().severe("Could not spawn CmdJoin NPCs. Make sure you have right version of Citizens for your server!")
                JoinNPC.setCitizensSupport(false)
            }
        }, 5L)
        /* Save messages for stats gui items if custom items added, for each language */
        Language.setupCustomStatsMessages()
        /* Team selector (used to be a separate addon) */
        TeamSelectorModule.enable()
        /* PlaceholderAPI Support */
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            out.info("Hooked into PlaceholderAPI support!")
            PAPISupport().register()
            SupportPAPI.setSupportPAPI(SupportPAPI.withPAPI())
        }
        /*
         * Vault support
         * The task is to initialize after all plugins have loaded,
         *  to make sure any economy/chat plugins have been loaded and registered.
         */
        Bukkit.getScheduler().runTask(this, Runnable {
            if (this.getServer().getPluginManager().getPlugin("Vault") != null) {
                try {
                    //noinspection rawtypes
                    val rsp: RegisteredServiceProvider<*>? = this.getServer().getServicesManager().getRegistration(net.milkbowl.vault.chat.Chat::class.java)
                    if (rsp != null) {
                        WithChat.setChat(rsp.getProvider() as net.milkbowl.vault.chat.Chat)
                        plugin.getLogger().info("Hooked into vault chat support!")
                        chat = WithChat()
                    } else {
                        plugin.getLogger().info("Vault found, but no chat provider!")
                        chat = NoChat()
                    }
                } catch (var2_2: Exception) {
                    chat = NoChat()
                }
                try {
                    registerEvents(MoneyListeners())
                    val rsp: RegisteredServiceProvider<net.milkbowl.vault.economy.Economy>? = this.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy::class.java)
                    if (rsp != null) {
                        WithEconomy.setEconomy(rsp.getProvider())
                        plugin.getLogger().info("Hooked into vault economy support!")
                        economy = WithEconomy()
                    } else {
                        plugin.getLogger().info("Vault found, but no economy provider!")
                        economy = NoEconomy()
                    }
                } catch (var2_2: Exception) {
                    economy = NoEconomy()
                }
            } else {
                chat = NoChat()
                economy = NoEconomy()
            }
        })
        /* Chat support */
        if (BedWars.config.getBoolean(ConfigPath.GENERAL_CHAT_FORMATTING)) {
            registerEvents(ChatFormatting())
        }
        /* Protect glass walls from tnt explosion */
        nms.registerTntWhitelist(BedWars.config.getDouble(ConfigPath.GENERAL_TNT_PROTECTION_END_STONE_BLAST).toFloat(), BedWars.config.getDouble(ConfigPath.GENERAL_TNT_PROTECTION_GLASS_BLAST).toFloat())
        /* Prevent issues on reload */
        for (p in Bukkit.getOnlinePlayers()) {
            p.kickPlayer("BedWars1058 was RELOADED! (do not reload plugins)")
        }
        /* Load sounds configuration */
        Sounds.init()
        /* Initialize shop */
        shop = ShopManager()
        //Leave this code at the end of the enable method
        for (l in Language.getLanguages()) {
            l.setupUnSetCategories()
            Language.addDefaultMessagesCommandItems(l)
        }
        LevelsConfig.init()
        /* Load Money Configuration */
        MoneyConfig.init()
        // bStats metrics
        MetricsManager.initService(this)
        if (Bukkit.getPluginManager().getPlugin("VipFeatures") != null) {
            try {
                val vf = Bukkit.getServicesManager().getRegistration(IVipFeatures::class.java).getProvider()
                vf.registerMiniGame(VipFeatures(this))
                registerEvents(VipListeners(vf))
                out.log(java.util.logging.Level.INFO, "Hook into VipFeatures support.")
            } catch (e: Exception) {
                out.warning("Could not load support for VipFeatures.")
            } catch (miniGameAlreadyRegistered: MiniGameAlreadyRegistered) {
                miniGameAlreadyRegistered.printStackTrace()
            }
        }
        Bukkit.getScheduler().runTaskLater(this, Runnable { out.info("This server is running in " + getServerType() + " with auto-scale " + autoscale) }, 100L)
        // Initialize team upgrades
        com.andrei1058.bedwars.upgrades.UpgradesManager.init()
        // Initialize sidebar manager
        if (SidebarService.init(this)) {
            out.info("Initializing SidebarLib by andrei1058")
        } else {
            this.getLogger().severe("SidebarLib by andrei1058 does not support your server version")
            Bukkit.getPluginManager().disablePlugin(this)
            return
        }
        // Halloween Special
        HalloweenSpecial.init()
        // TNT Spoil Feature
        SpoilPlayerTNTFeature.init()
        // Warn user if current server version support is deprecated
        this.performDeprecationCheck()
        // The regular features start on the first tick: after this plugin finished enabling and after the
        // early features (MapLoader, MultiGroup) hooked into it.
        Bukkit.getScheduler().runTask(this, Runnable { FeatureManager.enableAll() })
    }

    /**
     * Try loading custom adapter support.
     *
     * @return true when custom adapter was registered.
     */
    private fun handleWorldAdapter(): Boolean {
        val swmPlugin: Plugin? = Bukkit.getPluginManager().getPlugin("SlimeWorldManager")
        if (null == swmPlugin) {
            return false
        }
        val pluginDescription: PluginDescriptionFile? = swmPlugin.getDescription()
        if (null == pluginDescription) {
            return false
        }
        val versionString = pluginDescription.getVersion().split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        try {
            val major: Int = Integer.parseInt(versionString[0])
            val minor: Int = Integer.parseInt(versionString[1])
            val release: Int = if (versionString.size > 2) Integer.parseInt(versionString[2]) else 0
            var adapterPath: String
            if (major == 2 && minor == 2 && release == 1) {
                adapterPath = "com.andrei1058.bedwars.arena.mapreset.slime.SlimeAdapter"
            } else if (major == 2 && minor == 8 && release == 0) {
                adapterPath = "com.andrei1058.bedwars.arena.mapreset.slime.AdvancedSlimeAdapter"
            } else if (major > 2 || major == 2 && minor >= 10) {
                adapterPath = "com.andrei1058.bedwars.arena.mapreset.slime.SlimePaperAdapter"
            } else {
                return false
            }
            val constructor = Class.forName(adapterPath).getConstructor(Plugin::class.java)
            getLogger().info("Loading restore adapter: " + adapterPath + " ...")
            val candidate = constructor.newInstance(this) as RestoreAdapter
            api.setRestoreAdapter(candidate)
            getLogger().info("Hook into " + candidate.getDisplayName() + " as restore adapter.")
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            this.getLogger().info("Something went wrong! Using internal reset adapter...")
        }
        return false
    }

    private fun registerDelayedCommands() {
        if (!nms.isBukkitCommandRegistered("shout")) {
            nms.registerCommand("shout", ShoutCommand("shout"))
        }
        nms.registerCommand("rejoin", RejoinCommand("rejoin"))
        if (!(nms.isBukkitCommandRegistered("leave") && getServerType() == ServerType.BUNGEE)) {
            nms.registerCommand("leave", LeaveCommand("leave"))
        }
        if (getServerType() != ServerType.BUNGEE && BedWars.config.getBoolean(ConfigPath.GENERAL_ENABLE_PARTY_CMD)) {
            Bukkit.getLogger().info("Registering /party command..")
            nms.registerCommand("party", PartyCommand("party"))
        }
    }

    override fun onDisable() {
        shuttingDown = true
        if (!serverSoftwareSupport) {
            return
        }
        FeatureManager.disableAll()
        if (getServerType() == ServerType.BUNGEE) {
            ArenaSocket.disable()
        }
        for (a in LinkedList(Arena.getArenas())) {
            try {
                a.disable()
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
        FeatureManager.disableEarly()
    }

    private fun loadArenasAndSigns() {
        api.getRestoreAdapter().convertWorlds()
        val dir = File(plugin.getDataFolder(), "/Arenas")
        if (dir.exists()) {
            val files: MutableList<File> = ArrayList()
            val fls = dir.listFiles()
            for (fl in Objects.requireNonNull(fls)) {
                if (fl.isFile()) {
                    if (fl.getName().endsWith(".yml")) {
                        files.add(fl)
                    }
                }
            }
            if (serverType == ServerType.BUNGEE && !autoscale) {
                if (files.isEmpty()) {
                    this.getLogger().log(java.util.logging.Level.WARNING, "Could not find any arena!")
                    return
                }
                val r = Random()
                val x: Int = r.nextInt(files.size)
                val name = files.get(x).getName().replace(".yml", "")
                Arena(name, null)
            } else {
                for (file in files) {
                    Arena(file.getName().replace(".yml", ""), null)
                }
            }
        }
    }

    open fun performDeprecationCheck() {
        Bukkit.getScheduler().runTaskAsynchronously(this, Runnable {
            if (Arrays.stream(nms.javaClass.getAnnotations()).anyMatch({ annotation -> annotation is Deprecated })) {
                this.getLogger().warning("Support for " + getServerVersion() + " is scheduled for removal. " + "Please consider upgrading your server software to a newer Minecraft version.")
            }
        })
    }

    override fun getDefaultWorldGenerator(worldName: String, id: String): ChunkGenerator {
        return VoidChunkGenerator()
    }

    companion object {
        private var serverType: ServerType = ServerType.MULTIARENA
        @JvmField var debug: Boolean = true
        @JvmField var autoscale: Boolean = false
        @JvmField var mainCmd: String = "bw"
        @JvmField var link: String = "https://www.spigotmc.org/resources/50942/"
        @JvmField var signs: ConfigManager = uninitialized()
        @JvmField var generators: ConfigManager = uninitialized()
        @JvmField var config: MainConfig = uninitialized()
        @JvmField var shop: ShopManager = uninitialized()
        @JvmField var statsManager: StatsManager = uninitialized()
        @JvmField var plugin: BedWars = uninitialized()
        @JvmField var nms: VersionSupport = uninitialized()
        @JvmField var isPaper: Boolean = false
        private var party: Party = NoParty()
        private var chat: Chat = NoChat()
        @JvmField var level: Level = uninitialized()
        private var economy: Economy = uninitialized()
        private val version: String = ServerVersionResolver.getVersionName()
        private var lobbyWorld: String = ""
        private var shuttingDown: Boolean = false
        @JvmField var arenaManager: ArenaManager = ArenaManager()
        //remote database
        private var remoteDatabase: Database = uninitialized()
        private var api: com.andrei1058.bedwars.api.BedWars = uninitialized()

        @JvmStatic
        fun registerEvents(vararg listeners: Listener) {
            Arrays.stream(listeners).forEach({ l -> plugin.getServer().getPluginManager().registerEvents(l, plugin) })
        }

        @JvmStatic
        fun setDebug(value: Boolean) {
            debug = value
        }

        @JvmStatic
        fun setServerType(serverType: ServerType) {
            BedWars.serverType = serverType
            if (serverType == ServerType.BUNGEE) {
                autoscale = true
            }
        }

        @JvmStatic
        fun setAutoscale(autoscale: Boolean) {
            BedWars.autoscale = autoscale
        }

        @JvmStatic
        fun debug(message: String) {
            if (debug) {
                plugin.getLogger().info("DEBUG: " + message)
            }
        }

        @JvmStatic
        fun getForCurrentVersion(v18: String, v12: String, v13: String): String {
            when (getServerVersion()) {
                "v1_8_R3" -> {
                    return v18
                }
                "v1_12_R1" -> {
                    return v12
                }
                else -> {}
            }
            return v13
        }

        @JvmStatic
        fun getServerType(): ServerType {
            return serverType
        }

        @JvmStatic
        fun getParty(): Party {
            return party
        }

        @JvmStatic
        fun getChatSupport(): Chat {
            return chat
        }

        /**
         * Get current levels manager.
         */
        @JvmStatic
        fun getLevelSupport(): Level {
            return BedWars.level
        }

        @JvmStatic
        fun setLevelAdapter(levelsManager: Level) {
            if (levelsManager is InternalLevel) {
                if (LevelListeners.instance == null) {
                    Bukkit.getPluginManager().registerEvents(LevelListeners(), BedWars.plugin)
                }
            } else {
                if (LevelListeners.instance != null) {
                    PlayerJoinEvent.getHandlerList().unregister(LevelListeners.instance)
                    PlayerQuitEvent.getHandlerList().unregister(LevelListeners.instance)
                    LevelListeners.instance = null
                }
            }
            BedWars.level = levelsManager
        }

        @JvmStatic
        fun getEconomy(): Economy {
            return economy
        }

        @JvmStatic
        fun getGeneratorsCfg(): ConfigManager {
            return generators
        }

        @JvmStatic
        fun setLobbyWorld(lobbyWorld: String) {
            BedWars.lobbyWorld = lobbyWorld
        }

        /**
         * Get the server version
         * Ex: v1_8_R3
         *
         * @since v0.6.5beta
         */
        @JvmStatic
        fun getServerVersion(): String {
            return version
        }

        @JvmStatic
        fun getLobbyWorld(): String {
            return lobbyWorld
        }

        /**
         * Get remote database.
         */
        @JvmStatic
        fun getRemoteDatabase(): Database {
            return remoteDatabase
        }

        @JvmStatic
        fun getStatsManager(): StatsManager {
            return statsManager
        }

        @JvmStatic
        fun getAPI(): com.andrei1058.bedwars.api.BedWars {
            return api
        }

        @JvmStatic
        fun isShuttingDown(): Boolean {
            return shuttingDown
        }

        @JvmStatic
        fun setParty(party: Party) {
            BedWars.party = party
        }
    }
}
