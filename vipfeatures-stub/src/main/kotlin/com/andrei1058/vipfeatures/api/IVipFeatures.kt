package com.andrei1058.vipfeatures.api

import org.bukkit.entity.Player

interface IVipFeatures {
    @Throws(MiniGameAlreadyRegistered::class)
    fun registerMiniGame(miniGame: MiniGame)

    fun givePlayerItemStack(player: Player)
}
