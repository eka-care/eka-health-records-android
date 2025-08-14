package eka.care.records.data.repository

import android.content.Context
import androidx.sqlite.db.SupportSQLiteQueryBuilder
import com.google.gson.Gson
import eka.care.records.client.model.CaseModel
import eka.care.records.client.model.DocumentTypeCount
import eka.care.records.client.model.EventLog
import eka.care.records.client.model.RecordModel
import eka.care.records.client.model.RecordStatus
import eka.care.records.client.model.SortOrder
import eka.care.records.client.repository.RecordsRepository
import eka.care.records.client.utils.Records
import eka.care.records.client.utils.RecordsUtility
import eka.care.records.client.utils.RecordsUtility.Companion.getMimeType
import eka.care.records.client.utils.RecordsUtility.Companion.md5
import eka.care.records.data.core.FileStorageManagerImpl
import eka.care.records.data.db.RecordsDatabase
import eka.care.records.data.entity.CaseEntity
import eka.care.records.data.entity.CaseRecordRelationEntity
import eka.care.records.data.entity.RecordEntity
import eka.care.records.data.entity.RecordFile
import eka.care.records.data.entity.toCaseModel
import eka.care.records.data.remote.dto.request.CaseRequest
import eka.care.records.data.remote.dto.request.FileType
import eka.care.records.data.remote.dto.request.UpdateFileDetailsRequest
import eka.care.records.data.utility.LoggerConstant.Companion.CASE_ID
import eka.care.records.data.utility.LoggerConstant.Companion.CASE_NAME
import eka.care.records.data.utility.LoggerConstant.Companion.CASE_TYPE
import eka.care.records.data.utility.LoggerConstant.Companion.DOCUMENT_DATE
import eka.care.records.data.utility.LoggerConstant.Companion.DOCUMENT_ID
import eka.care.records.data.utility.LoggerConstant.Companion.DOCUMENT_TYPE
import eka.care.records.data.utility.LoggerConstant.Companion.FILTER_ID
import eka.care.records.data.utility.LoggerConstant.Companion.OWNER_ID
import eka.care.records.data.utility.isNetworkAvailable
import id.zelory.compressor.Compressor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import org.json.JSONObject
import java.io.File
import java.util.UUID

internal class RecordsRepositoryImpl(private val context: Context) : RecordsRepository {
    private var dao = RecordsDatabase.getInstance(context).recordsDao()
    private val myFileRepository = MyFileRepository()
    private val fileStorageManager = FileStorageManagerImpl(context)
    private val encountersRepository = EncountersRepository()
    private val awsRepository = AwsRepository()
    private var syncJob: Job? = null

    fun startAutoSync(ownerId: String) {
        syncJob?.cancel()
        syncJob = CoroutineScope(Dispatchers.IO).launch {
            dao.getDirtyRecords(ownerId)?.also {
                syncUpdatedRecordsToServer(it)
            }
            dao.getDeletedRecords(ownerId)?.also {
                syncDeletedRecordsToServer(it)
            }
            dao.getUnsyncedCases(ownerId)?.also {
                syncCasesToServer(it)
            }
            dao.getDirtyCases(ownerId)?.also {
                syncCasesToServer(it)
            }
        }
    }

