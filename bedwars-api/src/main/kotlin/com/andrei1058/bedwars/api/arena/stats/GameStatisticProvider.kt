package com.andrei1058.bedwars.api.arena.stats

import com.andrei1058.bedwars.api.language.Language
import org.bukkit.plugin.Plugin

interface GameStatisticProvider<T : GameStatistic<*>> {
    /**
     * Unique statistic identifier.
     */
    fun getIdentifier(): String

    /**
     * Plugin provider.
     * @return statistic owner.
     */
    fun getOwner(): Plugin

    /**
     * Default value used when initializing game stats.
     */
    fun getDefault(): T

    /**
     * Display value for undetermined values.
     * @param language desired translation.
     */
    fun getVoidReplacement(language: Language?): String
}
