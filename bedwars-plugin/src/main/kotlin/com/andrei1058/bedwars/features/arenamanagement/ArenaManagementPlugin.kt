package com.andrei1058.bedwars.features.arenamanagement

import com.andrei1058.bedwars.features.arenamanagement.arena.ArenaOps
import com.andrei1058.bedwars.features.arenamanagement.arena.ArenaRepository
import com.andrei1058.bedwars.features.arenamanagement.arena.BulkRunner
import com.andrei1058.bedwars.features.arenamanagement.bedwars.BedWarsHook
import com.andrei1058.bedwars.features.arenamanagement.command.CommandInterceptor
import com.andrei1058.bedwars.features.arenamanagement.gui.ArenaBrowserMenu
import com.andrei1058.bedwars.features.arenamanagement.gui.ArenaSelectorMenu
import com.andrei1058.bedwars.features.arenamanagement.gui.MenuListener
import com.andrei1058.bedwars.features.arenamanagement.session.ChatPrompts
import com.andrei1058.bedwars.features.arenamanagement.session.SessionManager
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import com.andrei1058.bedwars.features.Feature
import com.andrei1058.bedwars.features.FeatureManager
import org.bukkit.scheduler.BukkitTask

class ArenaManagementPlugin : Feature("arenamanagement", "ArenaManagement", listOf("BedWars1058-ArenaManagement"), listOf("arenamanagement")) {
    lateinit var settings: Settings
        private set
    lateinit var hook: BedWarsHook
        private set
    lateinit var repository: ArenaRepository
        private set
    lateinit var sessions: SessionManager
        private set
    lateinit var prompts: ChatPrompts
        private set
    lateinit var runner: BulkRunner
        private set
    lateinit var ops: ArenaOps
        private set
    lateinit var interceptor: CommandInterceptor
        private set

    /**
     * BedWars1058-MapLoader, if installed. It lists "parked" arenas in BedWars1058 without a loaded world and
     * wakes them through command / join hooks, so everything here goes through those hooks (see ArenaOps).
     */
    val mapLoader: Feature? get() = FeatureManager.find<com.andrei1058.bedwars.features.maploader.WorldLoaderPlugin>()?.takeIf { FeatureManager.isActive(it) }

    /** BedWars1058-MultiGroup, if installed: it owns the variants of maps shared between groups. */
    val multiGroup: Feature? get() = FeatureManager.find<com.andrei1058.bedwars.features.multigroup.MultiGroupPlugin>()?.takeIf { FeatureManager.isActive(it) }

    /** `/bw gui` menus currently open; the refresh task keeps their player counts live. */
    val liveSelectors: MutableSet<ArenaSelectorMenu> = LinkedHashSet()

    private var refreshTask: BukkitTask? = null

    override fun onEnable() {
        saveDefaultConfig()
        settings = Settings(this)

        hook = BedWarsHook(this)
        if (!hook.connect()) {
            logger.severe("BedWars1058 is not installed - disabling.")
            disableSelf()
            return
        }

        repository = ArenaRepository(this)
        sessions = SessionManager()
        prompts = ChatPrompts(this)
        runner = BulkRunner(this)
        ops = ArenaOps(this)
        interceptor = CommandInterceptor(this)

        server.pluginManager.registerEvents(MenuListener(this), this.host)
        server.pluginManager.registerEvents(prompts, this.host)
        server.pluginManager.registerEvents(interceptor, this.host)

        startRefreshTask()
        logger.info("Enabled (server ${Bukkit.getBukkitVersion()}).")
    }

    override fun onDisable() {
        refreshTask?.cancel()
        // Close our menus so nobody is left holding a dead inventory after a reload.
        for (menu in liveSelectors.toList()) menu.player.closeInventory()
    }

    private fun startRefreshTask() {
        refreshTask?.cancel()
        refreshTask = server.scheduler.runTaskTimer(
            this.host,
            Runnable {
                for (menu in liveSelectors.toList()) {
                    if (!menu.player.isOnline) liveSelectors.remove(menu) else menu.live()
                }
            },
            settings.guiRefreshTicks,
            settings.guiRefreshTicks
        )
    }

    /** Runs [block] on the main thread after [ticks]. */
    fun later(ticks: Long, block: () -> Unit) {
        server.scheduler.runTaskLater(this.host, Runnable { block() }, ticks)
    }

    fun msg(target: CommandSender, text: String) {
        target.sendMessage(settings.prefix + com.andrei1058.bedwars.features.arenamanagement.util.Text.color(text))
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val sub = args.firstOrNull()?.lowercase()
        when (sub) {
            "reload" -> {
                if (!sender.hasPermission("arenamanagement.admin")) {
                    msg(sender, "&cYou do not have permission.")
                    return true
                }
                reloadConfig()
                repository.invalidate()
                startRefreshTask()
                msg(sender, "&aConfig reloaded.")
            }
            "menu", "gui" -> {
                if (sender !is Player) {
                    msg(sender, "&cOnly players can open menus.")
                    return true
                }
                if (sub == "menu") {
                    if (!sender.hasPermission(settings.permission)) {
                        msg(sender, "&cYou do not have permission.")
                        return true
                    }
                    ArenaBrowserMenu(this, sender).open()
                } else {
                    interceptor.openSelector(sender, args.getOrNull(1))
                }
            }
            else -> msg(sender, "&7Usage: &e/${label} <reload|menu|gui [group]>")
        }
        return true
    }
}
