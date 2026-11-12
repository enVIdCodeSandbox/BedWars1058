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

package com.andrei1058.bedwars.arena.spectator

import com.andrei1058.bedwars.BedWars
import com.andrei1058.bedwars.BedWars.Companion.nms
import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.api.events.player.PlayerKillEvent
import com.andrei1058.bedwars.api.events.player.PlayerLeaveArenaEvent
import com.andrei1058.bedwars.api.events.spectator.SpectatorFirstPersonEnterEvent
import com.andrei1058.bedwars.api.events.spectator.SpectatorFirstPersonLeaveEvent
import com.andrei1058.bedwars.api.events.spectator.SpectatorTeleportToPlayerEvent
import com.andrei1058.bedwars.api.language.Language.Companion.getMsg
import com.andrei1058.bedwars.api.language.Messages
import com.andrei1058.bedwars.arena.Arena
import com.andrei1058.bedwars.configuration.Sounds
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.entity.TNTPrimed
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityRegainHealthEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.projectiles.ProjectileSource

open class SpectatorListeners : Listener {
    @EventHandler
    open fun onSpectatorItemInteract(e: PlayerInteractEvent) {
        val p = e.getPlayer()
        val i: ItemStack? = nms.getItemInHand(p)
        if (i == null) {
            return
        }
        if (i.getType() == Material.AIR) {
            return
        }
        if (!nms.isCustomBedWarsItem(i)) {
            return
        }
        val a: IArena? = Arena.getArenaByPlayer(p)
        if (a == null) {
            return
        }
        if (!a.isSpectator(p)) {
            return
        }
        // Disable spectator interact
        e.setCancelled(true)
    }

    @EventHandler
    open fun onSpectatorBlockInteract(e: PlayerInteractEvent) {
        if (e.getClickedBlock() == null) {
            return
        }
        if (!BedWars.getAPI().getArenaUtil().isSpectating(e.getPlayer())) {
            return
        }
        if (e.getClickedBlock().getType().toString().contains("DOOR")) {
            // Disable spectator interact
            e.setCancelled(true)
        }
    }

    @EventHandler
    open fun onSpectatorInventoryClose(e: InventoryCloseEvent) {
        val p = e.getPlayer() as Player
        TeleporterGUI.closeGUI(p)
    }

    @EventHandler
    open fun onSpectatorClick(e: InventoryClickEvent) {
        if (e.getWhoClicked().getGameMode() == GameMode.SPECTATOR) {
            e.setCancelled(true)
            return
        }
        if (e.getCurrentItem() == null) {
            return
        }
        val i = e.getCurrentItem()
        if (i.getType() == Material.AIR) {
            return
        }
        val p = e.getWhoClicked() as Player
        val a: IArena? = Arena.getArenaByPlayer(p)
        if (a == null) {
            return
        }
        if (!a.isSpectator(p)) {
            return
        }
        // Teleporter heads
        if (nms.isPlayerHead(i.getType().toString(), 3) && nms.itemStackDataCompare(i, 3.toShort())) {
            if (nms.isCustomBedWarsItem(i)) {
                e.setCancelled(true)
                val data = nms.getCustomData(i)
                if (data.contains(TeleporterGUI.NBT_SPECTATOR_TELEPORTER_GUI_HEAD)) {
                    val player = data.replace(TeleporterGUI.NBT_SPECTATOR_TELEPORTER_GUI_HEAD, "")
                    val target: Player? = Bukkit.getPlayer(player)
                    if (target == null) {
                        return
                    }
                    if (target.isDead()) {
                        return
                    }
                    if (!target.isOnline()) {
                        return
                    }
                    val event = SpectatorTeleportToPlayerEvent(p, target, a)
                    Bukkit.getPluginManager().callEvent(event)
                    if (!event.isCancelled()) {
                        p.teleport(target)
                    }
                    Sounds.playSound("spectator-gui-click", p)
                    p.closeInventory()
                }
            }
        }
    }

    @EventHandler
    open fun onHealthChange(e: EntityRegainHealthEvent) {
        if (e.getEntity().getType() != EntityType.PLAYER) {
            return
        }
        val p = e.getEntity() as Player
        val arena: IArena? = Arena.getArenaByPlayer(p)
        if (arena == null) {
            return
        }
        if (arena.isPlayer(p)) {
            TeleporterGUI.refreshAllGUIs()
        }
    }

    @EventHandler
    open fun onFoodChange(e: FoodLevelChangeEvent) {
        if (e.getEntity().getType() != EntityType.PLAYER) {
            return
        }
        val p = e.getEntity() as Player
        val arena: IArena? = Arena.getArenaByPlayer(p)
        if (arena == null) {
            return
        }
        if (arena.isPlayer(p)) {
            TeleporterGUI.refreshAllGUIs()
        }
    }

    @EventHandler
    open fun onPlayerLeave(e: PlayerLeaveArenaEvent) {
        if (e.getArena().isPlayer(e.getPlayer())) {
            TeleporterGUI.refreshAllGUIs()
        }
    }

