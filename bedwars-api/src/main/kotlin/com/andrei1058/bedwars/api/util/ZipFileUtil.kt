package com.andrei1058.bedwars.api.util

import java.io.*
import java.util.Enumeration
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

object ZipFileUtil {
    @Throws(IOException::class)
    @JvmStatic
    fun zipDirectory(dir: File, zipFile: File) {
        val fout = FileOutputStream(zipFile)
        val zout = ZipOutputStream(fout)
        zipSubDirectory("", dir, zout)
        zout.close()
    }

    @Throws(IOException::class)
    @JvmStatic
    private fun zipSubDirectory(basePath: String, dir: File, zout: ZipOutputStream) {
        val buffer = ByteArray(4096)
        val files: Array<File>? = dir.listFiles()
        if (files == null) {
            return
        }
        for (file in files) {
            if (file.isDirectory()) {
                val path = basePath + file.getName() + "/"
                zout.putNextEntry(ZipEntry(path))
                zipSubDirectory(path, file, zout)
                zout.closeEntry()
            } else {
                val fin = FileInputStream(file)
                zout.putNextEntry(ZipEntry(basePath + file.getName()))
                var length: Int
                while ((fin.read(buffer).also { length = it }) > 0) {
                    zout.write(buffer, 0, length)
                }
                zout.closeEntry()
                fin.close()
            }
        }
    }

    @Throws(IOException::class)
    @JvmStatic
    fun unzipFileIntoDirectory(file: File, jiniHomeParentDir: File) {
        if (!file.exists()) {
            return
        }
        val zipFile = ZipFile(file)
        val files = zipFile.entries()
        var f: File
        var fos: FileOutputStream? = null
        while (files.hasMoreElements()) {
            try {
                val entry = files.nextElement() as ZipEntry
                val eis = zipFile.getInputStream(entry)
                val buffer = ByteArray(1024)
                var bytesRead: Int
                f = File(jiniHomeParentDir.getAbsolutePath(), entry.getName())
                if (entry.isDirectory()) {
                    f.mkdirs()
                    continue
                } else {
                    f.getParentFile().mkdirs()
                    f.createNewFile()
                }
                fos = FileOutputStream(f)
                while ((eis.read(buffer).also { bytesRead = it }) != -1) {
                    fos.write(buffer, 0, bytesRead)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                if (fos != null) {
                    try {
                        fos.close()
                    } catch (ignored: IOException) {
                        // ignore
                    }
                }
            }
        }
    }
}
