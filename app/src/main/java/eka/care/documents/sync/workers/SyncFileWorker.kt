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
import eka.care.documents.data.utility.DocumentUtility
import eka.care.documents.data.utility.DocumentUtility.Companion.docTypes
import eka.care.documents.sync.data.remote.dto.request.FileType
import eka.care.documents.sync.data.remote.dto.request.UpdateFileDetailsRequest
import eka.care.documents.sync.data.remote.dto.response.GetFilesResponse
import eka.care.documents.sync.data.repository.AwsRepository
import eka.care.documents.sync.data.repository.MyFileRepository
import eka.care.documents.sync.data.repository.SyncRecordsRepository
import eka.care.documents.ui.utility.RecordsUtility.Companion.changeDateFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
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
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private suspend fun updateFilePath(doctorId: String?, oid: String?) {
        try {
            vaultRepository.getDocumentsWithoutFilePath(doctorId = doctorId, patientOid = oid)
                .forEach { document ->
                    val response = document.documentId?.let {
                        myFileRepository.getDocument(
                            filterId = oid,
                            documentId = it
                        )
                    }
                    Log.d("FILE_PATH_DOCUMENT-3", response.toString())
                    response?.let {
                        val filePaths = it.files.map { file ->
                            downloadFile(file.assetUrl, file.fileType)
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

    private suspend fun downloadFile(assetUrl: String?, type: String): String {
        val directory = ContextWrapper(applicationContext).getDir("cache", Context.MODE_PRIVATE)
        val ext = if (type.trim().lowercase() == "pdf") "pdf" else "jpg"
        val childPath = "${UUID.randomUUID()}.$ext"
        withContext(Dispatchers.IO) {
            val resp = myFileRepository.downloadFile(assetUrl)
            resp?.saveFile(File(directory, childPath))
        }

        return "${directory.path}/$childPath"
    }

    private suspend fun syncDocuments(oid: String?, uuid: String?, doctorId: String?) {
        try {
            val vaultDocuments =
                vaultRepository.getUnSyncedDocuments(oid = oid, doctorId = doctorId)
            if (vaultDocuments.isEmpty()) return

            val tags = mutableListOf<String>()

            vaultDocuments.forEach { vaultEntity ->
                val tagList = vaultEntity.tags?.split(",") ?: emptyList()
                // Process tags if needed, e.g., using:
                // val tagNames = Tags().getTagNamesByIds(tagList)
                // tags.addAll(tagNames)
            }

            vaultDocuments.forEach { vaultEntity ->
                // Prepare file list and metadata for the current document
                val files = vaultEntity.filePath?.map { File(it) }
                val fileContentList = files?.map { file ->
                    FileType(contentType = file.getMimeType() ?: "", fileSize = file.length())
                }
                val isMultiFile =
                    (files?.size ?: 0) > 1 // Determine if the document has multiple files
                val documentType = vaultEntity.documentType?.let { docType ->
                    docTypes.find { it.idNew == docType }?.id ?: "ot"
                } ?: "ot"

                // Initialize the upload for the current document
                if (fileContentList.isNullOrEmpty()) {
                    return
                }
                val uploadInitResponse = oid?.let {
                    uuid?.let {
                        awsRepository.fileUploadInit(
                            files = fileContentList,
                            patientOid = it,
                            patientUuid = uuid,
                            isMultiFile = isMultiFile,
                            tags = tags,
                            documentType = documentType
                        )
                    }
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
                oid = vaultEntity.oid,
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
                updatedAt = updatedAt,
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
                    app_oid = oid
                )
            }

            val newOffset = records?.nextToken
            if (!newOffset.isNullOrEmpty()) {
                fetchRecords(
                    offset = newOffset,
                    updatedAt = updatedAt,
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
        app_oid: String?
    ) {
        val vaultList = mutableListOf<VaultEntity>()
        recordsResponse.items?.forEach {
            val recordItem = it.record.item

            //  val recordCta = recordItem.metadata.cta
            val params = mutableMapOf<String, String>()
//            recordCta.paramsMap.forEach { entry ->
//                params[entry.key] = entry.value.stringValue
//            }
            //        val localCta = CTA(action = recordCta.action, pageId = recordCta.pid, params = params)
            val localId = vaultRepository.getLocalId(recordItem.documentId)
            val documentDate =
                if (recordItem.metadata.documentDate.toLong() == 0L) null else recordItem.metadata.documentDate.toLong()
//            if (!localId.isNullOrEmpty()) {
//                vaultRepository.storeDocument(
//                    localId = localId,
//                    cta = if (localCta.pageId.isNullOrEmpty()) null
//                    else Gson().toJson(localCta, CTA::class.java).toString(),
//                    isAnalysing = recordItem.availableDocumentCase == Records.Record.Item.AvailableDocumentCase.IN_TRANSIT,
//                    docId = recordItem.documentId,
//                    hasId = it.record.hash.toString(),
//                    oid = app_oid,
//                    tags = recordItem.metadata.tagsValueList.joinToString(","),
//                    documentDate = documentDate,
//                )
//            } else if (recordItem.availableDocumentCase != Records.Record.Item.AvailableDocumentCase.IN_TRANSIT && recordItem.documentId != null) {
            vaultList.add(
                VaultEntity(
                    localId = localId ?: UUID.randomUUID().toString(),
                    documentId = recordItem.documentId,
                    ownerId = doctorId,
                    filterId = app_oid,
                    uuid = uuid,
                    oid = app_oid,
                    filePath = null,
                    fileType = "",
                    thumbnail = null,
                    createdAt = recordItem.uploadDate.toLong(),
                    source = null,
                    documentType = docTypes.find { it.id == recordItem.documentType }?.idNew ?: -1,
                    tags = recordItem.metadata?.tags?.joinToString(",") ?: "",
                    documentDate = documentDate,
                    hashId = null,
                    isAnalyzing = false,
                    cta = null,
                    doctorId = doctorId
                )
            )
            //  }
        }

        vaultRepository.storeDocuments(vaultList)
        storeThumbnails(vaultList, recordsResponse)
    }

    private suspend fun storeThumbnails(
        vaultList: List<VaultEntity>,
        recordsResponse: GetFilesResponse?
    ) {
        recordsResponse?.items?.forEach {
            val path = downloadThumbnail(it.record.item.metadata.thumbnail)

            val documentId = it.record.item.documentId
            vaultRepository.setThumbnail(
                path, vaultList.first { it.documentId == documentId }.documentId
            )
        }
        return
    }

    private suspend fun downloadThumbnail(assetUrl: String?): String {
        val directory = ContextWrapper(applicationContext).getDir("imageDir", Context.MODE_PRIVATE)
        val childPath = "image${UUID.randomUUID()}.jpg"
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

fun File.getMimeType(): String? =
    MimeTypeMap.getSingleton().getMimeTypeFromExtension(this.extension)