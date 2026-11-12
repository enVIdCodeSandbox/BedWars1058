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
import com.andrei1058.bedwars.BedWars.Companion.config
import com.andrei1058.bedwars.api.language.Language
import com.andrei1058.bedwars.shop.quickbuy.QuickBuyElement
import com.andrei1058.bedwars.stats.PlayerStats
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.sql.*
import java.util.HashMap
import java.util.UUID
import java.util.concurrent.TimeUnit
import com.andrei1058.bedwars.api.util.unchecked
import com.andrei1058.bedwars.api.util.uninitialized

open class MySQL : Database {
    private var dataSource: HikariDataSource = uninitialized()
    private val host: String
    private val database: String
    private val user: String
    private val pass: String
    private val port: Int
    private val ssl: Boolean
    private val certificateVerification: Boolean
    private val poolSize: Int
    private val maxLifetime: Int

    /**
     * Creates the SQL connection pool and tries to connect.
     *
     * @return true if connected successfully.
     */
    open fun connect(): Boolean {
        val hikariConfig = HikariConfig()
        hikariConfig.setPoolName("BedWars1058MySQLPool")
        hikariConfig.setMaximumPoolSize(poolSize)
        hikariConfig.setMaxLifetime(maxLifetime * 1000L)
        hikariConfig.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database)
        hikariConfig.setUsername(user)
        hikariConfig.setPassword(pass)
        hikariConfig.addDataSourceProperty("useSSL", ssl.toString())
        if (!certificateVerification) {
            hikariConfig.addDataSourceProperty("verifyServerCertificate", false.toString())
        }
        hikariConfig.addDataSourceProperty("characterEncoding", "utf8")
        hikariConfig.addDataSourceProperty("encoding", "UTF-8")
        hikariConfig.addDataSourceProperty("useUnicode", "true")
        hikariConfig.addDataSourceProperty("rewriteBatchedStatements", "true")
        hikariConfig.addDataSourceProperty("jdbcCompliantTruncation", "false")
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true")
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "275")
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
        // Recover if connection gets interrupted
        hikariConfig.addDataSourceProperty("socketTimeout", TimeUnit.SECONDS.toMillis(30).toString())
        dataSource = HikariDataSource(hikariConfig)
        try {
            dataSource.getConnection()
        } catch (e: SQLException) {
            e.printStackTrace()
            return false
        }
        return true
    }

    override fun hasStats(uuid: UUID): Boolean {
        val sql = "SELECT uuid FROM global_stats WHERE uuid = ?;"
        try {
            dataSource.getConnection().use { connection ->
                connection.prepareStatement(sql).use { statement ->
                    statement.setString(1, uuid.toString())
                    statement.executeQuery().use { result ->
                        return result.next()
                    }
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return false
    }

    override fun init() {
        try {
            dataSource.getConnection().use { connection ->
                var sql = "CREATE TABLE IF NOT EXISTS global_stats (id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, " + "name VARCHAR(200), uuid VARCHAR(200), first_play TIMESTAMP NULL DEFAULT NULL, " + "last_play TIMESTAMP NULL DEFAULT NULL, wins INT(200), kills INT(200), " + "final_kills INT(200), looses INT(200), deaths INT(200), final_deaths INT(200), beds_destroyed INT(200), games_played INT(200));"
                connection.createStatement().use { statement ->
                    statement.executeUpdate(sql)
                }
                sql = "CREATE TABLE IF NOT EXISTS quick_buy_2 (uuid VARCHAR(36) PRIMARY KEY, " + "slot_19 VARCHAR(200), slot_20 VARCHAR(200), slot_21 VARCHAR(200), slot_22 VARCHAR(200), slot_23 VARCHAR(200), slot_24 VARCHAR(200), slot_25 VARCHAR(200)," + "slot_28 VARCHAR(200), slot_29 VARCHAR(200), slot_30 VARCHAR(200), slot_31 VARCHAR(200), slot_32 VARCHAR(200), slot_33 VARCHAR(200), slot_34 VARCHAR(200)," + "slot_37 VARCHAR(200), slot_38 VARCHAR(200), slot_39 VARCHAR(200), slot_40 VARCHAR(200), slot_41 VARCHAR(200), slot_42 VARCHAR(200), slot_43 VARCHAR(200));"
                connection.createStatement().use { statement ->
                    statement.executeUpdate(sql)
                }
                sql = "CREATE TABLE IF NOT EXISTS player_levels (id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, uuid VARCHAR(200), " + "level INT(200), xp INT(200), name VARCHAR(200) CHARACTER SET utf8, next_cost INT(200));"
                connection.createStatement().use { statement ->
                    statement.executeUpdate(sql)
                }
                sql = "CREATE TABLE IF NOT EXISTS player_language (id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, uuid VARCHAR(200), iso VARCHAR(200));"
                connection.createStatement().use { statement ->
                    statement.executeUpdate(sql)
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    override fun saveStats(stats: PlayerStats) {
        var sql: String
        try {
            dataSource.getConnection().use { connection ->
                if (hasStats(stats.getUuid())) {
                    sql = "UPDATE global_stats SET first_play=?, last_play=?, wins=?, kills=?, final_kills=?, looses=?, deaths=?, final_deaths=?, beds_destroyed=?, games_played=?, name=? WHERE uuid = ?;"
                    connection.prepareStatement(sql).use { statement ->
                        statement.setTimestamp(1, if (stats.getFirstPlay() != null) Timestamp.from(stats.getFirstPlay()) else null)
                        statement.setTimestamp(2, if (stats.getLastPlay() != null) Timestamp.from(stats.getLastPlay()) else null)
                        statement.setInt(3, stats.getWins())
                        statement.setInt(4, stats.getKills())
                        statement.setInt(5, stats.getFinalKills())
                        statement.setInt(6, stats.getLosses())
                        statement.setInt(7, stats.getDeaths())
                        statement.setInt(8, stats.getFinalDeaths())
                        statement.setInt(9, stats.getBedsDestroyed())
                        statement.setInt(10, stats.getGamesPlayed())
                        statement.setString(11, stats.getName())
                        statement.setString(12, stats.getUuid().toString())
                        statement.executeUpdate()
                    }
                } else {
                    sql = "INSERT INTO global_stats (name, uuid, first_play, last_play, wins, kills, final_kills, looses, deaths, final_deaths, beds_destroyed, games_played) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"
                    connection.prepareStatement(sql).use { statement ->
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
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    override fun fetchStats(uuid: UUID): PlayerStats {
        val stats = PlayerStats(uuid)
        val sql = "SELECT first_play, last_play, wins, kills, final_kills, looses, deaths, final_deaths," + "beds_destroyed, games_played FROM global_stats WHERE uuid = ?;"
        try {
            dataSource.getConnection().use { connection ->
                connection.prepareStatement(sql).use { statement ->
                    statement.setString(1, uuid.toString())
                    statement.executeQuery().use { result ->
                        if (result.next()) {
                            val firstPlay: Timestamp? = result.getTimestamp(1)
                            val lastPlay: Timestamp? = result.getTimestamp(2)
                            stats.setFirstPlay((if (firstPlay != null) firstPlay.toInstant() else null).unchecked())
                            stats.setLastPlay((if (lastPlay != null) lastPlay.toInstant() else null).unchecked())
                            stats.setWins(result.getInt(3))
                            stats.setKills(result.getInt(4))
                            stats.setFinalKills(result.getInt(5))
                            stats.setLosses(result.getInt(6))
                            stats.setDeaths(result.getInt(7))
                            stats.setFinalDeaths(result.getInt(8))
                            stats.setBedsDestroyed(result.getInt(9))
                            stats.setGamesPlayed(result.getInt(10))
                        }
                    }
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return stats
    }

    override fun setQuickBuySlot(uuid: UUID, shopPath: String, slot: Int) {
        var sql = "SELECT uuid FROM quick_buy_2 WHERE uuid = ?;"
        try {
            dataSource.getConnection().use { connection ->
                connection.prepareStatement(sql).use { statement ->
                    statement.setString(1, uuid.toString())
                    statement.executeQuery().use { result ->
                        if (!result.next()) {
                            sql = "INSERT INTO quick_buy_2 VALUES(0, ?, ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ');"
                            connection.prepareStatement(sql).use { statement2 ->
                                statement2.setString(1, uuid.toString())
                                statement2.executeUpdate()
                            }
                        }
                        BedWars.debug("UPDATE SET SLOT " + slot + " identifier " + shopPath)
                        sql = "UPDATE quick_buy_2 SET slot_" + slot + " = ? WHERE uuid = ?;"
                        connection.prepareStatement(sql).use { statement2 ->
                            statement2.setString(1, shopPath)
                            statement2.setString(2, uuid.toString())
                            statement2.executeUpdate()
                        }
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    override fun getQuickBuySlots(uuid: UUID, slot: Int): String {
        val sql = "SELECT slot_" + slot + " FROM quick_buy_2 WHERE uuid = ?;"
        try {
            dataSource.getConnection().use { connection ->
                connection.prepareStatement(sql).use { statement ->
                    statement.setString(1, uuid.toString())
                    statement.executeQuery().use { result ->
                        if (result.next()) {
                            return result.getString(1)
                        }
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return ""
    }

    override fun getQuickBuySlots(uuid: UUID, slot: IntArray): HashMap<Int, String> {
        val results: HashMap<Int, String> = HashMap()
        if (slot.size == 0) {
            return results
        }
        try {
            dataSource.getConnection().use { connection ->
                connection.prepareStatement("SELECT * FROM quick_buy_2 WHERE uuid = ?;").use { ps ->
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
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return results
    }

    override fun hasQuickBuy(uuid: UUID): Boolean {
        val sql = "SELECT uuid FROM quick_buy_2 WHERE uuid = ?;"
        try {
            dataSource.getConnection().use { connection ->
                connection.prepareStatement(sql).use { statement ->
                    statement.setString(1, uuid.toString())
                    statement.executeQuery().use { result ->
                        return result.next()
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
            dataSource.getConnection().use { connection ->
                connection.prepareStatement(sql).use { statement ->
                    statement.setString(1, column)
                    statement.setString(2, player.toString())
                    statement.executeQuery().use { result ->
                        if (result.next()) {
                            return result.getInt(column)
                        }
                    }
                }
            }
        } catch (ex: SQLException) {
            ex.printStackTrace()
            return 0
        }
        return 0
    }

    override fun getLevelData(uuid: UUID): Array<Any> {
        val sql = "SELECT level, xp, name, next_cost FROM player_levels WHERE uuid = ?;"
        try {
            dataSource.getConnection().use { connection ->
                connection.prepareStatement(sql).use { statement ->
                    statement.setString(1, uuid.toString())
                    statement.executeQuery().use { result ->
                        if (result.next()) {
                            return arrayOf<Any>(result.getInt(1), result.getInt(2), result.getString(3), result.getInt(4))
                        }
                    }
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return arrayOf<Any>(1, 0, "", 0)
    }

    override fun setLevelData(uuid: UUID, level: Int, xp: Int, displayName: String?, nextCost: Int) {
        var sql = "SELECT uuid from player_levels WHERE uuid = ?;"
        try {
            dataSource.getConnection().use { connection ->
                connection.prepareStatement(sql).use { statement ->
                    statement.setString(1, uuid.toString())
                    statement.executeQuery().use { result ->
                        if (!result.next()) {
                            sql = "INSERT INTO player_levels VALUES (?, ?, ?, ?, ?, ?);"
                            connection.prepareStatement(sql).use { statement2 ->
                                statement2.setInt(1, 0)
                                statement2.setString(2, uuid.toString())
                                statement2.setInt(3, level)
                                statement2.setInt(4, xp)
                                statement2.setString(5, displayName)
                                statement2.setInt(6, nextCost)
                                statement2.executeUpdate()
                            }
                        } else {
                            if (displayName == null) {
                                sql = "UPDATE player_levels SET level=?, xp=? WHERE uuid = ?;"
                            } else {
                                sql = "UPDATE player_levels SET level=?, xp=?, name=?, next_cost=? WHERE uuid = ?;"
                            }
                            connection.prepareStatement(sql).use { statement2 ->
                                statement2.setInt(1, level)
                                statement2.setInt(2, xp)
                                if (displayName != null) {
                                    statement2.setString(3, displayName)
                                    statement2.setInt(4, nextCost)
                                    statement2.setString(5, uuid.toString())
                                } else {
                                    statement2.setString(3, uuid.toString())
                                }
                                statement2.executeUpdate()
                            }
                        }
                    }
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    override fun setLanguage(uuid: UUID, iso: String) {
        var sql = "SELECT iso FROM player_language WHERE uuid = ?;"
        try {
            dataSource.getConnection().use { connection ->
                connection.prepareStatement(sql).use { statement ->
                    statement.setString(1, uuid.toString())
                    statement.executeQuery().use { result ->
                        if (result.next()) {
                            sql = "UPDATE player_language SET iso = ? WHERE uuid = ?;"
                            connection.prepareStatement(sql).use { statement2 ->
                                statement2.setString(1, iso)
                                statement2.setString(2, uuid.toString())
                                statement2.executeUpdate()
                            }
                        } else {
                            sql = "INSERT INTO player_language VALUES (0, ?, ?);"
                            connection.prepareStatement(sql).use { statement2 ->
                                statement2.setString(1, uuid.toString())
                                statement2.setString(2, iso)
                                statement2.executeUpdate()
                            }
                        }
                    }
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    override fun getLanguage(uuid: UUID): String {
        val sql = "SELECT iso FROM player_language WHERE uuid = ?;"
        try {
            dataSource.getConnection().use { connection ->
                connection.prepareStatement(sql).use { statement ->
                    statement.setString(1, uuid.toString())
                    statement.executeQuery().use { result ->
                        if (result.next()) {
                            return result.getString(1)
                        }
                    }
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return Language.getDefaultLanguage().getIso().unchecked()
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
            dataSource.getConnection().use { con ->
                con.prepareStatement(sql).use { ps ->
                    var index: Int = if (hasQuick) 0 else 1
                    for (key in updateSlots.keys) {
                        index++
                        val identifier = updateSlots.get(key)
                        ps.setString(index, if (identifier!!.trim().isEmpty()) null else identifier)
                    }
                    ps.setString(if (hasQuick) updateSlots.size + 1 else 1, uuid.toString())
                    ps.execute()
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    init {
        this.host = config.getYml().getString("database.host")
        this.database = config.getYml().getString("database.database")
        this.user = config.getYml().getString("database.user")
        this.pass = config.getYml().getString("database.pass")
        this.port = config.getYml().getInt("database.port")
        this.ssl = config.getYml().getBoolean("database.ssl")
        this.certificateVerification = config.getYml().getBoolean("database.verify-certificate", true)
        this.poolSize = config.getYml().getInt("database.pool-size", 10)
        this.maxLifetime = config.getYml().getInt("database.max-lifetime", 1800)
    }
}
