/*
 * BedWars1058 - A bed wars mini-game.
 * Copyright (C) 2021 Andrei Dascălu
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Contact e-mail: andrew.dascalu@gmail.com
 */

package com.andrei1058.bedwars.arena

import com.andrei1058.bedwars.BedWars
import com.andrei1058.bedwars.BedWars.Companion.config
import com.andrei1058.bedwars.BedWars.Companion.getChatSupport
import com.andrei1058.bedwars.BedWars.Companion.getServerType
import com.andrei1058.bedwars.BedWars.Companion.getStatsManager
import com.andrei1058.bedwars.BedWars.Companion.nms
import com.andrei1058.bedwars.BedWars.Companion.plugin
import com.andrei1058.bedwars.api.arena.GameState
import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.api.arena.generator.IGenerator
import com.andrei1058.bedwars.api.arena.team.ITeam
import com.andrei1058.bedwars.api.configuration.ConfigPath
import com.andrei1058.bedwars.api.exceptions.InvalidMaterialException
import com.andrei1058.bedwars.api.language.Language.Companion.getList
import com.andrei1058.bedwars.api.language.Language.Companion.getMsg
import com.andrei1058.bedwars.api.language.Messages
import com.andrei1058.bedwars.api.region.Region
import com.andrei1058.bedwars.api.server.ServerType
import com.andrei1058.bedwars.configuration.Sounds
import com.andrei1058.bedwars.stats.PlayerStats
import com.andrei1058.bedwars.support.papi.SupportPAPI
import com.google.common.io.ByteArrayDataOutput
import com.google.common.io.ByteStreams
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.ArrayList
import java.util.Random
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.*
import org.bukkit.block.BlockFace
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Firework
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.FireworkMeta
import org.bukkit.inventory.meta.ItemMeta

object Misc {
    @JvmStatic
    fun moveToLobbyOrKick(p: Player, arena: IArena?, notAbandon: Boolean) {
        if (getServerType() != ServerType.BUNGEE) {
            if (!p.getWorld().getName().equals(config.getLobbyWorldName(), ignoreCase = true)) {
                val loc: Location? = config.getConfigLoc("lobbyLoc")
                if (loc != null) {
                    // Can happen when location is not set in config
                    try {
                        p.teleport(loc)
                    } catch (ignored: Exception) {
                        Bukkit.getLogger().severe("Could not teleport player to lobby! Try setting the lobby again with /bw setLobby")
                    }
                } else {
                    forceKick(p, arena, notAbandon)
                    return
                }
                if (arena != null) {
                    if (arena.isSpectator(p)) {
                        arena.removeSpectator(p, false)
                    } else {
                        arena.removePlayer(p, false)
                        if (!notAbandon && arena.getStatus() == GameState.playing) {
                            if (config.getBoolean(ConfigPath.GENERAL_CONFIGURATION_MARK_LEAVE_AS_ABANDON)) {
                                arena.abandonGame(p)
                            }
                        }
                    }
                }
            } else {
                forceKick(p, arena, notAbandon)
            }
            return
        }
        forceKick(p, arena, notAbandon)
    }

    @JvmStatic
    private fun forceKick(p: Player, arena: IArena?, notAbandon: Boolean) {
        val out = ByteStreams.newDataOutput()
        out.writeUTF("Connect")
        out.writeUTF(config.getYml().getString("lobbyServer"))
        p.sendPluginMessage(plugin, "BungeeCord", out.toByteArray())
        if (arena != null && !notAbandon && arena.getStatus() == GameState.playing) {
            if (config.getBoolean(ConfigPath.GENERAL_CONFIGURATION_MARK_LEAVE_AS_ABANDON)) {
                arena.abandonGame(p)
            }
        }
        if (getServerType() == ServerType.BUNGEE) {
            Bukkit.getScheduler().runTaskLater(plugin, Runnable {
                // if lobby server is unreachable
                if (p.isOnline()) {
                    p.kickPlayer(getMsg(p, Messages.ARENA_RESTART_PLAYER_KICK))
                    if (arena != null && !notAbandon && arena.getStatus() == GameState.playing) {
                        if (config.getBoolean(ConfigPath.GENERAL_CONFIGURATION_MARK_LEAVE_AS_ABANDON)) {
                            arena.abandonGame(p)
                        }
                    }
                }
            }, 30L)
        }
    }

