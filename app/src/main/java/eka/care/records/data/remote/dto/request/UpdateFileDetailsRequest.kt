package eka.care.records.data.remote.dto.request

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class UpdateFileDetailsRequest(
    @SerializedName("oid")
    val filterId: String? = null,
    @SerializedName("dt")
    val documentType: String? = null,
    @SerializedName("dd_e")
    val documentDate: Long? = null,
    @SerializedName("tg")
    val userTags: List<String>? = null,
    @SerializedName("dtg_del")
    val derivedTagsDeleted: List<String>? = null,
)