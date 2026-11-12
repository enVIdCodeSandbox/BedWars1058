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

package com.andrei1058.bedwars.database

import com.andrei1058.bedwars.BedWars
import com.andrei1058.bedwars.api.language.Language
import com.andrei1058.bedwars.shop.quickbuy.QuickBuyElement
import com.andrei1058.bedwars.stats.PlayerStats
import java.io.File
import java.io.IOException
import java.sql.*
import java.util.HashMap
import java.util.UUID
import com.andrei1058.bedwars.api.util.unchecked
import com.andrei1058.bedwars.api.util.uninitialized

open class SQLite : Database {
    private var url: String = uninitialized()
    private var connection: Connection? = null

    constructor() {
        val folder = File(BedWars.plugin.getDataFolder().toString() + "/Cache")
        if (!folder.exists()) {
            if (!folder.mkdir()) {
                BedWars.plugin.getLogger().severe("Could not create /Cache folder!")
            }
        }
        val dataFolder = File(folder.getPath() + "/shop.db")
        if (!dataFolder.exists()) {
            try {
                if (!dataFolder.createNewFile()) {
                    BedWars.plugin.getLogger().severe("Could not create /Cache/shop.db file!")
                }
            } catch (e: IOException) {
                e.printStackTrace()
                return
            }
        }
        this.url = "jdbc:sqlite:" + dataFolder
        try {
            Class.forName("org.sqlite.JDBC")
            DriverManager.getConnection(url)
        } catch (e: SQLException) {
            if (e is ClassNotFoundException) {
                BedWars.plugin.getLogger().severe("Could Not Found SQLite Driver on your system!")
            }
            e.printStackTrace()
        } catch (e: ClassNotFoundException) {
            if (e is ClassNotFoundException) {
                BedWars.plugin.getLogger().severe("Could Not Found SQLite Driver on your system!")
            }
            e.printStackTrace()
        }
    }

