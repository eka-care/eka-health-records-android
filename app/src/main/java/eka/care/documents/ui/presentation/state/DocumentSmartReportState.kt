package eka.care.documents.ui.presentation.state

import eka.care.documents.sync.data.remote.dto.response.SmartReport

sealed class DocumentSmartReportState {
    object Loading : DocumentSmartReportState()
    data class Error(val message: String) : DocumentSmartReportState()
    data class Success(val data: SmartReport) : DocumentSmartReportState()
}