    private suspend fun syncUpdatedRecordsToServer(dirtyRecords: List<RecordEntity>) =
        supervisorScope {
            dirtyRecords.forEach { record ->
                launch {
                    val documentId = record.documentId
                    if (documentId != null) {
                        if (isNetworkAvailable(context = context)) {
                            dao.updateRecords(listOf(record.copy(status = RecordStatus.SYNCING)))
                        } else {
                            dao.updateRecords(listOf(record.copy(status = RecordStatus.WAITING_FOR_NETWORK)))
                            return@launch
                        }
                        val result = myFileRepository.updateFileDetails(
                            documentId = documentId,
                            oid = record.filterId,
                            request = UpdateFileDetailsRequest(
                                filterId = record.filterId,
                                documentType = record.documentType,
                                documentDate = record.documentDate,
                            )
                        )
                        result?.let {
                            if (it !in 200..299) {
                                Records.logEvent(
                                    EventLog(
                                        params = JSONObject().also { param ->
                                            param.put(DOCUMENT_ID, documentId)
                                            param.put(OWNER_ID, record.ownerId)
                                            param.put(FILTER_ID, record.filterId)
                                        },
                                        message = "Syncing failed code: $it",
                                    )
                                )
                                dao.updateRecords(listOf(record.copy(status = RecordStatus.SYNC_FAILED)))
                            } else {
                                Records.logEvent(
                                    EventLog(
                                        params = JSONObject().also { param ->
                                            param.put(DOCUMENT_ID, documentId)
                                            param.put(OWNER_ID, record.ownerId)
                                            param.put(FILTER_ID, record.filterId)
                                        },
                                        message = "Syncing dirty record success: $documentId",
                                    )
                                )
                                dao.updateRecords(
                                    listOf(
                                        record.copy(
                                            status = RecordStatus.SYNC_SUCCESS,
                                            isDirty = false
                                        )
                                    )
                                )
                                Records.logEvent(
                                    EventLog(
                                        params = JSONObject().also { param ->
                                            param.put(DOCUMENT_ID, documentId)
                                            param.put(OWNER_ID, record.ownerId)
                                            param.put(FILTER_ID, record.filterId)
                                        },
                                        message = "DB updated successfully: $documentId",
                                    )
                                )
                            }
                        }
                    } else {
                        uploadRecord(id = record.id)
                    }
                }
            }
        }

    private suspend fun syncDeletedRecordsToServer(deletedRecords: List<RecordEntity>) =
        supervisorScope {
            deletedRecords.forEach { record ->
                launch {
                    record.documentId?.let { documentId ->
                        val result = myFileRepository.deleteDocument(documentId, record.filterId)
                        if (result in (200..299)) {
                            dao.deleteRecord(record)
                            Records.logEvent(
                                EventLog(
                                    params = JSONObject().also { param ->
                                        param.put(DOCUMENT_ID, documentId)
                                        param.put(OWNER_ID, record.ownerId)
                                        param.put(FILTER_ID, record.filterId)
                                    },
                                    message = "Syncing deleted record success: $documentId",
                                )
                            )
                        } else {
                            Records.logEvent(
                                EventLog(
                                    params = JSONObject().also { param ->
                                        param.put(DOCUMENT_ID, documentId)
                                        param.put(OWNER_ID, record.ownerId)
                                        param.put(FILTER_ID, record.filterId)
                                    },
                                    message = "Syncing failed code: $result",
                                )
                            )
                        }
                    }
                }
            }
        }

    private suspend fun syncCasesToServer(cases: List<CaseEntity>) = supervisorScope {
        val event = EventLog(
            params = JSONObject(),
            message = ""
        )
        cases.forEach { case ->
            launch {
                if (isNetworkAvailable(context = context)) {
                    val response = encountersRepository.createCase(
                        patientId = case.filterId,
                        caseRequest = CaseRequest(
                            caseId = case.caseId,
                            name = case.name,
                            caseType = case.caseType,
                            occurredAt = case.createdAt
                        )
                    )
                    if (response != null) {
                        dao.updateCase(
                            case.copy(
                                isSynced = true,
                                isDirty = false,
                                isArchived = false,
                                caseId = response.caseId
                            )
                        )
                        Records.logEvent(
                            EventLog(
                                params = event.params.also { param ->
                                    param.put(OWNER_ID, case.ownerId)
                                    param.put(FILTER_ID, case.filterId)
                                    param.put(CASE_ID, case.caseId)
                                },
                                message = "Syncing case success: ${response.caseId}",
                            )
                        )
                    } else {
                        Records.logEvent(
                            EventLog(
                                params = JSONObject().also { param ->
                                    param.put(OWNER_ID, case.ownerId)
                                    param.put(FILTER_ID, case.filterId)
                                    param.put(CASE_ID, case.caseId)
                                },
                                message = "Syncing case failed: ${case.caseId}",
                            )
                        )
                    }
                } else {
                    Records.logEvent(
                        EventLog(
                            params = JSONObject().also { param ->
                                param.put(OWNER_ID, case.ownerId)
                                param.put(FILTER_ID, case.filterId)
                                param.put(CASE_ID, case.caseId)
                            },
                            message = "Syncing case failed due to no network: ${case.caseId}",
                        )
                    )
                }
            }
        }
    }