    /**
     * Win fireworks
     */
    @JvmStatic
    fun launchFirework(p: Player) {
        val colors: Array<Color> = arrayOf<Color>(Color.WHITE, Color.AQUA, Color.BLUE, Color.FUCHSIA, Color.GRAY, Color.GREEN, Color.LIME, Color.RED, Color.YELLOW, Color.BLACK, Color.MAROON, Color.NAVY, Color.OLIVE, Color.ORANGE, Color.PURPLE)
        val r = Random()
        val fw = p.getWorld().spawn(p.getEyeLocation(), Firework::class.java)
        val meta = fw.getFireworkMeta()
        meta.setPower(1)
        meta.addEffect(FireworkEffect.builder().withFade(colors[r.nextInt(colors.size - 1)]).withTrail().withColor(colors[r.nextInt(colors.size - 1)]).with(FireworkEffect.Type.BALL_LARGE).build())
        fw.setFireworkMeta(meta)
        fw.setVelocity(p.getEyeLocation().getDirection())
    }

    @JvmStatic
    fun launchFirework(l: Location) {
        val colors: Array<Color> = arrayOf<Color>(Color.WHITE, Color.AQUA, Color.BLUE, Color.FUCHSIA, Color.GRAY, Color.GREEN, Color.LIME, Color.RED, Color.YELLOW, Color.BLACK, Color.MAROON, Color.NAVY, Color.OLIVE, Color.ORANGE, Color.PURPLE)
        val r = Random()
        val fw = l.getWorld().spawn(l, Firework::class.java)
        val meta = fw.getFireworkMeta()
        meta.setPower(1)
        meta.addEffect(FireworkEffect.builder().withFade(colors[r.nextInt(colors.size - 1)]).withTrail().withColor(colors[r.nextInt(colors.size - 1)]).with(FireworkEffect.Type.BALL_LARGE).build())
        fw.setFireworkMeta(meta)
    }

    @JvmStatic
    fun replaceFirst(text: String, regex: String, replacement: String): String {
        return text.replaceFirst(Regex("(?s)" + regex + "(?!.*?" + regex + ")"), replacement)
    }

    /**
     * Create an item stack
     *
     * @param material item material
     * @param data     item data
     * @param name     item name
     * @param lore     item lore
     * @param owner    in case of skull, can be null, don't worry
     */
    @JvmStatic
    fun createItem(material: Material, data: Byte, enchanted: Boolean, name: String, lore: MutableList<String>, owner: Player?, metaKey: String, metaData: String): ItemStack {
        var i = ItemStack(material, 1, data.toShort())
        val im = i.getItemMeta()
        im.setDisplayName(name)
        im.setLore(lore)
        if (enchanted) {
            im.addEnchant(Enchantment.LUCK, 1, true)
            im.addItemFlags(ItemFlag.HIDE_ENCHANTS)
        }
        i.setItemMeta(im)
        if (!(metaData.isEmpty() || metaKey.isEmpty())) {
            i = nms.addCustomData(i, metaKey + "_" + metaData)
        }
        if (owner != null) {
            if (nms.isPlayerHead(material.toString(), data.toInt())) {
                i = nms.getPlayerHead(owner, i)
            }
        }
        return i
    }

    /**
     * Create an itemStack
     */
    @Throws(InvalidMaterialException::class)
    @JvmStatic
    fun createItemStack(material: String, data: Int, name: String, lore: MutableList<String>, enchanted: Boolean, customData: String): ItemStack {
        var m: Material
        try {
            m = Material.valueOf(material)
        } catch (e: Exception) {
            throw InvalidMaterialException(material)
        }
        var i = ItemStack(m, 1, data.toShort())
        val im = i.getItemMeta()
        im.setDisplayName(name)
        im.setLore(lore)
        if (enchanted) {
            im.addEnchant(Enchantment.LUCK, 1, true)
            im.addItemFlags(ItemFlag.HIDE_ENCHANTS)
        }
        i.setItemMeta(im)
        if (!customData.isEmpty()) {
            i = nms.addCustomData(i, customData)
        }
        return i
    }

