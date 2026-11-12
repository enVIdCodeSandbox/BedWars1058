package com.andrei1058.spigot.sidebar

import java.util.regex.Matcher
import java.util.regex.Pattern
import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.entity.Player

open class PAPIAdapter : PAPISupport {
    override fun replacePlaceholders(p: Player, s: String): String {
        return PlaceholderAPI.setPlaceholders(p, s)
    }

    override fun hasPlaceholders(s: String): Boolean {
        val pattern = Pattern.compile("%([^%]+)%")
        val matcher = pattern.matcher(s)
        return matcher.find()
    }
}
