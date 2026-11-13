package com.andrei1058.bedwars.support

import com.andrei1058.bedwars.BedWars
import java.lang.reflect.Method
import java.util.HashMap
import java.util.HashSet
import java.util.Locale
import org.bukkit.World

/**
 * Sets game rules by their classic names ("doDaylightCycle", ...) on every server version.
 * <p>
 * Minecraft 26.1 renamed all game rules ("advance_time", "spawn_mobs", ...) and {@link World#setGameRuleValue}
 * silently returns false for the old names, so a map would keep its day/night cycle, weather, fire spread... This
 * class first tries the classic call and falls back to the new rules through reflection (the plugin is compiled
 * against an old Bukkit API that has no {@code GameRule} class).
 */
object GameRuleCompat {
    private val ALIASES: MutableMap<String, String> = HashMap()
    private val WARNED: MutableSet<String> = HashSet()

    init {
        ALIASES.put("announceadvancements", "show_advancement_messages")
        ALIASES.put("dodaylightcycle", "advance_time")
        ALIASES.put("doweathercycle", "advance_weather")
        ALIASES.put("doinsomnia", "spawn_phantoms")
        ALIASES.put("doimmediaterespawn", "immediate_respawn")
        ALIASES.put("dofiretick", "fire_spread_radius_around_player")
        ALIASES.put("domobspawning", "spawn_mobs")
        ALIASES.put("domobloot", "mob_drops")
        ALIASES.put("dotiledrops", "block_drops")
        ALIASES.put("doentitydrops", "entity_drops")
        ALIASES.put("naturalregeneration", "natural_health_regeneration")
        ALIASES.put("dopatrolspawning", "spawn_patrols")
        ALIASES.put("dotraderspawning", "spawn_wandering_traders")
        ALIASES.put("dowardenspawning", "spawn_wardens")
        ALIASES.put("dovinesspread", "spread_vines")
        ALIASES.put("dolimitedcrafting", "limited_crafting")
        ALIASES.put("disableraids", "raids")
        ALIASES.put("commandblocksenabled", "command_blocks_work")
        ALIASES.put("spawnerblocksenabled", "spawner_blocks_work")
        ALIASES.put("spawnradius", "respawn_radius")
    }

    /**
     * @param legacyName a classic rule name such as {@code doDaylightCycle} (modern names work too)
     * @param value      "true", "false" or a number
     * @return true if the rule was applied
     */
    @JvmStatic
    fun set(world: World, legacyName: String, value: String): Boolean {
        try {
            if (world.setGameRuleValue(legacyName, value)) {
                return true
            }
        } catch (ignored: Throwable) {
            // fall through to the modern API
        }
        try {
            return setModern(world, legacyName, value)
        } catch (t: Throwable) {
            warnOnce(legacyName, t.javaClass.getSimpleName() + ": " + t.message)
            return false
        }
    }

    @Throws(Exception::class)
    @JvmStatic
    private fun setModern(world: World, legacyName: String, value: String): Boolean {
        val ruleClass = Class.forName("org.bukkit.GameRule")
        val lower = legacyName.lowercase(Locale.ROOT)
        var modern: String? = ALIASES.get(lower)
        if (modern == null) {
            modern = snake(legacyName)
        }
        var rule: Any? = find(ruleClass, modern)
        if (rule == null && modern != snake(legacyName)) {
            rule = find(ruleClass, snake(legacyName))
        }
        if (rule == null) {
            warnOnce(legacyName, "no such game rule on this server")
            return false
        }
        val type = ruleClass.getMethod("getType").invoke(rule) as Class<*>
        val flag: Boolean = value.equals("true", ignoreCase = true)
        var parsed: Any
        if (type === Boolean::class.javaObjectType) {
            // classic rule "disableRaids"-style inversions
            parsed = if (lower == "disableraids") !flag else flag
        } else if (type === Int::class.javaObjectType) {
            if (value.equals("true", ignoreCase = true) || value.equals("false", ignoreCase = true)) {
                // doFireTick -> fire_spread_radius_around_player: off means no spread at all
                parsed = if (flag) ruleClass.getMethod("getDefaultValue").invoke(rule) else Integer.valueOf(0)
            } else {
                parsed = Integer.valueOf(value)
            }
        } else {
            return false
        }
        val set = World::class.java.getMethod("setGameRule", ruleClass, Any::class.java)
        return set.invoke(world, rule, parsed) as Boolean
    }

    @Throws(Exception::class)
    @JvmStatic
    private fun find(ruleClass: Class<*>, name: String): Any? {
        val all = ruleClass.getMethod("values").invoke(null) as Array<Any>
        for (r in all) {
            val n = ruleClass.getMethod("getName").invoke(r) as String
            if (n.equals(name, ignoreCase = true) || n.equals("minecraft:" + name, ignoreCase = true)) {
                return r
            }
        }
        return null
    }

    @JvmStatic
    private fun snake(camel: String): String {
        val sb = StringBuilder()
        for (c in camel.toCharArray()) {
            if (Character.isUpperCase(c)) {
                sb.append('_').append(Character.toLowerCase(c))
            } else {
                sb.append(c)
            }
        }
        return sb.toString()
    }

    @JvmStatic
    private fun warnOnce(rule: String, why: String) {
        if (WARNED.add(rule)) {
            BedWars.plugin.getLogger().warning("Could not set the game rule '" + rule + "': " + why)
        }
    }
}
