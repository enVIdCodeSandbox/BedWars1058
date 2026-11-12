package com.andrei1058.bedwars.features.generalfixes.winfix

import com.andrei1058.bedwars.api.BedWars
import com.andrei1058.bedwars.api.arena.GameState
import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.api.events.gameplay.GameEndEvent
import com.andrei1058.bedwars.api.events.gameplay.GameStateChangeEvent
import com.andrei1058.bedwars.api.events.player.PlayerJoinArenaEvent
import com.andrei1058.bedwars.api.events.player.PlayerKillEvent
import com.andrei1058.bedwars.api.events.player.PlayerLeaveArenaEvent
import com.andrei1058.bedwars.features.generalfixes.common.Fix
import com.andrei1058.bedwars.features.generalfixes.common.FixPlugin
import org.bukkit.Bukkit
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.scheduler.BukkitTask

/**
 * Fix 7: the end-of-game sidebar shows raw {winnerTeamName} / {topPlayerDisplayName} / {topValue} text.
 *
 * After a game ends the sidebar of every player is rebuilt with the real winner and the real "top" list
 * (from BedWars1058's statistics, or this fix's own kill counter if they are unavailable).
 * Integrated from the standalone BedWars1058-WinFix addon.
 */
class WinFix(plugin: FixPlugin, section: String) : Fix(plugin, section) {

    @Volatile
    private var settings = WinSettings.from(plugin.cfg, prefix)

    private val tracker = KillTracker()
    private val topStats = TopStatsSource(tracker)

    /** One pending rebuild per arena; scheduling again replaces the previous one. */
    private val pending = HashMap<String, BukkitTask>()

    override fun load(cfg: FileConfiguration) {
        settings = WinSettings.from(cfg, prefix)
        if (!settings.enabled) cancelAll()
    }

    override fun onStop() = cancelAll()

    private fun cancelAll() {
        pending.values.forEach { it.cancel() }
        pending.clear()
    }

    private fun renderer(): SidebarRenderer? {
        val api = Bukkit.getServicesManager().getRegistration(BedWars::class.java)?.provider ?: return null
        return SidebarRenderer(api, topStats) { settings }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onKill(event: PlayerKillEvent) {
        if (!settings.enabled) return
        val killer = event.getKiller() ?: return
        tracker.addKill(event.getArena(), killer)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onStateChange(event: GameStateChangeEvent) {
        if (!settings.enabled) return
        val arena = event.getArena()
        when (event.getNewState()) {
            // New match starting: forget the previous match's kills.
            GameState.waiting, GameState.starting -> {
                pending.remove(arena.getArenaName())?.cancel()
                tracker.reset(arena)
            }
            GameState.playing -> if (event.getOldState() == GameState.starting) tracker.reset(arena)
            GameState.restarting -> scheduleRefresh(arena)
            else -> Unit
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onGameEnd(event: GameEndEvent) {
        if (settings.enabled) scheduleRefresh(event.getArena())
    }

    // BedWars1058 rebuilds a player's sidebar on these events, which brings the raw placeholders back.
    @EventHandler(priority = EventPriority.MONITOR)
    fun onJoin(event: PlayerJoinArenaEvent) {
        if (settings.enabled && event.getArena().getStatus() == GameState.restarting) scheduleRefresh(event.getArena())
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onLeave(event: PlayerLeaveArenaEvent) {
        if (settings.enabled && event.getArena().getStatus() == GameState.restarting) scheduleRefresh(event.getArena())
    }

    private fun scheduleRefresh(arena: IArena) {
        val key = arena.getArenaName()
        pending.remove(key)?.cancel()
        pending[key] = Bukkit.getScheduler().runTaskLater(plugin.host, Runnable {
            pending.remove(key)
            try {
                val rebuilt = renderer()?.render(arena) ?: 0
                plugin.debug("Rebuilt $rebuilt end-game sidebar(s) for arena '$key'.")
            } catch (t: Throwable) {
                plugin.logger.warning("Could not rebuild the end-game sidebar for '$key': $t")
                if (plugin.debugEnabled) t.printStackTrace()
            }
        }, settings.refreshDelayTicks)
    }
}
