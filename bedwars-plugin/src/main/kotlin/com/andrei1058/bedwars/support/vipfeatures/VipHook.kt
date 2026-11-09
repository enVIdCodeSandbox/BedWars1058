package com.andrei1058.bedwars.support.vipfeatures

import com.andrei1058.bedwars.BedWars
import com.andrei1058.vipfeatures.api.IVipFeatures
import org.bukkit.Bukkit

/**
 * Only ever loaded when the VipFeatures plugin is present: referencing its API classes from
 * {@link BedWars} directly would make the main class fail to load on servers without it.
 */
object VipHook {
    @Throws(Exception::class)
    @JvmStatic
    fun register(plugin: BedWars) {
        val vf = Bukkit.getServicesManager().getRegistration(IVipFeatures::class.java).getProvider()
        vf.registerMiniGame(VipFeatures(plugin))
        BedWars.registerEvents(VipListeners(vf))
    }
}
