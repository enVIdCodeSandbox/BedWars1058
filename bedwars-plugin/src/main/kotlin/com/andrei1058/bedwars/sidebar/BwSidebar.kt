package com.andrei1058.bedwars.sidebar

import com.andrei1058.bedwars.BedWars
import com.andrei1058.bedwars.BedWars.Companion.config
import com.andrei1058.bedwars.BedWars.Companion.getAPI
import com.andrei1058.bedwars.BedWars.Companion.getEconomy
import com.andrei1058.bedwars.BedWars.Companion.getServerType
import com.andrei1058.bedwars.BedWars.Companion.getStatsManager
import com.andrei1058.bedwars.BedWars.Companion.level
import com.andrei1058.bedwars.BedWars.Companion.plugin
import com.andrei1058.bedwars.api.arena.GameState
import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.api.arena.NextEvent
import com.andrei1058.bedwars.api.arena.stats.DefaultStatistics
import com.andrei1058.bedwars.api.arena.team.ITeam
import com.andrei1058.bedwars.api.configuration.ConfigPath
import com.andrei1058.bedwars.api.language.Language
import com.andrei1058.bedwars.api.language.Language.Companion.getMsg
import com.andrei1058.bedwars.api.language.Language.Companion.getPlayerLanguage
import com.andrei1058.bedwars.api.language.Messages
import com.andrei1058.bedwars.api.server.ServerType
import com.andrei1058.bedwars.api.sidebar.ISidebar
import com.andrei1058.bedwars.arena.Arena
import com.andrei1058.bedwars.arena.stats.StatisticsOrdered
import com.andrei1058.bedwars.levels.internal.PlayerLevel
import com.andrei1058.bedwars.stats.PlayerStats
import com.andrei1058.spigot.sidebar.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.jetbrains.annotations.Contract
import com.andrei1058.bedwars.api.util.unchecked

open class BwSidebar(private val player: Player) : ISidebar {
    private var arena: IArena? = null
    private var handle: Sidebar? = null
    private var headerFooter: TabHeaderFooter? = null
    private val dateFormat: SimpleDateFormat
    private val nextEventDateFormat: SimpleDateFormat
    private val persistentProviders: ConcurrentLinkedQueue<PlaceholderProvider> = ConcurrentLinkedQueue()
    private val tabList: BwTabList
    @JvmField var topStatistics: StatisticsOrdered? = null

    open fun remove() {
        if (handle == null) {
            return
        }
        tabList.onSidebarRemoval()
        handle!!.remove(player)
    }

    override fun setContent(titleArray: MutableList<String>, lineArray: MutableList<String>, arena: IArena?) {
        this.arena = arena
        // dropped before the lines are parsed with it.
        val statisticsFromAnotherArena: Boolean = null != topStatistics && topStatistics.getArena() !== arena
        if (null == arena || statisticsFromAnotherArena) {
            setTopStatistics(null)
        }
        val title = this.normalizeTitle(titleArray)
        val lines = this.normalizeLines(lineArray)
        val placeholders = this.getPlaceholders(this.getPlayer())
        placeholders.addAll(this.persistentProviders)
        // if it is the first time setting content we create the handle
        if (null == handle) {
            handle = SidebarService.getInstance()!!.getSidebarHandler().createSidebar(title, lines, placeholders)
            handle!!.add(player)
        } else {
            handle!!.clearLines()
            Bukkit.getScheduler().runTaskLater(plugin, Runnable {
                ArrayList(handle!!.getPlaceholders()).forEach({ p -> handle!!.removePlaceholder(p.getPlaceholder()) })
                placeholders.forEach({ p -> handle!!.addPlaceholder(p) })
                handle!!.setTitle(title)
                lines.forEach({ l -> handle!!.addLine(l) })
            }, 2L)
        }
        tabList.handlePlayerList()
        assignTabHeaderFooter()
    }

    override fun getPlayer(): Player {
        return player
    }

    override fun normalizeTitle(titleArray: MutableList<String>?): SidebarLine {
        val data = arrayOfNulls<String>(titleArray!!.size) as Array<String>
        for (x in 0 until titleArray!!.size) {
            data[x] = titleArray!!.get(x)
        }
        return if (null == titleArray || titleArray.isEmpty()) EMPTY_TITLE else SidebarLineAnimated(data)
    }

