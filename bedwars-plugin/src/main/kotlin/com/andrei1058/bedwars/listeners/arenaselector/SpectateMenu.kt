package com.andrei1058.bedwars.listeners.arenaselector

import com.andrei1058.bedwars.BedWars
import com.andrei1058.bedwars.api.arena.GameState
import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.api.configuration.ConfigPath
import com.andrei1058.bedwars.api.language.Language
import com.andrei1058.bedwars.arena.Arena
import com.andrei1058.bedwars.configuration.Sounds
import java.time.Duration
import java.time.Instant
import java.util.ArrayList
import java.util.Arrays
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

/**
 * Lobby menu used to pick which running game of an arena to spectate.
 * Every running instance of every arena has its own world, so there can be
 * several games to choose from for the same map.
 */
object SpectateMenu {
    const val DATA_PREFIX: String = "spectate="
    const val MSG_TITLE: String = "spectate-menu-name"
    const val MSG_ITEM_NAME: String = "spectate-menu-item-name"
    const val MSG_ITEM_LORE: String = "spectate-menu-item-lore"
    const val MSG_NONE: String = "spectate-menu-none"
    const val MSG_DENIED: String = "spectate-menu-denied"
    const val MSG_ARENA_UNKNOWN: String = "spectate-menu-unknown-arena"

    /**
     * Adds the default texts to every language file that doesn't have them yet.
     */
    @JvmStatic
    fun registerMessages() {
        Language.saveIfNotExists(MSG_TITLE, "&8Spectate a game")
        Language.saveIfNotExists(MSG_ITEM_NAME, "&a{arena} &7#{number}")
        Language.saveIfNotExists(MSG_ITEM_LORE, Arrays.asList("&7Players: &f{on}&7/&f{max}", "&7Teams alive: &f{teams}", "&7Running for: &f{time}", "", "&eClick to spectate!"))
        Language.saveIfNotExists(MSG_NONE, "&c▪ &7There are no games to spectate right now.")
        Language.saveIfNotExists(MSG_DENIED, "&c▪ &7You can't spectate that game.")
        Language.saveIfNotExists(MSG_ARENA_UNKNOWN, "&c▪ &7There is no arena called &f{name}&7.")
    }

    /**
     * Running games that can be spectated.
     *
     * @param arenaName only games of this arena, or null for every arena.
     */
    @JvmStatic
    fun spectatable(arenaName: String?): MutableList<IArena> {
        val list: MutableList<IArena> = ArrayList()
        for (a in Arena.getArenas()) {
            if (a.getStatus() != GameState.playing) {
                continue
            }
            if (!a.isAllowSpectate()) {
                continue
            }
            if (arenaName != null && !a.getArenaName().equals(arenaName, ignoreCase = true)) {
                continue
            }
            list.add(a)
        }
        return list
    }

    /**
     * Opens the menu.
     *
     * @param arenaName limit to one arena, or null for every arena.
     * @return false when there is nothing to spectate.
     */
    @JvmStatic
    fun open(p: Player, arenaName: String?): Boolean {
        val games = spectatable(arenaName)
        if (games.isEmpty()) {
            p.sendMessage(Language.getMsg(p, MSG_NONE))
            Sounds.playSound("spectate-denied", p)
            return false
        }
        // a single game needs no menu
        if (games.size() == 1 && arenaName != null) {
            if (games.get(0).addSpectator(p, false, null)) {
                Sounds.playSound("spectate-allowed", p)
            } else {
                p.sendMessage(Language.getMsg(p, MSG_DENIED))
                Sounds.playSound("spectate-denied", p)
            }
            return true
        }
        val size: Int = Math.min(54, Math.max(9, ((games.size() + 8) / 9) * 9))
        val inv = Bukkit.createInventory(Holder(arenaName), size, Language.getMsg(p, MSG_TITLE))
        val material = BedWars.config.getString(ConfigPath.GENERAL_CONFIGURATION_ARENA_SELECTOR_STATUS_MATERIAL.replace("%path%", "playing"))
        val data: Short = BedWars.config.getInt(ConfigPath.GENERAL_CONFIGURATION_ARENA_SELECTOR_STATUS_DATA.replace("%path%", "playing")).toShort()
        // number games per arena so players can tell them apart
        val counters: MutableMap<String, Int> = java.util.HashMap()
        var slot: Int = 0
        while (slot < games.size() && slot < size) {
            val g = games.get(slot)
            val number: Int = counters.merge(g.getArenaName(), 1, Int::sum)
            var item = BedWars.nms.createItemStack(material, 1, data)
            val im = item.getItemMeta()
            im.setDisplayName(fill(Language.getMsg(p, MSG_ITEM_NAME), g, number))
            val lore: MutableList<String> = ArrayList()
            for (line in Language.getList(p, MSG_ITEM_LORE)) {
                lore.add(fill(line, g, number))
            }
            im.setLore(lore)
            item.setItemMeta(im)
            item = BedWars.nms.addCustomData(item, DATA_PREFIX + g.getWorldName())
            inv.setItem(slot, item)
            slot++
        }
        p.openInventory(inv)
        Sounds.playSound("arena-selector-open", p)
        return true
    }

    @JvmStatic
    private fun fill(s: String, g: IArena, number: Int): String {
        var alive: Int = 0
        for (t in g.getTeams()) {
            if (!t.getMembers().isEmpty()) {
                alive++
            }
        }
        return s.replace("{arena}", g.getDisplayName()).replace("{map_name}", g.getArenaName()).replace("{number}", number.toString()).replace("{world}", g.getWorldName()).replace("{on}", g.getPlayers().size().toString()).replace("{max}", g.getMaxPlayers().toString()).replace("{teams}", alive.toString()).replace("{time}", elapsed(g.getStartTime()))
    }

    @JvmStatic
    private fun elapsed(start: Instant?): String {
        if (start == null) {
            return "0:00"
        }
        val d = Duration.between(start, Instant.now())
        val secs: Long = Math.max(0, d.getSeconds())
        return (secs / 60).toString() + ":" + String.format("%02d", secs % 60)
    }

    class Holder(@JvmField val arenaName: String) : InventoryHolder {
        fun getArenaName(): String {
            return arenaName
        }

        override fun getInventory(): Inventory? {
            return null
        }
    }
}