    @EventHandler
    open fun onSpectatorInteractPlayer(e: PlayerInteractEntityEvent) {
        if (e.getRightClicked().getType() != EntityType.PLAYER) {
            return
        }
        val p = e.getPlayer()
        val a: IArena? = Arena.getArenaByPlayer(p)
        if (a == null) {
            return
        }
        if (a.isPlayer(p)) {
            return
        }
        e.setCancelled(true)
        val target = e.getRightClicked() as Player
        if (a.isPlayer(target)) {
            if (p.getSpectatorTarget() != null) {
                val e2 = SpectatorFirstPersonLeaveEvent(p, a, { player -> getMsg(player, Messages.ARENA_SPECTATOR_FIRST_PERSON_LEAVE_TITLE) }, { player -> getMsg(player, Messages.ARENA_SPECTATOR_FIRST_PERSON_LEAVE_SUBTITLE) })
                Bukkit.getPluginManager().callEvent(e2)
            }
            val event = SpectatorFirstPersonEnterEvent(p, target, a, { player -> getMsg(player, Messages.ARENA_SPECTATOR_FIRST_PERSON_ENTER_TITLE) }, { player -> getMsg(player, Messages.ARENA_SPECTATOR_FIRST_PERSON_ENTER_SUBTITLE) })
            Bukkit.getPluginManager().callEvent(event)
            if (event.isCancelled()) {
                return
            }
            p.getInventory().setHeldItemSlot(5)
            p.setGameMode(GameMode.SPECTATOR)
            p.setSpectatorTarget(target)
            nms.sendTitle(p, event.getTitle().apply(p)!!.replace("{playername}", p.getName()).replace("{player}", target.getDisplayName()), event.getSubTitle().apply(p)!!.replace("{player}", target.getDisplayName()), event.getFadeIn(), event.getStay(), event.getFadeOut())
        }
    }

    @EventHandler
    open fun onSneak(e: PlayerToggleSneakEvent) {
        val p = e.getPlayer()
        val a: IArena? = Arena.getArenaByPlayer(p)
        if (a == null) {
            return
        }
        if (a.isSpectator(p) && p.getSpectatorTarget() != null) {
            p.setGameMode(GameMode.ADVENTURE)
            p.setAllowFlight(true)
            p.setFlying(true)
            val event = SpectatorFirstPersonLeaveEvent(p, a, { player -> getMsg(player, Messages.ARENA_SPECTATOR_FIRST_PERSON_LEAVE_TITLE) }, { player -> getMsg(player, Messages.ARENA_SPECTATOR_FIRST_PERSON_LEAVE_SUBTITLE) })
            Bukkit.getPluginManager().callEvent(event)
            nms.sendTitle(p, event.getTitle().apply(p), event.getSubTitle().apply(p), event.getFadeIn(), event.getStay(), event.getFadeOut())
        }
    }

    @EventHandler
    open fun onTeleport(e: PlayerTeleportEvent) {
        val a: IArena? = Arena.getArenaByPlayer(e.getPlayer())
        if (a == null) {
            return
        }
        if (a.isSpectator(e.getPlayer())) {
            if (!(e.getTo().getWorld() == e.getPlayer().getWorld()) && e.getCause() == PlayerTeleportEvent.TeleportCause.SPECTATE) {
                val p = e.getPlayer()
                e.setCancelled(true)
                p.setGameMode(GameMode.ADVENTURE)
                p.setAllowFlight(true)
                p.setFlying(true)
                val event = SpectatorFirstPersonLeaveEvent(p, Arena.getArenaByPlayer(p), { player -> getMsg(player, Messages.ARENA_SPECTATOR_FIRST_PERSON_LEAVE_TITLE) }, { player -> getMsg(player, Messages.ARENA_SPECTATOR_FIRST_PERSON_LEAVE_SUBTITLE) })
                Bukkit.getPluginManager().callEvent(event)
                nms.sendTitle(p, event.getTitle().apply(p), event.getSubTitle().apply(p), event.getFadeIn(), event.getStay(), event.getFadeOut())
            }
        }
    }

    @EventHandler
    open fun onTargetDeath(e: PlayerKillEvent) {
        for (p in e.getArena().getSpectators()!!) {
            if (p.getSpectatorTarget() == null) {
                continue
            }
            if (p.getSpectatorTarget() === e.getVictim()) {
                p.setGameMode(GameMode.ADVENTURE)
                p.setAllowFlight(true)
                p.setFlying(true)
                val event = SpectatorFirstPersonLeaveEvent(p, e.getArena(), { player -> getMsg(player, Messages.ARENA_SPECTATOR_FIRST_PERSON_LEAVE_TITLE) }, { player -> getMsg(player, Messages.ARENA_SPECTATOR_FIRST_PERSON_LEAVE_SUBTITLE) })
                Bukkit.getPluginManager().callEvent(event)
                nms.sendTitle(p, event.getTitle().apply(p), event.getSubTitle().apply(p), event.getFadeIn(), event.getStay(), event.getFadeOut())
            }
        }
    }

    @EventHandler
    open fun onDamageByEntity(e: EntityDamageByEntityEvent) {
        if (e.isCancelled()) {
            return
        }
        val a: IArena? = Arena.getArenaByIdentifier(e.getEntity().getWorld().getName())
        if (a == null) {
            return
        }
        var damager: Player? = null
        if (e.getDamager() is Projectile) {
            val shooter = (e.getDamager() as Projectile).getShooter()
            if (shooter is Player) {
                damager = shooter as Player
            }
        } else if (e.getDamager() is Player) {
            damager = e.getDamager() as Player
            if (a.getRespawnSessions()!!.containsKey(damager)) {
                e.setCancelled(true)
                return
            }
        } else if (e.getDamager() is TNTPrimed) {
            val tnt = e.getDamager() as TNTPrimed
            if (tnt.getSource() is Player) {
                damager = tnt.getSource() as Player
            }
        }
        if (damager == null) {
            return
        }
        if (a.isSpectator(damager)) {
            e.setCancelled(true)
        }
    }
}
