package com.andrei1058.bedwars.features

import com.andrei1058.bedwars.features.maploader.WorldLoaderPlugin
import com.andrei1058.bedwars.features.multigroup.MultiGroupPlugin

/** The features that ship inside BedWars1058. The order is the start order. */
internal object BuiltInFeatures {
    fun registerAll() {
        // Map handling first: MapLoader takes over world loading, MultiGroup wraps whatever loads worlds.
        FeatureManager.register(WorldLoaderPlugin())
        FeatureManager.register(MultiGroupPlugin())
    }
}
