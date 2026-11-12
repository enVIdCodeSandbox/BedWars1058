package com.andrei1058.bedwars.sidebar

import com.andrei1058.spigot.sidebar.ScoredLine
import com.andrei1058.spigot.sidebar.SidebarLine

open class BwSidebarLine(@JvmField val content: String, score: String?) : SidebarLine(), ScoredLine {
    @JvmField val score: String?

    override fun getScore(): String? {
        return score
    }

    override fun getLine(): String {
        return content
    }

    init {
        this.score = score ?: ""
    }
}
