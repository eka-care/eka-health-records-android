package eka.care.documents.sync.data.remote.dto.request

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class UpdateFileDetailsRequest(
    @SerializedName("oid")
    val filterId: String? = null,

    @SerializedName("dt")
    val documentType: String? = null,

    @SerializedName("dd_e")
    val documentDate: String? = null,

    @SerializedName("tg")
    val userTags: List<String>? = null,

    @SerializedName("dtg_del")
    val derivedTagsDeleted: List<String>? = null,

    @SerializedName("sh_oids")
    val sharedWithDoctors: List<String>? = null,

    @SerializedName("ndhm")
    val linkAbha: Boolean? = null,

    @SerializedName("order_meds")
    val orderMedicine: Boolean? = null

)