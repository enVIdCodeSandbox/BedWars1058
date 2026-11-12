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

package com.andrei1058.bedwars.api.language

import com.andrei1058.bedwars.api.BedWars
import com.andrei1058.bedwars.api.configuration.ConfigManager
import com.andrei1058.bedwars.api.configuration.ConfigPath
import com.andrei1058.bedwars.api.events.player.PlayerLangChangeEvent
import java.util.*
import net.md_5.bungee.api.ChatColor
import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import com.andrei1058.bedwars.api.util.unchecked
import com.andrei1058.bedwars.api.util.uninitialized

open class Language(plugin: Plugin, private val iso: String?) : ConfigManager(plugin, "messages_" + iso, plugin.getDataFolder().getPath() + "/Languages") {
    private var prefix: String? = ""
    private var serverIp: String? = null

    /**
     * Set chat prefix.
     */
    open fun setPrefix(prefix: String) {
        this.prefix = prefix
    }

    open fun setPrefixStatic(prefix: String) {
        prefixStatic = prefix
    }

    /**
     * Get language display name.
     */
    open fun getLangName(): String {
        return getYml().getString("name")
    }

    /**
     * Check if a message was set.
     */
    open fun exists(path: String): Boolean {
        return getYml().get(path) != null
    }

    open fun relocate(from: String, to: String) {
        val fromData: Any? = getYml().get(from)
        if (null != fromData) {
            this.getYml().set(to, fromData)
            this.getYml().set(from, null)
        }
    }

    /**
     * Get a color translated message.
     */
    open fun m(path: String): String {
        var message: String? = getYml().getString(path)
        if (message == null) {
            System.err.println("Missing message key " + path + " in language " + getIso())
            message = "MISSING_LANG"
        }
        if (null == serverIp) {
            val api = Bukkit.getServicesManager().getRegistration(BedWars::class.java).getProvider()
            if (null != api.getConfigs().getMainConfig()) {
                serverIp = api.getConfigs().getMainConfig().getString(ConfigPath.GENERAL_CONFIG_PLACEHOLDERS_REPLACEMENTS_SERVER_IP)
            }
        }
        return ChatColor.translateAlternateColorCodes('&', message.replace("{prefix}", (prefix ?: "")).replace("{serverIp}", serverIp ?: ""))
    }

    /**
     * Get a color translated list.
     */
    open fun l(path: String): MutableList<String> {
        val result: MutableList<String> = ArrayList()
        var lines: MutableList<String>? = getYml().getStringList(path)
        if (lines == null) {
            System.err.println("Missing message list key " + path + " in language " + getIso())
            lines = Collections.emptyList()
        }
        for (line in lines) {
            result.add(ChatColor.translateAlternateColorCodes('&', line))
        }
        return result
    }

    /**
     * Get language iso code.
     */
    open fun getIso(): String? {
        return iso
    }

    open fun addDefaultStatsMsg(yml: YamlConfiguration, path: String, name: String, vararg lore: String) {
        if (yml.getDefaults() == null || !yml.getDefaults().contains(Messages.PLAYER_STATS_GUI_PATH + "-" + path + "-name")) {
            yml.addDefault(Messages.PLAYER_STATS_GUI_PATH + "-" + path + "-name", name)
        }
        if (yml.getDefaults() == null || !yml.getDefaults().contains(Messages.PLAYER_STATS_GUI_PATH + "-" + path + "-lore")) {
            yml.addDefault(Messages.PLAYER_STATS_GUI_PATH + "-" + path + "-lore", lore)
        }
    }

