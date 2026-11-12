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

import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.api.arena.shop.ShopHolo
import com.andrei1058.bedwars.api.arena.team.ITeam
import com.andrei1058.bedwars.api.arena.team.TeamColor
import com.andrei1058.bedwars.api.entity.Despawnable
import com.andrei1058.bedwars.api.events.player.PlayerKillEvent
import com.andrei1058.bedwars.api.language.Language
import com.andrei1058.bedwars.api.language.Messages
import com.andrei1058.bedwars.api.server.VersionSupport
import com.andrei1058.bedwars.support.version.common.VersionCommon
import com.mojang.datafixers.DataFixUtils
import com.mojang.datafixers.types.Type
import com.mojang.datafixers.util.Pair
import java.lang.reflect.Field
import java.util.ArrayList
import java.util.UUID
import java.util.logging.Level
import net.minecraft.server.v1_16_R3.*
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.BlockState
import org.bukkit.block.data.type.Bed
import org.bukkit.block.data.type.Ladder
import org.bukkit.block.data.type.WallSign
import org.bukkit.command.Command
import org.bukkit.craftbukkit.v1_16_R3.CraftServer
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftFireball
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftLivingEntity
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftTNTPrimed
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack
import org.bukkit.entity.*
import org.bukkit.entity.Entity
import org.bukkit.event.inventory.InventoryEvent
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.plugin.Plugin
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector
import com.andrei1058.bedwars.api.util.unchecked

open class v1_16_R3(plugin: Plugin, name: String) : VersionSupport(plugin, name) {
    override fun registerVersionListeners() {
        VersionCommon(this)
    }

    override fun registerCommand(name: String, clasa: Command) {
        (getPlugin().getServer() as CraftServer).getCommandMap().register(name, clasa)
    }

    override fun getTag(itemStack: org.bukkit.inventory.ItemStack, key: String): String? {
        val i: net.minecraft.server.v1_16_R3.ItemStack? = CraftItemStack.asNMSCopy(itemStack)
        if (i == null) {
            return null
        }
        val tag: NBTTagCompound? = i.getTag()
        return if (tag == null) null else if (tag.hasKey(key)) tag.getString(key) else null
    }

    override fun sendTitle(p: Player, title: String?, subtitle: String?, fadeIn: Int, stay: Int, fadeOut: Int) {
        p.sendTitle(title ?: " ", subtitle ?: " ", fadeIn, stay, fadeOut)
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
        val ppoc = PacketPlayOutChat(cbc, ChatMessageType.GAME_INFO, chatUUID)
        cPlayer.getHandle().playerConnection.sendPacket(ppoc)
    }

    override fun isBukkitCommandRegistered(name: String): Boolean {
        return (getPlugin().getServer() as CraftServer).getCommandMap().getCommand(name) != null
    }

    override fun getItemInHand(p: Player): org.bukkit.inventory.ItemStack {
        return p.getInventory().getItemInMainHand()
    }

    override fun hideEntity(e: Entity, p: Player) {
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
        if (CraftItemStack.asNMSCopy(itemStack).A() == null) {
            return false
        }
        return CraftItemStack.asNMSCopy(itemStack).A() is IProjectile
    }

    override fun isInvisibilityPotion(itemStack: org.bukkit.inventory.ItemStack): Boolean {
        if (itemStack.getType() != org.bukkit.Material.POTION) {
            return false
        }
        val pm: org.bukkit.inventory.meta.PotionMeta? = itemStack.getItemMeta() as org.bukkit.inventory.meta.PotionMeta?
        return pm != null && pm.hasCustomEffects() && pm.hasCustomEffect(org.bukkit.potion.PotionEffectType.INVISIBILITY)
    }

    override fun registerEntities() {
        val types = DataConverterRegistry.a().getSchema(DataFixUtils.makeKey(SharedConstants.getGameVersion().getWorldVersion())).findChoiceType(DataConverterTypes.ENTITY).types() as MutableMap<String, Type<*>>
        types.put("minecraft:bwsilverfish", types.get("minecraft:silverfish").unchecked())
        EntityTypes.Builder.a<Silverfish>(EntityTypes.b<Silverfish> { t, w -> Silverfish(t, w) }, EnumCreatureType.MONSTER).a("bwsilverfish")
        types.put("minecraft:bwgolem", types.get("minecraft:iron_golem").unchecked())
        EntityTypes.Builder.a<IGolem>(EntityTypes.b<IGolem> { t, w -> IGolem(t, w) }, EnumCreatureType.AMBIENT).a("bwgolem")
    }

