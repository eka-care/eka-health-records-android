package eka.care.records.client.model

data class RecordModel(
    val id: String,
    val thumbnail: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
    val documentDate: Long? = null,
    val documentType: String = "ot",
    val isSmart: Boolean = false,
    val smartReport: String? = null,
    val files: List<File> = emptyList(),
) {
    data class File(
        val id: Long,
        val filePath: String?,
        val fileType: String,
    )
}