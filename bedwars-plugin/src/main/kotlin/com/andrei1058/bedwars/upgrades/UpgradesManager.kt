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

package com.andrei1058.bedwars.upgrades

import com.andrei1058.bedwars.BedWars
import com.andrei1058.bedwars.BedWars.Companion.nms
import com.andrei1058.bedwars.BedWars.Companion.plugin
import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.api.language.Language
import com.andrei1058.bedwars.api.language.Messages
import com.andrei1058.bedwars.api.upgrades.MenuContent
import com.andrei1058.bedwars.api.upgrades.UpgradesIndex
import com.andrei1058.bedwars.arena.Misc
import com.andrei1058.bedwars.configuration.UpgradesConfig
import com.andrei1058.bedwars.upgrades.listeners.InventoryListener
import com.andrei1058.bedwars.upgrades.listeners.UpgradeOpenListener
import com.andrei1058.bedwars.upgrades.menu.*
import java.io.File
import java.util.HashMap
import java.util.LinkedList
import java.util.UUID
import java.util.logging.Level
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import com.andrei1058.bedwars.api.util.unchecked
import com.andrei1058.bedwars.api.util.uninitialized

object UpgradesManager {
    private val upgradeViewers: LinkedList<UUID> = LinkedList()
    //store lower case names
    private val menuContentByName: HashMap<String, MenuContent> = HashMap()
    //store lower case names
    private val menuByName: HashMap<String, UpgradesIndex> = HashMap()
    private val customMenuForArena: HashMap<IArena, UpgradesIndex> = HashMap()
    private var upgrades: UpgradesConfig = uninitialized()

    @JvmStatic
    fun init() {
        val oldFile = File(plugin.getDataFolder(), "/upgrades.yml")
        //noinspection ResultOfMethodCallIgnored
        oldFile.delete()
        upgrades = UpgradesConfig("upgrades2", plugin.getDataFolder().getPath())
        loadAll()
        BedWars.registerEvents(InventoryListener(), UpgradeOpenListener())
    }

    /**
     * Re-reads upgrades2.yml and rebuilds the team upgrade menus (the listeners stay registered). Arenas that are
     * running keep the menus they were created with.
     */
    @JvmStatic
    fun reloadConfig() {
        menuContentByName.clear()
        upgrades.reload()
        loadAll()
    }

    @JvmStatic
    private fun loadAll() {
        var name: String
        for (index in upgrades.getYml().getConfigurationSection("").getKeys(false)) {
            name = index
            if (index.startsWith("upgrade-")) {
                //if (!name.isEmpty()) {
                if (getMenuContent(name) == null && !loadUpgrade(name)) {
                    Bukkit.getLogger().log(Level.WARNING, "Could not load upgrade: " + name)
                }
                //name = index.replace("upgrade-", "");
                //}
            } else if (index.startsWith("separator-")) {
                //if (!name.isEmpty()) {
                if (getMenuContent(name) == null && !loadSeparator(name)) {
                    Bukkit.getLogger().log(Level.WARNING, "Could not load separator: " + name)
                }
                //name = index.replace("separator-", "");
                //}
            } else if (index.startsWith("category-")) {
                //if (!name.isEmpty()) {
                if (getMenuContent(name) == null && !loadCategory(name)) {
                    Bukkit.getLogger().log(Level.WARNING, "Could not load category: " + name)
                }
                //name = index.replace("category-", "");
                //}
            } else if (index.startsWith("base-trap-")) {
                //if (!name.isEmpty()) {
                if (getMenuContent(name) == null && !loadBaseTrap(name)) {
                    Bukkit.getLogger().log(Level.WARNING, "Could not base trap: " + name)
                }
                //name = index.replace("category-", "");
                //}
            } else if (index.endsWith("-upgrades-settings")) {
                name = index.replace("-upgrades-settings", "")
                if (!name.isEmpty()) {
                    if (!loadMenu(name)) {
                        Bukkit.getLogger().log(Level.WARNING, "Could not load menu: " + name)
                    }
                }
            }
        }
    }

    /**
     * @return true if has the upgrades GUI opened.
     */
    @JvmStatic
    fun isWatchingUpgrades(uuid: UUID): Boolean {
        return upgradeViewers.contains(uuid)
    }

