package eka.care.documents.sync.data.remote.dto.response

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class GetFilesResponse(
    @SerializedName("items")
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
    val documentType: String? = null,
    val metadata: Metadata? = null,
    @SerializedName("patient_id")
    val patientId: String? = null,
    @SerializedName("upload_date")
    val uploadDate: Long? = null
)

@Keep
data class Metadata(
    val abha: Abha? = null,
    @SerializedName("document_date")
    val documentDate: Long? = null,
    @SerializedName("auto_tags")
    val autoTags: List<String>? = null,
    @SerializedName("tags")
    val tags: List<String>? = null,
    @SerializedName("thumbnail")
    val thumbnail: String? = null,
    @SerializedName("title")
    val title: String? = null
)

@Keep
data class Abha(
    @SerializedName("health_id")
    val healthId: String? = null,
    @SerializedName("link_status")
    val linkStatus: String? = null
)