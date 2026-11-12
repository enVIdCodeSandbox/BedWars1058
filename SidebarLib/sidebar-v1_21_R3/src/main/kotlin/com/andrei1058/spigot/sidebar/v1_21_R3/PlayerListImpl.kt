package com.andrei1058.spigot.sidebar.v1_21_R3

import com.andrei1058.spigot.sidebar.*
import com.andrei1058.spigot.sidebar.PlayerTab
import dev.andrei1058.spigot.sidebar.cmn1.PlayerListImplCmn1
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import net.minecraft.network.chat.IChatBaseComponent
import net.minecraft.network.chat.IChatMutableComponent
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.PacketPlayOutScoreboardTeam
import net.minecraft.world.scores.ScoreboardTeam
import org.bukkit.craftbukkit.v1_21_R3.entity.CraftPlayer
import org.bukkit.entity.Player
import org.jetbrains.annotations.Contract
import com.andrei1058.spigot.sidebar.unchecked

open class PlayerListImpl(sidebar: WrappedSidebar, identifier: String, prefix: SidebarLine, suffix: SidebarLine, pushingRule: PlayerTab.PushingRule, nameTagVisibility: PlayerTab.NameTagVisibility, placeholders: MutableCollection<PlaceholderProvider>?) : ScoreboardTeam(null, identifier), VersionedTabGroup {
    private val handle: PlayerListImplCmn1

    override fun sendCreateToPlayer(player: Player) {
        sendPacket(player, PacketPlayOutScoreboardTeam.a(this, true))
    }

    override fun sendUserCreateToReceivers(player: Player) {
        // send 3: add entities to team
        val packetPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam.a(this, player.getName(), cachedScoreboardActionA)
        handle.getSidebar().getReceivers().forEach({ r -> sendPacket(r, packetPlayOutScoreboardTeam) })
    }

    override fun sendUpdateToReceivers() {
        val packetPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam.a(this, false)
        handle.getSidebar().getReceivers().forEach({ r -> sendPacket(r, packetPlayOutScoreboardTeam) })
    }

    override fun sendRemoveToReceivers() {
        val packetPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam.a(this)
        handle.getSidebar().getReceivers().forEach({ r -> sendPacket(r, packetPlayOutScoreboardTeam) })
    }

    override fun refreshContent(): Boolean {
        return handle.refreshContent()
    }

    private fun sendPacket(player: Player, packet: Packet<*>) {
        (player as CraftPlayer).getHandle().f.b(packet)
    }

    override fun add(player: Player) {
        val packetPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam.a(this, player.getName(), cachedScoreboardActionA)
        handle.getSidebar().getReceivers().forEach({ r -> sendPacket(r, packetPlayOutScoreboardTeam) })
    }

    override fun remove(player: Player) {
        // send 4: remove entities from team
        val packetPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam.a(this, player.getName(), cachedScoreboardActionB)
        handle.getSidebar().getReceivers().forEach({ r -> sendPacket(r, packetPlayOutScoreboardTeam) })
    }

    override fun setSubject(player: Player?) {
        this.handle.setPapiSubject(player.unchecked())
    }

    override fun getSubject(): Player? {
        return this.handle.getPapiSubject()
    }

    override fun setPushingRule(rule: PlayerTab.PushingRule) {
        this.handle.setPushingRule(this.handle.toNmsPushing(rule))
        if (null != this.handle.getId()) {
            sendUpdateToReceivers()
        }
    }

    override fun setNameTagVisibility(nameTagVisibility: PlayerTab.NameTagVisibility) {
        this.handle.setNameTagVisibility(this.handle.toNmsTagVisibility(nameTagVisibility))
        if (null != this.handle.getId()) {
            sendUpdateToReceivers()
        }
    }

    override fun getIdentifier(): String? {
        return handle.getId()
    }

    override fun e(): IChatBaseComponent {
        return handle.getPrefixComp()
    }

    override fun f(): IChatBaseComponent {
        return handle.getSuffixComp()
    }

    @Contract(value = "_ -> new", pure = true)
    override fun d(var0: IChatBaseComponent): IChatMutableComponent {
        return IChatBaseComponent.b(handle.getPrefixComp().toString() + var0.getString() + handle.getSuffixComp().toString())
    }

    init {
        handle = PlayerListImplCmn1(sidebar, identifier, prefix, suffix, pushingRule, nameTagVisibility, placeholders)
        if (null == cachedScoreboardActionA) {
            cachedScoreboardActionA = getScoreboardAction("ADD") as PacketPlayOutScoreboardTeam.a
            if (null == cachedScoreboardActionA) {
                cachedScoreboardActionA = getScoreboardAction("a") as PacketPlayOutScoreboardTeam.a
            }
        }
        if (null == cachedScoreboardActionB) {
            cachedScoreboardActionB = getScoreboardAction("REMOVE") as PacketPlayOutScoreboardTeam.a
            if (null == cachedScoreboardActionB) {
                cachedScoreboardActionB = getScoreboardAction("b") as PacketPlayOutScoreboardTeam.a
            }
        }
    }

    companion object {
        private var cachedScoreboardActionA: PacketPlayOutScoreboardTeam.a? = null
        private var cachedScoreboardActionB: PacketPlayOutScoreboardTeam.a? = null

        @JvmStatic
        private fun getScoreboardAction(action: String): Any {
            try {
                val cls = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutScoreboardTeam\$a")
                for (obj in cls.getEnumConstants()) {
                    try {
                        val m = cls.getMethod("name")
                        val name = m.invoke(obj) as String
                        if (action == name) {
                            return obj
                        }
                    } catch (ignored: NoSuchMethodException) {
                    } catch (ignored: IllegalAccessException) {
                    } catch (ignored: InvocationTargetException) {
                    }
                }
            } catch (ignored: Exception) {
            }
            throw RuntimeException("Something went wrong... please report this to SidebarLib by andrei1058")
        }
    }
}
