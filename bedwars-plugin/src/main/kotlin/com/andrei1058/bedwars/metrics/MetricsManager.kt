package com.andrei1058.bedwars.metrics

import com.andrei1058.bedwars.BedWars
import com.andrei1058.bedwars.api.language.Language
import com.andrei1058.bedwars.support.citizens.JoinNPC
import com.andrei1058.bedwars.teamselector.configuration.Config
import java.util.concurrent.Callable
import org.bstats.bukkit.Metrics
import org.bstats.charts.SimplePie

open class MetricsManager private constructor(plugin: BedWars) {
    private val metrics: Metrics

    open fun getMetrics(): Metrics {
        return metrics
    }

    init {
        metrics = Metrics(plugin, 1885)
        // base metrics
        metrics.addCustomChart(SimplePie("server_type", { BedWars.getServerType().toString() }))
        metrics.addCustomChart(SimplePie("default_language", { Language.getDefaultLanguage().getIso() }))
        metrics.addCustomChart(SimplePie("auto_scale", { BedWars.autoscale.toString() }))
        metrics.addCustomChart(SimplePie("party_adapter", { BedWars.getParty().javaClass.getName() }))
        metrics.addCustomChart(SimplePie("chat_adapter", { BedWars.getChatSupport().javaClass.getName() }))
        metrics.addCustomChart(SimplePie("level_adapter", { BedWars.getLevelSupport().javaClass.getName() }))
        metrics.addCustomChart(SimplePie("db_adapter", { BedWars.getRemoteDatabase().javaClass.getName() }))
        metrics.addCustomChart(SimplePie("map_adapter", { BedWars.getAPI().getRestoreAdapter().javaClass.getName() }))
        metrics.addCustomChart(SimplePie("citizens_support", { JoinNPC.isCitizensSupport().toString() }))
        // team selector
        metrics.addCustomChart(SimplePie("selector_slot", { if (Config.isLoaded()) Config.config.getInt(Config.SELECTOR_SLOT).toString() else "disabled" }))
        metrics.addCustomChart(SimplePie("allot_team_change", { if (Config.isLoaded()) Config.config.getBoolean(Config.ALLOW_TEAM_CHANGE).toString() else "disabled" }))
        metrics.addCustomChart(SimplePie("balance_teams", { if (Config.isLoaded()) Config.config.getBoolean(Config.BALANCE_TEAMS).toString() else "disabled" }))
    }

    companion object {
        private var instance: MetricsManager? = null

        @JvmStatic
        fun appendPie(id: String, callable: Callable<String>) {
            if (null == instance) {
                throw RuntimeException("Metrics manager is not initialized!")
            }
            instance!!.getMetrics().addCustomChart(SimplePie(id, callable))
        }

        @JvmStatic
        fun initService(plugin: BedWars) {
            if (null != instance) {
                return
            }
            instance = MetricsManager(plugin)
        }
    }
}
