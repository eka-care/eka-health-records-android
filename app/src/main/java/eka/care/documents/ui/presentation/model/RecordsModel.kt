package eka.care.documents.ui.presentation.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class RecordModel(
    val localId: String?,
    val documentId: String?,
    val doctorId : String?,
    val documentType: Int?,
    val documentDate: Long?,
    val createdAt : Long?,
    val thumbnail: String?,
    val filePath: List<String>?,
    val fileType: String?,
    val cta: CTA?,
    val tags: String?,
    val source: Int?,
    val isAnalyzing: Boolean = false,
): Parcelable