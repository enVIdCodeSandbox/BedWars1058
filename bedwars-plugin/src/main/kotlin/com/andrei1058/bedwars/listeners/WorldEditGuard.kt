package com.andrei1058.bedwars.listeners

import com.andrei1058.bedwars.BedWars
import com.andrei1058.bedwars.arena.Arena
import java.util.Locale
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

/**
 * Keeps WorldEdit (and FastAsyncWorldEdit) out of matches: while a player is in an arena and is not in creative
 * mode, the WorldEdit wand and navigation wand do nothing and WorldEdit commands are refused.
 * Creative players are not restricted. Switch off with {@code world-edit.block-in-game: false}.
 */
open class WorldEditGuard : Listener {
    /** WorldEdit skips an interaction whose item use was already denied, so the wand never gets to select anything. */
    @EventHandler(priority = EventPriority.LOWEST)
    open fun onInteract(e: PlayerInteractEvent) {
        if (!enabled() || !hasWorldEdit()) {
            return
        }
        val p = e.getPlayer()
        if (!restricted(p)) {
            return
        }
        if (!isTool(e.getItem())) {
            return
        }
        e.setUseItemInHand(Event.Result.DENY)
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    open fun onCommand(e: PlayerCommandPreprocessEvent) {
        if (!enabled() || !hasWorldEdit()) {
            return
        }
        val p = e.getPlayer()
        if (!restricted(p)) {
            return
        }
        val msg = e.getMessage().trim()
        val lower = msg.lowercase(Locale.ROOT)
        var label: String = if (lower.startsWith("/")) lower.substring(1) else lower
        val space: Int = label.indexOf(' ')
        if (space >= 0) {
            label = label.substring(0, space)
        }
        val worldEdit: Boolean = lower.startsWith("//") || label == "wand" || label == "we" || label == "worldedit" || label == "fawe" || label == "fastasyncworldedit" || label.startsWith("worldedit:") || label.startsWith("fastasyncworldedit:") || label.startsWith("fawe:")
        if (!worldEdit) {
            return
        }
        e.setCancelled(true)
        p.sendMessage(ChatColor.RED.toString() + "WorldEdit is disabled during a match.")
    }

    companion object {
        @JvmStatic
        private fun enabled(): Boolean {
            return BedWars.config.getYml().getBoolean("world-edit.block-in-game", true)
        }

        @JvmStatic
        private fun restricted(p: Player): Boolean {
            if (p.getGameMode() == GameMode.CREATIVE) {
                return false
            }
            return Arena.getArenaByPlayer(p) != null
        }

        @JvmStatic
        private fun hasWorldEdit(): Boolean {
            return Bukkit.getPluginManager().getPlugin("WorldEdit") != null || Bukkit.getPluginManager().getPlugin("FastAsyncWorldEdit") != null
        }

        /** Material of a WorldEdit tool item as configured in WorldEdit ("minecraft:wooden_axe"), or the default. */
        @JvmStatic
        private fun configured(field: String, fallback: String): Material {
            var id: String? = null
            try {
                val instance = Class.forName("com.sk89q.worldedit.WorldEdit").getMethod("getInstance").invoke(null)
                val config = instance.javaClass.getMethod("getConfiguration").invoke(instance)
                id = config.javaClass.getField(field).get(config) as String
            } catch (ignored: Throwable) {
                // WorldEdit not (fully) there: use the defaults
            }
            if (id != null) {
                var name = id.lowercase(Locale.ROOT)
                if (name.startsWith("minecraft:")) {
                    name = name.substring("minecraft:".length)
                }
                val m: Material? = Material.matchMaterial(name)
                if (m != null) {
                    return m
                }
            }
            return Material.matchMaterial(fallback)
        }

        @JvmStatic
        private fun isTool(item: ItemStack?): Boolean {
            if (item == null) {
                return false
            }
            val type = item.getType()
            if (type == Material.AIR) {
                return false
            }
            var wand: Material? = configured("wandItem", "WOODEN_AXE")
            if (wand == null) {
                wand = Material.matchMaterial("WOOD_AXE")
            }
            val nav = configured("navigationWand", "COMPASS")
            return type == wand || type == nav
        }
    }
}