    override fun spawnShop(loc: Location, name1: String, players: MutableList<Player>, arena: IArena) {
        val l = loc.clone()
        if (l.getWorld() == null) {
            return
        }
        val vlg = l.getWorld()!!.spawnEntity(loc, EntityType.VILLAGER) as Villager
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

    override fun voidKill(p: Player) {
        (p as CraftPlayer).getHandle().damageEntity(DamageSource.OUT_OF_WORLD, 1000f)
    }

    override fun hideArmor(victim: Player, receiver: Player) {
        val items: MutableList<Pair<EnumItemSlot, ItemStack>> = ArrayList()
        items.add(Pair(EnumItemSlot.HEAD, ItemStack(net.minecraft.server.v1_16_R3.Item.getById(0))))
        items.add(Pair(EnumItemSlot.CHEST, ItemStack(net.minecraft.server.v1_16_R3.Item.getById(0))))
        items.add(Pair(EnumItemSlot.LEGS, ItemStack(net.minecraft.server.v1_16_R3.Item.getById(0))))
        items.add(Pair(EnumItemSlot.FEET, ItemStack(net.minecraft.server.v1_16_R3.Item.getById(0))))
        val packet1 = PacketPlayOutEntityEquipment(victim.getEntityId(), items)
        val pc = (receiver as CraftPlayer).getHandle()
        pc.playerConnection.sendPacket(packet1)
    }

    override fun showArmor(victim: Player, receiver: Player) {
        val items: MutableList<Pair<EnumItemSlot, ItemStack>> = ArrayList()
        items.add(Pair(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(victim.getInventory().getHelmet())))
        items.add(Pair(EnumItemSlot.CHEST, CraftItemStack.asNMSCopy(victim.getInventory().getChestplate())))
        items.add(Pair(EnumItemSlot.LEGS, CraftItemStack.asNMSCopy(victim.getInventory().getLeggings())))
        items.add(Pair(EnumItemSlot.FEET, CraftItemStack.asNMSCopy(victim.getInventory().getBoots())))
        val packet1 = PacketPlayOutEntityEquipment(victim.getEntityId(), items)
        val pc = (receiver as CraftPlayer).getHandle()
        pc.playerConnection.sendPacket(packet1)
    }

    override fun spawnDragon(l: Location?, bwt: ITeam) {
        if (l == null || l.getWorld() == null) {
            getPlugin().getLogger().log(Level.WARNING, "Could not spawn Dragon. Location is null")
            return
        }
        val ed = l.getWorld()!!.spawnEntity(l, EntityType.ENDER_DRAGON) as EnderDragon
        ed.setPhase(EnderDragon.Phase.CIRCLING)
    }

    override fun colorBed(bwt: ITeam) {
        for (x in -1..1) {
            for (z in -1..1) {
                val bed = bwt.getBed()!!.clone().add(x.toDouble(), 0.0, z.toDouble()).getBlock().getState()
                if (bed is Bed) {
                    bed.setType(bwt.getColor().bedMaterial())
                    bed.update()
                }
            }
        }
    }

    override fun registerTntWhitelist(endStoneBlast: Float, glassBlast: Float) {
        try {
            val field = net.minecraft.server.v1_16_R3.BlockBase::class.java.getDeclaredField("durability")
            field.setAccessible(true)
            for (glass in arrayOf<net.minecraft.server.v1_16_R3.Block>(Blocks.WHITE_STAINED_GLASS, Blocks.ORANGE_STAINED_GLASS, Blocks.MAGENTA_STAINED_GLASS, Blocks.LIGHT_BLUE_STAINED_GLASS, Blocks.YELLOW_STAINED_GLASS, Blocks.LIME_STAINED_GLASS, Blocks.PINK_STAINED_GLASS, Blocks.GRAY_STAINED_GLASS, Blocks.LIGHT_GRAY_STAINED_GLASS, Blocks.CYAN_STAINED_GLASS, Blocks.PURPLE_STAINED_GLASS, Blocks.BLUE_STAINED_GLASS, Blocks.BROWN_STAINED_GLASS, Blocks.GREEN_STAINED_GLASS, Blocks.RED_STAINED_GLASS, Blocks.BLACK_STAINED_GLASS, Blocks.GLASS)) {
                field.set(glass, glassBlast)
            }
            field.set(Blocks.END_STONE, endStoneBlast)
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
    }

    override fun setBlockTeamColor(block: Block, teamColor: TeamColor) {
        if (block.getType().toString().contains("STAINED_GLASS") || block.getType().toString() == "GLASS") {
            block.setType(teamColor.glassMaterial())
        } else if (block.getType().toString().contains("_TERRACOTTA")) {
            block.setType(teamColor.glazedTerracottaMaterial())
        } else if (block.getType().toString().contains("_WOOL")) {
            block.setType(teamColor.woolMaterial())
        }
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

    override fun setTag(itemStack: org.bukkit.inventory.ItemStack, key: String, value: String): org.bukkit.inventory.ItemStack {
        val `is` = CraftItemStack.asNMSCopy(itemStack)
        var tag: NBTTagCompound? = `is`.getTag()
        if (tag == null) {
            tag = NBTTagCompound()
        }
        tag.setString(key, value)
        `is`.setTag(tag)
        return CraftItemStack.asBukkitCopy(`is`)
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
        val type = itemStack.getType().toString()
        if (type.contains("_BED")) {
            return org.bukkit.inventory.ItemStack(bedWarsTeam.getColor().bedMaterial(), itemStack.getAmount())
        } else if (type.contains("_STAINED_GLASS_PANE")) {
            return org.bukkit.inventory.ItemStack(bedWarsTeam.getColor().glassPaneMaterial(), itemStack.getAmount())
        } else if (type.contains("STAINED_GLASS") || type == "GLASS") {
            return org.bukkit.inventory.ItemStack(bedWarsTeam.getColor().glassMaterial(), itemStack.getAmount())
        } else if (type.contains("_TERRACOTTA")) {
            return org.bukkit.inventory.ItemStack(bedWarsTeam.getColor().glazedTerracottaMaterial(), itemStack.getAmount())
        } else if (type.contains("_WOOL")) {
            return org.bukkit.inventory.ItemStack(bedWarsTeam.getColor().woolMaterial(), itemStack.getAmount())
        }
        return itemStack
    }

    override fun createItemStack(material: String, amount: Int, data: Short): org.bukkit.inventory.ItemStack {
        var i: org.bukkit.inventory.ItemStack
        try {
            i = org.bukkit.inventory.ItemStack(org.bukkit.Material.valueOf(material), amount)
        } catch (ex: Exception) {
            getPlugin().getLogger().log(Level.WARNING, material + " is not a valid " + getName() + " material!")
            i = org.bukkit.inventory.ItemStack(org.bukkit.Material.BEDROCK)
        }
        return i
    }

    override fun materialFireball(): org.bukkit.Material {
        return org.bukkit.Material.FIRE_CHARGE
    }

    override fun materialPlayerHead(): org.bukkit.Material {
        return org.bukkit.Material.PLAYER_HEAD
    }

    override fun materialSnowball(): org.bukkit.Material {
        return org.bukkit.Material.SNOWBALL
    }

    override fun materialGoldenHelmet(): org.bukkit.Material {
        return org.bukkit.Material.GOLDEN_HELMET
    }

    override fun materialGoldenChestPlate(): org.bukkit.Material {
        return org.bukkit.Material.GOLDEN_CHESTPLATE
    }

    override fun materialGoldenLeggings(): org.bukkit.Material {
        return org.bukkit.Material.GOLDEN_LEGGINGS
    }

    override fun materialNetheriteHelmet(): org.bukkit.Material {
        return Material.NETHERITE_HELMET
    }

    override fun materialNetheriteChestPlate(): org.bukkit.Material {
        return Material.NETHERITE_CHESTPLATE
    }

    override fun materialNetheriteLeggings(): org.bukkit.Material {
        return Material.NETHERITE_LEGGINGS
    }

    override fun materialElytra(): org.bukkit.Material? {
        return Material.ELYTRA
    }

    override fun materialCake(): org.bukkit.Material {
        return org.bukkit.Material.CAKE
    }

    override fun materialCraftingTable(): org.bukkit.Material {
        return org.bukkit.Material.CRAFTING_TABLE
    }

    override fun materialEnchantingTable(): org.bukkit.Material {
        return org.bukkit.Material.ENCHANTING_TABLE
    }

    override fun woolMaterial(): org.bukkit.Material {
        return org.bukkit.Material.WHITE_WOOL
    }

    override fun getShopUpgradeIdentifier(itemStack: org.bukkit.inventory.ItemStack): String {
        val i = CraftItemStack.asNMSCopy(itemStack)
        val tag: NBTTagCompound? = i.getTag()
        return if (tag == null) "null" else if (tag.hasKey("tierIdentifier")) tag.getString("tierIdentifier") else "null"
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
        var head = org.bukkit.inventory.ItemStack(materialPlayerHead())
        if (copyTagFrom != null) {
            val i = CraftItemStack.asNMSCopy(head)
            i.setTag(CraftItemStack.asNMSCopy(copyTagFrom).getTag())
            head = CraftItemStack.asBukkitCopy(i)
        }
        val headMeta = head.getItemMeta() as SkullMeta
        var profileField: Field
        try {
            //noinspection ConstantConditions
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
        val list: MutableList<Pair<EnumItemSlot, ItemStack>> = ArrayList()
        list.add(Pair(EnumItemSlot.MAINHAND, entityPlayer.getItemInMainHand()))
        list.add(Pair(EnumItemSlot.OFFHAND, entityPlayer.getItemInOffHand()))
        list.add(Pair(EnumItemSlot.HEAD, entityPlayer.inventory.getArmorContents().get(3)))
        list.add(Pair(EnumItemSlot.CHEST, entityPlayer.inventory.getArmorContents().get(2)))
        list.add(Pair(EnumItemSlot.LEGS, entityPlayer.inventory.getArmorContents().get(1)))
        list.add(Pair(EnumItemSlot.FEET, entityPlayer.inventory.getArmorContents().get(0)))
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
                    boundTo.playerConnection.sendPacket(head)
                    boundTo.playerConnection.sendPacket(playerVelocity)
                    boundTo.playerConnection.sendPacket(PacketPlayOutEntityEquipment(entityPlayer.getId(), list))
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
                    boundTo.playerConnection.sendPacket(PacketPlayOutEntityEquipment(entityPlayer.getId(), list))
                    boundTo.playerConnection.sendPacket(PacketPlayOutEntityHeadRotation(entityPlayer, getCompressedAngle(entityPlayer.yaw)))
                }
            }
        }
    }

    override fun getInventoryName(e: InventoryEvent): String {
        return e.getView().getTitle()
    }

    override fun setUnbreakable(itemMeta: ItemMeta) {
        itemMeta.setUnbreakable(true)
    }

    override fun getMainLevel(): String {
        //noinspection deprecation
        return (MinecraftServer.getServer() as DedicatedServer).getDedicatedServerProperties().levelName
    }

    override fun getVersion(): Int {
        return 8
    }

    override fun setJoinSignBackground(b: BlockState, material: org.bukkit.Material) {
        if (b.getBlockData() is WallSign) {
            b.getBlock().getRelative((b.getBlockData() as WallSign).getFacing().getOppositeFace()).setType(material)
        }
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
        val particlePacket = PacketPlayOutWorldParticles(ParticleParamRedstone(color.getRed().toFloat(), color.getBlue().toFloat(), color.getGreen().toFloat(), 1.toFloat()), true, player.getLocation().getX().toFloat().toDouble(), (player.getLocation().getY() + 2.6).toFloat().toDouble(), player.getLocation().getZ().toFloat().toDouble(), 0f, 0f, 0f, 0f, 0)
        for (inWorld in player.getWorld().getPlayers()) {
            if (inWorld == player) {
                continue
            }
            (inWorld as CraftPlayer).getHandle().playerConnection.sendPacket(particlePacket)
        }
    }

    override fun clearArrowsFromPlayerBody(player: Player) {
        (player as CraftLivingEntity).getHandle().getDataWatcher().set(DataWatcherObject(11, DataWatcherRegistry.b), -1)
    }

    override fun placeTowerBlocks(b: Block, a: IArena, color: TeamColor, x: Int, y: Int, z: Int) {
        b.getRelative(x, y, z).setType(color.woolMaterial())
        a.addPlacedBlock(b.getRelative(x, y, z))
    }

    override fun placeLadder(b: Block, x: Int, y: Int, z: Int, a: IArena, ladderdata: Int) {
        //ladder block
        val block = b.getRelative(x, y, z)
        block.setType(Material.LADDER)
        val ladder = block.getBlockData() as Ladder
        a.addPlacedBlock(block)
        when (ladderdata) {
            2 -> {
                ladder.setFacing(BlockFace.NORTH)
                block.setBlockData(ladder)
                return
            }
            3 -> {
                ladder.setFacing(BlockFace.SOUTH)
                block.setBlockData(ladder)
                return
            }
            4 -> {
                ladder.setFacing(BlockFace.WEST)
                block.setBlockData(ladder)
                return
            }
            5 -> {
                ladder.setFacing(BlockFace.EAST)
                block.setBlockData(ladder)
            }
            else -> {}
        }
    }

    override fun playVillagerEffect(player: Player, location: Location) {
        player.spawnParticle(org.bukkit.Particle.VILLAGER_HAPPY, location, 1)
    }

    init {
        loadDefaultEffects()
    }

    companion object {
        private val chatUUID: UUID = UUID(0L, 0L)

        @JvmStatic
        private fun createArmorStand(name: String, loc: Location?): ArmorStand? {
            if (loc == null) {
                return null
            }
            if (loc.getWorld() == null) {
                return null
            }
            val a = loc.getWorld()!!.spawn(loc, ArmorStand::class.java)
            a.setGravity(false)
            a.setVisible(false)
            a.setCustomNameVisible(true)
            a.setCustomName(name)
            return a
        }
    }
}
