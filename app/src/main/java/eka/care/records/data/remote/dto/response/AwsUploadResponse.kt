package eka.care.records.data.remote.dto.response

import androidx.annotation.Keep

@Keep
data class AwsUploadResponse(
    val error: Boolean,
    val message: String?,
    val documentId: String?
)