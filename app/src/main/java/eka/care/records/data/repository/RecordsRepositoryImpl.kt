package eka.care.records.data.repository

import android.content.Context
import androidx.sqlite.db.SupportSQLiteQueryBuilder
import com.google.gson.Gson
import eka.care.records.client.model.CaseModel
import eka.care.records.client.model.DocumentTypeCount
import eka.care.records.client.model.EventLog
import eka.care.records.client.model.RecordModel
import eka.care.records.client.model.RecordState
import eka.care.records.client.model.SortOrder
import eka.care.records.client.repository.RecordsRepository
import eka.care.records.client.utils.Records
import eka.care.records.client.utils.RecordsUtility
import eka.care.records.client.utils.RecordsUtility.Companion.getMimeType
import eka.care.records.client.utils.RecordsUtility.Companion.md5
import eka.care.records.data.core.FileStorageManagerImpl
import eka.care.records.data.db.RecordsDatabase
import eka.care.records.data.entity.EncounterEntity
import eka.care.records.data.entity.EncounterRecordCrossRef
import eka.care.records.data.entity.FileEntity
import eka.care.records.data.entity.RecordEntity
import eka.care.records.data.entity.RecordStatus
import eka.care.records.data.entity.toCaseModel
import eka.care.records.data.remote.dto.request.CaseRequest
import eka.care.records.data.remote.dto.request.FileType
import eka.care.records.data.remote.dto.request.UpdateFileDetailsRequest
import eka.care.records.data.utility.LoggerConstant.Companion.BUSINESS_ID
import eka.care.records.data.utility.LoggerConstant.Companion.CASE_ID
import eka.care.records.data.utility.LoggerConstant.Companion.DOCUMENT_ID
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
    private var encountersDao = RecordsDatabase.getInstance(context).encounterDao()
    private val myFileRepository = MyFileRepository()
    private val fileStorageManager = FileStorageManagerImpl(context)
    private val encountersRepository = EncountersRepository()
    private val awsRepository = AwsRepository()
    private var syncJob: Job? = null

    fun startAutoSync(businessId: String) {
        syncJob?.cancel()
        syncJob = CoroutineScope(Dispatchers.IO).launch {
            dao.getRecordsByStatus(
                businessId, listOf(
                    RecordStatus.CREATED_LOCALLY,
                    RecordStatus.UPDATED_LOCALLY,
                    RecordStatus.ARCHIVED,
                )
            )?.also {
                syncRecords(it)
            }
            encountersDao.getUnsyncedEncounters(businessId)?.also {
                syncCasesToServer(it)
            }
            encountersDao.getDirtyEncounter(businessId)?.also {
                syncCasesToServer(it)
            }
        }
    }

    private suspend fun syncRecords(records: List<RecordEntity>) = supervisorScope {
        records.forEach {
            when (it.status) {
                RecordStatus.CREATED_LOCALLY -> {
                    uploadRecords(it)
                }

                RecordStatus.UPDATED_LOCALLY -> {
                    updateDocument(it)
                }

                RecordStatus.ARCHIVED -> {
                    syncDeletedRecordsToServer(it)
                }

                else -> {
                    //NO-OP
                }
            }
        }
    }

    private suspend fun updateDocument(record: RecordEntity) {
        if (isNetworkAvailable(context = context)) {
            dao.updateRecord(record.copy(state = RecordState.SYNCING))
        } else {
            dao.updateRecord(record.copy(state = RecordState.WAITING_FOR_NETWORK))
            return
        }
        val result = myFileRepository.updateFileDetails(
            documentId = record.documentId,
            oid = record.ownerId,
            request = UpdateFileDetailsRequest(
                filterId = record.ownerId,
                documentType = record.documentType,
                documentDate = record.documentDate,
            )
        )
        if (result == null) {
            return
        }

        if (result !in 200..299) {
            logRecordSyncEvent(
                dId = record.documentId,
                bId = record.businessId,
                oId = record.ownerId,
                msg = "Update failed for document $record.documentId code: $result"
            )
            dao.updateRecord(record.copy(state = RecordState.SYNC_FAILED))
        } else {
            dao.updateRecord(
                record.copy(
                    state = RecordState.SYNC_SUCCESS,
                    status = RecordStatus.SYNC_COMPLETED
                )
            )
            logRecordSyncEvent(
                dId = record.documentId,
                bId = record.businessId,
                oId = record.ownerId,
                msg = "Document updated successfully $record.documentId",
            )
        }
    }

    private suspend fun syncDeletedRecordsToServer(record: RecordEntity) {
        val documentId = record.documentId
        val result = myFileRepository.deleteDocument(documentId, record.ownerId)
        if (result in (200..299)) {
            dao.deleteRecord(record)
            logRecordSyncEvent(
                dId = record.documentId,
                bId = record.businessId,
                oId = record.ownerId,
                msg = "Syncing deleted record success: $documentId"
            )
        } else {
            logRecordSyncEvent(
                dId = record.documentId,
                bId = record.businessId,
                oId = record.ownerId,
                msg = "Sync delete failed code: $result",
            )
        }
    }

    private suspend fun uploadRecords(record: RecordEntity) {
        if (isNetworkAvailable(context = context)) {
            dao.updateRecord(record.copy(state = RecordState.SYNCING))
        } else {
            dao.updateRecord(record.copy(state = RecordState.WAITING_FOR_NETWORK))
            return
        }

        val files = dao.getRecordFile(record.documentId)?.map { File(it.filePath) }
        if (files.isNullOrEmpty()) {
            logRecordSyncEvent(
                dId = record.documentId,
                bId = record.businessId,
                oId = record.ownerId,
                msg = "Upload error: No file for the given documentId: ${record.documentId}"
            )
            return
        }

        val fileContentList =
            files.map {
                FileType(
                    contentType = it.getMimeType() ?: "",
                    fileSize = it.length()
                )
            }
        val uploadInitResponse =
            awsRepository.fileUploadInit(
                documentId = record.documentId,
                files = fileContentList,
                patientOid = record.ownerId,
                isMultiFile = files.size > 1,
                tags = emptyList(), // TODO add tags from the user tags table
                documentType = record.documentType,
                documentDate = record.documentDate,
            )
        if (uploadInitResponse?.error == true) {
            logRecordSyncEvent(
                dId = record.documentId,
                bId = record.businessId,
                oId = record.ownerId,
                msg = "Upload initialization error: ${uploadInitResponse.message}"
            )
            dao.updateRecord(record.copy(state = RecordState.SYNC_FAILED))
            return
        }
        val batchResponse = uploadInitResponse?.batchResponse?.firstOrNull()
        if (batchResponse == null) {
            logRecordSyncEvent(
                dId = record.documentId,
                bId = record.businessId,
                oId = record.ownerId,
                msg = "Batch response is null"
            )
            dao.updateRecord(record.copy(state = RecordState.SYNC_FAILED))
            return
        }
        val uploadResponse =
            awsRepository.uploadFile(batch = batchResponse, fileList = files)
        if (uploadResponse.error) {
            logRecordSyncEvent(
                dId = record.documentId,
                bId = record.businessId,
                oId = record.ownerId,
                msg = "Upload error: ${uploadResponse.message}"
            )
            myFileRepository.deleteDocument(uploadResponse.documentId, record.ownerId)
            dao.updateRecord(record.copy(state = RecordState.SYNC_FAILED))
            return
        }
        dao.updateRecord(
            record.copy(
                state = RecordState.SYNC_SUCCESS,
                documentId = uploadResponse.documentId,
                status = RecordStatus.SYNC_COMPLETED
            )
        )
        logRecordSyncEvent(
            dId = record.documentId,
            bId = record.businessId,
            oId = record.ownerId,
            msg = "Upload success: ${uploadResponse.documentId}"
        )
    }

    private suspend fun syncCasesToServer(cases: List<EncounterEntity>) = supervisorScope {
        cases.forEach { case ->
            launch {
                if (!isNetworkAvailable(context = context)) {
                    logRecordSyncEvent(
                        bId = case.businessId,
                        oId = case.ownerId,
                        caseId = case.encounterId,
                        msg = "Syncing case failed due to no network: ${case.encounterId}",
                    )
                    return@launch
                }
                val response = encountersRepository.createCase(
                    patientId = case.ownerId,
                    caseRequest = CaseRequest(
                        caseId = case.encounterId,
                        name = case.name,
                        caseType = case.encounterType,
                        occurredAt = case.createdAt
                    )
                )
                if (response == null) {
                    logRecordSyncEvent(
                        bId = case.businessId,
                        oId = case.ownerId,
                        caseId = case.encounterId,
                        msg = "Syncing case failed: ${case.encounterId}",
                    )
                    return@launch
                }
                encountersDao.updateEncounter(
                    case.copy(
                        isSynced = true,
                        isDirty = false,
                        isArchived = false,
                        encounterId = response.caseId
                    )
                )
                logRecordSyncEvent(
                    bId = case.businessId,
                    oId = case.ownerId,
                    caseId = case.encounterId,
                    msg = "Syncing case success: ${response.caseId}",
                )
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
        businessId: String,
        ownerId: String,
        caseId: String?,
        documentType: String,
        documentDate: Long?,
        tags: List<String>
    ): String? = supervisorScope {
        if (files.isEmpty()) {
            return@supervisorScope null
        }

        val time = System.currentTimeMillis() / 1000
        val id = UUID.randomUUID().toString()
        val thumbnail =
            if (files.first().extension.lowercase() in listOf("jpg", "jpeg", "png", "webp")) {
                files.first().path
            } else {
                fileStorageManager.generateThumbnail(
                    filePath = files.first().path
                )
            }
        val record = RecordEntity(
            documentId = id,
            businessId = businessId,
            ownerId = ownerId,
            thumbnail = thumbnail,
            documentType = documentType,
            createdAt = time,
            updatedAt = time,
            documentDate = documentDate ?: time,
            documentHash = files.first().md5(),
            status = RecordStatus.CREATED_LOCALLY,
            state = RecordState.WAITING_TO_UPLOAD
        )
        val files = files.map { file ->
            val compressedFile =
                if (file.extension.lowercase() == "pdf") file else Compressor.compress(
                    context,
                    file
                )
            val path = compressedFile.path
            val type = compressedFile.extension
            FileEntity(
                documentId = record.documentId,
                filePath = path,
                fileType = type
            )
        }
        dao.insertRecordWithFiles(record = record, files = files)
        caseId?.let { insertRecordIntoCase(caseId = it, documentId = id) }

        return@supervisorScope id
    }

    override fun readRecords(
        businessId: String,
        ownerIds: List<String>,
        caseId: String?,
        includeDeleted: Boolean,
        documentType: String?,
        sortOrder: SortOrder,
    ): Flow<List<RecordModel>> = flow {
        try {
            val selection = StringBuilder()
            val selectionArgs = mutableListOf<String>()

            if (!includeDeleted) {
                selection.append("STATUS != 4 AND ")
            }

            selection.append("BUSINESS_ID = ? ")
            selectionArgs.add(businessId)

            if (!documentType.isNullOrEmpty()) {
                selection.append("AND DOCUMENT_TYPE = ? ")
                selectionArgs.add(documentType)
            }

            if (ownerIds.isNotEmpty()) {
                val placeholders = ownerIds.joinToString(",") { "?" }
                selection.append("AND (OWNER_ID IN ($placeholders)) ")
                selectionArgs.addAll(ownerIds)
            }

            val query = SupportSQLiteQueryBuilder
                .builder("EKA_RECORDS_TABLE")
                .selection(selection.toString().trim(), selectionArgs.toTypedArray())
                .orderBy("${sortOrder.value} ${sortOrder.order}")
                .create()

            if (caseId != null) {
                val records = getCaseWithRecords(caseId)?.records ?: emptyList()
                emit(records)
            } else {
                val dataFlow = dao.readRecords(query).map { records ->
                    records.map {
                        RecordModel(
                            id = it.documentId,
                            thumbnail = it.thumbnail ?: dao.getRecordFile(it.documentId)
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
            logRecordSyncEvent(
                bId = businessId,
                oId = ownerIds.joinToString(","),
                msg = "Error reading records: ${e.localizedMessage}",
            )
            emit(emptyList())
        }
    }

    override suspend fun getRecordById(id: String) = dao.getRecordById(id = id)

    override suspend fun getRecordByDocumentId(id: String) = dao.getRecordByDocumentId(id = id)

    override suspend fun getCaseByCaseId(id: String) = encountersDao.getEncounterById(id)

    override suspend fun getRecordDetails(id: String): RecordModel? {
        val record = getRecordById(id)
        if (record == null) {
            return null
        }

        val files = getRecordFile(record.documentId)
        if (files?.isNotEmpty() == true && !record.isSmart) {
            logRecordSyncEvent(
                dId = record.documentId,
                bId = record.businessId,
                oId = record.ownerId,
                msg = "Found local files for record: $id",
            )
            return RecordModel(
                id = record.documentId,
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
            logRecordSyncEvent(
                dId = record.documentId,
                bId = record.businessId,
                oId = record.ownerId,
                msg = "Found smart report for record: $id",
            )
            return RecordModel(
                id = record.documentId,
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

        val documentId = record.documentId

        val response = myFileRepository.getDocument(
            documentId = documentId,
            filterId = record.ownerId
        )
        if (response == null) {
            logRecordSyncEvent(
                dId = record.documentId,
                bId = record.businessId,
                oId = record.ownerId,
                msg = "Error fetching document details for: $documentId",
            )
            return null
        }

        val smartReportField = response.smartReport?.let {
            Gson().toJson(it)
        }
        val updatedRecord = record.copy(
            smartReport = smartReportField
        )
        updateRecord(updatedRecord)
        response.files.forEach { file ->
            val fileType = response.files.firstOrNull()?.fileType ?: ""
            val filePath = RecordsUtility.downloadFile(
                file.assetUrl,
                context = context.applicationContext,
                type = file.fileType
            )
            insertRecordFile(
                FileEntity(
                    documentId = record.documentId,
                    filePath = filePath,
                    fileType = fileType
                )
            )
            logRecordSyncEvent(
                dId = record.documentId,
                bId = record.businessId,
                oId = record.ownerId,
                msg = "Inserted file: $filePath for record: $documentId",
            )
        }
        return RecordModel(
            id = record.documentId,
            thumbnail = record.thumbnail,
            createdAt = record.createdAt,
            updatedAt = record.updatedAt,
            documentDate = record.documentDate,
            documentType = record.documentType,
            isSmart = record.isSmart,
            smartReport = smartReportField,
            files = getRecordFile(record.documentId)?.map { file ->
                RecordModel.RecordFile(
                    id = file.id,
                    filePath = file.filePath,
                    fileType = file.fileType
                )
            } ?: emptyList()
        )
    }

    override fun getRecordTypeCounts(
        businessId: String,
        ownerIds: List<String>
    ): Flow<List<DocumentTypeCount>> = flow {
        try {
            val selection = StringBuilder()
            val selectionArgs = mutableListOf<String>()

            selection.append("STATUS != 4 AND ")
            selection.append("BUSINESS_ID = ? ")
            selectionArgs.add(businessId)

            val placeholders = ownerIds.joinToString(",") { "?" }
            selection.append("AND (OWNER_ID IN ($placeholders)) ")
            selectionArgs.addAll(ownerIds)

            val query = SupportSQLiteQueryBuilder
                .builder("EKA_RECORDS_TABLE")
                .columns(arrayOf("document_type as documentType", "COUNT(DOCUMENT_ID) as count"))
                .selection(selection.toString().trim(), selectionArgs.toTypedArray())
                .groupBy("document_type")
                .create()

            emitAll(dao.getDocumentTypeCounts(query))
        } catch (e: Exception) {
            logRecordSyncEvent(
                bId = businessId,
                oId = ownerIds.joinToString(","),
                msg = "Error getting record type counts: ${e.localizedMessage}"
            )
            emit(emptyList())
        }
    }

    override suspend fun updateRecord(record: RecordEntity) = dao.updateRecord(record)

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
            status = RecordStatus.UPDATED_LOCALLY
        )
        dao.updateRecord(updatedRecord)
        caseId?.let {
            encountersDao.insertEncounterRecordCrossRef(
                EncounterRecordCrossRef(
                    encounterId = it,
                    documentId = id,
                )
            )
        }
        logRecordSyncEvent(
            dId = record.documentId,
            bId = record.businessId,
            oId = record.ownerId,
            msg = "Update record: $documentDate, $documentType"
        )
        return record.documentId
    }

    override suspend fun deleteRecords(ids: List<String>) {
        ids.forEach { id ->
            val record = getRecordById(id)
            if (record != null) {
                logRecordSyncEvent(
                    dId = record.documentId,
                    bId = record.businessId,
                    oId = record.ownerId,
                    msg = "DeleteRecord with id: $id"
                )
                dao.updateRecord(
                    record.copy(status = RecordStatus.ARCHIVED)
                )
            }
        }
    }

    override suspend fun getLatestRecordUpdatedAt(businessId: String, ownerId: String): Long? {
        return dao.getLatestRecordUpdatedAt(businessId = businessId, ownerId = ownerId)
    }

    override suspend fun getLatestCaseUpdatedAt(businessId: String, ownerId: String): Long? {
        return encountersDao.getLatestEncounterUpdatedAt(businessId = businessId, ownerId = ownerId)
    }

    override suspend fun insertRecordFile(file: FileEntity): Long {
        return dao.insertRecordFile(recordFile = file)
    }

    override suspend fun getRecordFile(localId: String): List<FileEntity>? {
        return dao.getRecordFile(documentId = localId)
    }

    override suspend fun createCase(
        caseId: String?,
        businessId: String,
        ownerId: String,
        name: String,
        type: String,
        isSynced: Boolean
    ): String = supervisorScope {
        val id = caseId ?: UUID.randomUUID().toString()
        logRecordSyncEvent(
            bId = businessId,
            oId = ownerId,
            caseId = id,
            msg = "CreateCase with caseId: $id, name: $name, type: $type"
        )
        encountersDao.insertEncounter(
            EncounterEntity(
                encounterId = id,
                name = name,
                encounterType = type,
                businessId = businessId,
                ownerId = ownerId,
                isSynced = isSynced,
                createdAt = System.currentTimeMillis() / 1000,
                updatedAt = System.currentTimeMillis() / 1000,
            )
        )
        return@supervisorScope id
    }

    override suspend fun updateCase(caseId: String, name: String, type: String): String? {
        val encounter = getCaseByCaseId(caseId) ?: return null
        val updatedEncounter = encounter.copy(
            encounter = encounter.encounter.copy(
                name = name,
                encounterType = type,
                isDirty = true
            )
        )
        encountersDao.updateEncounter(updatedEncounter.encounter)
        return encounter.encounter.encounterId
    }

    override fun readCases(businessId: String, ownerId: String): Flow<List<CaseModel>> = flow {
        emitAll(
            encountersDao.getAllEncounters(businessId, ownerId)
                .map { list -> list.map { it.toCaseModel() } }
        )
    }

    override suspend fun getCaseWithRecords(caseId: String): CaseModel? {
        return encountersDao.getEncounterById(caseId)?.toCaseModel()
    }

    override suspend fun assignRecordToCase(caseId: String, recordId: String) {
        insertRecordIntoCase(
            caseId = caseId,
            documentId = recordId
        )
    }

    private suspend fun insertRecordIntoCase(caseId: String, documentId: String) {
        encountersDao.insertEncounterRecordCrossRef(
            EncounterRecordCrossRef(
                encounterId = caseId,
                documentId = documentId
            )
        )
    }

    private fun logRecordSyncEvent(
        dId: String? = null,
        caseId: String? = null,
        bId: String,
        oId: String,
        msg: String
    ) {
        Records.logEvent(
            EventLog(
                params = JSONObject().also { param ->
                    param.put(DOCUMENT_ID, dId)
                    param.put(BUSINESS_ID, bId)
                    param.put(CASE_ID, caseId)
                    param.put(OWNER_ID, oId)
                },
                message = msg
            )
        )
    }
}