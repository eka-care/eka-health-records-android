package eka.care.doctor.features.documents.features.drive.presentation.state

sealed class DocumentPreviewState {
    object Loading : DocumentPreviewState()
    data class Error(val message: String) : DocumentPreviewState()
    data class Success(val data: Pair<List<String>, String>) : DocumentPreviewState()
}