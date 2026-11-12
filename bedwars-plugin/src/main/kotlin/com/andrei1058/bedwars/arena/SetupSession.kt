/*
 * BedWars1058 - A bed wars mini-game.
 * Copyright (C) 2021 Andrei Dascălu
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Contact e-mail: andrew.dascalu@gmail.com
 */

package com.andrei1058.bedwars.arena

import com.andrei1058.bedwars.BedWars
import com.andrei1058.bedwars.BedWars.Companion.config
import com.andrei1058.bedwars.BedWars.Companion.plugin
import com.andrei1058.bedwars.api.arena.team.TeamColor
import com.andrei1058.bedwars.api.configuration.ConfigPath
import com.andrei1058.bedwars.api.events.server.SetupSessionCloseEvent
import com.andrei1058.bedwars.api.events.server.SetupSessionStartEvent
import com.andrei1058.bedwars.api.server.ISetupSession
import com.andrei1058.bedwars.api.server.ServerType
import com.andrei1058.bedwars.api.server.SetupType
import com.andrei1058.bedwars.commands.Misc.createArmorStand
import com.andrei1058.bedwars.commands.bedwars.MainCommand
import com.andrei1058.bedwars.configuration.ArenaConfig
import com.andrei1058.bedwars.support.paper.TeleportManager
import java.util.*
import net.md_5.bungee.api.chat.ClickEvent
import org.bukkit.*
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import com.andrei1058.bedwars.api.util.unchecked
import com.andrei1058.bedwars.api.util.uninitialized

open class SetupSession(private var player: Player, private var worldName: String) : ISetupSession {
    private var setupType: SetupType = uninitialized()
    private var cm: ArenaConfig = uninitialized()
    private var started: Boolean = false
    private var autoCreatedEmerald: Boolean = false
    private var autoCreatedDiamond: Boolean = false
    private var skipAutoCreateGen: MutableList<Location> = ArrayList()

    open fun setSetupType(setupType: SetupType) {
        this.setupType = setupType
    }

    override fun getSetupType(): SetupType {
        return setupType
    }

    override fun getPlayer(): Player {
        return player
    }

    override fun getWorldName(): String {
        return worldName
    }

    open fun isStarted(): Boolean {
        return started
    }

    /**
     * Start setup session, loadStructure world etc
     *
     * @return return is broken. do not use it.
     */
    open fun startSetup(): Boolean {
        getPlayer().sendMessage("§6 ▪ §7Loading " + getWorldName())
        cm = ArenaConfig(BedWars.plugin, getWorldName(), plugin.getDataFolder().getPath() + "/Arenas")
        BedWars.getAPI().getRestoreAdapter().onSetupSessionStart(this)
        return true
    }

    /**
     * Cancel setup
     */
    open fun cancel() {
        getSetupSessions().remove(this)
        if (isStarted()) {
            player.sendMessage("§6 ▪ §7" + getWorldName() + " setup cancelled!")
            done()
        }
    }

    /**
     * End setup session
     */
    open fun done() {
        BedWars.getAPI().getRestoreAdapter().onSetupSessionClose(this)
        getSetupSessions().remove(this)
        if (BedWars.getServerType() != ServerType.BUNGEE) {
            try {
                TeleportManager.teleportC(getPlayer(), config.getConfigLoc("lobbyLoc").unchecked(), PlayerTeleportEvent.TeleportCause.PLUGIN)
            } catch (ex: Exception) {
                TeleportManager.teleportC(getPlayer(), Bukkit.getWorlds().get(0).getSpawnLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN)
            }
        }
        getPlayer().removePotionEffect(PotionEffectType.SPEED)
        if (BedWars.getServerType() == ServerType.MULTIARENA) {
            Arena.sendLobbyCommandItems(getPlayer())
        }
        Bukkit.getPluginManager().callEvent(SetupSessionCloseEvent(this))
    }

    open fun setStarted(started: Boolean) {
        this.started = started
    }

    /**
     * Get arena configuration
     */
    override fun getConfig(): ArenaConfig {
        return cm
    }

