package com.andrei1058.spigot.sidebar

import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import net.md_5.bungee.api.ChatColor
import org.bukkit.entity.Player

abstract class WrappedSidebar(title: SidebarLine, lines: MutableCollection<SidebarLine>, placeholderProvider: MutableCollection<PlaceholderProvider>) : Sidebar {
    // sidebar lines
    private val lines: LinkedList<ScoreLine> = LinkedList()
    // who is receiving this sidebar
    private val receivers: LinkedList<Player> = LinkedList()
    // placeholders for sidebar lines
    private val placeholderProviders: MutableCollection<PlaceholderProvider> = ConcurrentLinkedQueue()
    // chat colors used for line indexing -
    private val availableColors: LinkedList<String> = LinkedList()
    // sidebar lines objective
    private val sidebarObjective: SidebarObjective?
    // bellow player name health objective (will show numbers in tab as well)
    private var healthObjective: SidebarObjective? = null
    // player tab formatting
    private val tabView: LinkedList<VersionedTabGroup> = LinkedList()

    override fun setTitle(title: SidebarLine) {
        SidebarLine.markHasPlaceholders(title, getPlaceholders())
        this.sidebarObjective!!.setTitle(title)
        this.sidebarObjective!!.sendUpdate()
    }

    override fun addPlaceholder(placeholderProvider: PlaceholderProvider) {
        placeholderProviders.remove(placeholderProvider)
        placeholderProviders.add(placeholderProvider)
        val placeholder: ConcurrentLinkedQueue<PlaceholderProvider> = ConcurrentLinkedQueue()
        placeholder.add(placeholderProvider)
        for (line in lines) {
            SidebarLine.markHasPlaceholders(line.getLine(), placeholder)
        }
        if (null != this.sidebarObjective) {
            SidebarLine.markHasPlaceholders(this.sidebarObjective.getTitle(), placeholder)
        }
    }

    /**
     * @return -1 if no more lines can be added.
     */
    private fun getAvailableScore(): Int {
        if (this.lines.isEmpty()) {
            return 0
        }
        if (this.lines.size == 16) {
            return -1
        }
        return this.lines.getFirst().getScoreAmount()
    }

    // used when adding/ removing a line
    private fun order() {
        Collections.sort(this.lines)
    }

    override fun addLine(sidebarLine: SidebarLine) {
        val score: Int = getAvailableScore()
        if (score == -1) {
            return
        }
        if (availableColors.isEmpty()) {
            return
        }
        scoreOffsetIncrease(this.lines)
        val color = availableColors.removeFirst()
        SidebarLine.markHasPlaceholders(sidebarLine, getPlaceholders())
        val s = SidebarManager.getInstance()!!.getSidebarProvider().createScoreLine(this, sidebarLine, if (score == 0) score else score - 1, color)
        s.refreshContent()
        s.sendCreateToAllReceivers()
        this.lines.add(s)
        order()
    }

    override fun setLine(sidebarLine: SidebarLine, line: Int) {
        if (line >= 0 && line < this.lines.size) {
            val s = this.lines.get(line)
            SidebarLine.markHasPlaceholders(s.getLine(), getPlaceholders())
            s.setLine(sidebarLine)
        }
    }

    override fun add(player: Player) {
        sidebarObjective!!.sendCreate(player)
        this.lines.forEach({ line -> line.sendCreate(player) })
        if (healthObjective != null) {
            healthObjective!!.sendCreate(player)
            this.tabView.forEach({ tab -> tab.sendCreateToPlayer(player) })
        }
        this.receivers.add(player)
    }

    override fun refreshPlaceholders() {
        for (line in this.lines) {
            if (line.getLine() !is SidebarLineAnimated) {
                if (line.refreshContent()) {
                    line.sendUpdateToAllReceivers()
                }
            }
        }
    }

    override fun refreshTitle() {
        if (this.sidebarObjective!!.refreshTitle()) {
            this.sidebarObjective!!.sendUpdate()
        }
    }

    override fun refreshAnimatedLines() {
        for (line in lines) {
            if (line.getLine() is SidebarLineAnimated) {
                if (line.refreshContent()) {
                    line.sendUpdateToAllReceivers()
                }
            }
        }
    }

    override fun removeLine(line: Int) {
        if (line >= 0 && line < this.lines.size) {
            val scoreLine = this.lines.get(line)
            this.lines.removeAt(line)
            scoreLine.sendRemoveToAllReceivers()
            this.restoreColor(scoreLine.getColor())
            scoreOffsetDecrease(this.lines.subList(line, this.lines.size))
        }
    }

    override fun clearLines() {
        this.lines.forEach({ line ->
            line.sendRemoveToAllReceivers()
            this.restoreColor(line.getColor())
        })
        scoreOffsetDecrease(Collections.emptyList())
        this.lines.clear()
    }

    override fun lineCount(): Int {
        return lines.size
    }

