package com.andrei1058.spigot.sidebar

import java.lang.reflect.InvocationTargetException
import net.md_5.bungee.api.ChatColor
import org.apache.commons.lang.StringUtils
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.jetbrains.annotations.Contract

open class SidebarManager {
    private var sidebarProvider: SidebarProvider = uninitialized()
    private var papiSupport: PAPISupport? = object : PAPISupport {
        override fun replacePlaceholders(p: Player, s: String): String {
            return s
        }

        override fun hasPlaceholders(s: String): Boolean {
            return false
        }
    }

    constructor(provider: SidebarProvider, papiSupport: PAPISupport) {
        this.sidebarProvider = provider
        this.papiSupport = papiSupport
    }

    private constructor(provider: SidebarProvider) {
        this.sidebarProvider = provider
    }

    constructor() {
        // PAPI hook
        try {
            Class.forName("me.clip.placeholderapi.PlaceholderAPI")
            papiSupport = PAPIAdapter()
        } catch (ignored: ClassNotFoundException) {
        }
        // load server version support
        val serverVersion = Bukkit.getServer().javaClass.getName().split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[3]
        val className = "com.andrei1058.spigot.sidebar." + serverVersion + ".ProviderImpl"
        try {
            val c = Class.forName(className)
            sidebarProvider = c.getDeclaredConstructor().newInstance() as SidebarProvider
        } catch (ignored: ClassNotFoundException) {
            throw InstantiationException()
        } catch (ignored: NoSuchMethodException) {
            throw InstantiationException()
        } catch (ignored: InvocationTargetException) {
            throw InstantiationException()
        } catch (ignored: InstantiationException) {
            throw InstantiationException()
        } catch (ignored: IllegalAccessException) {
            throw InstantiationException()
        }
        // only register once a provider was found, a failed attempt must not leave a half built singleton behind
        instance = this
    }

    /**
     * Create a new sidebar.
     *
     * @param title                scoreboard title.
     * @param lines                scoreboard lines.
     * @param placeholderProviders placeholders.
     * @return sb instance.
     */
    open fun createSidebar(title: SidebarLine, lines: MutableCollection<SidebarLine>, placeholderProviders: MutableCollection<PlaceholderProvider>): Sidebar {
        lines.forEach({ sidebarLine -> SidebarLine.markHasPlaceholders(sidebarLine, placeholderProviders) })
        return sidebarProvider.createSidebar(title, lines, placeholderProviders)
    }

    /**
     * Set a user header and footer in TAB.
     *
     * @param player receiver.
     * @param header header text.
     * @param footer footer text.
     */
    open fun sendHeaderFooter(player: Player, header: String, footer: String) {
        this.sidebarProvider.sendHeaderFooter(player, header, footer)
    }

    open fun sendHeaderFooter(player: Player, headerFooter: TabHeaderFooter) {
        this.sendHeaderFooter(player, buildTabContent(player, headerFooter.getHeader(), headerFooter), buildTabContent(player, headerFooter.getFooter(), headerFooter))
    }

    @Contract(pure = true)
    private fun buildTabContent(player: Player, lines: MutableList<SidebarLine>, headerFooter: TabHeaderFooter): String {
        val data = arrayOfNulls<String>(lines.size) as Array<String>
        for (i in 0 until data.size) {
            val line = lines.get(i)
            var currentLine = line.getLine()
            if (line.isInternalPlaceholders()) {
                for (placeholderProvider in headerFooter.getPlaceholders()!!) {
                    currentLine = currentLine.replace(placeholderProvider.getPlaceholder(), placeholderProvider.getReplacement())
                }
            }
            if (line.isPapiPlaceholders()) {
                currentLine = ChatColor.translateAlternateColorCodes('&', SidebarManager.getInstance()!!.getPapiSupport()!!.replacePlaceholders(player, currentLine))
            }
            data[i] = currentLine
        }
        return StringUtils.join(data, "\n")
    }

    open fun getPapiSupport(): PAPISupport? {
        return papiSupport
    }

    open fun setPapiSupport(papiSupport: PAPISupport) {
        this.papiSupport = papiSupport
    }

    open fun getSidebarProvider(): SidebarProvider {
        return sidebarProvider
    }

    open fun setSidebarProvider(sidebarProvider: SidebarProvider) {
        this.sidebarProvider = sidebarProvider
    }

    companion object {
        private var instance: SidebarManager? = null

        /**
         * Initialize the sidebar manager with a provider you supply yourself, skipping server version detection.
         * Used on servers whose CraftBukkit package is not relocated (Paper 1.20.5+), where no
         * version specific provider can be looked up by name.
         */
        @JvmStatic
        fun init(provider: SidebarProvider): SidebarManager? {
            if (null != instance) {
                return instance
            }
            var papiSupport: PAPISupport? = null
            try {
                Class.forName("me.clip.placeholderapi.PlaceholderAPI")
                papiSupport = PAPIAdapter()
            } catch (ignored: ClassNotFoundException) {
            }
            instance = if (papiSupport == null) SidebarManager(provider) else SidebarManager(provider, papiSupport)
            return instance
        }

        /**
         * Initialize sidebar manager.
         * This will detect your server version.
         */
        @JvmStatic
        fun init(): SidebarManager? {
            if (null != instance) {
                return instance
            }
            try {
                instance = SidebarManager()
            } catch (e: InstantiationException) {
                return null
            }
            return instance
        }

        @JvmStatic
        fun getInstance(): SidebarManager? {
            return instance
        }

        @JvmStatic
        fun setInstance(instance: SidebarManager) {
            SidebarManager.instance = instance
        }
    }
}
