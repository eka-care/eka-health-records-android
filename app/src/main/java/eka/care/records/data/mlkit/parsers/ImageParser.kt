package eka.care.records.data.mlkit.parsers

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import eka.care.records.client.utils.Document
import eka.care.records.data.mlkit.await
import eka.care.records.data.mlkit.interfaces.DocumentParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class ImageParser : DocumentParser {
    override suspend fun parseDocument(filePath: String, context: Context): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                val absolutePath = filePath.toUri().path ?: return@withContext Result.failure(
                    Exception("Invalid file path")
                )
                val actualFile = File(absolutePath)
                val uri: Uri = FileProvider.getUriForFile(
                    context,
                    Document.getConfiguration().provider,
                    File(actualFile.absolutePath)
                )
                val inputImage = InputImage.fromFilePath(context, uri)
                val result = recognizer.process(inputImage).await()

                val tagList = result.text
                Log.d("OCRTextExtractor", "extractTagsFromDocument text : $tagList")
                Result.success(tagList)
            } catch (e: Exception) {
                Log.e(
                    "OCRTextExtractor",
                    "extractTagsFromDocument error : ${e.localizedMessage}",
                    e
                )
                Result.failure(e)
            }
        }
}