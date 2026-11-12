package com.andrei1058.spigot.sidebar

import org.bukkit.entity.Player

interface SidebarObjective {
    fun setTitle(title: SidebarLine)

    fun getTitle(): SidebarLine

    fun sendCreate(player: Player)

    fun sendUpdate()

    fun sendRemove(player: Player)

    fun getName(): String

    /**
     * Refresh contents in case of placeholders etc.
     * Used for triggering refresh a single time and then send the data to all receivers.
     * @return true if different from previous state.
     */
    fun refreshTitle(): Boolean
}
