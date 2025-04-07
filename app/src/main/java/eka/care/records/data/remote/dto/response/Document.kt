package eka.care.records.data.remote.dto.response

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class Document(
    @SerializedName("patient_name") val patientName: String,
    @SerializedName("document_type") val documentType: String,
    @SerializedName("document_date_epoch") val documentDate: String,
    @SerializedName("user_tags") val userTags: List<String>? = null,
    @SerializedName("tags") val tags: List<String>? = null,
    @SerializedName("files") val files: List<File>,
    @SerializedName("can_delete") val canDelete: Boolean,
    @SerializedName("smart_report") val smartReport: SmartReport?,
    @SerializedName("thumbnail") val thumbnail: String,
    @SerializedName("document_id") val documentId: String,
    @SerializedName("uploaded_by_me") val uploadedByMe: Boolean,
    @SerializedName("source") val source: String
)

@Keep
data class SmartReport(
    @SerializedName("verified")
    val verified: List<SmartReportField>? = null,

    @SerializedName("unverified")
    val unverified: List<SmartReportField>? = null,
)

@Keep
data class SmartReportField(
    @SerializedName("name")
    var name: String? = null,

    @SerializedName("value")
    var value: String? = null,

    @SerializedName("unit")
    var unit: String? = null,

    @SerializedName("range")
    val range: String? = null,

    @SerializedName("result")
    var result: String? = null,

    @SerializedName("vital_id")
    val vitalId: String?,

    @SerializedName("page_num")
    val pageNum: Int,

    @SerializedName("file_index")
    val fileIndex: Int,

    @SerializedName("coordinates")
    val coordinates: List<FieldCoordinates>? = null,

    @SerializedName("eka_id")
    val ekaId: String?,

    @SerializedName("is_result_editable")
    val isResultEditable: Boolean = false,

    @SerializedName("date")
    val date: Long?,

    @SerializedName("result_id")
    val resultId: String?,

    @SerializedName("display_result")
    val displayResult: String? = null,

    var isVerified: Boolean = false,

    var keepConfirmButtonEnabled: Boolean = true,

    )

@Keep
data class FieldCoordinates(
    @SerializedName("x")
    val x: Float,

    @SerializedName("y")
    val y: Float,
)

@Keep
data class File(
    @SerializedName("asset_url") val assetUrl: String,
    @SerializedName("file_type") val fileType: String,
    @SerializedName("share_text") val shareText: String
)