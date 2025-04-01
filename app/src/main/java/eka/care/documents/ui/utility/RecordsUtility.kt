package eka.care.documents.ui.utility

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import eka.care.documents.sync.data.repository.MyFileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import java.io.File
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class RecordsUtility {
    companion object{
        fun loadFromUri(context: Context, photoUri: Uri): Bitmap? {
            var bitmap: Bitmap? = null
            try {
                bitmap = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O_MR1) {
                    val source: ImageDecoder.Source =
                        ImageDecoder.createSource(context.contentResolver, photoUri)
                    ImageDecoder.decodeBitmap(source)
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, photoUri)
                }
            } catch (ex: IOException) {
                Log.e("log", "Exception in loadFromUri() = ", ex)
            }
            return bitmap
        }

        fun timestampToLong(timestamp: String, format: String = "EEE, dd MMM, yyyy"): Long? {
            if (timestamp == "Add Date") {
                return null
            }
            val dateFormat = SimpleDateFormat(format, Locale.getDefault())
            val date = dateFormat.parse(timestamp) ?: throw IllegalArgumentException("Invalid date format")
            return date.time / 1000
        }

        fun formatLocalDateToCustomFormat(date: Date): String? {
            val formatter = SimpleDateFormat("EEE, dd MMM, yyyy", Locale.getDefault())
            return formatter.format(date)
        }

        fun changeDateFormat(inputDate: String?): Long {
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val date = inputDate?.let { inputFormat.parse(it) }
                date?.time ?: 0L
            } catch (e: ParseException) {
                0L
            }
        }

        fun convertLongToDateString(time: Long?): String {
            if (time == null) return ""
            val date = Date(time * 1000)
            val format = SimpleDateFormat("dd EEE yyyy", Locale.getDefault())
            return format.format(date)
        }

        suspend fun downloadFile(url: String?, context: Context, type: String?): String {
            if (url == null) return ""

            val directory = ContextWrapper(context).getDir("cache", Context.MODE_PRIVATE)
            val ext = if (type?.trim()?.lowercase() == "pdf") "pdf" else "jpg"
            val childPath = "${UUID.randomUUID()}.$ext"

            withContext(Dispatchers.IO) {
                val myFileRepository = MyFileRepository()
                val resp = myFileRepository.downloadFile(url)
                resp?.saveFile(File(directory, childPath))
            }

            return "${directory.path}/$childPath"
        }

        suspend fun downloadThumbnail(assetUrl: String?, context: Context): String {
            val directory =
                ContextWrapper(context).getDir("imageDir", Context.MODE_PRIVATE)
            val childPath = "image${UUID.randomUUID()}.jpg"
            withContext(Dispatchers.IO) {
                val myFileRepository = MyFileRepository()
                val resp = myFileRepository.downloadFile(assetUrl)
                resp?.saveFile(File(directory, childPath))
            }

            return "${directory.path}/$childPath"
        }

        fun ResponseBody.saveFile(destFile: File) {
            byteStream().use { inputStream ->
                destFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        }
        fun File.getMimeType(): String? =
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(this.extension)

        enum class Status(val value: Int) {
            SYNCED_DOCUMENT(0),
            UNSYNCED_DOCUMENT(1), // unsynced document
            WAITING_TO_UPLOAD(2), // when worker start
            UPLOADING_DOCUMENT(3), // ui state
            WAITING_FOR_NETWORK(4) // offline
        }
    }
}