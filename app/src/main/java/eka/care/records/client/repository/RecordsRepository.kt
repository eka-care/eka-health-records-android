package eka.care.records.client.repository

import eka.care.records.client.model.DocumentTypeCount
import eka.care.records.client.model.RecordModel
import eka.care.records.client.model.SortOrder
import eka.care.records.data.entity.RecordEntity
import eka.care.records.data.entity.RecordFile
import kotlinx.coroutines.flow.Flow
import java.io.File

interface RecordsRepository {
    suspend fun createRecords(
        files: List<File>,
        ownerId: String,
        filterId: String? = null,
        documentType: String = "ot"
    )
    suspend fun createRecords(records: List<RecordEntity>)
    fun readRecords(
        ownerId: String,
        filterIds: List<String>?,
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
        documentDate: Long? = null,
        documentType: String? = null
    )
    suspend fun deleteRecords(ids: List<String>)
    suspend fun getLatestRecordUpdatedAt(ownerId: String, filterId: String?): Long?
    suspend fun insertRecordFile(file: RecordFile): Long
    suspend fun getRecordFile(localId: String): List<RecordFile>?
}