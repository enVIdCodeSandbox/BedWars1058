package com.andrei1058.bedwars.sidebar

import com.andrei1058.bedwars.BedWars
import com.andrei1058.bedwars.BedWars.Companion.config
import com.andrei1058.bedwars.api.arena.GameState
import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.api.arena.team.ITeam
import com.andrei1058.bedwars.api.configuration.ConfigPath
import com.andrei1058.bedwars.api.events.sidebar.PlayerSidebarInitEvent
import com.andrei1058.bedwars.api.language.Language
import com.andrei1058.bedwars.api.language.Language.Companion.getScoreboard
import com.andrei1058.bedwars.api.language.Messages
import com.andrei1058.bedwars.api.server.ServerType
import com.andrei1058.bedwars.api.sidebar.ISidebar
import com.andrei1058.bedwars.api.sidebar.ISidebarService
import com.andrei1058.bedwars.metrics.MetricsManager
import com.andrei1058.bedwars.sidebar.thread.*
import com.andrei1058.spigot.sidebar.SidebarManager
import java.util.*
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import com.andrei1058.bedwars.api.util.unchecked

open class SidebarService private constructor() : ISidebarService {
    private val sidebarHandler: SidebarManager
    // the title/placeholder refresh tasks iterate this off the main thread while players join and leave
    private val sidebars: java.util.concurrent.ConcurrentHashMap<UUID, BwSidebar> = java.util.concurrent.ConcurrentHashMap()

