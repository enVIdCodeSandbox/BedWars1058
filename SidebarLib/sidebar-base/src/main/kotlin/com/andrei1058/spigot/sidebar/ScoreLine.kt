package com.andrei1058.spigot.sidebar

import org.bukkit.entity.Player

interface ScoreLine : Comparable<ScoreLine> {
    fun getLine(): SidebarLine

    fun setLine(line: SidebarLine)

    fun getScoreAmount(): Int

    fun setScoreAmount(score: Int)

    fun sendCreateToAllReceivers()

    fun sendCreate(player: Player)

    /**
     * @return if content has been changed
     */
    fun setContent(line: SidebarLine): Boolean

    fun sendUpdateToAllReceivers()

    fun sendUpdate(player: Player)

    fun sendRemoveToAllReceivers()

    fun sendRemove(player: Player)

    fun getColor(): String

    /**
     * Refresh contents in case of placeholders etc.
     * Used for triggering refresh a single time and then send the data to all receivers.
     * @return true if different from previous state.
     */
    fun refreshContent(): Boolean
}
