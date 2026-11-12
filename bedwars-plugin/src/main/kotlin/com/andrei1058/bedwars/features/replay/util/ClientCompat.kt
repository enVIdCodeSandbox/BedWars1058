package com.andrei1058.bedwars.features.replay.util

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.manager.server.ServerVersion
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.UUID

/**
 * Whether a viewer's client is older than 1.13. Such clients (a 1.8 Lunar through ViaVersion, for example) cut
 * every scoreboard team prefix and suffix at 16 characters, so the sidebar draws their rows another way.
 */
object ClientCompat {
    /** playback.legacy-rows: auto (detect), always or never. */
    @Volatile
    var mode: String = "auto"

    fun isLegacy(viewer: Player): Boolean {
        when (mode.lowercase()) {
            "always" -> return true
            "never" -> return false
        }
        try {
            val v = PacketEvents.getAPI().playerManager.getClientVersion(viewer)
            if (v != null && v.isOlderThan(com.github.retrooper.packetevents.protocol.player.ClientVersion.V_1_13)) return true
        } catch (_: Throwable) {
        }
        return viaProtocol(viewer.uniqueId) in 1 until 393
    }

    /** ViaVersion's idea of the client protocol, or -1 without ViaVersion. */
    private fun viaProtocol(id: UUID): Int = try {
        if (Bukkit.getPluginManager().getPlugin("ViaVersion") == null) -1 else {
            val api = Class.forName("com.viaversion.viaversion.api.Via").getMethod("getAPI").invoke(null)
            val m = api.javaClass.getMethod("getPlayerVersion", UUID::class.java)
            m.isAccessible = true
            m.invoke(api, id) as Int
        }
    } catch (_: Throwable) {
        -1
    }
}
