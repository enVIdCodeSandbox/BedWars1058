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

package com.andrei1058.bedwars.api.arena

import com.andrei1058.bedwars.api.arena.generator.IGenerator
import com.andrei1058.bedwars.api.arena.stats.GameStatsHolder
import com.andrei1058.bedwars.api.arena.team.ITeam
import com.andrei1058.bedwars.api.arena.team.ITeamAssigner
import com.andrei1058.bedwars.api.configuration.ConfigManager
import com.andrei1058.bedwars.api.language.Language
import com.andrei1058.bedwars.api.region.Region
import com.andrei1058.bedwars.api.tasks.PlayingTask
import com.andrei1058.bedwars.api.tasks.RestartingTask
import com.andrei1058.bedwars.api.tasks.StartingTask
import java.time.Instant
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.util.Vector

interface IArena {
    /**
     * Check if a player is spectating on this arena.
     */
    fun isSpectator(player: Player): Boolean

    /**
     * Check if a player is spectating on this arena.
     */
    fun isSpectator(player: UUID): Boolean

    /**
     * Check if a player is spectating on this arena.
     */
    fun isReSpawning(player: UUID?): Boolean

    /**
     * Get used world name.
     */
    fun getArenaName(): String

    /**
     * Initialize the arena after loading the world.
     * This needs to be called in order to allow players to join.
     */
    fun init(world: World)

    /**
     * Get arena config.
     */
    fun getConfig(): ConfigManager?

    /**
     * Check if user is playing.
     */
    fun isPlayer(player: Player): Boolean

    /**
     * Get a list of spectators.
     */
    fun getSpectators(): MutableList<Player>?

    /**
     * Get the player's team.
     * This will work if the player is alive only.
     * <p>
     * Use {@link #getExTeam(UUID)} to get the team where the player has played in current match.
     */
    fun getTeam(player: Player): ITeam?

    /**
     * Get the team where the player has played in current match.
     * To be used if the player was eliminated.
     */
    fun getExTeam(player: UUID): ITeam?

    /**
     * Get the arena name as a message that can be used on signs etc.
     *
     * @return A string with - and _ replaced by a space.
     */
    fun getDisplayName(): String

    /**
     * Change world name for auto-scaling.
     *
     * @param name new name.
     */
    fun setWorldName(name: String)

    /**
     * Get arena status.
     */
    fun getStatus(): GameState

    /**
     * Get players in arena.
     */
    fun getPlayers(): MutableList<Player>?

    /**
     * Get maximum allowed players amount.
     */
    fun getMaxPlayers(): Int

    /**
     * Get arena group.
     */
    fun getGroup(): String

    /**
     * Get maximum players allowed in a team.
     */
    fun getMaxInTeam(): Int

    /**
     * Get list of players in respawn screen.
     * Player is the actual player in re-spawn screen.
     * Integer is the remaining time.
     */
    fun getRespawnSessions(): ConcurrentHashMap<Player, Int>?

    /**
     * Disable spectator collisions.
     *
     * @param p - spectator.
     *          Use false when the spectator got removed from the arena.
     */
    fun updateSpectatorCollideRule(p: Player, collide: Boolean)

    /**
     * This will attempt to upgrade the next event if it is the case.
     */
    fun updateNextEvent()

    /**
     * Add a player to the arena
     *
     * @param p              - Player to add.
     * @param skipOwnerCheck - True if you want to skip the party checking for this player. This
     * @return true if was added.
     */
    fun addPlayer(p: Player?, skipOwnerCheck: Boolean): Boolean

    /**
     * Add a player as Spectator
     *
     * @param p            Player to be added
     * @param playerBefore True if the player has played in this arena before and he died so now should be a spectator.
     */
    fun addSpectator(p: Player, playerBefore: Boolean, staffTeleport: Location?): Boolean

    /**
     * Remove a player from the arena
     *
     * @param p          Player to be removed
     * @param disconnect True if the player was disconnected
     */
    fun removePlayer(p: Player, disconnect: Boolean)

    /**
     * Remove a spectator from the arena
     *
     * @param p          Player to be removed
     * @param disconnect True if the player was disconnected
     */
    fun removeSpectator(p: Player, disconnect: Boolean)

