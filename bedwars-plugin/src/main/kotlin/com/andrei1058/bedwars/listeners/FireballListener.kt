package com.andrei1058.bedwars.listeners

import com.andrei1058.bedwars.BedWars.Companion.config
import com.andrei1058.bedwars.BedWars.Companion.getAPI
import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.api.configuration.ConfigPath
import com.andrei1058.bedwars.arena.Arena
import com.andrei1058.bedwars.arena.LastHit
import java.util.*
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Entity
import org.bukkit.entity.Fireball
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.ExplosionPrimeEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.projectiles.ProjectileSource
import org.bukkit.util.Vector

open class FireballListener : Listener {
    private val fireballExplosionSize: Double
    private val fireballMakeFire: Boolean
    private val fireballHorizontal: Double
    private val fireballVertical: Double
    private val damageSelf: Double
    private val damageEnemy: Double
    private val damageTeammates: Double

    @EventHandler
    open fun fireballHit(e: ProjectileHitEvent) {
        if (e.getEntity() !is Fireball) {
            return
        }
        val location = e.getEntity().getLocation()
        val projectileSource = e.getEntity().getShooter()
        if (projectileSource !is Player) {
            return
        }
        val source = projectileSource as Player
        val arena = Arena.getArenaByPlayer(source)
        val vector = location.toVector()
        val world: World? = location.getWorld()
        assert(world != null)
        val nearbyEntities = world!!.getNearbyEntities(location, fireballExplosionSize, fireballExplosionSize, fireballExplosionSize)
        for (entity in nearbyEntities) {
            if (entity !is Player) {
                continue
            }
            val player = entity as Player
            if (!getAPI().getArenaUtil().isPlaying(player)) {
                continue
            }
            val playerVector = player.getLocation().toVector()
            val normalizedVector = vector.subtract(playerVector).normalize()
            val horizontalVector = normalizedVector.multiply(fireballHorizontal)
            var y: Double = normalizedVector.getY()
            if (y < 0) {
                y += 1.5
            }
            if (y <= 0.5) {
                // kb for not jumping
                y = fireballVertical * 1.5
            } else {
                // kb for jumping
                y = y * fireballVertical * 1.5
            }
            player.setVelocity(horizontalVector.setY(y))
            val lh: LastHit? = LastHit.getLastHit(player)
            if (lh != null) {
                lh.setDamager(source)
                lh.setTime(System.currentTimeMillis())
            } else {
                LastHit(player, source, System.currentTimeMillis())
            }
            if (player == source) {
                if (damageSelf > 0) {
                    // damage shooter
                    player.damage(damageSelf)
                }
            } else if (arena.getTeam(player) == arena.getTeam(source)) {
                if (damageTeammates > 0) {
                    // damage teammates
                    player.damage(damageTeammates)
                }
            } else {
                if (damageEnemy > 0) {
                    // damage enemies
                    player.damage(damageEnemy)
                }
            }
        }
    }

    @EventHandler
    open fun fireballDirectHit(e: EntityDamageByEntityEvent) {
        if (e.getDamager() !is Fireball) {
            return
        }
        if (e.getEntity() !is Player) {
            return
        }
        if (Arena.getArenaByPlayer(e.getEntity() as Player) == null) {
            return
        }
        e.setCancelled(true)
    }

    @EventHandler
    open fun fireballPrime(e: ExplosionPrimeEvent) {
        if (e.getEntity() !is Fireball) {
            return
        }
        val shooter = (e.getEntity() as Fireball).getShooter()
        if (shooter !is Player) {
            return
        }
        val player = shooter as Player
        if (Arena.getArenaByPlayer(player) == null) {
            return
        }
        e.setFire(fireballMakeFire)
    }

    init {
        this.fireballExplosionSize = config.getYml().getDouble(ConfigPath.GENERAL_FIREBALL_EXPLOSION_SIZE)
        this.fireballMakeFire = config.getYml().getBoolean(ConfigPath.GENERAL_FIREBALL_MAKE_FIRE)
        this.fireballHorizontal = config.getYml().getDouble(ConfigPath.GENERAL_FIREBALL_KNOCKBACK_HORIZONTAL) * -1
        this.fireballVertical = config.getYml().getDouble(ConfigPath.GENERAL_FIREBALL_KNOCKBACK_VERTICAL)
        this.damageSelf = config.getYml().getDouble(ConfigPath.GENERAL_FIREBALL_DAMAGE_SELF)
        this.damageEnemy = config.getYml().getDouble(ConfigPath.GENERAL_FIREBALL_DAMAGE_ENEMY)
        this.damageTeammates = config.getYml().getDouble(ConfigPath.GENERAL_FIREBALL_DAMAGE_TEAMMATES)
    }
}
