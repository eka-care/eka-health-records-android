package eka.care.records.data.mlkit.interfaces

import android.content.Context

interface DocumentParser {
    suspend fun parseDocument(filePath: String, context: Context): Result<String>
}