package com.andrei1058.bedwars.features.fastforward

import com.andrei1058.bedwars.arena.Arena
import com.andrei1058.bedwars.arena.OreGenerator
import com.andrei1058.bedwars.arena.tasks.GamePlayingTask

/**
 * The countdowns and generator tiers FastForward changes live in the game's implementation classes
 * (Arena, GamePlayingTask, OreGenerator), not in the API. Everything that needs them goes through here.
 */
internal object Internals {

    private fun arena(target: Any): Arena =
        target as? Arena ?: throw IllegalStateException("${target.javaClass.simpleName} is not a BedWars1058 arena.")

    private fun task(target: Any): GamePlayingTask =
        target as? GamePlayingTask ?: throw IllegalStateException("${target.javaClass.simpleName} is not the BedWars1058 playing task.")

    private fun generator(target: Any): OreGenerator =
        target as? OreGenerator ?: throw IllegalStateException("${target.javaClass.simpleName} is not a BedWars1058 generator.")

    private fun unknown(name: String): Nothing = throw IllegalStateException("Unknown value '$name'. Run /bwff debug and report it.")

    fun getInt(target: Any, name: String): Int = when (name) {
        "upgradeDiamondsCount" -> arena(target).upgradeDiamondsCount
        "upgradeEmeraldsCount" -> arena(target).upgradeEmeraldsCount
        "diamondTier" -> arena(target).getDiamondTier()
        "emeraldTier" -> arena(target).getEmeraldTier()
        "beds_destroy_countdown" -> task(target).getBedsDestroyCountdown()
        "dragon_spawn_countdown" -> task(target).getDragonSpawnCountdown()
        "game_end_countdown" -> task(target).getGameEndCountdown()
        "upgradeStage" -> generator(target).getUpgradeStage()
        else -> unknown(name)
    }

    fun setInt(target: Any, name: String, value: Int) {
        when (name) {
            "upgradeDiamondsCount" -> arena(target).upgradeDiamondsCount = value
            "upgradeEmeraldsCount" -> arena(target).upgradeEmeraldsCount = value
            "diamondTier" -> arena(target).setDiamondTier(value)
            "emeraldTier" -> arena(target).setEmeraldTier(value)
            "beds_destroy_countdown" -> task(target).setBedsDestroyCountdown(value)
            "dragon_spawn_countdown" -> task(target).setDragonSpawnCountdown(value)
            "game_end_countdown" -> task(target).setGameEndCountdown(value)
            "upgradeStage" -> generator(target).setUpgradeStage(value)
            else -> unknown(name)
        }
    }

    fun has(target: Any, name: String): Boolean =
        try {
            getInt(target, name)
            true
        } catch (e: IllegalStateException) {
            false
        }

    /** Current middle-generator tiers (1-3). */
    fun diamondTier(arena: Any): Int = getInt(arena, "diamondTier")

    fun emeraldTier(arena: Any): Int = getInt(arena, "emeraldTier")
}
