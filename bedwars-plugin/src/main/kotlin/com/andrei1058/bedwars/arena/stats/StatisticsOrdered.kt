package com.andrei1058.bedwars.arena.stats

import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.api.arena.stats.GameStatistic
import com.andrei1058.bedwars.api.arena.stats.GameStatisticProvider
import com.andrei1058.bedwars.api.arena.stats.PlayerGameStats
import com.andrei1058.bedwars.api.arena.team.ITeam
import com.andrei1058.bedwars.api.language.Language
import java.util.Optional
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import com.andrei1058.bedwars.api.util.unchecked

/**
 * This cannot be used for live tops.
 */
open class StatisticsOrdered(private val arena: IArena, orderBy: String) {
    private val ordered: MutableList<Optional<PlayerGameStats>>
    private var boundsPolicy: BoundsPolicy = BoundsPolicy.EMPTY
    private val orderBy: String

    open fun newParser(): StringParser {
        return StringParser()
    }

    /**
     * @return the arena these statistics were created for.
     */
    open fun getArena(): IArena {
        return arena
    }

    open inner class StringParser {
        @JvmField var index: Int = 0

        /**
         * @param string           string to be placeholder replaced.
         * @param emptyReplacement replace empty top position with this string.
         */
        open fun parseString(string: String, lang: Language?, emptyReplacement: String): String? {
            var string = string
            if (null == arena.getStatsHolder()) {
                return null
            }
            if (index >= ordered.size) {
                if (boundsPolicy == BoundsPolicy.SKIP) {
                    if (string.isBlank()) {
                        return string
                    }
                    var hasPlaceholders: Boolean = false
                    for (placeholder in arrayOf<String>("{topPlayerName}", "{topPlayerDisplayName}", "{topTeamColor}", "{topTeamName}", "{topValue}")) {
                        if (string.contains(placeholder)) {
                            hasPlaceholders = true
                            break
                        }
                    }
                    if (!hasPlaceholders) {
                        for (registered in arena.getStatsHolder()!!.getRegistered()) {
                            if (string.contains("{topValue-" + registered + "}")) {
                                hasPlaceholders = true
                                break
                            }
                        }
                    }
                    return if (hasPlaceholders) null else string
                }
                string = string.replace("{topPlayerName}", emptyReplacement).replace("{topPlayerDisplayName}", emptyReplacement).replace("{topTeamColor}", "").replace("{topTeamName}", "").replace("{topValue}", "{topValue-" + orderBy + "}")
                for (registered in arena.getStatsHolder()!!.getRegistered()) {
                    var displayValue: String? = "null"
                    val provider: GameStatisticProvider<*>? = arena.getStatsHolder()!!.getProvider(registered)
                    if (null != provider) {
                        displayValue = provider.getVoidReplacement(lang)
                    }
                    string = string.replace("{topValue-" + registered + "}", displayValue.unchecked())
                }
                return string
            }
            val statsOptional = ordered.get(index)
            if (statsOptional.isEmpty()) {
                return string
            }
            val stats = statsOptional.get()
            var increment: Boolean = string.contains("{topPlayerName}") || string.contains("{topPlayerDisplayName}")
            val online: Player? = Bukkit.getPlayer(stats.getPlayer())
            var team: ITeam? = if (null == online) arena.getExTeam(stats.getPlayer()) else arena.getTeam(online)
            if (null == team) {
                // if player online but eliminated
                team = arena.getExTeam(stats.getPlayer())
            }
            string = string.replace("{topPlayerName}", stats.getUsername()).replace("{topPlayerDisplayName}", stats.getDisplayPlayer()).replace("{topTeamColor}", if (null == team) "" else team.getColor().chat().toString()).replace("{topTeamName}", if (null == team) "" else team.getDisplayName(lang.unchecked())).replace("{topValue}", "{topValue-" + orderBy + "}")
            for (registered in arena.getStatsHolder()!!.getRegistered()) {
                val statistic = stats.getStatistic(registered)
                if (!increment && string.contains("{topValue-" + registered + "}")) {
                    increment = true
                }
                var displayValue: String? = statistic!!.map({ gameStatistic -> gameStatistic.getDisplayValue(lang) }).orElse(null)
                if (null == displayValue) {
                    val provider: GameStatisticProvider<*>? = arena.getStatsHolder()!!.getProvider(registered)
                    if (null == provider) {
                        displayValue = "null"
                    } else {
                        displayValue = provider.getVoidReplacement(lang)
                    }
                }
                string = string.replace("{topValue-" + registered + "}", displayValue)
            }
            if (increment) {
                index++
            }
            return string
        }

        open fun resetIndex() {
            index = 0
        }
    }

    open fun setBoundsPolicy(boundsPolicy: BoundsPolicy) {
        this.boundsPolicy = boundsPolicy
    }

    enum class BoundsPolicy {
        /**
         * Skip line. Do not send it to receivers.
         */
        SKIP,
        /**
         * Send empty line to receivers.
         */
        EMPTY;

    }

    init {
        if (null == arena.getStatsHolder()) {
            throw RuntimeException("Arena stats holder is null.")
        }
        if (!arena.getStatsHolder()!!.hasStatistic(orderBy)) {
            throw RuntimeException("Invalid order by. Provided: " + orderBy)
        }
        ordered = arena.getStatsHolder()!!.getOrderedBy(orderBy)
        this.orderBy = orderBy
    }
}