    /**
     * Rejoin an arena
     *
     * @return true if can rejoin
     */
    fun reJoin(p: Player): Boolean

    /**
     * Disable the arena.
     * This will automatically kick/ remove the people from the arena.
     */
    fun disable()

    /**
     * Restart the arena.
     */
    fun restart()

    /**
     * Get the arena world
     */
    fun getWorld(): World?

    /**
     * Get the display status for an arena.
     * A message that can be used on signs etc.
     */
    fun getDisplayStatus(lang: Language): String

    /**
     * Get arena display group for given player.
     *
     * @return translated group.
     */
    fun getDisplayGroup(player: Player): String

    /**
     * Get arena display group for given language.
     *
     * @return translated group.
     */
    fun getDisplayGroup(language: Language): String

    fun getTeams(): MutableList<ITeam>?

    /**
     * Add placed block to cache.
     * So players will be able to remove blocks placed by players only.
     */
    fun addPlacedBlock(block: Block?)

    /**
     * Gets the cooldowns for fireballs
     * @return The cooldowns for fireballs
     */
    fun getFireballCooldowns(): MutableMap<UUID, Long>

    /**
     * Remove placed block.
     */
    fun removePlacedBlock(block: Block?)

    fun isBlockPlaced(block: Block): Boolean

    /**
     * Get a player kills count.
     *
     * @param p          Target player
     * @param finalKills True if you want to get the Final Kills. False for regular kills.
     */
    @Deprecated("")
    fun getPlayerKills(p: Player?, finalKills: Boolean): Int

    /**
     * Session stats.
     * @return stats container for this game.
     */
    fun getStatsHolder(): GameStatsHolder?

    /**
     * Get the player beds destroyed count
     *
     * @param p Target player
     */
    @Deprecated("")
    fun getPlayerBedsDestroyed(p: Player?): Int

    /**
     * Get the join signs for this arena
     *
     * @return signs.
     */
    fun getSigns(): MutableList<Block>?

    /**
     * Get the island radius
     */
    fun getIslandRadius(): Int

    fun setGroup(group: String)

    /**
     * Set game status without starting stats.
     */
    fun setStatus(status: GameState)

    /**
     * Change game status starting tasks.
     */
    fun changeStatus(status: GameState)

    /**
     * Check if target player is in re-spawning screen.
     */
    @Deprecated("")
    fun isRespawning(p: Player): Boolean {
        return isReSpawning(p)
    }

    /**
     * Add a join sign for the arena.
     */
    fun addSign(loc: Location?)

    /**
     * Refresh signs.
     */
    fun refreshSigns()

    /**
     * Add a kill point to the game stats.
     */
    @Deprecated("")
    fun addPlayerKill(p: Player, finalKill: Boolean, victim: Player)

    /**
     * Add a destroyed bed point to the player temp stats.
     */
    @Deprecated("")
    fun addPlayerBedDestroyed(p: Player)

    /**
     * Get arena by player name.
     * Used to get the team for a player that has left the arena.
     * Make sure the player is in this arena first.
     */
    @Deprecated("")
    fun getPlayerTeam(playerName: String): ITeam?

    /**
     * Check winner. Will check if the game has a winner in certain conditions. Manage your win conditions.
     * Call the arena restart and the needed stuff.
     */
    fun checkWinner()

    /**
     * Add a kill to the player temp stats.
     */
    @Deprecated("")
    fun addPlayerDeath(p: Player)

    /**
     * Set next event for the arena.
     */
    fun setNextEvent(nextEvent: NextEvent?)

    /**
     * Get next event.
     */
    fun getNextEvent(): NextEvent?

    /**
     * This will give the pre-game command Items.
     * This will clear the inventory first.
     */
    fun sendPreGameCommandItems(p: Player)

    /**
     * This will give the spectator command Items.
     * This will clear the inventory first.
     */
    fun sendSpectatorCommandItems(p: Player)

    /**
     * Get a team by name
     */
    fun getTeam(name: String): ITeam?

    fun getStartingTask(): StartingTask?