    @JvmStatic
    fun getDirection(loc: Location): BlockFace {
        var rotation: Int = loc.getYaw().toInt()
        if (rotation < 0) {
            rotation += 360
        }
        if (0 <= rotation && rotation < 22) {
            return BlockFace.SOUTH
        } else if (22 <= rotation && rotation < 67) {
            return BlockFace.SOUTH
        } else if (67 <= rotation && rotation < 112) {
            return BlockFace.WEST
        } else if (112 <= rotation && rotation < 157) {
            return BlockFace.NORTH
        } else if (157 <= rotation && rotation < 202) {
            return BlockFace.NORTH
        } else if (202 <= rotation && rotation < 247) {
            return BlockFace.NORTH
        } else if (247 <= rotation && rotation < 292) {
            return BlockFace.EAST
        } else if (292 <= rotation && rotation < 337) {
            return BlockFace.SOUTH
        } else if (337 <= rotation && rotation < 360) {
            return BlockFace.SOUTH
        } else {
            return BlockFace.SOUTH
        }
    }

    @JvmStatic
    fun isProjectile(i: Material): Boolean {
        return Material.EGG == i || nms.materialFireball() == i || nms.materialSnowball() == i || Material.ARROW == i
    }

    /**
     * create TextComponent message
     */
    @JvmStatic
    fun msgHoverClick(msg: String, hover: String, click: String, clickAction: ClickEvent.Action): TextComponent {
        val tc = TextComponent(msg)
        tc.setHoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, ComponentBuilder(hover).create()))
        tc.setClickEvent(ClickEvent(clickAction, click))
        return tc
    }

    /**
     * add default stats gui item
     */
    @JvmStatic
    fun addDefaultStatsItem(yml: YamlConfiguration, slot: Int, itemstack: Material, data: Int, path: String) {
        yml.addDefault(ConfigPath.GENERAL_CONFIGURATION_STATS_ITEMS_MATERIAL.replace("%path%", path), itemstack.toString())
        yml.addDefault(ConfigPath.GENERAL_CONFIGURATION_STATS_ITEMS_DATA.replace("%path%", path), data)
        yml.addDefault(ConfigPath.GENERAL_CONFIGURATION_STATS_ITEMS_SLOT.replace("%path%", path), slot)
    }

    /**
     * open stats GUI to player
     */
    @JvmStatic
    fun openStatsGUI(p: Player) {
        Bukkit.getScheduler().runTask(plugin, Runnable {
            /* create inventory */
            val inv = Bukkit.createInventory(null, config.getInt(ConfigPath.GENERAL_CONFIGURATION_STATS_GUI_SIZE), replaceStatsPlaceholders(p, getMsg(p, Messages.PLAYER_STATS_GUI_INV_NAME), true))
            /* add custom items to gui */
            for (s in config.getYml().getConfigurationSection(ConfigPath.GENERAL_CONFIGURATION_STATS_PATH).getKeys(false)) {
                /* skip inv size, it isn't a content */
                if (ConfigPath.GENERAL_CONFIGURATION_STATS_GUI_SIZE.contains(s)) {
                    continue
                }
                /* create new itemStack for content */
                val i = nms.createItemStack(config.getYml().getString(ConfigPath.GENERAL_CONFIGURATION_STATS_ITEMS_MATERIAL.replace("%path%", s)).uppercase(), 1, config.getInt(ConfigPath.GENERAL_CONFIGURATION_STATS_ITEMS_DATA.replace("%path%", s)).toShort())
                val im = i.getItemMeta()
                im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
                im.setDisplayName(replaceStatsPlaceholders(p, getMsg(p, Messages.PLAYER_STATS_GUI_PATH + "-" + s + "-name"), true))
                val lore: MutableList<String> = ArrayList()
                for (string in getList(p, Messages.PLAYER_STATS_GUI_PATH + "-" + s + "-lore")) {
                    lore.add(replaceStatsPlaceholders(p, string, true))
                }
                im.setLore(lore)
                i.setItemMeta(im)
                inv.setItem(config.getInt(ConfigPath.GENERAL_CONFIGURATION_STATS_ITEMS_SLOT.replace("%path%", s)), i)
            }
            p.openInventory(inv)
            Sounds.playSound("stats-gui-open", p)
        })
    }

    @JvmStatic
    fun replaceStatsPlaceholders(player: Player, s: String, papiReplacements: Boolean): String {
        var s = s
        val stats = BedWars.getStatsManager().get(player.getUniqueId())
        if (s.contains("{kills}")) {
            s = s.replace("{kills}", stats.getKills().toString())
        }
        if (s.contains("{deaths}")) {
            s = s.replace("{deaths}", stats.getDeaths().toString())
        }
        if (s.contains("{losses}")) {
            s = s.replace("{losses}", stats.getLosses().toString())
        }
        if (s.contains("{wins}")) {
            s = s.replace("{wins}", stats.getWins().toString())
        }
        if (s.contains("{finalKills}")) {
            s = s.replace("{finalKills}", stats.getFinalKills().toString())
        }
        if (s.contains("{finalDeaths}")) {
            s = s.replace("{finalDeaths}", stats.getFinalDeaths().toString())
        }
        if (s.contains("{bedsDestroyed}")) {
            s = s.replace("{bedsDestroyed}", stats.getBedsDestroyed().toString())
        }
        if (s.contains("{gamesPlayed}")) {
            s = s.replace("{gamesPlayed}", stats.getGamesPlayed().toString())
        }
        if (s.contains("{firstPlay}")) {
            s = s.replace("{firstPlay}", SimpleDateFormat(getMsg(player, Messages.FORMATTING_STATS_DATE_FORMAT)).format(if (stats.getFirstPlay() != null) Timestamp.from(stats.getFirstPlay()) else Timestamp.from(Instant.now())))
        }
        if (s.contains("{lastPlay}")) {
            s = s.replace("{lastPlay}", SimpleDateFormat(getMsg(player, Messages.FORMATTING_STATS_DATE_FORMAT)).format(if (stats.getLastPlay() != null) Timestamp.from(stats.getLastPlay()) else Timestamp.from(Instant.now())))
        }
        if (s.contains("{player}")) {
            s = s.replace("{player}", player.getDisplayName())
        }
        if (s.contains("{playername")) {
            s = s.replace("{playername}", player.getName())
        }
        if (s.contains("{prefix}")) {
            s = s.replace("{prefix}", BedWars.getChatSupport().getPrefix(player))
        }
        return if (papiReplacements) SupportPAPI.getSupportPAPI().replace(player, s) else s
    }

    @JvmStatic
    fun isNumber(s: String): Boolean {
        try {
            java.lang.Double.parseDouble(s)
        } catch (e: Exception) {
            try {
                Integer.parseInt(s)
            } catch (ex: Exception) {
                try {
                    java.lang.Long.parseLong(s)
                } catch (exx: Exception) {
                    return false
                }
            }
        }
        return true
    }

    /**
     * Check if a location is outside the World Border
     *
     * @since API 8
     */
    @JvmStatic
    fun isOutsideOfBorder(l: Location): Boolean {
        val border = l.getWorld().getWorldBorder()
        val radius: Double = (border.getSize() / 2) + border.getWarningDistance()
        val center = border.getCenter()
        return center.distance(l) >= radius
    }

    /**
     * Check if location is on a protected region
     */
    @JvmStatic
    fun isBuildProtected(l: Location, a: IArena): Boolean {
        for (region in a.getRegionsList()!!) {
            if (region.isInRegion(l)) {
                return true
            }
        }
        for (t in a.getTeams()!!) {
            for (o in t.getGenerators()) {
                if (o.getLocation()!!.distance(l) <= a.getConfig()!!.getInt(ConfigPath.ARENA_GENERATOR_PROTECTION)) {
                    return true
                }
            }
        }
        for (o in a.getOreGenerators()!!) {
            if (o.getLocation()!!.distance(l) <= a.getConfig()!!.getInt(ConfigPath.ARENA_GENERATOR_PROTECTION)) {
                return true
            }
        }
        return isOutsideOfBorder(l)
    }

    /**
     * Get lower location between 2 locations.
     *
     * @return a new Location instance.
     */
    @JvmStatic
    fun minLoc(loc1: Location, loc2: Location): Location {
        if (loc1.getWorld() !== loc2.getWorld()) {
            throw IllegalStateException("Locations are not in the same world!")
        }
        val x: Double = Math.min(loc1.getX(), loc2.getX())
        val y: Double = Math.min(loc1.getY(), loc2.getY())
        val z: Double = Math.min(loc1.getZ(), loc2.getZ())
        return Location(loc1.getWorld(), x, y, z)
    }

    /**
     * Get higher location between 2 locations.
     *
     * @return a new Location instance.
     */
    @JvmStatic
    fun maxLoc(loc1: Location, loc2: Location): Location {
        if (loc1.getWorld() !== loc2.getWorld()) {
            throw IllegalStateException("Locations are not in the same world!")
        }
        val x: Double = Math.max(loc1.getX(), loc2.getX())
        val y: Double = Math.max(loc1.getY(), loc2.getY())
        val z: Double = Math.max(loc1.getZ(), loc2.getZ())
        return Location(loc1.getWorld(), x, y, z)
    }
}
