package com.andrei1058.bedwars.features.multigroup.bedwars

import com.andrei1058.bedwars.api.BedWars
import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.api.configuration.ConfigPath
import com.andrei1058.bedwars.api.server.ISetupSession
import com.andrei1058.bedwars.api.server.RestoreAdapter
import com.andrei1058.bedwars.api.server.ServerType
import com.andrei1058.bedwars.features.multigroup.Settings
import com.andrei1058.bedwars.features.multigroup.core.VariantRegistry
import com.andrei1058.bedwars.features.multigroup.world.VariantWorlds
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.plugin.Plugin
import java.util.function.Consumer
import java.util.logging.Logger
import com.andrei1058.bedwars.api.util.unchecked

/**
 * Sits in front of whatever restore adapter BedWars1058 uses (its own, or BedWars1058-MapLoader's) and handles the
 * variant arenas. Every call about a normal arena goes straight to [delegate], unchanged.
 *
 * Variants are arenas whose world is a copy of another arena's map. Depending on what is installed:
 *  - [delegateVariants] true (BedWars1058-MapLoader with the alias hook): MapLoader knows the variants and loads them
 *    like any other arena (lazily, from its library). This adapter only protects them (setup / delete).
 *  - otherwise [worlds] copies the base map for them.
 */
class MultiGroupAdapter(
    owner: Plugin,
    private val bedwarsPlugin: Plugin,
    val delegate: RestoreAdapter,
    private val api: BedWars,
    private val registry: VariantRegistry,
    private val worlds: VariantWorlds,
    private val settings: () -> Settings,
    private val delegateVariants: () -> Boolean,
    private val log: Logger,
) : RestoreAdapter(owner) {

    private val plugin = owner

    private fun handledHere(name: String): Boolean = registry.isKnown(name) && !delegateVariants()

    // ---------------------------------------------------------------------------------------------
    // Arena lifecycle
    // ---------------------------------------------------------------------------------------------

    override fun onEnable(a: IArena) {
        val base = registry.baseOf(a.getArenaName())
        if (base == null || delegateVariants()) {
            delegate.onEnable(a)
            return
        }
        worlds.load(a, base)
    }

    override fun onRestart(a: IArena) {
        val name = a.getArenaName()
        if (registry.isRetired(name) && !registry.isVariant(name)) {
            // removed from its map file by /bwmg reload: wind it down instead of creating it again
            log.info("Variant arena '$name' was removed from its map; it is not restarted.")
            registry.forgetRetired(name)
            if (delegateVariants()) delegate.onDisable(a) else sync { worlds.unloadAndDelete(a.getWorldName().unchecked()) }
            return
        }
        if (!handledHere(name)) {
            delegate.onRestart(a)
            return
        }
        val worldName = a.getWorldName()
        sync {
            if (api.getServerType() == ServerType.BUNGEE) {
                val util = api.getArenaUtil()
                if (util.getGamesBeforeRestart() == 0) {
                    if (util.getArenas().isEmpty()) {
                        val cmd = api.getConfigs().getMainConfig().getString(ConfigPath.GENERAL_CONFIGURATION_BUNGEE_OPTION_RESTART_CMD)
                        log.info("Dispatching command: $cmd")
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd)
                    }
                } else {
                    if (util.getGamesBeforeRestart() != -1) util.setGamesBeforeRestart(util.getGamesBeforeRestart() - 1)
                    worlds.unloadAndDelete(worldName.unchecked())
                    if (util.canAutoScale(name)) later(80L) { reenter(name) }
                }
            } else {
                worlds.unloadAndDelete(worldName.unchecked())
                // with several instances per arena another game may already be waiting for players
                if (api.getArenaUtil().canAutoScale(name)) later(80L) { reenter(name) }
            }
        }
    }

    /** A fresh arena object for [name], exactly like BedWars1058 does after a game. */
    private fun reenter(name: String) {
        if (shuttingDown() || !registry.isVariant(name)) return
        api.getArenaUtil().loadArena(name, null)
    }

    override fun onDisable(a: IArena) {
        if (!handledHere(a.getArenaName())) {
            delegate.onDisable(a)
            return
        }
        val worldName = a.getWorldName()
        sync { worlds.unloadAndDelete(worldName.unchecked()) }
    }

    override fun onLobbyRemoval(a: IArena) = delegate.onLobbyRemoval(a)

    // ---------------------------------------------------------------------------------------------
    // Setup sessions: a variant has no map of its own, so it can never be set up
    // ---------------------------------------------------------------------------------------------

    override fun onSetupSessionStart(s: ISetupSession) {
        val v = registry[s.getWorldName()]
        if (v == null) {
            delegate.onSetupSessionStart(s)
            return
        }
        val m = settings().messages
        s.getPlayer().sendMessage(m.format(m.setupBlocked, "arena" to v.arenaName, "map" to v.map, "group" to v.group))
        sync { s.close() }
    }

    override fun onSetupSessionClose(s: ISetupSession) {
        if (registry.isKnown(s.getWorldName())) return
        delegate.onSetupSessionClose(s)
    }

    // ---------------------------------------------------------------------------------------------
    // World queries
    // ---------------------------------------------------------------------------------------------

    override fun isWorld(name: String): Boolean {
        val base = registry.baseOf(name) ?: return delegate.isWorld(name)
        if (delegateVariants()) return delegate.isWorld(name)
        return delegate.isWorld(base) || worlds.findSource(base) != null
    }

    override fun deleteWorld(name: String) {
        if (registry.isKnown(name)) {
            log.info("Not deleting a map for '$name': it is a variant and uses the map '${registry.baseOf(name)}'.")
            return
        }
        delegate.deleteWorld(name)
    }

    override fun cloneArena(name1: String, name2: String) {
        // cloning a variant clones its base map
        delegate.cloneArena(registry.baseOf(name1) ?: name1, name2)
    }

    override fun getWorldsList(): MutableList<String> =
        delegate.getWorldsList().filter { !registry.isKnown(it) && !worlds.isOwned(it) }.toMutableList()

    override fun convertWorlds() = delegate.convertWorlds()

    override fun getDisplayName(): String = "BedWars1058-MultiGroup + " + delegate.getDisplayName()

    override fun foreachBlockInRegion(corner1: Location?, corner2: Location?, consumer: Consumer<Block>) =
        delegate.foreachBlockInRegion(corner1, corner2, consumer)

    override fun clearItems(world: World) = delegate.clearItems(world)

    // ---------------------------------------------------------------------------------------------

    /** The server is stopping (or BedWars1058 is going away): run inline, scheduled tasks would never run. */
    private fun shuttingDown(): Boolean = !plugin.isEnabled || !bedwarsPlugin.isEnabled

    private fun sync(block: () -> Unit) {
        if (shuttingDown()) block() else Bukkit.getScheduler().runTask(plugin, Runnable { block() })
    }

    private fun later(ticks: Long, block: () -> Unit) {
        if (!shuttingDown()) Bukkit.getScheduler().runTaskLater(plugin, Runnable { block() }, ticks)
    }
}
