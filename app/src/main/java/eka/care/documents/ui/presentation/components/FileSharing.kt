package eka.care.documents.ui.presentation.components

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File


class FileSharing {
    fun shareFile(context: Context, filePath: String) {
        try {
            val file = File(filePath)
            if (!file.exists()) {
                throw IllegalArgumentException("File does not exist: $filePath")
            }
            // Get URI using FileProvider
            val uri = FileProvider.getUriForFile(
                context,
                "com.eka.care.doctor.records.provider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                // Set type based on file extension
                type = when {
                    filePath.endsWith(".pdf", true) -> "application/pdf"
                    filePath.endsWith(".jpg", true) -> "image/jpeg"
                    else -> "*/*"
                }
                putExtra(Intent.EXTRA_STREAM, uri)
            }
            context.startActivity(Intent.createChooser(intent, "Share file via"))
        } catch (e: Exception) {
            e.printStackTrace()

        }
    }
}