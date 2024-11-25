package eka.care.documents.ui.presentation.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.reader.presentation.states.PdfSource
import com.google.gson.Gson
import eka.care.documents.data.db.database.DocumentDatabase
import eka.care.documents.data.db.entity.VaultEntity
import eka.care.documents.data.repository.VaultRepository
import eka.care.documents.data.repository.VaultRepositoryImpl
import eka.care.documents.data.utility.DocumentUtility.Companion.docTypes
import eka.care.documents.sync.data.remote.dto.request.UpdateFileDetailsRequest
import eka.care.documents.sync.data.repository.MyFileRepository
import eka.care.documents.ui.presentation.components.DocumentBottomSheetType
import eka.care.documents.ui.presentation.components.DocumentViewType
import eka.care.documents.ui.presentation.model.CTA
import eka.care.documents.ui.presentation.model.RecordModel
import eka.care.documents.ui.presentation.screens.DocumentSortEnum
import eka.care.documents.ui.presentation.state.GetAvailableDocTypesState
import eka.care.documents.ui.presentation.state.GetRecordsState
import eka.care.documents.ui.utility.RecordsUtility.Companion.convertLongToFormattedDate
import id.zelory.compressor.Compressor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class RecordsViewModel(app: Application) : AndroidViewModel(app) {

    private val myFileRepository = MyFileRepository()
    private val vaultRepository: VaultRepository =
        VaultRepositoryImpl(DocumentDatabase.getInstance(app))

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

    private val _documentData = MutableStateFlow<Pair<List<String>?, String>>(Pair(emptyList(), ""))
    val documentData: StateFlow<Pair<List<String>?, String>> = _documentData

    var pdfSource by mutableStateOf<PdfSource?>(null)

    var documentBottomSheetType by mutableStateOf<DocumentBottomSheetType?>(null)

    var documentViewType by mutableStateOf(DocumentViewType.GridView)

    fun getTags(docId: String, userId: String) {
        viewModelScope.launch {
            _selectedTags.value = emptyList()
            val response = myFileRepository.getDocument(docId = docId, userId = userId)?.tags
            //   val apiTags = response?.let { Tags().getTagIdByNames(it) } ?: emptyList()
            val cardTags = cardClickData.value?.tags?.split(",")?.map { it.trim() } ?: emptyList()
            val allTags = (cardTags).distinct()
            _selectedTags.value = allTags
        }
    }

    fun loadSelectedTags(editDocument: Boolean) {
        if (editDocument) {
            viewModelScope.launch {
                val apiTags = _selectedTags.value
                val cardTags =
                    cardClickData.value?.tags?.split(",")?.map { it.trim() } ?: emptyList()
                val allTags = (apiTags + cardTags).distinct()
                _selectedTags.value = allTags
            }
        }
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
                    vaultRepository.fetchDocuments(
                        oid = oid,
                        docType = documentType.intValue,
                        doctorId = doctorId
                    )
                } else {
                    vaultRepository.fetchDocumentsByDocDate(
                        oid = oid,
                        docType = documentType.intValue,
                        doctorId = doctorId
                    )
                }

                documentsFlowResp
                    .onCompletion {
                        if (!dataEmitted) {
                            _getRecordsState.value = GetRecordsState.EmptyState
                        }
                    }
                    .collect { vaultEntities ->
                        if (!dataEmitted) { // Check if data has already been emitted
                            val records = vaultEntities.map { vaultEntity ->
                                RecordModel(
                                    localId = vaultEntity.localId,
                                    documentId = vaultEntity.documentId,
                                    doctorId = doctorId,
                                    documentType = vaultEntity.documentType,
                                    documentDate = vaultEntity.documentDate,
                                    createdAt = vaultEntity.createdAt,
                                    thumbnail = vaultEntity.thumbnail,
                                    filePath = vaultEntity.filePath,
                                    fileType = vaultEntity.fileType,
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
                                dataEmitted = true // Set flag to prevent further emission
                                GetRecordsState.Success(resp = records)
                            }
                        }
                    }
            } catch (ex: Exception) {
                _getRecordsState.value =
                    GetRecordsState.Error(ex.localizedMessage ?: "An error occurred")
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
                vaultRepository.editDocument(localId, docType, docDate, tags, patientId = oid)
                val tagList = tags.split(",")
                //  val tagNames = Tags().getTagNamesByIds(tagList)
                val updateFileDetailsRequest = UpdateFileDetailsRequest(
                    oid = oid,
                    documentType = docTypes.find { it.idNew == docType }?.id,
                    documentDate = convertLongToFormattedDate(docDate),
                    userTags = emptyList(),
                    linkAbha = false
                )

                cardClickData.value?.documentId?.let {
                    myFileRepository.updateFileDetails(
                        docId = it,
                        oid = oid,
                        updateFileDetailsRequest = updateFileDetailsRequest
                    )
                }
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

    fun syncEditedDocuments(oid: String, doctorId: String) {
        try {
            viewModelScope.launch {
                vaultRepository.getEditedDocuments(oid = oid, doctorId = doctorId)
            }
        } catch (_: Exception) {
        }
    }

    fun getDocumentData(oid: String, localId: String) {
        viewModelScope.launch {
            try {
                val data = vaultRepository.fetchDocumentData(oid = oid, localId = localId)
                val filePath = data.filePath
                val fileType = data.fileType
                _documentData.value = Pair(filePath, fileType)
            } catch (e: Exception) {
                e.printStackTrace() // Log the error or handle it appropriately
            }
        }
    }


    fun syncDeletedDocuments(oid: String, doctorId: String) {
        try {
            viewModelScope.launch {
                val vaultDocuments =
                    vaultRepository.getDeletedDocuments(doctorId = doctorId, oid = oid)

                vaultDocuments.forEach { vaultEntity ->
                    vaultEntity.documentId?.let {
                        val resp = myFileRepository.deleteDocument(docId = it, oid = oid)

                        if (resp in 200..299) {
                            vaultRepository.removeDocument(localId = vaultEntity.localId, oid = oid)
                        }
                    }

                }
            }
        } catch (_: Exception) {
        }
    }

    private fun getAvailableDocTypes(oid: String, doctorId: String) {
        try {
            viewModelScope.launch {
                _getAvailableDocTypes.value =
                    GetAvailableDocTypesState(
                        resp = vaultRepository.getAvailableDocTypes(
                            oid = oid,
                            doctorId = doctorId
                        )
                    )
            }
        } catch (_: Exception) {
        }
    }
}