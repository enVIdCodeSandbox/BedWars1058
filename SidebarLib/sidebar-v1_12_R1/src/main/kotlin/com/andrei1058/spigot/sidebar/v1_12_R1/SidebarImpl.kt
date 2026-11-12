package com.andrei1058.spigot.sidebar.v1_12_R1

import com.andrei1058.spigot.sidebar.*
import net.md_5.bungee.api.ChatColor
import net.minecraft.server.v1_12_R1.*
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer
import org.bukkit.entity.Player
import org.jetbrains.annotations.Contract

open class SidebarImpl(title: SidebarLine, lines: MutableCollection<SidebarLine>, placeholderProvider: MutableCollection<PlaceholderProvider>) : WrappedSidebar(title, lines, placeholderProvider) {
    open fun createScore(line: SidebarLine, score: Int, color: String): ScoreLine {
        return BucharestScoreLine(line, score, color)
    }

    open fun createObjective(name: String, iScoreboardCriteria: IScoreboardCriteria, title: SidebarLine, type: Int): SidebarObjective {
        return NarniaSidebarObjective(name, iScoreboardCriteria, title, type)
    }

    protected open inner class NarniaSidebarObjective(name: String, criteria: IScoreboardCriteria, @JvmField var displayName: SidebarLine, @JvmField val type: Int) : ScoreboardObjective(null, name, criteria), SidebarObjective {
        @JvmField var displayNameString: String = ""

        override fun e(): IScoreboardCriteria.EnumScoreboardHealthDisplay {
            return IScoreboardCriteria.EnumScoreboardHealthDisplay.INTEGER
        }

        override fun setTitle(title: SidebarLine) {
            this.displayName = title
        }

        override fun getTitle(): SidebarLine {
            return displayName
        }

        override fun sendCreate(player: Player) {
            val playerConnection = (player as CraftPlayer).getHandle().playerConnection
            val packetPlayOutScoreboardObjective = PacketPlayOutScoreboardObjective(this, 0)
            playerConnection.sendPacket(packetPlayOutScoreboardObjective)
            val packetPlayOutScoreboardDisplayObjective = PacketPlayOutScoreboardDisplayObjective(type, this)
            playerConnection.sendPacket(packetPlayOutScoreboardDisplayObjective)
            if (getName().equals("health", ignoreCase = true)) {
                val packetPlayOutScoreboardDisplayObjective2 = PacketPlayOutScoreboardDisplayObjective(0, this)
                playerConnection.sendPacket(packetPlayOutScoreboardDisplayObjective2)
            }
        }

        override fun sendRemove(player: Player) {
            val packetPlayOutScoreboardObjective = PacketPlayOutScoreboardObjective(this, 1)
            (player as CraftPlayer).getHandle().playerConnection.sendPacket(packetPlayOutScoreboardObjective)
        }

        override fun refreshTitle(): Boolean {
            val newTitle = displayName.getTrimReplacePlaceholders(if (getReceivers().size == 1) getReceivers().getFirst() else null, 16, getPlaceholders())
            if (newTitle == getDisplayName()) {
                return false
            }
            this.displayNameString = newTitle
            return true
        }

        override fun getDisplayName(): String {
            return displayNameString
        }

        override fun setDisplayName(var0: String) {
        }

        override fun sendUpdate() {
            val packetPlayOutScoreboardObjective = PacketPlayOutScoreboardObjective(this, 2)
            getReceivers().forEach({ player -> (player as CraftPlayer).getHandle().playerConnection.sendPacket(packetPlayOutScoreboardObjective) })
        }
    }

    open inner class BucharestScoreLine(@JvmField var text: SidebarLine, @JvmField var score: Int, color: String) : ScoreboardScore(null, getSidebarObjective() as ScoreboardObjective, color), ScoreLine, Comparable<ScoreLine> {
        @JvmField var prefix: String = " "
        @JvmField var suffix: String = ""
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
            this.setScore(score)
        }

        override fun sendCreateToAllReceivers() {
            val packetPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam(team, 0)
            getReceivers().forEach({ p -> (p as CraftPlayer).getHandle().playerConnection.sendPacket(packetPlayOutScoreboardTeam) })
            val packetPlayOutScoreboardScore = PacketPlayOutScoreboardScore(this)
            getReceivers().forEach({ p -> (p as CraftPlayer).getHandle().playerConnection.sendPacket(packetPlayOutScoreboardScore) })
        }

        override fun sendCreate(player: Player) {
            val conn = (player as CraftPlayer).getHandle().playerConnection
            val packetPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam(team, 0)
            conn.sendPacket(packetPlayOutScoreboardTeam)
            val packetPlayOutScoreboardScore = PacketPlayOutScoreboardScore(this)
            conn.sendPacket(packetPlayOutScoreboardScore)
        }

