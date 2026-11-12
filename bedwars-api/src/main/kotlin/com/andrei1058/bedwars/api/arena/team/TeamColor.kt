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

package com.andrei1058.bedwars.api.arena.team

import org.bukkit.ChatColor
import org.bukkit.Color
import org.bukkit.DyeColor
import org.bukkit.Material

enum class TeamColor {
    RED,
    BLUE,
    GREEN,
    YELLOW,
    AQUA,
    WHITE,
    PINK,
    GRAY,
    DARK_GREEN,
    DARK_GRAY;

    /**
     * @return Chat color for this team color.
     */
    fun chat(): ChatColor {
        var color: ChatColor
        val teamColor = TeamColor.valueOf(this.toString())
        if (teamColor == TeamColor.PINK) {
            color = ChatColor.LIGHT_PURPLE
        } else {
            color = ChatColor.valueOf(teamColor.toString())
        }
        return color
    }

    /**
     * @return DyeColor color for this team color.
     */
    fun dye(): DyeColor {
        var color: DyeColor
        when (this) {
            TeamColor.GREEN -> {
                color = DyeColor.LIME
            }
            TeamColor.AQUA -> {
                color = DyeColor.LIGHT_BLUE
            }
            TeamColor.DARK_GREEN -> {
                color = DyeColor.GREEN
            }
            TeamColor.DARK_GRAY -> {
                color = DyeColor.GRAY
            }
            else -> {
                color = DyeColor.valueOf(this.toString())
            }
        }
        return color
    }

    /**
     * @return byte color for MC versions until 1.12 included
     */
    fun itemByte(): Byte {
        // 0 white
        var i: Int = 0
        when (this) {
            TeamColor.WHITE -> {
            }
            TeamColor.PINK -> {
                i = 6
            }
            TeamColor.RED -> {
                i = 14
            }
            TeamColor.AQUA -> {
                i = 9
            }
            TeamColor.GREEN -> {
                i = 5
            }
            TeamColor.DARK_GREEN -> {
                i = 13
            }
            TeamColor.YELLOW -> {
                i = 4
            }
            TeamColor.BLUE -> {
                i = 11
            }
            TeamColor.GRAY -> {
                i = 8
            }
            TeamColor.DARK_GRAY -> {
                i = 7
            }
            else -> {}
        }
        return i.toByte()
    }

    /**
     * This is usually used for leather armor.
     *
     * @return Equivalent color. Returns WHITE if given color has no equivalent.
     */
    fun bukkitColor(): Color {
        var color = Color.WHITE
        when (this) {
            TeamColor.PINK -> {
                color = Color.FUCHSIA
            }
            TeamColor.GRAY -> {
                color = Color.GRAY
            }
            TeamColor.BLUE -> {
                color = Color.BLUE
            }
            TeamColor.WHITE -> {
            }
            TeamColor.DARK_GREEN -> {
                color = Color.GREEN
            }
            TeamColor.AQUA -> {
                color = Color.AQUA
            }
            TeamColor.RED -> {
                color = Color.RED
            }
            TeamColor.GREEN -> {
                color = Color.LIME
            }
            TeamColor.YELLOW -> {
                color = Color.YELLOW
            }
            TeamColor.DARK_GRAY -> {
                color = Color.fromBGR(74, 74, 74)
            }
            else -> {}
        }
        return color
    }

    /**
     * Get bed with color. Used for 1.13+.
     *
     * @return 1.13+ bed material. Return RED_BED if not found.
     */
    fun bedMaterial(): Material {
        var color = "RED_BED"
        when (this) {
            TeamColor.PINK -> {
                color = "PINK_BED"
            }
            TeamColor.GRAY -> {
                color = "LIGHT_GRAY_BED"
            }
            TeamColor.BLUE -> {
                color = "BLUE_BED"
            }
            TeamColor.WHITE -> {
                color = "WHITE_BED"
            }
            TeamColor.DARK_GREEN -> {
                color = "GREEN_BED"
            }
            TeamColor.AQUA -> {
                color = "LIGHT_BLUE_BED"
            }
            TeamColor.GREEN -> {
                color = "LIME_BED"
            }
            TeamColor.YELLOW -> {
                color = "YELLOW_BED"
            }
            TeamColor.DARK_GRAY -> {
                color = "GRAY_BED"
            }
            else -> {}
        }
        return Material.valueOf(color)
    }

