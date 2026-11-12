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

import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.api.arena.shop.ShopHolo
import com.andrei1058.bedwars.api.arena.team.ITeam
import com.andrei1058.bedwars.api.arena.team.TeamColor
import com.andrei1058.bedwars.api.entity.Despawnable
import com.andrei1058.bedwars.api.events.player.PlayerKillEvent
import com.andrei1058.bedwars.api.exceptions.InvalidEffectException
import com.andrei1058.bedwars.api.language.Language
import com.andrei1058.bedwars.api.language.Language.Companion.getMsg
import com.andrei1058.bedwars.api.language.Messages
import com.andrei1058.bedwars.api.server.VersionSupport
import com.andrei1058.bedwars.support.version.common.VersionCommon
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.util.ArrayList
import java.util.logging.Level
import net.minecraft.server.v1_8_R3.*
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.craftbukkit.v1_8_R3.CraftServer
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld
import org.bukkit.craftbukkit.v1_8_R3.entity.*
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack
import org.bukkit.craftbukkit.v1_8_R3.util.UnsafeList
import org.bukkit.entity.*
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.inventory.InventoryEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.plugin.Plugin
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector
import com.andrei1058.bedwars.api.util.unchecked

@Deprecated("")
open class v1_8_R3(pl: Plugin, name: String) : VersionSupport(pl, name) {
    override fun spawnSilverfish(loc: Location, bedWarsTeam: ITeam, speed: Double, health: Double, despawn: Int, damage: Double) {
        Despawnable(Silverfish.spawn(loc, bedWarsTeam, speed, health, despawn, damage), bedWarsTeam, despawn, Messages.SHOP_UTILITY_NPC_SILVERFISH_NAME, PlayerKillEvent.PlayerKillCause.SILVERFISH_FINAL_KILL, PlayerKillEvent.PlayerKillCause.SILVERFISH)
    }

    override fun spawnIronGolem(loc: Location, bedWarsTeam: ITeam, speed: Double, health: Double, despawn: Int) {
        Despawnable(IGolem.spawn(loc, bedWarsTeam, speed, health, despawn), bedWarsTeam, despawn, Messages.SHOP_UTILITY_NPC_IRON_GOLEM_NAME, PlayerKillEvent.PlayerKillCause.IRON_GOLEM_FINAL_KILL, PlayerKillEvent.PlayerKillCause.IRON_GOLEM)
    }

    override fun registerCommand(name: String, clasa: Command) {
        (getPlugin().getServer() as CraftServer).getCommandMap().register(name, clasa)
    }

