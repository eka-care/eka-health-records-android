package eka.care.documents.ui.response


import androidx.annotation.Keep
import com.google.gson.JsonElement
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.RawValue

@Keep
data class FilesStaticDataResponse(
    @SerializedName("add_vitals_cta")
    @Expose
    val addVitalsCta: @RawValue AddVitalsCta?,
    @SerializedName("file_size_limit")
    @Expose
    val fileSizeLimit: HashMap<String, SizeLimit?>?,
    @SerializedName("document_filters")
    @Expose
    val documentFilters: List<Filter>?
)

@Keep
data class Filter(
    @SerializedName("id")
    @Expose
    val id: String?,
    @SerializedName("id_new")
    val idNew: Int?,
    @SerializedName("filter")
    @Expose
    val title: String?,
    @SerializedName("document_type")
    @Expose
    val documentType: String?,
)

@Keep
data class AddVitalsCta(
    @SerializedName("params")
    @Expose
    val params: @RawValue JsonElement?,
    @SerializedName("pid")
    @Expose
    val pid: String?
)

@Keep
data class SizeLimit(
    @SerializedName("max_size")
    @Expose
    val maxSize: MaxSize?
)

@Keep
data class MaxSize(
    @SerializedName("display_val")
    @Expose
    val displayVal: String?,
    @SerializedName("val")
    @Expose
    val valX: Int?
)


