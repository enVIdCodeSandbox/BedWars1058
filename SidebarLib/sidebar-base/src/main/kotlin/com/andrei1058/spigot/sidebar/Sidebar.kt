package com.andrei1058.spigot.sidebar

import org.bukkit.entity.Player

interface Sidebar {
    /**
     * Add a new line to the scoreboard.
     *
     * @param sidebarLine content.
     */
    fun addLine(sidebarLine: SidebarLine)

    /**
     * Set a line content.
     * Ignored if not exists.
     * It will send the update to players automatically.
     *
     * @param sidebarLine content.
     * @param line        position. 0 bottom.
     */
    fun setLine(sidebarLine: SidebarLine, line: Int)

    /**
     * Set scoreboard title.
     * It will send the update to players automatically.
     *
     * @param title content.
     */
    fun setTitle(title: SidebarLine)

    /**
     * Refresh scoreboard placeholders where line is not animated.
     * Can be used async.
     * Does not refresh instances of {@link SidebarLineAnimated}, use {@link #refreshAnimatedLines()} instead.
     */
    fun refreshPlaceholders()

    /**
     * Refresh scoreboard title.
     * Useful when you have an animated title.
     * Can be used async.
     */
    fun refreshTitle()

    /**
     * It is very important to call this when
     * a player logs out or before deleting a scoreboard.
     *
     * @param player uuid.
     */
    fun remove(player: Player)

    /**
     * Apply scoreboard to a player.
     *
     * @param player user.
     */
    fun add(player: Player)

    /**
     * Refresh animated lines. Title excluded.
     * Can be used async.
     */
    fun refreshAnimatedLines()

    /**
     * Remove a line from the scoreboard.
     * Ignored if line does not exist.
     * Line 0 is the first from bottom.
     * It will send the update to players automatically.
     *
     * @param line position. 0 is bottom.
     */
    fun removeLine(line: Int)

    fun clearLines()

    /**
     * @return lines amount.
     */
    fun lineCount(): Int

    /**
     * Get placeholder providers list.
     *
     * @return placeholder providers list.
     */
    fun getPlaceholders(): MutableCollection<PlaceholderProvider>

    /**
     * Update player health if shown with {@link #showPlayersHealth(SidebarLine, boolean)}.
     * @param player subject.
     * @param health amount.
     */
    fun setPlayerHealth(player: Player, health: Int)

    /**
     * Hide players health previously shown with {@link #showPlayersHealth(SidebarLine, boolean)}.
     */
    fun hidePlayersHealth()

    /**
     * Show players name.
     * @param displayName text under player name.
     * @param list show health scale on tab.
     */
    fun showPlayersHealth(displayName: SidebarLine, list: Boolean)

    /**
     * Create a new tab list layout group.
     * Players added to this group will have the same prefix-suffix.
     * @param identifier group identifier.
     * @param player initial member. PAPI subject.
     * @param prefix prefix text or animation.
     * @param suffix suffix text or animation.
     * @param pushingRule how to manage pushing.
     * @return tab group instance.
     */
    fun playerTabCreate(identifier: String, player: Player?, prefix: SidebarLine, suffix: SidebarLine, pushingRule: PlayerTab.PushingRule): PlayerTab {
        return playerTabCreate(identifier, player, prefix, suffix, pushingRule, null)
    }

    /**
     * Create a new tab list layout group.
     * Players added to this group will have the same prefix-suffix.
     * @param identifier group identifier.
     * @param player initial member. PAPI subject.
     * @param prefix prefix text or animation.
     * @param suffix suffix text or animation.
     * @param pushingRule how to manage pushing.
     * @param placeholders placeholders.
     * @return tab group instance.
     */
    fun playerTabCreate(identifier: String, player: Player?, prefix: SidebarLine, suffix: SidebarLine, pushingRule: PlayerTab.PushingRule, placeholders: MutableCollection<PlaceholderProvider>?): PlayerTab

    /**
     * Remove tab group form tab list by identifier.
     * @param identifier name.
     */
    fun removeTab(identifier: String)

    /**
     * Remove all tab lists.
     */
    fun removeTabs()

    /**
     * Refresh tab-list animations (if making use of {@link SidebarLineAnimated}).
     */
    fun playerTabRefreshAnimation()

    /**
     * Refresh bellow player-name animations (if making use of {@link SidebarLineAnimated})
     * and if health is displayed via {@link #showPlayersHealth(SidebarLine, boolean)}.
     */
    fun playerHealthRefreshAnimation()

    /**
     * Add a new placeholder provider for sidebar lines.
     * @param placeholderProvider placeholder.
     */
    fun addPlaceholder(placeholderProvider: PlaceholderProvider)

    /**
     * Remove a placeholder.
     *
     * @param placeholder placeholder to be removed.
     */
    fun removePlaceholder(placeholder: String)
}
