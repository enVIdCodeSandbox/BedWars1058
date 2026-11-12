package com.andrei1058.bedwars.features.replay.gui

import com.andrei1058.bedwars.features.replay.playback.PlaybackController
import com.andrei1058.bedwars.features.replay.settings.PlayerSettingsStore
import com.andrei1058.bedwars.features.replay.storage.ReplayRepository

/** Shared handles the menus need; set once when the plugin enables. */
object ReplayServices {
    lateinit var controller: PlaybackController
    lateinit var settings: PlayerSettingsStore
    lateinit var repo: ReplayRepository
}
