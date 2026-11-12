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

package com.andrei1058.bedwars.support.version.v1_12_R1

import com.andrei1058.bedwars.api.arena.team.ITeam
import com.andrei1058.bedwars.api.language.Language
import com.andrei1058.bedwars.api.language.Messages
import com.andrei1058.bedwars.support.version.common.VersionCommon
import com.google.common.collect.Sets
import java.lang.reflect.Field
import java.util.Random
import net.minecraft.server.v1_12_R1.*
import org.apache.commons.lang.StringUtils
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftLivingEntity
import org.bukkit.craftbukkit.v1_12_R1.event.CraftEventFactory
import org.bukkit.entity.LivingEntity
import org.bukkit.event.entity.CreatureSpawnEvent
import com.andrei1058.bedwars.api.util.uninitialized

open class Silverfish(world: World, team: ITeam) : EntitySilverfish(world) {
    private var team: ITeam?

    open fun getTeam(): ITeam? {
        return team
    }

    open class PathfinderGoalSilverfishHideInBlock(entitysilverfish: EntitySilverfish) : PathfinderGoalRandomStroll(entitysilverfish, 1.0, 10) {
        @JvmField var h: EnumDirection = uninitialized()
        @JvmField var i: Boolean = false

        override fun a(): Boolean {
            if (this.a.getGoalTarget() != null) {
                return false
            } else if (!this.a.getNavigation().o()) {
                return false
            } else {
                val random = this.a.getRandom()
                if (this.a.world.getGameRules().getBoolean("mobGriefing") && random.nextInt(10) == 0) {
                    this.h = EnumDirection.a(random)
                    val blockposition = (BlockPosition(this.a.locX, this.a.locY + 0.5, this.a.locZ)).shift(this.h)
                    val iblockdata = this.a.world.getType(blockposition)
                    if (BlockMonsterEggs.x(iblockdata)) {
                        this.i = true
                        return true
                    }
                }
                this.i = false
                return super.a()
            }
        }

        override fun b(): Boolean {
            return !this.i && super.b()
        }

        override fun c() {
            if (!this.i) {
                super.c()
            } else {
                val world = this.a.world
                val blockposition = (BlockPosition(this.a.locX, this.a.locY + 0.5, this.a.locZ)).shift(this.h)
                val iblockdata = world.getType(blockposition)
                if (BlockMonsterEggs.x(iblockdata)) {
                    if (CraftEventFactory.callEntityChangeBlockEvent(this.a, blockposition, Blocks.MONSTER_EGG, Block.getId(BlockMonsterEggs.getById(iblockdata.getBlock().toLegacyData(iblockdata)))).isCancelled()) {
                        return
                    }
                    world.setTypeAndData(blockposition, Blocks.MONSTER_EGG.getBlockData().set(BlockMonsterEggs.VARIANT, BlockMonsterEggs.EnumMonsterEggVarient.a(iblockdata)), 3)
                    this.a.doSpawnEffect()
                    this.a.die()
                }
            }
        }

        init {
            this.a(1)
        }
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
            bField.set(this.goalSelector, Sets.newLinkedHashSet<Any>())
            bField.set(this.targetSelector, Sets.newLinkedHashSet<Any>())
            cField.set(this.goalSelector, Sets.newLinkedHashSet<Any>())
            cField.set(this.targetSelector, Sets.newLinkedHashSet<Any>())
        } catch (e1: IllegalAccessException) {
            e1.printStackTrace()
        } catch (e1: NoSuchFieldException) {
            e1.printStackTrace()
        }
        this.team = team
        this.goalSelector.a(1, PathfinderGoalFloat(this))
        this.goalSelector.a(2, PathfinderGoalMeleeAttack(this, 1.9, false))
        this.targetSelector.a(1, PathfinderGoalHurtByTarget(this, true))
        this.goalSelector.a(3, PathfinderGoalRandomStroll(this, 2.0))
        this.targetSelector.a(2, PathfinderGoalNearestAttackableTarget(this, EntityHuman::class.java, 20, true, false, { player ->
            (player as EntityHuman).isAlive() && !team.wasMember((player as EntityHuman).getUniqueID()) && !team.getArena()!!.isReSpawning((player as EntityHuman).getUniqueID()) && !team.getArena()!!.isSpectator((player as EntityHuman).getUniqueID())
        }))
        this.targetSelector.a(3, PathfinderGoalNearestAttackableTarget(this, IGolem::class.java, 20, true, false, { golem ->
            (golem as IGolem).getTeam() !== team
        }))
        this.targetSelector.a(4, PathfinderGoalNearestAttackableTarget(this, Silverfish::class.java, 20, true, false, { sf ->
            (sf as Silverfish).getTeam() !== team
        }))
    }

    companion object {
        @JvmStatic
        fun spawn(loc: Location, team: ITeam, speed: Double, health: Double, despawn: Int, damage: Double): LivingEntity {
            val mcWorld = (loc.getWorld() as CraftWorld).getHandle()
            val customEnt = Silverfish(mcWorld, team)
            customEnt.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch())
            customEnt.getAttributeInstance(GenericAttributes.maxHealth).setValue(health)
            customEnt.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(speed)
            customEnt.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(damage)
            (customEnt.getBukkitEntity() as CraftLivingEntity).setRemoveWhenFarAway(false)
            customEnt.setCustomName(Language.getDefaultLanguage().m(Messages.SHOP_UTILITY_NPC_SILVERFISH_NAME).replace("{despawn}", despawn.toString().replace("{health}", StringUtils.repeat(Language.getDefaultLanguage().m(Messages.FORMATTING_DESPAWNABLE_UTILITY_NPC_HEALTH) + " ", 10)).replace("{TeamColor}", team.getColor().chat().toString())))
            customEnt.setCustomNameVisible(true)
            mcWorld.addEntity(customEnt, CreatureSpawnEvent.SpawnReason.CUSTOM)
            return customEnt.getBukkitEntity() as LivingEntity
        }
    }
}
