package eka.care.records.data.mlkit.parsers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.core.graphics.createBitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import eka.care.records.data.mlkit.interfaces.DocumentParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class PdfParser : DocumentParser {
    override suspend fun parseDocument(filePath: String, context: Context): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                if (filePath.isEmpty()) {
                    return@withContext Result.success("")
                }
                val file = File(filePath)
                if (!file.exists()) {
                    return@withContext Result.failure(Exception("File does not exist"))
                }
                val fileDescriptor =
                    ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                val pdfRenderer = PdfRenderer(fileDescriptor)
                val textRecognizer =
                    TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                val extractedText = StringBuilder()
                for (pageIndex in 0 until pdfRenderer.pageCount) {
                    val page = pdfRenderer.openPage(pageIndex)

                    // Create bitmap for the page
                    val bitmap = createBitmap(page.width, page.height)

                    // Render PDF page to bitmap
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

                    // Extract text from bitmap using ML Kit
                    val pageText = extractTextFromBitmap(textRecognizer, bitmap)
                    extractedText.append(pageText)
                    extractedText.append("\n")

                    // Clean up
                    bitmap.recycle()
                    page.close()
                }

                pdfRenderer.close()
                fileDescriptor.close()
                textRecognizer.close()

                return@withContext Result.success(extractedText.toString())
            } catch (e: Exception) {
                return@withContext Result.failure(e)
            }
        }

    private suspend fun extractTextFromBitmap(
        textRecognizer: TextRecognizer,
        bitmap: Bitmap
    ): String {
        return suspendCancellableCoroutine { continuation ->
            val image = InputImage.fromBitmap(bitmap, 0)

            textRecognizer.process(image)
                .addOnSuccessListener { visionText ->
                    continuation.resume(visionText.text)
                }
                .addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }
        }
    }
}