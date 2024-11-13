package eka.care.documents.sync.data.remote.dto.response

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class Document(
    @SerializedName("patient_name") val patientName: String,
    @SerializedName("document_type") val documentType: String,
    @SerializedName("files") val files: List<File>,
    @SerializedName("can_delete") val canDelete: Boolean,
    @SerializedName("smart_report") val smartReport: SmartReport?, // Nullable
    @SerializedName("thumbnail") val thumbnail: String,
    @SerializedName("document_id") val documentId: String,
    @SerializedName("uploaded_by_me") val uploadedByMe: Boolean,
    @SerializedName("source") val source: String
)

@Keep
data class SmartReport(
    @SerializedName("report_id") val reportId: Int,
    @SerializedName("status") val status: String
)

@Keep
data class File(
    @SerializedName("asset_url") val assetUrl: String,
    @SerializedName("file_type") val fileType: String,
    @SerializedName("share_text") val shareText: String
)