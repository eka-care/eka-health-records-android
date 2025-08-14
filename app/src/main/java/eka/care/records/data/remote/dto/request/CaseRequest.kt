package eka.care.records.data.remote.dto.request

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class CaseRequest(
    @SerializedName("id")
    val caseId: String,
    @SerializedName("display_name")
    val name: String,
    @SerializedName("type")
    val caseType: String? = null,
    @SerializedName("occurred_at")
    val occurredAt: Long,
)