    /**
     * Set watching upgrades GUI.
     */
    @JvmStatic
    fun setWatchingUpgrades(uuid: UUID) {
        if (!upgradeViewers.contains(uuid)) {
            upgradeViewers.add(uuid)
        }
    }

    /**
     * Remove from upgrades GUI.
     */
    @JvmStatic
    fun removeWatchingUpgrades(uuid: UUID) {
        upgradeViewers.remove(uuid)
    }

    /**
     * Load a menu for a group.
     *
     * @param groupName arena group name.
     * @return false if cannot be loaded.
     */
    @JvmStatic
    fun loadMenu(groupName: String): Boolean {
        if (!upgrades.getYml().isSet(groupName + "-upgrades-settings.menu-content")) {
            return false
        }
        if (menuByName.containsKey(groupName.lowercase())) {
            return false
        }
        val um = InternalMenu(groupName)
        for (component in upgrades.getYml().getStringList(groupName + "-upgrades-settings.menu-content")) {
            val data = component.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (data.size <= 1) {
                continue
            }
            var mc: MenuContent? = getMenuContent(data[0])
            if (data[0].startsWith("category-")) {
                if (mc == null && loadCategory(data[0])) {
                    mc = getMenuContent(data[0])
                }
            } else if (data[0].startsWith("upgrade-")) {
                if (mc == null && loadUpgrade(data[0])) {
                    mc = getMenuContent(data[0])
                }
            } else if (data[0].startsWith("trap-slot-")) {
                if (mc == null && loadTrapSlot(data[0])) {
                    mc = getMenuContent(data[0])
                }
            } else if (data[0].startsWith("separator-")) {
                if (mc == null && loadSeparator(data[0])) {
                    mc = getMenuContent(data[0])
                }
            } else if (data[0].startsWith("base-trap-")) {
                if (mc == null && loadBaseTrap(data[0])) {
                    mc = getMenuContent(data[0])
                }
            }
            if (mc == null) {
                continue
            }
            for (i in 1 until data.size) {
                if (Misc.isNumber(data[i])) {
                    um.addContent(mc, Integer.parseInt(data[i]))
                }
            }
        }
        menuByName.put(groupName.lowercase(), um)
        BedWars.debug("Registering upgrade menu: " + groupName)
        return true
    }

    /**
     * Load a category with given name from the shop file.
     *
     * @param name category name. Must start with "category-".
     * @return false if cannot be loaded.
     */
    @JvmStatic
    private fun loadCategory(name: String?): Boolean {
        if (name == null) {
            return false
        }
        if (!name.startsWith("category-")) {
            return false
        }
        if (upgrades.getYml().get(name) == null) {
            return false
        }
        if (getMenuContent(name) != null) {
            return false
        }
        val uc = MenuCategory(name, createDisplayItem(name))
        for (component in upgrades.getYml().getStringList(name + ".category-content")) {
            val data = component.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (data.size <= 1) {
                continue
            }
            var mc: MenuContent? = null
            if (data[0].startsWith("category-")) {
                mc = getMenuContent(data[0])
                if (mc == null && loadCategory(data[0])) {
                    mc = getMenuContent(data[0])
                }
            } else if (data[0].startsWith("upgrade-")) {
                mc = getMenuContent(data[0])
                if (mc == null && loadUpgrade(data[0])) {
                    mc = getMenuContent(data[0])
                }
            } else if (data[0].startsWith("trap-slot-")) {
                mc = getMenuContent(data[0])
                if (mc == null && loadTrapSlot(data[0])) {
                    mc = getMenuContent(data[0])
                }
            } else if (data[0].startsWith("separator-")) {
                mc = getMenuContent(data[0])
                if (mc == null && loadSeparator(data[0])) {
                    mc = getMenuContent(data[0])
                }
            } else if (data[0].startsWith("base-trap-")) {
                mc = getMenuContent(data[0])
                if (mc == null && loadBaseTrap(data[0])) {
                    mc = getMenuContent(data[0])
                }
            }
            if (mc == null) {
                continue
            }
            for (i in 1 until data.size) {
                if (Misc.isNumber(data[i])) {
                    uc.addContent(mc, Integer.parseInt(data[i]))
                }
            }
        }
        menuContentByName.put(name.lowercase(), uc)
        BedWars.debug("Registering upgrade: " + name)
        return true
    }

