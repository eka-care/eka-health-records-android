package eka.care.documents.sync.data.remote.dto.response

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class HealthSummaryResponse(
    @SerializedName("summary")
    val healthSummary: HealthSummary? = null,
    @SerializedName("lab_parameters")
    val labParams: LabParams? = null,
)

@Keep
data class HealthSummary(
    @SerializedName("h1")
    val heading: String? = null,

    @SerializedName("static")
    val static: EmptyHealthSummary? = null,

    @SerializedName("user")
    val user: UserHealthSummary? = null,
)

@Keep
data class EmptyHealthSummary(
    @SerializedName("d")
    val text: String? = null,
)

@Keep
data class UserHealthSummary(
    @SerializedName("d")
    val text: String? = null,
    @SerializedName("organs")
    val organs: List<Organ>? = null,
)

@Keep
data class Organ(
    @SerializedName("pic")
    val pic: String? = null,
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("s1")
    val result: String? = null,
    @SerializedName("s2")
    val resultTime: String? = null,
    @SerializedName("card_enum")
    val cardType: Int? = null,  // RED(0), GREEN(1), PURPLE(2), GREY(3)
    @SerializedName("id")
    val id: String? = null,
)

@Keep
data class LabParams(
    @SerializedName("h1")
    val heading: String? = null,

    @SerializedName("h2")
    val subHeading: String? = null,

    @SerializedName("static")
    val static: EmptyLabParams? = null,

    @SerializedName("user")
    val user: UserLabParams? = null,
)

@Keep
data class EmptyLabParams(
    @SerializedName("h")
    val heading: String? = null,
    @SerializedName("d")
    val text: String? = null,
)

@Keep
data class UserLabParams(
    @SerializedName("parameters")
    val labParameters: List<LabParameter>? = null,
)

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