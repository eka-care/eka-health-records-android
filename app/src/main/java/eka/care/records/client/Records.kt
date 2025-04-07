package eka.care.records.client

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import eka.care.records.client.model.RecordModel
import eka.care.records.client.model.SortOrder
import eka.care.records.data.entity.RecordEntity
import eka.care.records.data.repository.RecordsRepositoryImpl
import eka.care.records.sync.RecordsSync
import kotlinx.coroutines.flow.Flow
import java.io.File

class Records private constructor() {
    private lateinit var recordsRepository: RecordsRepositoryImpl

    companion object {
        @Volatile
        private var INSTANCE: Records? = null

        fun getInstance(
            context: Context,
            token: String
        ): Records {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildClient().also {
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

    fun syncData(context: Context, ownerId: String?, filterIds: List<String>? = null) {
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

    suspend fun createRecord(
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

    suspend fun addRecords(records: List<RecordEntity>) {
        recordsRepository.createRecords(records = records)
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

    suspend fun getRecordDetails(documentId: String): RecordModel? {
        return recordsRepository.getRecordDetails(documentId = documentId)
    }

    suspend fun updateRecords(records: List<RecordEntity>) {
        recordsRepository.updateRecords(records = records)
    }

    suspend fun deleteRecords(ids: List<String>) {
        recordsRepository.deleteRecords(ids = ids)
    }
}