    override suspend fun createRecords(records: List<RecordEntity>) {
        records.forEach {
            dao.insertRecordWithFiles(it, emptyList())
        }
    }

    override suspend fun createRecord(
        files: List<File>,
        ownerId: String,
        filterId: String?,
        caseId: String?,
        documentType: String,
        documentDate: Long?,
        tags: List<String>
    ): String? = supervisorScope {
        if (files.isEmpty()) {
            Records.logEvent(
                EventLog(
                    message = "No files to create records",
                )
            )
            return@supervisorScope null
        }

        val time = System.currentTimeMillis() / 1000
        val id = UUID.randomUUID().toString()
        val thumbnail = if (files.first().extension.lowercase() in listOf(
                "jpg",
                "jpeg",
                "png",
                "webp"
            )
        ) {
            files.first().path
        } else {
            fileStorageManager.generateThumbnail(
                filePath = files.first().path
            )
        }
        val record = RecordEntity(
            id = id,
            ownerId = ownerId,
            filterId = filterId,
            thumbnail = thumbnail,
            documentType = documentType,
            createdAt = time,
            updatedAt = time,
            documentDate = documentDate ?: time,
            documentHash = files.first().md5(),
            isDirty = true,
            status = RecordStatus.WAITING_TO_UPLOAD
        )
        val files = files.map { file ->
            val compressedFile =
                if (file.extension.lowercase() == "pdf") file else Compressor.compress(
                    context,
                    file
                )
            val path = compressedFile.path
            val type = compressedFile.extension
            RecordFile(
                localId = record.id,
                filePath = path,
                fileType = type
            )
        }
        dao.insertRecordWithFiles(
            record = record,
            files = files
        )
        caseId?.let {
            insertRecordIntoCase(
                caseId = caseId,
                documentId = id,
            )
        }

        return@supervisorScope id
    }