    /**
     * Normalize lines where subject player is sidebar holder.
     */
    @Contract(pure = true)
    override fun normalizeLines(lineArray: MutableList<String>): LinkedList<SidebarLine> {
        val lines: LinkedList<SidebarLine> = LinkedList()
        var teamCount: Int = 0
        val language = Language.getPlayerLanguage(player)
        val genericTeamFormat = language.m(Messages.FORMATTING_SCOREBOARD_TEAM_GENERIC)
        val statParser: StatisticsOrdered.StringParser? = if (null == topStatistics) null else topStatistics!!.newParser()
        for (line0 in lineArray) {
            var line = line0
            // convert old placeholders
            line = line.replace("{server_ip}", "{serverIp}")
            var scoreLine: String? = null
            // generic team placeholder {team}
            if (null != arena) {
                if (line.trim() == "{team}") {
                    if (arena!!.getTeams()!!.size > teamCount) {
                        val team = arena!!.getTeams()!!.get(teamCount++)
                        val teamName = team.getDisplayName(language)
                        val teamLetter = (if (!teamName.isEmpty()) teamName[0] else "").toString()
                        line = genericTeamFormat.replace("{TeamLetter}", teamLetter).replace("{TeamColor}", team.getColor().chat().toString()).replace("{TeamName}", teamName)
                        if (line.contains("{TeamStatus}") && getAPI().getVersionSupport().getVersion() >= 10) {
                            line = line.replace("{TeamStatus}", "")
                            scoreLine = "{Team" + team.getName() + "Status}"
                        } else {
                            line = line.replace("{TeamStatus}", "{Team" + team.getName() + "Status}")
                        }
                    } else {
                        // skip line
                        continue
                    }
                }
                line = line.replace("{map}", arena!!.getDisplayName()).replace("{map_name}", arena!!.getArenaName()).replace("{group}", arena!!.getDisplayGroup(player))
                for (currentTeam in arena!!.getTeams()!!) {
                    val color = currentTeam.getColor().chat()
                    val teamName = currentTeam.getDisplayName(language)
                    val teamLetter = (if (!teamName.isEmpty()) teamName[0] else "").toString()
                    // Static team placeholders
                    line = line.replace("{Team" + currentTeam.getName() + "Color}", color.toString()).replace("{Team" + currentTeam.getName() + "Name}", teamName).replace("{Team" + currentTeam.getName() + "Letter}", teamLetter)
                    val isMember: Boolean = currentTeam.isMember(getPlayer()) || currentTeam.wasMember(getPlayer().getUniqueId())
                    if (isMember) {
                        val replacements = tabList.getTeamReplacements(currentTeam)
                        for (entry in replacements.entries) {
                            line = line.replace(entry.key, entry.value)
                        }
                    }
                }
                if (arena!!.getWinner() != null) {
                    val winnerDisplayName = arena!!.getWinner()!!.getDisplayName(Language.getPlayerLanguage(getPlayer()))
                    line = line.replace("{winnerTeamName}", winnerDisplayName).replace("{winnerTeamLetter}", arena!!.getWinner()!!.getColor().chat().toString() + (winnerDisplayName.substring(0, 1))).replace("{winnerTeamColor}", arena!!.getWinner()!!.getColor().chat().toString())
                }
                if (null != this.topStatistics && null != statParser) {
                    line = statParser.parseString(line, language, language.m(Messages.MEANING_NOBODY)).unchecked()
                    if (null == line) {
                        continue
                    }
                }
            }
            // General static placeholders
            line = line.replace("{serverIp}", BedWars.config.getString(ConfigPath.GENERAL_CONFIG_PLACEHOLDERS_REPLACEMENTS_SERVER_IP)).replace("{poweredBy}", BedWars.config.getString(ConfigPath.GENERAL_CONFIG_PLACEHOLDERS_REPLACEMENTS_POWERED_BY)).replace("{version}", plugin.getDescription().getVersion()).replace("{server}", config.getString(ConfigPath.GENERAL_CONFIGURATION_BUNGEE_OPTION_SERVER_ID))
            // Add the line to the sidebar
            val finalTemp = line
            val divided = finalTemp.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            // {comma} is a comma that does not split the line into animation frames
            for (i in 0 until divided.size) {
                divided[i] = divided[i].replace("{comma}", ",")
            }
            var sidebarLine: SidebarLine
            if (divided.size > 1) {
                sidebarLine = normalizeTitle(Arrays.asList(*divided))
            } else {
                sidebarLine = BwSidebarLine(finalTemp.replace("{comma}", ","), scoreLine)
            }
            lines.add(sidebarLine)
        }
        return lines
    }

