package com.andrei1058.bedwars.api.arena.stats

import com.andrei1058.bedwars.api.language.Language

/**
 * Game statistic.
 * @param <T> statistic type. We usually work with integers.
 */
interface GameStatistic<T> : Comparable<GameStatistic<T>> {
    /**
     * Current value.
     */
    fun getValue(): T

    /**
     * Value displayed in tops etc.
     * @param language - message receiver.
     */
    fun getDisplayValue(language: Language?): String

    /**
     * Comparison for tops.
     * @param o the object to be compared.
     */
    override fun compareTo(o: GameStatistic<T>): Int
}
