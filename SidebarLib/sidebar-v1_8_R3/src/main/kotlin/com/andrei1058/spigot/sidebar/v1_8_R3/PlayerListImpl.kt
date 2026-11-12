package com.andrei1058.spigot.sidebar.v1_8_R3

import com.andrei1058.spigot.sidebar.*
import com.andrei1058.spigot.sidebar.PlayerTab
import java.util.Collections
import net.minecraft.server.v1_8_R3.PacketPlayOutScoreboardTeam
import net.minecraft.server.v1_8_R3.ScoreboardTeam
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer
import org.bukkit.entity.Player
import com.andrei1058.spigot.sidebar.unchecked
import com.andrei1058.spigot.sidebar.uninitialized

open class PlayerListImpl(private val sidebar: WrappedSidebar, identifier: String, private val prefix: SidebarLine, private val suffix: SidebarLine, pushingRule: PlayerTab.PushingRule, nameTagVisibility: PlayerTab.NameTagVisibility, placeholders: MutableCollection<PlaceholderProvider>?) : ScoreboardTeam(null, identifier), VersionedTabGroup {
    @JvmField var prefixString: String = ""
    @JvmField var suffixString: String = ""
    private val id: String?
    private var nameTagVisibility: EnumNameTagVisibility = uninitialized()
    private var papiSubject: Player? = null
    private val placeholders: MutableCollection<PlaceholderProvider>?

    override fun setPrefix(var0: String?) {
    }

    override fun getFormattedName(var0: String): String {
        return getPrefix().plus(var0).plus(getSuffix())
    }

    override fun getPrefix(): String {
        return prefixString
    }

    override fun getSuffix(): String {
        return suffixString
    }

    override fun setNameTagVisibility(enumNameTagVisibility: EnumNameTagVisibility) {
        nameTagVisibility = enumNameTagVisibility
    }

    override fun getNameTagVisibility(): EnumNameTagVisibility {
        return nameTagVisibility
    }

    override fun add(player: Player) {
        val packetPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam(this, Collections.singleton(player.getName()), 3)
        sidebar.getReceivers().forEach({ r -> (r as CraftPlayer).getHandle().playerConnection.sendPacket(packetPlayOutScoreboardTeam) })
    }

    override fun sendCreateToPlayer(player: Player) {
        val packetPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam(this, 0)
        (player as CraftPlayer).getHandle().playerConnection.sendPacket(packetPlayOutScoreboardTeam)
    }

    override fun remove(player: Player) {
        // send 4: remove entities from team
        val packetPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam(this, Collections.singleton(player.getName()), 4)
        sidebar.getReceivers().forEach({ r -> (r as CraftPlayer).getHandle().playerConnection.sendPacket(packetPlayOutScoreboardTeam) })
    }

    override fun sendUserCreateToReceivers(player: Player) {
        // send 3: add entities to team
        val packetPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam(this, Collections.singleton(player.getName()), 3)
        sidebar.getReceivers().forEach({ r -> (r as CraftPlayer).getHandle().playerConnection.sendPacket(packetPlayOutScoreboardTeam) })
    }

    override fun sendUpdateToReceivers() {
        val packetPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam(this, 2)
        sidebar.getReceivers().forEach({ r -> (r as CraftPlayer).getHandle().playerConnection.sendPacket(packetPlayOutScoreboardTeam) })
    }

    override fun sendRemoveToReceivers() {
        val packetPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam(this, 1)
        sidebar.getReceivers().forEach({ r -> (r as CraftPlayer).getHandle().playerConnection.sendPacket(packetPlayOutScoreboardTeam) })
    }

    override fun refreshContent(): Boolean {
        val newPrefix = prefix.getTrimReplacePlaceholders(getSubject(), 16, this.placeholders.unchecked())
        val newSuffix = suffix.getTrimReplacePlaceholders(getSubject(), 16, this.placeholders.unchecked())
        if (newPrefix == prefixString && newSuffix == suffixString) {
            return false
        }
        this.prefixString = newPrefix
        this.suffixString = newSuffix
        return true
    }

    override fun getIdentifier(): String? {
        return id
    }

    override fun setSubject(@javax.annotation.Nullable papiSubject: Player?) {
        this.papiSubject = papiSubject
    }

    @org.jetbrains.annotations.Nullable
    override fun getSubject(): Player? {
        return papiSubject
    }

    override fun setPushingRule(rule: PlayerTab.PushingRule) {
    }

    override fun setNameTagVisibility(nameTagVisibility: PlayerTab.NameTagVisibility) {
        this.nameTagVisibility = EnumNameTagVisibility.valueOf(nameTagVisibility.toString())
        if (null != id) {
            sendUpdateToReceivers()
        }
    }

    init {
        this.setPushingRule(pushingRule)
        this.setNameTagVisibility(nameTagVisibility)
        this.id = identifier
        this.placeholders = placeholders
    }
}
