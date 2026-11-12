package com.andrei1058.spigot.sidebar.v1_20_R3

import com.andrei1058.spigot.sidebar.*
import com.andrei1058.spigot.sidebar.PlayerTab
import net.minecraft.network.chat.IChatBaseComponent
import net.minecraft.network.chat.IChatMutableComponent
import net.minecraft.network.protocol.game.PacketPlayOutScoreboardTeam
import net.minecraft.world.scores.ScoreboardTeam
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer
import org.bukkit.entity.Player
import com.andrei1058.spigot.sidebar.unchecked
import com.andrei1058.spigot.sidebar.uninitialized

open class PlayerListImpl(private val sidebar: WrappedSidebar, identifier: String, private val prefix: SidebarLine, private val suffix: SidebarLine, pushingRule: PlayerTab.PushingRule, nameTagVisibility: PlayerTab.NameTagVisibility, placeholders: MutableCollection<PlaceholderProvider>?) : ScoreboardTeam(null, identifier), VersionedTabGroup {
    private var pushingRule: EnumTeamPush = uninitialized()
    private var prefixComp: IChatMutableComponent = IChatBaseComponent.b(" ")
    private var suffixComp: IChatMutableComponent = IChatBaseComponent.b(" ")
    private val id: String?
    private var nameTagVisibility: EnumNameTagVisibility = EnumNameTagVisibility.a
    private var papiSubject: Player? = null
    private val placeholders: MutableCollection<PlaceholderProvider>?

    override fun b(var0: IChatBaseComponent?) {
    }

    override fun l(): EnumTeamPush {
        return pushingRule
    }

    override fun d(): IChatMutableComponent {
        return IChatBaseComponent.b(id)
    }

    override fun d(var0: IChatBaseComponent): IChatMutableComponent {
        return IChatBaseComponent.b(prefixComp.getString() + var0 + suffixComp.getString())
    }

    override fun b(): String {
        return getIdentifier().unchecked()
    }

    override fun e(): IChatBaseComponent {
        return prefixComp
    }

    override fun c(var0: IChatBaseComponent?) {
    }

    override fun f(): IChatBaseComponent {
        return suffixComp
    }

    override fun a(b: Boolean) {
    }

    override fun b(b: Boolean) {
    }

    override fun a(enumNameTagVisibility: EnumNameTagVisibility) {
        nameTagVisibility = enumNameTagVisibility
    }

    override fun j(): EnumNameTagVisibility {
        return nameTagVisibility
    }

    override fun add(player: Player) {
        val packetPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam.a(this, player.getName(), PacketPlayOutScoreboardTeam.a.a)
        sidebar.getReceivers().forEach({ r -> (r as CraftPlayer).getHandle().c.b(packetPlayOutScoreboardTeam) })
    }

    override fun sendCreateToPlayer(player: Player) {
        val packetPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam.a(this, true)
        (player as CraftPlayer).getHandle().c.b(packetPlayOutScoreboardTeam)
    }

    override fun remove(player: Player) {
        // send 4: remove entities from team
        val packetPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam.a(this, player.getName(), PacketPlayOutScoreboardTeam.a.b)
        sidebar.getReceivers().forEach({ r -> (r as CraftPlayer).getHandle().c.b(packetPlayOutScoreboardTeam) })
    }

    override fun sendUserCreateToReceivers(player: Player) {
        // send 3: add entities to team
        val packetPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam.a(this, player.getName(), PacketPlayOutScoreboardTeam.a.a)
        sidebar.getReceivers().forEach({ r -> (r as CraftPlayer).getHandle().c.b(packetPlayOutScoreboardTeam) })
    }

    override fun sendUpdateToReceivers() {
        val packetPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam.a(this, false)
        sidebar.getReceivers().forEach({ r -> (r as CraftPlayer).getHandle().c.b(packetPlayOutScoreboardTeam) })
    }

    override fun sendRemoveToReceivers() {
        val packetPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam.a(this)
        sidebar.getReceivers().forEach({ r -> (r as CraftPlayer).getHandle().c.b(packetPlayOutScoreboardTeam) })
    }

    override fun refreshContent(): Boolean {
        val newPrefix = prefix.getTrimReplacePlaceholders(getSubject(), 256, this.placeholders.unchecked())
        val newSuffix = suffix.getTrimReplacePlaceholders(getSubject(), 256, this.placeholders.unchecked())
        if (newPrefix == prefixComp.getString() && newSuffix == suffixComp.getString()) {
            return false
        }
        this.prefixComp = IChatBaseComponent.b(newPrefix)
        this.suffixComp = IChatBaseComponent.b(newSuffix)
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
