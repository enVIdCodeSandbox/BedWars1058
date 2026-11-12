package com.andrei1058.bedwars.api.sidebar

import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.spigot.sidebar.PlaceholderProvider
import com.andrei1058.spigot.sidebar.Sidebar
import com.andrei1058.spigot.sidebar.SidebarLine
import org.bukkit.entity.Player

interface ISidebar {
    /**
     * Sidebar holder.
     */
    fun getPlayer(): Player

    /**
     * Coincides with the arena where the player is on.
     */
    fun getArena(): IArena?

    /**
     * Get sidebar lib handle.
     */
    fun getHandle(): Sidebar?

    /**
     * Set sidebar content.
     */
    fun setContent(titleArray: MutableList<String>, lineArray: MutableList<String>, arena: IArena?)

    /**
     * Convert an animated string to an object.
     */
    fun normalizeTitle(titleArray: MutableList<String>?): SidebarLine

    /**
     * Convert string lines to string objects.
     */
    fun normalizeLines(lineArray: MutableList<String>): MutableList<SidebarLine>

    /**
     * Will update tab prefix and suffix for the given player on current sidebar.
     *
     * @param player         format given player on current holder's sidebar.
     * @param skipStateCheck will skip checking if tab formatting is disabled.
     * @param spectator when you already know the player is a spectator. E.g. on join. Null will let the plugin whether the player is spectator or not.
     */
    fun giveUpdateTabFormat(player: Player, skipStateCheck: Boolean, spectator: Boolean?)

    /**
     * Will update tab prefix and suffix for the given player on current sidebar.
     *
     * @param player         format given player on current holder's sidebar.
     * @param skipStateCheck will skip checking if tab formatting is disabled.
     */
    fun giveUpdateTabFormat(player: Player, skipStateCheck: Boolean) {
        giveUpdateTabFormat(player, skipStateCheck, null)
    }

    /**
     * @return true if tab formatting is disabled for current sidebar/ arena stage.
     */
    @Deprecated("")
    fun isTabFormattingDisabled(): Boolean

    /**
     * Register a placeholder that is not going to be removed trough game state changes.
     */
    fun registerPersistentPlaceholder(placeholderProvider: PlaceholderProvider): Boolean
}
