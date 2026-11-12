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

package com.andrei1058.bedwars.support.version.v1_8_R3

import com.andrei1058.bedwars.api.arena.team.ITeam
import com.andrei1058.bedwars.api.language.Language
import com.andrei1058.bedwars.api.language.Messages
import com.andrei1058.bedwars.support.version.common.VersionCommon
import java.lang.reflect.Field
import net.minecraft.server.v1_8_R3.*
import org.apache.commons.lang3.StringUtils
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity
import org.bukkit.craftbukkit.v1_8_R3.util.UnsafeList
import org.bukkit.entity.LivingEntity
import org.bukkit.event.entity.CreatureSpawnEvent

@Deprecated("")
open class IGolem private constructor(world: World, private var team: ITeam?) : EntityIronGolem(world) {
    open fun getTeam(): ITeam? {
        return team
    }

    protected override fun dropDeathLoot(flag: Boolean, i: Int) {
    }

    override fun die() {
        super.die()
        team = null
        VersionCommon.api.getVersionSupport().getDespawnablesList().remove(this.getUniqueID())
    }

    override fun die(damagesource: DamageSource) {
        super.die(damagesource)
        team = null
        VersionCommon.api.getVersionSupport().getDespawnablesList().remove(this.getUniqueID())
    }

    init {
        try {
            val bField = PathfinderGoalSelector::class.java.getDeclaredField("b")
            bField.setAccessible(true)
            val cField = PathfinderGoalSelector::class.java.getDeclaredField("c")
            cField.setAccessible(true)
            bField.set(this.goalSelector, UnsafeList<Any>())
            bField.set(this.targetSelector, UnsafeList<Any>())
            cField.set(this.goalSelector, UnsafeList<Any>())
            cField.set(this.targetSelector, UnsafeList<Any>())
        } catch (e1: IllegalAccessException) {
            e1.printStackTrace()
        } catch (e1: NoSuchFieldException) {
            e1.printStackTrace()
        }
        this.setSize(1.4f, 2.9f)
        (this.getNavigation() as Navigation).a(true)
        this.goalSelector.a(1, PathfinderGoalFloat(this))
        this.goalSelector.a(2, PathfinderGoalMeleeAttack(this, 1.5, false))
        this.targetSelector.a(1, PathfinderGoalHurtByTarget(this, true))
        this.goalSelector.a(3, PathfinderGoalRandomStroll(this, 1.0))
        this.goalSelector.a(4, PathfinderGoalRandomLookaround(this))
        this.targetSelector.a(2, PathfinderGoalNearestAttackableTarget(this, EntityHuman::class.java, 20, true, false, lambda1@{ player ->
            if (player == null) {
                return@lambda1 false
            }
            (player as EntityHuman).isAlive() && !team!!.wasMember((player as EntityHuman).getUniqueID()) && !team!!.getArena()!!.isReSpawning((player as EntityHuman).getUniqueID()) && !team!!.getArena()!!.isSpectator((player as EntityHuman).getUniqueID())
        }))
        this.targetSelector.a(3, PathfinderGoalNearestAttackableTarget(this, IGolem::class.java, 20, true, false, lambda2@{ golem ->
            if (golem == null) {
                return@lambda2 false
            }
            (golem as IGolem).getTeam() !== team
        }))
        this.targetSelector.a(4, PathfinderGoalNearestAttackableTarget(this, Silverfish::class.java, 20, true, false, lambda3@{ sf ->
            if (sf == null) {
                return@lambda3 false
            }
            (sf as Silverfish).getTeam() !== team
        }))
    }

    companion object {
        @JvmStatic
        fun spawn(loc: Location, bedWarsTeam: ITeam, speed: Double, health: Double, despawn: Int): LivingEntity {
            val mcWorld = (loc.getWorld() as CraftWorld).getHandle()
            val customEnt = IGolem(mcWorld, bedWarsTeam)
            customEnt.getAttributeInstance(GenericAttributes.maxHealth).setValue(health)
            customEnt.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(speed)
            customEnt.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch())
            (customEnt.getBukkitEntity() as CraftLivingEntity).setRemoveWhenFarAway(false)
            customEnt.setCustomNameVisible(true)
            customEnt.setCustomName(Language.getDefaultLanguage().m(Messages.SHOP_UTILITY_NPC_IRON_GOLEM_NAME).replace("{despawn}", despawn.toString()).replace("{health}", StringUtils.repeat(Language.getDefaultLanguage().m(Messages.FORMATTING_DESPAWNABLE_UTILITY_NPC_HEALTH) + " ", 10)).replace("{TeamColor}", bedWarsTeam.getColor().chat().toString()))
            mcWorld.addEntity(customEnt, CreatureSpawnEvent.SpawnReason.CUSTOM)
            return customEnt.getBukkitEntity() as LivingEntity
        }
    }
}
