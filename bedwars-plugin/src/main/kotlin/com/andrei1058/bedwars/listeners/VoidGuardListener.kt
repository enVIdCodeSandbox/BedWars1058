package com.andrei1058.bedwars.listeners

import com.andrei1058.bedwars.BedWars
import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.arena.Arena
import java.io.PrintWriter
import java.io.StringWriter
import java.util.HashSet
import org.bukkit.World
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.world.WorldInitEvent

/**
 * Diagnostic only: arena maps must be created with a void chunk generator. When a world that belongs to a queued
 * arena is created without one (vanilla terrain would show up around the map), this logs which code created it.
 * Nothing is changed in the world.
 */
open class VoidGuardListener : Listener {
    private val warned: MutableSet<String> = HashSet()

    @EventHandler(priority = EventPriority.MONITOR)
    open fun onWorldInit(e: WorldInitEvent) {
        val world = e.getWorld()
        if (world.getGenerator() != null) {
            return
        }
        val name = world.getName()
        var arenaWorld: Boolean = false
        for (a in Arena.getEnableQueue()) {
            if (name.equals(a.getWorldName(), ignoreCase = true) || name.equals(a.getArenaName(), ignoreCase = true)) {
                arenaWorld = true
                break
            }
        }
        if (!arenaWorld || !warned.add(name.lowercase())) {
            return
        }
        val trace = StringWriter()
        Throwable("world created here").printStackTrace(PrintWriter(trace))
        BedWars.plugin.getLogger().warning("The world '" + name + "' of an arena is being created WITHOUT a void chunk generator, " + "so vanilla terrain will generate around the map. Created by:\n" + trace)
    }
}
