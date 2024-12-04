package eka.care.documents.ui.presentation.state

import eka.care.documents.sync.data.remote.dto.response.Document

sealed class DocumentSmartReportState {
    object Loading : DocumentSmartReportState()
    data class Error(val message: String) : DocumentSmartReportState()
    data class Success(val data: Document) : DocumentSmartReportState()
}