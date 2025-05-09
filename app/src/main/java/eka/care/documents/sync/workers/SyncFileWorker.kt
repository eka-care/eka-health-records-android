package eka.care.documents.sync.workers

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import eka.care.documents.data.db.database.DocumentDatabase
import eka.care.documents.data.db.entity.VaultEntity
import eka.care.documents.data.repository.VaultRepository
import eka.care.documents.data.repository.VaultRepositoryImpl
import eka.care.documents.data.utility.DocumentUtility.Companion.docTypes
import eka.care.documents.sync.data.remote.dto.request.FileType
import eka.care.documents.sync.data.remote.dto.request.UpdateFileDetailsRequest
import eka.care.documents.sync.data.remote.dto.response.GetFilesResponse
import eka.care.documents.sync.data.repository.AwsRepository
import eka.care.documents.sync.data.repository.MyFileRepository
import eka.care.documents.sync.data.repository.SyncRecordsRepository
import eka.care.documents.ui.utility.RecordsUtility
import eka.care.documents.ui.utility.RecordsUtility.Companion.downloadThumbnail
import eka.care.documents.ui.utility.RecordsUtility.Companion.getMimeType
import kotlinx.coroutines.coroutineScope
import java.io.File
import java.util.UUID

class SyncFileWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    private val db = DocumentDatabase.getInstance(applicationContext)
    private val vaultRepository: VaultRepository = VaultRepositoryImpl(db)
    private val awsRepository = AwsRepository()
    private val myFileRepository = MyFileRepository()
    private val recordsRepository = SyncRecordsRepository(appContext as Application)

    override suspend fun doWork(): Result = coroutineScope {
        try {
            val ownerId = inputData.getString("ownerId") ?: return@coroutineScope Result.failure()
            val filterIds = inputData.getString("filterIds")?.split(",") ?: emptyList()

            filterIds.forEach { filterId ->
                //    val updatedAt = vaultRepository.getUpdatedAtByOid(filterId = filterId,ownerId = ownerId)
                fetchRecords(filterId = filterId, ownerId = ownerId)
                //         vaultRepository.updateUpdatedAtByOid(filterId = filterId, ownerId = ownerId, updatedAt = System.currentTimeMillis())
            }

            syncDocuments(filterIds, ownerId)
            updateFilePath(ownerId = ownerId)
            syncDeletedAndEditedDocuments(filterIds, ownerId)

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private suspend fun syncDocuments(filterIds: List<String>?, ownerId: String) {
        try {
            val vaultDocuments =
                vaultRepository.getUnSyncedDocuments(filterIds = filterIds, ownerId = ownerId)
            if (vaultDocuments.isEmpty()) return

            val tags = mutableListOf<String>()

            vaultDocuments.forEach { vaultEntity ->
                val files = vaultEntity.filePath?.map { File(it) }
                val fileContentList = files?.map { file ->
                    FileType(contentType = file.getMimeType() ?: "", fileSize = file.length())
                }
                val isMultiFile = (files?.size ?: 0) > 1
                val documentType = vaultEntity.documentType?.let { docType ->
                    docTypes.find { it.idNew == docType }?.id ?: "ot"
                } ?: "ot"

                if (fileContentList.isNullOrEmpty()) {
                    return
                }
                val uploadInitResponse = awsRepository.fileUploadInit(
                    files = fileContentList,
                    patientOid = vaultEntity.filterId,
                    isMultiFile = isMultiFile,
                    tags = tags,
                    documentType = documentType
                )
                if (uploadInitResponse?.error == true) {
                    Log.d(
                        "SYNC_DOCUMENTS",
                        "Upload initialization error: ${uploadInitResponse.message}"
                    )
                    return
                }

                val batchResponses = uploadInitResponse?.batchResponse ?: emptyList()

                if (isMultiFile) {
                    // Handle multi-file upload for the current document
                    val batchResponse = batchResponses.firstOrNull()
                    if (batchResponse != null) {
                        val response =
                            awsRepository.uploadFile(batch = batchResponse, fileList = files)
                        if (response?.error == false) {
                            response.documentId?.let { docId ->
                                updateDocumentDetails(docId, vaultEntity.filterId, vaultEntity)
                            }
                        }
                    }
                } else {
                    // Handle single-file upload for the current document
                    vaultEntity.filePath?.forEachIndexed { index, path ->
                        val file = File(path)
                        val batchResponse = batchResponses.getOrNull(index)
                        if (batchResponse != null) {
                            val response =
                                awsRepository.uploadFile(file = file, batch = batchResponse)
                            if (response?.error == false) {
                                response.documentId?.let { docId ->
                                    updateDocumentDetails(docId, vaultEntity.filterId, vaultEntity)
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("SYNC_DOCUMENTS", "Error syncing documents: ${e.message}", e)
        }
    }

    private suspend fun syncDeletedAndEditedDocuments(filterIds: List<String>?, ownerId: String) {
        try {
            val resp = vaultRepository.getEditedDocuments(filterIds = filterIds, ownerId = ownerId)
            resp.forEach { vaultEntity ->
                vaultEntity.documentId?.let {
                    val updateFileDetailsRequest = UpdateFileDetailsRequest(
                        filterId = vaultEntity.filterId,
                        documentType = docTypes.find { it.idNew == vaultEntity.documentType }?.id,
                        documentDate = vaultEntity.documentDate,
                        userTags = emptyList()
                    )
                    myFileRepository.updateFileDetails(
                        documentId = it,
                        oid = vaultEntity.filterId,
                        updateFileDetailsRequest = updateFileDetailsRequest
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("SyncFileWorker", "Failed to sync edited documents", e)
        }

        try {
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
        } catch (e: Exception) {
            Log.e("SyncFileWorker", "Failed to sync deleted documents", e)
        }
    }

    private suspend fun updateDocumentDetails(
        documentId: String,
        filterId: String?,
        vaultEntity: VaultEntity
    ) {
        vaultRepository.updateDocumentId(documentId, vaultEntity.localId)
        try {
            val updateFileDetailsRequest = UpdateFileDetailsRequest(
                filterId = vaultEntity.filterId,
                documentType = docTypes.find { it.idNew == vaultEntity.documentType }?.id,
                documentDate = if (vaultEntity.documentDate != null) vaultEntity.documentDate else null,
                userTags = emptyList()
            )

            myFileRepository.updateFileDetails(
                documentId = documentId,
                oid = filterId,
                updateFileDetailsRequest = updateFileDetailsRequest
            )
        } catch (e: Exception) {
            Log.d(
                "SYNC_DOCUMENTS",
                "Update File Detail: ${e.message.toString()}"
            )
        }
    }

    private suspend fun fetchRecords(
        offset: String? = null,
        updatedAt: Long? = null,
        filterId: String?,
        ownerId: String
    ) {
        var currentOffset = offset
        do {
            try {
                val response = recordsRepository.getRecords(
                    updatedAt = updatedAt,
                    offset = currentOffset,
                    oid = filterId
                )

                if (response == null) {
                    Log.w("SYNC_DOCUMENTS", "No response for filterId: $filterId")
                    break
                }

                val records = response.body()

                if (records != null) {
                    storeRecords(
                        recordsResponse = records,
                        ownerId = ownerId,
                        context = applicationContext
                    )
                }

                currentOffset = records?.nextToken

            } catch (e: Exception) {
                Log.e(
                    "SYNC_DOCUMENTS",
                    "Error fetching documents for filterId: $filterId, error: ${e.message}",
                    e
                )
                break
            }
        } while (!currentOffset.isNullOrEmpty())
    }

    private suspend fun storeRecords(
        recordsResponse: GetFilesResponse,
        ownerId: String?,
        context: Context
    ) {
        val vaultList = mutableListOf<VaultEntity>()
        recordsResponse.items.forEach {
            val recordItem = it.record.item
            val entity = vaultRepository.getDocumentById(recordItem.documentId)
            val documentDate =
                if (recordItem.metadata?.documentDate == 0L) entity?.documentDate else recordItem.metadata?.documentDate
            if (!entity?.localId.isNullOrEmpty()) {
                vaultRepository.storeDocument(
                    localId = entity?.localId ?: "",
                    cta = null,
                    isAnalysing = false,
                    docId = recordItem.documentId,
                    hasId = "",
                    filterId = recordItem.patientId,
                    tags = recordItem.metadata?.tags?.joinToString(",") ?: "",
                    autoTags = recordItem.metadata?.autoTags?.joinToString(",") ?: "",
                    documentDate = documentDate,
                )
            } else {
                vaultList.add(
                    VaultEntity(
                        localId = entity?.localId ?: UUID.randomUUID().toString(),
                        documentId = recordItem.documentId,
                        ownerId = ownerId,
                        filterId = recordItem.patientId,
                        filePath = null,
                        fileType = "",
                        thumbnail = null,
                        createdAt = recordItem.uploadDate ?: 0L,
                        source = null,
                        documentType = docTypes.find { it.id == recordItem.documentType }?.idNew
                            ?: -1,
                        tags = recordItem.metadata?.tags?.joinToString(",") ?: "",
                        documentDate = if (recordItem.metadata?.documentDate == 0L) null else recordItem.metadata?.documentDate,
                        hashId = null,
                        isAnalyzing = false,
                        cta = null,
                        autoTags = recordItem.metadata?.autoTags?.joinToString(",") ?: ""
                    )
                )
            }
        }

        vaultRepository.storeDocuments(vaultList)
        storeThumbnails(vaultList = vaultList, recordsResponse = recordsResponse, context = context)
    }

    private suspend fun updateFilePath(ownerId: String) {
        try {
            val documentsWithoutPath = vaultRepository.getDocumentsWithoutFilePath(
                ownerId = ownerId
            )
            for (document in documentsWithoutPath) {
                val documentId = document.documentId ?: continue
                val vaultEntity = vaultRepository.getDocumentById(id = documentId)
                val response = myFileRepository.getDocument(
                    filterId = vaultEntity?.filterId,
                    documentId = documentId
                )

                if (response != null) {
                    val filePaths = ArrayList<String>(response.files.size)
                    val fileType = response.files.firstOrNull()?.fileType ?: ""

                    for (file in response.files) {
                        val filePath = RecordsUtility.downloadFile(
                            file.assetUrl,
                            context = applicationContext,
                            type = file.fileType
                        )
                        filePaths.add(filePath)
                    }

                    val smartReportField = response.smartReport?.let {
                        Gson().toJson(it)
                    }

                    val updatedDocument = document.copy(
                        filePath = filePaths,
                        fileType = fileType,
                        smartReportField = smartReportField
                    )

                    vaultRepository.updateDocuments(listOf(updatedDocument))
                }
            }
        } catch (e: Exception) {
            Log.e("UpdateFilePath", "Error updating file paths", e)
        }
    }

    private suspend fun storeThumbnails(
        vaultList: List<VaultEntity>,
        recordsResponse: GetFilesResponse?,
        context: Context
    ) {
        recordsResponse?.items?.forEach {
            val path = downloadThumbnail(it.record.item.metadata?.thumbnail, context = context)
            val documentId = it.record.item.documentId
            vaultList.find { it.documentId == documentId }?.documentId?.let { vaultDocId ->
                vaultRepository.setThumbnail(path, vaultDocId)
            }
        }
        return
    }

}