package com.andrei1058.bedwars.listeners

import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.arena.Arena
import com.andrei1058.bedwars.configuration.Sounds
import org.bukkit.entity.EnderPearl
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.ProjectileHitEvent
import com.andrei1058.bedwars.api.util.unchecked

open class EnderPearlLanded : Listener {
    @EventHandler
    open fun onPearlHit(e: ProjectileHitEvent) {
        if (e.getEntity() !is EnderPearl) {
            return
        }
        if (e.getEntity().getShooter() !is Player) {
            return
        }
        val player = e.getEntity().getShooter() as Player
        val iArena = Arena.getArenaByPlayer(player)
        if (!Arena.isInArena(player) || iArena.isSpectator(player)) {
            return
        }
        Sounds.playSound("ender-pearl-landed", iArena.getPlayers().unchecked())
    }
}
