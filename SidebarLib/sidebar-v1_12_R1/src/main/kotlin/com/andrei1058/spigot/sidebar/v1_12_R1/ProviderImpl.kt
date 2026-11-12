package com.andrei1058.spigot.sidebar.v1_12_R1

import com.andrei1058.spigot.sidebar.*
import java.lang.reflect.Field
import java.util.LinkedList
import java.util.concurrent.ConcurrentLinkedQueue
import net.minecraft.server.v1_12_R1.*
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer
import org.bukkit.entity.Player
import com.andrei1058.spigot.sidebar.uninitialized

open class ProviderImpl : SidebarProvider() {
    private var headerM: Field? = null
    private var footerM: Field? = null
    private var scorePlayerName: Field? = null
    private var scoreObjectiveName: Field = uninitialized()
    private var scoreScore: Field = uninitialized()
    private var scoreAction: Field = uninitialized()

    override fun createSidebar(title: SidebarLine, lines: MutableCollection<SidebarLine>, placeholderProviders: MutableCollection<PlaceholderProvider>): Sidebar {
        return SidebarImpl(title, lines, placeholderProviders)
    }

    override fun createObjective(sidebar: WrappedSidebar, name: String, health: Boolean, title: SidebarLine, type: Int): SidebarObjective {
        return (sidebar as SidebarImpl).createObjective(name, if (health) ScoreboardBaseCriteria("health") else IScoreboardCriteria.b, title, type)
    }

    override fun createScoreLine(sidebar: WrappedSidebar, line: SidebarLine, score: Int, color: String): ScoreLine {
        return (sidebar as SidebarImpl).createScore(line, score, color)
    }

    override fun sendScore(sidebar: WrappedSidebar, playerName: String, score: Int) {
        if (sidebar.getHealthObjective() == null) {
            return
        }
        val packetPlayOutScoreboardScore = PacketPlayOutScoreboardScore()
        if (null == scorePlayerName) {
            try {
                scorePlayerName = PacketPlayOutScoreboardScore::class.java.getDeclaredField("a")
                scorePlayerName!!.setAccessible(true)
                scoreObjectiveName = PacketPlayOutScoreboardScore::class.java.getDeclaredField("b")
                scoreObjectiveName.setAccessible(true)
                scoreScore = PacketPlayOutScoreboardScore::class.java.getDeclaredField("c")
                scoreScore.setAccessible(true)
                scoreAction = PacketPlayOutScoreboardScore::class.java.getDeclaredField("d")
                scoreAction.setAccessible(true)
            } catch (e: NoSuchFieldException) {
                e.printStackTrace()
            }
        }
        try {
            scorePlayerName!!.set(packetPlayOutScoreboardScore, playerName)
            scoreObjectiveName.set(packetPlayOutScoreboardScore, sidebar.getHealthObjective()!!.getName())
            scoreScore.setInt(packetPlayOutScoreboardScore, score)
            scoreAction.set(packetPlayOutScoreboardScore, PacketPlayOutScoreboardScore.EnumScoreboardAction.CHANGE)
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
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
        try {
            if (null == headerM) {
                headerM = PacketPlayOutPlayerListHeaderFooter::class.java.getDeclaredField("a")
                headerM!!.setAccessible(true)
            }
            if (null == footerM) {
                footerM = PacketPlayOutPlayerListHeaderFooter::class.java.getDeclaredField("b")
                footerM!!.setAccessible(true)
            }
            headerM!!.set(packet, ChatComponentText(header))
            footerM!!.set(packet, ChatComponentText(footer))
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
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