    /**
     * Load an upgrade element with given name.
     *
     * @param name upgrade name. Must start with "upgrade-".
     * @return false if can't be loaded.
     */
    @JvmStatic
    private fun loadUpgrade(name: String?): Boolean {
        if (name == null) {
            return false
        }
        if (!name.startsWith("upgrade-")) {
            return false
        }
        if (upgrades.getYml().get(name) == null) {
            return false
        }
        if (upgrades.getYml().get(name + ".tier-1") == null) {
            return false
        }
        if (getMenuContent(name) != null) {
            return false
        }
        val mu = MenuUpgrade(name)
        for (s in upgrades.getYml().getConfigurationSection(name).getKeys(false)) {
            if (!s.startsWith("tier-")) {
                continue
            }
            if (upgrades.getYml().get(name + "." + s + ".receive") == null) {
                BedWars.debug("Could not load Upgrade " + name + " tier: " + s + ". Receive not set.")
                continue
            }
            if (upgrades.getYml().get(name + "." + s + ".display-item") == null) {
                BedWars.debug("Could not load Upgrade " + name + " tier: " + s + ". Display item not set.")
                continue
            }
            if (upgrades.getYml().get(name + "." + s + ".cost") == null) {
                BedWars.debug("Could not load Upgrade " + name + " tier: " + s + ". Cost not set.")
                continue
            }
            if (upgrades.getYml().get(name + "." + s + ".currency") == null) {
                BedWars.debug("Could not load Upgrade " + name + " tier: " + s + ". Currency not set.")
                continue
            }
            val ut = UpgradeTier(name, s, createDisplayItem(name + "." + s), upgrades.getYml().getInt(name + "." + s + ".cost"), getCurrency(upgrades.getYml().getString(name + "." + s + ".currency")).unchecked())
            if (!mu.addTier(ut)) {
                Bukkit.getLogger().log(Level.WARNING, "Could not load tier: " + s + " at upgrade: " + name)
            }
        }
        BedWars.debug("Registering upgrade: " + name)
        menuContentByName.put(name.lowercase(), mu)
        return true
    }

    /**
     * Load a separator with given name.
     *
     * @param name name. Must start with "separator-".
     * @return false if cannot be loaded.
     */
    @JvmStatic
    private fun loadSeparator(name: String?): Boolean {
        if (name == null) {
            return false
        }
        if (!name.startsWith("separator-")) {
            return false
        }
        if (upgrades.getYml().get(name) == null) {
            return false
        }
        if (getMenuContent(name) != null) {
            return false
        }
        val ms = MenuSeparator(name, createDisplayItem(name))
        menuContentByName.put(name.lowercase(), ms)
        BedWars.debug("Registering upgrade: " + name)
        return true
    }

    /**
     * Load a trap slot with given name.
     *
     * @param name name. Must start with "trap-slot-".
     * @return false if cannot be loaded.
     */
    @JvmStatic
    private fun loadTrapSlot(name: String?): Boolean {
        if (name == null) {
            return false
        }
        if (!name.startsWith("trap-slot-")) {
            return false
        }
        if (upgrades.getYml().get(name) == null) {
            return false
        }
        if (getMenuContent(name) != null) {
            return false
        }
        val mts = MenuTrapSlot(name, createDisplayItem(name))
        menuContentByName.put(name.lowercase(), mts)
        BedWars.debug("Registering upgrade: " + name)
        return true
    }

    @JvmStatic
    private fun loadBaseTrap(name: String?): Boolean {
        if (name == null) {
            return false
        }
        if (!name.startsWith("base-trap-")) {
            return false
        }
        if (upgrades.getYml().get(name) == null) {
            return false
        }
        if (upgrades.getYml().get(name + ".receive") == null) {
            BedWars.debug("Could not load BaseTrap. Receive not set.")
            return false
        }
        if (upgrades.getYml().get(name + ".display-item") == null) {
            BedWars.debug("Could not load BaseTrap. Display item not set.")
            return false
        }
        val bt = MenuBaseTrap(name, createDisplayItem(name), upgrades.getYml().getInt(name + ".cost"), getCurrency(upgrades.getYml().getString(name + ".currency")).unchecked())
        BedWars.debug("Registering upgrade: " + name)
        menuContentByName.put(name.lowercase(), bt)
        return true
    }

