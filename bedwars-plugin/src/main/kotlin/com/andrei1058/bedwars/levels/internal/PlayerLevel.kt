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

package com.andrei1058.bedwars.levels.internal

import com.andrei1058.bedwars.BedWars
import com.andrei1058.bedwars.api.events.player.PlayerLevelUpEvent
import com.andrei1058.bedwars.api.events.player.PlayerXpGainEvent
import com.andrei1058.bedwars.configuration.LevelsConfig
import java.text.NumberFormat
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import com.andrei1058.bedwars.api.util.uninitialized

open class PlayerLevel(player: UUID, level: Int, currentXp: Int) {
    private var uuid: UUID
    private var level: Int
    private var nextLevelCost: Int = 0
    private var levelName: String = uninitialized()
    private var currentXp: Int
    private var progressBar: String = uninitialized()
    private var requiredXp: String = uninitialized()
    private var formattedCurrentXp: String = uninitialized()
    // keep trace if current level is different than the one in database
    private var modified: Boolean = false

    open fun setLevelName(level: Int) {
        this.levelName = ChatColor.translateAlternateColorCodes('&', LevelsConfig.getLevelName(level)).replace("{number}", level.toString())
    }

    open fun setNextLevelCost(level: Int, initialize: Boolean) {
        if (!initialize) {
            modified = true
        }
        this.nextLevelCost = LevelsConfig.getNextCost(level)
    }

    open fun lazyLoad(level: Int, currentXp: Int) {
        var level = level
        var currentXp = currentXp
        modified = false
        if (level < 1) {
            level = 1
        }
        if (currentXp < 0) {
            currentXp = 0
        }
        setLevelName(level)
        setNextLevelCost(level, true)
        this.level = level
        this.currentXp = currentXp
        updateProgressBar()
        modified = false
    }

    /**
     * Update the player progress bar.
     */
    private fun updateProgressBar() {
        val l1: Double = ((nextLevelCost - currentXp) / ((nextLevelCost).toDouble())) * 10
        var locked: Int = l1.toInt()
        var unlocked: Int = 10 - locked
        if (locked < 0 || unlocked < 0) {
            locked = 10
            unlocked = 0
        }
        progressBar = ChatColor.translateAlternateColorCodes('&', LevelsConfig.levels.getString("progress-bar.format").replace("{progress}", LevelsConfig.levels.getString("progress-bar.unlocked-color") + CharArray(unlocked).toString().replace("\u0000", LevelsConfig.levels.getString("progress-bar.symbol")) + LevelsConfig.levels.getString("progress-bar.locked-color") + CharArray(locked).toString().replace("\u0000", LevelsConfig.levels.getString("progress-bar.symbol"))))
        requiredXp = formatNumber(nextLevelCost)
        formattedCurrentXp = formatNumber(currentXp)
    }

    /**
     * Get player current level.
     */
    open fun getLevel(): Int {
        return level
    }

    /**
     * Get the amount of xp required to level up.
     */
    open fun getNextLevelCost(): Int {
        return nextLevelCost
    }

    /**
     * Get player uuid.
     */
    open fun getUuid(): UUID {
        return uuid
    }

    /**
     * Get player current level display name.
     */
    open fun getLevelName(): String {
        return levelName
    }

    /**
     * Get player xp.
     */
    open fun getCurrentXp(): Int {
        return currentXp
    }

    /**
     * Get progress bar for player.
     */
    open fun getProgress(): String {
        return progressBar
    }

    /**
     * Get target xp already formatted.
     * Like: 2000 is 2k
     */
    open fun getFormattedRequiredXp(): String {
        return requiredXp
    }

    /**
     * Add xp to player with source.
     */
    open fun addXp(xp: Int, source: PlayerXpGainEvent.XpSource) {
        if (xp < 0) {
            return
        }
        this.currentXp += xp
        upgradeLevel()
        updateProgressBar()
        Bukkit.getPluginManager().callEvent(PlayerXpGainEvent(Bukkit.getPlayer(uuid), xp, source))
        modified = true
    }

