package eka.care.records.client.utils

import android.content.Context
import android.content.ContextWrapper
import android.net.Uri
import android.webkit.MimeTypeMap
import eka.care.records.data.mlkit.OCRTextExtractor
import eka.care.records.data.repository.MyFileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest
import java.util.UUID

class RecordsUtility {
    companion object{
        suspend fun downloadFile(url: String?, context: Context, type: String?): String {
            if (url == null) return ""
            val ocrTextExtractor = OCRTextExtractor(context = context)

            val directory = ContextWrapper(context).getDir("cache", Context.MODE_PRIVATE)
            val ext = if (type?.trim()?.lowercase() == "pdf") "pdf" else "jpg"
            val childPath = "${UUID.randomUUID()}.$ext"

            withContext(Dispatchers.IO) {
                val myFileRepository = MyFileRepository()
                val resp = myFileRepository.downloadFile(url)
                val file = File(directory, childPath)
                resp?.saveFile(file)
                if (ext == "jpg") {
                    ocrTextExtractor.extractTagsFromDocument(Uri.fromFile(file))
                }
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

        fun File.md5(): String {
            val buffer = ByteArray(1024 * 4)
            val md = MessageDigest.getInstance("MD5")

            FileInputStream(this).use { fis ->
                var bytesRead: Int
                while (fis.read(buffer).also { bytesRead = it } != -1) {
                    md.update(buffer, 0, bytesRead)
                }
            }

            val digest = md.digest()
            return digest.joinToString("") { "%02x".format(it) }
        }

        fun File.getMimeType(): String? =
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(this.extension)

        fun getWorkerTag(businessId: String): String {
            return "sync_records_${businessId}"
        }
    }
}