    /**
     * Check the money amount in a given currency of a player.
     *
     * @param player   target player.
     * @param currency {@link org.bukkit.Material#AIR} is used for vault, {@link org.bukkit.Material#IRON_INGOT} for iron, {@link org.bukkit.Material#GOLD_INGOT} for gold, {@link org.bukkit.Material#DIAMOND} for diamond, {@link org.bukkit.Material#EMERALD} for emerald.
     * @return the amount of money.
     */
    @JvmStatic
    fun getMoney(player: Player, currency: Material): Int {
        if (currency == Material.AIR) {
            val amount: Double = BedWars.getEconomy().getMoney(player)
            return if (amount % 2 == 0.0) amount.toInt() else (amount - 1).toInt()
        }
        return BedWars.getAPI().getShopUtil().calculateMoney(player, currency)
    }

    /**
     * @param name the string to be converted.
     * @return NULL if not a currency. {@link org.bukkit.Material#AIR} is used for vault, {@link org.bukkit.Material#IRON_INGOT} for iron, {@link org.bukkit.Material#GOLD_INGOT} for gold, {@link org.bukkit.Material#DIAMOND} for diamond, {@link org.bukkit.Material#EMERALD} for emerald.
     */
    @JvmStatic
    fun getCurrency(name: String?): Material? {
        if (name == null || name.isEmpty()) {
            return null
        }
        return BedWars.getAPI().getShopUtil().getCurrency(name)
    }

    /**
     * Check if is upgradable item.
     * Used in inventory click.
     *
     * @param item item to be checked.
     * @retrun {@link MenuContent} NULL if isn't an element.
     */
    @JvmStatic
    fun getMenuContent(item: ItemStack?): MenuContent? {
        if (item == null) {
            return null
        }
        var identifier: String? = nms.getCustomData(item)
        if (identifier == null) {
            return null
        }
        if (identifier == "null") {
            return null
        }
        if (!identifier.startsWith("MCONT_")) {
            return null
        }
        identifier = identifier.replaceFirst("MCONT_".toRegex(), "")
        if (identifier.isEmpty()) {
            return null
        }
        return menuContentByName.getOrDefault(identifier.lowercase(), null)
    }

    /**
     * Get menu content by identifier.
     *
     * @retrun null if not found.
     */
    @JvmStatic
    fun getMenuContent(identifier: String?): MenuContent? {
        return menuContentByName.getOrDefault(identifier!!.lowercase(), null)
    }

    /**
     * Set a custom menu that will override the group menu for an arena.
     *
     * @param arena target arena.
     * @param menu  custom menu.
     */
    @JvmStatic
    fun setCustomMenuForArena(arena: IArena, menu: UpgradesIndex) {
        if (!customMenuForArena.containsKey(arena)) {
            customMenuForArena.put(arena, menu)
            BedWars.debug("Registering custom menu for arena: " + arena.getArenaName() + ". Using index: " + menu.getName())
        } else {
            BedWars.debug("Overriding custom menu for arena: " + arena.getArenaName() + ". Using index: " + menu.getName() + " Old index: " + customMenuForArena.get(arena)!!.getName())
            customMenuForArena.replace(arena, menu)
        }
    }

    /**
     * Get upgrade menu index for an arena.
     *
     * @param arena target arena.
     * @return the default menu if the arena doesn't have an arena group menu set or a custom menu by an addon.
     */
    @JvmStatic
    fun getMenuForArena(arena: IArena): UpgradesIndex {
        if (customMenuForArena.containsKey(arena)) {
            return customMenuForArena.get(arena).unchecked()
        }
        return menuByName.getOrDefault(arena.getGroup().lowercase(), menuByName.get("default")).unchecked()
    }

    /**
     * Create a display item.
     *
     * @param path config path.
     * @return bedrock if given material is null
     */
    @JvmStatic
    private fun createDisplayItem(path: String): ItemStack {
        var m: Material
        try {
            m = Material.valueOf(upgrades.getYml().getString(path + ".display-item.material"))
        } catch (e: Exception) {
            m = Material.BEDROCK
        }
        val i = ItemStack(m, Integer.parseInt(upgrades.getYml().getString(path + ".display-item.amount")), upgrades.getYml().getInt(path + ".display-item.data").toShort())
        if (upgrades.getYml().getBoolean(path + ".display-item.enchanted")) {
            i.addUnsafeEnchantment(Enchantment.DURABILITY, 1)
            val im: ItemMeta? = i.getItemMeta()
            if (im != null) {
                im.addItemFlags(ItemFlag.HIDE_ENCHANTS)
                i.setItemMeta(im)
            }
        }
        return i
    }

