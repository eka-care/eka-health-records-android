package eka.care.records.client.utils

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import eka.care.records.client.model.CaseModel
import eka.care.records.client.model.DocumentTypeCount
import eka.care.records.client.model.EventLog
import eka.care.records.client.model.RecordModel
import eka.care.records.client.model.SortOrder
import eka.care.records.client.utils.RecordsUtility.Companion.getWorkerTag
import eka.care.records.data.contract.LogInterceptor
import eka.care.records.data.db.RecordsDatabase
import eka.care.records.data.entity.CaseStatus
import eka.care.records.data.entity.CaseUiState
import eka.care.records.data.repository.EncountersRepository
import eka.care.records.data.repository.RecordsRepositoryImpl
import eka.care.records.sync.RecordsSync
import kotlinx.coroutines.flow.Flow
import java.io.File

class Records private constructor() {
    private lateinit var recordsRepository: RecordsRepositoryImpl
    private lateinit var encounterRepository: EncountersRepository
    private lateinit var db: RecordsDatabase

    private var logger: LogInterceptor? = null

    companion object {
        @Volatile
        private var INSTANCE: Records? = null

        fun getInstance(context: Context, token: String): Records {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildClient().also {
                    it.db = RecordsDatabase.getInstance(context)
                    it.recordsRepository = RecordsRepositoryImpl(context)
                    it.encounterRepository = EncountersRepository()
                    INSTANCE = it
                }
            }
        }

        fun logEvent(eventLog: EventLog) {
            INSTANCE?.logger?.logEvent(eventLog)
        }

        private fun buildClient(): Records {
            return Records()
        }
    }

    fun setLogInterceptor(listener: LogInterceptor) {
        logger = listener
    }

    fun refreshRecords(context: Context, businessId: String, ownerIds: List<String>? = null) {
        val inputData = Data.Builder()
            .putString("businessId", businessId)
            .putStringArray("ownerIds", ownerIds?.toTypedArray() ?: emptyArray())
            .build()
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest =
            OneTimeWorkRequestBuilder<RecordsSync>()
                .setInputData(inputData)
                .setConstraints(constraints)
                .addTag(getWorkerTag(businessId))
                .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                getWorkerTag(businessId),
                ExistingWorkPolicy.REPLACE,
                syncRequest
            )
    }

    fun syncRecords(businessId: String) {
        recordsRepository.startAutoSync(businessId = businessId)
    }

    suspend fun addNewRecord(
        files: List<File>,
        businessId: String,
        ownerId: String,
        caseId: String? = null,
        documentType: String = "ot",
        documentDate: Long? = null,
        tags: List<String> = emptyList()
    ): String? {
        return recordsRepository.createRecord(
            files = files,
            businessId = businessId,
            ownerId = ownerId,
            caseId = caseId,
            documentDate = documentDate,
            documentType = documentType,
            tags = tags
        )
    }

    fun getRecords(
        businessId: String,
        ownerIds: List<String>,
        caseId: String? = null,
        includeDeleted: Boolean = false,
        documentType: String? = null,
        sortOrder: SortOrder,
    ): Flow<List<RecordModel>> {
        return recordsRepository.readRecords(
            businessId = businessId,
            ownerIds = ownerIds,
            caseId = caseId,
            includeDeleted = includeDeleted,
            documentType = documentType,
            sortOrder = sortOrder
        )
    }

    fun getRecordsCountGroupByType(
        businessId: String,
        ownerIds: List<String>,
    ): Flow<List<DocumentTypeCount>> {
        return recordsRepository.getRecordTypeCounts(
            businessId = businessId,
            ownerIds = ownerIds
        )
    }

    suspend fun updateRecord(
        id: String,
        caseId: String? = null,
        documentDate: Long? = null,
        documentType: String? = null,
    ): String? {
        return recordsRepository.updateRecord(
            id = id,
            caseId = caseId,
            documentDate = documentDate,
            documentType = documentType
        )
    }

    suspend fun getRecordDetailsById(id: String): RecordModel? {
        return recordsRepository.getRecordDetails(id = id)
    }

    suspend fun deleteRecords(ids: List<String>) {
        recordsRepository.deleteRecords(ids = ids)
    }

    suspend fun createCase(
        businessId: String,
        ownerId: String,
        name: String,
        type: String,
        createdAt: Long? = null,
        updatedAt: Long? = null
    ): String {
        return recordsRepository.createCase(
            caseId = null,
            businessId = businessId,
            ownerId = ownerId,
            name = name,
            type = type,
            createdAt = createdAt,
            updatedAt = updatedAt,
            status = CaseStatus.CREATED_LOCALLY,
            uiStatus = CaseUiState.WAITING_TO_UPLOAD
        )
    }

    fun readCases(businessId: String, ownerId: String): Flow<List<CaseModel>> {
        return recordsRepository.readCases(
            businessId = businessId,
            ownerId = ownerId
        )
    }

    suspend fun updateEncounter(caseId: String, name: String, type: String): String? {
        return recordsRepository.updateCase(
            caseId = caseId,
            name = name,
            type = type,
            status = CaseStatus.UPDATED_LOCALLY,
            uiStatus = CaseUiState.WAITING_TO_UPLOAD
        )
    }

    suspend fun getCaseWithRecords(caseId: String): CaseModel? {
        return recordsRepository.getCaseWithRecords(caseId = caseId)
    }

    suspend fun assignRecordToCase(caseId: String, recordId: String) {
        recordsRepository.assignRecordLocally(caseId = caseId, recordId = recordId)
    }

    suspend fun deleteEncounter(encounterId: String) {
        recordsRepository.deleteCases(
            caseId = encounterId
        )
    }

    suspend fun getUniqueEncounters(businessId: String) = recordsRepository.getUniqueEncounterTypes(
        businessId = businessId
    )

    suspend fun syncOrigin(ownerId: String) {
        encounterRepository.syncOrigin(ownerId)
    }

    fun clearAllData() {
        db.clearAllTables()
    }
}