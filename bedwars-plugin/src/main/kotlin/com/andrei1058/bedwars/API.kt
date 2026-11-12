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

package com.andrei1058.bedwars

import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.api.arena.shop.IContentTier
import com.andrei1058.bedwars.api.command.ParentCommand
import com.andrei1058.bedwars.api.configuration.ConfigManager
import com.andrei1058.bedwars.api.events.player.PlayerAfkEvent
import com.andrei1058.bedwars.api.language.Language
import com.andrei1058.bedwars.api.party.Party
import com.andrei1058.bedwars.api.server.ISetupSession
import com.andrei1058.bedwars.api.server.RestoreAdapter
import com.andrei1058.bedwars.api.server.ServerType
import com.andrei1058.bedwars.api.server.VersionSupport
import com.andrei1058.bedwars.api.sidebar.ISidebarService
import com.andrei1058.bedwars.arena.Arena
import com.andrei1058.bedwars.arena.SetupSession
import com.andrei1058.bedwars.commands.bedwars.MainCommand
import com.andrei1058.bedwars.shop.main.CategoryContent
import com.andrei1058.bedwars.sidebar.SidebarService
import com.andrei1058.bedwars.stats.StatsAPI
import com.andrei1058.bedwars.upgrades.UpgradesManager
import java.io.File
import java.util.HashMap
import java.util.LinkedList
import java.util.UUID
import java.util.logging.Level
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import com.andrei1058.bedwars.api.util.unchecked
import com.andrei1058.bedwars.api.util.uninitialized

