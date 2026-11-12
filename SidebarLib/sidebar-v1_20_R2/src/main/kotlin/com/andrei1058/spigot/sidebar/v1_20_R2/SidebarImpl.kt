package com.andrei1058.spigot.sidebar.v1_20_R2

import com.andrei1058.spigot.sidebar.*
import net.md_5.bungee.api.ChatColor
import net.minecraft.EnumChatFormat
import net.minecraft.network.chat.IChatBaseComponent
import net.minecraft.network.chat.IChatMutableComponent
import net.minecraft.network.protocol.game.PacketPlayOutScoreboardDisplayObjective
import net.minecraft.network.protocol.game.PacketPlayOutScoreboardObjective
import net.minecraft.network.protocol.game.PacketPlayOutScoreboardScore
import net.minecraft.network.protocol.game.PacketPlayOutScoreboardTeam
import net.minecraft.server.ScoreboardServer
import net.minecraft.server.network.PlayerConnection
import net.minecraft.world.scores.DisplaySlot
import net.minecraft.world.scores.ScoreboardObjective
import net.minecraft.world.scores.ScoreboardScore
import net.minecraft.world.scores.ScoreboardTeam
import net.minecraft.world.scores.criteria.IScoreboardCriteria
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftPlayer
import org.bukkit.entity.Player
import org.jetbrains.annotations.Contract

open class SidebarImpl(title: SidebarLine, lines: MutableCollection<SidebarLine>, placeholderProvider: MutableCollection<PlaceholderProvider>) : WrappedSidebar(title, lines, placeholderProvider) {
    open fun createScore(line: SidebarLine, score: Int, color: String): ScoreLine {
        return NarniaScoreLine(line, score, color)
    }

    open fun createObjective(name: String, iScoreboardCriteria: IScoreboardCriteria, title: SidebarLine, type: Int): SidebarObjective {
        return NarniaSidebarObjective(name, iScoreboardCriteria, title, type)
    }

    protected open inner class NarniaSidebarObjective(name: String, criteria: IScoreboardCriteria, @JvmField var displayName: SidebarLine, type: Int) : ScoreboardObjective(null, name, criteria, IChatBaseComponent.b(name), IScoreboardCriteria.EnumScoreboardHealthDisplay.a), SidebarObjective {
        @JvmField var displayNameComp: IChatMutableComponent = IChatBaseComponent.b(" ")
        @JvmField val type: DisplaySlot

        override fun setTitle(title: SidebarLine) {
            this.displayName = title
        }

        override fun getTitle(): SidebarLine {
            return displayName
        }

        override fun sendCreate(player: Player) {
            this.sendCreate((player as CraftPlayer).getHandle().c)
        }

        override fun sendRemove(player: Player) {
            this.sendRemove((player as CraftPlayer).getHandle().c)
        }

        override fun getName(): String {
            return this.b()
        }

        override fun refreshTitle(): Boolean {
            val newTitle = displayName.getTrimReplacePlaceholders(if (getReceivers().isEmpty()) null else getReceivers().getFirst(), 256, getPlaceholders())
            if (newTitle == displayNameComp.getString()) {
                return false
            }
            this.displayNameComp = IChatBaseComponent.b(newTitle)
            return true
        }

        override fun d(): IChatBaseComponent {
            return displayNameComp
        }

        override fun a(var0: IChatBaseComponent) {
        }

        override fun e(): IChatBaseComponent {
            return IChatBaseComponent.b((this.d().toString()))
        }

        override fun a(var0: IScoreboardCriteria.EnumScoreboardHealthDisplay) {
        }

        fun sendCreate(playerConnection: PlayerConnection) {
            val packetPlayOutScoreboardObjective = PacketPlayOutScoreboardObjective(this, 0)
            playerConnection.b(packetPlayOutScoreboardObjective)
            val packetPlayOutScoreboardDisplayObjective = PacketPlayOutScoreboardDisplayObjective(type, this)
            playerConnection.b(packetPlayOutScoreboardDisplayObjective)
            if (b().equals("health", ignoreCase = true)) {
                val packetPlayOutScoreboardDisplayObjective2 = PacketPlayOutScoreboardDisplayObjective(DisplaySlot.a, this)
                playerConnection.b(packetPlayOutScoreboardDisplayObjective2)
            }
        }

        // must be called when updating the name
        override fun sendUpdate() {
            val packetPlayOutScoreboardObjective = PacketPlayOutScoreboardObjective(this, 2)
            getReceivers().forEach({ player -> (player as CraftPlayer).getHandle().c.b(packetPlayOutScoreboardObjective) })
        }

        open fun sendRemove(playerConnection: PlayerConnection) {
            val packetPlayOutScoreboardObjective = PacketPlayOutScoreboardObjective(this, 1)
            playerConnection.b(packetPlayOutScoreboardObjective)
        }

        init {
            this.type = DisplaySlot.values()[type]
        }
    }

