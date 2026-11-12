package com.andrei1058.spigot.sidebar

import org.bukkit.entity.Player

interface PlayerTab {
    /**
     * Add a player to tab-list formatting.
     * @param player to be formatted.
     */
    fun add(player: Player)

    /**
     * Remove the given player from tab-list formatting.
     * @param player to be removed.
     */
    fun remove(player: Player)

    /**
     * PAPI subject.
     * @param player papi target for placeholders.
     */
    fun setSubject(player: Player?)

    /**
     * Get PAPI user used for placeholder replacements.
     * @return papi subject.
     */
    fun getSubject(): Player?

    /**
     * Set pushing rule for current team.
     */
    fun setPushingRule(rule: PushingRule)

    /**
     * Set name-tag visibility for current team.
     */
    fun setNameTagVisibility(nameTagVisibility: NameTagVisibility)

    /**
     * Unique identifier.
     */
    fun getIdentifier(): String?

    enum class PushingRule {
        ALWAYS,
        NEVER,
        PUSH_OTHER_TEAMS,
        PUSH_OWN_TEAM;

    }

    enum class NameTagVisibility {
        ALWAYS,
        NEVER,
        HIDE_FOR_OTHER_TEAMS,
        HIDE_FOR_OWN_TEAM;

    }
}
