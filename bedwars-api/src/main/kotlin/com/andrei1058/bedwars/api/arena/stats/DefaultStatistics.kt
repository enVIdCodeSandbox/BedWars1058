package com.andrei1058.bedwars.api.arena.stats

enum class DefaultStatistics(private val id: String, private val incrementable: Boolean) {
    KILLS("kills", true),
    KILLS_FINAL("finalKills", true),
    DEATHS("deaths", true),
    DEATHS_FINAL("finalDeaths", true),
    BEDS_DESTROYED("bedsDestroyed", true);

    override fun toString(): String {
        return id
    }

    fun isIncrementable(): Boolean {
        return incrementable
    }
}
