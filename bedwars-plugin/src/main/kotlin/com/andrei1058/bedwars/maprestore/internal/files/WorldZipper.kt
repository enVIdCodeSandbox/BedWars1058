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

package com.andrei1058.bedwars.maprestore.internal.files

import com.andrei1058.bedwars.api.util.ZipFileUtil
import com.andrei1058.bedwars.maprestore.internal.InternalAdapter
import java.io.File
import java.io.IOException
import org.bukkit.Bukkit

open class WorldZipper(private val worldName: String, private var replace: Boolean) {
    private fun execute() {
        if (!exists() || replace) {
            try {
                zipWorldFolder()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    @Throws(IOException::class)
    private fun zipWorldFolder() {
        val worldFolder = getWorldFolder()
        val backupFile = getBackupFile()
        ZipFileUtil.zipDirectory(worldFolder, backupFile)
    }

    private fun getWorldFolder(): File {
        val worldContainer = Bukkit.getWorldContainer()
        return File(worldContainer, worldName)
    }

    private fun getBackupFile(): File {
        val backupFolder = InternalAdapter.backupFolder
        return File(backupFolder, worldName + ".zip")
    }

    private fun exists(): Boolean {
        val worldFolder = getWorldFolder()
        return worldFolder.isDirectory()
    }

    init {
        execute()
    }
}
