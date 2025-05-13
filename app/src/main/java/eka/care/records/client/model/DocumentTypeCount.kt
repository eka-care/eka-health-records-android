package eka.care.records.client.model

import androidx.annotation.Keep

@Keep
data class DocumentTypeCount(
    val documentType: String?,
    val count: Int?
)