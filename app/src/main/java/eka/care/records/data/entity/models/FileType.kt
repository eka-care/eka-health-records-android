package eka.care.records.data.entity.models

enum class FileType {
    PDF,
    IMAGE;

    companion object {
        fun fromString(fileType: String): FileType {
            return when (fileType) {
                "pdf" -> FileType.PDF
                else -> FileType.IMAGE
            }
        }
    }
}