package eka.care.documents.sync.workers

import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import eka.care.documents.data.db.database.DocumentDatabase
import eka.care.documents.data.db.entity.UpdatedAtEntity
import eka.care.documents.data.db.entity.VaultEntity
import eka.care.documents.data.repository.UpdatedAtRepository
import eka.care.documents.data.repository.UpdatedAtRepositoryImpl
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
import eka.care.documents.ui.utility.RecordsUtility.Companion.changeDateFormat
import eka.care.documents.ui.utility.RecordsUtility.Companion.downloadThumbnail
import eka.care.documents.ui.utility.RecordsUtility.Companion.saveFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class SyncFileWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    private val db = DocumentDatabase.getInstance(applicationContext)
    private val vaultRepository: VaultRepository = VaultRepositoryImpl(db)
    private val updatedAtRepository: UpdatedAtRepository = UpdatedAtRepositoryImpl(db)
    private val awsRepository = AwsRepository()
    private val myFileRepository = MyFileRepository()
    private val recordsRepository = SyncRecordsRepository(appContext as Application)

    override suspend fun doWork(): Result = coroutineScope {
        try {
            val uuid = inputData.getString("p_uuid")
            val oid = inputData.getString("oid")
            val doctorId = inputData.getString("doctorId")
            syncDocuments(oid = oid, uuid = uuid, doctorId = doctorId)
            val updatedAt =
                updatedAtRepository.getUpdatedAtByOid(filterId = oid, ownerId = doctorId)
                    ?: run {
                        updatedAtRepository.insertUpdatedAtEntity(
                            UpdatedAtEntity(
                                filterId = oid ?: "",
                                updatedAt = "0",
                                ownerId = doctorId
                            )
                        )
                        "0"
                    }
            fetchRecords(
                offset = null,
                updatedAt = updatedAt,
                uuid = uuid,
                oid = oid,
                doctorId = doctorId
            )
            updateFilePath(oid = oid, doctorId = doctorId)
            syncDeletedAndEditedDocuments(oid = oid, doctorId = doctorId)
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private suspend fun syncDeletedAndEditedDocuments(oid: String?, doctorId: String?) {
        try {
            val resp = vaultRepository.getEditedDocuments(filterId = oid, ownerId = doctorId)
            resp.forEach { vaultEntity ->
                vaultEntity.documentId?.let {
                    val updateFileDetailsRequest = UpdateFileDetailsRequest(
                        oid = vaultEntity.filterId,
                        documentType = docTypes.find { it.idNew == vaultEntity.documentType }?.id,
                        documentDate = vaultEntity.documentDate.toString(),
                        userTags = emptyList()
                    )
                    myFileRepository.updateFileDetails(
                        documentId = it,
                        oid = oid,
                        updateFileDetailsRequest = updateFileDetailsRequest
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("SyncFileWorker", "Failed to sync edited documents", e)
        }

        try {
            val vaultDocuments = vaultRepository.getDeletedDocuments(ownerId = doctorId, filterId = oid)

            vaultDocuments.forEach { vaultEntity ->
                vaultEntity.documentId?.let {
                    val resp = myFileRepository.deleteDocument(documentId = it, filterId = oid)
                    if (resp in 200..299) {
                        vaultRepository.removeDocument(localId = vaultEntity.localId, filterId = oid)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("SyncFileWorker", "Failed to sync deleted documents", e)
        }
    }

    private suspend fun updateFilePath(doctorId: String?, oid: String?) {
        try {
            vaultRepository.getDocumentsWithoutFilePath(ownerId = doctorId, filterId = oid)
                .forEach { document ->
                    val response = document.documentId?.let {
                        myFileRepository.getDocument(
                            filterId = oid,
                            documentId = it
                        )
                    }
                    response?.let {
                        val filePaths = it.files.map { file ->
                            RecordsUtility.downloadFile(
                                file.assetUrl,
                                context = applicationContext,
                                type = file.fileType
                            )
                        }
                        val fileType = it.files.firstOrNull()?.fileType ?: ""
                        val smartReportField =
                            it.smartReport?.let { report -> Gson().toJson(report) }
                        val updatedDocument = document.copy(
                            filePath = filePaths,
                            fileType = fileType,
                            smartReportField = smartReportField
                        )
                        vaultRepository.updateDocuments(listOf(updatedDocument))
                    }
                }
        } catch (_: Exception) {
        }
    }

    private suspend fun syncDocuments(oid: String?, uuid: String?, doctorId: String?) {
        try {
            val vaultDocuments =
                vaultRepository.getUnSyncedDocuments(filterId = oid, ownerId = doctorId)
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
                val uploadInitResponse = uuid?.let {
                    awsRepository.fileUploadInit(
                        files = fileContentList,
                        patientOid = oid,
                        patientUuid = uuid,
                        isMultiFile = isMultiFile,
                        tags = tags,
                        documentType = documentType
                    )
                }

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
                                updateDocumentDetails(docId, oid, vaultEntity)
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
                                    updateDocumentDetails(docId, oid, vaultEntity)
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


    private suspend fun updateDocumentDetails(
        documentId: String,
        oid: String?,
        vaultEntity: VaultEntity
    ) {
        vaultRepository.updateDocumentId(documentId, vaultEntity.localId)
        try {
            val documentDate = vaultEntity.documentDate?.let { timestamp ->
                val date = Date(timestamp * 1000)
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
            } ?: ""

            val updateFileDetailsRequest = UpdateFileDetailsRequest(
                oid = vaultEntity.filterId,
                documentType = docTypes.find { it.idNew == vaultEntity.documentType }?.id,
                documentDate = if (documentDate.isNotEmpty()) changeDateFormat(documentDate) else null,
                userTags = emptyList()
            )

            myFileRepository.updateFileDetails(
                documentId = documentId,
                oid = oid,
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
        offset: String?,
        updatedAt: String?,
        uuid: String?,
        oid: String?,
        doctorId: String?
    ) {
        try {
            val response = recordsRepository.getRecords(
                updatedAt = null,
                offset = offset,
                oid = oid
            )
            // eka-uat of latest updated or inserted record
            val ekaUat = response?.headers()?.get("Eka-Uat")
            if (ekaUat != null) {
                updatedAtRepository.updateUpdatedAtByOid(
                    filterId = oid,
                    updatedAt = ekaUat,
                    ownerId = doctorId
                )
            }

            val records = response?.body()

            if (records != null) {
                storeRecords(
                    recordsResponse = records,
                    doctorId = doctorId,
                    uuid = uuid,
                    app_oid = oid,
                    context = applicationContext
                )
            }

            val newOffset = records?.nextToken
            if (!newOffset.isNullOrEmpty()) {
                fetchRecords(
                    offset = newOffset,
                    updatedAt = null,
                    uuid = uuid,
                    oid = oid,
                    doctorId = doctorId
                )
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("SYNC_DOCUMENTS", "Error fetching documents: ${e.message}", e)
        }
    }


    private suspend fun storeRecords(
        recordsResponse: GetFilesResponse,
        doctorId: String?,
        uuid: String?,
        app_oid: String?,
        context: Context
    ) {
        val vaultList = mutableListOf<VaultEntity>()
        recordsResponse.items?.forEach {
            val recordItem = it.record.item
            val localId = vaultRepository.getLocalId(recordItem.documentId)
            val documentDate =
                if (recordItem.metadata.documentDate.toLong() == 0L) null else recordItem.metadata.documentDate.toLong()
            if (!localId.isNullOrEmpty()) {
                vaultRepository.storeDocument(
                    localId = localId,
                    cta = null,
                    isAnalysing = false,
                    docId = recordItem.documentId,
                    hasId = "",
                    filterId = app_oid,
                    tags = recordItem.metadata?.tags?.joinToString(",") ?: "",
                    autoTags = recordItem.metadata?.autoTags?.joinToString(",") ?: "",
                    documentDate = documentDate,
                )
            } else {
                vaultList.add(
                    VaultEntity(
                        localId = localId ?: UUID.randomUUID().toString(),
                        documentId = recordItem.documentId,
                        ownerId = recordItem.patientId,
                        filterId = app_oid,
                        uuid = uuid,
                        filePath = null,
                        fileType = "",
                        thumbnail = null,
                        createdAt = recordItem.uploadDate.toLong(),
                        source = null,
                        documentType = docTypes.find { it.id == recordItem.documentType }?.idNew
                            ?: -1,
                        tags = recordItem.metadata?.tags?.joinToString(",") ?: "",
                        documentDate = documentDate,
                        hashId = null,
                        isAnalyzing = false,
                        cta = null,
                        autoTags = recordItem.metadata?.autoTags?.joinToString(",") ?: ""
                    )
                )
            }
        }

        vaultRepository.storeDocuments(vaultList)
        storeThumbnails(vaultList = vaultList, recordsResponse =  recordsResponse, context = context)
    }

    private suspend fun storeThumbnails(
        vaultList: List<VaultEntity>,
        recordsResponse: GetFilesResponse?,
        context: Context
    ) {
        recordsResponse?.items?.forEach {
            val path = downloadThumbnail(it.record.item.metadata.thumbnail, context = context)
            val documentId = it.record.item.documentId
            vaultList.find { it.documentId == documentId }?.documentId?.let { vaultDocId ->
                vaultRepository.setThumbnail(path, vaultDocId)
            }
        }
        return
    }

}

fun File.getMimeType(): String? =
    MimeTypeMap.getSingleton().getMimeTypeFromExtension(this.extension)