open class API : com.andrei1058.bedwars.api.BedWars {
    private val afkSystem: com.andrei1058.bedwars.api.BedWars.AFKUtil = object : com.andrei1058.bedwars.api.BedWars.AFKUtil {
        private val afkPlayers: HashMap<UUID, Int> = HashMap()

        override fun isPlayerAFK(player: Player): Boolean {
            return afkPlayers.containsKey(player.getUniqueId())
        }

        override fun setPlayerAFK(player: Player, value: Boolean) {
            if (value) {
                if (!afkPlayers.containsKey(player.getUniqueId())) {
                    afkPlayers.put(player.getUniqueId(), Arena.afkCheck.get(player.getUniqueId()).unchecked())
                    Bukkit.getPluginManager().callEvent(PlayerAfkEvent(player, PlayerAfkEvent.AFKType.START))
                }
            } else {
                if (afkPlayers.containsKey(player.getUniqueId())) {
                    afkPlayers.remove(player.getUniqueId())
                    Bukkit.getPluginManager().callEvent(PlayerAfkEvent(player, PlayerAfkEvent.AFKType.END))
                }
                Arena.afkCheck.remove(player.getUniqueId())
            }
        }

        override fun getPlayerTimeAFK(player: Player): Int {
            return afkPlayers.getOrDefault(player.getUniqueId(), 0)
        }
    }
    private val arenaUtil: com.andrei1058.bedwars.api.BedWars.ArenaUtil = object : com.andrei1058.bedwars.api.BedWars.ArenaUtil {
        override fun canAutoScale(arenaName: String): Boolean {
            return Arena.canAutoScale(arenaName)
        }

        override fun addToEnableQueue(a: IArena) {
            Arena.addToEnableQueue(a)
        }

        override fun removeFromEnableQueue(a: IArena) {
            Arena.removeFromEnableQueue(a)
        }

        override fun isPlaying(p: Player): Boolean {
            return Arena.isInArena(p) && Arena.getArenaByPlayer(p).isPlayer(p)
        }

        override fun isSpectating(p: Player): Boolean {
            return Arena.isInArena(p) && Arena.getArenaByPlayer(p).isSpectator(p)
        }

        override fun loadArena(worldName: String, sender: Player?) {
            Arena(worldName, sender)
        }

        override fun setGamesBeforeRestart(games: Int) {
            Arena.setGamesBeforeRestart(games)
        }

        override fun getGamesBeforeRestart(): Int {
            return Arena.getGamesBeforeRestart()
        }

        override fun getArenaByPlayer(player: Player): IArena {
            return Arena.getArenaByPlayer(player)
        }

        override fun setArenaByPlayer(p: Player, arena: IArena) {
            Arena.setArenaByPlayer(p, arena)
        }

        override fun removeArenaByPlayer(p: Player, a: IArena) {
            Arena.removeArenaByPlayer(p, a)
        }

        override fun getArenaByName(worldName: String): IArena {
            return Arena.getArenaByName(worldName)
        }

        override fun getArenaByIdentifier(worldName: String): IArena {
            return Arena.getArenaByIdentifier(worldName)
        }

        override fun setArenaByName(arena: IArena) {
            Arena.setArenaByName(arena)
        }

        override fun removeArenaByName(worldName: String) {
            Arena.removeArenaByName(worldName)
        }

        override fun getArenas(): LinkedList<IArena> {
            return Arena.getArenas()
        }

        override fun vipJoin(p: Player): Boolean {
            return Arena.isVip(p)
        }

        override fun getPlayers(group: String): Int {
            return Arena.getPlayers(group)
        }

        override fun joinRandomArena(p: Player): Boolean {
            return Arena.joinRandomArena(p)
        }

        override fun joinRandomFromGroup(p: Player, group: String): Boolean {
            return Arena.joinRandomFromGroup(p, group)
        }

        override fun getEnableQueue(): LinkedList<IArena> {
            return Arena.getEnableQueue()
        }

        override fun sendLobbyCommandItems(p: Player) {
            Arena.sendLobbyCommandItems(p)
        }
    }
    private val configs: com.andrei1058.bedwars.api.BedWars.Configs = object : com.andrei1058.bedwars.api.BedWars.Configs {
        override fun getMainConfig(): ConfigManager {
            return BedWars.config
        }

        override fun getSignsConfig(): ConfigManager {
            return BedWars.signs
        }

        override fun getGeneratorsConfig(): ConfigManager {
            return BedWars.generators
        }

        override fun getShopConfig(): ConfigManager {
            return BedWars.shop
        }

        override fun getUpgradesConfig(): ConfigManager {
            return UpgradesManager.getConfiguration()
        }
    }
    private val shopUtil: com.andrei1058.bedwars.api.BedWars.ShopUtil = object : com.andrei1058.bedwars.api.BedWars.ShopUtil {
        override fun calculateMoney(player: Player, currency: Material): Int {
            return CategoryContent.calculateMoney(player, currency)
        }

        override fun getCurrency(currency: String): Material {
            return CategoryContent.getCurrency(currency)
        }

        override fun getCurrencyColor(currency: Material): ChatColor {
            return CategoryContent.getCurrencyColor(currency)
        }

        override fun getCurrencyMsgPath(contentTier: IContentTier): String {
            return CategoryContent.getCurrencyMsgPath(contentTier)
        }

        override fun getRomanNumber(n: Int): String {
            return CategoryContent.getRomanNumber(n)
        }

        override fun takeMoney(player: Player, currency: Material, amount: Int) {
            CategoryContent.takeMoney(player, currency, amount)
        }
    }

    override fun getStatsUtil(): com.andrei1058.bedwars.api.BedWars.IStats {
        return StatsAPI.getInstance().unchecked()
    }

    override fun getAFKUtil(): com.andrei1058.bedwars.api.BedWars.AFKUtil {
        return afkSystem
    }

    override fun getArenaUtil(): com.andrei1058.bedwars.api.BedWars.ArenaUtil {
        return arenaUtil
    }

    override fun getConfigs(): com.andrei1058.bedwars.api.BedWars.Configs {
        return configs
    }

    override fun getShopUtil(): com.andrei1058.bedwars.api.BedWars.ShopUtil {
        return shopUtil
    }
    private val teamUpgradesUtil: com.andrei1058.bedwars.api.BedWars.TeamUpgradesUtil = object : com.andrei1058.bedwars.api.BedWars.TeamUpgradesUtil {
        override fun isWatchingGUI(player: Player): Boolean {
            return UpgradesManager.isWatchingUpgrades(player.getUniqueId())
        }

        override fun setWatchingGUI(player: Player) {
            UpgradesManager.setWatchingUpgrades(player.getUniqueId())
        }

        override fun removeWatchingUpgrades(uuid: UUID) {
            UpgradesManager.removeWatchingUpgrades(uuid)
        }

        override fun getTotalUpgradeTiers(arena: IArena): Int {
            return UpgradesManager.getMenuForArena(arena).countTiers()
        }
    }

