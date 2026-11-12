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
import com.andrei1058.bedwars.api.arena.team.ITeam
import com.andrei1058.bedwars.api.language.Language
import com.andrei1058.bedwars.api.language.Messages
import com.andrei1058.bedwars.api.upgrades.MenuContent
import com.andrei1058.bedwars.upgrades.UpgradesManager
import java.util.ArrayList
import java.util.Collections
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import com.andrei1058.bedwars.api.util.uninitialized

open class MenuSeparator : MenuContent {
    private var displayItem: ItemStack = uninitialized()
    private var name: String? = null
    private var playerCommands: MutableList<String> = ArrayList()
    private var consoleCommands: MutableList<String> = ArrayList()

    /**
     * Create a separator.
     *
     * @param displayItem display item.
     */
    constructor(name: String?, displayItem: ItemStack) {
        if (name == null) {
            return
        }
        this.displayItem = BedWars.nms.addCustomData(displayItem, "MCONT_" + name)
        this.name = name
        Language.saveIfNotExists(Messages.UPGRADES_SEPARATOR_ITEM_NAME_PATH + name.replace("separator-", ""), "&cName not set")
        Language.saveIfNotExists(Messages.UPGRADES_SEPARATOR_ITEM_LORE_PATH + name.replace("separator-", ""), Collections.singletonList("&cLore not set"))
        if (UpgradesManager.getConfiguration().getYml().getStringList(name + ".on-click.player") != null) {
            playerCommands.addAll(UpgradesManager.getConfiguration().getYml().getStringList(name + ".on-click.player"))
        }
        if (UpgradesManager.getConfiguration().getYml().getStringList(name + ".on-click.console") != null) {
            consoleCommands.addAll(UpgradesManager.getConfiguration().getYml().getStringList(name + ".on-click.console"))
        }
    }

    override fun getDisplayItem(player: Player, team: ITeam): ItemStack {
        val i = ItemStack(displayItem)
        val im: ItemMeta? = i.getItemMeta()
        if (im != null) {
            im.setDisplayName(Language.getMsg(player, Messages.UPGRADES_SEPARATOR_ITEM_NAME_PATH + name!!.replace("separator-", "")))
            im.setLore(Language.getList(player, Messages.UPGRADES_SEPARATOR_ITEM_LORE_PATH + name!!.replace("separator-", "")))
            im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
            i.setItemMeta(im)
        }
        return i
    }

    override fun onClick(player: Player, clickType: ClickType, team: ITeam?) {
        for (cmd in playerCommands) {
            if (cmd.trim().isEmpty()) {
                continue
            }
            Bukkit.dispatchCommand(player, cmd.replace("{playername}", player.getName()).replace("{player}", player.getDisplayName()).replace("{team}", if (team == null) "null" else team.getDisplayName(Language.getPlayerLanguage(player))))
        }
        for (cmd in consoleCommands) {
            if (cmd.trim().isEmpty()) {
                continue
            }
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("{playername}", player.getName()).replace("{player}", player.getDisplayName()).replace("{team}", if (team == null) "null" else team.getDisplayName(Language.getPlayerLanguage(player))))
        }
    }

    override fun getName(): String? {
        return name
    }
}
