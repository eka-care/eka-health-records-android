package eka.care.records.data.entity.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import eka.care.records.data.entity.FileEntity

@Keep
data class DocumentGroup(
    @SerializedName("document_id")
    val documentId: String,
    @SerializedName("files")
    val files: List<FileEntity>,
    @SerializedName("total_size")
    val totalSize: Long,
    @SerializedName("last_used")
    val lastUsed: Long
)
