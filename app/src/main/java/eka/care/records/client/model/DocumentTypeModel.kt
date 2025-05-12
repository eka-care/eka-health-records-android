package eka.care.records.client.model

import androidx.annotation.Keep

@Keep
data class DocumentTypeModel(
    val documentType: String?,
    val filter: String?,
    val id: String?,
    val idNew: Int?,
    val icon : Int?
)