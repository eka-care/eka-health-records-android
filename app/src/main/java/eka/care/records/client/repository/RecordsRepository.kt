package eka.care.records.client.repository

import eka.care.records.client.model.CaseModel
import eka.care.records.client.model.DocumentTypeCount
import eka.care.records.client.model.RecordModel
import eka.care.records.client.model.SortOrder
import eka.care.records.data.entity.CaseStatus
import eka.care.records.data.entity.CaseUiState
import eka.care.records.data.entity.EncounterEntity
import eka.care.records.data.entity.EncounterWithRecords
import eka.care.records.data.entity.FileEntity
import eka.care.records.data.entity.RecordEntity
import kotlinx.coroutines.flow.Flow
import java.io.File

interface RecordsRepository {
    suspend fun createRecord(
        files: List<File>,
        businessId: String,
        ownerId: String,
        caseId: String?,
        documentType: String = "ot",
        documentDate: Long? = null,
        tags: List<String>
    ): String?

    suspend fun createRecords(records: List<RecordEntity>)
    fun readRecords(
        businessId: String,
        ownerIds: List<String>,
        caseId: String?,
        includeDeleted: Boolean,
        documentType: String?,
        sortOrder: SortOrder
    ): Flow<List<RecordModel>>

    fun getRecordTypeCounts(
        businessId: String,
        ownerIds: List<String>,
    ): Flow<List<DocumentTypeCount>>

    suspend fun getRecordById(id: String): RecordEntity?
    suspend fun getRecordByDocumentId(id: String): RecordEntity?
    suspend fun getRecordDetails(id: String): RecordModel?
    suspend fun updateRecord(record: RecordEntity)
    suspend fun updateRecord(
        id: String,
        caseId: String?,
        documentDate: Long? = null,
        documentType: String? = null
    ): String?

    suspend fun deleteRecords(ids: List<String>)
    suspend fun getLatestRecordUpdatedAt(businessId: String, ownerId: String): Long?
    suspend fun getLatestCaseUpdatedAt(businessId: String, ownerId: String): Long?
    suspend fun getUniqueEncounterTypes(businessId: String): Flow<List<String>>
    suspend fun insertRecordFile(file: FileEntity): Long
    suspend fun getRecordFile(localId: String): List<FileEntity>?

    suspend fun getCaseByCaseId(id: String): EncounterWithRecords?

    suspend fun createCase(
        caseId: String?,
        businessId: String,
        ownerId: String,
        name: String,
        type: String,
        createdAt: Long?,
        updatedAt: Long?,
        status: CaseStatus,
        uiStatus: CaseUiState
    ): String

    suspend fun updateCase(
        caseId: String,
        name: String,
        type: String,
        status: CaseStatus,
        uiStatus: CaseUiState
    ): String?

    fun readCases(businessId: String, ownerId: String): Flow<List<CaseModel>>

    suspend fun deleteCases(caseId: String)

    suspend fun deleteCase(encounter: EncounterEntity)

    suspend fun getCaseWithRecords(caseId: String): CaseModel?

    suspend fun assignRecordToCase(caseId: String, recordId: String): Unit
}