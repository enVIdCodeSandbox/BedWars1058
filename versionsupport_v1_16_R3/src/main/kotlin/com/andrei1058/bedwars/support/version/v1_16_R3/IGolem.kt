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

package com.andrei1058.bedwars.support.version.v1_16_R3

import com.andrei1058.bedwars.api.arena.team.ITeam
import com.andrei1058.bedwars.api.language.Language
import com.andrei1058.bedwars.api.language.Messages
import com.andrei1058.bedwars.support.version.common.VersionCommon
import net.minecraft.server.v1_16_R3.*
import org.apache.commons.lang.StringUtils
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftLivingEntity
import org.bukkit.entity.LivingEntity
import org.bukkit.event.entity.CreatureSpawnEvent

open class IGolem : EntityIronGolem {
    private var team: ITeam? = null

    private constructor(entitytypes: EntityTypes<out EntityIronGolem>, world: World, bedWarsTeam: ITeam) : super(entitytypes, world) {
        this.team = bedWarsTeam
    }

    constructor(entityTypes: EntityTypes<out EntityIronGolem>, world: World) : super(entityTypes, world)

    protected override fun initPathfinder() {
        this.goalSelector.a(1, PathfinderGoalFloat(this))
        this.goalSelector.a(2, PathfinderGoalMeleeAttack(this, 1.5, false))
        this.targetSelector.a(1, PathfinderGoalHurtByTarget(this))
        this.goalSelector.a(3, PathfinderGoalRandomStroll(this, 1.0))
        this.goalSelector.a(4, PathfinderGoalRandomLookaround(this))
        this.targetSelector.a(2, PathfinderGoalNearestAttackableTarget(this, EntityHuman::class.java, 20, true, false, { player ->
            player.isAlive() && !team!!.wasMember(player.getUniqueID()) && !team!!.getArena()!!.isReSpawning(player.getUniqueID()) && !team!!.getArena()!!.isSpectator(player.getUniqueID())
        }))
        this.targetSelector.a(3, PathfinderGoalNearestAttackableTarget(this, IGolem::class.java, 20, true, false, { golem -> (golem as IGolem).getTeam() !== team }))
        this.targetSelector.a(4, PathfinderGoalNearestAttackableTarget(this, Silverfish::class.java, 20, true, false, { sf -> (sf as Silverfish).getTeam() !== team }))
    }

    open fun getTeam(): ITeam? {
        return team
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

    companion object {
        @JvmStatic
        fun spawn(loc: Location, bedWarsTeam: ITeam, speed: Double, health: Double, despawn: Int): LivingEntity {
            val mcWorld = (loc.getWorld() as CraftWorld).getHandle()
            val customEnt = IGolem(EntityTypes.IRON_GOLEM, mcWorld, bedWarsTeam)
            customEnt.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch())
            (customEnt.getBukkitEntity() as CraftLivingEntity).setRemoveWhenFarAway(false)
            customEnt.setCustomNameVisible(true)
            customEnt.setPersistent()
            customEnt.getAttributeInstance(GenericAttributes.MAX_HEALTH)!!.setValue(health)
            customEnt.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED)!!.setValue(speed)
            mcWorld.addEntity(customEnt, CreatureSpawnEvent.SpawnReason.CUSTOM)
            customEnt.getBukkitEntity().setCustomName(Language.getDefaultLanguage().m(Messages.SHOP_UTILITY_NPC_IRON_GOLEM_NAME).replace("{despawn}", despawn.toString().replace("{health}", StringUtils.repeat(Language.getDefaultLanguage().m(Messages.FORMATTING_DESPAWNABLE_UTILITY_NPC_HEALTH) + " ", 10)).replace("{TeamColor}", bedWarsTeam.getColor().chat().toString())))
            return customEnt.getBukkitEntity() as LivingEntity
        }
    }
}
