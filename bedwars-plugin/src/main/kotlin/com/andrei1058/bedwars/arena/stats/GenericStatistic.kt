package com.andrei1058.bedwars.arena.stats

import com.andrei1058.bedwars.BedWars
import com.andrei1058.bedwars.api.arena.stats.GameStatistic
import com.andrei1058.bedwars.api.arena.stats.GameStatisticProvider
import com.andrei1058.bedwars.api.arena.stats.Incrementable
import com.andrei1058.bedwars.api.language.Language
import org.bukkit.plugin.Plugin

abstract class GenericStatistic : GameStatisticProvider<GenericStatistic.Value> {
    override fun getDefault(): GenericStatistic.Value {
        return Value()
    }

    override fun getVoidReplacement(language: Language?): String {
        return "0"
    }

    override fun getOwner(): Plugin {
        return BedWars.plugin
    }

    open class Value : GameStatistic<Int>, Incrementable, Comparable<GameStatistic<Int>> {
        @JvmField var count: Int = 0

        override fun getValue(): Int {
            return count
        }

        override fun getDisplayValue(language: Language?): String {
            return getValue().toString()
        }

        override fun compareTo(o: GameStatistic<Int>): Int {
            return Integer.compare(this.count, o.getValue())
        }

        override fun increment() {
            count++
        }
    }
}
