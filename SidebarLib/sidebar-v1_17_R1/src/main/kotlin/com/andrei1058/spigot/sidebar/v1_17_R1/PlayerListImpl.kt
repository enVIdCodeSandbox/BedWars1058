package com.andrei1058.spigot.sidebar.v1_17_R1

import com.andrei1058.spigot.sidebar.*
import com.andrei1058.spigot.sidebar.PlayerTab
import net.minecraft.network.chat.ChatComponentText
import net.minecraft.network.chat.IChatBaseComponent
import net.minecraft.network.chat.IChatMutableComponent
import net.minecraft.network.protocol.game.PacketPlayOutScoreboardTeam
import net.minecraft.world.scores.ScoreboardTeam
import net.minecraft.world.scores.ScoreboardTeamBase
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer
import org.bukkit.entity.Player
import com.andrei1058.spigot.sidebar.uninitialized

open class PlayerListImpl(private val sidebar: WrappedSidebar, identifier: String, private val prefix: SidebarLine, private val suffix: SidebarLine, pushingRule: PlayerTab.PushingRule, nameTagVisibility: PlayerTab.NameTagVisibility, placeholders: MutableCollection<PlaceholderProvider>) : ScoreboardTeam(null, identifier), VersionedTabGroup {
    private var prefixComponent: ChatComponentText = ChatComponentText("")
    private var suffixComponent: ChatComponentText = ChatComponentText("")
    private val id: String?
    private var nameTagVisibility: ScoreboardTeamBase.EnumNameTagVisibility = uninitialized()
    private var papiSubject: Player? = null
    private var pushingRule: EnumTeamPush = uninitialized()
    private val placeholders: MutableCollection<PlaceholderProvider>

    override fun setPrefix(var0: IChatBaseComponent?) {
    }

    override fun getCollisionRule(): EnumTeamPush {
        return pushingRule
    }

    override fun d(): IChatMutableComponent {
        return ChatComponentText(id)
    }

    override fun getFormattedName(var0: IChatBaseComponent): IChatMutableComponent {
        return ChatComponentText(prefixComponent.h() + var0 + suffixComponent.h())
    }

    override fun getPrefix(): IChatBaseComponent {
        return prefixComponent
    }

    override fun getSuffix(): IChatBaseComponent {
        return suffixComponent
    }

    override fun setNameTagVisibility(enumNameTagVisibility: ScoreboardTeamBase.EnumNameTagVisibility) {
        nameTagVisibility = enumNameTagVisibility
    }

    override fun getNameTagVisibility(): ScoreboardTeamBase.EnumNameTagVisibility {
        return nameTagVisibility
    }

    override fun add(player: Player) {
        val packetPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam.a(this, player.getName(), PacketPlayOutScoreboardTeam.a.a)
        sidebar.getReceivers().forEach({ r -> (r as CraftPlayer).getHandle().b.sendPacket(packetPlayOutScoreboardTeam) })
    }

    override fun sendCreateToPlayer(player: Player) {
        val packetPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam.a(this, true)
        (player as CraftPlayer).getHandle().b.sendPacket(packetPlayOutScoreboardTeam)
    }

    override fun remove(player: Player) {
        // send 4: remove entities from team
        val packetPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam.a(this, player.getName(), PacketPlayOutScoreboardTeam.a.b)
        sidebar.getReceivers().forEach({ r -> (r as CraftPlayer).getHandle().b.sendPacket(packetPlayOutScoreboardTeam) })
    }

    override fun sendUserCreateToReceivers(player: Player) {
        // send 3: add entities to team
        val packetPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam.a(this, player.getName(), PacketPlayOutScoreboardTeam.a.a)
        sidebar.getReceivers().forEach({ r -> (r as CraftPlayer).getHandle().b.sendPacket(packetPlayOutScoreboardTeam) })
    }

    override fun sendUpdateToReceivers() {
        val packetPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam.a(this, false)
        sidebar.getReceivers().forEach({ r -> (r as CraftPlayer).getHandle().b.sendPacket(packetPlayOutScoreboardTeam) })
    }

    override fun sendRemoveToReceivers() {
        val packetPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam.a(this)
        sidebar.getReceivers().forEach({ r -> (r as CraftPlayer).getHandle().b.sendPacket(packetPlayOutScoreboardTeam) })
    }

    override fun refreshContent(): Boolean {
        val newPrefix = this.prefix.getTrimReplacePlaceholders(getSubject(), 16, this.placeholders)
        val newSuffix = this.suffix.getTrimReplacePlaceholders(getSubject(), 16, this.placeholders)
        if (newPrefix == prefixComponent.h() && newSuffix == suffixComponent.h()) {
            return false
        }
        this.prefixComponent = ChatComponentText(newPrefix)
        this.suffixComponent = ChatComponentText(newSuffix)
        return true
    }

    override fun getIdentifier(): String? {
        return id
    }

    override fun setSubject(papiSubject: Player?) {
        this.papiSubject = papiSubject
    }

    @org.jetbrains.annotations.Nullable
    override fun getSubject(): Player? {
        return papiSubject
    }

    override fun setPushingRule(rule: PlayerTab.PushingRule) {
        when (rule) {
            PlayerTab.PushingRule.NEVER -> this.pushingRule = EnumTeamPush.b
            PlayerTab.PushingRule.ALWAYS -> this.pushingRule = EnumTeamPush.a
            PlayerTab.PushingRule.PUSH_OTHER_TEAMS -> this.pushingRule = EnumTeamPush.c
            PlayerTab.PushingRule.PUSH_OWN_TEAM -> this.pushingRule = EnumTeamPush.d
            else -> {}
        }
        if (null != this.id) {
            sendUpdateToReceivers()
        }
    }

    override fun setNameTagVisibility(nameTagVisibility: PlayerTab.NameTagVisibility) {
        when (nameTagVisibility) {
            PlayerTab.NameTagVisibility.NEVER -> this.nameTagVisibility = EnumNameTagVisibility.b
            PlayerTab.NameTagVisibility.ALWAYS -> this.nameTagVisibility = EnumNameTagVisibility.a
            PlayerTab.NameTagVisibility.HIDE_FOR_OTHER_TEAMS -> this.nameTagVisibility = EnumNameTagVisibility.c
            PlayerTab.NameTagVisibility.HIDE_FOR_OWN_TEAM -> this.nameTagVisibility = EnumNameTagVisibility.d
            else -> {}
        }
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
