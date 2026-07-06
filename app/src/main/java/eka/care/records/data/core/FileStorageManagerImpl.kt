package eka.care.records.data.core

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.core.graphics.createBitmap
import eka.care.records.client.model.EventLog
import eka.care.records.client.model.MedicalRecordException
import eka.care.records.client.utils.Records
import eka.care.records.data.contract.FileStorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class FileStorageManagerImpl(
    private val context: Context
) : FileStorageManager {

    companion object {
        // Decoded thumbnails must stay far below RecordingCanvas.MAX_BITMAP_SIZE
        // (100MB); keeping the smaller edge near this value caps the decode at
        // a few MB regardless of source resolution.
        private const val THUMBNAIL_MIN_DIMENSION_PX = 1080
    }

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
            Records.logEvent(
                EventLog(
                    params = mutableMapOf<String, Any?>().also {
                        it.put("fileName", file.name)
                        it.put("time", System.currentTimeMillis())
                    },
                    message = "Error saving file: ${e.message}"
                )
            )
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
            Records.logEvent(
                EventLog(
                    params = mutableMapOf<String, Any?>().also {
                        it.put("fileName", path)
                        it.put("time", System.currentTimeMillis())
                    },
                    message = "Error deleting file: ${e.message}"
                )
            )
            false
        }
    }

    override suspend fun getFile(path: String): File? = withContext(Dispatchers.IO) {
        val file = File(path)
        if (file.exists() && file.isFile) file else null
    }

    override suspend fun generateThumbnail(filePath: String): String? =
        withContext(Dispatchers.IO) {
            return@withContext try {
                val file = File(filePath)
                if (!file.exists()) {
                    return@withContext null
                }

                if (file.extension.lowercase() == "pdf") {
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
                    val tempFile = File(fileDir, "image${System.currentTimeMillis()}.png")
                    try {
                        val out = FileOutputStream(tempFile)
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                        out.flush()
                        out.close()
                    } catch (e: Exception) {
                        Records.logEvent(
                            EventLog(
                                params = mutableMapOf<String, Any?>().also {
                                    it.put("fileName", filePath)
                                    it.put("time", System.currentTimeMillis())
                                },
                                message = "Error saving thumbnail: ${e.message}"
                            )
                        )
                    }
                    tempFile.path
                } else {
                    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    BitmapFactory.decodeFile(file.path, bounds)
                    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
                        val thumbnailPath = "${file.parent}/thumbnail_${file.name}"
                        file.copyTo(File(thumbnailPath), overwrite = true)
                        return@withContext thumbnailPath
                    }
                    var sampleSize = 1
                    while (bounds.outWidth / (sampleSize * 2) >= THUMBNAIL_MIN_DIMENSION_PX &&
                        bounds.outHeight / (sampleSize * 2) >= THUMBNAIL_MIN_DIMENSION_PX
                    ) {
                        sampleSize *= 2
                    }
                    val bitmap = BitmapFactory.decodeFile(
                        file.path,
                        BitmapFactory.Options().apply { inSampleSize = sampleSize }
                    ) ?: return@withContext null
                    val thumbFile = File(fileDir, "thumb_${UUID.randomUUID()}.jpg")
                    FileOutputStream(thumbFile).use { out ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
                    }
                    bitmap.recycle()
                    return@withContext thumbFile.path
                }
            } catch (e: Exception) {
                Records.logEvent(
                    EventLog(
                        params = mutableMapOf<String, Any?>().also {
                            it.put("fileName", filePath)
                            it.put("time", System.currentTimeMillis())
                        },
                        message = "Error generating thumbnail: ${e.message}"
                    )
                )
                null
            }
        }

    override suspend fun cleanupUnusedFiles(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            Result.success(0)
        } catch (e: Exception) {
            Records.logEvent(
                EventLog(
                    params = mutableMapOf<String, Any?>().also {
                        it.put("time", System.currentTimeMillis())
                    },
                    message = "Error cleaning up files: ${e.message}"
                )
            )
            Result.failure(MedicalRecordException.FileStorageError)
        }
    }
}