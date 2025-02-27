package eka.care.documents.sync.data.remote.dto.response

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
@Keep
data class GetFilesResponse(
    val items: List<Item>? = null,
    @SerializedName("next_token")
    val nextToken: String? = null
)
@Keep
data class Item(
    val record: Record? = null
)
@Keep
data class Record(
    val item: ItemData? = null
)
@Keep
data class ItemData(
    @SerializedName("document_id")
    val documentId: String? = null,
    @SerializedName("document_type")
    val documentType: String? = null,
    val metadata: Metadata? = null,
    @SerializedName("patient_id")
    val patientId: String? = null,
    @SerializedName("upload_date")
    val uploadDate: Int? = null
)
@Keep
data class Metadata(
    val abha: Abha? = null,
    @SerializedName("document_date")
    val documentDate: Int? = null,
    @SerializedName("auto_tags")
    val autoTags: List<String>? = null,
    val tags: List<String>? = null,
    val thumbnail: String? = null,
    val title: String? = null
)
@Keep
data class Abha(
    @SerializedName("health_id")
    val healthId: String? = null,
    @SerializedName("link_status")
    val linkStatus: String? = null
)