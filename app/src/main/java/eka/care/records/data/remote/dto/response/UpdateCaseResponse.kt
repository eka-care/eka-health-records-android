package eka.care.records.data.remote.dto.response

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class UpdateCaseResponse(
    @SerializedName("error")
    val error: String? = null,
    @SerializedName("code")
    val code: String? = null,
)