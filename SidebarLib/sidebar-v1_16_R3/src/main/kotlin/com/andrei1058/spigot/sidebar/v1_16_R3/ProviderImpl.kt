package com.andrei1058.spigot.sidebar.v1_16_R3

import com.andrei1058.spigot.sidebar.*
import net.minecraft.server.v1_16_R3.*
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer
import org.bukkit.entity.Player

open class ProviderImpl : SidebarProvider() {
    override fun createSidebar(title: SidebarLine, lines: MutableCollection<SidebarLine>, placeholderProviders: MutableCollection<PlaceholderProvider>): Sidebar {
        return SidebarImpl(title, lines, placeholderProviders)
    }

    override fun createObjective(sidebar: WrappedSidebar, name: String, health: Boolean, title: SidebarLine, type: Int): SidebarObjective {
        return (sidebar as SidebarImpl).createObjective(name, if (health) IScoreboardCriteria.HEALTH else IScoreboardCriteria.DUMMY, title, type)
    }

    override fun createScoreLine(sidebar: WrappedSidebar, line: SidebarLine, score: Int, color: String): ScoreLine {
        return (sidebar as SidebarImpl).createScore(line, score, color)
    }

    override fun sendScore(sidebar: WrappedSidebar, playerName: String, score: Int) {
        if (sidebar.getHealthObjective() == null) {
            return
        }
        val packetPlayOutScoreboardScore = PacketPlayOutScoreboardScore(ScoreboardServer.Action.CHANGE, (sidebar.getHealthObjective() as ScoreboardObjective).getName(), playerName, score)
        for (player in sidebar.getReceivers()) {
            val playerConnection = (player as CraftPlayer).getHandle().playerConnection
            playerConnection.sendPacket(packetPlayOutScoreboardScore)
        }
    }

    override fun createPlayerTab(sidebar: WrappedSidebar, identifier: String, prefix: SidebarLine, suffix: SidebarLine, pushingRule: PlayerTab.PushingRule, nameTagVisibility: PlayerTab.NameTagVisibility, placeholders: MutableCollection<PlaceholderProvider>?): VersionedTabGroup {
        return PlayerListImpl(sidebar, identifier, prefix, suffix, pushingRule, nameTagVisibility, placeholders)
    }

    override fun sendHeaderFooter(player: Player, header: String, footer: String) {
        val packet = PacketPlayOutPlayerListHeaderFooter()
        packet.header = ChatComponentText(header)
        packet.footer = ChatComponentText(footer)
        (player as CraftPlayer).getHandle().playerConnection.sendPacket(packet)
    }

    companion object {
        private var instance: SidebarProvider? = null

        @JvmStatic
        fun getInstance(): SidebarProvider {
            return instance ?: (ProviderImpl().also { instance = it })
        }
    }
}
