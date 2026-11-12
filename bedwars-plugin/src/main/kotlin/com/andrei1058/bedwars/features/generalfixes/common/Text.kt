package com.andrei1058.bedwars.features.generalfixes.common

import org.bukkit.ChatColor

object Text {
    fun color(text: String?): String = ChatColor.translateAlternateColorCodes('&', text ?: "")

    fun strip(text: String): String = ChatColor.stripColor(color(text)) ?: text
}
