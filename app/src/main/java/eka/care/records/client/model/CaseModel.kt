package eka.care.records.client.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class CaseModel(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("createdAt")
    val createdAt: Long,
    @SerializedName("updatedAt")
    val updatedAt: Long,
    @SerializedName("files")
    val records: List<RecordModel> = emptyList(),
)