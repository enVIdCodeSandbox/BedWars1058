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

package com.andrei1058.bedwars.listeners.chat

import com.andrei1058.bedwars.BedWars
import com.andrei1058.bedwars.BedWars.Companion.config
import com.andrei1058.bedwars.BedWars.Companion.getChatSupport
import com.andrei1058.bedwars.BedWars.Companion.getLevelSupport
import com.andrei1058.bedwars.BedWars.Companion.getLobbyWorld
import com.andrei1058.bedwars.BedWars.Companion.getServerType
import com.andrei1058.bedwars.api.arena.GameState
import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.api.arena.team.ITeam
import com.andrei1058.bedwars.api.configuration.ConfigPath
import com.andrei1058.bedwars.api.language.Language
import com.andrei1058.bedwars.api.language.Language.Companion.getMsg
import com.andrei1058.bedwars.api.language.Language.Companion.getPlayerLanguage
import com.andrei1058.bedwars.api.language.Messages
import com.andrei1058.bedwars.api.server.ServerType
import com.andrei1058.bedwars.arena.Arena
import com.andrei1058.bedwars.commands.shout.ShoutCommand
import com.andrei1058.bedwars.configuration.Permissions
import com.andrei1058.bedwars.support.papi.SupportPAPI
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import com.andrei1058.bedwars.api.util.unchecked

open class ChatFormatting : Listener {
    @EventHandler(ignoreCancelled = true)
    open fun onChat(e: AsyncPlayerChatEvent?) {
        if (e == null) {
            return
        }
        val p = e.getPlayer()
        // in shared mode we don't want messages from outside the arena to be seen in game
        if (getServerType() == ServerType.SHARED && Arena.getArenaByPlayer(p) == null) {
            e.getRecipients().removeIf({ pl -> Arena.getArenaByPlayer(pl) != null })
            return
        }
        // handle chat color. we would need to work on permission inheritance
        if (Permissions.hasPermission(p, Permissions.PERMISSION_CHAT_COLOR, Permissions.PERMISSION_VIP, Permissions.PERMISSION_ALL)) {
            e.setMessage(ChatColor.translateAlternateColorCodes('&', e.getMessage()))
        }
        val language = getPlayerLanguage(p)
        // handle lobby world for multi arena
        if (getServerType() == ServerType.MULTIARENA && p.getWorld().getName().equals(BedWars.getLobbyWorld(), ignoreCase = true)) {
            setRecipients(e, p.getWorld().getPlayers())
        }
        // handle arena chat
        if (Arena.getArenaByPlayer(p) != null) {
            val a = Arena.getArenaByPlayer(p)
            // spectator chat
            if (a.isSpectator(p)) {
                setRecipients(e, a.getSpectators().unchecked())
                e.setFormat(parsePHolders(language.m(Messages.FORMATTING_CHAT_SPECTATOR), p, null))
                return
            }
            // arena lobby chat
            if (a.getStatus() == GameState.waiting || a.getStatus() == GameState.starting) {
                setRecipients(e, a.getPlayers().unchecked())
                e.setFormat(parsePHolders(language.m(Messages.FORMATTING_CHAT_WAITING), p, null))
                return
            }
            val team = a.getTeam(p)
            var msg = e.getMessage()
            // shout format
            if (isShouting(msg, language)) {
                if (!(p.hasPermission(Permissions.PERMISSION_SHOUT_COMMAND) || p.hasPermission(Permissions.PERMISSION_ALL))) {
                    e.setCancelled(true)
                    p.sendMessage(Language.getMsg(p, Messages.COMMAND_NOT_FOUND_OR_INSUFF_PERMS))
                    return
                }
                if (ShoutCommand.isShoutCooldown(p)) {
                    e.setCancelled(true)
                    p.sendMessage(language.m(Messages.COMMAND_COOLDOWN).replace("{seconds}", Math.round(ShoutCommand.getShoutCooldown(p)).toString()))
                    return
                }
                ShoutCommand.updateShout(p)
                setRecipients(e, a.getPlayers().unchecked(), a.getSpectators().unchecked())
                msg = clearShout(msg, language)
                if (msg.isEmpty()) {
                    e.setCancelled(true)
                    return
                }
                e.setMessage(msg)
                e.setFormat(parsePHolders(language.m(Messages.FORMATTING_CHAT_SHOUT), p, team))
                return
            }
            // player team chat
            if (a.getMaxInTeam() == 1) {
                setRecipients(e, a.getPlayers().unchecked(), a.getSpectators().unchecked())
            } else {
                setRecipients(e, team!!.getMembers().unchecked())
            }
            e.setFormat(parsePHolders(language.m(Messages.FORMATTING_CHAT_TEAM), p, team))
            return
        }
        // multi arena lobby chat
        e.setFormat(parsePHolders(language.m(Messages.FORMATTING_CHAT_LOBBY), p, null))
    }

    companion object {
        @JvmStatic
        private fun parsePHolders(content: String, player: Player, team: ITeam?): String {
            var content = content
            content = content.replace("{vPrefix}", getChatSupport().getPrefix(player)).replace("{vSuffix}", getChatSupport().getSuffix(player)).replace("{playername}", player.getName()).replace("{level}", getLevelSupport().getLevel(player)).replace("{player}", player.getDisplayName())
            if (team != null) {
                val teamFormat = getMsg(player, Messages.FORMAT_PAPI_PLAYER_TEAM_TEAM).replace("{TeamColor}", team.getColor().chat().toString() + "").replace("{TeamName}", team.getDisplayName(Language.getPlayerLanguage(player)).uppercase())
                content = content.replace("{team}", teamFormat)
            }
            return SupportPAPI.getSupportPAPI().replace(player, content).replace("{message}", "%2\$s")
        }

        @JvmStatic
        private fun isShouting(msg: String, lang: Language): Boolean {
            return msg.startsWith("!") || msg.startsWith("shout") || msg.startsWith("SHOUT") || msg.startsWith(lang.m(Messages.MEANING_SHOUT))
        }

        @JvmStatic
        private fun clearShout(msg: String, lang: Language): String {
            var msg = msg
            if (msg.startsWith("!")) {
                msg = msg.replaceFirst("!".toRegex(), "")
            }
            if (msg.startsWith("SHOUT")) {
                msg = msg.replaceFirst("SHOUT".toRegex(), "")
            }
            if (msg.startsWith("shout")) {
                msg = msg.replaceFirst("shout".toRegex(), "")
            }
            if (msg.startsWith(lang.m(Messages.MEANING_SHOUT))) {
                msg = msg.replaceFirst(Regex(lang.m(Messages.MEANING_SHOUT)), "")
            }
            return msg.trim()
        }

        @JvmStatic
        fun setRecipients(event: AsyncPlayerChatEvent, vararg target: MutableList<Player>) {
            if (!config.getBoolean(ConfigPath.GENERAL_CHAT_GLOBAL)) {
                event.getRecipients().clear()
                for (list in target) {
                    event.getRecipients().addAll(list)
                }
            }
        }
    }
}