    override fun giveUpdateTabFormat(player: Player, skipStateCheck: Boolean, spectator: Boolean?) {
        tabList.giveUpdateTabFormat(player, skipStateCheck, spectator)
    }

    override fun isTabFormattingDisabled(): Boolean {
        return tabList.isTabFormattingDisabled()
    }

    /**
     * Get placeholders for given player.
     *
     * @param player subject.
     * @return placeholders.
     */
    @Contract(pure = true)
    open fun getPlaceholders(player: Player): ConcurrentLinkedQueue<PlaceholderProvider> {
        val providers: ConcurrentLinkedQueue<PlaceholderProvider> = ConcurrentLinkedQueue()
        providers.add(PlaceholderProvider("{player}", player::getDisplayName))
        providers.add(PlaceholderProvider("{money}", { getEconomy().getMoney(player).toString() }))
        providers.add(PlaceholderProvider("{playerName}", player::getCustomName))
        providers.add(PlaceholderProvider("{date}", { dateFormat.format(Date(System.currentTimeMillis())) }))
        //        providers.add(new PlaceholderProvider("{serverIp}", () -> BedWars.config.getString(ConfigPath.GENERAL_CONFIG_PLACEHOLDERS_REPLACEMENTS_SERVER_IP)));
        providers.add(PlaceholderProvider("{version}", { plugin.getDescription().getVersion() }))
        val level: PlayerLevel? = PlayerLevel.getLevelByPlayer(player.getUniqueId())
        if (null != level) {
            providers.add(PlaceholderProvider("{progress}", level::getProgress))
            providers.add(PlaceholderProvider("{level}", { level.getLevelName().toString() }))
            providers.add(PlaceholderProvider("{levelUnformatted}", { level.getLevel().toString() }))
            providers.add(PlaceholderProvider("{currentXp}", level::getFormattedCurrentXp))
            providers.add(PlaceholderProvider("{requiredXp}", level::getFormattedRequiredXp))
        }
        if (hasNoArena()) {
            providers.add(PlaceholderProvider("{on}", { Bukkit.getOnlinePlayers().size.toString() }))
            providers.add(PlaceholderProvider("{tabStatus}", { org.bukkit.ChatColor.translateAlternateColorCodes('&', "&fOnline: &a" + Bukkit.getOnlinePlayers().size) }))
            val persistentStats: PlayerStats? = BedWars.getStatsManager().get(player.getUniqueId())
            //noinspection ConstantConditions
            if (null != persistentStats) {
                providers.add(PlaceholderProvider("{kills}", { persistentStats.getKills().toString() }))
                providers.add(PlaceholderProvider("{finalKills}", { persistentStats.getFinalKills().toString() }))
                providers.add(PlaceholderProvider("{beds}", { persistentStats.getBedsDestroyed().toString() }))
                providers.add(PlaceholderProvider("{deaths}", { persistentStats.getDeaths().toString() }))
                providers.add(PlaceholderProvider("{finalDeaths}", { persistentStats.getFinalDeaths().toString() }))
                providers.add(PlaceholderProvider("{wins}", { persistentStats.getWins().toString() }))
                providers.add(PlaceholderProvider("{losses}", { persistentStats.getLosses().toString() }))
                providers.add(PlaceholderProvider("{gamesPlayed}", { persistentStats.getGamesPlayed().toString() }))
            }
        } else {
            providers.add(PlaceholderProvider("{on}", { arena!!.getPlayers()!!.size.toString() }))
            providers.add(PlaceholderProvider("{max}", { arena!!.getMaxPlayers().toString() }))
            providers.add(PlaceholderProvider("{group}", { arena!!.getDisplayGroup(player) }))
            providers.add(PlaceholderProvider("{nextEvent}", this::getNextEventName))
            providers.add(PlaceholderProvider("{tabStatus}", this::buildTabStatus))
            if (arena!!.isSpectator(player)) {
                val lang = getPlayerLanguage(player)
                val targetFormat = lang.m(Messages.FORMAT_SPECTATOR_TARGET)
                providers.add(PlaceholderProvider("{spectatorTarget}", lambda7@{
                    if (null == player.getSpectatorTarget() || player.getSpectatorTarget() !is Player) {
                        return@lambda7 ""
                    }
                    val target = player.getSpectatorTarget() as Player
                    val targetTeam: ITeam? = arena!!.getTeam(target)
                    if (null == targetTeam) {
                        return@lambda7 ""
                    }
                    targetFormat.replace("{targetTeamColor}", targetTeam.getColor().chat().toString()).replace("{targetDisplayName}", target.getDisplayName()).replace("{targetName}", target.getDisplayName()).replace("{targetTeamName}", targetTeam.getDisplayName(lang))
                }))
            }
            providers.add(PlaceholderProvider("{time}", lambda8@{
                val status = this.arena!!.getStatus()
                if (status == GameState.playing || status == GameState.restarting) {
                    return@lambda8 getNextEventTime()
                } else {
                    if (status == GameState.starting) {
                        if (arena!!.getStartingTask() != null) {
                            return@lambda8 (arena!!.getStartingTask()!!.getCountdown() + 1).toString()
                        }
                    }
                    return@lambda8 dateFormat.format(Date(System.currentTimeMillis()))
                }
            }))
            if (null != arena!!.getStatsHolder()) {
                arena!!.getStatsHolder()!!.get(player).ifPresent({ holder ->
                    holder.getStatistic(DefaultStatistics.KILLS)!!.ifPresent({ st -> providers.add(PlaceholderProvider("{kills}", { st.getDisplayValue(null).toString() })) })
                    holder.getStatistic(DefaultStatistics.KILLS_FINAL)!!.ifPresent({ st -> providers.add(PlaceholderProvider("{finalKills}", { st.getDisplayValue(null).toString() })) })
                    holder.getStatistic(DefaultStatistics.BEDS_DESTROYED)!!.ifPresent({ st -> providers.add(PlaceholderProvider("{beds}", { st.getDisplayValue(null).toString() })) })
                    holder.getStatistic(DefaultStatistics.DEATHS)!!.ifPresent({ st -> providers.add(PlaceholderProvider("{deaths}", { st.getDisplayValue(null).toString() })) })
                })
            }
            // Dynamic team placeholders
            for (currentTeam in arena!!.getTeams()!!) {
                val isMember: Boolean = currentTeam.isMember(player) || currentTeam.wasMember(player.getUniqueId())
                providers.add(PlaceholderProvider("{Team" + currentTeam.getName() + "Status}", {
                    var result: String
                    if (currentTeam.isBedDestroyed()) {
                        if (currentTeam.getSize() > 0) {
                            result = getMsg(getPlayer(), Messages.FORMATTING_SCOREBOARD_BED_DESTROYED).replace("{remainingPlayers}", currentTeam.getSize().toString())
                        } else {
                            result = getMsg(getPlayer(), Messages.FORMATTING_SCOREBOARD_TEAM_ELIMINATED)
                        }
                    } else {
                        result = getMsg(getPlayer(), Messages.FORMATTING_SCOREBOARD_TEAM_ALIVE)
                    }
                    if (isMember) {
                        result += getMsg(getPlayer(), Messages.FORMATTING_SCOREBOARD_YOUR_TEAM)
                    }
                    result
                }))
                if (isMember) {
                    providers.add(PlaceholderProvider("{teamStatus}", lambda12@{
                        if (currentTeam.isBedDestroyed()) {
                            if (currentTeam.getSize() > 0) {
                                return@lambda12 getMsg(getPlayer(), Messages.FORMATTING_SCOREBOARD_BED_DESTROYED).replace("{remainingPlayers}", currentTeam.getSize().toString())
                            }
                            return@lambda12 getMsg(getPlayer(), Messages.FORMATTING_SCOREBOARD_TEAM_ELIMINATED)
                        }
                        getMsg(getPlayer(), Messages.FORMATTING_SCOREBOARD_TEAM_ALIVE)
                    }))
                }
            }
        }
        return providers
    }

