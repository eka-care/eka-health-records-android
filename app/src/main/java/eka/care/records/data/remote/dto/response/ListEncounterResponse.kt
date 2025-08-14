package eka.care.records.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class ListEncounterResponse(
    @SerializedName("cases")
    val cases: List<CaseItem>?,
    @SerializedName("next_token")
    val nextToken: String?
)

data class CaseItem(
    @SerializedName("id")
    val id: String,
    @SerializedName("status")
    val status: String?,
    @SerializedName("updated_at")
    val updatedAt: Long?,
    @SerializedName("item")
    val itemDetails: ItemDetails?
)

data class ItemDetails(
    @SerializedName("display_name")
    val displayName: String?,
    @SerializedName("type")
    val type: String?,
    @SerializedName("created_at")
    val createdAt: Long?
)

