package dev.andrei1058.spigot.sidebar.cmn1

import com.andrei1058.spigot.sidebar.*
import com.andrei1058.spigot.sidebar.PlayerTab
import net.minecraft.network.chat.IChatBaseComponent
import net.minecraft.network.chat.IChatMutableComponent
import net.minecraft.world.scores.ScoreboardTeamBase
import org.bukkit.entity.Player
import com.andrei1058.spigot.sidebar.unchecked
import com.andrei1058.spigot.sidebar.uninitialized

open class PlayerListImplCmn1(private val sidebar: WrappedSidebar, identifier: String, private val prefix: SidebarLine, private val suffix: SidebarLine, pushingRule: PlayerTab.PushingRule, nameTagVisibility: PlayerTab.NameTagVisibility, placeholders: MutableCollection<PlaceholderProvider>?) {
    private var pushingRule: ScoreboardTeamBase.EnumTeamPush = uninitialized()
    private var prefixComp: IChatMutableComponent = IChatBaseComponent.b(" ")
    private var suffixComp: IChatMutableComponent = IChatBaseComponent.b(" ")
    private val id: String
    private var nameTagVisibility: ScoreboardTeamBase.EnumNameTagVisibility = ScoreboardTeamBase.EnumNameTagVisibility.a
    private var papiSubject: Player? = null
    private val placeholders: MutableCollection<PlaceholderProvider>?

    open fun toNmsPushing(rule: PlayerTab.PushingRule): ScoreboardTeamBase.EnumTeamPush {
        return when (rule) {
            PlayerTab.PushingRule.NEVER -> ScoreboardTeamBase.EnumTeamPush.b
            PlayerTab.PushingRule.ALWAYS -> ScoreboardTeamBase.EnumTeamPush.a
            PlayerTab.PushingRule.PUSH_OTHER_TEAMS -> ScoreboardTeamBase.EnumTeamPush.c
            PlayerTab.PushingRule.PUSH_OWN_TEAM -> ScoreboardTeamBase.EnumTeamPush.d
        }
    }

    open fun toNmsTagVisibility(nameTagVisibility: PlayerTab.NameTagVisibility): ScoreboardTeamBase.EnumNameTagVisibility {
        return when (nameTagVisibility) {
            PlayerTab.NameTagVisibility.NEVER -> ScoreboardTeamBase.EnumNameTagVisibility.b
            PlayerTab.NameTagVisibility.ALWAYS -> ScoreboardTeamBase.EnumNameTagVisibility.a
            PlayerTab.NameTagVisibility.HIDE_FOR_OTHER_TEAMS -> ScoreboardTeamBase.EnumNameTagVisibility.c
            PlayerTab.NameTagVisibility.HIDE_FOR_OWN_TEAM -> ScoreboardTeamBase.EnumNameTagVisibility.d
        }
    }

    open fun getPushingRule(): ScoreboardTeamBase.EnumTeamPush {
        return pushingRule
    }

    open fun setPushingRule(pushingRule: ScoreboardTeamBase.EnumTeamPush) {
        this.pushingRule = pushingRule
    }

    open fun getPrefix(): SidebarLine {
        return prefix
    }

    open fun getPrefixComp(): IChatMutableComponent {
        return prefixComp
    }

    open fun setPrefixComp(prefixComp: IChatMutableComponent) {
        this.prefixComp = prefixComp
    }

    open fun getSuffix(): SidebarLine {
        return suffix
    }

    open fun getSuffixComp(): IChatMutableComponent {
        return suffixComp
    }

    open fun setSuffixComp(suffixComp: IChatMutableComponent) {
        this.suffixComp = suffixComp
    }

    open fun getSidebar(): WrappedSidebar {
        return sidebar
    }

    open fun getId(): String {
        return id
    }

    open fun getNameTagVisibility(): ScoreboardTeamBase.EnumNameTagVisibility {
        return nameTagVisibility
    }

    open fun setNameTagVisibility(nameTagVisibility: ScoreboardTeamBase.EnumNameTagVisibility) {
        this.nameTagVisibility = nameTagVisibility
    }

    open fun getPapiSubject(): Player? {
        return papiSubject
    }

    open fun setPapiSubject(papiSubject: Player) {
        this.papiSubject = papiSubject
    }

    open fun getPlaceholders(): MutableCollection<PlaceholderProvider>? {
        return placeholders
    }

    open fun refreshContent(): Boolean {
        val newPrefix = prefix.getTrimReplacePlaceholders(papiSubject, 256, this.placeholders.unchecked())
        val newSuffix = suffix.getTrimReplacePlaceholders(papiSubject, 256, this.placeholders.unchecked())
        if (newPrefix == prefixComp.getString() && newSuffix == suffixComp.getString()) {
            return false
        }
        this.prefixComp = IChatBaseComponent.b(newPrefix)
        this.suffixComp = IChatBaseComponent.b(newSuffix)
        return true
    }

    init {
        setPushingRule(toNmsPushing(pushingRule))
        setNameTagVisibility(toNmsTagVisibility(nameTagVisibility))
        this.id = identifier
        this.placeholders = placeholders
    }
}
