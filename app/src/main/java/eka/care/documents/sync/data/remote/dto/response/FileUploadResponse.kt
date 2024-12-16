package eka.care.documents.sync.data.remote.dto.response

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

@Parcelize
@Keep
data class MyFilePage(
    @SerializedName("asset_url")
    var assetUrl: String? = null,

    @SerializedName("file_type")
    val fileType: String? = null,

    @SerializedName("share_text")
    val shareText: String? = null,
) : Parcelable

@Keep
data class FileUploadResponse(

    @SerializedName("document_id")
    val documentId: String? = null,

    @SerializedName("result")
    val result: String? = null,

    @SerializedName("error_string")
    val errorString: String? = null,

    @SerializedName("files")
    val files: List<MyFilePage>? = null,

    var code: Int?,
) : Serializable