    /**
     * Create messages paths for new shop categories
     */
    open fun setupUnSetCategories() {
        val api = Bukkit.getServer().getServicesManager().getRegistration(BedWars::class.java).getProvider()
        for (s in api.getConfigs().getShopConfig().getYml().getConfigurationSection("").getKeys(false)) {
            if (s.equals(ConfigPath.SHOP_SETTINGS_PATH, ignoreCase = true)) {
                continue
            }
            if (s.equals(ConfigPath.SHOP_SPECIALS_PATH, ignoreCase = true)) {
                continue
            }
            if (s == ConfigPath.SHOP_QUICK_DEFAULTS_PATH) {
                continue
            }
            if (!exists(Messages.SHOP_CATEGORY_INVENTORY_NAME.replace("%category%", s))) {
                set(Messages.SHOP_CATEGORY_INVENTORY_NAME.replace("%category%", s), "&8Name not set")
            }
            if (!exists(Messages.SHOP_CATEGORY_ITEM_NAME.replace("%category%", s))) {
                set(Messages.SHOP_CATEGORY_ITEM_NAME.replace("%category%", s), "&8Name not set")
            }
            if (!exists(Messages.SHOP_CATEGORY_ITEM_LORE.replace("%category%", s))) {
                set(Messages.SHOP_CATEGORY_ITEM_LORE.replace("%category%", s), Collections.singletonList("&8Lore not set"))
            }
            if (api.getConfigs().getShopConfig().getYml().get(s + ConfigPath.SHOP_CATEGORY_CONTENT_PATH) == null) {
                continue
            }
            for (c in api.getConfigs().getShopConfig().getYml().getConfigurationSection(s + ConfigPath.SHOP_CATEGORY_CONTENT_PATH).getKeys(false)) {
                if (!exists(Messages.SHOP_CONTENT_TIER_ITEM_NAME.replace("%category%", s).replace("%content%", c))) {
                    set(Messages.SHOP_CONTENT_TIER_ITEM_NAME.replace("%category%", s).replace("%content%", c), "&8Name not set")
                }
                if (!exists(Messages.SHOP_CONTENT_TIER_ITEM_LORE.replace("%category%", s).replace("%content%", c))) {
                    set(Messages.SHOP_CONTENT_TIER_ITEM_LORE.replace("%category%", s).replace("%content%", c), Collections.singletonList("&8Lore not set"))
                }
            }
        }
    }

    init {
        // replace old placeholders
        val oldMsg = getYml().getStringList(Messages.GAME_END_TOP_PLAYER_CHAT)
        if (!oldMsg.isEmpty()) {
            val oldTop1 = arrayOf<String>("{firstName}", "{secondName}", "{thirdName}")
            val oldTop2 = arrayOf<String>("{firstKills}", "{secondKills}", "{thirdKills}")
            val newMsg: MutableList<String> = ArrayList()
            for (oldString0 in oldMsg) {
                var oldString = oldString0
                for (oldPlaceholder in oldTop1) {
                    oldString = oldString.replace(oldPlaceholder, "{topPlayerName}")
                }
                for (oldPlaceholder in oldTop2) {
                    oldString = oldString.replace(oldPlaceholder, "{topValue}")
                }
                newMsg.add(oldString)
            }
            getYml().set(Messages.GAME_END_TOP_PLAYER_CHAT, newMsg)
        }
        if (null != getYml().get("scoreboard")) {
            for (group in getYml().getConfigurationSection("scoreboard").getKeys(false)) {
                if (group.equals("lobby", ignoreCase = true)) {
                    relocate("scoreboard." + group, "sidebar." + group)
                } else {
                    val stages: MutableMap<String, Array<String>> = HashMap()
                    stages.put("waiting", arrayOf<String>(Messages.SCOREBOARD_DEFAULT_WAITING, Messages.SCOREBOARD_DEFAULT_WAITING_SPEC))
                    stages.put("starting", arrayOf<String>(Messages.SCOREBOARD_DEFAULT_STARTING, Messages.SCOREBOARD_DEFAULT_STARTING_SPEC))
                    for (stage in stages.entries) {
                        if (exists("scoreboard." + group + "." + stage.key + ".player")) {
                            relocate("scoreboard." + group + "." + stage.key + ".player", stage.value[0].replace("Default", group))
                        } else {
                            relocate("scoreboard." + group + "." + stage.key, stage.value[0].replace("Default", group))
                        }
                        if (exists("scoreboard." + group + "." + stage.key + ".spectator")) {
                            relocate("scoreboard." + group + "." + stage.key + ".spectator", stage.value[1].replace("Default", group))
                        }
                    }
                    if (exists("scoreboard." + group + ".playing.alive")) {
                        relocate("scoreboard." + group + ".playing.alive", Messages.SCOREBOARD_DEFAULT_PLAYING.replace("Default", group))
                        relocate("scoreboard." + group + ".playing.spectator", Messages.SCOREBOARD_DEFAULT_PLAYING.replace("Default", group))
                    } else {
                        relocate("scoreboard." + group + ".playing", Messages.SCOREBOARD_DEFAULT_PLAYING.replace("Default", group))
                    }
                }
            }
            getYml().set("scoreboard", null)
        }
        languages.add(this)
    }