        override fun sendRemove(player: Player) {
            val conn = (player as CraftPlayer).getHandle().playerConnection
            val packetPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam(team, 1)
            conn.sendPacket(packetPlayOutScoreboardTeam)
            val packetPlayOutScoreboardScore = PacketPlayOutScoreboardScore(getPlayerName(), getSidebarObjective() as ScoreboardObjective)
            conn.sendPacket(packetPlayOutScoreboardScore)
        }

        override fun sendRemoveToAllReceivers() {
            val packetPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam(team, 1)
            getReceivers().forEach({ p -> (p as CraftPlayer).getHandle().playerConnection.sendPacket(packetPlayOutScoreboardTeam) })
            val packetPlayOutScoreboardScore = PacketPlayOutScoreboardScore(getPlayerName(), getSidebarObjective() as ScoreboardObjective)
            getReceivers().forEach({ p -> (p as CraftPlayer).getHandle().playerConnection.sendPacket(packetPlayOutScoreboardScore) })
        }

        override fun sendUpdate(player: Player) {
            val packetTeamUpdate = PacketPlayOutScoreboardTeam(team, 2)
            (player as CraftPlayer).getHandle().playerConnection.sendPacket(packetTeamUpdate)
        }

        @Contract(pure = true)
        override fun setContent(line: SidebarLine): Boolean {
            val content = line.getTrimReplacePlaceholders(if (getReceivers().size == 1) getReceivers().getFirst() else null, null, getPlaceholders())
            val oldPrefix = this.prefix
            val oldSuffix = this.suffix
            if (content.length > 16) {
                this.prefix = content.substring(0, 16)
                if (this.prefix[15] == ChatColor.COLOR_CHAR) {
                    this.prefix = content.substring(0, 15)
                    setSuffix(content.substring(15))
                } else {
                    setSuffix(content.substring(16))
                }
            } else {
                this.prefix = content
                this.suffix = ""
            }
            return oldPrefix != this.prefix || oldSuffix != this.suffix
        }

        open fun setSuffix(secondPart: String) {
            var secondPart = secondPart
            if (secondPart.isEmpty()) {
                this.suffix = ""
                return
            }
            secondPart = org.bukkit.ChatColor.getLastColors(this.prefix) + secondPart
            this.suffix = if (secondPart.length > 16) secondPart.substring(0, 16) else secondPart
        }

        override fun sendUpdateToAllReceivers() {
            val packetTeamUpdate = PacketPlayOutScoreboardTeam(team, 2)
            getReceivers().forEach({ r -> (r as CraftPlayer).getHandle().playerConnection.sendPacket(packetTeamUpdate) })
        }

        override fun compareTo(o: ScoreLine): Int {
            return Integer.compare(score, o.getScoreAmount())
        }

        override fun setScore(score: Int) {
            this.score = score
            val packetPlayOutScoreboardScore = PacketPlayOutScoreboardScore(this)
            getReceivers().forEach({ r -> (r as CraftPlayer).getHandle().playerConnection.sendPacket(packetPlayOutScoreboardScore) })
        }

        override fun getScore(): Int {
            return score
        }

        override fun addScore(i: Int) {
        }

        override fun incrementScore() {
        }

        override fun getColor(): String {
            return if (team.getName()[0] == ChatColor.COLOR_CHAR) team.getName() else ChatColor.COLOR_CHAR.toString() + team.getName()
        }

        override fun refreshContent(): Boolean {
            return setContent(getLine())
        }

        open inner class TeamLine(color: String) : ScoreboardTeam(null, color) {
            override fun getPrefix(): String {
                return this@BucharestScoreLine.prefix
            }

            override fun setPrefix(var0: String?) {
            }

            override fun setSuffix(var0: String?) {
            }

            override fun getSuffix(): String {
                return this@BucharestScoreLine.suffix
            }

            override fun setAllowFriendlyFire(var0: Boolean) {
            }

            override fun setCanSeeFriendlyInvisibles(var0: Boolean) {
            }

            override fun setNameTagVisibility(var0: EnumNameTagVisibility) {
            }

            override fun setCollisionRule(var0: EnumTeamPush) {
            }

            override fun setColor(var0: EnumChatFormat) {
            }

            override fun getFormattedName(var0: String): String {
                return this@BucharestScoreLine.prefix.plus(var0).plus(this@BucharestScoreLine.suffix)
            }

            init {
                getPlayerNameSet().add(color)
            }
        }

        init {
            this.team = TeamLine(color)
        }
    }
}
