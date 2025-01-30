package eka.care.documents.ui.presentation.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File


class FileSharing {

    fun shareFiles(context: Context, filePaths: List<String>) {
        try {
            if (filePaths.isEmpty()) {
                Toast.makeText(context, "No files to share", Toast.LENGTH_SHORT).show()
                return
            }

            val uris = ArrayList<Uri>()
            for (filePath in filePaths) {
                val file = File(filePath)
                if (!file.exists()) {
                    Toast.makeText(context, "File does not exist: $filePath", Toast.LENGTH_SHORT)
                        .show()
                    continue
                }

                // Generate a URI for each file
                val uri = FileProvider.getUriForFile(
                    context,
                    "eka.care.doctor.fileprovider", // Your FileProvider authority
                    file
                )
                uris.add(uri)
            }

            if (uris.isEmpty()) {
                Toast.makeText(context, "No valid files to share", Toast.LENGTH_SHORT).show()
                return
            }

            // Determine MIME type
            val mimeType = when {
                filePaths.all { it.endsWith(".pdf", true) } -> "application/pdf"
                filePaths.all {
                    it.endsWith(".jpg", true) || it.endsWith(
                        ".jpeg",
                        true
                    )
                } -> "image/jpeg"

                else -> "*/*"
            }

            // Use ACTION_SEND_MULTIPLE for multiple files
            val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                type = mimeType
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
            }

            // Start share activity
            context.startActivity(Intent.createChooser(intent, "Share files via"))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error sharing files: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}