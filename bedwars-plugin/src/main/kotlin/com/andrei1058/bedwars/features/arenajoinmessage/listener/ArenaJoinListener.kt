package com.andrei1058.bedwars.features.arenajoinmessage.listener

import com.andrei1058.bedwars.api.events.player.PlayerJoinArenaEvent
import com.andrei1058.bedwars.features.arenajoinmessage.ArenaJoinMessagePlugin
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

class ArenaJoinListener(private val plugin: ArenaJoinMessagePlugin) : Listener {

    // MONITOR: runs after every other plugin had the chance to cancel the join.
    @EventHandler(priority = EventPriority.MONITOR)
    fun onArenaJoin(event: PlayerJoinArenaEvent) {
        // PlayerJoinArenaEvent has isCancelled() but does not implement Cancellable, so Bukkit's
        // ignoreCancelled flag does nothing for it. Check by hand.
        if (event.isCancelled()) return
        if (event.isSpectator()) return // spectators cannot be joined by clicking, so nothing to announce
        plugin.notifier.queue(event.getArena() ?: return, event.getPlayer() ?: return)
    }
}
