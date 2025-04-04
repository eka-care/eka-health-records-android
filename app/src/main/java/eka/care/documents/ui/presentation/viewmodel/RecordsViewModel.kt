package eka.care.documents.ui.presentation.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
//import com.example.reader.presentation.states.PdfSource
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class RecordsViewModel(app: Application) : AndroidViewModel(app) {

    private val myFileRepository = MyFileRepository()
    private val vaultRepository: VaultRepository =
        VaultRepositoryImpl(DocumentDatabase.getInstance(app))

    private lateinit var launch: Job

    private val _isOnline = MutableStateFlow(true)
    val isOnline = _isOnline.asStateFlow()

    private var networkCallback: ConnectivityManager.NetworkCallback? = null

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

//    var pdfSource by mutableStateOf<PdfSource?>(null)

    var documentBottomSheetType by mutableStateOf<DocumentBottomSheetType?>(null)

    var documentViewType by mutableStateOf(DocumentViewType.GridView)

    private val _photoUri = MutableStateFlow<Uri?>(null)
    val photoUri: StateFlow<Uri?> = _photoUri

    fun updatePhotoUri(uri: Uri?) {
        _photoUri.value = uri
    }

    fun observeNetworkStatus(context: Context) {
        unregisterNetworkCallback(context)

        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        _isOnline.value = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ?: false

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                _isOnline.value = true
            }

            override fun onLost(network: Network) {
                _isOnline.value = false
            }

            override fun onCapabilitiesChanged(
                network: Network,
                capabilities: NetworkCapabilities
            ) {
                _isOnline.value = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            }
        }

        try {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()

            connectivityManager.registerNetworkCallback(request, networkCallback!!)
        } catch (e: Exception) {
            Log.e("RecordsViewModel", "Failed to register network callback", e)
        }
    }

    private fun unregisterNetworkCallback(context: Context) {
        networkCallback?.let {
            try {
                val connectivityManager =
                    context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                connectivityManager.unregisterNetworkCallback(it)
            } catch (e: Exception) {
                Log.e("RecordsViewModel", "Failed to unregister network callback", e)
            }
        }
        networkCallback = null
    }

    override fun onCleared() {
        super.onCleared()
        unregisterNetworkCallback(context = getApplication())
    }

    fun getTags(documentId: String, filterId: String) {
        viewModelScope.launch {
            _selectedTags.value = emptyList()
            val response =
                myFileRepository.getDocument(documentId = documentId, filterId = filterId)?.tags
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

    fun getLocalRecords(filterIds: List<String>, docType: Int = -1, ownerId: String) {
        _getRecordsState.value = GetRecordsState.Loading

        documentType.intValue = docType
        if (::launch.isInitialized) {
            launch.cancel()
        }
        launch = viewModelScope.launch {
            try {
                val documentsFlowResp = if (sortBy.value == DocumentSortEnum.UPLOAD_DATE) {
                    vaultRepository.fetchDocuments(
                        filterIds = filterIds,
                        docType = documentType.intValue,
                        ownerId = ownerId
                    )
                } else {
                    vaultRepository.fetchDocumentsByDocDate(
                        filterIds = filterIds,
                        docType = documentType.intValue,
                        ownerId = ownerId
                    )
                }

                documentsFlowResp
                    .cancellable()
                    .collect { vaultEntities ->
                        val records = vaultEntities.map { vaultEntity ->
                            RecordModel(
                                localId = vaultEntity.localId,
                                documentId = vaultEntity.documentId,
                                ownerId = ownerId,
                                documentType = vaultEntity.documentType,
                                documentDate = vaultEntity.documentDate,
                                createdAt = vaultEntity.createdAt,
                                thumbnail = vaultEntity.thumbnail,
                                filePath = vaultEntity.filePath,
                                fileType = vaultEntity.fileType,
                                cta = Gson().fromJson(vaultEntity.cta, CTA::class.java),
                                tags = vaultEntity.tags,
                                autoTags = vaultEntity.autoTags,
                                source = vaultEntity.source,
                                isAnalyzing = vaultEntity.isAnalyzing
                            )
                        }
                        getAvailableDocTypes(filterIds = filterIds, ownerId = ownerId)
                        _getRecordsState.value = if (records.isEmpty()) {
                            GetRecordsState.EmptyState
                        } else {
                            GetRecordsState.Success(resp = records)
                        }
                    }
            } catch (ex: Exception) {
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
        filterId: String,
        docDate: Long?,
        tags: String,
        ownerId: String,
        allFilterIds: List<String>
    ) {
        try {
            viewModelScope.launch {
                vaultRepository.editDocument(localId, docType, docDate, filterId = filterId)
                val tagList = tags.split(",")
                //  val tagNames = Tags().getTagNamesByIds(tagList)
                val updateFileDetailsRequest = UpdateFileDetailsRequest(
                    filterId = filterId,
                    documentType = docTypes.find { it.idNew == docType }?.id,
                    documentDate = convertLongToFormattedDate(docDate),
                    userTags = emptyList(),
                    linkAbha = false
                )
                cardClickData.value?.documentId?.let {
                    myFileRepository.updateFileDetails(
                        documentId = it,
                        oid = filterId,
                        updateFileDetailsRequest = updateFileDetailsRequest
                    )
                }
                getLocalRecords(filterIds = allFilterIds, ownerId = ownerId)
            }
        } catch (_: Exception) {
        }
    }

    fun deleteDocument(
        localId: String,
        ownerId: String,
        allFilterIds: List<String>
    ) {
        try {
            viewModelScope.launch {
                vaultRepository.deleteDocument(localId = localId)
                getLocalRecords(filterIds = allFilterIds, ownerId = ownerId)
            }
        } catch (_: Exception) {
        }
    }

    fun syncEditedDocuments(filterIds: List<String>, ownerId: String) {
        try {
            viewModelScope.launch {
                vaultRepository.getEditedDocuments(filterIds = filterIds, ownerId = ownerId)
            }
        } catch (_: Exception) {
        }
    }

    fun syncDeletedDocuments(filterIds: List<String>, ownerId: String) {
        try {
            viewModelScope.launch {
                val vaultDocuments =
                    vaultRepository.getDeletedDocuments(ownerId = ownerId, filterIds = filterIds)
                vaultDocuments.forEach { vaultEntity ->
                    vaultEntity.documentId?.let {
                        val resp = myFileRepository.deleteDocument(
                            documentId = it,
                            filterId = vaultEntity.filterId
                        )
                        if (resp in 200..299) {
                            vaultRepository.removeDocument(
                                localId = vaultEntity.localId,
                                filterId = vaultEntity.filterId
                            )
                        }
                    }

                }
            }
        } catch (_: Exception) {
        }
    }

    fun getAvailableDocTypes(filterIds: List<String>, ownerId: String?) {
        try {
            viewModelScope.launch {
                _getAvailableDocTypes.value =
                    GetAvailableDocTypesState(
                        resp = vaultRepository.getAvailableDocTypes(
                            filterIds = filterIds,
                            ownerId = ownerId
                        )
                    )
            }
        } catch (_: Exception) {
        }
    }
}