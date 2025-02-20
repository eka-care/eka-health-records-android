package eka.care.documents.sync.data.remote.dto.response

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

// cta nhi h
// no isAnalysing
// no hash id
// no source
// tags nhi aa re h
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
    val item: ItemX
)
@Keep
data class ItemX(
    @SerializedName("document_id")
    val documentId: String,
    @SerializedName("document_type")
    val documentType: String,
    val metadata: Metadata,
    @SerializedName("patient_id")
    val patientId: Long,
    @SerializedName("upload_date")
    val uploadDate: Int
)
@Keep
data class Metadata(
    val abha: Abha,
    @SerializedName("document_date")
    val documentDate: Int,
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