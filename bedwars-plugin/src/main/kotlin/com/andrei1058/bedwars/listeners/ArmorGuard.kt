package com.andrei1058.bedwars.listeners

import com.andrei1058.bedwars.BedWars
import com.andrei1058.bedwars.arena.Arena
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

/**
 * Armor in a match comes from the shop (or the default leather set) and never moves: a player must not be able to
 * put a piece in another slot, swap it with the worn one or take it off. Clicking, shift-clicking, number keys,
 * dragging and right-click equipping are all refused, and the client gets its inventory back afterwards because
 * a cancelled click can leave a 1.8 client (through ViaVersion) showing the item where it was dropped.
 */
open class ArmorGuard : Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    open fun onClick(e: InventoryClickEvent) {
        if (e.getWhoClicked() !is Player) {
            return
        }
        val player = e.getWhoClicked() as Player
        if (Arena.getArenaByPlayer(player) == null) {
            return
        }
        val armorSlot: Boolean = e.getSlotType() == InventoryType.SlotType.ARMOR
        // shift-click sends an armor piece to its slot, and a double click gathers it from there
        val armorMove: Boolean = (e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY || e.getAction() == InventoryAction.COLLECT_TO_CURSOR) && (isArmor(e.getCurrentItem()) || isArmor(e.getCursor()))
        if (!armorSlot && !armorMove) {
            return
        }
        e.setCancelled(true)
        resync(player)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    open fun onDrag(e: InventoryDragEvent) {
        if (e.getWhoClicked() !is Player) {
            return
        }
        val player = e.getWhoClicked() as Player
        if (Arena.getArenaByPlayer(player) == null) {
            return
        }
        if (e.getView().getTopInventory().getType() != InventoryType.CRAFTING) {
            return
        }
        for (raw in e.getRawSlots()) {
            if (raw >= FIRST_ARMOR_RAW_SLOT && raw <= LAST_ARMOR_RAW_SLOT) {
                e.setCancelled(true)
                resync(player)
                return
            }
        }
    }

    /** Newer servers equip (and swap) armor on right click. */
    @EventHandler(priority = EventPriority.LOWEST)
    open fun onInteract(e: PlayerInteractEvent) {
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return
        }
        val player = e.getPlayer()
        if (Arena.getArenaByPlayer(player) == null) {
            return
        }
        if (!isArmor(e.getItem())) {
            return
        }
        e.setUseItemInHand(Event.Result.DENY)
        resync(player)
    }

    companion object {
        /** Raw slots 5 to 8 of the player's own inventory view are helmet, chestplate, leggings and boots. */
        private const val FIRST_ARMOR_RAW_SLOT: Int = 5
        private const val LAST_ARMOR_RAW_SLOT: Int = 8

        @JvmStatic
        private fun isArmor(item: ItemStack?): Boolean {
            return item != null && item.getType() != org.bukkit.Material.AIR && BedWars.nms.isArmor(item)
        }

        @JvmStatic
        private fun resync(player: Player) {
            Bukkit.getScheduler().runTask(BedWars.plugin, player::updateInventory)
        }
    }
}
