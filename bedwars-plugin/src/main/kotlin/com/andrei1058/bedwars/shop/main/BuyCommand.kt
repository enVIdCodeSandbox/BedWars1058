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

package com.andrei1058.bedwars.shop.main

import com.andrei1058.bedwars.BedWars
import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.api.arena.shop.IBuyItem
import com.andrei1058.bedwars.api.arena.team.ITeam
import com.andrei1058.bedwars.api.language.Language
import java.util.ArrayList
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import com.andrei1058.bedwars.api.util.unchecked

open class BuyCommand(path: String, yml: YamlConfiguration, upgradeIdentifier: String) : IBuyItem {
    private val asPlayer: MutableList<String> = ArrayList()
    private val asConsole: MutableList<String> = ArrayList()
    private val upgradeIdentifier: String

    override fun isLoaded(): Boolean {
        return true
    }

    override fun give(player: Player, arena: IArena) {
        BedWars.debug("Giving BuyCMD: " + getUpgradeIdentifier() + " to: " + player.getName())
        val playerName = player.getName()
        val playerUUID = player.getUniqueId().toString()
        val team: ITeam? = arena.getTeam(player)
        val teamName: String = (if (team == null) "null" else team.getName()).unchecked()
        val teamDisplay: String = if (team == null) "null" else team.getDisplayName(Language.getPlayerLanguage(player))
        val teamColor: String = if (team == null) ChatColor.WHITE.toString() else team.getColor().chat().toString()
        val arenaIdentifier = arena.getArenaName()
        val arenaWorld = arena.getWorldName()
        val arenaDisplay = arena.getDisplayName()
        val arenaGroup = arena.getGroup()
        for (playerCmd in asPlayer) {
            player.chat(playerCmd.replace("{player}", playerName).replace("{player_uuid}", playerUUID).replace("{team}", teamName).replace("{team_display}", teamDisplay).replace("{team_color}", teamColor).replace("{arena}", arenaIdentifier).replace("{arena_world}", arenaWorld.unchecked()).replace("{arena_display}", arenaDisplay).replace("{arena_group}", arenaGroup))
        }
        for (consoleCmd in asConsole) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), consoleCmd.replace("{player}", playerName).replace("{player_uuid}", playerUUID).replace("{team}", teamName).replace("{team_display}", teamDisplay).replace("{team_color}", teamColor).replace("{arena}", arenaIdentifier).replace("{arena_world}", arenaWorld.unchecked()).replace("{arena_display}", arenaDisplay).replace("{arena_group}", arenaGroup))
        }
    }

    override fun getUpgradeIdentifier(): String? {
        return upgradeIdentifier
    }

    override fun getItemStack(): ItemStack? {
        return null
    }

    override fun setItemStack(itemStack: ItemStack) {
    }

    override fun isAutoEquip(): Boolean {
        return false
    }

    override fun setAutoEquip(autoEquip: Boolean) {
    }

    override fun isPermanent(): Boolean {
        return false
    }

    override fun setPermanent(permanent: Boolean) {
    }

    override fun isUnbreakable(): Boolean {
        return false
    }

    override fun setUnbreakable(unbreakable: Boolean) {
    }

    init {
        BedWars.debug("Loading BuyCommand: " + path)
        this.upgradeIdentifier = upgradeIdentifier
        for (cmd0 in yml.getStringList(path + ".as-console")) {
            var cmd = cmd0
            if (cmd.startsWith("/")) {
                cmd = cmd.replaceFirst("/".toRegex(), "")
            }
            asConsole.add(cmd)
        }
        for (cmd0 in yml.getStringList(path + ".as-player")) {
            var cmd = cmd0
            if (!cmd.startsWith("/")) {
                cmd = "/" + cmd
            }
            asPlayer.add(cmd)
        }
    }
}
