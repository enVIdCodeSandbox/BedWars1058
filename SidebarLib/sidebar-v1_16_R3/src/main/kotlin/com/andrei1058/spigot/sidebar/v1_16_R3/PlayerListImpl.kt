package com.andrei1058.spigot.sidebar.v1_16_R3

import com.andrei1058.spigot.sidebar.*
import com.andrei1058.spigot.sidebar.PlayerTab
import java.util.Collections
import net.minecraft.server.v1_16_R3.*
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer
import org.bukkit.entity.Player
import com.andrei1058.spigot.sidebar.unchecked
import com.andrei1058.spigot.sidebar.uninitialized

open class PlayerListImpl(private val sidebar: WrappedSidebar, identifier: String, private val prefix: SidebarLine, private val suffix: SidebarLine, pushingRule: PlayerTab.PushingRule, nameTagVisibility: PlayerTab.NameTagVisibility, placeholders: MutableCollection<PlaceholderProvider>?) : ScoreboardTeam(null, identifier), VersionedTabGroup {
    private var pushingRule: EnumTeamPush = uninitialized()
    private var prefixString: ChatComponentText = ChatComponentText("")
    private var suffixString: ChatComponentText = ChatComponentText("")
    private val id: String?
    private var nameTagVisibility: ScoreboardTeamBase.EnumNameTagVisibility = uninitialized()
    private var papiSubject: Player? = null
    private val placeholders: MutableCollection<PlaceholderProvider>?

    override fun setPrefix(@javax.annotation.Nullable var0: IChatBaseComponent?) {
    }

    override fun getCollisionRule(): EnumTeamPush {
        return pushingRule
    }

    override fun d(): IChatMutableComponent {
        return ChatComponentText(id)
    }

    override fun getFormattedName(var0: IChatBaseComponent): IChatMutableComponent {
        return ChatComponentText(prefixString.h() + var0 + suffixString.h())
    }

    override fun getPrefix(): IChatBaseComponent {
        return this.prefixString
    }

    override fun getSuffix(): IChatBaseComponent {
        return this.suffixString
    }

    override fun setNameTagVisibility(enumNameTagVisibility: ScoreboardTeamBase.EnumNameTagVisibility) {
        nameTagVisibility = enumNameTagVisibility
    }

    override fun getNameTagVisibility(): ScoreboardTeamBase.EnumNameTagVisibility {
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
        val newPrefix = this.prefix.getTrimReplacePlaceholders(getSubject(), 16, this.placeholders.unchecked())
        val newSuffix = this.suffix.getTrimReplacePlaceholders(getSubject(), 16, this.placeholders.unchecked())
        if (newPrefix == prefixString.h() && newSuffix == suffixString.h()) {
            return false
        }
        this.prefixString = ChatComponentText(newPrefix)
        this.suffixString = ChatComponentText(newSuffix)
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
        this.pushingRule = EnumTeamPush.valueOf(rule.toString())
        if (null != this.id) {
            sendUpdateToReceivers()
        }
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
