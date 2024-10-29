package eka.care.documents.data.db.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Keep
@Parcelize
data class CTA(
    @SerializedName("action")
    var action: String? = null,
    @SerializedName("title")
    var title: String? = null,
    @SerializedName("pid")
    var pageId: String? = null,
    @SerializedName("params")
    var params: MutableMap<String, String>? = null,
    @SerializedName("syncid")
    val syncId: String? = null
) : Parcelable


@Keep
@Parcelize
data class CTAData(
    @SerializedName("action")
    val action: String?,
    @SerializedName("title")
    val title: String?,
    @SerializedName("pid")
    val pageId: String?,
    @SerializedName("params")
    val params: @RawValue JsonElement?
) : Parcelable

@Keep
data class CTAInternal(
    @SerializedName("action")
    val action: String? = null,
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("pid")
    val pageId: String?,
    @SerializedName("params")
    val params: MutableMap<String, Any?>? = null
)

@Keep
data class EkaButtonModel(
    val leftIcon: String? = null,
    val rightIcon: String? = null,
    val hide: Boolean? = null,
    val cta: CTA? = null,
    val style: EkaButtonStyle?
) {
    @Keep
    data class EkaButtonStyle(
        val type: String? = "primary"
    )
}