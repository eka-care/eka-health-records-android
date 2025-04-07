package eka.care.records.data.remote.dto.response

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class FilesUploadInitResponse(
    @SerializedName("batch_response")
    val batchResponse: List<BatchResponse>?,
    @SerializedName("error")
    val error: Boolean?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("token")
    val uploadTime: String?
)

@Keep
data class BatchResponse(
    @SerializedName("document_id")
    val documentId: String?,
    @SerializedName("forms")
    val forms: List<Form>
)

@Keep
data class Form(
    @SerializedName("url")
    val url: String,
    @SerializedName("fields")
    val fields: Map<String, String>
)