    companion object {
        private var prefixStatic: String? = ""
        private val langByPlayer: HashMap<UUID, Language> = HashMap()
        private val languages: MutableList<Language> = ArrayList()
        private var defaultLanguage: Language = uninitialized()

        /**
         * Get scoreboard strings.
         */
        @JvmStatic
        fun getScoreboard(player: Player, path: String, alternative: String): MutableList<String> {
            val language = getPlayerLanguage(player)
            if (language.exists(path)) {
                return language.l(path)
            } else {
                if (path.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray().size == 3) {
                    val sp = path.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    var path2 = sp[1]
                    path2 = path2[0].toString().uppercase() + path2.substring(1).lowercase()
                    path2 = sp[0] + "." + path2 + "." + sp[2]
                    if (language.exists(path2)) {
                        return language.l(path)
                    } else if (language.exists(sp[0] + "." + sp[1].uppercase() + "." + sp[2])) {
                        return language.l(sp[0] + "." + sp[1].uppercase() + "." + sp[2])
                    }
                }
            }
            return language.l(alternative)
        }

        /**
         * Get message in player's language.
         */
        @JvmStatic
        fun getMsg(player: Player?, path: String): String {
            if (player == null) {
                return getDefaultLanguage().m(path)
            }
            return langByPlayer.getOrDefault(player.getUniqueId(), getDefaultLanguage()).m(path).replace("{prefix}", (prefixStatic ?: ""))
        }

        /**
         * Retrieve a player language.
         */
        @JvmStatic
        fun getPlayerLanguage(player: Player): Language {
            return langByPlayer.getOrDefault(player.getUniqueId(), getDefaultLanguage())
        }

        @JvmStatic
        fun getPlayerLanguage(p: UUID): Language {
            return langByPlayer.getOrDefault(p, getDefaultLanguage())
        }

        /**
         * Get a string list in player's language.
         */
        @JvmStatic
        fun getList(player: Player, path: String): MutableList<String> {
            return langByPlayer.getOrDefault(player.getUniqueId(), getDefaultLanguage()).l(path)
        }

        /**
         * Save a value to file if not exists.
         */
        @JvmStatic
        fun saveIfNotExists(path: String, data: Any) {
            for (l in languages) {
                if (l.getYml().get(path) == null) {
                    l.set(path, data)
                }
            }
        }

        @JvmStatic
        fun getLangByPlayer(): HashMap<UUID, Language> {
            return langByPlayer
        }

        /**
         * Check if a language exists.
         */
        @JvmStatic
        fun isLanguageExist(iso: String): Boolean {
            for (l in languages) {
                if (l.iso.equals(iso, ignoreCase = true)) {
                    return true
                }
            }
            return false
        }

        /**
         * Get language with given info.
         *
         * @return null if you could not find.
         */
        @JvmStatic
        fun getLang(iso: String): Language {
            for (l in languages) {
                if (l.iso.equals(iso, ignoreCase = true)) {
                    return l
                }
            }
            return getDefaultLanguage()
        }

        /**
         * Get loaded languages list.
         */
        @JvmStatic
        fun getLanguages(): MutableList<Language> {
            return languages
        }

        /**
         * Save messages for unset stats items.
         */
        @JvmStatic
        fun setupCustomStatsMessages() {
            val api = Bukkit.getServer().getServicesManager().getRegistration(BedWars::class.java).getProvider()
            for (l in getLanguages()) {
                if (l == null) {
                    continue
                }
                if (l.getYml() == null) {
                    continue
                }
                /* save messages for stats gui items if custom items added */
                if (api.getConfigs().getMainConfig().getYml().get(ConfigPath.GENERAL_CONFIGURATION_STATS_PATH) == null) {
                    return
                }
                for (item in api.getConfigs().getMainConfig().getYml().getConfigurationSection(ConfigPath.GENERAL_CONFIGURATION_STATS_PATH).getKeys(false)) {
                    if (ConfigPath.GENERAL_CONFIGURATION_STATS_GUI_SIZE.contains(item)) {
                        continue
                    }
                    if (l.getYml().getDefaults() == null || !l.getYml().getDefaults().contains(Messages.PLAYER_STATS_GUI_PATH + "-" + item + "-name")) {
                        l.getYml().addDefault(Messages.PLAYER_STATS_GUI_PATH + "-" + item + "-name", "Name not set")
                    }
                    if (l.getYml().getDefaults() == null || !l.getYml().getDefaults().contains(Messages.PLAYER_STATS_GUI_PATH + "-" + item + "-lore")) {
                        l.getYml().addDefault(Messages.PLAYER_STATS_GUI_PATH + "-" + item + "-lore", Collections.singletonList("lore not set"))
                    }
                }
                l.save()
            }
        }

        /**
         * Create missing name/ lore for items: multi arena lobby, waiting, spectating
         */
        @JvmStatic
        fun addDefaultMessagesCommandItems(language: Language?) {
            if (language == null) {
                return
            }
            val yml: YamlConfiguration? = language.getYml()
            if (yml == null) {
                return
            }
            val api = Bukkit.getServer().getServicesManager().getRegistration(BedWars::class.java).getProvider()
            if (api.getConfigs().getMainConfig().getYml().get(ConfigPath.GENERAL_CONFIGURATION_LOBBY_ITEMS_PATH) != null) {
                for (item in api.getConfigs().getMainConfig().getYml().getConfigurationSection(ConfigPath.GENERAL_CONFIGURATION_LOBBY_ITEMS_PATH).getKeys(false)) {
                    if (item.isEmpty()) {
                        continue
                    }
                    val p1 = Messages.GENERAL_CONFIGURATION_LOBBY_ITEMS_NAME.replace("%path%", item)
                    val p2 = Messages.GENERAL_CONFIGURATION_LOBBY_ITEMS_LORE.replace("%path%", item)
                    if (yml.getDefaults() == null || !yml.getDefaults().contains(p1)) {
                        yml.addDefault(p1, "&cName not set at: &f" + p1)
                    }
                    if (yml.getDefaults() == null || !yml.getDefaults().contains(p1)) {
                        yml.addDefault(p2, Arrays.asList("&cLore not set at:", " &f" + p2))
                    }
                }
            }
            if (api.getConfigs().getMainConfig().getYml().get(ConfigPath.GENERAL_CONFIGURATION_SPECTATOR_ITEMS_PATH) != null) {
                for (item in api.getConfigs().getMainConfig().getYml().getConfigurationSection(ConfigPath.GENERAL_CONFIGURATION_SPECTATOR_ITEMS_PATH).getKeys(false)) {
                    if (item.isEmpty()) {
                        continue
                    }
                    val p1 = Messages.GENERAL_CONFIGURATION_SPECTATOR_ITEMS_NAME.replace("%path%", item)
                    val p2 = Messages.GENERAL_CONFIGURATION_SPECTATOR_ITEMS_LORE.replace("%path%", item)
                    if (yml.getDefaults() == null || !yml.getDefaults().contains(p1)) {
                        yml.addDefault(p1, "&cName not set at: &f" + p1)
                    }
                    if (yml.getDefaults() == null || !yml.getDefaults().contains(p1)) {
                        yml.addDefault(p2, Arrays.asList("&cLore not set at:", " &f" + p2))
                    }
                }
            }
            if (api.getConfigs().getMainConfig().getYml().get(ConfigPath.GENERAL_CONFIGURATION_PRE_GAME_ITEMS_PATH) != null) {
                for (item in api.getConfigs().getMainConfig().getYml().getConfigurationSection(ConfigPath.GENERAL_CONFIGURATION_PRE_GAME_ITEMS_PATH).getKeys(false)) {
                    if (item.isEmpty()) {
                        continue
                    }
                    val p1 = Messages.GENERAL_CONFIGURATION_WAITING_ITEMS_NAME.replace("%path%", item)
                    val p2 = Messages.GENERAL_CONFIGURATION_WAITING_ITEMS_LORE.replace("%path%", item)
                    if (yml.getDefaults() == null || !yml.getDefaults().contains(p1)) {
                        yml.addDefault(p1, "&cName not set at: &f" + p1)
                    }
                    if (yml.getDefaults() == null || !yml.getDefaults().contains(p1)) {
                        yml.addDefault(p2, Arrays.asList("&cLore not set at:", " &f" + p2))
                    }
                }
            }
            yml.options().copyDefaults(true)
            language.save()
        }

        /**
         * Add required messages for a shop category to the given yml
         */
        @JvmStatic
        fun addCategoryMessages(yml: YamlConfiguration, categoryName: String, invName: String, itemName: String, itemLore: MutableList<String>) {
            if (yml.getDefaults() == null || !yml.getDefaults().contains(Messages.SHOP_CATEGORY_INVENTORY_NAME.replace("%category%", categoryName))) {
                yml.addDefault(Messages.SHOP_CATEGORY_INVENTORY_NAME.replace("%category%", categoryName), invName)
            }
            if (yml.getDefaults() == null || !yml.getDefaults().contains(Messages.SHOP_CATEGORY_ITEM_NAME.replace("%category%", categoryName))) {
                yml.addDefault(Messages.SHOP_CATEGORY_ITEM_NAME.replace("%category%", categoryName), itemName)
            }
            if (yml.getDefaults() == null || !yml.getDefaults().contains(Messages.SHOP_CATEGORY_ITEM_LORE.replace("%category%", categoryName))) {
                yml.addDefault(Messages.SHOP_CATEGORY_ITEM_LORE.replace("%category%", categoryName), itemLore)
            }
        }

        /**
         * Add required messages for a shop category to the given yml
         */
        @JvmStatic
        fun addContentMessages(yml: YamlConfiguration, contentName: String, categoryName: String, itemName: String, itemLore: MutableList<String>) {
            val path1 = Messages.SHOP_CONTENT_TIER_ITEM_NAME.replace("%category%", categoryName).replace("%content%", contentName)
            val path2 = Messages.SHOP_CONTENT_TIER_ITEM_LORE.replace("%category%", categoryName).replace("%content%", contentName)
            if (yml.getDefaults() == null || !yml.getDefaults().contains(path1)) {
                yml.addDefault(path1, itemName)
            }
            if (yml.getDefaults() == null || !yml.getDefaults().contains(path2)) {
                yml.addDefault(path2, itemLore)
            }
        }

        /**
         * Change a player language and refresh
         * scoreboard and custom join items.
         */
        @JvmStatic
        fun setPlayerLanguage(uuid: UUID, iso: String?): Boolean {
            if (iso == null) {
                if (langByPlayer.containsKey(uuid)) {
                    val player: Player? = Bukkit.getPlayer(uuid)
                    if (player != null && player.isOnline()) {
                        val e = PlayerLangChangeEvent(player, langByPlayer.get(uuid)!!.iso.unchecked(), getDefaultLanguage().iso.unchecked())
                        Bukkit.getPluginManager().callEvent(e)
                        if (e.isCancelled()) {
                            return false
                        }
                    }
                }
                langByPlayer.remove(uuid)
                return true
            }
            val newLang: Language? = Language.getLang(iso)
            if (newLang == null) {
                return false
            }
            val oldLang = Language.getPlayerLanguage(uuid)
            if (oldLang.getIso() == newLang.getIso()) {
                return false
            }
            val player: Player? = Bukkit.getPlayer(uuid)
            if (player != null && player.isOnline()) {
                val e = PlayerLangChangeEvent(player, oldLang.getIso().unchecked(), newLang.getIso().unchecked())
                Bukkit.getPluginManager().callEvent(e)
                if (e.isCancelled()) {
                    return false
                }
            }
            if (Language.getDefaultLanguage().getIso() == newLang.getIso()) {
                langByPlayer.remove(uuid)
                return true
            }
            if (langByPlayer.containsKey(uuid)) {
                langByPlayer.replace(uuid, newLang)
            } else {
                langByPlayer.put(uuid, newLang)
            }
            return true
        }

        @JvmStatic
        fun getCountDownTitle(playerLang: Language, second: Int): Array<String> {
            val result = arrayOfNulls<String>(2) as Array<String>
            result[0] = ChatColor.translateAlternateColorCodes('&', playerLang.getYml().get(Messages.ARENA_STATUS_START_COUNTDOWN_TITLE + "-" + second, playerLang.getString(Messages.ARENA_STATUS_START_COUNTDOWN_TITLE)).toString().replace("{second}", second.toString()))
            if (result[0].isEmpty()) {
                result[0] = " "
            }
            result[1] = ChatColor.translateAlternateColorCodes('&', playerLang.getYml().get(Messages.ARENA_STATUS_START_COUNTDOWN_SUB_TITLE + "-" + second, playerLang.getString(Messages.ARENA_STATUS_START_COUNTDOWN_SUB_TITLE)).toString().replace("{second}", second.toString()))
            if (result[1].isEmpty()) {
                result[1] = " "
            }
            return result
        }

        /**
         * Change server default language.
         */
        @JvmStatic
        fun setDefaultLanguage(defaultLanguage: Language) {
            Language.defaultLanguage = defaultLanguage
        }

        /**
         * Get server default language.
         */
        @JvmStatic
        fun getDefaultLanguage(): Language {
            return defaultLanguage
        }
    }
}