    private fun getNextEventName(): String {
        if (arena !is Arena) {
            return "-"
        }
        val arena = this.arena as Arena
        var st = "-"
        when (arena.getNextEvent()) {
            NextEvent.EMERALD_GENERATOR_TIER_II -> {
                st = getMsg(getPlayer(), Messages.NEXT_EVENT_EMERALD_UPGRADE_II)
            }
            NextEvent.EMERALD_GENERATOR_TIER_III -> {
                st = getMsg(getPlayer(), Messages.NEXT_EVENT_EMERALD_UPGRADE_III)
            }
            NextEvent.DIAMOND_GENERATOR_TIER_II -> {
                st = getMsg(getPlayer(), Messages.NEXT_EVENT_DIAMOND_UPGRADE_II)
            }
            NextEvent.DIAMOND_GENERATOR_TIER_III -> {
                st = getMsg(getPlayer(), Messages.NEXT_EVENT_DIAMOND_UPGRADE_III)
            }
            NextEvent.GAME_END -> {
                st = getMsg(getPlayer(), Messages.NEXT_EVENT_GAME_END)
            }
            NextEvent.BEDS_DESTROY -> {
                st = getMsg(getPlayer(), Messages.NEXT_EVENT_BEDS_DESTROY)
            }
            NextEvent.ENDER_DRAGON -> {
                st = getMsg(getPlayer(), Messages.NEXT_EVENT_DRAGON_SPAWN)
            }
            else -> {}
        }
        return st
    }

