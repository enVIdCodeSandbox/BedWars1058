/*
 * BedWars1058 - A bed wars mini-game.
 * Copyright (C) 2023 Andrei Dascălu
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

package com.andrei1058.bedwars.sidebar

import com.andrei1058.bedwars.BedWars
import com.andrei1058.bedwars.BedWars.Companion.config
import com.andrei1058.bedwars.BedWars.Companion.getChatSupport
import com.andrei1058.bedwars.BedWars.Companion.getServerType
import com.andrei1058.bedwars.BedWars.Companion.plugin
import com.andrei1058.bedwars.api.arena.GameState
import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.api.arena.team.ITeam
import com.andrei1058.bedwars.api.configuration.ConfigPath
import com.andrei1058.bedwars.api.language.Language
import com.andrei1058.bedwars.api.language.Messages
import com.andrei1058.bedwars.api.server.ServerType
import com.andrei1058.spigot.sidebar.PlayerTab
import com.andrei1058.spigot.sidebar.Sidebar
import com.andrei1058.spigot.sidebar.SidebarLine
import com.andrei1058.spigot.sidebar.SidebarLineAnimated
import java.util.*
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffectType
import com.andrei1058.bedwars.api.util.unchecked

open class BwTabList(private val sidebar: BwSidebar) {
    // Key is player uuid.
    private val deployedPerPlayerTabList: HashMap<UUID, PlayerTab> = HashMap()
    // and still let players have individual placeholders
    private val teamOrderPrefix: HashMap<UUID, String> = HashMap()
    private var teamOrderIndex: Int = 0
    // unique string used for tab ordering. Does not track team here.
    private val playerTabIdentifier: HashMap<UUID, String> = HashMap()
    // concatenated later to playerTabIdentifier
    private val playerTabIdentifierDuplication: HashMap<String, Int> = HashMap()

    /**
     * Triggered when sidebar context changes.
     * Arena/ game state change.
     */
    open fun handlePlayerList() {
        // clear existing formatted player tab-lists
        if (null != sidebar.getHandle()) {
            deployedPerPlayerTabList.clear()
            sidebar.getHandle()!!.removeTabs()
        }
        handleHealthIcon()
        if (this.isTabFormattingDisabled()) {
            return
        }
        if (null == sidebar.getArena()) {
            // if tab formatting is enabled in lobby world
            if (config.getBoolean(ConfigPath.SB_CONFIG_SIDEBAR_LIST_FORMAT_LOBBY) && !config.getLobbyWorldName().trim().isEmpty()) {
                val lobby: World? = Bukkit.getWorld(config.getLobbyWorldName())
                if (null == lobby) {
                    return
                }
                lobby.getPlayers().forEach({ inLobby -> giveUpdateTabFormat(inLobby, true, null) })
            }
            // sometimes due to timing issues player is not listed yet in lobby players
            giveUpdateTabFormat(sidebar.getPlayer(), true, null)
            return
        }
        handleHealthIcon()
        sidebar.getArena()!!.getPlayers()!!.forEach({ playing -> giveUpdateTabFormat(playing, true, null) })
        sidebar.getArena()!!.getSpectators()!!.forEach({ spectating -> giveUpdateTabFormat(spectating, true, null) })
    }

    open fun handleHealthIcon() {
        if (null == sidebar.getHandle()) {
            return
        }
        if (null == sidebar.getArena()) {
            sidebar.getHandle()!!.hidePlayersHealth()
            return
        } else if (sidebar.getArena()!!.getStatus() != GameState.playing) {
            sidebar.getHandle()!!.hidePlayersHealth()
            return
        }
        val animation = Language.getList(sidebar.getPlayer(), Messages.FORMATTING_SCOREBOARD_HEALTH)
        if (animation.isEmpty()) {
            return
        }
        var line: SidebarLine
        if (animation.size > 1) {
            val lines = arrayOfNulls<String>(animation.size) as Array<String>
            for (i in 0 until animation.size) {
                lines[i] = animation.get(i)
            }
            line = SidebarLineAnimated(lines)
        } else {
            val text = animation.get(0)
            line = object : SidebarLine() {
                override fun getLine(): String {
                    return text
                }
            }
        }
        if (config.getBoolean(ConfigPath.SB_CONFIG_SIDEBAR_HEALTH_ENABLE)) {
            sidebar.getHandle()!!.showPlayersHealth(line, config.getBoolean(ConfigPath.SB_CONFIG_SIDEBAR_HEALTH_IN_TAB))
        }
        Bukkit.getScheduler().runTaskLater(plugin, Runnable {
            if (null != sidebar.getArena() && null != sidebar.getHandle()) {
                sidebar.getArena()!!.getPlayers()!!.forEach({ player -> sidebar.getHandle()!!.setPlayerHealth(player, Math.ceil(player.getHealth()).toInt()) })
                if (sidebar.getArena()!!.isSpectator(sidebar.getPlayer())) {
                    sidebar.getArena()!!.getSpectators()!!.forEach({ player -> sidebar.getHandle()!!.setPlayerHealth(player, Math.ceil(player.getHealth()).toInt()) })
                }
            }
        }, 10L)
    }

    /**
     * @return true if tab formatting is disabled for current sidebar/ arena stage
     */
    open fun isTabFormattingDisabled(): Boolean {
        if (null == sidebar.getArena()) {
            if (getServerType() == ServerType.SHARED) {
                if (config.getBoolean(ConfigPath.SB_CONFIG_SIDEBAR_LIST_FORMAT_LOBBY) && !config.getLobbyWorldName().trim().isEmpty()) {
                    val lobby: World? = Bukkit.getWorld(config.getLobbyWorldName())
                    return null == lobby || sidebar.getPlayer().getWorld().getName() != lobby.getName()
                }
            }
            return !config.getBoolean(ConfigPath.SB_CONFIG_SIDEBAR_LIST_FORMAT_LOBBY)
        }
        val status = sidebar.getArena()!!.getStatus()
        // if tab formatting is disabled in game
        if (status == GameState.playing) {
            return !config.getBoolean(ConfigPath.SB_CONFIG_SIDEBAR_LIST_FORMAT_PLAYING)
        }
        // if tab formatting is disabled in starting
        if (status == GameState.starting) {
            return !config.getBoolean(ConfigPath.SB_CONFIG_SIDEBAR_LIST_FORMAT_STARTING)
        }
        // if tab formatting is disabled in waiting
        if (status == GameState.waiting) {
            return !config.getBoolean(ConfigPath.SB_CONFIG_SIDEBAR_LIST_FORMAT_WAITING)
        }
        // if tab formatting is disabled in restarting
        return status != GameState.restarting || !config.getBoolean(ConfigPath.SB_CONFIG_SIDEBAR_LIST_FORMAT_RESTARTING)
    }

    /**
     * Handle given player in sidebar owner tab list.
     * Will remove existing tab and give a new one based on game conditions list like spectator, team red, etc.
     * Will handle invisibility potion as well.
     */
    open fun giveUpdateTabFormat(player: Player, skipStateCheck: Boolean, spectator: Boolean?) {
        // if sidebar was not created
        if (sidebar.getHandle() == null) {
            return
        }
        // unique tab list name
        val playerTabId = getCreatePlayerTabIdentifier(player)
        // clear existing tab formatting for given player
        val playerTab: PlayerTab? = deployedPerPlayerTabList.getOrDefault(player.getUniqueId(), null)
        if (null != playerTab) {
            sidebar.getHandle()!!.removeTab(playerTab.getIdentifier().unchecked())
            deployedPerPlayerTabList.remove(player.getUniqueId())
        }
        if (!skipStateCheck) {
            if (this.isTabFormattingDisabled()) {
                return
            }
        }
        var prefix: SidebarLine
        var suffix: SidebarLine
        val arena: IArena? = sidebar.getArena()
        val handle = sidebar.getHandle()
        if (null == arena) {
            prefix = getTabText(Messages.FORMATTING_SB_TAB_LOBBY_PREFIX, player, null)
            suffix = getTabText(Messages.FORMATTING_SB_TAB_LOBBY_SUFFIX, player, null)
            val tab = handle!!.playerTabCreate(playerTabId, player, prefix, suffix, PlayerTab.PushingRule.NEVER, this.sidebar.getPlaceholders(player))
            deployedPerPlayerTabList.put(player.getUniqueId(), tab)
            return
        }
        // in-game tab has a special treatment
        if (arena.isSpectator(player) || (spectator != null && spectator)) {
            // if has been eliminated from a team
            val exTeam: ITeam? = arena.getExTeam(player.getUniqueId())
            // when player leaves but decides to join to spectate later
            if (null != exTeam) {
                val replacements = getTeamReplacements(exTeam)
                if (arena.getStatus() == GameState.restarting && null != arena.getWinner()) {
                    if (arena.getWinner() == exTeam) {
                        prefix = getTabText(Messages.FORMATTING_SB_TAB_RESTARTING_WIN2_PREFIX, player, replacements)
                        suffix = getTabText(Messages.FORMATTING_SB_TAB_RESTARTING_WIN2_SUFFIX, player, replacements)
                    } else {
                        prefix = getTabText(Messages.FORMATTING_SB_TAB_RESTARTING_ELM_PREFIX, player, replacements)
                        suffix = getTabText(Messages.FORMATTING_SB_TAB_RESTARTING_ELM_SUFFIX, player, replacements)
                    }
                } else {
                    prefix = getTabText(Messages.FORMATTING_SB_TAB_PLAYING_ELM_PREFIX, player, replacements)
                    suffix = getTabText(Messages.FORMATTING_SB_TAB_PLAYING_ELM_SUFFIX, player, replacements)
                }
                val tab = handle!!.playerTabCreate(getPlayerTabIdentifierEliminatedInTeam(exTeam, playerTabId), player, prefix, suffix, PlayerTab.PushingRule.NEVER, this.sidebar.getPlaceholders(player))
                deployedPerPlayerTabList.put(player.getUniqueId(), tab)
                return
            }
            when (arena.getStatus()) {
                GameState.waiting -> {
                    prefix = getTabText(Messages.FORMATTING_SB_TAB_WAITING_PREFIX_SPEC, player, null)
                    suffix = getTabText(Messages.FORMATTING_SB_TAB_WAITING_SUFFIX_SPEC, player, null)
                }
                GameState.starting -> {
                    prefix = getTabText(Messages.FORMATTING_SB_TAB_STARTING_PREFIX_SPEC, player, null)
                    suffix = getTabText(Messages.FORMATTING_SB_TAB_STARTING_SUFFIX_SPEC, player, null)
                }
                GameState.playing -> {
                    prefix = getTabText(Messages.FORMATTING_SB_TAB_PLAYING_SPEC_PREFIX, player, null)
                    suffix = getTabText(Messages.FORMATTING_SB_TAB_PLAYING_SPEC_SUFFIX, player, null)
                }
                GameState.restarting -> {
                    prefix = getTabText(Messages.FORMATTING_SB_TAB_RESTARTING_SPEC_PREFIX, player, null)
                    suffix = getTabText(Messages.FORMATTING_SB_TAB_RESTARTING_SPEC_SUFFIX, player, null)
                }
                else -> {
                    throw RuntimeException("Unhandled game state..")
                }
            }
            val tab = handle!!.playerTabCreate(getPlayerTabIdentifierSpectator(null, playerTabId), player, prefix, suffix, PlayerTab.PushingRule.NEVER, this.sidebar.getPlaceholders(player))
            deployedPerPlayerTabList.put(player.getUniqueId(), tab)
            return
        }
        // this is reached only by alive players
        val status = arena.getStatus()
        if (status != GameState.playing) {
            var currentTabId = playerTabId
            when (status) {
                GameState.waiting -> {
                    prefix = getTabText(Messages.FORMATTING_SB_TAB_WAITING_PREFIX, player, null)
                    suffix = getTabText(Messages.FORMATTING_SB_TAB_WAITING_SUFFIX, player, null)
                }
                GameState.starting -> {
                    prefix = getTabText(Messages.FORMATTING_SB_TAB_STARTING_PREFIX, player, null)
                    suffix = getTabText(Messages.FORMATTING_SB_TAB_STARTING_SUFFIX, player, null)
                }
                GameState.restarting -> {
                    val team = arena.getTeam(player)
                    currentTabId = getPlayerTabIdentifierAliveInTeam(team.unchecked(), playerTabId.unchecked())
                    val replacements = getTeamReplacements(team)
                    prefix = getTabText(Messages.FORMATTING_SB_TAB_RESTARTING_WIN1_PREFIX, player, replacements)
                    suffix = getTabText(Messages.FORMATTING_SB_TAB_RESTARTING_WIN1_SUFFIX, player, replacements)
                }
                else -> {
                    throw IllegalStateException("Unhandled game status!")
                }
            }
            val t = handle!!.playerTabCreate(currentTabId, player, prefix, suffix, PlayerTab.PushingRule.NEVER, this.sidebar.getPlaceholders(player))
            deployedPerPlayerTabList.put(player.getUniqueId(), t)
            return
        }
        val team = arena.getTeam(player)
        // tab list of playing state
        val replacements = getTeamReplacements(team)
        prefix = getTabText(Messages.FORMATTING_SB_TAB_PLAYING_PREFIX, player, replacements)
        suffix = getTabText(Messages.FORMATTING_SB_TAB_PLAYING_SUFFIX, player, replacements)
        val teamTab = handle!!.playerTabCreate(getPlayerTabIdentifierAliveInTeam(team.unchecked(), playerTabId.unchecked()), player, prefix, suffix, PlayerTab.PushingRule.PUSH_OTHER_TEAMS, this.sidebar.getPlaceholders(player))
        deployedPerPlayerTabList.put(player.getUniqueId(), teamTab)
        if (player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
            teamTab.setNameTagVisibility(PlayerTab.NameTagVisibility.NEVER)
        }
    }

    private fun getTabText(path: String, targetPlayer: Player, replacements: HashMap<String, String>?): SidebarLine {
        var strings = Language.getList(sidebar.getPlayer(), path)
        // the in-game tab prefix of alive players always shows the team, even if the language file does not mention it
        val ensureTeamTag: Boolean = null != replacements && path == Messages.FORMATTING_SB_TAB_PLAYING_PREFIX && strings.stream().noneMatch({ l -> l.contains("{team") })
        val teamPrefix: Boolean = null != replacements && path == Messages.FORMATTING_SB_TAB_PLAYING_PREFIX
        if (ensureTeamTag && strings.isEmpty()) {
            strings = java.util.List.of("")
        }
        if (strings.isEmpty()) {
            return object : SidebarLine() {
                override fun getLine(): String {
                    return ""
                }
            }
        }
        strings = ArrayList()
        for (string0 in Language.getList(sidebar.getPlayer(), path)) {
            var string = string0
            if (ensureTeamTag) {
                // [RANK] [TEAM] name: the team tag goes right after the rank when the prefix has one
                val tag = BedWars.config.getYml().getString("scoreboard-settings.tab-list.team-tag", "{teamLetter} ")
                string = if (string.contains("{vPrefix}")) string.replace("{vPrefix}", "{vPrefix}" + tag) else tag + string
            }
            if (teamPrefix && !string.endsWith("{teamColor}")) {
                // the last colour code of the prefix is the colour of the player's name in the tab list
                string = string + "{teamColor}"
            }
            var parsed = string.replace("{vPrefix}", BedWars.getChatSupport().getPrefix(targetPlayer)).replace("{vSuffix}", BedWars.getChatSupport().getSuffix(targetPlayer))
            if (null != replacements) {
                for (entry in replacements.entries) {
                    parsed = parsed.replace(entry.key, entry.value)
                }
            }
            strings.add(parsed)
        }
        if (strings.size == 1) {
            val line = strings.get(0)
            return object : SidebarLine() {
                override fun getLine(): String {
                    return line
                }
            }
        }
        val lines = arrayOfNulls<String>(strings.size) as Array<String>
        for (i in 0 until lines.size) {
            lines[i] = strings.get(i)
        }
        return SidebarLineAnimated(lines)
    }

    /**
     * Gets/generates a prefix string to be concatenated to the player tab-list identifier, it keeps tab-list ordered by team.
     *
     * @param team target.
     * @return prefix string.
     */
    private fun getCreateTeamTabOrderPrefix(team: ITeam): String {
        var prefix: String? = teamOrderPrefix.getOrDefault(team.getIdentity(), null)
        if (null == prefix) {
            teamOrderIndex++
            prefix = teamOrderIndex.toString() + ""
            teamOrderPrefix.put(team.getIdentity(), prefix)
            if (prefix.length > 3) {
                throw RuntimeException("Could not generate new order prefixes. Char limit exceeded. Max value is 999.")
            }
            // todo how do we clean up index? when arena became null?
        }
        return prefix
    }

    /**
     * Get existing or create player unique identifier for tab ordering
     *
     * @param player target.
     * @return unique tab identifier for given player.
     */
    private fun getCreatePlayerTabIdentifier(player: Player): String {
        var id: String? = playerTabIdentifier.getOrDefault(player.getUniqueId(), null)
        if (null == id) {
            id = player.getName().substring(0, Math.min(player.getName().length, 9))
            if (hasPlayerIdentifier(id)) {
                var lastDuplicationIndex = playerTabIdentifierDuplication.getOrDefault(id, 0)
                lastDuplicationIndex++
                id += lastDuplicationIndex.toString()
                playerTabIdentifierDuplication.put(id, lastDuplicationIndex)
            }
            playerTabIdentifier.put(player.getUniqueId(), id)
        }
        return id
    }

    private fun hasPlayerIdentifier(id: String): Boolean {
        for (string in playerTabIdentifier.values) {
            if (string == id) {
                return true
            }
        }
        return false
    }

    private fun getPlayerTabIdentifierAliveInTeam(team: ITeam, player: Player): String {
        return getPlayerTabIdentifierAliveInTeam(team, getCreatePlayerTabIdentifier(player))
    }

    private fun getPlayerTabIdentifierAliveInTeam(team: ITeam, playerId: String): String {
        return getCreateTeamTabOrderPrefix(team) + playerId
    }

    private fun getPlayerTabIdentifierEliminatedInTeam(team: ITeam, player: Player): String {
        return getPlayerTabIdentifierEliminatedInTeam(team, getCreatePlayerTabIdentifier(player))
    }

    private fun getPlayerTabIdentifierEliminatedInTeam(team: ITeam, playerId: String): String {
        return ELIMINATED_FROM_TEAM_PREFIX.toString() + getCreateTeamTabOrderPrefix(team) + playerId
    }

    private fun getPlayerTabIdentifierSpectator(team: ITeam?, player: Player): String {
        return getPlayerTabIdentifierSpectator(team, getCreatePlayerTabIdentifier(player))
    }

    private fun getPlayerTabIdentifierSpectator(team: ITeam?, playerId: String): String {
        if (null == team) {
            return SPECTATOR_PREFIX.toString() + playerId
        }
        return getPlayerTabIdentifierEliminatedInTeam(team, playerId)
    }

    open fun getTeamReplacements(team: ITeam?): HashMap<String, String> {
        val replacements: HashMap<String, String> = HashMap()
        val displayName: String = if (null == team) "" else team.getDisplayName(Language.getPlayerLanguage(sidebar.getPlayer()))
        replacements.put("{teamName}", displayName)
        replacements.put("{teamLetter}", if (null == team) "" else team.getColor().chat().toString() + (displayName.substring(0, 1)))
        replacements.put("{teamColor}", if (null == team) "" else team.getColor().chat().toString())
        // upper-case tag such as [RED], for tab list prefixes
        replacements.put("{teamTag}", if (null == team) "" else "[" + displayName.uppercase() + "]")
        return replacements
    }

    /**
     * Clear tab lines from instance.
     */
    open fun onSidebarRemoval() {
        sidebar.getHandle()!!.clearLines()
        deployedPerPlayerTabList.clear()
        playerTabIdentifier.clear()
        playerTabIdentifierDuplication.clear()
        teamOrderPrefix.clear()
        teamOrderIndex = 0
    }

    companion object {
        private const val SPECTATOR_PREFIX: Char = 'z'
        private const val ELIMINATED_FROM_TEAM_PREFIX: Char = 'z'
    }
}