    /**
     * Get glass with team color. Used for 1.13+ team glass.
     *
     * @return 1.13+ glass material. Return GLASS if this team does not have a custom glass.
     */
    fun glassMaterial(): Material {
        var color = "GLASS"
        when (this) {
            TeamColor.PINK -> {
                color = "PINK_STAINED_GLASS"
            }
            TeamColor.GRAY -> {
                color = "LIGHT_GRAY_STAINED_GLASS"
            }
            TeamColor.BLUE -> {
                color = "BLUE_STAINED_GLASS"
            }
            TeamColor.WHITE -> {
                color = "WHITE_STAINED_GLASS"
            }
            TeamColor.DARK_GREEN -> {
                color = "GREEN_STAINED_GLASS"
            }
            TeamColor.AQUA -> {
                color = "LIGHT_BLUE_STAINED_GLASS"
            }
            TeamColor.GREEN -> {
                color = "LIME_STAINED_GLASS"
            }
            TeamColor.YELLOW -> {
                color = "YELLOW_STAINED_GLASS"
            }
            TeamColor.RED -> {
                color = "RED_STAINED_GLASS"
            }
            TeamColor.DARK_GRAY -> {
                color = "GRAY_STAINED_GLASS"
            }
            else -> {}
        }
        return Material.valueOf(color)
    }

    /**
     * Retrieve glass pane with team color.
     *
     * @return glass pane material for 1.13+.
     */
    fun glassPaneMaterial(): Material {
        var color = "GLASS"
        when (this) {
            TeamColor.PINK -> {
                color = "PINK_STAINED_GLASS_PANE"
            }
            TeamColor.GRAY -> {
                color = "LIGHT_GRAY_STAINED_GLASS_PANE"
            }
            TeamColor.BLUE -> {
                color = "BLUE_STAINED_GLASS_PANE"
            }
            TeamColor.WHITE -> {
                color = "WHITE_STAINED_GLASS_PANE"
            }
            TeamColor.DARK_GREEN -> {
                color = "GREEN_STAINED_GLASS_PANE"
            }
            TeamColor.AQUA -> {
                color = "LIGHT_BLUE_STAINED_GLASS_PANE"
            }
            TeamColor.GREEN -> {
                color = "LIME_STAINED_GLASS_PANE"
            }
            TeamColor.YELLOW -> {
                color = "YELLOW_STAINED_GLASS_PANE"
            }
            TeamColor.RED -> {
                color = "RED_STAINED_GLASS_PANE"
            }
            TeamColor.DARK_GRAY -> {
                color = "GRAY_STAINED_PANE"
            }
            else -> {}
        }
        return Material.valueOf(color)
    }

    /**
     * Get glazed terracotta with team color.
     *
     * @return 1.13+ material.
     */
    fun glazedTerracottaMaterial(): Material {
        var color = "ORANGE_TERRACOTTA"
        when (this) {
            TeamColor.PINK -> {
                color = "PINK_TERRACOTTA"
            }
            TeamColor.GRAY -> {
                color = "LIGHT_GRAY_TERRACOTTA"
            }
            TeamColor.DARK_GRAY -> {
                color = "GRAY_TERRACOTTA"
            }
            TeamColor.BLUE -> {
                color = "BLUE_TERRACOTTA"
            }
            TeamColor.WHITE -> {
                color = "WHITE_TERRACOTTA"
            }
            TeamColor.DARK_GREEN -> {
                color = "GREEN_TERRACOTTA"
            }
            TeamColor.AQUA -> {
                color = "LIGHT_BLUE_TERRACOTTA"
            }
            TeamColor.GREEN -> {
                color = "LIME_TERRACOTTA"
            }
            TeamColor.YELLOW -> {
                color = "YELLOW_TERRACOTTA"
            }
            TeamColor.RED -> {
                color = "RED_TERRACOTTA"
            }
            else -> {}
        }
        return Material.valueOf(color)
    }

