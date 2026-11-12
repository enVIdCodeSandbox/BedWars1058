package com.andrei1058.bedwars.popuptower

import com.andrei1058.bedwars.BedWars
import com.andrei1058.bedwars.api.arena.team.TeamColor
import com.andrei1058.bedwars.configuration.Sounds
import java.util.ArrayList
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitTask

open class TowerNorth(loc: Location, chest: Block, color: TeamColor, p: Player) {
    private var task: BukkitTask

    init {
        val itemInHand = p.getInventory().getItemInHand()
        if (itemInHand.getAmount() > 1) {
            itemInHand.setAmount(itemInHand.getAmount() - 1)
        } else {
            p.getInventory().setItemInHand(null)
        }
        val relloc: MutableList<String> = ArrayList()
        relloc.add("-1, 0, -2")
        relloc.add("-2, 0, -1")
        relloc.add("-2, 0, 0")
        relloc.add("-1, 0, 1")
        relloc.add("0, 0, 1")
        relloc.add("1, 0, 1")
        relloc.add("2, 0, 0")
        relloc.add("2, 0, -1")
        relloc.add("1, 0, -2")
        relloc.add("0, 0, 0, ladder2")
        relloc.add("-1, 1, -2")
        relloc.add("-2, 1, -1")
        relloc.add("-2, 1, 0")
        relloc.add("-1, 1, 1")
        relloc.add("0, 1, 1")
        relloc.add("1, 1, 1")
        relloc.add("2, 1, 0")
        relloc.add("2, 1, -1")
        relloc.add("1, 1, -2")
        relloc.add("0, 1, 0, ladder2")
        relloc.add("-1, 2, -2")
        relloc.add("-2, 2, -1")
        relloc.add("-2, 2, 0")
        relloc.add("-1, 2, 1")
        relloc.add("0, 2, 1")
        relloc.add("1, 2, 1")
        relloc.add("2, 2, 0")
        relloc.add("2, 2, -1")
        relloc.add("1, 2, -2")
        relloc.add("0, 2, 0, ladder2")
        relloc.add("0, 3, -2")
        relloc.add("-1, 3, -2")
        relloc.add("-2, 3, -1")
        relloc.add("-2, 3, 0")
        relloc.add("-1, 3, 1")
        relloc.add("0, 3, 1")
        relloc.add("1, 3, 1")
        relloc.add("2, 3, 0")
        relloc.add("2, 3, -1")
        relloc.add("1, 3, -2")
        relloc.add("0, 3, 0, ladder2")
        relloc.add("0, 4, -2")
        relloc.add("-1, 4, -2")
        relloc.add("-2, 4, -1")
        relloc.add("-2, 4, 0")
        relloc.add("-1, 4, 1")
        relloc.add("0, 4, 1")
        relloc.add("1, 4, 1")
        relloc.add("2, 4, 0")
        relloc.add("2, 4, -1")
        relloc.add("1, 4, -2")
        relloc.add("0, 4, 0, ladder2")
        relloc.add("-2, 5, 1")
        relloc.add("-2, 5, 0")
        relloc.add("-2, 5, -1")
        relloc.add("-2, 5, -2")
        relloc.add("-1, 5, 1")
        relloc.add("-1, 5, 0")
        relloc.add("-1, 5, -1")
        relloc.add("-1, 5, -2")
        relloc.add("0, 5, 1")
        relloc.add("0, 5, -1")
        relloc.add("0, 5, -2")
        relloc.add("1, 5, 1")
        relloc.add("0, 5, 0, ladder2")
        relloc.add("1, 5, 0")
        relloc.add("1, 5, -1")
        relloc.add("1, 5, -2")
        relloc.add("2, 5, 1")
        relloc.add("2, 5, 0")
        relloc.add("2, 5, -1")
        relloc.add("2, 5, -2")
        relloc.add("-3, 5, -2")
        relloc.add("-3, 6, -2")
        relloc.add("-3, 7, -2")
        relloc.add("-3, 6, -1")
        relloc.add("-3, 6, 0")
        relloc.add("-3, 5, 1")
        relloc.add("-3, 6, 1")
        relloc.add("-3, 7, 1")
        relloc.add("-2, 5, 2")
        relloc.add("-2, 6, 2")
        relloc.add("-2, 7, 2")
        relloc.add("-1, 6, 2")
        relloc.add("0, 5, 2")
        relloc.add("0, 6, 2")
        relloc.add("0, 7, 2")
        relloc.add("1, 6, 2")
        relloc.add("2, 5, 2")
        relloc.add("2, 6, 2")
        relloc.add("2, 7, 2")
        relloc.add("3, 5, -2")
        relloc.add("3, 6, -2")
        relloc.add("3, 7, -2")
        relloc.add("3, 6, -1")
        relloc.add("3, 6, 0")
        relloc.add("3, 5, 1")
        relloc.add("3, 6, 1")
        relloc.add("3, 7, 1")
        relloc.add("-2, 5, -3")
        relloc.add("-2, 6, -3")
        relloc.add("-2, 7, -3")
        relloc.add("-1, 6, -3")
        relloc.add("0, 5, -3")
        relloc.add("0, 6, -3")
        relloc.add("0, 7, -3")
        relloc.add("1, 6, -3")
        relloc.add("2, 5, -3")
        relloc.add("2, 6, -3")
        relloc.add("2, 7, -3")
        val i = intArrayOf(0)
        this.task = Bukkit.getScheduler().runTaskTimer(BedWars.plugin, Runnable {
            Sounds.playsoundArea("pop-up-tower-build", loc, 1.0f, 0.5f)
            if (relloc.size + 1 == i[0] + 1) {
                this.task.cancel()
            } else {
                val c1 = relloc.get(i[0])
                if (c1.contains("ladder")) {
                    val ldata: Int = Integer.parseInt(c1.split("ladder".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1])
                    NewPlaceBlock(chest, c1, color, p, true, ldata)
                } else {
                    NewPlaceBlock(chest, c1, color, p, false, 0)
                }
                if (relloc.size + 1 == i[0] + 2) {
                    this.task.cancel()
                } else {
                    val c2 = relloc.get(i[0] + 1)
                    if (c2.contains("ladder")) {
                        val ldatax: Int = Integer.parseInt(c2.split("ladder".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1])
                        NewPlaceBlock(chest, c2, color, p, true, ldatax)
                    } else {
                        NewPlaceBlock(chest, c2, color, p, false, 0)
                    }
                    i[0] += 2
                }
            }
        }, 0L, 1L)
    }
}
