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

package com.andrei1058.bedwars.upgrades.menu

import com.andrei1058.bedwars.BedWars
import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.api.arena.team.ITeam
import com.andrei1058.bedwars.api.language.Language
import com.andrei1058.bedwars.api.language.Messages
import com.andrei1058.bedwars.api.upgrades.EnemyBaseEnterTrap
import com.andrei1058.bedwars.api.upgrades.MenuContent
import com.andrei1058.bedwars.api.upgrades.TeamUpgrade
import com.andrei1058.bedwars.api.upgrades.UpgradesIndex
import com.andrei1058.bedwars.arena.Arena
import com.andrei1058.bedwars.upgrades.UpgradesManager
import com.google.common.collect.ImmutableMap
import java.util.HashMap
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory

open class InternalMenu(groupName: String) : UpgradesIndex {
    private var name: String
    private var menuContentBySlot: HashMap<Int, MenuContent> = HashMap()

    override fun open(player: Player) {
        val a: IArena? = Arena.getArenaByPlayer(player)
        if (a == null) {
            return
        }
        if (!a.isPlayer(player)) {
            return
        }
        val team: ITeam? = a.getTeam(player)
        if (team == null) {
            return
        }
        if (!BedWars.getAPI().getArenaUtil().isPlaying(player)) {
            return
        }
        val inv = Bukkit.createInventory(null, 45, Language.getMsg(player, Messages.UPGRADES_MENU_GUI_NAME_PATH + name))
        for (entry in menuContentBySlot.entries) {
            inv.setItem(entry.key, entry.value.getDisplayItem(player, team))
        }
        player.openInventory(inv)
        UpgradesManager.setWatchingUpgrades(player.getUniqueId())
    }

    override fun getName(): String {
        return name
    }

    override fun addContent(content: MenuContent, slot: Int): Boolean {
        if (menuContentBySlot.get(slot) != null) {
            return false
        }
        menuContentBySlot.put(slot, content)
        return true
    }

    override fun countTiers(): Int {
        var count: Int = 0
        for (content in menuContentBySlot.values) {
            if ((content is TeamUpgrade) && content !is EnemyBaseEnterTrap) {
                val tu = content as TeamUpgrade
                count += tu.getTierCount()
            }
        }
        return count
    }

    override fun getMenuContentBySlot(): ImmutableMap<Int, MenuContent> {
        return ImmutableMap.copyOf(menuContentBySlot)
    }

    init {
        this.name = groupName.lowercase()
        Language.saveIfNotExists(Messages.UPGRADES_MENU_GUI_NAME_PATH + groupName.lowercase(), "&8Upgrades & Traps")
    }
}