    private fun getNextEventTime(): String {
        if (arena !is Arena) {
            return nextEventDateFormat.format((0L))
        }
        val arena = this.arena as Arena
        var time: Long = 0L
        when (arena.getNextEvent()) {
            NextEvent.EMERALD_GENERATOR_TIER_II, NextEvent.EMERALD_GENERATOR_TIER_III -> {
                time = (arena.upgradeEmeraldsCount) * 1000L
            }
            NextEvent.DIAMOND_GENERATOR_TIER_II, NextEvent.DIAMOND_GENERATOR_TIER_III -> {
                time = (arena.upgradeDiamondsCount) * 1000L
            }
            NextEvent.GAME_END -> {
                time = (arena.getPlayingTask()!!.getGameEndCountdown()) * 1000L
            }
            NextEvent.BEDS_DESTROY -> {
                time = (arena.getPlayingTask()!!.getBedsDestroyCountdown()) * 1000L
            }
            NextEvent.ENDER_DRAGON -> {
                time = (arena.getPlayingTask()!!.getDragonSpawnCountdown()) * 1000L
            }
            else -> {}
        }
        return if (time == 0L) "0" else nextEventDateFormat.format(Date(time))
    }

    private fun hasNoArena(): Boolean {
        return null == arena
    }

    /** One compact status line for the tab footer, in the style of the rest of the tab list. */
    private fun buildTabStatus(): String {
        if (null == arena) {
            return ""
        }
        val map = "&a" + arena.getDisplayName()
        var out: String
        if (arena.isSpectator(player)) {
            out = "&7Spectating &8| " + map
        } else {
            when (arena.getStatus()) {
                GameState.waiting -> {
                    out = "&fWaiting for players &8| &a" + arena.getPlayers().size() + "&7/&a" + arena.getMaxPlayers()
                }
                GameState.starting -> {
                    val left: String = if (arena.getStartingTask() == null) "" else (arena.getStartingTask().getCountdown() + 1).toString()
                    out = "&fStarting in &a" + left + "s &8| &a" + arena.getPlayers().size() + "&7/&a" + arena.getMaxPlayers()
                }
                GameState.playing -> {
                    val team: ITeam? = arena.getTeam(player)
                    val teamPart: String = if (null == team) "" else " &8| " + team.getColor().chat() + team.getDisplayName(getPlayerLanguage(player)) + " Team"
                    out = map + teamPart + " &8| &f" + getNextEventName() + " &a" + getNextEventTime()
                }
                else -> {
                    out = "&cGame over"
                }
            }
        }
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', out)
    }

    /** True when the game mode is enabled in the config and the given lines do not show it already. */
    private fun showGameMode(lines: MutableList<String>): Boolean {
        return config.getYml().getBoolean("scoreboard-settings.game-mode.enable", true) && lines.stream().noneMatch({ l -> l.contains("{group}") })
    }

