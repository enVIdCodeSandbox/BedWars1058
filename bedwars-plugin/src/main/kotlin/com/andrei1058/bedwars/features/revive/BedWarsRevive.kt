package com.andrei1058.bedwars.features.revive

import com.andrei1058.bedwars.api.BedWars
import com.andrei1058.bedwars.features.revive.bed.BedManager
import com.andrei1058.bedwars.features.Feature

class BedWarsRevive : Feature("revive", "Revive", listOf("BedWars1058-Revive"), listOf("revive")) {

    lateinit var api: BedWars
        private set
    lateinit var messages: Messages
        private set
    lateinit var beds: BedManager
        private set
    lateinit var service: ReviveService
        private set

    override fun onEnable() {
        val registration = server.servicesManager.getRegistration(BedWars::class.java)
        if (registration == null) {
            logger.severe("BedWars1058 API not found. Is BedWars1058 installed and enabled? Disabling.")
            disableSelf()
            return
        }
        api = registration.provider

        saveDefaultConfig()
        messages = Messages(this)

        beds = BedManager(this)
        server.pluginManager.registerEvents(beds, this.host)
        service = ReviveService(this)

        val command = ReviveCommand(this)
        getCommand("revive")?.apply {
            setExecutor(command)
            tabCompleter = command
        } ?: logger.severe("Command 'revive' is missing from plugin.yml")

        // Cover games that are already running (e.g. after /reload).
        server.scheduler.runTask(this.host, Runnable {
            for (arena in api.getArenaUtil().getArenas()) beds.captureArena(arena)
        })

        logger.info("Enabled. BedWars1058 server type: ${api.getServerType()}")
    }

    fun reload() {
        reloadConfig()
        messages.reload()
    }

    fun debug(message: String) {
        if (config.getBoolean("debug", false)) logger.info("[debug] $message")
    }
}
