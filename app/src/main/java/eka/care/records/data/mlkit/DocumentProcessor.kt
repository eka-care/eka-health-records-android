package eka.care.records.data.mlkit

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resumeWithException

suspend fun <T> Task<T>.await(): T {
    return suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { result ->
            continuation.resumeWith(Result.success(result))
        }
        addOnFailureListener { exception ->
            continuation.resumeWithException(exception)
        }
        addOnCanceledListener {
            continuation.cancel()
        }
    }
}


internal class OCRTextExtractor(
    private val context: Context
) {
    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun extractTagsFromDocument(imageUri: Uri): Result<List<String>> =
        withContext(Dispatchers.IO) {
            try {
                val inputImage = InputImage.fromFilePath(context, imageUri)
                val result = recognizer.process(inputImage).await()

                val tagList =
                    result.textBlocks.mapNotNull { it.text }.filter { !it.containsDigit() }
                Log.d("OCRTextExtractor", "extractTagsFromDocument text : $tagList")
                Result.success(tagList)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}

fun String.containsDigit(): Boolean {
    return this.any { it.isDigit() }
}