    /** The lines with the game mode line right under the date line, or on top when there is no date. */
    private fun withGameMode(lines: MutableList<String>): MutableList<String> {
        val format: String? = config.getYml().getString("scoreboard-settings.game-mode.format", "&fMode: &a{group}")
        if (format == null || format.isEmpty()) {
            return lines
        }
        val out: MutableList<String> = ArrayList(lines)
        var at: Int = 0
        for (i in 0 until out.size()) {
            if (out.get(i).contains("{date}")) {
                at = i + 1
                break
            }
        }
        out.add(at, org.bukkit.ChatColor.translateAlternateColorCodes('&', format).replace(",", "{comma}"))
        return out
    }

    // Provide header and footer for current game state
    private fun assignTabHeaderFooter() {
        if (!config.getBoolean(ConfigPath.SB_CONFIG_TAB_HEADER_FOOTER_ENABLE)) {
            return
        }
        val lang = Language.getPlayerLanguage(player)
        var headerPath: String
        var footerPath: String
        if (hasNoArena()) {
            if (getServerType() == ServerType.SHARED) {
                this.headerFooter = null
                return
            }
            headerPath = Messages.FORMATTING_SB_TAB_LOBBY_HEADER
            footerPath = Messages.FORMATTING_SB_TAB_LOBBY_FOOTER
        } else {
            if (arena!!.isSpectator(player)) {
                val exTeam: ITeam? = arena!!.getExTeam(player.getUniqueId())
                if (null == exTeam) {
                    when (arena!!.getStatus()) {
                        GameState.waiting -> {
                            headerPath = Messages.FORMATTING_SB_TAB_WAITING_HEADER_SPEC
                            footerPath = Messages.FORMATTING_SB_TAB_WAITING_FOOTER_SPEC
                        }
                        GameState.starting -> {
                            headerPath = Messages.FORMATTING_SB_TAB_STARTING_HEADER_SPEC
                            footerPath = Messages.FORMATTING_SB_TAB_STARTING_FOOTER_SPEC
                        }
                        GameState.playing -> {
                            headerPath = Messages.FORMATTING_SB_TAB_PLAYING_SPEC_HEADER
                            footerPath = Messages.FORMATTING_SB_TAB_PLAYING_SPEC_FOOTER
                        }
                        GameState.restarting -> {
                            headerPath = Messages.FORMATTING_SB_TAB_RESTARTING_SPEC_HEADER
                            footerPath = Messages.FORMATTING_SB_TAB_RESTARTING_SPEC_FOOTER
                        }
                        else -> {
                            throw IllegalStateException("Unhandled arena status")
                        }
                    }
                } else {
                    // eliminated player
                    if (arena!!.getStatus() == GameState.restarting) {
                        if (null != arena!!.getWinner() && arena!!.getWinner() == exTeam) {
                            headerPath = Messages.FORMATTING_SB_TAB_RESTARTING_WIN2_HEADER
                            footerPath = Messages.FORMATTING_SB_TAB_RESTARTING_WIN2_FOOTER
                        } else {
                            headerPath = Messages.FORMATTING_SB_TAB_RESTARTING_ELM_HEADER
                            footerPath = Messages.FORMATTING_SB_TAB_RESTARTING_ELM_FOOTER
                        }
                    } else {
                        headerPath = Messages.FORMATTING_SB_TAB_PLAYING_ELM_HEADER
                        footerPath = Messages.FORMATTING_SB_TAB_PLAYING_ELM_FOOTER
                    }
                }
            } else {
                when (arena!!.getStatus()) {
                    GameState.waiting -> {
                        headerPath = Messages.FORMATTING_SB_TAB_WAITING_HEADER
                        footerPath = Messages.FORMATTING_SB_TAB_WAITING_FOOTER
                    }
                    GameState.starting -> {
                        headerPath = Messages.FORMATTING_SB_TAB_STARTING_HEADER
                        footerPath = Messages.FORMATTING_SB_TAB_STARTING_FOOTER
                    }
                    GameState.playing -> {
                        headerPath = Messages.FORMATTING_SB_TAB_PLAYING_HEADER
                        footerPath = Messages.FORMATTING_SB_TAB_PLAYING_FOOTER
                    }
                    GameState.restarting -> {
                        headerPath = Messages.FORMATTING_SB_TAB_RESTARTING_WIN1_HEADER
                        footerPath = Messages.FORMATTING_SB_TAB_RESTARTING_WIN1_FOOTER
                    }
                    else -> {
                        throw IllegalStateException("Unhandled arena status")
                    }
                }
            }
        }
        // empty lists fall back to the per-state texts of the language file
        var header = translateColors(config.getYml().getStringList(ConfigPath.SB_CONFIG_TAB_HEADER_FOOTER_ENABLE.replace(".enable", ".header")))
        var footer = translateColors(config.getYml().getStringList(ConfigPath.SB_CONFIG_TAB_HEADER_FOOTER_ENABLE.replace(".enable", ".footer")))
        if (header.isEmpty() && footer.isEmpty()) {
            header = lang.l(headerPath)
            footer = lang.l(footerPath)
        }
        if (null != arena && showGameMode(header) && showGameMode(footer)) {
            header = withGameMode(header)
        }
        this.headerFooter = TabHeaderFooter(this.normalizeLines(header), this.normalizeLines(footer), getPlaceholders(this.getPlayer()))
        SidebarManager.getInstance()!!.sendHeaderFooter(player, headerFooter.unchecked())
    }

