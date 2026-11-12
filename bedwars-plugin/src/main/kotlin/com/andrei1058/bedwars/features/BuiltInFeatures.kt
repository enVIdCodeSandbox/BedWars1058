package com.andrei1058.bedwars.features

import com.andrei1058.bedwars.features.arenamanagement.ArenaManagementPlugin
import com.andrei1058.bedwars.features.dragonfix.DragonFixPlugin
import com.andrei1058.bedwars.features.easysetup.EasySetupPlugin
import com.andrei1058.bedwars.features.generalfixes.GeneralFixesPlugin
import com.andrei1058.bedwars.features.generatorfix.GeneratorFixPlugin
import com.andrei1058.bedwars.features.maploader.WorldLoaderPlugin
import com.andrei1058.bedwars.features.multigroup.MultiGroupPlugin

/** The features that ship inside BedWars1058. The order is the start order. */
internal object BuiltInFeatures {
    fun registerAll() {
        // Map handling first: MapLoader takes over world loading, MultiGroup wraps whatever loads worlds.
        FeatureManager.register(WorldLoaderPlugin())
        FeatureManager.register(MultiGroupPlugin())
        // Arena tooling (uses the two above when they are on).
        FeatureManager.register(ArenaManagementPlugin())
        FeatureManager.register(EasySetupPlugin())
        // Gameplay.
        FeatureManager.register(GeneralFixesPlugin())
        FeatureManager.register(GeneratorFixPlugin())
        FeatureManager.register(DragonFixPlugin())
    }
}
