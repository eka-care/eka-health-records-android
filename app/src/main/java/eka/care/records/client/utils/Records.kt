package eka.care.records.client.utils

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import eka.care.records.client.model.DocumentTypeCount
import eka.care.records.client.model.RecordModel
import eka.care.records.client.model.SortOrder
import eka.care.records.data.db.RecordsDatabase
import eka.care.records.data.repository.RecordsRepositoryImpl
import eka.care.records.sync.RecordsSync
import kotlinx.coroutines.flow.Flow
import java.io.File

class Records private constructor() {
    private lateinit var recordsRepository: RecordsRepositoryImpl
    private lateinit var db: RecordsDatabase

    companion object {
        @Volatile
        private var INSTANCE: Records? = null

        fun getInstance(
            context: Context,
            token: String
        ): Records {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildClient().also {
                    it.db = RecordsDatabase.getInstance(context)
                    it.recordsRepository = RecordsRepositoryImpl(context)
                    INSTANCE = it
                }
            }
        }

        private fun buildClient(): Records {
            return Records()
        }

        fun enableLogging() {
            Logger.loggingEnabled = true
        }
    }

    fun refreshRecords(context: Context, ownerId: String?, filterIds: List<String>? = null) {
        val inputData = Data.Builder()
            .putString("ownerId", ownerId)
            .putStringArray("filterIds", filterIds?.toTypedArray() ?: emptyArray())
            .build()
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest =
            OneTimeWorkRequestBuilder<RecordsSync>()
                .setInputData(inputData)
                .setConstraints(constraints)
                .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                "sync_records",
                ExistingWorkPolicy.KEEP,
                syncRequest
            )
    }

    suspend fun addNewRecord(
        files: List<File>,
        ownerId: String,
        filterId: String? = null,
        documentType: String = "ot"
    ) {
        recordsRepository.createRecords(
            files = files,
            ownerId = ownerId,
            filterId = filterId,
            documentType = documentType
        )
    }

    fun fetchRecords(
        ownerId: String,
        filterIds: List<String>? = null,
        includeDeleted: Boolean = false,
        documentType: String? = null,
        sortOrder: SortOrder,
    ): Flow<List<RecordModel>> {
        return recordsRepository.readRecords(
            ownerId = ownerId,
            filterIds = filterIds,
            includeDeleted = includeDeleted,
            documentType = documentType,
            sortOrder = sortOrder
        )
    }

    fun getRecordsCountGroupByType(
        ownerId: String,
        filterIds: List<String>? = null
    ): Flow<List<DocumentTypeCount>> {
        return recordsRepository.getRecordTypeCounts(
            ownerId = ownerId,
            filterIds = filterIds
        )
    }

    suspend fun updateRecord(
        id: String,
        documentDate: Long? = null,
        documentType: String? = null,
    ) {
        recordsRepository.updateRecord(
            id = id,
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

    fun clearAllData() {
        db.clearAllTables()
    }
}