package eka.care.documents.sync.data.remote.dto.response

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class VitalParamData(
    @SerializedName("name")
    val name: String?,

    @SerializedName("count")
    val count: Int?,

    @SerializedName("eka_id")
    val ekaId: String?,

    @SerializedName("desc")
    val desc: String?,
)