    /**
     * Set player xp.
     */
    open fun setXp(currentXp: Int) {
        var currentXp = currentXp
        if (currentXp <= 0) {
            currentXp = 0
        }
        this.currentXp = currentXp
        upgradeLevel()
        updateProgressBar()
        modified = true
    }

    /**
     * Set player level.
     */
    open fun setLevel(level: Int) {
        this.level = level
        nextLevelCost = LevelsConfig.getNextCost(level)
        this.levelName = ChatColor.translateAlternateColorCodes('&', LevelsConfig.getLevelName(level)).replace("{number}", level.toString())
        requiredXp = if (nextLevelCost >= 1000) if (nextLevelCost % 1000 == 0) (nextLevelCost / 1000).toString() + "k" else ((nextLevelCost.toDouble()) / 1000).toString() + "k" else nextLevelCost.toString()
        updateProgressBar()
        modified = true
    }

    /**
     * Get player xp already formatted.
     * Like: 1000 is 1k
     */
    open fun getFormattedCurrentXp(): String {
        return formattedCurrentXp
    }

    /**
     * Used to upgrade player level.
     */
    open fun upgradeLevel() {
        if (currentXp >= nextLevelCost) {
            currentXp = currentXp - nextLevelCost
            level++
            nextLevelCost = LevelsConfig.getNextCost(level)
            this.levelName = ChatColor.translateAlternateColorCodes('&', LevelsConfig.getLevelName(level)).replace("{number}", level.toString())
            requiredXp = formatNumber(nextLevelCost)
            formattedCurrentXp = formatNumber(currentXp)
            Bukkit.getPluginManager().callEvent(PlayerLevelUpEvent(Bukkit.getPlayer(getUuid()), level, nextLevelCost))
            modified = true
        }
    }

    private fun formatNumber(score: Int): String {
        val format = NumberFormat.getInstance()
        format.setMaximumFractionDigits(2)
        format.setMinimumFractionDigits(0)
        if (score >= 1000) {
            return format.format(score / 1000.0) + "k"
        }
        return format.format(score)
    }

    /**
     * Get player level as int.
     */
    open fun getPlayerLevel(): Int {
        return level
    }

    /**
     * Destroy data.
     */
    open fun destroy() {
        levelByPlayer.remove(uuid)
        //        LevelsConfig.levels.getYml().getString("levels.others.name") : LevelsConfig.levels.getYml().getString("levels." + level + ".name"), nextLevelCost);
        updateDatabase()
    }

    open fun updateDatabase() {
        if (modified) {
            Bukkit.getScheduler().runTaskAsynchronously(BedWars.plugin, Runnable { BedWars.getRemoteDatabase().setLevelData(uuid, level, currentXp, LevelsConfig.getLevelName(level), nextLevelCost) })
            modified = false
        }
    }

    init {
        var level = level
        var currentXp = currentXp
        this.uuid = player
        setLevelName(level)
        setNextLevelCost(level, true)
        //fix levels broken in the past by an issue
        if (level < 1) {
            level = 1
        }
        if (currentXp < 0) {
            currentXp = 0
        }
        this.level = level
        this.currentXp = currentXp
        updateProgressBar()
        //formattedCurrentXp = currentXp >= 1000 ? currentXp % 1000 == 0 ? currentXp / 1000 + "k" : (double) currentXp / 1000 + "k" : String.valueOf(currentXp);
        if (!levelByPlayer.containsKey(player)) {
            levelByPlayer.put(player, this)
        }
    }

    companion object {
        private var levelByPlayer: ConcurrentHashMap<UUID, PlayerLevel> = ConcurrentHashMap()

        /**
         * Get PlayerLevel by player.
         */
        @JvmStatic
        fun getLevelByPlayer(player: UUID): PlayerLevel {
            return levelByPlayer.getOrDefault(player, PlayerLevel(player, 1, 0))
        }
    }
}
