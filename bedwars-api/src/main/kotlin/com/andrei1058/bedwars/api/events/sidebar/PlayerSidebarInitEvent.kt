package com.andrei1058.bedwars.api.events.sidebar

import com.andrei1058.bedwars.api.sidebar.ISidebar
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

open class PlayerSidebarInitEvent(private var player: Player, private var sidebar: ISidebar) : Event(), Cancellable {
    private var cancelled: Boolean = false

    override fun getHandlers(): HandlerList {
        return HANDLERS
    }

    override fun isCancelled(): Boolean {
        return cancelled
    }

    override fun setCancelled(cancel: Boolean) {
        this.cancelled = cancel
    }

    open fun getPlayer(): Player {
        return player
    }

    open fun setPlayer(player: Player) {
        this.player = player
    }

    open fun getSidebar(): ISidebar {
        return sidebar
    }

    open fun setSidebar(sidebar: ISidebar) {
        this.sidebar = sidebar
    }

    companion object {
        private val HANDLERS: HandlerList = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return HANDLERS
        }
    }
}
