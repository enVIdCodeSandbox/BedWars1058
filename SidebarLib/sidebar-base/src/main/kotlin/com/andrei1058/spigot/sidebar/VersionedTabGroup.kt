package com.andrei1058.spigot.sidebar

import org.bukkit.entity.Player

interface VersionedTabGroup : PlayerTab {
    // private
    fun sendCreateToPlayer(player: Player)

    // private
    fun sendUserCreateToReceivers(player: Player)

    // private
    fun sendUpdateToReceivers()

    // private
    fun sendRemoveToReceivers()

    /**
     * Refresh contents in case of placeholders etc.
     * Used for triggering refresh a single time and then send the data to all receivers.
     * @return true if different from previous state.
     */
    fun refreshContent(): Boolean
}
