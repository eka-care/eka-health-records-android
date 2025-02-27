package eka.care.documents.sync.data.remote.dto.response

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
@Keep
data class GetFilesResponse(
    val items: List<Item>,
    @SerializedName("next_token")
    val nextToken: String
)
@Keep
data class Item(
    val record: Record
)
@Keep
data class Record(
    val item: ItemData
)
@Keep
data class ItemData(
    @SerializedName("document_id")
    val documentId: String,
    @SerializedName("document_type")
    val documentType: String,
    val metadata: Metadata,
    @SerializedName("patient_id")
    val patientId: String,
    @SerializedName("upload_date")
    val uploadDate: Int
)
@Keep
data class Metadata(
    val abha: Abha,
    @SerializedName("document_date")
    val documentDate: Int,
    @SerializedName("auto_tags")
    val autoTags: List<String>,
    val tags: List<String>,
    val thumbnail: String,
    val title: String
)
@Keep
data class Abha(
    @SerializedName("health_id")
    val healthId: String,
    @SerializedName("link_status")
    val linkStatus: String
)