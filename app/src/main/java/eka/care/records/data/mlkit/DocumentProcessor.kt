package eka.care.records.data.mlkit

import android.content.Context
import com.google.android.gms.tasks.Task
import eka.care.records.data.entity.models.FileType
import eka.care.records.data.mlkit.interfaces.DocumentParser
import eka.care.records.data.mlkit.parsers.ImageParser
import eka.care.records.data.mlkit.parsers.PdfParser
import kotlinx.coroutines.suspendCancellableCoroutine
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

internal object OCRTextExtractor {
    suspend fun extractTextFromDocument(
        fileType: String,
        filePath: String,
        context: Context
    ): Result<String> {
        val parser = getParserByFileType(FileType.fromString(fileType))
        return parser.parseDocument(filePath = filePath, context = context)
    }

    fun getParserByFileType(fileType: FileType): DocumentParser {
        return when (fileType) {
            FileType.IMAGE -> {
                ImageParser()
            }

            FileType.PDF -> {
                PdfParser()
            }
        }
    }
}