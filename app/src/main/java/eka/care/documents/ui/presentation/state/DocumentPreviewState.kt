package eka.care.documents.ui.presentation.state

import eka.care.documents.ui.presentation.model.DocumentPreviewModel

sealed class DocumentPreviewState {
    object Loading : DocumentPreviewState()
    data class Error(val message: String) : DocumentPreviewState()
    data class Success(val data: DocumentPreviewModel) : DocumentPreviewState()
}