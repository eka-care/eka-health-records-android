package eka.care.records.client.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class TagModel(
    @SerializedName("document_id")
    val documentId: String,
    @SerializedName("tag")
    val tag: String,
)