    override fun getTeamUpgradesUtil(): com.andrei1058.bedwars.api.BedWars.TeamUpgradesUtil {
        return teamUpgradesUtil
    }

    override fun getLevelsUtil(): com.andrei1058.bedwars.api.levels.Level {
        return BedWars.getLevelSupport()
    }

    override fun getPartyUtil(): Party {
        return BedWars.getParty()
    }

    override fun getSetupSession(player: UUID): ISetupSession {
        return SetupSession.getSession(player).unchecked()
    }

    override fun isInSetupSession(player: UUID): Boolean {
        return SetupSession.isInSetupSession(player)
    }

    override fun getServerType(): ServerType {
        return BedWars.getServerType()
    }

    override fun getBedWarsCommand(): ParentCommand {
        return MainCommand.getInstance()
    }

    override fun getRestoreAdapter(): RestoreAdapter {
        return restoreAdapter
    }

    @Throws(IllegalAccessError::class)
    override fun setRestoreAdapter(adapter: RestoreAdapter) {
        if (!Arena.getArenas().isEmpty()) {
            throw IllegalAccessError("Arenas must be unloaded when changing the adapter")
        }
        restoreAdapter = adapter
        if (adapter.getOwner() != null) {
            if (adapter.getOwner() !== BedWars.plugin) {
                BedWars.plugin.getLogger().log(Level.WARNING, adapter.getOwner().getName() + " changed the restore system to its own adapter.")
            }
        }
    }

    @Throws(IllegalAccessError::class)
    override fun setPartyAdapter(partyAdapter: Party?) {
        if (partyAdapter == null) {
            return
        }
        if (partyAdapter == BedWars.getParty()) {
            return
        }
        BedWars.setParty(partyAdapter)
        BedWars.plugin.getLogger().log(Level.WARNING, "One of your plugins changed the Party adapter to: " + partyAdapter.javaClass.getName())
    }

    override fun getVersionSupport(): VersionSupport {
        return BedWars.nms
    }

    override fun getDefaultLang(): Language {
        return Language.getDefaultLanguage()
    }

    override fun getLobbyWorld(): String {
        return BedWars.getLobbyWorld()
    }

    override fun getForCurrentVersion(v18: String, v12: String, v13: String): String {
        return BedWars.getForCurrentVersion(v18, v12, v13)
    }

    override fun setLevelAdapter(level: com.andrei1058.bedwars.api.levels.Level) {
        BedWars.setLevelAdapter(level)
    }

    override fun isAutoScale(): Boolean {
        return BedWars.autoscale
    }

    override fun getLanguageByIso(isoCode: String): Language {
        return Language.getLang(isoCode)
    }

    override fun getPlayerLanguage(player: Player): Language {
        return Language.getPlayerLanguage(player)
    }

    override fun getLangIso(p: Player): String {
        return Language.getPlayerLanguage(p).getIso().unchecked()
    }

    override fun getAddonsPath(): File {
        return File(BedWars.plugin.getDataFolder(), "Addons")
    }

    override fun getScoreboardUtil(): com.andrei1058.bedwars.api.BedWars.ScoreboardUtil {
        return scoreboardUtil
    }

    override fun isShuttingDown(): Boolean {
        return BedWars.isShuttingDown()
    }

    override fun getScoreboardManager(): ISidebarService {
        return SidebarService.getInstance().unchecked()
    }

    companion object {
        private var restoreAdapter: RestoreAdapter = uninitialized()
        private val scoreboardUtil: com.andrei1058.bedwars.api.BedWars.ScoreboardUtil = object : com.andrei1058.bedwars.api.BedWars.ScoreboardUtil {
            override fun removePlayerScoreboard(player: Player) {
                SidebarService.getInstance()!!.remove(player)
            }

            override fun givePlayerScoreboard(player: Player, delay: Boolean) {
                SidebarService.getInstance()!!.giveSidebar(player, Arena.getArenaByPlayer(player), delay)
            }
        }
    }
}
