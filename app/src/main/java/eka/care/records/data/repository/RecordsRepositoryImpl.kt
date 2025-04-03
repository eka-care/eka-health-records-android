package eka.care.records.data.repository

import android.content.Context
import androidx.sqlite.db.SupportSQLiteQueryBuilder
import eka.care.documents.data.db.database.DocumentDatabase
import eka.care.records.client.Logger
import eka.care.records.client.model.RecordModel
import eka.care.records.client.model.SortOrder
import eka.care.records.client.repository.RecordsRepository
import eka.care.records.data.entity.RecordEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

internal class RecordsRepositoryImpl(context: Context) : RecordsRepository {
    private var dao = DocumentDatabase.getInstance(context).recordsDao()
    private var recordFilesDao = DocumentDatabase.getInstance(context).recordFilesDao()

    override suspend fun createRecords(records: List<RecordEntity>) {
        dao.createRecords(records)
    }

    override fun readRecords(
        ownerId: String,
        filterIds: List<String>?,
        includeDeleted: Boolean,
        documentType: String?,
        sortOrder: SortOrder,
    ): Flow<List<RecordModel>> = flow {
        Logger.i("ReadRecords with ownerId: $ownerId, filterIds: $filterIds, includeDeleted: $includeDeleted, documentType: $documentType, sortOrder: $sortOrder")
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

            Logger.i("Query: ${query.sql}")
            Logger.i("Params: ${selectionArgs.joinToString(",")}")

            val dataFlow = dao.readRecords(query).map { records ->
                records.map {
                    RecordModel(
                        id = it.id,
                        thumbnail = it.thumbnail,
                        createdAt = it.createdAt,
                        updatedAt = it.updatedAt,
                        documentType = it.documentType,
                        documentDate = it.documentDate,
                        smartReport = it.smartReport
                    )
                }
            }
            emitAll(dataFlow)
        } catch (e: Exception) {
            Logger.e(e.localizedMessage ?: "Error reading records")
            emit(emptyList())
        }
    }

    override suspend fun getRecordByDocumentId(id: String) = dao.getRecordByDocumentId(id = id)

    override suspend fun updateRecords(records: List<RecordEntity>) {
        dao.updateRecords(records)
    }

    override suspend fun deleteRecords(ids: List<String>) {
        dao.deleteRecords(ids)
    }

    override suspend fun getLatestRecordUpdatedAt(ownerId: String, filterId: String?): Long? {
        return dao.getLatestRecordUpdatedAt(ownerId = ownerId, filterId = filterId)
    }
}