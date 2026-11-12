package com.andrei1058.bedwars.features.invisfix

import com.andrei1058.bedwars.BedWars
import com.andrei1058.bedwars.api.arena.GameState
import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.api.events.player.PlayerInvisibilityPotionEvent
import org.bukkit.Bukkit
import org.bukkit.entity.Player

/** What InvisFix asks of the game, straight from the BedWars1058 API. */
object BedWarsHook {

    fun arenaOf(p: Player): IArena? = BedWars.getAPI().getArenaUtil().getArenaByPlayer(p)

    fun isPlaying(arena: IArena): Boolean = arena.getStatus() == GameState.playing

    fun isPlayer(arena: IArena, p: Player): Boolean = arena.isPlayer(p)

    /** Team name of [p] in [arena], or "" for spectators / players without a team. */
    fun teamName(arena: IArena, p: Player): String = arena.getTeam(p)?.getName() ?: ""

    /** The core's own bookkeeping of invisible players; removing the entry stops it from expiring them later. */
    fun clearShowTime(arena: IArena, p: Player) {
        arena.getShowTime()?.remove(p)
    }

    /** Fires the core's "invisibility removed" event so it restores the player's team / tab formatting. */
    fun fireInvisRemoved(p: Player) {
        try {
            val arena = arenaOf(p) ?: return
            val team = arena.getTeam(p) ?: return
            Bukkit.getPluginManager().callEvent(PlayerInvisibilityPotionEvent(PlayerInvisibilityPotionEvent.Type.REMOVED, team, p, arena))
        } catch (t: Throwable) {
            // a listener of that event failed; the invisibility itself is already gone
        }
    }

    /** The core's armor-hiding packets (used when ProtocolLib is not installed). */
    fun setArmorHidden(victim: Player, viewer: Player, hidden: Boolean) {
        val vs = BedWars.nms
        if (hidden) vs.hideArmor(victim, viewer) else vs.showArmor(victim, viewer)
    }
}
