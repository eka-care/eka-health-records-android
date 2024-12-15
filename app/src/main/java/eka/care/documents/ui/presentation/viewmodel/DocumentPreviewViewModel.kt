package eka.care.documents.ui.presentation.viewmodel

import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import eka.care.documents.data.db.database.DocumentDatabase
import eka.care.documents.data.repository.VaultRepository
import eka.care.documents.data.repository.VaultRepositoryImpl
import eka.care.documents.sync.data.remote.dto.response.SmartReport
import eka.care.documents.sync.data.remote.dto.response.SmartReportField
import eka.care.documents.sync.data.repository.MyFileRepository
import eka.care.documents.ui.presentation.components.Filter
import eka.care.documents.ui.presentation.components.LabParamResult
import eka.care.documents.ui.presentation.components.SmartViewTab
import eka.care.documents.ui.presentation.model.DocumentPreviewModel
import eka.care.documents.ui.presentation.state.DocumentPreviewState
import eka.care.documents.ui.presentation.state.DocumentSmartReportState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import java.io.File
import java.util.UUID

class DocumentPreviewViewModel(val app: Application) : AndroidViewModel(app) {

    private val vaultRepository: VaultRepository =
        VaultRepositoryImpl(DocumentDatabase.getInstance(app))
    private val myFileRepository = MyFileRepository()

    private val _selectedTab = MutableStateFlow(SmartViewTab.SMARTREPORT)
    val selectedTab = _selectedTab.asStateFlow()

    private val _selectedFilter = MutableStateFlow(Filter.ALL)
    val selectedFilter: StateFlow<Filter> = _selectedFilter

    private val _filteredSmartReport = MutableStateFlow<List<SmartReportField>>(emptyList())
    val filteredSmartReport: StateFlow<List<SmartReportField>> = _filteredSmartReport

    private val _document = MutableStateFlow<DocumentPreviewState>(DocumentPreviewState.Loading)
    val document: StateFlow<DocumentPreviewState> = _document

    private val _documentSmart =
        MutableStateFlow<DocumentSmartReportState>(DocumentSmartReportState.Loading)
    val documentSmart: StateFlow<DocumentSmartReportState> = _documentSmart
    fun updateFilter(filter: Filter, smartReport: SmartReport?) {
        _selectedFilter.value = filter
        _filteredSmartReport.value = getFilteredSmartReport(smartReport)
    }

    fun initializeReports(smartReport: SmartReport?) {
        _filteredSmartReport.value = getFilteredSmartReport(smartReport)
    }

    fun getFilteredSmartReport(smartReport: SmartReport?): List<SmartReportField> {
        return when (_selectedFilter.value) {
            Filter.ALL -> smartReport?.verified.orEmpty()
            Filter.OUT_OF_RANGE -> smartReport?.verified?.filter { field ->
                val resultEnum = LabParamResult.values().find { it.value == field.resultId }
                resultEnum != LabParamResult.NORMAL && resultEnum != LabParamResult.NO_INTERPRETATION_DONE
            } ?: emptyList()
        }
    }

    fun updateSelectedTab(newTab: SmartViewTab) {
        _selectedTab.value = newTab
    }

    fun getDocument(docId: String, userId: String) {
        viewModelScope.launch {
            try {
                val recordEntity = vaultRepository.getDocumentByDocId(docId = docId)
                Log.d("AYUSHI", recordEntity.toString())
                if (!recordEntity?.filePath.isNullOrEmpty()) {
                    _document.value = DocumentPreviewState.Success(
                        DocumentPreviewModel(
                            isEncryptedFile = recordEntity?.isEncrypted,
                            filePath = recordEntity?.filePath ?: emptyList(),
                            fileType = recordEntity?.fileType ?: ""
                        )

                    )
                    return@launch
                }
                val response = myFileRepository.getDocument(docId = docId, userId = userId)
                if (response == null) {
                    _document.value = DocumentPreviewState.Error("Something went wrong!")
                    return@launch
                }
                val files = mutableListOf<String>()
                var fileType = ""
                response.files.forEach {
                    fileType = it.fileType
                    val path = downloadFile(it.assetUrl, it.fileType)
                    files.add(path)
                }
                val documentEntity = vaultRepository.getDocumentByDocId(docId)
                if (documentEntity == null) {
                    _document.value = DocumentPreviewState.Error("Something went wrong!")
                    return@launch
                }
                val newDocumentEntity = documentEntity.copy(
                    filePath = files,
                    fileType = fileType
                )
                vaultRepository.updateDocuments(listOf(newDocumentEntity))
                _document.value = DocumentPreviewState.Success(
                    DocumentPreviewModel(
                        isEncryptedFile = recordEntity?.isEncrypted,
                        filePath = recordEntity?.filePath ?: emptyList(),
                        fileType = recordEntity?.fileType ?: ""
                    )
                )
            } catch (ex: Exception) {
                _document.value =
                    DocumentPreviewState.Error(ex.localizedMessage ?: "Something went wrong!")
            }
        }
    }

    fun getSmartReport(docId: String, userId: String) {
        viewModelScope.launch {
            val response = myFileRepository.getDocument(docId = docId, userId = userId)
            if (response != null) {
                _documentSmart.value = DocumentSmartReportState.Success(response)
            } else {
                _documentSmart.value = DocumentSmartReportState.Error("Something went wrong!")
            }
        }
    }

    private suspend fun downloadFile(assetUrl: String?, type: String): String {
        val directory = ContextWrapper(app).getDir("cache", Context.MODE_PRIVATE)
        val ext = if (type.trim().lowercase() == "pdf") "pdf" else "jpg"
        val childPath = "${UUID.randomUUID()}.$ext"
        withContext(Dispatchers.IO) {
            val resp = myFileRepository.downloadFile(assetUrl)
            resp?.saveFile(File(directory, childPath))
        }

        return "${directory.path}/$childPath"
    }

    private fun ResponseBody.saveFile(destFile: File) {
        byteStream().use { inputStream ->
            destFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    }
}