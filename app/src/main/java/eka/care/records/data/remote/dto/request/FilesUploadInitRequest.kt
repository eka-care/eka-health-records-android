package eka.care.records.data.remote.dto.request

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class FilesUploadInitRequest(
    @SerializedName("batch_request")
    val batchRequest: List<Batch>
)

@Keep
data class Batch(
    @SerializedName("files")
    val files: List<FileType>,
    @SerializedName("dt")
    val documentType: String,
    @SerializedName("dd")
    val documentDate: Long? = null,
    @SerializedName("sh")
    val sharable: Boolean = true,
    @SerializedName("tg")
    val tags: List<String>? = null,
    @SerializedName("is_encrypted")
    val isEncrypted: Boolean = false,
    @SerializedName("p_poid")
    val captainOid : String? = null
)

@Keep
data class FileType(
    @SerializedName("contentType")
    val contentType: String,
    @SerializedName("file_size")
    val fileSize: Long,
)