    override fun sendTitle(p: Player, title: String?, subtitle: String?, fadeIn: Int, stay: Int, fadeOut: Int) {
        if (title != null) {
            if (!title.isEmpty()) {
                val bc = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + title + "\"}")
                val tit = PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, bc)
                val length = PacketPlayOutTitle(fadeIn, stay, fadeOut)
                (p as CraftPlayer).getHandle().playerConnection.sendPacket(tit)
                (p as CraftPlayer).getHandle().playerConnection.sendPacket(length)
            }
        }
        if (subtitle != null) {
            val bc = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + subtitle + "\"}")
            val tit = PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE, bc)
            val length = PacketPlayOutTitle(fadeIn, stay, fadeOut)
            (p as CraftPlayer).getHandle().playerConnection.sendPacket(tit)
            (p as CraftPlayer).getHandle().playerConnection.sendPacket(length)
        }
    }

    override fun playAction(p: Player, text: String) {
        val cPlayer = p as CraftPlayer
        val cbc = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + text + "\"}")
        val ppoc = PacketPlayOutChat(cbc, 2.toByte())
        cPlayer.getHandle().playerConnection.sendPacket(ppoc)
    }

    override fun isBukkitCommandRegistered(name: String): Boolean {
        return (getPlugin().getServer() as CraftServer).getCommandMap().getCommand(name) != null
    }

    override fun getItemInHand(p: Player): ItemStack {
        return p.getItemInHand()
    }

    override fun hideEntity(e: org.bukkit.entity.Entity, p: Player) {
        val packet = PacketPlayOutEntityDestroy(e.getEntityId())
        (p as CraftPlayer).getHandle().playerConnection.sendPacket(packet)
    }

    override fun isArmor(itemStack: ItemStack): Boolean {
        if (CraftItemStack.asNMSCopy(itemStack) == null) {
            return false
        }
        if (CraftItemStack.asNMSCopy(itemStack).getItem() == null) {
            return false
        }
        return CraftItemStack.asNMSCopy(itemStack).getItem() is ItemArmor
    }

    override fun isTool(itemStack: ItemStack): Boolean {
        if (CraftItemStack.asNMSCopy(itemStack) == null) {
            return false
        }
        if (CraftItemStack.asNMSCopy(itemStack).getItem() == null) {
            return false
        }
        return CraftItemStack.asNMSCopy(itemStack).getItem() is ItemTool
    }

    override fun isSword(itemStack: ItemStack): Boolean {
        if (CraftItemStack.asNMSCopy(itemStack).getItem() == null) {
            return false
        }
        return CraftItemStack.asNMSCopy(itemStack).getItem() is ItemSword
    }

    override fun isAxe(itemStack: ItemStack): Boolean {
        if (CraftItemStack.asNMSCopy(itemStack).getItem() == null) {
            return false
        }
        return CraftItemStack.asNMSCopy(itemStack).getItem() is ItemAxe
    }

    override fun isBow(itemStack: ItemStack): Boolean {
        if (CraftItemStack.asNMSCopy(itemStack) == null) {
            return false
        }
        if (CraftItemStack.asNMSCopy(itemStack).getItem() == null) {
            return false
        }
        return CraftItemStack.asNMSCopy(itemStack).getItem() is ItemBow
    }

    override fun isProjectile(itemStack: org.bukkit.inventory.ItemStack): Boolean {
        if (CraftItemStack.asNMSCopy(itemStack) == null) {
            return false
        }
        if (CraftItemStack.asNMSCopy(itemStack).getItem() == null) {
            return false
        }
        return CraftItemStack.asNMSCopy(itemStack).getItem() is IProjectile
    }

    override fun isInvisibilityPotion(itemStack: org.bukkit.inventory.ItemStack): Boolean {
        if (itemStack.getType() != org.bukkit.Material.POTION) {
            return false
        }
        val pm: org.bukkit.inventory.meta.PotionMeta? = itemStack.getItemMeta() as org.bukkit.inventory.meta.PotionMeta?
        if (pm != null && pm.hasCustomEffects()) {
            return pm.hasCustomEffect(org.bukkit.potion.PotionEffectType.INVISIBILITY)
        }
        val potion = org.bukkit.potion.Potion.fromItemStack(itemStack)
        val type = potion.getType()
        return type.getEffectType() == org.bukkit.potion.PotionEffectType.INVISIBILITY
    }

    override fun isGlass(type: Material): Boolean {
        // Avoids string search
        return type == Material.GLASS || type == Material.STAINED_GLASS
    }

    override fun registerEntities() {
        registerEntity("Silverfish2", 60, Silverfish::class.java)
        registerEntity("IGolem", 99, IGolem::class.java)
        registerEntity("BwVilager", 120, VillagerShop::class.java)
    }

    override fun setCollide(p: Player, a: IArena?, value: Boolean) {
        p.spigot().setCollidesWithEntities(value)
    }

    override fun minusAmount(p: Player, i: ItemStack, amount: Int) {
        if (i.getAmount() - amount <= 0) {
            p.getInventory().removeItem(i)
            return
        }
        i.setAmount(i.getAmount() - amount)
        p.updateInventory()
    }

    open class VillagerShop(loc: Location) : EntityVillager((loc.getWorld() as CraftWorld).getHandle()) {
        override fun move(d0: Double, d1: Double, d2: Double) {
        }

        override fun collide(entity: net.minecraft.server.v1_8_R3.Entity) {
        }

        override fun damageEntity(damagesource: DamageSource, f: Float): Boolean {
            return false
        }

        override fun g(d0: Double, d1: Double, d2: Double) {
        }

        override fun makeSound(s: String, f: Float, f1: Float) {
        }

        protected override fun initAttributes() {
            super.initAttributes()
            this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.0)
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
            } catch (ignored: Exception) {
            }
            this.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch())
            this.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch())
            ((loc.getWorld() as CraftWorld).getHandle()).addEntity(this, CreatureSpawnEvent.SpawnReason.CUSTOM)
            persistent = true
        }
    }

    private fun spawnVillager(loc: Location) {
        val nmsEntity = VillagerShop(loc)
        var tag: NBTTagCompound? = nmsEntity.getNBTTag()
        if (tag == null) {
            tag = NBTTagCompound()
        }
        nmsEntity.c(tag)
        tag.setInt("NoAI", 1)
        nmsEntity.f(tag)
        (nmsEntity.getBukkitEntity() as CraftLivingEntity).setRemoveWhenFarAway(false)
    }

    override fun spawnShop(loc: Location, name1: String, players: MutableList<Player>, arena: IArena) {
        val l = loc.clone()
        spawnVillager(l)
        for (p in players) {
            val nume = getMsg(p, name1).split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (nume.size == 1) {
                val a = createArmorStand(nume[0], l.clone().add(0.0, 1.85, 0.0))
                ShopHolo(Language.getPlayerLanguage(p).getIso().unchecked(), a, null, l, arena)
            } else {
                val a = createArmorStand(nume[0], l.clone().add(0.0, 2.1, 0.0))
                val b = createArmorStand(nume[1], l.clone().add(0.0, 1.85, 0.0))
                ShopHolo(Language.getPlayerLanguage(p).getIso().unchecked(), a, b, l, arena)
            }
        }
        for (sh in ShopHolo.getShopHolo()) {
            if (sh.getA() === arena) {
                sh.update()
            }
        }
    }

    override fun getDamage(i: ItemStack): Double {
        val nmsStack = CraftItemStack.asNMSCopy(i)
        val compound: NBTTagCompound = if ((nmsStack.hasTag())) nmsStack.getTag() else NBTTagCompound()
        return compound.getDouble("generic.attackDamage")
    }

    private fun registerEntity(name: String, id: Int, customClass: Class<*>) {
        try {
            val dataMap: ArrayList<MutableMap<*, *>> = ArrayList()
            for (f in EntityTypes::class.java.getDeclaredFields()) {
                if (f.getType().getSimpleName() != MutableMap::class.java.getSimpleName()) {
                    continue
                }
                f.setAccessible(true)
                dataMap.add(f.get(null) as MutableMap<*, *>)
            }
            if (dataMap.get(2).containsKey(id)) {
                dataMap.get(0).remove(name)
                dataMap.get(2).remove(id)
            }
            val method = EntityTypes::class.java.getDeclaredMethod("a", Class::class.java, String::class.java, Integer.TYPE)
            method.setAccessible(true)
            method.invoke(null, customClass, name, id)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun setSource(tnt: TNTPrimed, owner: Player) {
        val nmsEntityLiving: EntityLiving = ((owner as CraftLivingEntity).getHandle())
        val nmsTNT: EntityTNTPrimed = ((tnt as CraftTNTPrimed).getHandle())
        try {
            val sourceField = EntityTNTPrimed::class.java.getDeclaredField("source")
            sourceField.setAccessible(true)
            sourceField.set(nmsTNT, nmsEntityLiving)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    override fun voidKill(p: Player) {
        (p as CraftPlayer).getHandle().damageEntity(DamageSource.OUT_OF_WORLD, 1000f)
    }

    override fun hideArmor(victim: Player, receiver: Player) {
        if (victim == receiver) {
            return
        }
        val helmet = PacketPlayOutEntityEquipment(victim.getEntityId(), 1, CraftItemStack.asNMSCopy(ItemStack(org.bukkit.Material.AIR)))
        val chest = PacketPlayOutEntityEquipment(victim.getEntityId(), 2, CraftItemStack.asNMSCopy(ItemStack(org.bukkit.Material.AIR)))
        val pants = PacketPlayOutEntityEquipment(victim.getEntityId(), 3, CraftItemStack.asNMSCopy(ItemStack(org.bukkit.Material.AIR)))
        val boots = PacketPlayOutEntityEquipment(victim.getEntityId(), 4, CraftItemStack.asNMSCopy(ItemStack(org.bukkit.Material.AIR)))
        val boundTo = (receiver as CraftPlayer).getHandle().playerConnection
        boundTo.sendPacket(helmet)
        boundTo.sendPacket(chest)
        boundTo.sendPacket(pants)
        boundTo.sendPacket(boots)
    }

    override fun showArmor(victim: Player, receiver: Player) {
        if (victim == receiver) {
            return
        }
        val entityPlayer = (victim as CraftPlayer).getHandle()
        val helmet = PacketPlayOutEntityEquipment(entityPlayer.getId(), 4, entityPlayer.inventory.getArmorContents()[3])
        val chest = PacketPlayOutEntityEquipment(entityPlayer.getId(), 3, entityPlayer.inventory.getArmorContents()[2])
        val pants = PacketPlayOutEntityEquipment(entityPlayer.getId(), 2, entityPlayer.inventory.getArmorContents()[1])
        val boots = PacketPlayOutEntityEquipment(entityPlayer.getId(), 1, entityPlayer.inventory.getArmorContents()[0])
        val boundTo = (receiver as CraftPlayer).getHandle()
        boundTo.playerConnection.sendPacket(helmet)
        boundTo.playerConnection.sendPacket(chest)
        boundTo.playerConnection.sendPacket(pants)
        boundTo.playerConnection.sendPacket(boots)
    }

    override fun spawnDragon(l: Location?, bwt: ITeam) {
        l!!.getWorld().spawnEntity(l, EntityType.ENDER_DRAGON)
    }

    override fun colorBed(bwt: ITeam) {
    }

    override fun registerTntWhitelist(endStoneBlast: Float, glassBlast: Float) {
        try {
            val field = Block::class.java.getDeclaredField("durability")
            field.setAccessible(true)
            field.set(Block.getByName("glass"), glassBlast)
            field.set(Block.getByName("stained_glass"), glassBlast)
            field.set(Block.getByName("end_stone"), endStoneBlast)
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
    }

    override fun setBlockTeamColor(block: org.bukkit.block.Block, teamColor: TeamColor) {
        block.setData(teamColor.itemByte())
    }

    override fun addCustomData(i: org.bukkit.inventory.ItemStack, data: String): org.bukkit.inventory.ItemStack {
        val itemStack = CraftItemStack.asNMSCopy(i)
        var tag: NBTTagCompound? = itemStack.getTag()
        if (tag == null) {
            tag = NBTTagCompound()
        }
        tag.setString("BedWars1058", data)
        itemStack.setTag(tag)
        return CraftItemStack.asBukkitCopy(itemStack)
    }

    override fun setTag(itemStack: ItemStack, key: String, value: String): ItemStack {
        val `is` = CraftItemStack.asNMSCopy(itemStack)
        var tag: NBTTagCompound? = `is`.getTag()
        if (tag == null) {
            tag = NBTTagCompound()
        }
        tag.setString(key, value)
        `is`.setTag(tag)
        return CraftItemStack.asBukkitCopy(`is`)
    }

    override fun getTag(itemStack: ItemStack, key: String): String? {
        val i: net.minecraft.server.v1_8_R3.ItemStack? = CraftItemStack.asNMSCopy(itemStack)
        if (i == null) {
            return null
        }
        val tag: NBTTagCompound? = i.getTag()
        return if (tag == null) null else if (tag.hasKey(key)) tag.getString(key) else null
    }

    override fun isCustomBedWarsItem(i: org.bukkit.inventory.ItemStack): Boolean {
        val itemStack: net.minecraft.server.v1_8_R3.ItemStack? = CraftItemStack.asNMSCopy(i)
        if (itemStack == null) {
            return false
        }
        val tag: NBTTagCompound? = itemStack.getTag()
        if (tag == null) {
            return false
        }
        return tag.hasKey("BedWars1058")
    }

    override fun getCustomData(i: org.bukkit.inventory.ItemStack): String {
        val itemStack = CraftItemStack.asNMSCopy(i)
        val tag: NBTTagCompound? = itemStack.getTag()
        if (tag == null) {
            return ""
        }
        return tag.getString("BedWars1058")
    }

    override fun colourItem(itemStack: ItemStack?, bedWarsTeam: ITeam): ItemStack? {
        if (itemStack == null) {
            return null
        }
        when (itemStack.getType().toString()) {
            "WOOL", "STAINED_CLAY", "STAINED_GLASS" -> {
                return ItemStack(itemStack.getType(), itemStack.getAmount(), bedWarsTeam.getColor().itemByte().toShort())
            }
            "GLASS" -> {
                return ItemStack(org.bukkit.Material.STAINED_GLASS, itemStack.getAmount(), bedWarsTeam.getColor().itemByte().toShort())
            }
            else -> {
                return itemStack
            }
        }
    }

    override fun createItemStack(material: String, amount: Int, data: Short): org.bukkit.inventory.ItemStack {
        var i: org.bukkit.inventory.ItemStack
        try {
            i = org.bukkit.inventory.ItemStack(org.bukkit.Material.valueOf(material), amount, data)
        } catch (ex: Exception) {
            getPlugin().getLogger().log(Level.WARNING, material + " is not a valid " + getName() + " material!")
            i = org.bukkit.inventory.ItemStack(org.bukkit.Material.BEDROCK)
        }
        return i
    }

    override fun isPlayerHead(material: String, data: Int): Boolean {
        return material == "SKULL_ITEM" && data == 3
    }

    override fun materialFireball(): org.bukkit.Material {
        return org.bukkit.Material.FIREBALL
    }

    override fun materialPlayerHead(): org.bukkit.Material {
        return org.bukkit.Material.SKULL_ITEM
    }

    override fun materialSnowball(): org.bukkit.Material {
        return org.bukkit.Material.SNOW_BALL
    }

    override fun materialGoldenHelmet(): org.bukkit.Material {
        return org.bukkit.Material.GOLD_HELMET
    }

    override fun materialGoldenChestPlate(): org.bukkit.Material {
        return org.bukkit.Material.GOLD_CHESTPLATE
    }

    override fun materialGoldenLeggings(): org.bukkit.Material {
        return org.bukkit.Material.GOLD_LEGGINGS
    }

    override fun materialNetheriteHelmet(): org.bukkit.Material {
        //Netherite doesn't exist
        return Material.DIAMOND_HELMET
    }

    override fun materialNetheriteChestPlate(): org.bukkit.Material {
        //Netherite doesn't exist
        return Material.DIAMOND_CHESTPLATE
    }

    override fun materialNetheriteLeggings(): org.bukkit.Material {
        //Netherite doesn't exist
        return Material.DIAMOND_LEGGINGS
    }

    override fun materialElytra(): org.bukkit.Material? {
        //Elytra is 1.9+
        return null
    }

    override fun materialCake(): org.bukkit.Material {
        return org.bukkit.Material.CAKE_BLOCK
    }

    override fun materialCraftingTable(): org.bukkit.Material {
        return org.bukkit.Material.WORKBENCH
    }

    override fun materialEnchantingTable(): org.bukkit.Material {
        return org.bukkit.Material.ENCHANTMENT_TABLE
    }

    override fun isBed(material: org.bukkit.Material): Boolean {
        return material == org.bukkit.Material.BED_BLOCK || material == org.bukkit.Material.BED
    }

    override fun itemStackDataCompare(i: ItemStack, data: Short): Boolean {
        return i.getData().getData().toShort() == data
    }

    override fun setJoinSignBackgroundBlockData(block: org.bukkit.block.BlockState, data: Byte) {
        block.getBlock().getRelative((block.getData() as org.bukkit.material.Sign).getAttachedFace()).setData(data, true)
    }

    override fun setJoinSignBackground(b: org.bukkit.block.BlockState, material: org.bukkit.Material) {
        b.getLocation().getBlock().getRelative((b.getData() as org.bukkit.material.Sign).getAttachedFace()).setType(material)
    }

    override fun woolMaterial(): org.bukkit.Material {
        return org.bukkit.Material.WOOL
    }

    override fun getShopUpgradeIdentifier(itemStack: org.bukkit.inventory.ItemStack): String {
        val i = CraftItemStack.asNMSCopy(itemStack)
        val tag: NBTTagCompound? = i.getTag()
        return if (tag == null) "" else if (tag.hasKey("tierIdentifier")) tag.getString("tierIdentifier") else ""
    }

    override fun setShopUpgradeIdentifier(itemStack: org.bukkit.inventory.ItemStack, identifier: String): org.bukkit.inventory.ItemStack {
        val i = CraftItemStack.asNMSCopy(itemStack)
        var tag: NBTTagCompound? = i.getTag()
        if (tag == null) {
            tag = NBTTagCompound()
        }
        tag.setString(VersionSupport.PLUGIN_TAG_TIER_KEY, identifier)
        i.setTag(tag)
        return CraftItemStack.asBukkitCopy(i)
    }

    override fun getPlayerHead(player: Player, copyTagFrom: ItemStack?): ItemStack {
        var head = ItemStack(org.bukkit.Material.SKULL_ITEM, 1, 3.toShort())
        if (copyTagFrom != null) {
            val i = CraftItemStack.asNMSCopy(head)
            i.setTag(CraftItemStack.asNMSCopy(copyTagFrom).getTag())
            head = CraftItemStack.asBukkitCopy(i)
        }
        val headMeta = head.getItemMeta() as SkullMeta
        var profileField: Field
        try {
            profileField = headMeta.javaClass.getDeclaredField("profile")
            profileField.setAccessible(true)
            profileField.set(headMeta, (player as CraftPlayer).getProfile())
        } catch (e1: NoSuchFieldException) {
            e1.printStackTrace()
        } catch (e1: IllegalArgumentException) {
            e1.printStackTrace()
        } catch (e1: IllegalAccessException) {
            e1.printStackTrace()
        }
        head.setItemMeta(headMeta)
        return head
    }

    override fun sendPlayerSpawnPackets(respawned: Player?, arena: IArena?) {
        if (respawned == null) {
            return
        }
        if (arena == null) {
            return
        }
        if (!arena.isPlayer(respawned)) {
            return
        }
        // if method was used when the player was still in re-spawning screen
        if (arena.getRespawnSessions()!!.containsKey(respawned)) {
            return
        }
        val entityPlayer = (respawned as CraftPlayer).getHandle()
        val show = PacketPlayOutNamedEntitySpawn(entityPlayer)
        val playerVelocity = PacketPlayOutEntityVelocity(entityPlayer)
        val head = PacketPlayOutEntityHeadRotation(entityPlayer, getCompressedAngle(entityPlayer.yaw))
        val hand1 = PacketPlayOutEntityEquipment(entityPlayer.getId(), 0, entityPlayer.inventory.getItemInHand())
        val helmet = PacketPlayOutEntityEquipment(entityPlayer.getId(), 4, entityPlayer.inventory.getArmorContents()[3])
        val chest = PacketPlayOutEntityEquipment(entityPlayer.getId(), 3, entityPlayer.inventory.getArmorContents()[2])
        val pants = PacketPlayOutEntityEquipment(entityPlayer.getId(), 2, entityPlayer.inventory.getArmorContents()[1])
        val boots = PacketPlayOutEntityEquipment(entityPlayer.getId(), 1, entityPlayer.inventory.getArmorContents()[0])
        for (p in arena.getPlayers()!!) {
            if (p == null) {
                continue
            }
            if (p == respawned) {
                continue
            }
            // if p is in re-spawning screen continue
            if (arena.getRespawnSessions()!!.containsKey(p)) {
                continue
            }
            val boundTo = (p as CraftPlayer).getHandle()
            if (p.getWorld() == respawned.getWorld()) {
                if (respawned.getLocation().distance(p.getLocation()) <= arena.getRenderDistance()) {
                    // send respawned player to regular players
                    boundTo.playerConnection.sendPacket(show)
                    boundTo.playerConnection.sendPacket(playerVelocity)
                    for (packet in arrayOf<Packet<*>>(hand1, helmet, chest, pants, boots, head)) {
                        boundTo.playerConnection.sendPacket(packet)
                    }
                    // if the player has invisibility hide armor
                    if (p.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                        hideArmor(p, respawned)
                    } else {
                        val show2 = PacketPlayOutNamedEntitySpawn(boundTo)
                        val playerVelocity2 = PacketPlayOutEntityVelocity(boundTo)
                        val head2 = PacketPlayOutEntityHeadRotation(boundTo, getCompressedAngle(boundTo.yaw))
                        entityPlayer.playerConnection.sendPacket(show2)
                        entityPlayer.playerConnection.sendPacket(playerVelocity2)
                        entityPlayer.playerConnection.sendPacket(head2)
                        showArmor(p, respawned)
                    }
                    // send nearby players to respawned player
                }
            }
        }
        for (spectator in arena.getSpectators()!!) {
            if (spectator == null) {
                continue
            }
            if (spectator == respawned) {
                continue
            }
            val boundTo = (spectator as CraftPlayer).getHandle()
            respawned.hidePlayer(spectator)
            if (spectator.getWorld() == respawned.getWorld()) {
                if (respawned.getLocation().distance(spectator.getLocation()) <= arena.getRenderDistance()) {
                    // send respawned player to spectator
                    boundTo.playerConnection.sendPacket(show)
                    boundTo.playerConnection.sendPacket(playerVelocity)
                    boundTo.playerConnection.sendPacket(PacketPlayOutEntityHeadRotation(entityPlayer, getCompressedAngle(entityPlayer.yaw)))
                    for (packet in arrayOf<Packet<*>>(hand1, helmet, chest, pants, boots)) {
                        boundTo.playerConnection.sendPacket(packet)
                    }
                }
            }
        }
    }

    override fun getInventoryName(e: InventoryEvent): String {
        return e.getInventory().getName()
    }

    override fun setUnbreakable(itemMeta: ItemMeta) {
        itemMeta.spigot().setUnbreakable(true)
    }

    override fun getVersion(): Int {
        return 0
    }

    override fun registerVersionListeners() {
        VersionCommon(this)
    }

    override fun getMainLevel(): String {
        return (MinecraftServer.getServer() as DedicatedServer).propertyManager.properties.getProperty("level-name")
    }

    override fun setFireballDirection(fireball: Fireball, vector: Vector): Fireball {
        val fb = (fireball as CraftFireball).getHandle()
        fb.dirX = vector.getX() * 0.1
        fb.dirY = vector.getY() * 0.1
        fb.dirZ = vector.getZ() * 0.1
        return fb.getBukkitEntity() as Fireball
    }

    override fun playRedStoneDot(player: Player) {
        val color = Color.RED
        val particlePacket = PacketPlayOutWorldParticles(EnumParticle.REDSTONE, true, player.getLocation().getX().toFloat(), (player.getLocation().getY() + 2.6).toFloat(), player.getLocation().getZ().toFloat(), color.getRed().toFloat(), color.getRed().toFloat(), color.getRed().toFloat(), 0f, 0)
        for (inWorld in player.getWorld().getPlayers()) {
            if (inWorld == player) {
                continue
            }
            (inWorld as CraftPlayer).getHandle().playerConnection.sendPacket(particlePacket)
        }
    }

    override fun clearArrowsFromPlayerBody(player: Player) {
        (player as CraftLivingEntity).getHandle().getDataWatcher().watch(9, (-1).toByte())
    }

    override fun placeTowerBlocks(b: org.bukkit.block.Block, a: IArena, color: TeamColor, x: Int, y: Int, z: Int) {
        b.getRelative(x, y, z).setType(Material.WOOL)
        setBlockTeamColor(b.getRelative(x, y, z), color)
        a.addPlacedBlock(b.getRelative(x, y, z))
    }

    override fun placeLadder(b: org.bukkit.block.Block, x: Int, y: Int, z: Int, a: IArena, ladderdata: Int) {
        b.getRelative(x, y, z).setType(Material.LADDER)
        //noinspection deprecation
        b.getRelative(x, y, z).setData(ladderdata.toByte())
        a.addPlacedBlock(b.getRelative(x, y, z))
    }

    override fun playVillagerEffect(player: Player, location: Location) {
        val pwp = PacketPlayOutWorldParticles(EnumParticle.VILLAGER_HAPPY, true, location.getX().toFloat(), location.getY().toFloat(), location.getZ().toFloat(), 0.toFloat(), 0.toFloat(), 0.toFloat(), 0.toFloat(), 1)
        (player as CraftPlayer).getHandle().playerConnection.sendPacket(pwp)
    }

    init {
        try {
            setEggBridgeEffect("MOBSPAWNER_FLAMES")
        } catch (e: InvalidEffectException) {
            e.printStackTrace()
        }
    }

    companion object {
        @JvmStatic
        private fun createArmorStand(name: String, loc: Location?): ArmorStand? {
            val a = loc!!.getWorld().spawn(loc, ArmorStand::class.java)
            a.setGravity(false)
            a.setVisible(false)
            a.setCustomNameVisible(true)
            a.setCustomName(name)
            return a
        }
    }
}