    override fun giveSidebar(player: Player, arena: IArena?, delay: Boolean) {
        var sidebar: BwSidebar? = sidebars.getOrDefault(player.getUniqueId(), null)
        // check if we might need to remove the existing sidebar
        if (null != sidebar) {
            if (null == arena) {
                // if sidebar is disabled in lobby on shared or multi-arena mode
                if (!config.getBoolean(ConfigPath.SB_CONFIG_SIDEBAR_USE_LOBBY_SIDEBAR) || BedWars.getServerType() == ServerType.SHARED) {
                    this.remove(sidebar)
                    return
                }
            } else {
                // if sidebar is disabled in game
                if (!config.getBoolean(ConfigPath.SB_CONFIG_SIDEBAR_USE_GAME_SIDEBAR)) {
                    this.remove(sidebar)
                    return
                }
            }
        }
        // if sidebar was null but still disabled for lobbies
        if (!config.getBoolean(ConfigPath.SB_CONFIG_SIDEBAR_USE_LOBBY_SIDEBAR) && null == arena) {
            return
        }
        // if sidebar was null but still disabled in game
        if (!config.getBoolean(ConfigPath.SB_CONFIG_SIDEBAR_USE_GAME_SIDEBAR) && null != arena) {
            return
        }
        // set sidebar lines based on game state or lobby
        var lines: MutableList<String>? = null
        var title: MutableList<String>
        if (null == arena) {
            if (BedWars.getServerType() != ServerType.SHARED) {
                lines = Language.getList(player, Messages.SCOREBOARD_LOBBY)
            }
        } else {
            if (arena.getStatus() == GameState.waiting) {
                if (arena.isSpectator(player)) {
                    lines = getScoreboard(player, "sidebar." + arena.getGroup() + ".waiting.spectator", Messages.SCOREBOARD_DEFAULT_WAITING_SPEC)
                } else {
                    lines = getScoreboard(player, "sidebar." + arena.getGroup() + ".waiting.player", Messages.SCOREBOARD_DEFAULT_WAITING)
                }
            } else if (arena.getStatus() == GameState.starting) {
                if (arena.isSpectator(player)) {
                    lines = getScoreboard(player, "sidebar." + arena.getGroup() + ".starting.spectator", Messages.SCOREBOARD_DEFAULT_STARTING_SPEC)
                } else {
                    lines = getScoreboard(player, "sidebar." + arena.getGroup() + ".starting.player", Messages.SCOREBOARD_DEFAULT_STARTING)
                }
            } else if (arena.getStatus() == GameState.playing) {
                if (arena.isSpectator(player)) {
                    val holderExTeam: ITeam? = arena.getExTeam(player.getUniqueId())
                    if (null == holderExTeam) {
                        lines = getScoreboard(player, "sidebar." + arena.getGroup() + ".playing.spectator", Messages.SCOREBOARD_DEFAULT_PLAYING_SPEC)
                    } else {
                        lines = getScoreboard(player, "sidebar." + arena.getGroup() + ".playing.eliminated", Messages.SCOREBOARD_DEFAULT_PLAYING_SPEC_ELIMINATED)
                    }
                } else {
                    lines = getScoreboard(player, "sidebar." + arena.getGroup() + ".playing.alive", Messages.SCOREBOARD_DEFAULT_PLAYING)
                }
            } else if (arena.getStatus() == GameState.restarting) {
                val holderTeam: ITeam? = arena.getTeam(player)
                val holderExTeam: ITeam? = if (null == holderTeam) arena.getExTeam(player.getUniqueId()) else null
                if (null == holderTeam && null == holderExTeam) {
                    lines = getScoreboard(player, "sidebar." + arena.getGroup() + ".restarting.spectator", Messages.SCOREBOARD_DEFAULT_RESTARTING_SPEC)
                } else {
                    if (null == holderTeam && holderExTeam == arena.getWinner()) {
                        lines = getScoreboard(player, "sidebar." + arena.getGroup() + ".restarting.winner-eliminated", Messages.SCOREBOARD_DEFAULT_RESTARTING_WIN2)
                    } else if (null == holderExTeam && holderTeam == arena.getWinner()) {
                        lines = getScoreboard(player, "sidebar." + arena.getGroup() + ".restarting.winner-alive", Messages.SCOREBOARD_DEFAULT_RESTARTING_WIN1)
                    } else {
                        lines = getScoreboard(player, "sidebar." + arena.getGroup() + ".restarting.loser", Messages.SCOREBOARD_DEFAULT_RESTARTING_LOSER)
                    }
                }
            }
        }
        // if we do not have lines we eventually remove the sidebar
        if (null == lines || lines.isEmpty()) {
            if (null != sidebar) {
                this.remove(sidebar)
            }
            return
        }
        // title is the first line from array
        title = ArrayList(Arrays.asList(*lines.get(0).split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()))
        if (lines.size == 1) {
            lines = ArrayList()
        }
        lines = lines.subList(1, lines.size)
        // at this point we are sure we need a sidebar instance
        var newlyAdded: Boolean = false
        if (null == sidebar) {
            sidebar = BwSidebar(player)
            newlyAdded = true
            val event = PlayerSidebarInitEvent(player, sidebar)
            Bukkit.getPluginManager().callEvent(event)
            if (event.isCancelled()) {
                return
            }
        }
        sidebar.setContent(title, lines, arena)
        if (newlyAdded) {
            sidebars.put(player.getUniqueId(), sidebar)
        }
    }

    /**
     * Kill a sidebar lifecycle.
     */
    open fun remove(sidebar: BwSidebar) {
        this.sidebars.remove(sidebar.getPlayer().getUniqueId())
        sidebar.remove()
    }

    override fun remove(player: Player) {
        val sidebar: BwSidebar? = this.sidebars.remove(player.getUniqueId())
        if (null != sidebar) {
            sidebar.remove()
        }
    }

    open fun getSidebarHandler(): SidebarManager {
        return sidebarHandler
    }

    override fun refreshTitles() {
        this.sidebars.forEach({ k, v -> v.getHandle()!!.refreshTitle() })
    }

    override fun refreshPlaceholders() {
        this.sidebars.forEach({ k, v -> v.getHandle()!!.refreshPlaceholders() })
    }

    override fun refreshPlaceholders(arena: IArena) {
        this.sidebars.forEach({ k, v ->
            if (v.getArena() != null) {
                if (v.getArena() == arena) {
                    v.getHandle()!!.refreshPlaceholders()
                }
            }
        })
    }

