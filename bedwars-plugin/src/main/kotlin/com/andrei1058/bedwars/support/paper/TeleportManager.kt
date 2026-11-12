package com.andrei1058.bedwars.support.paper

import com.andrei1058.bedwars.BedWars.Companion.config
import com.andrei1058.bedwars.BedWars.Companion.isPaper
import com.andrei1058.bedwars.api.configuration.ConfigPath
import io.papermc.lib.PaperLib
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.event.player.PlayerTeleportEvent

object TeleportManager {
    @JvmStatic
    fun teleport(entity: Entity, location: Location) {
        teleportC(entity, location, PlayerTeleportEvent.TeleportCause.PLUGIN)
    }

    @JvmStatic
    fun teleportC(entity: Entity, location: Location, cause: PlayerTeleportEvent.TeleportCause) {
        if (isPaper && config.getBoolean(ConfigPath.GENERAL_CONFIGURATION_PERFORMANCE_PAPER_FEATURES)) {
            PaperLib.teleportAsync(entity, location, cause)
            return
        }
        entity.teleport(location, cause)
    }
}
