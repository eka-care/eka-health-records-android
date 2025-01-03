package eka.care.documents.ui.presentation.components

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import eka.care.documents.ui.presentation.state.DocumentPreviewState
import java.io.File

fun handleFileDownload(
    state: DocumentPreviewState?,
    context: Context,
    selectedUri: Uri? = null
) {
    try {
        val file: File
        val fileType: String
        if (selectedUri != null) {
            file = File(selectedUri.path ?: "")
            fileType = if (selectedUri.path?.endsWith(".pdf") == true) "pdf" else "jpg"
        } else {
            val fileState = (state as? DocumentPreviewState.Success)
            file = File(fileState?.data?.first?.getOrNull(0) ?: "")
            fileType = fileState?.data?.second?.trim()?.lowercase() ?: "jpg"
        }

        val fileName = when (fileType) {
            "pdf" -> "MyFile_${System.currentTimeMillis()}.pdf"
            else -> "MyFile_${System.currentTimeMillis()}.jpg"
        }
        val mimeType = when (fileType) {
            "pdf" -> "application/pdf"
            else -> "image/jpeg"
        }

        if (file.exists()) {
            downloadFile(fileName = fileName, context = context, file = file, mimeType = mimeType)
        } else {
            Toast.makeText(context, "File not found", Toast.LENGTH_SHORT).show()
        }
    } catch (ex: Exception) {
        ex.printStackTrace()
        Toast.makeText(context, "Error downloading file: ${ex.message}", Toast.LENGTH_SHORT).show()
    }
}



fun downloadFile(fileName: String, context: Context, file: File, mimeType: String) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val destFileUri = context.contentResolver.insert(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                contentValues
            )
            if (destFileUri == null) {
                Log.d("FileDebug", "Failed to create file URI")
                return
            }
            context.contentResolver.openOutputStream(destFileUri)?.use { outputStream ->
                file.inputStream().use { inputStream ->
                    inputStream.copyTo(outputStream)
                    Log.d("FileDebug", "File successfully copied to $destFileUri")
                }
            }
        } else {
            val path =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    .toString()
            val dir = File(path)
            if (!dir.exists()) dir.mkdirs()
            val destFile = File(dir, fileName)
            file.copyTo(destFile, overwrite = true)
            Log.d("FileDebug", "File successfully copied to $destFile")
        }
        Toast.makeText(context, "File downloaded successfully", Toast.LENGTH_SHORT).show()
    } catch (ex: Exception) {
        ex.printStackTrace()
        Toast.makeText(context, "Error downloading file: ${ex.message}", Toast.LENGTH_SHORT).show()
    }
}