    override fun refreshTabList() {
        this.sidebars.forEach({ k, v -> v.getHandle()!!.playerTabRefreshAnimation() })
    }

    open fun refreshTabHeaderFooter() {
        this.sidebars.forEach({ k, v ->
            if (null != v && null != v.getHeaderFooter()) {
                this.sidebarHandler.sendHeaderFooter(v.getPlayer(), v.getHeaderFooter().unchecked())
            }
        })
    }

    override fun refreshHealth() {
        this.sidebars.forEach({ k, v ->
            if (null != v.getArena()) {
                v.getHandle()!!.playerHealthRefreshAnimation()
                for (player in v.getArena()!!.getPlayers()!!) {
                    v.getHandle()!!.setPlayerHealth(player, Math.ceil(player.getHealth()).toInt())
                }
            }
        })
    }

    override fun getSidebar(player: Player): ISidebar? {
        return this.sidebars.getOrDefault(player.getUniqueId(), null)
    }

    open fun refreshHealth(arena: IArena, player: Player, health: Int) {
        this.sidebars.forEach({ k, v ->
            if (null != v.getArena() && v.getArena() == arena) {
                v.getHandle()!!.setPlayerHealth(player, health)
            }
        })
    }

    open fun handleReJoin(arena: IArena, player: Player) {
        this.sidebars.forEach({ k, v ->
            if (null != v.getArena() && v.getArena() == arena) {
                v.giveUpdateTabFormat(player, false)
            }
        })
    }

    open fun handleJoin(arena: IArena, player: Player, spectator: Boolean?) {
        this.sidebars.forEach({ k, v ->
            if (null != v.getArena() && v.getArena() == arena) {
                if (v.getPlayer() != player) {
                    v.giveUpdateTabFormat(player, false, spectator)
                }
            }
        })
    }

    open fun applyLobbyTab(player: Player) {
        this.sidebars.forEach({ k, v ->
            if (null == v.getArena()) {
                if (v.getPlayer() != player) {
                    v.giveUpdateTabFormat(player, false)
                }
            }
        })
    }

    open fun handleInvisibility(team: ITeam, player: Player, toggle: Boolean) {
        this.sidebars.forEach({ k, v ->
            if (null != v.getArena() && v.getArena() == team.getArena()) {
                v.handleInvisibilityPotion(player, toggle)
            }
        })
    }

    init {
        sidebarHandler = SidebarBootstrap.init().unchecked()
    }

