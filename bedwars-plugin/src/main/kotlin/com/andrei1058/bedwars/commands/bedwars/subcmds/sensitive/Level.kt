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

package com.andrei1058.bedwars.commands.bedwars.subcmds.sensitive

import com.andrei1058.bedwars.BedWars
import com.andrei1058.bedwars.api.command.ParentCommand
import com.andrei1058.bedwars.api.command.SubCommand
import com.andrei1058.bedwars.api.events.player.PlayerXpGainEvent
import com.andrei1058.bedwars.arena.Arena
import com.andrei1058.bedwars.arena.Misc
import com.andrei1058.bedwars.arena.SetupSession
import com.andrei1058.bedwars.configuration.LevelsConfig
import com.andrei1058.bedwars.configuration.Permissions
import net.md_5.bungee.api.chat.ClickEvent
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player

open class Level(parent: ParentCommand, name: String) : SubCommand(parent, name) {
    override fun execute(args: Array<String>, s: CommandSender): Boolean {
        if (args.size == 0) {
            sendSubCommands(s, BedWars.getAPI())
            return true
        }
        if (args[0].equals("setlevel", ignoreCase = true)) {
            if (args.size != 3) {
                s.sendMessage(ChatColor.GOLD.toString() + " ▪ " + ChatColor.GRAY + "Usage: /bw level setLevel §o<player> <level>")
                return true
            }
            val pl: Player? = Bukkit.getPlayer(args[1])
            if (pl == null) {
                s.sendMessage(ChatColor.RED.toString() + " ▪ " + ChatColor.GRAY + "Player not found!")
                return true
            }
            var level: Int
            try {
                level = Integer.parseInt(args[2])
            } catch (e: Exception) {
                s.sendMessage(ChatColor.RED.toString() + "Level must be an integer!")
                return true
            }
            BedWars.getAPI().getLevelsUtil().setLevel(pl, level)
            val nextLevelCost: Int = if (LevelsConfig.levels.getYml().get("levels." + level + ".rankup-cost") == null) LevelsConfig.levels.getInt("levels.others.rankup-cost") else LevelsConfig.levels.getInt("levels." + level + ".rankup-cost")
            val levelName: String = if (LevelsConfig.levels.getYml().get("levels." + level + ".name") == null) LevelsConfig.levels.getYml().getString("levels.others.name") else LevelsConfig.levels.getYml().getString("levels." + level + ".name")
            BedWars.plugin.getServer().getScheduler().runTaskAsynchronously(BedWars.plugin, Runnable {
                BedWars.getRemoteDatabase().setLevelData(pl.getUniqueId(), level, 0, levelName, nextLevelCost)
                s.sendMessage(ChatColor.GOLD.toString() + " ▪ " + ChatColor.GRAY + pl.getName() + " level was set to: " + args[2])
                s.sendMessage(ChatColor.GOLD.toString() + " ▪ " + ChatColor.GRAY + "The player may need to rejoin to see it updated.")
            })
        } else if (args[0].equals("givexp", ignoreCase = true)) {
            if (args.size != 3) {
                s.sendMessage(ChatColor.GOLD.toString() + " ▪ " + ChatColor.GRAY + "Usage: /bw level giveXp §o<player> <amount>")
                return true
            }
            val pl: Player? = Bukkit.getPlayer(args[1])
            if (pl == null) {
                s.sendMessage(ChatColor.RED.toString() + " ▪ " + ChatColor.GRAY + "Player not found!")
                return true
            }
            var amount: Int
            try {
                amount = Integer.parseInt(args[2])
            } catch (e: Exception) {
                s.sendMessage(ChatColor.RED.toString() + "Amount must be an integer!")
                return true
            }
            BedWars.getAPI().getLevelsUtil().addXp(pl, amount, PlayerXpGainEvent.XpSource.OTHER)
            BedWars.plugin.getServer().getScheduler().runTaskAsynchronously(BedWars.plugin, Runnable {
                val data = BedWars.getRemoteDatabase().getLevelData(pl.getUniqueId())
                BedWars.getRemoteDatabase().setLevelData(pl.getUniqueId(), data[0] as Int, (data[1] as Int) + amount, data[2] as String, data[3] as Int)
                s.sendMessage(ChatColor.GOLD.toString() + " ▪ " + ChatColor.GRAY + args[2] + " xp was given to: " + pl.getName())
                s.sendMessage(ChatColor.GOLD.toString() + " ▪ " + ChatColor.GRAY + "The player may need to rejoin to see it updated.")
            })
        } else {
            sendSubCommands(s, BedWars.getAPI())
        }
        return true
    }

    private fun sendSubCommands(s: CommandSender, api: com.andrei1058.bedwars.api.BedWars) {
        if (s is Player) {
            val p = s as Player
            p.spigot().sendMessage(Misc.msgHoverClick("§6 ▪ §7/" + getParent().getName() + " " + getSubCommandName() + " setLevel §o<player> <level>", "Set a player level.", "/" + getParent().getName() + " " + getSubCommandName() + " setLevel", ClickEvent.Action.SUGGEST_COMMAND))
            p.spigot().sendMessage(Misc.msgHoverClick("§6 ▪ §7/" + getParent().getName() + " " + getSubCommandName() + " giveXp §o<player> <amount>", "Give Xp to a player.", "/" + getParent().getName() + " " + getSubCommandName() + " giveXp", ClickEvent.Action.SUGGEST_COMMAND))
        } else {
            s.sendMessage(ChatColor.GOLD.toString() + "bw level setLevel <player> <level>")
            s.sendMessage(ChatColor.GOLD.toString() + "bw level giveXp <player> <amount>")
        }
    }

    override fun getTabComplete(): MutableList<String>? {
        return null
    }

    override fun canSee(s: CommandSender, api: com.andrei1058.bedwars.api.BedWars): Boolean {
        if (s is ConsoleCommandSender) {
            return false
        }
        val p = s as Player
        if (Arena.isInArena(p)) {
            return false
        }
        if (SetupSession.isInSetupSession(p.getUniqueId())) {
            return false
        }
        return hasPermission(s)
    }

    init {
        setPermission(Permissions.PERMISSION_LEVEL)
        setPriority(10)
        showInList(true)
        setDisplayInfo(Misc.msgHoverClick("§6 ▪ §7/" + getParent().getName() + " " + getSubCommandName() + " §8      - §eclick for details", "§fManage a player level.", "/" + getParent().getName() + " " + getSubCommandName(), ClickEvent.Action.RUN_COMMAND))
    }
}
