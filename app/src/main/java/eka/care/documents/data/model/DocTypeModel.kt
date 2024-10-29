package eka.care.documents.data.model

import androidx.annotation.Keep

@Keep
data class DocTypeModel(
    val documentType: String?,
    val filter: String?,
    val id: String?,
    val idNew: Int?,
    val icon : Int?
)