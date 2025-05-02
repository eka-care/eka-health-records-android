package eka.care.records.client.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class RecordModel(
    @SerializedName("id")
    val id: String,
    @SerializedName("thumbnail")
    val thumbnail: String? = null,
    @SerializedName("status")
    val status: RecordStatus = RecordStatus.NONE,
    @SerializedName("createdAt")
    val createdAt: Long,
    @SerializedName("updatedAt")
    val updatedAt: Long,
    @SerializedName("documentDate")
    val documentDate: Long? = null,
    @SerializedName("documentType")
    val documentType: String = "ot",
    @SerializedName("isSmart")
    val isSmart: Boolean = false,
    @SerializedName("smartReport")
    val smartReport: String? = null,
    @SerializedName("files")
    val files: List<RecordFile> = emptyList(),
) {
    @Keep
    data class RecordFile(
        @SerializedName("id")
        val id: Long,
        @SerializedName("filePath")
        val filePath: String?,
        @SerializedName("fileType")
        val fileType: String,
    )
}