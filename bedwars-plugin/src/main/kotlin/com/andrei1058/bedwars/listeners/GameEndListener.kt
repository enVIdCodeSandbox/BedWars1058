package com.andrei1058.bedwars.listeners

import com.andrei1058.bedwars.api.events.gameplay.GameEndEvent
import java.util.UUID
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.entity.Entity
import org.bukkit.entity.Item
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack

open class GameEndListener : Listener {
    @EventHandler
    open fun cleanInventoriesAndDroppedItems(event: GameEndEvent) {
        if (event.getArena().getPlayers()!!.isEmpty()) {
            return
        }
        // clear inventories
        for (p in event.getAliveWinners()) {
            Bukkit.getPlayer(p).getInventory().clear()
        }
        // clear dropped items
        val game = event.getArena().getWorld()
        for (item in game!!.getEntities()) {
            if ((item is Item) || (item is ItemStack)) {
                item.remove()
            }
        }
    }
}
