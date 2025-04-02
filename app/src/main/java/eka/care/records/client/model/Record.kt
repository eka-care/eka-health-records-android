package eka.care.records.client.model

data class Record(
    val id: String,
    val thumbnail: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
    val documentDate: Long? = null,
    val documentType: String = "ot",
    val isSmart: Boolean = false,
    val files: List<File> = emptyList(),
) {
    data class File(
        val id: String,
        val filePath: String?,
        val fileType: String,
    )
}