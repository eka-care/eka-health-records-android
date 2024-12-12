package eka.care.documents.ui.response

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
