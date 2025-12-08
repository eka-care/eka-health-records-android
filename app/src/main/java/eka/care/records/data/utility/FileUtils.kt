package eka.care.records.data.utility

import java.io.File

object FileUtils {
    fun getFileSize(filePath: String): Long {
        return try {
            val file = File(filePath)
            if (file.exists() && file.isFile) {
                file.length()
            } else {
                0L
            }
        } catch (e: Exception) {
            0L
        }
    }
}