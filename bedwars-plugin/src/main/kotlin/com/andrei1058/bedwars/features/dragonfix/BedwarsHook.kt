package com.andrei1058.bedwars.features.dragonfix

import com.andrei1058.bedwars.BedWars
import com.andrei1058.bedwars.api.arena.GameState
import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.api.arena.generator.IGenerator
import com.andrei1058.bedwars.api.arena.team.ITeam
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player

/**
 * Everything DragonFix asks of the game goes through this file (BedWars1058 API: IArena, ITeam, IGenerator).
 */
object Reflect {
    /** Calls the public no-argument method [method] of a Bukkit object that only exists on newer servers; null on any failure. */
    fun call(target: Any, method: String): Any? {
        return try {
            val m = target.javaClass.methods.firstOrNull { it.name == method && it.parameterTypes.isEmpty() } ?: return null
            m.isAccessible = true
            m.invoke(target)
        } catch (_: Throwable) {
            null
        }
    }
}

class TeamRef(val raw: ITeam) {
    val name: String get() = raw.getName() ?: "team"
    val spawn: Location? get() = raw.getSpawn()
    val bed: Location? get() = raw.getBed()
    val shop: Location? get() = raw.getShop()
    val upgrades: Location? get() = raw.getTeamUpgrades()
    val members: List<Player> get() = raw.getMembers()?.toList() ?: emptyList()
    val generators: List<IGenerator> get() = raw.getGenerators()?.toList() ?: emptyList()

    /** Dragons this team gets at sudden death (1 by default, 2 with the Dragon Buff upgrade). */
    val dragons: Int get() = raw.getDragons()

    fun isMember(p: Player): Boolean = raw.isMember(p)
}

class ArenaRef(val raw: IArena) {
    val worldName: String get() = raw.getWorldName() ?: ""
    val arenaName: String get() = raw.getArenaName() ?: worldName
    val statusName: String get() = raw.getStatus()?.name ?: ""
    val world: World? get() = raw.getWorld()
    val teams: List<TeamRef> get() = raw.getTeams()?.map { TeamRef(it) } ?: emptyList()
    val players: List<Player> get() = raw.getPlayers()?.toList() ?: emptyList()
    val oreGenerators: List<IGenerator> get() = raw.getOreGenerators()?.toList() ?: emptyList()
    val isPlaying: Boolean get() = raw.getStatus() == GameState.playing

    fun isPlayer(p: Player): Boolean = raw.isPlayer(p)
    fun isSpectator(p: Player): Boolean = raw.isSpectator(p)

    /** The core's own build protection (protected regions, generator radius, outside the border). */
    fun isProtected(l: Location): Boolean = try { raw.isProtected(l) } catch (_: Throwable) { false }

    override fun equals(other: Any?) = other is ArenaRef && other.raw === raw
    override fun hashCode() = System.identityHashCode(raw)
}

/** One base (= one configured team island) of an arena. */
class BaseInfo(
    val team: TeamRef,
    val name: String,
    val spawn: Location,
    val bed: Location?,
    /** Extra dragons this base's team earned (Dragon Buff upgrade). */
    val buffExtra: Int,
) {
    fun alivePlayers(): List<Player> = team.members.filter { it.isOnline && !it.isDead }
}

/** Snapshot of everything the dragon AI needs to know about an arena at sudden death. */
class ArenaInfo(
    val arena: ArenaRef,
    val world: World,
    val bases: List<BaseInfo>,
    /** Average Y of all bases: the level dragons descend to. */
    val avgBaseY: Double,
    val centerX: Double,
    val centerZ: Double,
    /** Spheres (x, y, z, radius) that dragons may never break blocks in. */
    val protectedSpheres: List<DoubleArray>,
    /** Arena-wide (diamond / emerald) generator positions: the middle islands. */
    val middlePoints: List<org.bukkit.util.Vector>,
)

object BedwarsHook {

    /** The core uses the (copied) arena world's name as the arena identifier. */
    fun arenaOfWorld(world: World): ArenaRef? {
        val arena: IArena? = BedWars.getAPI().getArenaUtil().getArenaByIdentifier(world.name)
        return if (arena == null) null else ArenaRef(arena)
    }

    fun isParticipant(arena: ArenaRef, p: Player): Boolean = arena.isPlayer(p) && !arena.isSpectator(p)

    fun participants(arena: ArenaRef): List<Player> =
        arena.players.filter { it.isOnline && !it.isDead && isParticipant(arena, it) }

    fun buildInfo(arena: ArenaRef, s: Settings): ArenaInfo? {
        val world = arena.world ?: return null
        val bases = ArrayList<BaseInfo>()
        for (team in arena.teams) {
            val spawn = team.spawn ?: continue
            val buff = (team.dragons - 1).coerceAtLeast(0)
            bases += BaseInfo(team, team.name, spawn.clone(), team.bed?.clone(), buff)
        }
        if (bases.isEmpty()) return null

        // Average Y of the map, derived from where the bases really are (spawns + beds).
        val ys = ArrayList<Double>()
        for (b in bases) {
            ys += b.spawn.y
            b.bed?.let { ys += it.y }
        }
        val avgY = ys.average()
        val cx = bases.map { it.spawn.x }.average()
        val cz = bases.map { it.spawn.z }.average()

        val spheres = ArrayList<DoubleArray>()
        fun addSphere(l: Location?, r: Double) {
            if (l != null && r > 0.0) spheres += doubleArrayOf(l.x, l.y, l.z, r)
        }

        // Generators: arena-wide (diamond / emerald) and per team (iron / gold).
        // By default only a small area around each generator / shop is kept (so they keep working);
        // terrain around bases and generators is protected only with map-damage.protect-bases-and-generators.
        val full = s.protectBasesAndGenerators
        val genR = if (full) s.protectGeneratorRadius else s.functionalRadius
        val shopR = if (full) s.protectShopRadius else s.functionalRadius
        val gens = ArrayList<IGenerator>(arena.oreGenerators)
        for (b in bases) gens.addAll(b.team.generators)
        for (g in gens) addSphere(g.getLocation(), genR)

        // Shops and team upgrade NPCs, plus optional spawn / base protection.
        for (b in bases) {
            addSphere(b.team.shop, shopR)
            addSphere(b.team.upgrades, shopR)
            if (full) addSphere(b.spawn, maxOf(s.protectSpawnRadius, s.protectBaseRadius))
        }
        val middle = ArrayList<org.bukkit.util.Vector>()
        for (g in arena.oreGenerators) {
            val l = g.getLocation() ?: continue
            middle += org.bukkit.util.Vector(l.x, l.y, l.z)
        }
        return ArenaInfo(arena, world, bases, avgY, cx, cz, spheres, middle)
    }
}
