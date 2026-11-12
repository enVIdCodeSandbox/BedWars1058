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

package com.andrei1058.bedwars.lobbysocket

import com.andrei1058.bedwars.BedWars
import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.api.configuration.ConfigPath
import com.andrei1058.bedwars.arena.Arena
import com.andrei1058.bedwars.arena.Misc
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import java.io.IOException
import java.io.PrintWriter
import java.net.Socket
import java.util.ArrayList
import java.util.Scanner
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Level
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import com.andrei1058.bedwars.api.util.uninitialized

object ArenaSocket {
    @JvmField var lobbies: MutableList<String> = ArrayList()
    private val sockets: ConcurrentHashMap<String, RemoteLobby> = ConcurrentHashMap()

    /**
     * Send arena data to the lobbies.
     */
    @JvmStatic
    fun sendMessage(message: String?) {
        if (message == null) {
            return
        }
        if (message.isEmpty()) {
            return
        }
        for (lobby in lobbies) {
            val l = lobby.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (l.size != 2) {
                continue
            }
            if (!Misc.isNumber(l[1])) {
                continue
            }
            if (sockets.containsKey(lobby)) {
                sockets.get(lobby)!!.sendMessage(message)
            } else {
                try {
                    val socket = Socket(l[0], Integer.parseInt(l[1]))
                    val rl = RemoteLobby(socket, lobby)
                    if (rl.out != null) {
                        sockets.put(lobby, rl)
                        rl.sendMessage(message)
                    }
                } catch (ignored: IOException) {
                }
            }
        }
    }

    /**
     * Format message before sending it to lobbies.
     */
    @JvmStatic
    fun formatUpdateMessage(a: IArena?): String {
        if (a == null) {
            return ""
        }
        if (a.getWorldName() == null) {
            return ""
        }
        val js = JsonObject()
        js.addProperty("type", "UPDATE")
        js.addProperty("server_name", BedWars.config.getString(ConfigPath.GENERAL_CONFIGURATION_BUNGEE_OPTION_SERVER_ID))
        js.addProperty("arena_name", a.getArenaName())
        js.addProperty("arena_identifier", a.getWorldName())
        js.addProperty("arena_status", a.getStatus().toString().uppercase())
        js.addProperty("arena_current_players", a.getPlayers()!!.size)
        js.addProperty("arena_max_players", a.getMaxPlayers())
        js.addProperty("arena_max_in_team", a.getMaxInTeam())
        js.addProperty("arena_group", a.getGroup().uppercase())
        js.addProperty("spectate", a.isAllowSpectate())
        return js.toString()
    }

    open class RemoteLobby {
        @JvmField var socket: Socket? = null
        @JvmField var out: PrintWriter? = null
        @JvmField var `in`: Scanner? = null
        @JvmField var lobby: String = uninitialized()
        @JvmField var compute: Boolean = true

        constructor(socket: Socket, lobby: String) {
            this.socket = socket
            this.lobby = lobby
            try {
                out = PrintWriter(socket.getOutputStream(), true)
            } catch (ignored: IOException) {
                out = null
                return
            }
            try {
                `in` = Scanner(socket.getInputStream())
            } catch (ignored: IOException) {
                return
            }
            BedWars.debug("RemoteLobby created: " + lobby + " " + socket.toString())
            Bukkit.getScheduler().runTaskAsynchronously(BedWars.plugin, Runnable {
                while (compute) {
                    if (`in`!!.hasNext()) {
                        val msg = `in`!!.next()
                        BedWars.debug(msg)
                        if (msg.isEmpty()) {
                            continue
                        }
                        val json: JsonObject?
                        try {
                            json = JsonParser().parse(msg).getAsJsonObject()
                        } catch (e: JsonSyntaxException) {
                            BedWars.plugin.getLogger().log(Level.WARNING, "Received bad data from: " + socket.getInetAddress().toString())
                            continue
                        }
                        if (json == null) {
                            continue
                        }
                        if (!json.has("type")) {
                            continue
                        }
                        when (json.get("type").getAsString().uppercase()) {
                            "PLD" -> {
                                LoadedUser(json.get("uuid").getAsString(), json.get("arena_identifier").getAsString(), json.get("lang_iso").getAsString(), json.get("target").getAsString())
                            }
                            "Q" -> {
                                val p: Player? = Bukkit.getPlayer(json.get("name").getAsString())
                                if (p != null && p.isOnline()) {
                                    val a: IArena? = Arena.getArenaByPlayer(p)
                                    if (a != null) {
                                        val jo = JsonObject()
                                        jo.addProperty("type", "Q")
                                        jo.addProperty("name", p.getName())
                                        jo.addProperty("requester", json.get("requester").getAsString())
                                        jo.addProperty("server_name", BedWars.config.getString(ConfigPath.GENERAL_CONFIGURATION_BUNGEE_OPTION_SERVER_ID))
                                        jo.addProperty("arena_id", a.getWorldName())
                                        out!!.println(jo.toString())
                                    }
                                }
                            }
                            else -> {}
                        }
                    } else {
                        disable()
                    }
                }
            })
        }

        /**
         * Send a message to the given host with target port.
         *
         * @return true if message was sent successfully.
         */
        fun sendMessage(message: String): Boolean {
            if (socket == null) {
                disable()
                return false
            }
            if (!socket!!.isConnected()) {
                disable()
                return false
            }
            if (out == null) {
                disable()
                return false
            }
            if (`in` == null) {
                disable()
                return false
            }
            if (out!!.checkError()) {
                disable()
                return false
            }
            out!!.println(message)
            return true
        }

        fun disable() {
            compute = false
            BedWars.debug("Disabling socket: " + socket.toString())
            sockets.remove(lobby)
            try {
                socket!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            `in` = null
            out = null
        }
    }

    /**
     * Close active sockets.
     */
    @JvmStatic
    fun disable() {
        for (rl in ArrayList(sockets.values)) {
            rl.disable()
        }
    }
}