    /**
     * Get wool with team color.
     *
     * @return 1.13+ material.
     */
    fun woolMaterial(): Material {
        var color = "WHITE_WOOL"
        when (this) {
            TeamColor.PINK -> {
                color = "PINK_WOOL"
            }
            TeamColor.GRAY -> {
                color = "LIGHT_GRAY_WOOL"
            }
            TeamColor.DARK_GRAY -> {
                color = "GRAY_WOOL"
            }
            TeamColor.BLUE -> {
                color = "BLUE_WOOL"
            }
            TeamColor.WHITE -> {
                color = "WHITE_WOOL"
            }
            TeamColor.DARK_GREEN -> {
                color = "GREEN_WOOL"
            }
            TeamColor.AQUA -> {
                color = "LIGHT_BLUE_WOOL"
            }
            TeamColor.GREEN -> {
                color = "LIME_WOOL"
            }
            TeamColor.YELLOW -> {
                color = "YELLOW_WOOL"
            }
            TeamColor.RED -> {
                color = "RED_WOOL"
            }
            else -> {}
        }
        return Material.valueOf(color)
    }

    companion object {
        /**
         * Get chat color by team color.
         *
         * @param tColor - {@link TeamColor} string.
         * @return {@link ChatColor} for given team.
         */
        @JvmStatic
        fun getChatColor(tColor: String): ChatColor {
            var color: ChatColor
            val teamColor = TeamColor.valueOf(tColor.uppercase())
            if (teamColor == TeamColor.PINK) {
                color = ChatColor.LIGHT_PURPLE
            } else {
                color = ChatColor.valueOf(teamColor.toString())
            }
            return color
        }

        /**
         * @param teamColor team color.
         * @return Chat color for this team color.
         */
        @Deprecated("")
        @JvmStatic
        fun getChatColor(teamColor: TeamColor): ChatColor {
            var color: ChatColor
            if (teamColor == TeamColor.PINK) {
                color = ChatColor.LIGHT_PURPLE
            } else {
                color = ChatColor.valueOf(teamColor.toString())
            }
            return color
        }

        /**
         * @param teamColor team color.
         * @return DyeColor color for this team color.
         */
        @Deprecated("")
        @JvmStatic
        fun getDyeColor(teamColor: String): DyeColor {
            var color: DyeColor
            when (TeamColor.valueOf(teamColor.uppercase())) {
                TeamColor.GREEN -> {
                    color = DyeColor.LIME
                }
                TeamColor.AQUA -> {
                    color = DyeColor.LIGHT_BLUE
                }
                TeamColor.DARK_GREEN -> {
                    color = DyeColor.GREEN
                }
                TeamColor.DARK_GRAY -> {
                    color = DyeColor.GRAY
                }
                else -> {
                    color = DyeColor.valueOf(teamColor.uppercase())
                }
            }
            return color
        }

        /**
         * @param teamColor team color.
         * @return byte color for MC versions until 1.12 included
         */
        @Deprecated("")
        @JvmStatic
        fun itemColor(teamColor: TeamColor): Byte {
            // 0 white
            var i: Int = 0
            when (teamColor) {
                TeamColor.WHITE -> {
                }
                TeamColor.PINK -> {
                    i = 6
                }
                TeamColor.RED -> {
                    i = 14
                }
                TeamColor.AQUA -> {
                    i = 9
                }
                TeamColor.GREEN -> {
                    i = 5
                }
                TeamColor.DARK_GREEN -> {
                    i = 13
                }
                TeamColor.YELLOW -> {
                    i = 4
                }
                TeamColor.BLUE -> {
                    i = 11
                }
                TeamColor.GRAY -> {
                    i = 8
                }
                TeamColor.DARK_GRAY -> {
                    i = 7
                }
                else -> {}
            }
            return i.toByte()
        }

        /**
         * Get the english for material as color name.
         *
         * @param material material string.
         * @return the english color name for given material. EMPTY if item is not supported.
         */
        @JvmStatic
        fun enName(material: String): String {
            var name = ""
            when (material.uppercase()) {
                "PINK_WOOL" -> {
                    name = "Pink"
                }
                "RED_WOOL" -> {
                    name = "Red"
                }
                "LIGHT_GRAY_WOOL" -> {
                    name = "Gray"
                }
                "BLUE_WOOL" -> {
                    name = "Blue"
                }
                "WHITE_WOOL" -> {
                    name = "White"
                }
                "LIGHT_BLUE_WOOL" -> {
                    name = "Aqua"
                }
                "LIME_WOOL" -> {
                    name = "Green"
                }
                "YELLOW_WOOL" -> {
                    name = "Yellow"
                }
                "GRAY_WOOL" -> {
                    name = "Dark_Gray"
                }
                else -> {}
            }
            return name
        }

        /**
         * Get the english for byte as color name.
         *
         * @param b color byte. Used for 1.12 and older.
         * @return the english color name for given material. EMPTY if item is not supported.
         */
        @JvmStatic
        fun enName(b: Byte): String {
            var name = ""
            when (b.toInt()) {
                6 -> {
                    name = "Pink"
                }
                14 -> {
                    name = "Red"
                }
                9 -> {
                    name = "Aqua"
                }
                5 -> {
                    name = "Green"
                }
                4 -> {
                    name = "Yellow"
                }
                11 -> {
                    name = "Blue"
                }
                0 -> {
                    name = "White"
                }
                8 -> {
                    name = "Dark_Gray"
                }
                7 -> {
                    name = "Gray"
                }
                else -> {}
            }
            return name
        }

        /**
         * This may be used for leather armor.
         *
         * @param teamColor team color.
         * @return Equivalent color. Returns WHITE if given color has no equivalent.
         */
        @Deprecated("")
        @JvmStatic
        fun getColor(teamColor: TeamColor): Color {
            var color = Color.WHITE
            when (teamColor) {
                TeamColor.PINK -> {
                    color = Color.FUCHSIA
                }
                TeamColor.GRAY -> {
                    color = Color.GRAY
                }
                TeamColor.BLUE -> {
                    color = Color.BLUE
                }
                TeamColor.WHITE -> {
                }
                TeamColor.DARK_GREEN -> {
                    color = Color.GREEN
                }
                TeamColor.AQUA -> {
                    color = Color.AQUA
                }
                TeamColor.RED -> {
                    color = Color.RED
                }
                TeamColor.GREEN -> {
                    color = Color.LIME
                }
                TeamColor.YELLOW -> {
                    color = Color.YELLOW
                }
                TeamColor.DARK_GRAY -> {
                    color = Color.fromBGR(74, 74, 74)
                }
                else -> {}
            }
            return color
        }

        /**
         * Get bed with color.
         *
         * @param teamColor team color.
         * @return 1.13+ material
         */
        @Deprecated("")
        @JvmStatic
        fun getBedBlock(teamColor: TeamColor): Material {
            var color = "RED_BED"
            when (teamColor) {
                TeamColor.PINK -> {
                    color = "PINK_BED"
                }
                TeamColor.GRAY -> {
                    color = "LIGHT_GRAY_BED"
                }
                TeamColor.BLUE -> {
                    color = "BLUE_BED"
                }
                TeamColor.WHITE -> {
                    color = "WHITE_BED"
                }
                TeamColor.DARK_GREEN -> {
                    color = "GREEN_BED"
                }
                TeamColor.AQUA -> {
                    color = "LIGHT_BLUE_BED"
                }
                TeamColor.GREEN -> {
                    color = "LIME_BED"
                }
                TeamColor.YELLOW -> {
                    color = "YELLOW_BED"
                }
                TeamColor.DARK_GRAY -> {
                    color = "GRAY_BED"
                }
                else -> {}
            }
            return Material.valueOf(color)
        }

        /**
         * Get glass with team color.
         *
         * @param teamColor team color.
         * @return 1.13+ material
         */
        @Deprecated("")
        @JvmStatic
        fun getGlass(teamColor: TeamColor): Material {
            var color = "GLASS"
            when (teamColor) {
                TeamColor.PINK -> {
                    color = "PINK_STAINED_GLASS"
                }
                TeamColor.GRAY -> {
                    color = "LIGHT_GRAY_STAINED_GLASS"
                }
                TeamColor.BLUE -> {
                    color = "BLUE_STAINED_GLASS"
                }
                TeamColor.WHITE -> {
                    color = "WHITE_STAINED_GLASS"
                }
                TeamColor.DARK_GREEN -> {
                    color = "GREEN_STAINED_GLASS"
                }
                TeamColor.AQUA -> {
                    color = "LIGHT_BLUE_STAINED_GLASS"
                }
                TeamColor.GREEN -> {
                    color = "LIME_STAINED_GLASS"
                }
                TeamColor.YELLOW -> {
                    color = "YELLOW_STAINED_GLASS"
                }
                TeamColor.RED -> {
                    color = "RED_STAINED_GLASS"
                }
                TeamColor.DARK_GRAY -> {
                    color = "GRAY_STAINED_GLASS"
                }
                else -> {}
            }
            return Material.valueOf(color)
        }

        /**
         * Retrieve glass pane with team color.
         *
         * @param teamColor team color.
         * @return glass pane material for 1.13+.
         */
        @Deprecated("")
        @JvmStatic
        fun getGlassPane(teamColor: TeamColor): Material {
            var color = "GLASS"
            when (teamColor) {
                TeamColor.PINK -> {
                    color = "PINK_STAINED_GLASS_PANE"
                }
                TeamColor.GRAY -> {
                    color = "LIGHT_GRAY_STAINED_GLASS_PANE"
                }
                TeamColor.BLUE -> {
                    color = "BLUE_STAINED_GLASS_PANE"
                }
                TeamColor.WHITE -> {
                    color = "WHITE_STAINED_GLASS_PANE"
                }
                TeamColor.DARK_GREEN -> {
                    color = "GREEN_STAINED_GLASS_PANE"
                }
                TeamColor.AQUA -> {
                    color = "LIGHT_BLUE_STAINED_GLASS_PANE"
                }
                TeamColor.GREEN -> {
                    color = "LIME_STAINED_GLASS_PANE"
                }
                TeamColor.YELLOW -> {
                    color = "YELLOW_STAINED_GLASS_PANE"
                }
                TeamColor.RED -> {
                    color = "RED_STAINED_GLASS_PANE"
                }
                TeamColor.DARK_GRAY -> {
                    color = "GRAY_STAINED_PANE"
                }
                else -> {}
            }
            return Material.valueOf(color)
        }

        /**
         * Get glazed terracotta with team color.
         *
         * @param teamColor team color.
         * @return 1.13+ material
         */
        @Deprecated("")
        @JvmStatic
        fun getGlazedTerracotta(teamColor: TeamColor): Material {
            var color = "ORANGE_TERRACOTTA"
            when (teamColor) {
                TeamColor.PINK -> {
                    color = "PINK_TERRACOTTA"
                }
                TeamColor.GRAY -> {
                    color = "LIGHT_GRAY_TERRACOTTA"
                }
                TeamColor.DARK_GRAY -> {
                    color = "GRAY_TERRACOTTA"
                }
                TeamColor.BLUE -> {
                    color = "BLUE_TERRACOTTA"
                }
                TeamColor.WHITE -> {
                    color = "WHITE_TERRACOTTA"
                }
                TeamColor.DARK_GREEN -> {
                    color = "GREEN_TERRACOTTA"
                }
                TeamColor.AQUA -> {
                    color = "LIGHT_BLUE_TERRACOTTA"
                }
                TeamColor.GREEN -> {
                    color = "LIME_TERRACOTTA"
                }
                TeamColor.YELLOW -> {
                    color = "YELLOW_TERRACOTTA"
                }
                TeamColor.RED -> {
                    color = "RED_TERRACOTTA"
                }
                else -> {}
            }
            return Material.valueOf(color)
        }

        /**
         * Get wool with team color.
         *
         * @param teamColor team color.
         * @return 1.13+ material.
         */
        @Deprecated("")
        @JvmStatic
        fun getWool(teamColor: TeamColor): Material {
            var color = "WHITE_WOOL"
            when (teamColor) {
                TeamColor.PINK -> {
                    color = "PINK_WOOL"
                }
                TeamColor.GRAY -> {
                    color = "LIGHT_GRAY_WOOL"
                }
                TeamColor.DARK_GRAY -> {
                    color = "GRAY_WOOL"
                }
                TeamColor.BLUE -> {
                    color = "BLUE_WOOL"
                }
                TeamColor.WHITE -> {
                    color = "WHITE_WOOL"
                }
                TeamColor.DARK_GREEN -> {
                    color = "GREEN_WOOL"
                }
                TeamColor.AQUA -> {
                    color = "LIGHT_BLUE_WOOL"
                }
                TeamColor.GREEN -> {
                    color = "LIME_WOOL"
                }
                TeamColor.YELLOW -> {
                    color = "YELLOW_WOOL"
                }
                TeamColor.RED -> {
                    color = "RED_WOOL"
                }
                else -> {}
            }
            return Material.valueOf(color)
        }
    }
}
