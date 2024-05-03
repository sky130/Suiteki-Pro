package com.github.sky130.suiteki.pro.util


import java.io.File
import java.util.zip.ZipFile

object ZipUtils {

    fun extractFrom(file: File, fileName: String): String? {
        ZipFile(file).use { zipFile ->
            val entry = zipFile.getEntry(fileName) ?: return null
            return zipFile.getInputStream(entry).bufferedReader().use { it.readText() }
        }
    }

}