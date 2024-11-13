package eka.care.documents.ui.presentation.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reader.presentation.states.PdfSource
import com.google.gson.Gson
import eka.care.documents.data.db.database.DocumentDatabase
import eka.care.documents.data.db.entity.VaultEntity
import eka.care.documents.data.repository.VaultRepository
import eka.care.documents.data.repository.VaultRepositoryImpl
import eka.care.documents.ui.presentation.components.DocumentBottomSheetType
import eka.care.documents.ui.presentation.components.DocumentViewType
import eka.care.documents.ui.presentation.model.CTA
import eka.care.documents.ui.presentation.model.RecordModel
import eka.care.documents.ui.presentation.screens.DocumentSortEnum
import eka.care.documents.ui.presentation.state.GetAvailableDocTypesState
import eka.care.documents.ui.presentation.state.GetRecordsState
import id.zelory.compressor.Compressor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class RecordsViewModel(app: Application) : AndroidViewModel(app) {

    private val vaultRepository: VaultRepository = VaultRepositoryImpl(DocumentDatabase.getInstance(app))
    val cardClickData = mutableStateOf<RecordModel?>(null)

    private val _getRecordsState = MutableStateFlow<GetRecordsState>(GetRecordsState.Loading)
    val getRecordsState: StateFlow<GetRecordsState> = _getRecordsState

    private val _getAvailableDocTypes = MutableStateFlow(GetAvailableDocTypesState())
    val getAvailableDocTypes: StateFlow<GetAvailableDocTypesState> = _getAvailableDocTypes

    val sortBy = mutableStateOf(DocumentSortEnum.UPLOAD_DATE)
    val documentType = mutableIntStateOf(-1)

    val localId = mutableStateOf("")
    val isRefreshing = mutableStateOf(false)

    private val _selectedTags = MutableStateFlow<List<String>>(emptyList())
    val selectedTags: StateFlow<List<String>> = _selectedTags

    private val _compressedFiles = MutableStateFlow<List<File>>(emptyList())
    val compressedFiles: StateFlow<List<File>> = _compressedFiles

    var pdfSource by mutableStateOf<PdfSource?>(null)

    var documentBottomSheetType by mutableStateOf<DocumentBottomSheetType?>(null)

    var documentViewType by mutableStateOf(DocumentViewType.GridView)

    fun loadSelectedTags(editDocument: Boolean) {
        if (editDocument) {
            CoroutineScope(Dispatchers.Default).launch {
                _selectedTags.value = emptyList()
                val tagString = cardClickData.value?.tags
                val selectedTags = tagString?.split(",")?.map { it.trim() } ?: emptyList()
                withContext(Dispatchers.Main) {
                    _selectedTags.value = selectedTags
                }
            }
        }
    }

    fun updateSelectedTags(newTags: List<String>) {
        _selectedTags.value = newTags
    }

    fun compressFile(fileList: List<File>, context: Context) {
        viewModelScope.launch {
            try {
                val mutableFileList = ArrayList<File>()
                val mutableListIterator = fileList.listIterator()
                while (mutableListIterator.hasNext()) {
                    val originalFile = mutableListIterator.next()
                    val compressedFile = withContext(Dispatchers.Default) {
                        Compressor.compress(context, originalFile)
                    }
                    mutableFileList.add(compressedFile)
                }
                _compressedFiles.value = mutableFileList
            } catch (ex: Exception) {
                Log.e("log", "Exception in compressFiles() = ", ex)
            }
        }
    }

    fun getLocalRecords(oid: String, docType: Int = -1, doctorId: String) {
        documentType.intValue = docType

        viewModelScope.launch {
            var dataEmitted = false
            try {
                val documentsFlowResp = if (sortBy.value == DocumentSortEnum.UPLOAD_DATE) {
                    vaultRepository.fetchDocuments(oid = oid, docType = documentType.intValue, doctorId = doctorId)
                } else {
                    vaultRepository.fetchDocumentsByDocDate(oid = oid, docType = documentType.intValue, doctorId = doctorId)
                }

                documentsFlowResp
                    .onCompletion {
                        if (!dataEmitted) {
                            _getRecordsState.value = GetRecordsState.EmptyState
                        }
                    }
                    .collect { vaultEntities ->
                        val records = vaultEntities.map { vaultEntity ->
                            RecordModel(
                                localId = vaultEntity.localId,
                                documentId = vaultEntity.documentId,
                                doctorId = doctorId,
                                documentType = vaultEntity.documentType,
                                documentDate = vaultEntity.documentDate,
                                createdAt = vaultEntity.createdAt,
                                thumbnail = vaultEntity.thumbnail,
                                cta = Gson().fromJson(vaultEntity.cta, CTA::class.java),
                                tags = vaultEntity.tags,
                                source = vaultEntity.source,
                                isAnalyzing = vaultEntity.isAnalyzing
                            )
                        }
                        getAvailableDocTypes(oid = oid, doctorId = doctorId)
                        _getRecordsState.value = if (records.isEmpty()) {
                            GetRecordsState.EmptyState
                        } else {
                            dataEmitted = true
                            GetRecordsState.Success(resp = records)
                        }
                    }
            } catch (ex: Exception) {
                _getRecordsState.value = GetRecordsState.Error(ex.localizedMessage ?: "An error occurred")
            }
        }
    }

    fun createVaultRecord(vaultEntity: VaultEntity) {
        try {
            viewModelScope.launch {
                vaultRepository.storeDocuments(listOf(vaultEntity))
            }
        } catch (_: Exception) {
        }
    }

    fun editDocument(
        localId: String,
        docType: Int?,
        oid: String,
        docDate: Long,
        tags: String,
        doctorId: String
    ) {
        try {
            viewModelScope.launch {
                vaultRepository.editDocument(localId, docType,  docDate, tags, patientId = oid)
                getLocalRecords(oid, doctorId = doctorId)
            }
        } catch (_: Exception) {
        }
    }

    fun deleteDocument(localId: String, oid: String, doctorId: String) {
        try {
            viewModelScope.launch {
                vaultRepository.deleteDocument(oid = oid, localId = localId)
                getLocalRecords(oid, doctorId = doctorId)
            }
        } catch (_: Exception) {
        }
    }

    fun syncEditedDocuments(oid : String, doctorId: String) {
        try {
            viewModelScope.launch {
                vaultRepository.getEditedDocuments(oid = oid, doctorId =  doctorId)
            }
        } catch (_: Exception) {
        }
    }

    private fun getAvailableDocTypes(oid: String, doctorId: String) {
        try {
            viewModelScope.launch {
                _getAvailableDocTypes.value =
                    GetAvailableDocTypesState(resp = vaultRepository.getAvailableDocTypes(oid = oid, doctorId = doctorId))
            }
        } catch (_: Exception) {
        }
    }
}