    /**
     * Get currency msg.
     */
    @JvmStatic
    fun getCurrencyMsg(p: Player, ut: UpgradeTier): String {
        var c = ""
        when (ut.getCurrency()) {
            Material.IRON_INGOT -> {
                c = if (ut.getCost() == 1) Messages.MEANING_IRON_SINGULAR else Messages.MEANING_IRON_PLURAL
            }
            Material.GOLD_INGOT -> {
                c = if (ut.getCost() == 1) Messages.MEANING_GOLD_SINGULAR else Messages.MEANING_GOLD_PLURAL
            }
            Material.EMERALD -> {
                c = if (ut.getCost() == 1) Messages.MEANING_EMERALD_SINGULAR else Messages.MEANING_EMERALD_PLURAL
            }
            Material.DIAMOND -> {
                c = if (ut.getCost() == 1) Messages.MEANING_DIAMOND_SINGULAR else Messages.MEANING_DIAMOND_PLURAL
            }
            Material.AIR -> {
                c = if (ut.getCost() == 1) Messages.MEANING_VAULT_SINGULAR else Messages.MEANING_VAULT_PLURAL
            }
            else -> {}
        }
        return Language.getMsg(p, c)
    }

    @JvmStatic
    fun getCurrencyMsg(p: Player, money: Int, currency: String?): String {
        var c: String
        if (currency == null) {
            return Language.getMsg(p, if (money == 1) Messages.MEANING_VAULT_SINGULAR else Messages.MEANING_VAULT_PLURAL)
        }
        when (currency.lowercase()) {
            "iron" -> {
                c = if (money == 1) Messages.MEANING_IRON_SINGULAR else Messages.MEANING_IRON_PLURAL
            }
            "gold" -> {
                c = if (money == 1) Messages.MEANING_GOLD_SINGULAR else Messages.MEANING_GOLD_PLURAL
            }
            "emerald" -> {
                c = if (money == 1) Messages.MEANING_EMERALD_SINGULAR else Messages.MEANING_EMERALD_PLURAL
            }
            "diamond" -> {
                c = if (money == 1) Messages.MEANING_DIAMOND_SINGULAR else Messages.MEANING_DIAMOND_PLURAL
            }
            else -> {
                c = if (money == 1) Messages.MEANING_VAULT_SINGULAR else Messages.MEANING_VAULT_PLURAL
            }
        }
        return Language.getMsg(p, c)
    }

    @JvmStatic
    fun getCurrencyMsg(p: Player, money: Int, currency: Material?): String {
        var c: String
        when (currency) {
            Material.IRON_INGOT -> {
                c = if (money == 1) Messages.MEANING_IRON_SINGULAR else Messages.MEANING_IRON_PLURAL
            }
            Material.GOLD_INGOT -> {
                c = if (money == 1) Messages.MEANING_GOLD_SINGULAR else Messages.MEANING_GOLD_PLURAL
            }
            Material.EMERALD -> {
                c = if (money == 1) Messages.MEANING_EMERALD_SINGULAR else Messages.MEANING_EMERALD_PLURAL
            }
            Material.DIAMOND -> {
                c = if (money == 1) Messages.MEANING_DIAMOND_SINGULAR else Messages.MEANING_DIAMOND_PLURAL
            }
            else -> {
                c = if (money == 1) Messages.MEANING_VAULT_SINGULAR else Messages.MEANING_VAULT_PLURAL
            }
        }
        return Language.getMsg(p, c)
    }

    @JvmStatic
    fun getCurrencyColor(currency: Material): ChatColor {
        when (currency) {
            Material.DIAMOND -> {
                return ChatColor.AQUA
            }
            Material.GOLD_INGOT -> {
                return ChatColor.GOLD
            }
            Material.IRON_INGOT -> {
                return ChatColor.WHITE
            }
            Material.EMERALD -> {
                return ChatColor.GREEN
            }
            else -> {
                return ChatColor.DARK_GREEN
            }
        }
    }

    @JvmStatic
    fun getConfiguration(): UpgradesConfig {
        return upgrades
    }
}
