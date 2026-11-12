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

package com.andrei1058.bedwars.api

import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.api.arena.shop.IContentTier
import com.andrei1058.bedwars.api.command.ParentCommand
import com.andrei1058.bedwars.api.configuration.ConfigManager
import com.andrei1058.bedwars.api.language.Language
import com.andrei1058.bedwars.api.levels.Level
import com.andrei1058.bedwars.api.party.Party
import com.andrei1058.bedwars.api.server.ISetupSession
import com.andrei1058.bedwars.api.server.RestoreAdapter
import com.andrei1058.bedwars.api.server.ServerType
import com.andrei1058.bedwars.api.server.VersionSupport
import com.andrei1058.bedwars.api.sidebar.ISidebarService
import java.io.File
import java.sql.Timestamp
import java.util.LinkedList
import java.util.UUID
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player

interface BedWars {
    /**
     * Get stats utils.
     */
    fun getStatsUtil(): IStats

    interface IStats {
        /**
         * Get player first play date.
         * You get data from the local cache.
         */
        fun getPlayerFirstPlay(p: UUID): Timestamp?

        /**
         * Get player last play date.
         * You get data from the local cache.
         */
        fun getPlayerLastPlay(p: UUID): Timestamp?

        /**
         * Get player total wins.
         * You get data from the local cache.
         */
        fun getPlayerWins(p: UUID): Int

        /**
         * Get player regular kills.
         * You get data from the local cache.
         */
        fun getPlayerKills(p: UUID): Int

        /**
         * Get player total kills.
         * Regular kills + final kills.
         * You get data from the local cache.
         */
        fun getPlayerTotalKills(p: UUID): Int

        /**
         * Get player total final kills.
         * You get data from the local cache.
         */
        fun getPlayerFinalKills(p: UUID): Int

        /**
         * Get player total looses.
         * You get data from the local cache.
         */
        fun getPlayerLoses(p: UUID): Int

        /**
         * Get player total deaths.
         * You get data from the local cache.
         */
        fun getPlayerDeaths(p: UUID): Int

        /**
         * Get player total final deaths.
         * You get data from the local cache.
         */
        fun getPlayerFinalDeaths(p: UUID): Int

        /**
         * Get player beds destroyed.
         * You get data from the local cache.
         */
        fun getPlayerBedsDestroyed(p: UUID): Int

        /**
         * Get player games played.
         * You get data from the local cache.
         */
        fun getPlayerGamesPlayed(p: UUID): Int
    }

    /**
     * Get afk system methods. It will only work if the game is started.
     */
    fun getAFKUtil(): AFKUtil

    interface AFKUtil {
        /**
         * Check if a player is AFK.
         */
        fun isPlayerAFK(player: Player): Boolean

        /**
         * Set a player afk.
         */
        fun setPlayerAFK(player: Player, value: Boolean)

        /**
         * Get the seconds since the player is AFK
         */
        fun getPlayerTimeAFK(player: Player): Int
    }

    fun getArenaUtil(): ArenaUtil

    interface ArenaUtil {
        /**
         * Check if an arena can be auto-scaled.
         *
         * @return always true if auto-scale is disabled.
         */
        fun canAutoScale(arenaName: String): Boolean

        /**
         * Add a custom arena to the enable queue.
         */
        fun addToEnableQueue(a: IArena)

        /**
         * Remove an arena from the enable queue.
         */
        fun removeFromEnableQueue(a: IArena)

        /**
         * Check if a player is playing.
         */
        fun isPlaying(p: Player): Boolean

        /**
         * Check if a player is spectating.
         */
        fun isSpectating(p: Player): Boolean

        /**
         * Load an arena.
         * Add it to the enable queue.
         *
         * @param sender If you want to send feedback. Use null otherwise.
         */
        fun loadArena(worldName: String, sender: Player?)

        /**
         * Set how many games to the next serve restart.
         * This is used only if {@link com.andrei1058.bedwars.api.server.ServerType#BUNGEE}
         */
        fun setGamesBeforeRestart(games: Int)

        /**
         * Get how many games till the next restart.
         * This is used only if {@link com.andrei1058.bedwars.api.server.ServerType#BUNGEE}
         */
        fun getGamesBeforeRestart(): Int

        /**
         * Get an arena by a player. Spectator or Player.
         *
         * @param player Target player
         * @return The arena where the player is in. Can be NULL.
         */
        fun getArenaByPlayer(player: Player): IArena

        /**
         * Set an arena by player if the player is in this arena.
         */
        fun setArenaByPlayer(p: Player, arena: IArena)

        /**
         * Remove
         */
        fun removeArenaByPlayer(p: Player, a: IArena)

        /**
         * Get an arena by world name
         *
         * @param worldName World name
         */
        fun getArenaByName(worldName: String): IArena

        fun getArenaByIdentifier(worldName: String): IArena

        fun setArenaByName(arena: IArena)

        /**
         * Remove
         */
        fun removeArenaByName(worldName: String)

        fun getArenas(): LinkedList<IArena>

        /**
         * Check if a player has vip join.
         */
        fun vipJoin(p: Player): Boolean

        /**
         * Get players count for a group
         */
        fun getPlayers(group: String): Int

