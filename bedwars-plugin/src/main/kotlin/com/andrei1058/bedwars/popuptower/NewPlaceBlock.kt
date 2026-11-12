package com.andrei1058.bedwars.popuptower

import com.andrei1058.bedwars.BedWars
import com.andrei1058.bedwars.api.arena.team.TeamColor
import com.andrei1058.bedwars.api.region.Region
import com.andrei1058.bedwars.arena.Arena
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Player

open class NewPlaceBlock {
    constructor(b: Block, xyz: String, color: TeamColor, p: Player, ladder: Boolean, ladderdata: Int) {
        val x: Int = Integer.parseInt(xyz.split(", ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0])
        val y: Int = Integer.parseInt(xyz.split(", ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1])
        val z: Int = Integer.parseInt(xyz.split(", ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[2])
        if (b.getRelative(x, y, z).getType() == Material.AIR) {
            for (r in Arena.getArenaByPlayer(p).getRegionsList()!!) {
                if (r.isInRegion(b.getRelative(x, y, z).getLocation())) {
                    return
                }
            }
            if (!ladder) {
                BedWars.nms.placeTowerBlocks(b, Arena.getArenaByPlayer(p), color, x, y, z)
            } else {
                BedWars.nms.placeLadder(b, x, y, z, Arena.getArenaByPlayer(p), ladderdata)
            }
        }
    }
}
