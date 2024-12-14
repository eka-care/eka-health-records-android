package eka.care.documents.ui.presentation.model

data class DocumentPreviewModel(
    val isEncryptedFile: Boolean? = false,
    val filePath : List<String>,
    val fileType: String
)