        /**
         * Add a player to the most filled arena.
         * Check if is the party owner first.
         *
         * @return true if joined.
         */
        fun joinRandomArena(p: Player): Boolean

        /**
         * Add a player to the most filled arena from a group.
         *
         * @return true if added.
         */
        fun joinRandomFromGroup(p: Player, group: String): Boolean

        /**
         * Arena enable queue.
         */
        fun getEnableQueue(): LinkedList<IArena>

        /**
         * This will give the lobby items to the player.
         * Not used in serverType BUNGEE.
         * This will clear the inventory first.
         */
        fun sendLobbyCommandItems(p: Player)
    }

    fun getConfigs(): Configs

    interface Configs {
        /**
         * Get plugin main configuration.
         */
        fun getMainConfig(): ConfigManager

        /**
         * Get signs configuration.
         */
        fun getSignsConfig(): ConfigManager

        /**
         * Get generators configuration.
         */
        fun getGeneratorsConfig(): ConfigManager

        /**
         * Get shop configuration.
         */
        fun getShopConfig(): ConfigManager

        /**
         * Get upgrades configuration.
         */
        fun getUpgradesConfig(): ConfigManager
    }

    /**
     * Get shop util.
     */
    fun getShopUtil(): ShopUtil

    interface ShopUtil {
        /**
         * Get player's money amount
         */
        fun calculateMoney(player: Player, currency: Material): Int

        /**
         * Get currency as material
         *
         * @return {@link Material#AIR} if is vault.
         */
        fun getCurrency(currency: String): Material

        fun getCurrencyColor(currency: Material): ChatColor

        /**
         * Cet currency path
         */
        fun getCurrencyMsgPath(contentTier: IContentTier): String

        /**
         * Get roman number for given int.
         */
        fun getRomanNumber(n: Int): String

        /**
         * Take money from player on buy
         */
        fun takeMoney(player: Player, currency: Material, amount: Int)
    }

    /**
     * Get shop util.
     */
    fun getTeamUpgradesUtil(): TeamUpgradesUtil

    interface TeamUpgradesUtil {
        /**
         * Check if a player is watching the team upgrades menu.
         */
        fun isWatchingGUI(player: Player): Boolean

        /**
         * Set a player watching the team upgrades menu.
         */
        fun setWatchingGUI(player: Player)

        /**
         * Remove from upgrades GUI.
         */
        fun removeWatchingUpgrades(uuid: UUID)

        /**
         * Get total tiers in team upgrades to be bought in the given arena.
         * Sum of tiers in team upgrades.
         * @param arena arena
         * @return count
         */
        fun getTotalUpgradeTiers(arena: IArena): Int
    }

    /**
     * Get levels methods.
     */
    fun getLevelsUtil(): Level

    /**
     * Get party util.
     */
    fun getPartyUtil(): Party

    /**
     * Get active setup session.
     *
     * @param player player uuid.
     * @return null if no session was found.
     */
    fun getSetupSession(player: UUID): ISetupSession

    /**
     * Check if a player is in setup session.
     */
    fun isInSetupSession(player: UUID): Boolean

    /**
     * Get server type.
     **/
    fun getServerType(): ServerType

    /**
     * Get a player language iso code
     */
    fun getLangIso(p: Player): String

    /**
     * Get bedWars main command
     */
    fun getBedWarsCommand(): ParentCommand

    /**
     * Get the restore adapter.
     */
    fun getRestoreAdapter(): RestoreAdapter

    /**
     * Change the arena restore adapter.
     *
     * @param restoreAdapter your custom adapter.
     */
    @Throws(IllegalAccessError::class)
    fun setRestoreAdapter(restoreAdapter: RestoreAdapter)

    /**
     * Change the party interface.
     * You man need to unregister /party command yourself.
     */
    fun setPartyAdapter(partyAdapter: Party?)

    /**
     * Get nms operations.
     */
    fun getVersionSupport(): VersionSupport

    /**
     * Get server default language.
     */
    fun getDefaultLang(): Language

    /**
     * Get lobby world name.
     */
    fun getLobbyWorld(): String

    fun getForCurrentVersion(v18: String, v12: String, v13: String): String

    fun setLevelAdapter(level: Level)

    fun isAutoScale(): Boolean

    /**
     * Get language by iso code.
     */
    fun getLanguageByIso(isoCode: String): Language

    /**
     * Get a player language.
     */
    fun getPlayerLanguage(player: Player): Language

    /**
     * This is my standard location where to store addons configurations.
     * Make sure to create a folder in it with your addon name.
     */
    fun getAddonsPath(): File

    /**
     * Scoreboard options.
     */
    fun getScoreboardUtil(): ScoreboardUtil

    fun isShuttingDown(): Boolean

    interface ScoreboardUtil {
        /**
         * Removes bed-wars sidebar for the given player.
         */
        fun removePlayerScoreboard(player: Player)

        /**
         * Restores user scoreboard based on plugin configuration.
         * @param delay 5 seconds delay. to be used on server join.
         */
        fun givePlayerScoreboard(player: Player, delay: Boolean)
    }

    fun getScoreboardManager(): ISidebarService
}
