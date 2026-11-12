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

import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.api.arena.shop.ShopHolo
import com.andrei1058.bedwars.api.arena.team.ITeam
import com.andrei1058.bedwars.api.arena.team.TeamColor
import com.andrei1058.bedwars.api.entity.Despawnable
import com.andrei1058.bedwars.api.events.player.PlayerKillEvent
import com.andrei1058.bedwars.api.exceptions.InvalidEffectException
import com.andrei1058.bedwars.api.language.Language
import com.andrei1058.bedwars.api.language.Messages
import com.andrei1058.bedwars.api.server.VersionSupport
import com.andrei1058.bedwars.support.version.common.VersionCommon
import java.lang.reflect.Field
import java.util.logging.Level
import net.minecraft.server.v1_12_R1.*
import net.minecraft.server.v1_12_R1.Item
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.block.Bed
import org.bukkit.block.BlockState
import org.bukkit.command.Command
import org.bukkit.craftbukkit.v1_12_R1.CraftServer
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftFireball
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftLivingEntity
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftTNTPrimed
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack
import org.bukkit.entity.*
import org.bukkit.event.inventory.InventoryEvent
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.plugin.Plugin
import org.bukkit.potion.PotionEffectType
import org.bukkit.scoreboard.Team
import org.bukkit.util.Vector
import com.andrei1058.bedwars.api.util.unchecked

open class v1_12_R1(plugin: Plugin, name: String) : VersionSupport(plugin, name) {
    override fun getTag(itemStack: org.bukkit.inventory.ItemStack, key: String): String? {
        val i: net.minecraft.server.v1_12_R1.ItemStack? = CraftItemStack.asNMSCopy(itemStack)
        if (i == null) {
            return null
        }
        val tag: NBTTagCompound? = i.getTag()
        return if (tag == null) null else if (tag.hasKey(key)) tag.getString(key) else null
    }

    override fun registerVersionListeners() {
        VersionCommon(this)
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

    override fun spawnSilverfish(loc: Location, bedWarsTeam: ITeam, speed: Double, health: Double, despawn: Int, damage: Double) {
        Despawnable(Silverfish.spawn(loc, bedWarsTeam, speed, health, despawn, damage), bedWarsTeam, despawn, Messages.SHOP_UTILITY_NPC_SILVERFISH_NAME, PlayerKillEvent.PlayerKillCause.SILVERFISH_FINAL_KILL, PlayerKillEvent.PlayerKillCause.SILVERFISH)
    }

    override fun spawnIronGolem(loc: Location, bedWarsTeam: ITeam, speed: Double, health: Double, despawn: Int) {
        Despawnable(IGolem.spawn(loc, bedWarsTeam, speed, health, despawn), bedWarsTeam, despawn, Messages.SHOP_UTILITY_NPC_IRON_GOLEM_NAME, PlayerKillEvent.PlayerKillCause.IRON_GOLEM_FINAL_KILL, PlayerKillEvent.PlayerKillCause.IRON_GOLEM)
    }

    override fun playAction(p: Player, text: String) {
        val cPlayer = p as CraftPlayer
        val cbc = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + text + "\"}")
        val ppoc = PacketPlayOutChat(cbc, ChatMessageType.GAME_INFO)
        cPlayer.getHandle().playerConnection.sendPacket(ppoc)
    }

    override fun isBukkitCommandRegistered(name: String): Boolean {
        return (getPlugin().getServer() as CraftServer).getCommandMap().getCommand(name) != null
    }

    override fun getItemInHand(p: Player): org.bukkit.inventory.ItemStack {
        return p.getInventory().getItemInMainHand()
    }

    override fun hideEntity(e: org.bukkit.entity.Entity, p: Player) {
        val packet = PacketPlayOutEntityDestroy(e.getEntityId())
        (p as CraftPlayer).getHandle().playerConnection.sendPacket(packet)
    }

