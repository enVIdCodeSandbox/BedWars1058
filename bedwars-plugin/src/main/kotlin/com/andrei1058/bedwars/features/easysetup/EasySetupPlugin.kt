package com.andrei1058.bedwars.features.easysetup

import com.andrei1058.bedwars.features.easysetup.bw.BwCommands
import com.andrei1058.bedwars.features.easysetup.bw.CommandSniffer
import com.andrei1058.bedwars.features.easysetup.cmd.AutoCommand
import com.andrei1058.bedwars.features.easysetup.cmd.NextGenCommand
import com.andrei1058.bedwars.features.easysetup.cmd.SetupCommand
import com.andrei1058.bedwars.features.easysetup.gui.MenuListener
import com.andrei1058.bedwars.features.easysetup.model.SessionManager
import com.andrei1058.bedwars.features.easysetup.tools.Ghosts
import com.andrei1058.bedwars.features.easysetup.tools.Hotbar
import com.andrei1058.bedwars.features.easysetup.tools.ToolListener
import org.bukkit.Bukkit
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Entity
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.plugin.EventExecutor
import com.andrei1058.bedwars.features.Feature

/** Entry point: wires settings, sessions, listeners and commands. */
class EasySetupPlugin : Feature("easysetup", "EasySetup", listOf("BedWars1058-EasySetup"), listOf("bwsetup", "bwauto", "nextgen")) {
    private lateinit var settingsRef: Settings
    private lateinit var sessionsRef: SessionManager
    private lateinit var bwRef: BwCommands
    private lateinit var ghostsRef: Ghosts

    override fun onEnable() {
        saveDefaultConfig()
        settingsRef = Settings(this)
        sessionsRef = SessionManager()
        bwRef = BwCommands(this)
        ghostsRef = Ghosts(this)

        server.pluginManager.registerEvents(MenuListener(), this.host)
        server.pluginManager.registerEvents(ToolListener(this), this.host)
        registerEntitiesLoad()
        registerSwapGuard()
        Bukkit.getScheduler().runTask(this.host, Runnable { ghostsRef.start() })
        server.pluginManager.registerEvents(CommandSniffer(this), this.host)

        bind("bwsetup", SetupCommand(this))
        bind("bwauto", AutoCommand(this))
        bind("nextgen", NextGenCommand(this))
        logger.info("BedWars1058-EasySetup enabled.")
    }

    override fun onDisable() {
        if (::sessionsRef.isInitialized) {
            for (s in sessionsRef.all()) {
                if (!s.toolsActive) continue
                val p = Bukkit.getPlayer(s.owner)
                if (p != null) Hotbar.deactivate(this, p, s)
            }
        }
        if (::ghostsRef.isInitialized) ghostsRef.stop()
    }

    /** 1.17+ loads entities separately from chunks; hook that event by name so older servers are unaffected. */
    @Suppress("UNCHECKED_CAST")
    private fun registerEntitiesLoad() {
        try {
            val c = Class.forName("org.bukkit.event.world.EntitiesLoadEvent") as Class<out Event>
            val get = c.getMethod("getEntities")
            server.pluginManager.registerEvent(c, object : Listener {}, EventPriority.NORMAL, EventExecutor { _, event ->
                try {
                    ghostsRef.sweepEntities(get.invoke(event) as Collection<Entity>)
                } catch (ignored: Throwable) {
                    // best effort
                }
            }, this.host)
        } catch (ignored: Throwable) {
            // not available on this version
        }
    }

    /** 1.9+: the swap-hands key would move a tool into the off hand; cancel it by event name so 1.8 is unaffected. */
    @Suppress("UNCHECKED_CAST")
    private fun registerSwapGuard() {
        try {
            val c = Class.forName("org.bukkit.event.player.PlayerSwapHandItemsEvent") as Class<out Event>
            val main = c.getMethod("getMainHandItem")
            val off = c.getMethod("getOffHandItem")
            val player = c.getMethod("getPlayer")
            server.pluginManager.registerEvent(c, object : Listener {}, EventPriority.HIGH, EventExecutor { _, event ->
                try {
                    val p = player.invoke(event) as org.bukkit.entity.Player
                    val s = sessionsRef.peek(p)
                    if (s != null && s.toolsActive &&
                        (Hotbar.isTool(main.invoke(event) as org.bukkit.inventory.ItemStack?) ||
                            Hotbar.isTool(off.invoke(event) as org.bukkit.inventory.ItemStack?))
                    ) {
                        (event as org.bukkit.event.Cancellable).isCancelled = true
                    }
                } catch (ignored: Throwable) {
                    // best effort
                }
            }, this.host)
        } catch (ignored: Throwable) {
            // not available on this version
        }
    }

    private fun bind(name: String, executor: TabExecutor) {
        val cmd = getCommand(name)
        if (cmd == null) {
            logger.severe("Command missing from plugin.yml: $name")
            return
        }
        cmd.executor = executor
        cmd.tabCompleter = executor
    }

    fun settings(): Settings = settingsRef

    fun sessions(): SessionManager = sessionsRef

    fun ghosts(): Ghosts = ghostsRef

    fun bw(): BwCommands = bwRef
}
