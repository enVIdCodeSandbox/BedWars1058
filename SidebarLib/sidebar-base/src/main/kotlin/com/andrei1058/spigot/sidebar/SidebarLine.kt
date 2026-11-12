package com.andrei1058.spigot.sidebar

import net.md_5.bungee.api.ChatColor
import org.bukkit.entity.Player
import org.jetbrains.annotations.ApiStatus

abstract class SidebarLine {
    private var internalPlaceholders: Boolean = false
    private var papiPlaceholders: Boolean = false

    /**
     * Get static message.
     */
    abstract fun getLine(): String

    /**
     * Toggle placeholders verification.
     *
     * @param value true if it has placeholders.
     */
    @Deprecated("")
    open fun setHasPlaceholders(value: Boolean) {
        this.setInternalPlaceholders(value)
    }

    /**
     * @return if contains placeholders.
     */
    @Deprecated("")
    open fun isHasPlaceholders(): Boolean {
        return this.isInternalPlaceholders()
    }

    /**
     * Use this for tab prefix-suffix or scoreboard title.
     * @param papiSubject papi player subject.
     * @param limit char limit.
     * @param placeholders internal placeholders.
     * @return parsed string.
     */
    open fun getTrimReplacePlaceholders(papiSubject: Player?, limit: Int?, placeholders: MutableCollection<PlaceholderProvider>): String {
        return getTrimReplacePlaceholders(this.getLine(), papiSubject, limit, placeholders)
    }

    @ApiStatus.Experimental
    open fun getTrimReplacePlaceholdersScore(papiSubject: Player?, limit: Int?, placeholders: MutableCollection<PlaceholderProvider>): String {
        if (this is ScoredLine) {
            return getTrimReplacePlaceholders((this as ScoredLine).getScore().unchecked(), papiSubject, limit, placeholders)
        }
        return ""
    }

    /**
     * @return if line is PAPI dependent.
     */
    open fun isPapiPlaceholders(): Boolean {
        return papiPlaceholders
    }

    /**
     * Mark line as PAPI dependent.
     */
    open fun setPapiPlaceholders(papiPlaceholders: Boolean) {
        this.papiPlaceholders = papiPlaceholders
    }

    /**
     * @return if line is dependent on internal placeholders.
     */
    open fun isInternalPlaceholders(): Boolean {
        return internalPlaceholders
    }

    /**
     * Mark line as internal placeholder support dependent.
     */
    open fun setInternalPlaceholders(internalPlaceholders: Boolean) {
        this.internalPlaceholders = internalPlaceholders
    }

    companion object {
        /**
         * Search for placeholders and mark line as placeholder dependent.
         */
        @JvmStatic
        fun markHasPlaceholders(text: SidebarLine, placeholders: MutableCollection<PlaceholderProvider>) {
            if (text.isPapiPlaceholders() && text.isInternalPlaceholders()) {
                return
            }
            if (text is SidebarLineAnimated) {
                for (line in (text as SidebarLineAnimated).getLines()) {
                    if (SidebarManager.getInstance()!!.getPapiSupport()!!.hasPlaceholders(line)) {
                        text.setPapiPlaceholders(true)
                        break
                    }
                    for (provider in placeholders) {
                        if (text.getLine().contains(provider.getPlaceholder())) {
                            text.setInternalPlaceholders(true)
                            break
                        }
                    }
                }
            } else {
                for (provider in placeholders) {
                    if (text.getLine().contains(provider.getPlaceholder())) {
                        text.setInternalPlaceholders(true)
                    }
                }
                if (SidebarManager.getInstance()!!.getPapiSupport()!!.hasPlaceholders(text.getLine())) {
                    text.setPapiPlaceholders(true)
                }
            }
        }

        @JvmStatic
        fun getTrimReplacePlaceholders(scope: String, papiSubject: Player?, limit: Int?, placeholders: MutableCollection<PlaceholderProvider>?): String {
            var t = scope
            if (null != placeholders) {
                for (placeholderProvider in placeholders) {
                    if (t.contains(placeholderProvider.getPlaceholder())) {
                        t = t.replace(placeholderProvider.getPlaceholder(), placeholderProvider.getReplacement())
                    }
                }
            }
            if (null != papiSubject) {
                t = ChatColor.translateAlternateColorCodes('&', SidebarManager.getInstance()!!.getPapiSupport()!!.replacePlaceholders(papiSubject, t))
            }
            if (null != limit && t.length > limit) {
                t = t.substring(0, limit)
            }
            return t
        }
    }
}
