package com.andrei1058.spigot.sidebar

import org.bukkit.entity.Player

interface PAPISupport {
    fun replacePlaceholders(p: Player, s: String): String

    fun hasPlaceholders(s: String): Boolean
}
