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

package com.andrei1058.bedwars.commands.bedwars.subcmds.sensitive.setup

import com.andrei1058.bedwars.BedWars
import com.andrei1058.bedwars.api.command.ParentCommand
import com.andrei1058.bedwars.api.command.SubCommand
import com.andrei1058.bedwars.arena.Misc
import com.andrei1058.bedwars.arena.SetupSession
import com.andrei1058.bedwars.configuration.Permissions
import com.andrei1058.bedwars.support.paper.TeleportManager
import net.md_5.bungee.api.chat.ClickEvent
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player

open class Save(parent: ParentCommand, name: String) : SubCommand(parent, name) {
    override fun execute(args: Array<String>, s: CommandSender): Boolean {
        if (s is ConsoleCommandSender) {
            return false
        }
        val p = s as Player
        val ss: SetupSession? = SetupSession.getSession(p.getUniqueId())
        if (ss == null) {
            //s.sendMessage("§c ▪ §7You're not in a setup session!");
            return false
        }
        //Clear setup armor-stands
        for (e in p.getWorld().getEntities()) {
            if (e.getType() == EntityType.ARMOR_STAND) {
                e.remove()
            }
        }
        if (Bukkit.getWorld(BedWars.getLobbyWorld()) != null) {
            TeleportManager.teleport(p, Bukkit.getWorld(BedWars.getLobbyWorld()).getSpawnLocation())
        } else {
            TeleportManager.teleport(p, Bukkit.getWorlds().get(0).getSpawnLocation())
        }
        ss.done()
        p.sendMessage(ss.getPrefix() + "Arena changes saved!")
        p.sendMessage(ss.getPrefix() + "You can now enable it using:")
        p.spigot().sendMessage(Misc.msgHoverClick(ChatColor.GOLD.toString() + "/" + getParent().getName() + " enableArena " + ss.getWorldName() + ChatColor.GRAY + " (click to enable)", ChatColor.GREEN.toString() + "Enable this arena.", "/" + getParent().getName() + " enableArena " + ss.getWorldName(), ClickEvent.Action.RUN_COMMAND))
        return true
    }

    override fun getTabComplete(): MutableList<String>? {
        return null
    }

    override fun canSee(s: CommandSender, api: com.andrei1058.bedwars.api.BedWars): Boolean {
        if (s is ConsoleCommandSender) {
            return false
        }
        val p = s as Player
        if (!SetupSession.isInSetupSession(p.getUniqueId())) {
            return false
        }
        return hasPermission(s)
    }

    init {
        setArenaSetupCommand(true)
        setPermission(Permissions.PERMISSION_SETUP_ARENA)
    }
}
