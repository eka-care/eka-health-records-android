package eka.care.documents.data.db.model

import androidx.annotation.Keep

@Keep
data class AvailableDocTypes(
    val docType: Int,
    val count: Int,
)