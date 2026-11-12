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

package com.andrei1058.bedwars.listeners

import com.andrei1058.bedwars.BedWars
import com.andrei1058.bedwars.BedWars.Companion.nms
import com.andrei1058.bedwars.api.arena.GameState
import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.api.events.gameplay.GameStateChangeEvent
import com.andrei1058.bedwars.api.language.Language
import com.andrei1058.bedwars.api.language.Messages
import com.andrei1058.bedwars.api.server.ServerType
import com.andrei1058.bedwars.api.server.SetupType
import com.andrei1058.bedwars.arena.Arena
import com.andrei1058.bedwars.arena.SetupSession
import java.util.Objects
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryAction.HOTBAR_SWAP
import org.bukkit.event.inventory.InventoryAction.MOVE_TO_OTHER_INVENTORY
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffectType

open class Inventory : Listener {
    @EventHandler
    open fun onClose(e: InventoryCloseEvent) {
        val p = e.getPlayer() as Player
        if (nms.getInventoryName(e).equals(SetupSession.getInvName(), ignoreCase = true)) {
            val ss: SetupSession? = SetupSession.getSession(p.getUniqueId())
            if (ss != null) {
                if (ss.getSetupType() == null) {
                    ss.cancel()
                }
            }
        }
    }

    /**
     * Manage command-items when clicked in inventory
     */
    @EventHandler
    open fun onCommandItemClick(e: InventoryClickEvent) {
        //block moving from hotBar
        if (e.getAction() == HOTBAR_SWAP && e.getClick() == ClickType.NUMBER_KEY) {
            if (e.getHotbarButton() > -1) {
                val i: ItemStack? = e.getWhoClicked().getInventory().getItem(e.getHotbarButton())
                if (i != null) {
                    if (isCommandItem(i)) {
                        e.setCancelled(true)
                        return
                    }
                }
            }
        }
        //block moving cursor item outside
        if (e.getCursor() != null) {
            if (e.getCursor().getType() != Material.AIR) {
                if (e.getClickedInventory() == null) {
                    if (isCommandItem(e.getCursor())) {
                        e.getWhoClicked().closeInventory()
                        e.setCancelled(true)
                    }
                } else if (e.getClickedInventory().getType() != e.getWhoClicked().getInventory().getType()) {
                    if (isCommandItem(e.getCursor())) {
                        e.getWhoClicked().closeInventory()
                        e.setCancelled(true)
                    }
                } else {
                    if (isCommandItem(e.getCursor())) {
                        e.setCancelled(true)
                    }
                }
            }
        }
        //block moving current item outside
        if (e.getCurrentItem() != null) {
            if (e.getCurrentItem().getType() != Material.AIR) {
                if (e.getClickedInventory() == null) {
                    if (isCommandItem(e.getCurrentItem())) {
                        e.getWhoClicked().closeInventory()
                        e.setCancelled(true)
                    }
                } else if (e.getClickedInventory().getType() != e.getWhoClicked().getInventory().getType()) {
                    if (isCommandItem(e.getCurrentItem())) {
                        e.getWhoClicked().closeInventory()
                        e.setCancelled(true)
                    }
                } else {
                    if (isCommandItem(e.getCurrentItem())) {
                        e.setCancelled(true)
                    }
                }
            }
        }
        //block moving with shift
        if (e.getAction() == MOVE_TO_OTHER_INVENTORY) {
            if (isCommandItem(e.getCurrentItem())) {
                e.setCancelled(true)
            }
        }
    }

    @EventHandler
    open fun onClick(e: InventoryClickEvent) {
        //issue #225
        if (e.getSlotType() == InventoryType.SlotType.ARMOR) {
            if (Arena.getArenaByPlayer(e.getWhoClicked() as Player) != null) {
                if (e.getWhoClicked().hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                    e.getWhoClicked().closeInventory()
                    for (pl in e.getWhoClicked().getWorld().getPlayers()) {
                        BedWars.nms.hideArmor(e.getWhoClicked() as Player, pl)
                    }
                }
            }
        }
        if (e.getCurrentItem() == null) {
            return
        }
        if (e.getCurrentItem().getType() == Material.AIR) {
            return
        }
        val p = e.getWhoClicked() as Player
        val i = e.getCurrentItem()
        val a: IArena? = Arena.getArenaByPlayer(p)
        if (a != null) {
            //Prevent players from moving items in stats GUI
            if (nms.getInventoryName(e) == Language.getMsg(p, Messages.PLAYER_STATS_GUI_INV_NAME).replace("{playername}", p.getName()).replace("{player}", p.getDisplayName())) {
                e.setCancelled(true)
                return
            }
            /* Make it so they can't toggle their armor */
            if (e.getSlotType() == InventoryType.SlotType.ARMOR) {
                e.setCancelled(true)
                return
            }
        }
        if (!i.hasItemMeta()) {
            return
        }
        if (!i.getItemMeta().hasDisplayName()) {
            return
        }
        if (BedWars.getServerType() == ServerType.MULTIARENA) {
            if (e.getWhoClicked().getLocation().getWorld().getName().equals(BedWars.getLobbyWorld(), ignoreCase = true)) {
                e.setCancelled(true)
            }
        }
        /* Check setup gui items */
        if (SetupSession.isInSetupSession(p.getUniqueId()) && nms.getInventoryName(e).equals(SetupSession.getInvName(), ignoreCase = true)) {
            val ss = SetupSession.getSession(p.getUniqueId())
            if (e.getSlot() == SetupSession.getAdvancedSlot()) {
                Objects.requireNonNull(ss)!!.setSetupType(SetupType.ADVANCED)
            } else if (e.getSlot() == SetupSession.getAssistedSlot()) {
                Objects.requireNonNull(ss)!!.setSetupType(SetupType.ASSISTED)
            }
            if (!Objects.requireNonNull(ss)!!.startSetup()) {
                ss!!.getPlayer().sendMessage(ChatColor.RED.toString() + "Could not start setup session. Pleas check the console.")
            }
            p.closeInventory()
            return
        }
        if (a != null) {
            if (a.isSpectator(p)) {
                e.setCancelled(true)
                //noinspection UnnecessaryReturnStatement
                return
            }
        }
    }

    @EventHandler
    open fun onGameEnd(e: GameStateChangeEvent) {
        if (e.getNewState() != GameState.restarting) {
            return
        }
        // close any open guis when the game ends (e.g. shop)
        e.getArena().getPlayers()!!.forEach(Player::closeInventory)
    }

    companion object {
        /**
         * Check if an item is command-item
         */
        @JvmStatic
        private fun isCommandItem(i: ItemStack?): Boolean {
            if (i == null) {
                return false
            }
            if (i.getType() == Material.AIR) {
                return false
            }
            if (nms.isCustomBedWarsItem(i)) {
                val customData = nms.getCustomData(i).split("_".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                if (customData.size >= 2) {
                    return customData[0] == "RUNCOMMAND"
                }
            }
            return false
        }
    }
}
