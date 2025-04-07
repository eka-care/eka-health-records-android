package eka.care.records.client.model

import com.google.gson.annotations.SerializedName

data class DocumentTypeCount(
    @SerializedName("document_type") val documentType: String,
    @SerializedName("count") val count: Int
)