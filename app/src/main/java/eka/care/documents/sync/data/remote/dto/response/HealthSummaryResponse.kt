package eka.care.documents.sync.data.remote.dto.response

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class LabParameter(
    @SerializedName("name")
    val name: String? = null,

    @SerializedName("id")
    val id: String? = null,

    @SerializedName("eka_id")
    val ekaId: String? = null,

    @SerializedName("unit")
    val unit: String? = null,

    @SerializedName("latest")
    val latest: Latest? = null,

    @SerializedName("prev")
    val prev: Previous? = null,

    @SerializedName("non_interpretable")
    val isNonInterpretable: Boolean? = null

)

@Keep
data class Latest(
    @SerializedName("result")
    val result: String? = null,
    @SerializedName("display_result")
    val displayResult: String? = null,
    @SerializedName("date")
    val date: Long? = null,
    @SerializedName("val_string")
    val valStr: String? = null,
    @SerializedName("val")
    val value: Float? = null,
    @SerializedName("range")
    val range: LabParamRange? = null,
    @SerializedName("display_time")
    val time: String? = null
)

@Keep
data class Previous(
    @SerializedName("val")
    val value: String? = null,
    @SerializedName("val_string")
    val valStr: String? = null,
    @SerializedName("date")
    val date: Long? = null,
    @SerializedName("result")
    val result: String? = null,
    @SerializedName("display_time")
    val time: String? = null
)

@Keep
data class LabParamRange(
    @SerializedName("low")
    val low: Float? = null,
    @SerializedName("high")
    val high: Float? = null,
)