    override fun readRecords(
        ownerId: String,
        filterIds: List<String>?,
        caseId: String?,
        includeDeleted: Boolean,
        documentType: String?,
        sortOrder: SortOrder,
    ): Flow<List<RecordModel>> = flow {
        Records.logEvent(
            EventLog(
                params = JSONObject().also { param ->
                    param.put(OWNER_ID, ownerId)
                    param.put(FILTER_ID, filterIds?.joinToString(","))
                },
                message = "Reading records with ownerId: $ownerId, filterIds: ${
                    filterIds?.joinToString(
                        ","
                    )
                }"
            )
        )
        try {
            val selection = StringBuilder()
            val selectionArgs = mutableListOf<String>()

            if (!includeDeleted) {
                selection.append("IS_ARCHIVED = 0 AND ")
            }

            selection.append("OWNER_ID = ? ")
            selectionArgs.add(ownerId)

            if (!documentType.isNullOrEmpty()) {
                selection.append("AND DOCUMENT_TYPE = ? ")
                selectionArgs.add(documentType)
            }

            if (!filterIds.isNullOrEmpty()) {
                val placeholders = filterIds.joinToString(",") { "?" }
                selection.append("AND (FILTER_ID IN ($placeholders) OR FILTER_ID IS NULL) ")
                selectionArgs.addAll(filterIds)
            } else {
                selection.append("AND FILTER_ID IS NULL ")
            }

            val query = SupportSQLiteQueryBuilder
                .builder("EKA_RECORDS_TABLE")
                .selection(selection.toString().trim(), selectionArgs.toTypedArray())
                .orderBy("${sortOrder.value} ${sortOrder.order}")
                .create()

            if (caseId != null) {
                val dataFlow = getCaseWithRecords(caseId).map {
                    it?.records?.map { record ->
                        RecordModel(
                            id = record.id,
                            thumbnail = record.thumbnail ?: dao.getRecordFile(record.id)
                                ?.firstOrNull()?.filePath,
                            status = record.status,
                            createdAt = record.createdAt,
                            updatedAt = record.updatedAt,
                            documentType = record.documentType,
                            documentDate = record.documentDate,
                            isSmart = record.isSmart,
                            smartReport = record.smartReport
                        )
                    } ?: emptyList()
                }
                emitAll(dataFlow)
            } else {
                val dataFlow = dao.readRecords(query).map { records ->
                    records.map {
                        RecordModel(
                            id = it.id,
                            thumbnail = it.thumbnail ?: dao.getRecordFile(it.id)
                                ?.firstOrNull()?.filePath,
                            status = it.status,
                            createdAt = it.createdAt,
                            updatedAt = it.updatedAt,
                            documentType = it.documentType,
                            documentDate = it.documentDate,
                            isSmart = it.isSmart,
                            smartReport = it.smartReport
                        )
                    }
                }
                emitAll(dataFlow)
            }
        } catch (e: Exception) {
            Records.logEvent(
                EventLog(
                    params = JSONObject().also { param ->
                        param.put(OWNER_ID, ownerId)
                        param.put(FILTER_ID, filterIds?.joinToString(","))
                    },
                    message = "Error reading records: ${e.localizedMessage}",
                )
            )
            emit(emptyList())
        }
    }

    override suspend fun getRecordById(id: String) = dao.getRecordById(id = id)

    override suspend fun getRecordByDocumentId(id: String) = dao.getRecordByDocumentId(id = id)

    override suspend fun getCaseByCaseId(id: String) = dao.getCaseByCaseId(id)

    override suspend fun getRecordDetails(id: String): RecordModel? {
        val record = getRecordById(id)
        if (record == null) {
            Records.logEvent(
                EventLog(
                    params = JSONObject().also { param ->
                        param.put(DOCUMENT_ID, id)
                    },
                    message = "Error fetching record details for: $id",
                )
            )
            return null
        }
        val documentId = record.documentId

        val files = getRecordFile(record.id)
        if (files?.isNotEmpty() == true && !record.isSmart) {
            Records.logEvent(
                EventLog(
                    params = JSONObject().also { param ->
                        param.put(DOCUMENT_ID, documentId)
                        param.put(OWNER_ID, record.ownerId)
                        param.put(FILTER_ID, record.filterId)
                    },
                    message = "Found local files for record: $id",
                )
            )
            return RecordModel(
                id = record.id,
                thumbnail = record.thumbnail,
                createdAt = record.createdAt,
                updatedAt = record.updatedAt,
                documentDate = record.documentDate,
                documentType = record.documentType,
                isSmart = record.isSmart,
                smartReport = record.smartReport,
                status = record.status,
                files = files.map { file ->
                    RecordModel.RecordFile(
                        id = file.id,
                        filePath = file.filePath,
                        fileType = file.fileType
                    )
                }
            )
        }

        if (record.smartReport?.isNotEmpty() == true) {
            Records.logEvent(
                EventLog(
                    params = JSONObject().also { param ->
                        param.put(DOCUMENT_ID, documentId)
                        param.put(OWNER_ID, record.ownerId)
                        param.put(FILTER_ID, record.filterId)
                    },
                    message = "Found smart report for record: $id",
                )
            )
            return RecordModel(
                id = record.id,
                thumbnail = record.thumbnail,
                createdAt = record.createdAt,
                updatedAt = record.updatedAt,
                documentDate = record.documentDate,
                documentType = record.documentType,
                isSmart = record.isSmart,
                smartReport = record.smartReport,
                status = record.status,
                files = files?.map { file ->
                    RecordModel.RecordFile(
                        id = file.id,
                        filePath = file.filePath,
                        fileType = file.fileType
                    )
                } ?: emptyList()
            )
        }

        if (documentId == null) {
            Records.logEvent(
                EventLog(
                    params = JSONObject().also { param ->
                        param.put(DOCUMENT_ID, documentId)
                        param.put(OWNER_ID, record.ownerId)
                        param.put(FILTER_ID, record.filterId)
                    },
                    message = "Found smart report for record: $id",
                )
            )
            return null
        }

        val response = myFileRepository.getDocument(
            documentId = documentId,
            filterId = record.filterId
        )
        if (response == null) {
            Records.logEvent(
                EventLog(
                    params = JSONObject().also { param ->
                        param.put(DOCUMENT_ID, documentId)
                        param.put(OWNER_ID, record.ownerId)
                        param.put(FILTER_ID, record.filterId)
                    },
                    message = "Error fetching document details for: $documentId",
                )
            )
            return null
        }

        val smartReportField = response.smartReport?.let {
            Gson().toJson(it)
        }
        val updatedRecord = record.copy(
            smartReport = smartReportField
        )
        updateRecords(listOf(updatedRecord))
        response.files.forEach { file ->
            val fileType = response.files.firstOrNull()?.fileType ?: ""
            val filePath = RecordsUtility.downloadFile(
                file.assetUrl,
                context = context.applicationContext,
                type = file.fileType
            )
            Records.logEvent(
                EventLog(
                    params = JSONObject().also { param ->
                        param.put(DOCUMENT_ID, documentId)
                        param.put(OWNER_ID, record.ownerId)
                        param.put(FILTER_ID, record.filterId)
                    },
                    message = "Downloaded file: $filePath for record: $documentId",
                )
            )
            insertRecordFile(
                RecordFile(
                    localId = record.id,
                    filePath = filePath,
                    fileType = fileType
                )
            )
            Records.logEvent(
                EventLog(
                    params = JSONObject().also { param ->
                        param.put(DOCUMENT_ID, documentId)
                        param.put(OWNER_ID, record.ownerId)
                        param.put(FILTER_ID, record.filterId)
                    },
                    message = "Inserted file: $filePath for record: $documentId",
                )
            )
        }
        return RecordModel(
            id = record.id,
            thumbnail = record.thumbnail,
            createdAt = record.createdAt,
            updatedAt = record.updatedAt,
            documentDate = record.documentDate,
            documentType = record.documentType,
            isSmart = record.isSmart,
            smartReport = smartReportField,
            files = getRecordFile(record.id)?.map { file ->
                RecordModel.RecordFile(
                    id = file.id,
                    filePath = file.filePath,
                    fileType = file.fileType
                )
            } ?: emptyList()
        )
    }

    override fun getRecordTypeCounts(
        ownerId: String,
        filterIds: List<String>?
    ): Flow<List<DocumentTypeCount>> = flow {
        try {
            val selection = StringBuilder()
            val selectionArgs = mutableListOf<String>()

            selection.append("IS_ARCHIVED = 0 AND ")
            selection.append("OWNER_ID = ? ")
            selectionArgs.add(ownerId)

            if (!filterIds.isNullOrEmpty()) {
                val placeholders = filterIds.joinToString(",") { "?" }
                selection.append("AND (FILTER_ID IN ($placeholders) OR FILTER_ID IS NULL) ")
                selectionArgs.addAll(filterIds)
            } else {
                selection.append("AND FILTER_ID IS NULL ")
            }

            val query = SupportSQLiteQueryBuilder
                .builder("EKA_RECORDS_TABLE")
                .columns(arrayOf("document_type as documentType", "COUNT(local_id) as count"))
                .selection(selection.toString().trim(), selectionArgs.toTypedArray())
                .groupBy("document_type")
                .create()

            emitAll(dao.getDocumentTypeCounts(query))
        } catch (e: Exception) {
            Records.logEvent(
                EventLog(
                    params = JSONObject().also { param ->
                        param.put(OWNER_ID, ownerId)
                        param.put(FILTER_ID, filterIds?.joinToString(","))
                    },
                    message = "Error getting record type counts: ${e.localizedMessage}",
                )
            )
            emit(emptyList())
        }
    }

    override suspend fun updateRecords(records: List<RecordEntity>) {
        dao.updateRecords(records)
    }

    override suspend fun updateRecord(
        id: String,
        caseId: String?,
        documentDate: Long?,
        documentType: String?
    ): String? {
        if (documentDate == null && documentType == null) {
            return null
        }

        val record = getRecordById(id) ?: return null
        val updatedRecord = record.copy(
            documentDate = documentDate ?: record.documentDate,
            documentType = documentType ?: record.documentType,
            isDirty = true
        )
        dao.updateRecords(listOf(updatedRecord))
        caseId?.let {
            dao.updateCaseRecordRelation(
                CaseRecordRelationEntity(
                    caseId = it,
                    recordId = id,
                )
            )
        }
        Records.logEvent(
            EventLog(
                params = JSONObject().also { param ->
                    param.put(DOCUMENT_ID, id)
                    param.put(OWNER_ID, record.ownerId)
                    param.put(FILTER_ID, record.filterId)
                    param.put(DOCUMENT_DATE, documentDate)
                    param.put(DOCUMENT_TYPE, documentType)
                },
                message = "Update record: $updatedRecord"
            )
        )
        return record.id
    }

    override suspend fun deleteRecords(ids: List<String>) {
        ids.forEach { id ->
            val record = getRecordById(id)
            Records.logEvent(
                EventLog(
                    params = JSONObject().also { param ->
                        param.put(DOCUMENT_ID, record?.documentId)
                        param.put(OWNER_ID, record?.ownerId)
                        param.put(FILTER_ID, record?.filterId)
                    },
                    message = "DeleteRecord with id: $id"
                )
            )
            record?.let {
                dao.updateRecords(
                    listOf(it.copy(isDeleted = true))
                )
            }
        }
    }

    override suspend fun getLatestRecordUpdatedAt(ownerId: String, filterId: String?): Long? {
        return dao.getLatestRecordUpdatedAt(ownerId = ownerId, filterId = filterId)
    }

    override suspend fun getLatestCaseUpdatedAt(ownerId: String, filterId: String?): Long? {
        return dao.getLatestCaseUpdatedAt(ownerId = ownerId, filterId = filterId)
    }

    override suspend fun insertRecordFile(file: RecordFile): Long {
        return dao.insertRecordFile(recordFile = file)
    }

    override suspend fun getRecordFile(localId: String): List<RecordFile>? {
        return dao.getRecordFile(localId = localId)
    }

    private suspend fun uploadRecord(id: String) = supervisorScope {
        val record = getRecordById(id)

        if (record == null) {
            return@supervisorScope
        }

        if (isNetworkAvailable(context = context)) {
            dao.updateRecords(listOf(record.copy(status = RecordStatus.SYNCING)))
        } else {
            dao.updateRecords(listOf(record.copy(status = RecordStatus.WAITING_FOR_NETWORK)))
            return@supervisorScope
        }

        val files = dao.getRecordFile(id)?.map { File(it.filePath) }
        if (files.isNullOrEmpty()) {
            Records.logEvent(
                EventLog(
                    params = JSONObject().also { param ->
                        param.put(DOCUMENT_ID, id)
                        param.put(OWNER_ID, record.ownerId)
                        param.put(FILTER_ID, record.filterId)
                    },
                    message = "Upload error: No file for the given documentId: $id"
                )
            )
            return@supervisorScope
        }

        val fileContentList =
            files.map { FileType(contentType = it.getMimeType() ?: "", fileSize = it.length()) }
        val uploadInitResponse =
            awsRepository.fileUploadInit(
                files = fileContentList,
                patientOid = record.filterId,
                isMultiFile = files.size > 1,
                tags = emptyList(), // TODO add tags from the user tags table
                documentType = record.documentType,
                documentDate = record.documentDate,
            )
        if (uploadInitResponse?.error == true) {
            Records.logEvent(
                EventLog(
                    params = JSONObject().also { param ->
                        param.put(DOCUMENT_ID, id)
                        param.put(OWNER_ID, record.ownerId)
                        param.put(FILTER_ID, record.filterId)
                    },
                    message = "Upload initialization error: ${uploadInitResponse.message}"
                )
            )
            dao.updateRecords(listOf(record.copy(status = RecordStatus.SYNC_FAILED)))
            return@supervisorScope
        }
        val batchResponse = uploadInitResponse?.batchResponse?.firstOrNull()
        if (batchResponse == null) {
            Records.logEvent(
                EventLog(
                    params = JSONObject().also { param ->
                        param.put(DOCUMENT_ID, id)
                        param.put(OWNER_ID, record.ownerId)
                        param.put(FILTER_ID, record.filterId)
                    },
                    message = "Batch response is null"
                )
            )
            dao.updateRecords(listOf(record.copy(status = RecordStatus.SYNC_FAILED)))
            return@supervisorScope
        }
        val uploadResponse =
            awsRepository.uploadFile(batch = batchResponse, fileList = files)
        if (uploadResponse?.error == true) {
            Records.logEvent(
                EventLog(
                    params = JSONObject().also { param ->
                        param.put(DOCUMENT_ID, id)
                        param.put(OWNER_ID, record.ownerId)
                        param.put(FILTER_ID, record.filterId)
                    },
                    message = "Upload error: ${uploadResponse.message}"
                )
            )
            myFileRepository.deleteDocument(uploadResponse.documentId, record.filterId)
            dao.updateRecords(listOf(record.copy(status = RecordStatus.SYNC_FAILED)))
            return@supervisorScope
        }
        if (uploadResponse == null) {
            Records.logEvent(
                EventLog(
                    params = JSONObject().also { param ->
                        param.put(DOCUMENT_ID, id)
                        param.put(OWNER_ID, record.ownerId)
                        param.put(FILTER_ID, record.filterId)
                    },
                    message = "Upload response is null"
                )
            )
            dao.updateRecords(listOf(record.copy(status = RecordStatus.SYNC_FAILED)))
            return@supervisorScope
        }
        dao.updateRecords(
            listOf(
                record.copy(
                    status = RecordStatus.SYNC_SUCCESS,
                    documentId = uploadResponse.documentId,
                    isDirty = false
                )
            )
        )
        Records.logEvent(
            EventLog(
                params = JSONObject().also { param ->
                    param.put(DOCUMENT_ID, id)
                    param.put(OWNER_ID, record.ownerId)
                    param.put(FILTER_ID, record.filterId)
                },
                message = "Upload finished"
            )
        )
    }

    override suspend fun createCase(
        caseId: String?,
        name: String,
        type: String,
        ownerId: String,
        filterId: String,
        isSynced: Boolean
    ): String = supervisorScope {
        val id = caseId ?: UUID.randomUUID().toString()
        Records.logEvent(
            EventLog(
                params = JSONObject().also { param ->
                    param.put(OWNER_ID, ownerId)
                    param.put(FILTER_ID, filterId)
                    param.put(CASE_ID, id)
                    param.put(CASE_NAME, name)
                    param.put(CASE_TYPE, type)
                },
                message = "CreateCase with caseId: $id, name: $name, type: $type, ownerId: $ownerId, filterId: $filterId"
            )
        )
        dao.createCase(
            CaseEntity(
                caseId = id,
                name = name,
                caseType = type,
                ownerId = ownerId,
                filterId = filterId,
                isSynced = isSynced,
                createdAt = System.currentTimeMillis() / 1000,
                updatedAt = System.currentTimeMillis() / 1000,
            )
        )
        return@supervisorScope id
    }

    override suspend fun updateCase(
        caseId: String,
        name: String,
        type: String
    ): String? {
        val case = getCaseByCaseId(caseId) ?: return null
        val updatedCase = case.copy(
            name = name,
            caseType = type,
            isDirty = true
        )
        dao.updateCase(updatedCase)
        return case.caseId
    }

    override fun readCases(ownerId: String, filterId: String?): Flow<List<CaseModel>> {
        return dao.getCasesWithRecords(ownerId, filterId)
            .map { list -> list.map { it.toCaseModel() } }
    }

    override fun getCaseWithRecords(caseId: String): Flow<CaseModel?> {
        return dao.getCaseWithRecords(caseId).map { caseWithRecords ->
            caseWithRecords?.let {
                CaseModel(
                    id = it.caseEntity.caseId,
                    name = it.caseEntity.name,
                    type = it.caseEntity.caseType ?: "unknown",
                    createdAt = it.caseEntity.createdAt,
                    updatedAt = it.caseEntity.updatedAt,
                    records = it.records.map { record ->
                        RecordModel(
                            id = record.id,
                            documentType = record.documentType,
                            documentDate = record.documentDate,
                            createdAt = record.createdAt,
                            updatedAt = record.updatedAt,
                            thumbnail = record.thumbnail
                        )
                    }
                )
            }
        }
    }

    override suspend fun assignRecordToCase(caseId: String, recordId: String) {
        insertRecordIntoCase(
            caseId = caseId,
            documentId = recordId
        )
    }

    private suspend fun insertRecordIntoCase(caseId: String, documentId: String) {
        dao.insertCaseRecordRelation(
            CaseRecordRelationEntity(
                caseId = caseId,
                recordId = documentId
            )
        )
    }
}