    override fun teleportPlayer() {
        player.getInventory().clear()
        TeleportManager.teleport(player, Bukkit.getWorld(getWorldName()).getSpawnLocation())
        player.setGameMode(GameMode.CREATIVE)
        Bukkit.getScheduler().runTaskLater(plugin, Runnable {
            player.setAllowFlight(true)
            player.setFlying(true)
        }, 5L)
        player.addPotionEffect(PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2))
        player.sendMessage("\n" + ChatColor.WHITE + "\n")
        for (x in 0 until 10) {
            getPlayer().sendMessage(" ")
        }
        player.sendMessage(ChatColor.GREEN.toString() + "You were teleported to the " + ChatColor.GOLD + getWorldName() + ChatColor.GREEN + "'s spawn.")
        if (getSetupType() == SetupType.ASSISTED && getConfig().getYml().get("waiting.Loc") == null) {
            player.sendMessage("")
            player.sendMessage(ChatColor.GREEN.toString() + "Hello " + player.getDisplayName() + "!")
            player.sendMessage(ChatColor.WHITE.toString() + "Please set the waiting spawn.")
            player.sendMessage(ChatColor.WHITE.toString() + "It is the place where players will wait the game to start.")
            player.spigot().sendMessage(Misc.msgHoverClick(ChatColor.BLUE.toString() + "     ▪     " + ChatColor.GOLD + "CLICK HERE TO SET THE WAITING LOBBY    " + ChatColor.BLUE + " ▪", ChatColor.LIGHT_PURPLE.toString() + "Click to set the waiting spawn.", "/" + BedWars.mainCmd + " setWaitingSpawn", ClickEvent.Action.RUN_COMMAND))
            player.spigot().sendMessage(MainCommand.createTC(ChatColor.YELLOW.toString() + "Or type: " + ChatColor.GRAY + "/" + BedWars.mainCmd + " to see the command list.", "/" + BedWars.mainCmd + "", ChatColor.WHITE.toString() + "Show commands list."))
        } else {
            Bukkit.dispatchCommand(player, BedWars.mainCmd + " cmds")
        }
        val w = Bukkit.getWorld(getWorldName())
        Bukkit.getScheduler().runTaskLater(plugin, Runnable { w.getEntities().stream().filter({ e -> e.getType() != EntityType.PLAYER }).filter({ e -> e.getType() != EntityType.PAINTING }).filter({ e -> e.getType() != EntityType.ITEM_FRAME }).forEach(Entity::remove) }, 30L)
        w.setAutoSave(false)
        w.setGameRuleValue("doMobSpawning", "false")
        Bukkit.getPluginManager().callEvent(SetupSessionStartEvent(this))
        setStarted(true)
        Bukkit.getScheduler().runTaskLater(plugin, Runnable {
            for (team in getTeams()) {
                for (gen in arrayOf<String>("Iron", "Gold", "Emerald")) {
                    if (getConfig().getYml().get("Team." + team + "." + gen) != null) {
                        for (loc in getConfig().getList("Team." + team + ".Iron")) {
                            createArmorStand(ChatColor.GOLD.toString() + gen + " generator added for team: " + getTeamColor(team) + team, getConfig().convertStringToArenaLocation(loc), loc)
                        }
                    }
                    if (getConfig().getYml().get("Team." + team + ".Spawn") != null) {
                        createArmorStand(getTeamColor(team).toString() + team + " " + ChatColor.GOLD + "SPAWN SET", getConfig().getArenaLoc("Team." + team + ".Spawn").unchecked(), getConfig().getString("Team." + team + ".Spawn"))
                    }
                    if (getConfig().getYml().get("Team." + team + ".Bed") != null) {
                        createArmorStand(getTeamColor(team).toString() + team + " " + ChatColor.GOLD + "BED SET", getConfig().getArenaLoc("Team." + team + ".Bed").unchecked(), getConfig().getString("Team." + team + ".Bed"))
                    }
                }
                if (getConfig().getYml().get("Team." + team + ".Shop") != null) {
                    createArmorStand(getTeamColor(team).toString() + team + " " + ChatColor.GOLD + "SHOP SET", getConfig().getArenaLoc("Team." + team + ".Shop").unchecked(), null)
                }
                if (getConfig().getYml().get("Team." + team + ".Upgrade") != null) {
                    createArmorStand(getTeamColor(team).toString() + team + " " + ChatColor.GOLD + "UPGRADE SET", getConfig().getArenaLoc("Team." + team + ".Upgrade").unchecked(), null)
                }
                if (getConfig().getYml().get("Team." + team + "." + ConfigPath.ARENA_TEAM_KILL_DROPS_LOC) != null) {
                    createArmorStand(ChatColor.GOLD.toString() + "Kill drops " + team, getConfig().getArenaLoc("Team." + team + "." + ConfigPath.ARENA_TEAM_KILL_DROPS_LOC).unchecked(), null)
                }
            }
            for (type in arrayOf<String>("Emerald", "Diamond")) {
                if (getConfig().getYml().get("generator." + type) != null) {
                    for (loc in getConfig().getList("generator." + type)) {
                        createArmorStand(ChatColor.GOLD.toString() + type + " SET", getConfig().convertStringToArenaLocation(loc), loc)
                    }
                }
            }
        }, 90L)
    }

    override fun close() {
        cancel()
    }

    open fun getSkipAutoCreateGen(): MutableList<Location> {
        return ArrayList(skipAutoCreateGen)
    }

    open fun addSkipAutoCreateGen(location: Location) {
        skipAutoCreateGen.add(location)
    }

    open fun setAutoCreatedEmerald(autoCreatedEmerald: Boolean) {
        this.autoCreatedEmerald = autoCreatedEmerald
    }

    open fun isAutoCreatedEmerald(): Boolean {
        return autoCreatedEmerald
    }

    open fun setAutoCreatedDiamond(autoCreatedDiamond: Boolean) {
        this.autoCreatedDiamond = autoCreatedDiamond
    }

    open fun isAutoCreatedDiamond(): Boolean {
        return autoCreatedDiamond
    }

    open fun getPrefix(): String {
        return ChatColor.GREEN.toString() + "[" + getWorldName() + ChatColor.GREEN + "] " + ChatColor.GOLD
    }

    /**
     * Get a team color.
     *
     * @param team team name.
     * @return team color.
     */
    open fun getTeamColor(team: String): ChatColor {
        return TeamColor.getChatColor(getConfig().getString("Team." + team + ".Color"))
    }

    /**
     * Show available teams.
     */
    open fun displayAvailableTeams() {
        if (getConfig().getYml().get("Team") != null) {
            getPlayer().sendMessage(getPrefix() + "Available teams: ")
            for (team in Objects.requireNonNull(getConfig().getYml().getConfigurationSection("Team")).getKeys(false)) {
                getPlayer().sendMessage(getPrefix() + TeamColor.getChatColor(Objects.requireNonNull(getConfig().getYml().getString("Team." + team + ".Color"))) + team)
            }
        }
    }

    /**
     * Get nearest team name.
     *
     * @return empty if not found.
     */
    open fun getNearestTeam(): String {
        var foundTeam = ""
        val cs: ConfigurationSection? = getConfig().getYml().getConfigurationSection("Team")
        if (cs == null) {
            return foundTeam
        }
        var distance: Double = 100.0
        for (team in cs.getKeys(false)) {
            if (getConfig().getYml().get("Team." + team + ".Spawn") == null) {
                continue
            }
            val dis: Double = getConfig().getArenaLoc("Team." + team + ".Spawn")!!.distance(getPlayer().getLocation())
            if (dis <= getConfig().getInt(ConfigPath.ARENA_ISLAND_RADIUS)) {
                if (dis < distance) {
                    distance = dis
                    foundTeam = team
                }
            }
        }
        return foundTeam
    }

    open fun dot(): String {
        return ChatColor.BLUE.toString() + " " + '▪' + " " + ChatColor.GRAY + "/" + BedWars.mainCmd + " "
    }

    open fun getTeams(): MutableList<String> {
        if (getConfig().getYml().get("Team") == null) {
            return ArrayList()
        }
        return ArrayList(getConfig().getYml().getConfigurationSection("Team").getKeys(false))
    }

    init {
        getSetupSessions().add(this)
        openGUI(player)
    }

    companion object {
        private var setupSessions: MutableList<SetupSession> = ArrayList()

        @JvmStatic
        fun getSetupSessions(): MutableList<SetupSession> {
            return setupSessions
        }

        /**
         * Gets the setup type gui inv name
         */
        @JvmStatic
        fun getInvName(): String {
            return "§8Choose a setup method"
        }

        /**
         * Get advanced type item slot
         */
        @JvmStatic
        fun getAdvancedSlot(): Int {
            return 5
        }

        /**
         * Get assisted type item slot
         */
        @JvmStatic
        fun getAssistedSlot(): Int {
            return 3
        }

        @JvmStatic
        private fun openGUI(player: Player) {
            val inv = Bukkit.createInventory(null, 9, getInvName())
            val assisted = ItemStack(Material.GLOWSTONE_DUST)
            val am = assisted.getItemMeta()
            am.setDisplayName("§e§lASSISTED SETUP")
            am.setLore(Arrays.asList("", "§aEasy and quick setup!", "§7For beginners and lazy staff :D", "", "§3Reduced options."))
            assisted.setItemMeta(am)
            inv.setItem(getAssistedSlot(), assisted)
            val advanced = ItemStack(Material.REDSTONE)
            val amm = advanced.getItemMeta()
            amm.setDisplayName("§c§lADVANCED SETUP")
            amm.setLore(Arrays.asList("", "§aDetailed setup!", "§7For experienced staff :D", "", "§3Advanced options."))
            advanced.setItemMeta(amm)
            inv.setItem(getAdvancedSlot(), advanced)
            player.openInventory(inv)
        }

        /**
         * Check if a player is in setup session
         */
        @JvmStatic
        fun isInSetupSession(player: UUID): Boolean {
            for (ss in getSetupSessions()) {
                if (ss.getPlayer().getUniqueId() == player) {
                    return true
                }
            }
            return false
        }

        /**
         * Get a player session
         */
        @JvmStatic
        fun getSession(p: UUID): SetupSession? {
            for (ss in getSetupSessions()) {
                if (ss.getPlayer().getUniqueId() == p) {
                    return ss
                }
            }
            return null
        }
    }
}
