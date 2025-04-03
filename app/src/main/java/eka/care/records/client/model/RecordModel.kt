package eka.care.records.client.model

data class RecordModel(
    val id: String,
    val thumbnail: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
    val documentDate: Long? = null,
    val documentType: String = "ot",
    val isSmart: Boolean = false,
    var smartReport: String? = null,
)