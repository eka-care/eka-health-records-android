package eka.care.records.data.remote.dto.response

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class AwsUploadResponse(
    @SerializedName("error")
    val error: Boolean,
    @SerializedName("message")
    val message: String?,
    @SerializedName("documentId")
    val documentId: String
)