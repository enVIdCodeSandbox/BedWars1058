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

package com.andrei1058.bedwars.api.command

import com.andrei1058.bedwars.api.BedWars
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player
import com.andrei1058.bedwars.api.util.uninitialized

abstract class SubCommand(private var parent: ParentCommand, private var name: String) {
    // Show the subCommand in the main command list
    private var show: Boolean = false
    // Command order in main command list
    private var priority: Int = 20
    // Display name/ info in subCommands list
    private var displayInfo: TextComponent = uninitialized()
    // True if this is an arena setup SubCommand
    private var arenaSetupCommand: Boolean = false
    // Sub command permission
    private var permission: String = ""

    /**
     * Add your sub-command code under this method
     */
    abstract fun execute(args: Array<String>, s: CommandSender): Boolean

    /**
     * Get sub-command name
     */
    open fun getSubCommandName(): String {
        return name
    }

    /**
     * Display the sub-command under the main command
     * Don't forget to set the displayInfo
     * Ops only will see it cuz players receive a commands list from messages file
     */
    open fun showInList(value: Boolean) {
        this.show = value
    }

    /**
     * This is the command information in the subCommands list of the target parent
     */
    open fun setDisplayInfo(displayInfo: TextComponent) {
        this.displayInfo = displayInfo
    }

    /**
     * This is the command priority in the sub-commands list
     * You may use this method if you set showInList true
     * Commands with a minor number will be displayed first
     */
    open fun setPriority(priority: Int) {
        this.priority = priority
    }

    /**
     * Get parent command
     */
    open fun getParent(): ParentCommand {
        return parent
    }

    /**
     * Get show priority
     */
    open fun getPriority(): Int {
        return priority
    }

    /**
     * Get command description for subCommands list
     */
    open fun getDisplayInfo(): TextComponent {
        return displayInfo
    }

    /**
     * True if it is an arena setup sub-command
     */
    open fun setArenaSetupCommand(arenaSetupCommand: Boolean) {
        this.arenaSetupCommand = arenaSetupCommand
    }

    /**
     * Check if it is an arena setup command
     */
    open fun isArenaSetupCommand(): Boolean {
        return arenaSetupCommand
    }

    /**
     * Check if is displayed on the list
     */
    open fun isShow(): Boolean {
        return show
    }

    /**
     * Set permission for sub-command
     */
    open fun setPermission(permission: String) {
        this.permission = permission
    }

    /**
     * Check if player has permission to use the command
     */
    open fun hasPermission(p: CommandSender): Boolean {
        return permission.isEmpty() || p.hasPermission("bw.*") || p.hasPermission(permission)
    }

    /**
     * Check if a sender can see/ use the sub cmd
     *
     * @param api BedWars api instance
     */
    open fun canSee(sender: CommandSender, api: BedWars): Boolean {
        if (sender is ConsoleCommandSender) {
            return false
        }
        if (isArenaSetupCommand() && api.isInSetupSession((sender as Player).getUniqueId())) {
            return true
        }
        if (!isArenaSetupCommand() && api.isInSetupSession((sender as Player).getUniqueId())) {
            return false
        }
        return !isArenaSetupCommand() && hasPermission(sender)
    }

    /**
     * Manage sub-command tab complete
     */
    abstract fun getTabComplete(): MutableList<String>?

    init {
        parent.addSubCommand(this)
    }
}
