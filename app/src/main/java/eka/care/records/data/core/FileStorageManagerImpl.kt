package eka.care.records.data.core

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.core.graphics.createBitmap
import eka.care.records.client.model.MedicalRecordException
import eka.care.records.client.utils.Logger
import eka.care.records.data.contract.FileStorageManager
import eka.care.records.data.contract.LogInterceptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class FileStorageManagerImpl(
    private val context: Context,
    private val logInterceptor: LogInterceptor? = null
) : FileStorageManager {
    
    private val fileDir by lazy {
        File(context.cacheDir, "medical_records").apply {
            if (!exists()) mkdirs()
        }
    }
    
    override suspend fun saveFile(file: File): String = withContext(Dispatchers.IO) {
        return@withContext try {
            val fileName = "${UUID.randomUUID()}.${file.extension}"
            val destination = File(fileDir, fileName)
            
            file.inputStream().use { input ->
                destination.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            
            destination.absolutePath
        } catch (e: Exception) {
            logInterceptor?.logError("FileManager", e)
            ""
        }
    }
    
    override suspend fun deleteFile(path: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(path)
            if (file.exists() && file.isFile) {
                file.delete()
            } else {
                false
            }
        } catch (e: Exception) {
            logInterceptor?.logError("FileManager", e)
            false
        }
    }
    
    override suspend fun getFile(path: String): File? = withContext(Dispatchers.IO) {
        val file = File(path)
        if (file.exists() && file.isFile) file else null
    }
    
    override suspend fun generateThumbnail(filePath: String): String? = withContext(Dispatchers.IO) {
        return@withContext try {
            val file = File(filePath)
            if(!file.exists()) {
                return@withContext null
            }

            if(file.extension.lowercase() == "pdf") {
                val fileDescriptor =
                    ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                val renderer = PdfRenderer(fileDescriptor)
                val page = renderer.openPage(0)
                val bitmap = createBitmap(page.width, page.height)
                val canvas = Canvas(bitmap)
                canvas.drawColor(Color.WHITE)
                canvas.drawBitmap(bitmap, 0f, 0f, null)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()
                val file = File(fileDir, "image${System.currentTimeMillis()}.png")
                if (file.exists()) file.delete()
                try {
                    val out = FileOutputStream(file)
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    out.flush()
                    out.close()
                } catch (e: Exception) {
                    Logger.e("Error saving thumbnail: ${e.message}")
                }
                file.path
            } else {
                val thumbnailPath = "${file.parent}/thumbnail_${file.name}"
                file.copyTo(File(thumbnailPath), overwrite = true)
                return@withContext thumbnailPath
            }
            null
        } catch (e: Exception) {
            logInterceptor?.logError("FileManager", e)
            null
        }
    }
    
    override suspend fun cleanupUnusedFiles(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            Result.success(0)
        } catch (e: Exception) {
            logInterceptor?.logError("FileManager", e)
            Result.failure(MedicalRecordException.FileStorageError)
        }
    }
}