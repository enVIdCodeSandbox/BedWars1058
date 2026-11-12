package com.andrei1058.bedwars.features.generalfixes

import com.andrei1058.bedwars.features.generalfixes.chests.ChestVisibilityFix
import com.andrei1058.bedwars.features.generalfixes.cobwebs.HalloweenCobwebFix
import com.andrei1058.bedwars.features.generalfixes.common.Fix
import com.andrei1058.bedwars.features.generalfixes.common.FixPlugin
import com.andrei1058.bedwars.features.generalfixes.oldladders.OldLadderFix
import com.andrei1058.bedwars.features.generalfixes.shopnpcs.ShopNpcFix
import com.andrei1058.bedwars.features.generalfixes.swords.SwordReplacementFix
import com.andrei1058.bedwars.features.generalfixes.tower.PopUpTowerLanguageFix
import com.andrei1058.bedwars.features.generalfixes.winfix.WinFix

/** All seven fixes in one jar. Each one is switched on/off in config.yml under `fixes.<name>.enabled`. */
class GeneralFixesPlugin : FixPlugin(
    "generalfixes", "GeneralFixes",
    listOf("BedWars1058-GeneralFixes", "BedWars1058-NoDeathCobwebs", "BedWars1058-OldLadderFix", "BedWars1058-SwordReplacementFix", "BedWars1058-ChestVisibilityFix", "BedWars1058-PopUpTowerLanguageFix", "BedWars1058-ShopNpcFix", "BedWars1058-WinFix"),
    listOf("bwfixes")
) {
    override val adminPermission = "bwfixes.admin"

    override fun createFixes(): List<Fix> = listOf(
        HalloweenCobwebFix(this, "fixes.halloween-cobwebs"),
        ChestVisibilityFix(this, "fixes.invisible-chests"),
        SwordReplacementFix(this, "fixes.sword-replacement"),
        PopUpTowerLanguageFix(this, "fixes.pop-up-tower-language"),
        ShopNpcFix(this, "fixes.shop-npcs"),
        OldLadderFix(this, "fixes.old-ladders"),
        WinFix(this, "fixes.end-game-sidebar")
    )
}
