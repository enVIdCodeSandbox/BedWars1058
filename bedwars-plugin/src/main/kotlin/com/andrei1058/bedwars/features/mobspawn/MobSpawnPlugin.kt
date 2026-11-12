package com.andrei1058.bedwars.features.mobspawn

import com.andrei1058.bedwars.features.mobspawn.bedwars.BedWarsHook
import com.andrei1058.bedwars.features.mobspawn.command.MobSpawnCommand
import com.andrei1058.bedwars.features.mobspawn.compat.Version
import com.andrei1058.bedwars.features.mobspawn.listener.GuiListener
import com.andrei1058.bedwars.features.mobspawn.listener.MobListener
import com.andrei1058.bedwars.features.mobspawn.mob.MobService
import org.bukkit.GameMode
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import com.andrei1058.bedwars.features.Feature

class MobSpawnPlugin : Feature("mobspawn", "MobSpawn", listOf("BedWars1058-MobSpawn"), listOf("mobspawn")) {

    lateinit var cfg: Settings
        private set
    lateinit var bedWars: BedWarsHook
        private set
    lateinit var service: MobService
        private set

    override fun onEnable() {
        saveDefaultConfig()
        cfg = Settings(this)
        bedWars = BedWarsHook(this)
        service = MobService(this)

        server.pluginManager.registerEvents(GuiListener(), this.host)
        server.pluginManager.registerEvents(MobListener(this), this.host)

        val command = MobSpawnCommand(this)
        getCommand("mobspawn")?.let {
            it.executor = command
            it.tabCompleter = command
        }

        service.start()

        // If a listener fails to register (e.g. a class missing on this server version) Bukkit only logs it
        // and carries on, so verify our handlers are really attached.
        val ours = org.bukkit.event.entity.EntityDamageByEntityEvent.getHandlerList().registeredListeners
            .count { it.plugin === host && it.listener.javaClass.name.startsWith("com.andrei1058.bedwars.features.mobspawn") }
        logger.info("Registered $ours handler(s) for EntityDamageByEntityEvent (expected 1).")
        if (ours == 0) logger.severe("Mob listeners are NOT registered - punch/edit/map-damage features cannot work. See the error above.")

        logger.info("Enabled on server version ${Version.raw}; attribute API: ${Version.hasAttributeApi}; " +
            "BedWars1058 hook: ${bedWars.available}")
    }

    override fun onDisable() {
        service.stop()
    }

    fun reload() {
        reloadConfig()
        service.start() // re-reads the target scan interval
    }

    // ---- permissions --------------------------------------------------------------------------

    /** Spawn / edit / delete: permission, or creative mode when enabled in the config. */
    fun canUse(p: Player): Boolean =
        p.hasPermission("mobspawn.use") || (cfg.allowCreative && p.gameMode == GameMode.CREATIVE)

    /** Attribute editing: admin permission (optionally also creative). */
    fun canEditAttributes(p: Player): Boolean =
        p.hasPermission("mobspawn.admin") || (cfg.creativeAttributes && p.gameMode == GameMode.CREATIVE)

    fun isAdmin(sender: CommandSender): Boolean = sender.hasPermission("mobspawn.admin")

    fun msg(to: CommandSender, text: String) {
        to.sendMessage("§8[§bMobSpawn§8] §7$text")
    }
}
