package eka.care.documents.ui.presentation.state

sealed class DocumentState {
    object Loading : DocumentState()
    data class Success(val filePath: List<String>?, val fileType: String?) : DocumentState()
    data class Error(val message: String) : DocumentState()
}