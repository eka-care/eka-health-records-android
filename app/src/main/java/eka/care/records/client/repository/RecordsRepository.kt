package eka.care.records.client.repository

import eka.care.records.client.model.CaseModel
import eka.care.records.client.model.DocumentTypeCount
import eka.care.records.client.model.RecordModel
import eka.care.records.client.model.SortOrder
import eka.care.records.data.entity.CaseEntity
import eka.care.records.data.entity.RecordEntity
import eka.care.records.data.entity.RecordFile
import kotlinx.coroutines.flow.Flow
import java.io.File

interface RecordsRepository {
    suspend fun createRecord(
        files: List<File>,
        ownerId: String,
        filterId: String? = null,
        caseId: String?,
        documentType: String = "ot",
        documentDate: Long? = null,
        tags: List<String>
    ): String?

    suspend fun createRecords(records: List<RecordEntity>)
    fun readRecords(
        ownerId: String,
        filterIds: List<String>?,
        caseId: String?,
        includeDeleted: Boolean,
        documentType: String?,
        sortOrder: SortOrder
    ): Flow<List<RecordModel>>

    fun getRecordTypeCounts(
        ownerId: String,
        filterIds: List<String>?,
    ): Flow<List<DocumentTypeCount>>

    suspend fun getRecordById(id: String): RecordEntity?
    suspend fun getRecordByDocumentId(id: String): RecordEntity?
    suspend fun getRecordDetails(id: String): RecordModel?
    suspend fun updateRecords(records: List<RecordEntity>)
    suspend fun updateRecord(
        id: String,
        caseId: String?,
        documentDate: Long? = null,
        documentType: String? = null
    ): String?

    suspend fun deleteRecords(ids: List<String>)
    suspend fun getLatestRecordUpdatedAt(ownerId: String, filterId: String?): Long?
    suspend fun getLatestCaseUpdatedAt(ownerId: String, filterId: String?): Long?
    suspend fun insertRecordFile(file: RecordFile): Long
    suspend fun getRecordFile(localId: String): List<RecordFile>?

    suspend fun getCaseByCaseId(id: String): CaseEntity?

    suspend fun createCase(
        caseId: String?,
        name: String,
        type: String,
        ownerId: String,
        filterId: String,
        isSynced: Boolean
    ): String

    suspend fun updateCase(
        caseId: String,
        name: String,
        type: String,
    ): String?

    fun readCases(ownerId: String, filterId: String?): Flow<List<CaseModel>>

    fun getCaseWithRecords(caseId: String): Flow<CaseModel?>

    suspend fun assignRecordToCase(caseId: String, recordId: String): Unit
}