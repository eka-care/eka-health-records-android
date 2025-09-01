package eka.care.records.data.repository

import android.content.Context
import androidx.sqlite.db.SupportSQLiteQueryBuilder
import com.google.gson.Gson
import com.haroldadmin.cnradapter.NetworkResponse
import eka.care.records.client.model.CaseModel
import eka.care.records.client.model.DocumentTypeCount
import eka.care.records.client.model.EventLog
import eka.care.records.client.model.RecordModel
import eka.care.records.client.model.RecordUiState
import eka.care.records.client.model.SortOrder
import eka.care.records.client.repository.RecordsRepository
import eka.care.records.client.utils.Records
import eka.care.records.client.utils.RecordsUtility
import eka.care.records.client.utils.RecordsUtility.Companion.getMimeType
import eka.care.records.client.utils.RecordsUtility.Companion.md5
import eka.care.records.data.core.FileStorageManagerImpl
import eka.care.records.data.db.RecordsDatabase
import eka.care.records.data.entity.CaseStatus
import eka.care.records.data.entity.CaseUiState
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
            encountersDao.getEncountersByStatus(
                businessId, listOf(
                    CaseStatus.CREATED_LOCALLY,
                    CaseStatus.UPDATED_LOCALLY,
                    CaseStatus.ARCHIVED,
                )
            )?.also {
                syncEncounters(it)
            }
            dao.getRecordsByStatus(
                businessId, listOf(
                    RecordStatus.CREATED_LOCALLY,
                    RecordStatus.UPDATED_LOCALLY,
                    RecordStatus.ARCHIVED,
                )
            )?.also {
                syncRecords(it)
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

    private suspend fun syncEncounters(encounters: List<EncounterEntity>) = supervisorScope {
        encounters.forEach {
            when (it.status) {
                CaseStatus.CREATED_LOCALLY -> {
                    uploadEncounter(it)
                }

                CaseStatus.UPDATED_LOCALLY -> {
                    updateEncounter(it)
                }

                CaseStatus.ARCHIVED -> {
                    syncDeletedEncounterToServer(it)
                }

                else -> {
                    //NO-OP
                }
            }
        }
    }

    private suspend fun updateDocument(record: RecordEntity) {
        if (isNetworkAvailable(context = context)) {
            dao.updateRecord(record.copy(uiState = RecordUiState.SYNCING))
        } else {
            dao.updateRecord(record.copy(uiState = RecordUiState.WAITING_FOR_NETWORK))
            return
        }
        val recordWithCases = encountersDao.getRecordWithEncounters(record.documentId)
        recordWithCases.encounters.forEach { case ->
            if(case.status == CaseStatus.CREATED_LOCALLY) {
                logRecordSyncEvent(
                    caseId = case.encounterId,
                    bId = case.businessId,
                    oId = case.ownerId,
                    msg = "Uploading linked case for record: ${record.documentId}",
                )
                uploadEncounter(case)
            }
        }
        val linkedCases = recordWithCases.encounters.filter {
            it.status != CaseStatus.CREATED_LOCALLY
        }.map {
            it.encounterId
        }
        logRecordSyncEvent(
            dId = record.documentId,
            bId = record.businessId,
            oId = record.ownerId,
            msg = "Updating document: ${record.documentId} with cases: $linkedCases"
        )
        val result = myFileRepository.updateFileDetails(
            documentId = record.documentId,
            oid = record.ownerId,
            request = UpdateFileDetailsRequest(
                filterId = record.ownerId,
                documentType = record.documentType,
                documentDate = record.documentDate,
                cases = linkedCases
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
            dao.updateRecord(record.copy(uiState = RecordUiState.SYNC_FAILED))
        } else {
            dao.updateRecord(
                record.copy(
                    uiState = RecordUiState.SYNC_SUCCESS,
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
            dao.updateRecord(record.copy(uiState = RecordUiState.SYNCING))
        } else {
            dao.updateRecord(record.copy(uiState = RecordUiState.WAITING_FOR_NETWORK))
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
        val recordWithCases = encountersDao.getRecordWithEncounters(record.documentId)
        recordWithCases.encounters.forEach { case ->
            if(case.status == CaseStatus.CREATED_LOCALLY) {
                logRecordSyncEvent(
                    caseId = case.encounterId,
                    bId = case.businessId,
                    oId = case.ownerId,
                    msg = "Uploading linked case for record: ${record.documentId}",
                )
                uploadEncounter(case)
            }
        }
        val linkedCases = recordWithCases.encounters.filter {
            it.status != CaseStatus.CREATED_LOCALLY
        }.map {
            it.encounterId
        }
        logRecordSyncEvent(
            dId = record.documentId,
            bId = record.businessId,
            oId = record.ownerId,
            msg = "Created document: ${record.documentId} with cases: $linkedCases"
        )
        val uploadInitResponse =
            awsRepository.fileUploadInit(
                documentId = record.documentId,
                files = fileContentList,
                patientOid = record.ownerId,
                isMultiFile = files.size > 1,
                tags = emptyList(), // TODO add tags from the user tags table
                documentType = record.documentType,
                documentDate = record.documentDate,
                cases = linkedCases
            )
        if (uploadInitResponse?.error == true) {
            logRecordSyncEvent(
                dId = record.documentId,
                bId = record.businessId,
                oId = record.ownerId,
                msg = "Upload initialization error: ${uploadInitResponse.message}"
            )
            dao.updateRecord(record.copy(uiState = RecordUiState.SYNC_FAILED))
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
            dao.updateRecord(record.copy(uiState = RecordUiState.SYNC_FAILED))
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
            dao.updateRecord(record.copy(uiState = RecordUiState.SYNC_FAILED))
            return
        }
        dao.updateRecord(
            record.copy(
                uiState = RecordUiState.SYNC_SUCCESS,
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

    private suspend fun uploadEncounter(encounter: EncounterEntity) {
        if (isNetworkAvailable(context = context)) {
            encountersDao.updateEncounter(encounter.copy(uiState = CaseUiState.SYNCING))
        } else {
            encountersDao.updateEncounter(encounter.copy(uiState = CaseUiState.WAITING_FOR_NETWORK))
            return
        }

        val response = encountersRepository.createCase(
            patientId = encounter.ownerId,
            caseRequest = CaseRequest(
                caseId = encounter.encounterId,
                name = encounter.name,
                caseType = encounter.encounterType,
                occurredAt = encounter.createdAt
            )
        )
        if (response == null) {
            logRecordSyncEvent(
                bId = encounter.businessId,
                oId = encounter.ownerId,
                caseId = encounter.encounterId,
                msg = "Syncing case failed: ${encounter.encounterId}",
            )
            return
        }
        encountersDao.updateEncounter(
            encounter.copy(
                uiState = CaseUiState.SYNC_SUCCESS,
                status = CaseStatus.SYNC_COMPLETED,
                encounterId = response.caseId
            )
        )
        logRecordSyncEvent(
            bId = encounter.businessId,
            oId = encounter.ownerId,
            caseId = encounter.encounterId,
            msg = "Syncing case success: ${response.caseId}",
        )
    }

    private suspend fun updateEncounter(encounter: EncounterEntity) {
        if (isNetworkAvailable(context = context)) {
            encountersDao.updateEncounter(encounter.copy(uiState = CaseUiState.SYNCING))
        } else {
            encountersDao.updateEncounter(encounter.copy(uiState = CaseUiState.WAITING_FOR_NETWORK))
            return
        }
        val result = encountersRepository.updateEncounterDetails(
            patientId = encounter.ownerId,
            caseId = encounter.encounterId,
            caseRequest = CaseRequest(
                caseId = encounter.encounterId,
                name = encounter.name,
                caseType = encounter.encounterType,
                occurredAt = encounter.createdAt
            )
        )
        if (result is NetworkResponse.Success) {
            encountersDao.updateEncounter(
                encounter.copy(
                    uiState = CaseUiState.SYNC_SUCCESS,
                    status = CaseStatus.SYNC_COMPLETED
                )
            )
            logRecordSyncEvent(
                caseId = encounter.encounterId,
                bId = encounter.businessId,
                oId = encounter.ownerId,
                msg = "Update case success: ${encounter.encounterId}"
            )
        } else if (result is NetworkResponse.Error) {
            encountersDao.updateEncounter(encounter.copy(uiState = CaseUiState.SYNC_FAILED))
            logRecordSyncEvent(
                caseId = encounter.encounterId,
                bId = encounter.businessId,
                oId = encounter.ownerId,
                msg = "Update case failed: ${encounter.encounterId}"
            )
        }
    }

    private suspend fun syncDeletedEncounterToServer(encounter: EncounterEntity) {
        val result =
            encountersRepository.deleteEncounter(encounter.ownerId, encounter.encounterId)
        if (result is NetworkResponse.Success) {
            encountersDao.deleteEncounter(encounter)
            logRecordSyncEvent(
                caseId = encounter.encounterId,
                bId = encounter.businessId,
                oId = encounter.ownerId,
                msg = "Syncing case success: ${encounter.encounterId}"
            )
        } else if (result is NetworkResponse.Error) {
            logRecordSyncEvent(
                caseId = encounter.encounterId,
                bId = encounter.businessId,
                oId = encounter.ownerId,
                msg = "Syncing case failed: ${encounter.encounterId}, code: ${result.error?.message.toString()}"
            )
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
            uiState = RecordUiState.WAITING_TO_UPLOAD
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
                            uiState = it.uiState,
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
            msg = "Update record: $documentDate, $documentType case : $caseId"
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
        createdAt: Long?,
        updatedAt: Long?,
        status: CaseStatus,
        uiStatus: CaseUiState
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
                status = status,
                uiState = uiStatus,
                createdAt = createdAt ?: (System.currentTimeMillis() / 1000),
                updatedAt = updatedAt ?: (System.currentTimeMillis() / 1000),
            )
        )
        return@supervisorScope id
    }

    override suspend fun updateCase(
        caseId: String,
        name: String,
        type: String,
        status: CaseStatus,
        uiStatus: CaseUiState
    ): String? {
        val encounter = getCaseByCaseId(caseId) ?: return null
        val updatedEncounter = encounter.copy(
            encounter = encounter.encounter.copy(
                name = name,
                encounterType = type,
                status = status,
                uiState = uiStatus,
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
        updateRecord(id = recordId, caseId = caseId, documentDate = null, documentType = null)
    }

    override suspend fun deleteCases(caseId: String) {
        val case = getCaseByCaseId(caseId)
        if (case != null) {
            encountersDao.updateEncounter(
                case.encounter.copy(status = CaseStatus.ARCHIVED)
            )
            logRecordSyncEvent(
                bId = case.encounter.businessId,
                oId = case.encounter.ownerId,
                caseId = case.encounter.encounterId,
                msg = "Deleted case with id: $caseId"
            )
        }
    }

    override suspend fun deleteCase(encounter: EncounterEntity) {
        encountersDao.deleteEncounter(encounter)
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