    companion object {
        private var instance: SidebarService? = null

        @JvmStatic
        fun init(plugin: JavaPlugin): Boolean {
            if (null == instance) {
                instance = SidebarService()
                val log = Bukkit.getLogger()
                val playerListRefreshInterval: Int = config.getInt(ConfigPath.SB_CONFIG_SIDEBAR_LIST_REFRESH)
                if (playerListRefreshInterval < 1) {
                    Bukkit.getLogger().info("Scoreboard names list refresh is disabled. (It is set to " + playerListRefreshInterval + ").")
                } else {
                    if (playerListRefreshInterval < 20) {
                        log.warning("Scoreboard names list refresh interval is set to: " + playerListRefreshInterval)
                        log.warning("It is not recommended to use a value under 20 ticks.")
                        log.warning("If you expect performance issues please increase its timer.")
                    }
                    Bukkit.getScheduler().runTaskTimer(plugin, RefreshPlayerListTask(), 1L, playerListRefreshInterval.toLong())
                }
                MetricsManager.appendPie("sb_list_refresh_interval", { playerListRefreshInterval.toString() })
                val placeholdersRefreshInterval: Int = config.getInt(ConfigPath.SB_CONFIG_SIDEBAR_PLACEHOLDERS_REFRESH_INTERVAL)
                if (placeholdersRefreshInterval < 1) {
                    log.info("Scoreboard placeholders refresh is disabled. (It is set to " + placeholdersRefreshInterval + ").")
                } else {
                    if (placeholdersRefreshInterval < 20) {
                        log.warning("Scoreboard placeholders refresh interval is set to: " + placeholdersRefreshInterval)
                        log.warning("It is not recommended to use a value under 20 ticks.")
                        log.warning("If you expect performance issues please increase its timer.")
                    }
                    Bukkit.getScheduler().runTaskTimer(plugin, RefreshPlaceholdersTask(), 1L, placeholdersRefreshInterval.toLong())
                }
                MetricsManager.appendPie("sb_placeholder_refresh_interval", { placeholdersRefreshInterval.toString() })
                val titleRefreshInterval: Int = config.getInt(ConfigPath.SB_CONFIG_SIDEBAR_TITLE_REFRESH_INTERVAL)
                if (titleRefreshInterval < 1) {
                    log.info("Scoreboard title refresh is disabled. (It is set to " + titleRefreshInterval + ").")
                } else {
                    if (titleRefreshInterval < 4) {
                        log.warning("Scoreboard title refresh interval is set to: " + titleRefreshInterval)
                        log.warning("If you expect performance issues please increase its timer.")
                    }
                    Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, RefreshTitleTask(), 1L, titleRefreshInterval.toLong())
                }
                MetricsManager.appendPie("sb_title_refresh_interval", { titleRefreshInterval.toString() })
                val healthAnimationInterval: Int = config.getInt(ConfigPath.SB_CONFIG_SIDEBAR_HEALTH_REFRESH)
                if (healthAnimationInterval < 1) {
                    log.info("Scoreboard health animation refresh is disabled. (It is set to " + healthAnimationInterval + ").")
                } else {
                    if (healthAnimationInterval < 20) {
                        log.warning("Scoreboard health animation refresh interval is set to: " + healthAnimationInterval)
                        log.warning("It is not recommended to use a value under 20 ticks.")
                        log.warning("If you expect performance issues please increase its timer.")
                    }
                    Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, RefreshLifeTask(), 1L, healthAnimationInterval.toLong())
                }
                MetricsManager.appendPie("sb_health_refresh_interval", { healthAnimationInterval.toString() })
                val tabHeaderFooterRefreshInterval: Int = config.getInt(ConfigPath.SB_CONFIG_TAB_HEADER_FOOTER_REFRESH_INTERVAL)
                if (tabHeaderFooterRefreshInterval < 1 || !config.getBoolean(ConfigPath.SB_CONFIG_TAB_HEADER_FOOTER_ENABLE)) {
                    log.info("Scoreboard Tab header-footer refresh is disabled.")
                } else {
                    if (tabHeaderFooterRefreshInterval < 20) {
                        log.warning("Scoreboard tab header-footer refresh interval is set to: " + tabHeaderFooterRefreshInterval)
                        log.warning("It is not recommended to use a value under 20 ticks.")
                        log.warning("If you expect performance issues please increase its timer.")
                    }
                    Bukkit.getScheduler().runTaskTimer(plugin, RefreshTabHeaderFooterTask(), 1L, tabHeaderFooterRefreshInterval.toLong())
                }
                MetricsManager.appendPie("sb_header_footer_refresh_interval", { tabHeaderFooterRefreshInterval.toString() })
                val lobbySidebar = config.getBoolean(ConfigPath.SB_CONFIG_SIDEBAR_USE_LOBBY_SIDEBAR) && BedWars.getServerType() == ServerType.MULTIARENA
                MetricsManager.appendPie("sb_lobby_enable", { lobbySidebar.toString() })
                val gameSidebar = config.getBoolean(ConfigPath.SB_CONFIG_SIDEBAR_USE_GAME_SIDEBAR)
                MetricsManager.appendPie("sb_game_enable", { gameSidebar.toString() })
                BedWars.registerEvents(ScoreboardListener())
            }
            return instance!!.sidebarHandler != null
        }

        @JvmStatic
        fun getInstance(): SidebarService? {
            return instance
        }
    }
}
