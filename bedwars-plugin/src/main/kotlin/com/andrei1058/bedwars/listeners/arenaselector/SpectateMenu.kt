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
    fun spectatable(arenaName: String): MutableList<IArena> {
        return spectatable(arenaName, null)
    }

    /**
     * Running games that can be spectated by a player, who may still be in another game (their own ended, or they
     * are spectating one). That game itself is left out.
     */
    @JvmStatic
    fun spectatable(arenaName: String, viewer: Player?): MutableList<IArena> {
        return spectatable(arenaName, null, viewer)
    }

    /** Like {@link #spectatable(String, Player)}, limited to the arena groups of [group] ("default" or null: all). */
    @JvmStatic
    fun spectatable(arenaName: String?, group: String?, viewer: Player?): MutableList<IArena> {
        val current: IArena? = if (viewer == null) null else Arena.getArenaByPlayer(viewer)
        val list: MutableList<IArena> = ArrayList()
        for (a in Arena.getArenas()) {
            if (a === current) {
                continue
            }
            if (!com.andrei1058.bedwars.arena.ArenaGUI.groupMatches(group, a.getGroup())) {
                continue
            }
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
    fun open(p: Player, arenaName: String): Boolean {
        return open(p, arenaName, null)
    }

    /**
     * Opens the menu with every running game of an arena, within the given arena groups.
     *
     * @param group "default" or null for every group.
     */
    @JvmStatic
    fun open(p: Player, arenaName: String?, group: String?): Boolean {
        if (!canLeaveForSpectating(p)) {
            p.sendMessage(Language.getMsg(p, MSG_DENIED))
            Sounds.playSound("spectate-denied", p)
            return false
        }
        val games = spectatable(arenaName, group, p)
        if (games.isEmpty()) {
            p.sendMessage(Language.getMsg(p, MSG_NONE))
            Sounds.playSound("spectate-denied", p)
            return false
        }
        // a single game needs no menu
        if (games.size == 1 && arenaName != null) {
            if (spectate(p, games.get(0))) {
                Sounds.playSound("spectate-allowed", p)
            } else {
                p.sendMessage(Language.getMsg(p, MSG_DENIED))
                Sounds.playSound("spectate-denied", p)
            }
            return true
        }
        val size: Int = Math.min(54, Math.max(9, ((games.size + 8) / 9) * 9))
        val inv = Bukkit.createInventory(Holder(arenaName!!, group!!), size, Language.getMsg(p, MSG_TITLE))
        val material = BedWars.config.getString(ConfigPath.GENERAL_CONFIGURATION_ARENA_SELECTOR_STATUS_MATERIAL.replace("%path%", "playing"))
        val data: Short = BedWars.config.getInt(ConfigPath.GENERAL_CONFIGURATION_ARENA_SELECTOR_STATUS_DATA.replace("%path%", "playing")).toShort()
        // number games per arena so players can tell them apart
        val counters: MutableMap<String, Int> = java.util.HashMap()
        var slot: Int = 0
        while (slot < games.size && slot < size) {
            val g = games.get(slot)
            val number: Int = counters.merge(g.getArenaName(), 1, Int::plus)!!
            var item = BedWars.nms.createItemStack(material, 1, data)
            val im = item.getItemMeta()
            im.setDisplayName(fill(Language.getMsg(p, MSG_ITEM_NAME), g, number))
            val lore: MutableList<String> = ArrayList()
            var listsPlayers: Boolean = false
            for (line in Language.getList(p, MSG_ITEM_LORE)) {
                if (line.contains("{players}")) {
                    lore.addAll(playerLines(g))
                    listsPlayers = true
                } else {
                    lore.add(fill(line, g, number))
                }
            }
            if (!listsPlayers) {
                // language files saved by older versions have no {players} line: put the list before the last paragraph
                val at: Int = lore.lastIndexOf("")
                lore.addAll(if (at < 0) lore.size else at, playerLines(g))
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

    /**
     * Whether the player may switch to spectating another game: not in a game, spectating one, or in one that
     * has ended. Somebody still fighting in a running game has to leave it first.
     */
    @JvmStatic
    fun canLeaveForSpectating(p: Player): Boolean {
        val current: IArena? = Arena.getArenaByPlayer(p)
        if (current == null || current.isSpectator(p)) {
            return true
        }
        return current.getStatus() == GameState.restarting
    }

    /**
     * Spectates a game, leaving the one the player is still in (a spectator of another game, or a player whose
     * game ended) first, so several games can run at once and a player can watch another one when theirs is over.
     *
     * @return false when the game refused the spectator; the player stays where they were in that case.
     */
    @JvmStatic
    fun spectate(p: Player, game: IArena?): Boolean {
        if (game == null || game.getStatus() != GameState.playing) {
            return false
        }
        val current: IArena? = Arena.getArenaByPlayer(p)
        if (current === game) {
            return false
        }
        if (current != null) {
            if (!canLeaveForSpectating(p)) {
                return false
            }
            if (current.isSpectator(p)) {
                current.removeSpectator(p, false)
            } else {
                current.removePlayer(p, false)
            }
        }
        return game.addSpectator(p, false, null)
    }
    private const val MAX_LISTED_PLAYERS: Int = 16

    /** The players of a game, one per line and in their team color, so a viewer can tell which game to spectate. */
    @JvmStatic
    private fun playerLines(g: IArena): MutableList<String> {
        val lines: MutableList<String> = ArrayList()
        lines.add("§7Playing:")
        var shown: Int = 0
        val players: MutableList<Player> = ArrayList(g.getPlayers())
        for (pl in players) {
            if (shown >= MAX_LISTED_PLAYERS) {
                break
            }
            val team: com.andrei1058.bedwars.api.arena.team.ITeam? = g.getTeam(pl)
            lines.add(" §8▪ " + (if (team == null) "§f" else team.getColor().chat().toString()) + pl.getName())
            shown++
        }
        if (players.size > shown) {
            lines.add(" §8▪ §7+" + (players.size - shown) + " more")
        }
        if (players.isEmpty()) {
            lines.add(" §8▪ §7nobody")
        }
        return lines
    }

    @JvmStatic
    private fun fill(s: String, g: IArena, number: Int): String {
        var alive: Int = 0
        for (t in g.getTeams()!!) {
            if (!t.getMembers()!!.isEmpty()) {
                alive++
            }
        }
        return s.replace("{arena}", g.getDisplayName()).replace("{map_name}", g.getArenaName()).replace("{number}", number.toString()).replace("{world}", g.getWorldName()!!).replace("{on}", g.getPlayers()!!.size.toString()).replace("{max}", g.getMaxPlayers().toString()).replace("{teams}", alive.toString()).replace("{time}", elapsed(g.getStartTime()))
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

    class Holder(@JvmField val arenaName: String, @JvmField val group: String) : InventoryHolder {
        fun getGroup(): String {
            return group
        }

        fun getArenaName(): String {
            return arenaName
        }

        override fun getInventory(): Inventory? {
            return null
        }
    }
}
