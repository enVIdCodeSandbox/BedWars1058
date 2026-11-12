package com.andrei1058.bedwars.features.mobspawn.bedwars

import com.andrei1058.bedwars.BedWars
import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.features.Feature
import org.bukkit.entity.Player

data class TeamInfo(val name: String, val color: String)

/** What MobSpawn asks of the game (arena and team of a player), straight from the BedWars1058 API. */
class BedWarsHook(@Suppress("unused") private val plugin: Feature) {

    val available: Boolean get() = true

    fun arenaOf(p: Player): IArena? = BedWars.getAPI().getArenaUtil().getArenaByPlayer(p)

    fun arenaName(p: Player): String? = arenaOf(p)?.getArenaName()

    fun isSpectating(p: Player): Boolean = BedWars.getAPI().getArenaUtil().isSpectating(p)

    fun teamName(p: Player): String? = arenaOf(p)?.getTeam(p)?.getName()

    /** Teams of the arena [p] is currently in (empty when they are not in an arena). */
    fun teamsOf(p: Player): List<TeamInfo> {
        val teams = arenaOf(p)?.getTeams() ?: return emptyList()
        val out = ArrayList<TeamInfo>()
        for (t in teams) {
            val name = t.getName() ?: continue
            out.add(TeamInfo(name, t.getColor()?.name ?: name))
        }
        return out
    }
}