    override fun init() {
        var sql: String
        try {
            checkConnection()
            sql = "CREATE TABLE IF NOT EXISTS global_stats (id INTEGER PRIMARY KEY AUTOINCREMENT, " + "name VARCHAR(200), uuid VARCHAR(36), first_play TIMESTAMP NULL DEFAULT NULL, " + "last_play TIMESTAMP DEFAULT NULL, wins INTEGER(10), kills INTEGER(10), " + "final_kills INTEGER(10), looses INTEGER(10), deaths INTEGER(10), final_deaths INTEGER(10), beds_destroyed INTEGER(10), games_played INTEGER(10));"
            connection!!.createStatement().use { statement ->
                statement.executeUpdate(sql)
            }
            connection!!.createStatement().use { st ->
                sql = "CREATE TABLE IF NOT EXISTS quick_buy_2 (uuid VARCHAR(36) PRIMARY KEY, " + "slot_19 VARCHAR(200), slot_20 VARCHAR(200), slot_21 VARCHAR(200), slot_22 VARCHAR(200), slot_23 VARCHAR(200), slot_24 VARCHAR(200), slot_25 VARCHAR(200)," + "slot_28 VARCHAR(200), slot_29 VARCHAR(200), slot_30 VARCHAR(200), slot_31 VARCHAR(200), slot_32 VARCHAR(200), slot_33 VARCHAR(200), slot_34 VARCHAR(200)," + "slot_37 VARCHAR(200), slot_38 VARCHAR(200), slot_39 VARCHAR(200), slot_40 VARCHAR(200), slot_41 VARCHAR(200), slot_42 VARCHAR(200), slot_43 VARCHAR(200));"
                st.executeUpdate(sql)
            }
            connection!!.createStatement().use { st ->
                sql = "CREATE TABLE IF NOT EXISTS player_levels (id INTEGER PRIMARY KEY AUTOINCREMENT, uuid VARCHAR(200), " + "level INTEGER, xp INTEGER, name VARCHAR(200), next_cost INTEGER);"
                st.executeUpdate(sql)
            }
            connection!!.createStatement().use { st ->
                sql = "CREATE TABLE IF NOT EXISTS  player_language (id INTEGER PRIMARY KEY AUTOINCREMENT, uuid VARCHAR(200), " + "iso VARCHAR(200));"
                st.executeUpdate(sql)
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    override fun hasStats(uuid: UUID): Boolean {
        val sql = "SELECT uuid FROM global_stats WHERE uuid = ?;"
        try {
            checkConnection()
            connection!!.prepareStatement(sql).use { statement ->
                statement.setString(1, uuid.toString())
                statement.executeQuery().use { result ->
                    return result.next()
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return false
    }

    override fun saveStats(stats: PlayerStats) {
        var sql: String
        try {
            checkConnection()
            if (hasStats(stats.getUuid())) {
                sql = "UPDATE global_stats SET last_play=?, wins=?, kills=?, final_kills=?, looses=?, deaths=?, final_deaths=?, beds_destroyed=?, games_played=?, name=? WHERE uuid = ?;"
                connection!!.prepareStatement(sql).use { statement ->
                    statement.setTimestamp(1, Timestamp.from(stats.getLastPlay()))
                    statement.setInt(2, stats.getWins())
                    statement.setInt(3, stats.getKills())
                    statement.setInt(4, stats.getFinalKills())
                    statement.setInt(5, stats.getLosses())
                    statement.setInt(6, stats.getDeaths())
                    statement.setInt(7, stats.getFinalDeaths())
                    statement.setInt(8, stats.getBedsDestroyed())
                    statement.setInt(9, stats.getGamesPlayed())
                    statement.setString(10, stats.getName())
                    statement.setString(11, stats.getUuid().toString())
                    statement.executeUpdate()
                }
            } else {
                sql = "INSERT INTO global_stats (name, uuid, first_play, last_play, wins, kills, final_kills, looses, deaths, final_deaths, beds_destroyed, games_played) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"
                connection!!.prepareStatement(sql).use { statement ->
                    statement.setString(1, stats.getName())
                    statement.setString(2, stats.getUuid().toString())
                    statement.setTimestamp(3, Timestamp.from(stats.getFirstPlay()))
                    statement.setTimestamp(4, Timestamp.from(stats.getLastPlay()))
                    statement.setInt(5, stats.getWins())
                    statement.setInt(6, stats.getKills())
                    statement.setInt(7, stats.getFinalKills())
                    statement.setInt(8, stats.getLosses())
                    statement.setInt(9, stats.getDeaths())
                    statement.setInt(10, stats.getFinalDeaths())
                    statement.setInt(11, stats.getBedsDestroyed())
                    statement.setInt(12, stats.getGamesPlayed())
                    statement.executeUpdate()
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    override fun fetchStats(uuid: UUID): PlayerStats {
        val stats = PlayerStats(uuid)
        val sql = "SELECT * FROM global_stats WHERE uuid = ?;"
        try {
            checkConnection()
            connection!!.prepareStatement(sql).use { statement ->
                statement.setString(1, uuid.toString())
                statement.executeQuery().use { result ->
                    if (result.next()) {
                        stats.setFirstPlay(result.getTimestamp("first_play").toInstant())
                        stats.setLastPlay(result.getTimestamp("last_play").toInstant())
                        stats.setWins(result.getInt("wins"))
                        stats.setKills(result.getInt("kills"))
                        stats.setFinalKills(result.getInt("final_kills"))
                        stats.setLosses(result.getInt("looses"))
                        stats.setDeaths(result.getInt("deaths"))
                        stats.setFinalDeaths(result.getInt("final_deaths"))
                        stats.setBedsDestroyed(result.getInt("beds_destroyed"))
                        stats.setGamesPlayed(result.getInt("games_played"))
                    }
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return stats
    }

    override fun setQuickBuySlot(p: UUID, shopPath: String, slot: Int) {
        try {
            checkConnection()
            connection!!.prepareStatement("SELECT uuid FROM quick_buy_2 WHERE uuid = ?;").use { statement ->
                statement.setString(1, p.toString())
                statement.executeQuery().use { rs ->
                    if (!rs.next()) {
                        connection!!.prepareStatement("INSERT INTO quick_buy_2 (uuid, slot_19, slot_20, slot_21, slot_22, slot_23, slot_24, slot_25, slot_28, slot_29, slot_30, slot_31, slot_32, slot_33, slot_34, slot_37, slot_38, slot_39, slot_40, slot_41, slot_42, slot_43) VALUES(?,' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ');").use { ps ->
                            ps.setString(1, p.toString())
                            ps.execute()
                        }
                    }
                    BedWars.debug("UPDATE SET SLOT " + slot + " identifier " + shopPath)
                    connection!!.prepareStatement("UPDATE quick_buy_2 SET slot_" + slot + " = ? WHERE uuid = ?;").use { ps ->
                        ps.setString(1, shopPath)
                        ps.setString(2, p.toString())
                        ps.executeUpdate()
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    override fun getQuickBuySlots(p: UUID, slot: Int): String {
        var result = ""
        try {
            checkConnection()
            connection!!.prepareStatement("SELECT slot_" + slot + " FROM quick_buy_2 WHERE uuid = ?;").use { ps ->
                ps.setString(1, p.toString())
                ps.executeQuery().use { rs ->
                    if (rs.next()) {
                        result = rs.getString("slot_" + slot)
                    }
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return result
    }

    override fun hasQuickBuy(uuid: UUID): Boolean {
        try {
            checkConnection()
            connection!!.createStatement().use { statement ->
                statement.executeQuery("SELECT uuid FROM quick_buy_2 WHERE uuid = '" + uuid.toString() + "';").use { rs ->
                    if (rs.next()) {
                        rs.close()
                        return true
                    }
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return false
    }

    override fun getColumn(player: UUID, column: String): Int {
        val sql = "SELECT ? FROM global_stats WHERE uuid = ?;"
        try {
            checkConnection()
            connection!!.prepareStatement(sql).use { statement ->
                statement.setString(1, column)
                statement.setString(2, player.toString())
                statement.executeQuery().use { result ->
                    if (result.next()) {
                        return result.getInt(column)
                    }
                }
            }
        } catch (ex: SQLException) {
            ex.printStackTrace()
            return 0
        }
        return 0
    }

    override fun getLevelData(player: UUID): Array<Any> {
        val r = arrayOf<Any>(1, 0, "", 0)
        try {
            checkConnection()
            connection!!.prepareStatement("SELECT level, xp, name, next_cost FROM player_levels WHERE uuid = ?;").use { ps ->
                ps.setString(1, player.toString())
                ps.executeQuery().use { rs ->
                    if (rs.next()) {
                        r[0] = rs.getInt("level")
                        r[1] = rs.getInt("xp")
                        r[2] = rs.getString("name")
                        r[3] = rs.getInt("next_cost")
                    }
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return r
    }

    override fun setLevelData(player: UUID, level: Int, xp: Int, displayName: String?, nextCost: Int) {
        try {
            checkConnection()
            connection!!.prepareStatement("SELECT uuid from player_levels WHERE uuid = ?;").use { pss ->
                pss.setString(1, player.toString())
                pss.executeQuery().use { rs ->
                    if (!rs.next()) {
                        connection!!.prepareStatement("INSERT INTO player_levels (uuid, level, xp, name, next_cost) VALUES (?, ?, ?, ?, ?);").use { ps ->
                            //ps.setInt(1, 0);
                            ps.setString(1, player.toString())
                            ps.setInt(2, level)
                            ps.setInt(3, xp)
                            ps.setString(4, displayName)
                            ps.setInt(5, nextCost)
                            ps.executeUpdate()
                        }
                    } else {
                        if (displayName == null) connection!!.prepareStatement("UPDATE player_levels SET level=?, xp=? WHERE uuid = '" + player.toString() + "';") else connection!!.prepareStatement("UPDATE player_levels SET level=?, xp=?, name=?, next_cost=? WHERE uuid = '" + player.toString() + "';").use { ps ->
                            ps.setInt(1, level)
                            ps.setInt(2, xp)
                            if (displayName != null) {
                                ps.setString(3, displayName)
                                ps.setInt(4, nextCost)
                            }
                            ps.executeUpdate()
                        }
                    }
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    override fun setLanguage(player: UUID, iso: String) {
        try {
            checkConnection()
            connection!!.createStatement().use { statement ->
                statement.executeQuery("SELECT iso FROM player_language WHERE uuid = '" + player.toString() + "';").use { rs ->
                    if (rs.next()) {
                        connection!!.createStatement().use { st ->
                            st.executeUpdate("UPDATE player_language SET iso='" + iso + "' WHERE uuid = '" + player.toString() + "';")
                        }
                    } else {
                        connection!!.prepareStatement("INSERT INTO player_language (uuid, iso) VALUES (?, ?);").use { st ->
                            st.setString(1, player.toString())
                            st.setString(2, iso)
                            st.execute()
                        }
                    }
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    override fun getLanguage(player: UUID): String {
        var iso = Language.getDefaultLanguage().getIso()
        try {
            checkConnection()
            connection!!.prepareStatement("SELECT iso FROM player_language WHERE uuid = ?;").use { ps ->
                ps.setString(1, player.toString())
                ps.executeQuery().use { rs ->
                    if (rs.next()) {
                        iso = rs.getString("iso")
                    }
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return iso.unchecked()
    }

    override fun pushQuickBuyChanges(updateSlots: HashMap<Int, String>, uuid: UUID, elements: MutableList<QuickBuyElement>) {
        if (updateSlots.isEmpty()) {
            return
        }
        var hasQuick: Boolean
        if (!(hasQuickBuy(uuid).also { hasQuick = it })) {
            for (element in elements) {
                if (!updateSlots.containsKey(element.getSlot())) {
                    updateSlots.put(element.getSlot(), element.getCategoryContent()!!.getIdentifier().unchecked())
                }
            }
        }
        val columns = StringBuilder()
        val values = StringBuilder()
        var i: Int = 0
        if (hasQuick) {
            for (entry in updateSlots.entries) {
                i++
                columns.append("slot_").append(entry.key).append("=?")
                if (i != updateSlots.size) {
                    columns.append(", ")
                }
            }
        } else {
            for (entry in updateSlots.entries) {
                i++
                columns.append("slot_").append(entry.key)
                values.append("?")
                if (i != updateSlots.size) {
                    columns.append(", ")
                    values.append(", ")
                }
            }
        }
        val sql: String = if (hasQuick) "UPDATE quick_buy_2 SET " + columns + " WHERE uuid=?;" else "INSERT INTO quick_buy_2 (uuid," + columns + ") VALUES (?," + values + ");"
        try {
            checkConnection()
            connection!!.prepareStatement(sql).use { ps ->
                var index: Int = if (hasQuick) 0 else 1
                for (key in updateSlots.keys) {
                    index++
                    val identifier = updateSlots.get(key)
                    ps.setString(index, if (identifier!!.trim().isEmpty()) null else identifier)
                }
                ps.setString(if (hasQuick) updateSlots.size + 1 else 1, uuid.toString())
                ps.execute()
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    override fun getQuickBuySlots(uuid: UUID, slot: IntArray): HashMap<Int, String> {
        val results: HashMap<Int, String> = HashMap()
        if (slot.size == 0) {
            return results
        }
        try {
            checkConnection()
            connection!!.prepareStatement("SELECT * FROM quick_buy_2 WHERE uuid = ?;").use { ps ->
                ps.setString(1, uuid.toString())
                ps.executeQuery().use { rs ->
                    if (rs.next()) {
                        for (i in slot) {
                            val id: String? = rs.getString("slot_" + i)
                            if (null != id && !id.isEmpty()) {
                                results.put(i, id)
                            }
                        }
                    }
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return results
    }

    @Throws(SQLException::class)
    private fun checkConnection() {
        var renew: Boolean = false
        if (this.connection == null) {
            renew = true
        } else if (this.connection!!.isClosed()) {
            renew = true
        }
        if (renew) {
            this.connection = DriverManager.getConnection(url)
        }
    }
}
