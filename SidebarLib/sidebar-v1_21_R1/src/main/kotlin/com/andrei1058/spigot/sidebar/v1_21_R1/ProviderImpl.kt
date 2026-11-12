package com.andrei1058.spigot.sidebar.v1_21_R1

import com.andrei1058.spigot.sidebar.*
import java.util.Optional
import net.minecraft.network.chat.IChatBaseComponent
import net.minecraft.network.protocol.game.PacketPlayOutPlayerListHeaderFooter
import net.minecraft.network.protocol.game.PacketPlayOutScoreboardScore
import net.minecraft.server.network.PlayerConnection
import net.minecraft.world.scores.criteria.IScoreboardCriteria
import org.bukkit.craftbukkit.v1_21_R1.entity.CraftPlayer
import org.bukkit.entity.Player

open class ProviderImpl : SidebarProvider() {
    override fun createSidebar(title: SidebarLine, lines: MutableCollection<SidebarLine>, placeholderProviders: MutableCollection<PlaceholderProvider>): Sidebar {
        return SidebarImpl(title, lines, placeholderProviders)
    }

    override fun createObjective(sidebar: WrappedSidebar, name: String, health: Boolean, title: SidebarLine, type: Int): SidebarObjective {
        return (sidebar as SidebarImpl).createObjective(name, if (health) IScoreboardCriteria.f else IScoreboardCriteria.b, title, type)
    }

    override fun createScoreLine(sidebar: WrappedSidebar, line: SidebarLine, score: Int, color: String): ScoreLine {
        return (sidebar as SidebarImpl).createScore(line, score, color)
    }

    override fun sendScore(sidebar: WrappedSidebar, playerName: String, score: Int) {
        if (sidebar.getHealthObjective() == null) {
            return
        }
        val packetPlayOutScoreboardScore = PacketPlayOutScoreboardScore(playerName, sidebar.getHealthObjective()!!.getName(), score, Optional.empty(), Optional.empty())
        for (player in sidebar.getReceivers()) {
            val playerConnection = (player as CraftPlayer).getHandle().c
            playerConnection.b(packetPlayOutScoreboardScore)
        }
    }

    override fun createPlayerTab(sidebar: WrappedSidebar, identifier: String, prefix: SidebarLine, suffix: SidebarLine, pushingRule: PlayerTab.PushingRule, nameTagVisibility: PlayerTab.NameTagVisibility, placeholders: MutableCollection<PlaceholderProvider>?): VersionedTabGroup {
        return PlayerListImpl(sidebar, identifier, prefix, suffix, pushingRule, nameTagVisibility, placeholders)
    }

    override fun sendHeaderFooter(player: Player, header: String, footer: String) {
        val packet = PacketPlayOutPlayerListHeaderFooter(IChatBaseComponent.b(header), IChatBaseComponent.b(footer))
        (player as CraftPlayer).getHandle().c.b(packet)
    }

    companion object {
        private var instance: SidebarProvider? = null

        @JvmStatic
        fun getInstance(): SidebarProvider {
            return instance ?: (ProviderImpl().also { instance = it })
        }
    }
}