    open inner class NarniaScoreLine(@JvmField var text: SidebarLine, @JvmField var score: Int, color: String) : ScoreboardScore(null, getSidebarObjective() as ScoreboardObjective, color), ScoreLine, Comparable<ScoreLine> {
        @JvmField var prefix: IChatMutableComponent = IChatBaseComponent.b(" ")
        @JvmField var suffix: IChatMutableComponent = IChatBaseComponent.b(" ")
        @JvmField val team: TeamLine

        override fun getLine(): SidebarLine {
            return text
        }

        override fun setLine(line: SidebarLine) {
            this.text = line
        }

        override fun getScoreAmount(): Int {
            return score
        }

        override fun setScoreAmount(score: Int) {
            this.b(score)
        }

        override fun sendCreateToAllReceivers() {
            val packetPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam.a(team, true)
            getReceivers().forEach({ p -> (p as CraftPlayer).getHandle().c.b(packetPlayOutScoreboardTeam) })
            val packetPlayOutScoreboardScore = PacketPlayOutScoreboardScore(ScoreboardServer.Action.a, getSidebarObjective()!!.getName(), this.getColor(), this.getScoreAmount())
            getReceivers().forEach({ p -> (p as CraftPlayer).getHandle().c.b(packetPlayOutScoreboardScore) })
        }

        override fun sendCreate(player: Player) {
            val conn = (player as CraftPlayer).getHandle().c
            val packetPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam.a(team, true)
            conn.b(packetPlayOutScoreboardTeam)
            val packetPlayOutScoreboardScore = PacketPlayOutScoreboardScore(ScoreboardServer.Action.a, getSidebarObjective()!!.getName(), this.getColor(), this.getScoreAmount())
            conn.b(packetPlayOutScoreboardScore)
        }

        override fun sendRemove(player: Player) {
            val conn = (player as CraftPlayer).getHandle().c
            val packetPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam.a(team)
            val packetPlayOutScoreboardScore = PacketPlayOutScoreboardScore(ScoreboardServer.Action.b, getSidebarObjective()!!.getName(), this.getColor(), this.getScoreAmount())
            conn.b(packetPlayOutScoreboardTeam)
            conn.b(packetPlayOutScoreboardScore)
        }

        override fun sendRemoveToAllReceivers() {
            val packetPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam.a(team)
            getReceivers().forEach({ p -> (p as CraftPlayer).getHandle().c.b(packetPlayOutScoreboardTeam) })
            val packetPlayOutScoreboardScore = PacketPlayOutScoreboardScore(ScoreboardServer.Action.b, getSidebarObjective()!!.getName(), this.getColor(), this.getScoreAmount())
            getReceivers().forEach({ p -> (p as CraftPlayer).getHandle().c.b(packetPlayOutScoreboardScore) })
        }

        override fun sendUpdate(player: Player) {
            val packetTeamUpdate = PacketPlayOutScoreboardTeam.a(team, false)
            (player as CraftPlayer).getHandle().c.b(packetTeamUpdate)
        }

        @Contract(pure = true)
        override fun setContent(line: SidebarLine): Boolean {
            val oldPrefix = this.prefix
            val oldSuffix = this.suffix
            val content = line.getTrimReplacePlaceholders(if (getReceivers().isEmpty()) null else getReceivers().getFirst(), null, getPlaceholders())
            if (content.length > 256) {
                this.prefix = IChatBaseComponent.b(content.substring(0, 256))
                if (this.prefix.getString()[255] == ChatColor.COLOR_CHAR) {
                    this.prefix = IChatBaseComponent.b(content.substring(0, 255))
                    setSuffix(content.substring(255))
                } else {
                    setSuffix(content.substring(256))
                }
            } else {
                this.prefix = IChatBaseComponent.b(content)
                this.suffix = IChatBaseComponent.b("")
            }
            return oldPrefix != this.prefix || oldSuffix != this.suffix
        }

        open fun setSuffix(secondPart: String) {
            var secondPart = secondPart
            if (secondPart.isEmpty()) {
                this.suffix = IChatBaseComponent.b("")
                return
            }
            secondPart = org.bukkit.ChatColor.getLastColors(this.prefix.getString()) + secondPart
            this.suffix = IChatBaseComponent.b(if (secondPart.length > 256) secondPart.substring(0, 256) else secondPart)
        }

        override fun sendUpdateToAllReceivers() {
            val packetTeamUpdate = PacketPlayOutScoreboardTeam.a(team, false)
            getReceivers().forEach({ r -> (r as CraftPlayer).getHandle().c.b(packetTeamUpdate) })
        }

        override fun compareTo(o: ScoreLine): Int {
            return Integer.compare(score, o.getScoreAmount())
        }

        override fun b(score: Int) {
            this.score = score
            val packetPlayOutScoreboardScore = PacketPlayOutScoreboardScore(ScoreboardServer.Action.a, (getSidebarObjective() as ScoreboardObjective).b(), e(), score)
            getReceivers().forEach({ r -> (r as CraftPlayer).getHandle().c.b(packetPlayOutScoreboardScore) })
        }

        override fun b(): Int {
            return score
        }

        override fun c() {
        }

        override fun a(i: Int) {
        }

        override fun a() {
        }

        override fun getColor(): String {
            return if (team.b()[0] == ChatColor.COLOR_CHAR) team.b() else ChatColor.COLOR_CHAR.toString() + team.b()
        }

        override fun refreshContent(): Boolean {
            return setContent(getLine())
        }

        open inner class TeamLine(color: String) : ScoreboardTeam(null, color) {
            @Contract(value = " -> new", pure = true)
            override fun e(): IChatBaseComponent {
                return prefix
            }

            override fun b(var0: IChatBaseComponent?) {
            }

            override fun c(var0: IChatBaseComponent?) {
            }

            @Contract(value = " -> new", pure = true)
            override fun f(): IChatBaseComponent {
                return suffix
            }

            override fun a(var0: Boolean) {
            }

            override fun b(var0: Boolean) {
            }

            override fun a(var0: EnumNameTagVisibility) {
            }

            override fun a(var0: EnumTeamPush) {
            }

            override fun a(var0: EnumChatFormat) {
            }

            @Contract(value = "_ -> new", pure = true)
            override fun d(var0: IChatBaseComponent): IChatMutableComponent {
                return IChatBaseComponent.b(prefix.getString() + var0 + suffix.getString())
            }

            init {
                g().add(color)
            }
        }

        init {
            this.team = TeamLine(color)
        }
    }
}
