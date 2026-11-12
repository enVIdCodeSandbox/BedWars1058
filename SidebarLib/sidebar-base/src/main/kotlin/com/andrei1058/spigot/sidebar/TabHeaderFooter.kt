package com.andrei1058.spigot.sidebar

import com.andrei1058.spigot.sidebar.SidebarLine.Companion.markHasPlaceholders
import java.util.LinkedList
import java.util.concurrent.ConcurrentLinkedQueue

open class TabHeaderFooter(private var header: LinkedList<SidebarLine>, private var footer: LinkedList<SidebarLine>, placeholders: MutableCollection<PlaceholderProvider>?) {
    private var placeholders: MutableCollection<PlaceholderProvider>? = null

    open fun getPlaceholders(): MutableCollection<PlaceholderProvider>? {
        return placeholders
    }

    open fun getHeader(): LinkedList<SidebarLine> {
        return header
    }

    open fun getFooter(): LinkedList<SidebarLine> {
        return footer
    }

    open fun setPlaceholders(placeholders: MutableCollection<PlaceholderProvider>?) {
        if (null == placeholders) {
            this.placeholders = ConcurrentLinkedQueue()
            return
        }
        this.placeholders = placeholders
        for (line in footer) {
            markHasPlaceholders(line, placeholders)
        }
        for (line in header) {
            markHasPlaceholders(line, placeholders)
        }
    }

    open fun setFooter(footer: LinkedList<SidebarLine>) {
        this.footer = footer
        for (line in footer) {
            markHasPlaceholders(line, placeholders.unchecked())
        }
    }

    open fun setHeader(header: LinkedList<SidebarLine>) {
        this.header = header
        for (line in header) {
            markHasPlaceholders(line, placeholders.unchecked())
        }
    }

    init {
        setPlaceholders(placeholders)
    }
}
