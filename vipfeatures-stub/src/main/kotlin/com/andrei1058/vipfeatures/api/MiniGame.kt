package com.andrei1058.vipfeatures.api

import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

abstract class MiniGame(private val plugin: Plugin) {
    open fun getPlugin(): Plugin {
        return plugin
    }

    abstract fun isPlaying(player: Player): Boolean

    abstract fun hasBoosters(): Boolean

    abstract fun getDisplayName(): String
}