    override fun minusAmount(p: Player, i: org.bukkit.inventory.ItemStack, amount: Int) {
        if (i.getAmount() - amount <= 0) {
            if (p.getInventory().getItemInOffHand() == i) {
                p.getInventory().setItemInOffHand(null)
            } else {
                p.getInventory().removeItem(i)
            }
            return
        }
        i.setAmount(i.getAmount() - amount)
        p.updateInventory()
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

    override fun isArmor(itemStack: org.bukkit.inventory.ItemStack): Boolean {
        if (CraftItemStack.asNMSCopy(itemStack) == null) {
            return false
        }
        if (CraftItemStack.asNMSCopy(itemStack).getItem() == null) {
            return false
        }
        return (CraftItemStack.asNMSCopy(itemStack).getItem() is ItemArmor) || (CraftItemStack.asNMSCopy(itemStack).getItem() is ItemElytra)
    }

    override fun isTool(itemStack: org.bukkit.inventory.ItemStack): Boolean {
        if (CraftItemStack.asNMSCopy(itemStack) == null) {
            return false
        }
        if (CraftItemStack.asNMSCopy(itemStack).getItem() == null) {
            return false
        }
        return CraftItemStack.asNMSCopy(itemStack).getItem() is ItemTool
    }

    override fun isSword(itemStack: org.bukkit.inventory.ItemStack): Boolean {
        if (CraftItemStack.asNMSCopy(itemStack) == null) {
            return false
        }
        if (CraftItemStack.asNMSCopy(itemStack).getItem() == null) {
            return false
        }
        return CraftItemStack.asNMSCopy(itemStack).getItem() is ItemSword
    }

    override fun isAxe(itemStack: org.bukkit.inventory.ItemStack): Boolean {
        if (CraftItemStack.asNMSCopy(itemStack).getItem() == null) {
            return false
        }
        return CraftItemStack.asNMSCopy(itemStack).getItem() is ItemAxe
    }

    override fun isBow(itemStack: org.bukkit.inventory.ItemStack): Boolean {
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
        val pm = itemStack.getItemMeta() as org.bukkit.inventory.meta.PotionMeta
        return pm.hasCustomEffects() && pm.hasCustomEffect(org.bukkit.potion.PotionEffectType.INVISIBILITY)
    }

    override fun registerEntities() {
        registerEntity("Silverfish2", 60, Silverfish::class.java)
        registerEntity("IGolem", 99, IGolem::class.java)
    }

    override fun spawnShop(loc: Location, name1: String, players: MutableList<Player>, arena: IArena) {
        val l = loc.clone()
        val vlg = loc.getWorld().spawnEntity(loc, EntityType.VILLAGER) as Villager
        vlg.setAI(false)
        vlg.setRemoveWhenFarAway(false)
        vlg.setCollidable(false)
        vlg.setInvulnerable(true)
        vlg.setSilent(true)
        for (p in players) {
            val nume = Language.getMsg(p, name1).split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
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

    override fun getDamage(i: org.bukkit.inventory.ItemStack): Double {
        val nmsStack = CraftItemStack.asNMSCopy(i)
        val compound: NBTTagCompound = (if ((nmsStack.hasTag())) nmsStack.getTag() else NBTTagCompound()).unchecked()
        //noinspection ConstantConditions
        return compound.getDouble("generic.attackDamage")
    }

    private fun registerEntity(name: String, id: Int, customClass: Class<out net.minecraft.server.v1_12_R1.Entity>) {
        EntityTypes.b.a(id, MinecraftKey(name), customClass)
    }

    /**
     * Spawn shop npc
     */
    private fun spawnVillager(loc: Location) {
        val vlg = loc.getWorld().spawnEntity(loc, EntityType.VILLAGER) as Villager
        vlg.setAI(false)
        vlg.setRemoveWhenFarAway(false)
        vlg.setCollidable(false)
        vlg.setInvulnerable(true)
        vlg.setSilent(true)
    }

    override fun voidKill(p: Player) {
        (p as CraftPlayer).getHandle().damageEntity(DamageSource.OUT_OF_WORLD, 1000f)
    }

    override fun hideArmor(victim: Player, receiver: Player) {
        val helmet = PacketPlayOutEntityEquipment(victim.getEntityId(), EnumItemSlot.HEAD, ItemStack(Item.getById(0)))
        val chest = PacketPlayOutEntityEquipment(victim.getEntityId(), EnumItemSlot.CHEST, ItemStack(Item.getById(0)))
        val pants = PacketPlayOutEntityEquipment(victim.getEntityId(), EnumItemSlot.LEGS, ItemStack(Item.getById(0)))
        val boots = PacketPlayOutEntityEquipment(victim.getEntityId(), EnumItemSlot.FEET, ItemStack(Item.getById(0)))
        val pc = (receiver as CraftPlayer).getHandle()
        pc.playerConnection.sendPacket(helmet)
        pc.playerConnection.sendPacket(chest)
        pc.playerConnection.sendPacket(pants)
        pc.playerConnection.sendPacket(boots)
    }

    override fun showArmor(victim: Player, receiver: Player) {
        val helmet = PacketPlayOutEntityEquipment(victim.getEntityId(), EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(victim.getInventory().getHelmet()))
        val chest = PacketPlayOutEntityEquipment(victim.getEntityId(), EnumItemSlot.CHEST, CraftItemStack.asNMSCopy(victim.getInventory().getChestplate()))
        val pants = PacketPlayOutEntityEquipment(victim.getEntityId(), EnumItemSlot.LEGS, CraftItemStack.asNMSCopy(victim.getInventory().getLeggings()))
        val boots = PacketPlayOutEntityEquipment(victim.getEntityId(), EnumItemSlot.FEET, CraftItemStack.asNMSCopy(victim.getInventory().getBoots()))
        val pc = (receiver as CraftPlayer).getHandle()
        pc.playerConnection.sendPacket(helmet)
        pc.playerConnection.sendPacket(chest)
        pc.playerConnection.sendPacket(pants)
        pc.playerConnection.sendPacket(boots)
    }

    override fun spawnDragon(l: Location?, bwt: ITeam) {
        if (l == null || l.getWorld() == null) {
            getPlugin().getLogger().log(Level.WARNING, "Could not spawn Dragon. Location is null")
            return
        }
        val ed = l.getWorld().spawnEntity(l, EntityType.ENDER_DRAGON) as EnderDragon
        ed.setPhase(EnderDragon.Phase.CIRCLING)
    }

    override fun colorBed(bwt: ITeam) {
        for (x in -1..1) {
            for (z in -1..1) {
                val bed = bwt.getBed()!!.clone().add(x.toDouble(), 0.0, z.toDouble()).getBlock().getState()
                if (bed is Bed) {
                    (bed as Bed).setColor(bwt.getColor().dye())
                    bed.update()
                }
            }
        }
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

    override fun setCollide(p: Player, a: IArena?, value: Boolean) {
        p.setCollidable(value)
        if (a == null) {
            return
        }
        a.updateSpectatorCollideRule(p, value)
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

    override fun setTag(i: org.bukkit.inventory.ItemStack, key: String, value: String): org.bukkit.inventory.ItemStack {
        val itemStack = CraftItemStack.asNMSCopy(i)
        var tag: NBTTagCompound? = itemStack.getTag()
        if (tag == null) {
            tag = NBTTagCompound()
        }
        tag.setString(key, value)
        itemStack.setTag(tag)
        return CraftItemStack.asBukkitCopy(itemStack)
    }

    override fun isCustomBedWarsItem(i: org.bukkit.inventory.ItemStack): Boolean {
        val itemStack = CraftItemStack.asNMSCopy(i)
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

    override fun colourItem(itemStack: org.bukkit.inventory.ItemStack?, bedWarsTeam: ITeam): org.bukkit.inventory.ItemStack? {
        if (itemStack == null) {
            return null
        }
        when (itemStack.getType().toString()) {
            "WOOL", "STAINED_CLAY", "STAINED_GLASS" -> {
                return org.bukkit.inventory.ItemStack(itemStack.getType(), itemStack.getAmount(), bedWarsTeam.getColor().itemByte().toShort())
            }
            "GLASS" -> {
                return org.bukkit.inventory.ItemStack(org.bukkit.Material.STAINED_GLASS, itemStack.getAmount(), bedWarsTeam.getColor().itemByte().toShort())
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
            getPlugin().getLogger().severe(material + " is not a valid " + getVersion() + " material!")
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
        return Material.ELYTRA
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

    override fun itemStackDataCompare(i: org.bukkit.inventory.ItemStack, data: Short): Boolean {
        return i.getData().getData().toShort() == data
    }

    override fun setJoinSignBackgroundBlockData(block: BlockState, data: Byte) {
        block.getBlock().getRelative((block.getData() as org.bukkit.material.Sign).getAttachedFace()).setData(data, true)
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

    override fun getPlayerHead(player: Player, copyTagFrom: org.bukkit.inventory.ItemStack?): org.bukkit.inventory.ItemStack {
        var head = org.bukkit.inventory.ItemStack(org.bukkit.Material.SKULL_ITEM, 1, 3.toShort())
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
        if (arena.isReSpawning(respawned)) {
            return
        }
        val entityPlayer = (respawned as CraftPlayer).getHandle()
        val show = PacketPlayOutNamedEntitySpawn(entityPlayer)
        val playerVelocity = PacketPlayOutEntityVelocity(entityPlayer)
        val head = PacketPlayOutEntityHeadRotation(entityPlayer, getCompressedAngle(entityPlayer.yaw))
        val hand1 = PacketPlayOutEntityEquipment(entityPlayer.getId(), EnumItemSlot.MAINHAND, entityPlayer.inventory.getItemInHand())
        val hand2 = PacketPlayOutEntityEquipment(entityPlayer.getId(), EnumItemSlot.OFFHAND, entityPlayer.getItemInOffHand())
        val helmet = PacketPlayOutEntityEquipment(entityPlayer.getId(), EnumItemSlot.HEAD, entityPlayer.inventory.getArmorContents().get(3))
        val chest = PacketPlayOutEntityEquipment(entityPlayer.getId(), EnumItemSlot.CHEST, entityPlayer.inventory.getArmorContents().get(2))
        val pants = PacketPlayOutEntityEquipment(entityPlayer.getId(), EnumItemSlot.LEGS, entityPlayer.inventory.getArmorContents().get(1))
        val boots = PacketPlayOutEntityEquipment(entityPlayer.getId(), EnumItemSlot.FEET, entityPlayer.inventory.getArmorContents().get(0))
        for (p in arena.getPlayers()!!) {
            if (p == null) {
                continue
            }
            if (p == respawned) {
                continue
            }
            // if p is in re-spawning screen continue
            if (arena.isReSpawning(p)) {
                continue
            }
            val boundTo = (p as CraftPlayer).getHandle()
            if (p.getWorld() == respawned.getWorld()) {
                if (respawned.getLocation().distance(p.getLocation()) <= arena.getRenderDistance()) {
                    // send respawned player to regular players
                    boundTo.playerConnection.sendPacket(show)
                    boundTo.playerConnection.sendPacket(playerVelocity)
                    for (packet in arrayOf<Packet<*>>(hand1, helmet, chest, pants, boots, hand2, head)) {
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
            respawned.hidePlayer(getPlugin(), spectator)
            if (spectator.getWorld() == respawned.getWorld()) {
                if (respawned.getLocation().distance(spectator.getLocation()) <= arena.getRenderDistance()) {
                    // send respawned player to spectator
                    boundTo.playerConnection.sendPacket(show)
                    boundTo.playerConnection.sendPacket(playerVelocity)
                    boundTo.playerConnection.sendPacket(PacketPlayOutEntityHeadRotation(entityPlayer, getCompressedAngle(entityPlayer.yaw)))
                    for (packet in arrayOf<Packet<*>>(hand1, helmet, chest, pants, boots, hand2)) {
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
        itemMeta.setUnbreakable(true)
    }

    override fun getMainLevel(): String {
        //noinspection deprecation
        return (MinecraftServer.getServer() as DedicatedServer).propertyManager.properties.getProperty("level-name")
    }

    override fun getVersion(): Int {
        return 5
    }

    override fun setJoinSignBackground(b: org.bukkit.block.BlockState, material: org.bukkit.Material) {
        b.getLocation().getBlock().getRelative((b.getData() as org.bukkit.material.Sign).getAttachedFace()).setType(material)
    }

    override fun spigotShowPlayer(victim: Player, receiver: Player) {
        receiver.showPlayer(getPlugin(), victim)
    }

    override fun spigotHidePlayer(victim: Player, receiver: Player) {
        receiver.hidePlayer(getPlugin(), victim)
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
        (player as CraftLivingEntity).getHandle().getDataWatcher().set(DataWatcherObject(10, DataWatcherRegistry.b), -1)
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
        player.spawnParticle(Particle.VILLAGER_HAPPY, location, 1)
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
