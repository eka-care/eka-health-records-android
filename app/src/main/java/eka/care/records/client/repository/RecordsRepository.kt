package eka.care.records.client.repository

import eka.care.records.client.model.SortOrder
import eka.care.records.data.entity.RecordEntity
import kotlinx.coroutines.flow.Flow

interface RecordsRepository {
    suspend fun createRecords(records: List<RecordEntity>)
    fun readRecords(
        ownerId: String,
        filterIds: List<String>?,
        includeDeleted: Boolean,
        documentType: String?,
        sortOrder: SortOrder
    ): Flow<List<Record>>
    suspend fun getRecordByDocumentId(id: String): RecordEntity?
    suspend fun updateRecords(records: List<RecordEntity>)
    suspend fun deleteRecords()
    suspend fun getLatestRecordUpdatedAt(ownerId: String, filterId: String?): Long?
}