    override fun removePlaceholder(placeholder: String) {
        placeholderProviders.removeIf({ p -> p.getPlaceholder().equals(placeholder, ignoreCase = true) })
    }

    override fun getPlaceholders(): MutableCollection<PlaceholderProvider> {
        return placeholderProviders
    }

    override fun remove(player: Player) {
        tabView.forEach({ tab -> tab.remove(player) })
        lines.forEach({ line -> line.sendRemove(player) })
        this.sidebarObjective!!.sendRemove(player)
        if (this.healthObjective != null) {
            this.healthObjective!!.sendRemove(player)
        }
        this.receivers.remove(player)
        // clear player from any cached context
        this.tabView.forEach({ tab ->
            tab.remove(player)
            if (Objects.equals(tab.getSubject(), player)) {
                tab.setSubject(null)
            }
        })
    }

    override fun setPlayerHealth(player: Player, health: Int) {
        var health = health
        if (health < 0) {
            health = 0
        }
        SidebarManager.getInstance()!!.getSidebarProvider().sendScore(this, player.getName(), health)
    }

    open fun getHealthObjective(): SidebarObjective? {
        return healthObjective
    }

    open fun getReceivers(): LinkedList<Player> {
        return receivers
    }

    override fun hidePlayersHealth() {
        if (healthObjective != null) {
            this.receivers.forEach({ receiver -> healthObjective!!.sendRemove(receiver) })
            healthObjective = null
        }
    }

    override fun showPlayersHealth(displayName: SidebarLine, list: Boolean) {
        if (healthObjective == null) {
            healthObjective = SidebarManager.getInstance()!!.getSidebarProvider().createObjective(this, if (list) "health" else "health2", true, displayName, 2)
            healthObjective!!.refreshTitle()
            this.receivers.forEach({ receiver -> healthObjective!!.sendCreate(receiver) })
        } else {
            healthObjective!!.sendUpdate()
        }
    }

    /**
     * @param identifier char limit is 16.
     */
    override fun playerTabCreate(identifier: String, player: Player?, prefix: SidebarLine, suffix: SidebarLine, pushingRule: PlayerTab.PushingRule, placeholders: MutableCollection<PlaceholderProvider>?): PlayerTab {
        if (identifier.length > 16) {
            throw RuntimeException("Char limit exceeded")
        }
        val tab = SidebarManager.getInstance()!!.getSidebarProvider().createPlayerTab(this, identifier, prefix, suffix, pushingRule, PlayerTab.NameTagVisibility.ALWAYS, placeholders)
        tab.refreshContent()
        // send tab create to sidebar receivers
        getReceivers().forEach(tab::sendCreateToPlayer)
        if (null != player) {
            // add entity to tab team
            tab.setSubject(player)
            tab.add(player)
            tab.sendUserCreateToReceivers(player)
        }
        tabView.add(tab)
        return tab
    }

    override fun removeTab(identifier: String) {
        val playerTab = tabView.stream().filter({ tab -> tab.getIdentifier() == identifier }).findFirst()
        if (playerTab.isPresent()) {
            val tab = playerTab.get()
            tabView.remove(tab)
            tab.sendRemoveToReceivers()
        }
    }

    override fun removeTabs() {
        tabView.forEach(VersionedTabGroup::sendRemoveToReceivers)
        tabView.clear()
    }

    override fun playerTabRefreshAnimation() {
        for (tab in this.tabView) {
            if (tab.refreshContent()) {
                tab.sendUpdateToReceivers()
            }
        }
    }

    override fun playerHealthRefreshAnimation() {
        if (null == healthObjective) {
            return
        }
        healthObjective!!.sendUpdate()
    }

    open fun restoreColor(color: String) {
        this.availableColors.add(color)
    }

    open fun getSidebarObjective(): SidebarObjective? {
        return sidebarObjective
    }

    init {
        for (chatColor in ChatColor.values()) {
            this.availableColors.add(chatColor.toString())
        }
        this.placeholderProviders.addAll(placeholderProvider)
        for (line in lines) {
            SidebarLine.markHasPlaceholders(line, placeholderProvider)
        }
        this.sidebarObjective = SidebarManager.getInstance()!!.getSidebarProvider().createObjective(this, "Sidebar", false, title, 1)
        this.sidebarObjective.refreshTitle()
        for (line in lines) {
            this.addLine(line)
        }
    }

    companion object {
        // used when adding a line
        @JvmStatic
        private fun scoreOffsetIncrease(lineCollections: MutableCollection<ScoreLine>) {
            for (line in lineCollections) {
                line.setScoreAmount(line.getScoreAmount() + 1)
                line.sendUpdateToAllReceivers()
            }
        }

        // used when removing a line
        @JvmStatic
        private fun scoreOffsetDecrease(lineCollections: MutableCollection<ScoreLine>) {
            lineCollections.forEach({ c -> c.setScoreAmount(c.getScoreAmount() - 1) })
        }
    }
}
