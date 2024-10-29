package eka.care.documents.ui.presentation.model

import androidx.annotation.DrawableRes
import androidx.annotation.Keep

@Keep
data class FeaturedIconModel(
    @DrawableRes val icon: Int,
    val name: String,
    val cta: CTA
)

@Keep
data class RecordModel(
    val localId: String?,
    val documentId: String?,
    val doctorId : String?,
    val documentType: Int?,
    val documentDate: Long?,
    val createdAt : Long?,
    val thumbnail: String?,
    val cta: CTA?,
    val tags: String?,
    val source: Int?,
    val isAnalyzing: Boolean = false,
)