    fun getPlayingTask(): PlayingTask?

    fun getRestartingTask(): RestartingTask?

    /**
     * Get Ore Generators.
     */
    fun getOreGenerators(): MutableList<IGenerator>?

    /**
     * Get the list of next events to come.
     * Not ordered.
     */
    fun getNextEvents(): MutableList<String>

    /**
     * Get player deaths.
     */
    @Deprecated("")
    fun getPlayerDeaths(p: Player?, finalDeaths: Boolean): Int

    /**
     * Show upgrade announcement to players.
     * Change diamondTier value first.
     */
    fun sendDiamondsUpgradeMessages()

    /**
     * Show upgrade announcement to players.
     * Change emeraldTier value first.
     */
    fun sendEmeraldsUpgradeMessages()

    /**
     * List of placed blocks.
     */
    fun getPlaced(): LinkedList<Vector>?

    /**
     * This is used to destroy arena data when it restarts.
     */
    fun destroyData()

    fun getUpgradeDiamondsCount(): Int

    fun getUpgradeEmeraldsCount(): Int

    fun getRegionsList(): MutableList<Region>?

    /**
     * Get invisibility for armor
     */
    fun getShowTime(): ConcurrentHashMap<Player, Int>?

    fun setAllowSpectate(allowSpectate: Boolean)

    fun isAllowSpectate(): Boolean

    fun getWorldName(): String?

    /**
     * Get player render distance in blocks.
     */
    fun getRenderDistance(): Int

    /**
     * Put a player in re-spawning countdown.
     *
     * @param player  target player.
     * @param seconds countdown in seconds. 0 for instant re-spawn.
     * @return false if the player is not actually in game or if is in another re-spawn session.
     */
    fun startReSpawnSession(player: Player, seconds: Int): Boolean

    /**
     * Check if a player is in re-spawning screen/ countdown.
     */
    fun isReSpawning(player: Player?): Boolean

    /**
     * Get re-spawning screen location.
     */
    fun getReSpawnLocation(): Location?

    /**
     * Where spectators will spawn.
     */
    fun getSpectatorLocation(): Location?

    /**
     * Location where to spawn at join (waiting/ starting).
     */
    fun getWaitingLocation(): Location?

    /**
     * Check if the given location is protected.
     * Border checks, regions, island spawn protection, npc protections, generator protections.
     */
    fun isProtected(location: Location): Boolean

    /**
     * This is triggered when a player has abandoned a game.
     * This should remove its assist from existing team and re-join session.
     * This does not replace {@link #removePlayer(Player, boolean)}.
     */
    fun abandonGame(player: Player?)

    /**
     * -1 won't handle void kill.
     * Instant kill when player y is under this number.
     */
    fun getYKillHeight(): Int

    /**
     * y limit for players to be able to build.
     */
    fun getYHeightLimit(): Int

    fun getStartTime(): Instant

    fun getTeamAssigner(): ITeamAssigner?

    fun setTeamAssigner(teamAssigner: ITeamAssigner?)

    fun getLeavingPlayers(): MutableList<Player>

    /**
     * Check if breaking map is allowed, otherwise only placed blocks are allowed.
     * Some blocks like have a special protections, like blocks under shopkeepers, bed, ecc.
     */
    fun isAllowMapBreak(): Boolean

    /**
     * Toggle map block break rule.
     */
    fun setAllowMapBreak(allowMapBreak: Boolean)

    /**
     * Check if there is a player bed at given location.
     */
    fun isTeamBed(location: Location): Boolean

    /**
     * Get owner team of a bed based on location.
     */
    fun getBedsTeam(location: Location): ITeam?

    /**
     * Provides the winner team.
     * This is populated on restarting phase.
     */
    fun getWinner(): ITeam?

    /**
     * Get the current diamond generator tier for this arena.
     * @return the diamond generator tier (e.g., 1, 2, 3, 4, ...)
     */
    fun getDiamondTier(): Int

    /**
     * Get the current emerald generator tier for this arena.
     * @return the emerald generator tier (e.g., 1, 2, 3, 4, ...)
     */
    fun getEmeraldTier(): Int
}
