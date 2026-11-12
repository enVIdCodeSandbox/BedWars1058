package com.andrei1058.bedwars.api.events.team

import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.api.arena.team.ITeam
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

open class TeamEliminatedEvent(private val arena: IArena, private val team: ITeam) : Event() {
    open fun getArena(): IArena {
        return arena
    }

    open fun getTeam(): ITeam {
        return team
    }

    override fun getHandlers(): HandlerList {
        return HANDLERS
    }

    companion object {
        private val HANDLERS: HandlerList = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return HANDLERS
        }
    }
}