    override fun registerPersistentPlaceholder(placeholderProvider: PlaceholderProvider): Boolean {
        this.persistentProviders.add(placeholderProvider)
        return true
    }

    /**
     * Hide player name tag on head when he drinks an invisibility potion.
     * This is required because not all clients hide it automatically.
     *
     * @param _toggle true when applied, false when expired.
     */
    open fun handleInvisibilityPotion(player: Player, _toggle: Boolean) {
        if (null == arena) {
            throw RuntimeException("This can only be used when the player is in arena")
        }
        this.giveUpdateTabFormat(player, false)
    }

    override fun getHandle(): Sidebar? {
        return handle
    }

    override fun getArena(): IArena? {
        return arena
    }

    open fun getHeaderFooter(): TabHeaderFooter? {
        return headerFooter
    }

    open fun setHeaderFooter(headerFooter: TabHeaderFooter?) {
        this.headerFooter = headerFooter
    }

    open fun setTopStatistics(topStatistics: StatisticsOrdered?) {
        this.topStatistics = topStatistics
    }

    init {
        nextEventDateFormat = SimpleDateFormat(getMsg(player, Messages.FORMATTING_SCOREBOARD_NEXEVENT_TIMER))
        nextEventDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"))
        // one date format for every language (config.yml, scoreboard-settings.date-format); empty = language file
        val configuredDate: String? = config.getYml().getString("scoreboard-settings.date-format", "MM/dd/yyyy")
        var chosenDate: SimpleDateFormat
        try {
            chosenDate = SimpleDateFormat(if (configuredDate == null || configuredDate.isEmpty()) getMsg(player, Messages.FORMATTING_SCOREBOARD_DATE) else configuredDate)
        } catch (invalid: IllegalArgumentException) {
            chosenDate = SimpleDateFormat(getMsg(player, Messages.FORMATTING_SCOREBOARD_DATE))
        }
        dateFormat = chosenDate
        this.tabList = BwTabList(this)
        // Persistent placeholders
        val poweredBy = BedWars.config.getString(ConfigPath.GENERAL_CONFIG_PLACEHOLDERS_REPLACEMENTS_POWERED_BY)
        this.registerPersistentPlaceholder(PlaceholderProvider("{poweredBy}", { poweredBy }))
        val serverId = config.getString(ConfigPath.GENERAL_CONFIGURATION_BUNGEE_OPTION_SERVER_ID)
        this.registerPersistentPlaceholder(PlaceholderProvider("{server}", { serverId }))
        val serverIp = BedWars.config.getString(ConfigPath.GENERAL_CONFIG_PLACEHOLDERS_REPLACEMENTS_SERVER_IP)
        this.registerPersistentPlaceholder(PlaceholderProvider("{serverIp}", { serverIp }))
    }

    companion object {
        private val EMPTY_TITLE: SidebarLine = object : SidebarLine() {
            override fun getLine(): String {
                return ""
            }
        }

        @JvmStatic
        private fun translateColors(lines: MutableList<String>): MutableList<String> {
            val out: MutableList<String> = ArrayList(lines.size())
            for (l in lines) {
                // tab header/footer lines are never animations: keep their commas
                out.add(org.bukkit.ChatColor.translateAlternateColorCodes('&', l).replace(",", "{comma}"))
            }
            return out
        }
    }
}
