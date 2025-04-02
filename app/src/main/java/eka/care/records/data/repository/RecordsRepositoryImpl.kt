package eka.care.records.data.repository

import android.content.Context
import androidx.sqlite.db.SupportSQLiteQueryBuilder
import eka.care.documents.data.db.database.DocumentDatabase
import eka.care.records.client.Logger
import eka.care.records.client.model.SortOrder
import eka.care.records.client.repository.RecordsRepository
import eka.care.records.data.entity.RecordEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

internal class RecordsRepositoryImpl(context: Context) : RecordsRepository {
    private var dao = DocumentDatabase.getInstance(context).recordsDao()

    override suspend fun createRecords(records: List<RecordEntity>) {
        dao.createRecords(records)
    }

    override fun readRecords(
        ownerId: String,
        filterIds: List<String>?,
        includeDeleted: Boolean,
        documentType: String?,
        sortOrder: SortOrder,
    ): Flow<List<Record>> = flow {
        try {
            val selection = StringBuilder()
            val selectionArgs = mutableListOf<String>()

            if (!includeDeleted) {
                selection.append("IS_DELETED = 0 AND ")
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

            Logger.i("Query: ${query.sql}")

            dao.readRecords(query)
        } catch (e: Exception) {
            Logger.e(e.localizedMessage ?: "Error reading records")
            emit(emptyList())
        }
    }

    override suspend fun getRecordByDocumentId(id: String) = dao.getRecordByDocumentId(id = id)

    override suspend fun updateRecords(records: List<RecordEntity>) {
        dao.updateRecords(records)
    }

    override suspend fun deleteRecords() {

    }

    override suspend fun getLatestRecordUpdatedAt(ownerId: String, filterId: String?): Long? {
        return dao.getLatestRecordUpdatedAt(ownerId = ownerId, filterId = filterId)
    }
}