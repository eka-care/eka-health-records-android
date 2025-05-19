package eka.care.records.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import eka.care.records.client.model.EventLog
import eka.care.records.client.utils.Records
import eka.care.records.client.utils.RecordsUtility.Companion.downloadThumbnail
import eka.care.records.data.contract.LogInterceptor
import eka.care.records.data.entity.RecordEntity
import eka.care.records.data.remote.dto.response.GetFilesResponse
import eka.care.records.data.remote.dto.response.Item
import eka.care.records.data.repository.RecordsRepositoryImpl
import eka.care.records.data.repository.SyncRecordsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.UUID

class RecordsSync(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    private val syncRepository = SyncRecordsRepository()
    private val recordsRepository = RecordsRepositoryImpl(appContext.applicationContext)
    @OptIn(ExperimentalCoroutinesApi::class)
    private val limitedDispatcher = Dispatchers.IO.limitedParallelism(5)
    private val logger: LogInterceptor? = Records.getInstance(appContext, "").logger

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            val ownerId = inputData.getString("ownerId") ?: return@withContext Result.failure()
            val filterIds = inputData.getStringArray("filterIds")?.toList() ?: emptyList()
            Records.logEvent(
                EventLog(
                    params = JSONObject().also {
                        it.put("ownerId", ownerId)
                        it.put("filterIds", filterIds)
                        it.put("time", System.currentTimeMillis())
                    },
                    "Starting sync for ownerId: $ownerId, filterIds: $filterIds"
                )
            )

            fetchRecords(
                ownerId = ownerId,
                filterIds = filterIds
            )
            recordsRepository.startAutoSync(ownerId = ownerId)
            Result.success()
        }
    }

    private suspend fun fetchRecords(ownerId: String, filterIds: List<String>) {
        (filterIds.ifEmpty { listOf(null) }).forEach { filterId ->
            val updatedAt = recordsRepository.getLatestRecordUpdatedAt(ownerId, filterId)
            Records.logEvent(
                EventLog(
                    params = JSONObject().also {
                        it.put("ownerId", ownerId)
                        it.put("filterId", filterId)
                        it.put("updatedAt", updatedAt)
                        it.put("time", System.currentTimeMillis())
                    },
                    "Records updated till: $updatedAt"
                )
            )
            fetchRecordsFromServer(
                updatedAt = updatedAt,
                filterId = filterId,
                ownerId = ownerId
            )
        }
    }

    private suspend fun fetchRecordsFromServer(
        offset: String? = null,
        updatedAt: Long? = null,
        filterId: String?,
        ownerId: String
    ) {
        var currentOffset = offset
        do {
            val response = syncRepository.getRecords(
                updatedAt = updatedAt ?: 0L,
                offset = currentOffset,
                oid = filterId
            )
            response?.body()?.let<GetFilesResponse, Unit> {
                Records.logEvent(
                    EventLog(
                        params = JSONObject().also {
                            it.put("ownerId", ownerId)
                            it.put("filterId", filterId)
                            it.put("updatedAt", updatedAt)
                            it.put("time", System.currentTimeMillis())
                        },
                        "Got records for: owner: $ownerId, filterIds: $filterId, data: $it"
                    )
                )
                storeRecords(recordsResponse = it, ownerId = ownerId)
                currentOffset = it.nextToken
            }
        } while (!currentOffset.isNullOrEmpty())
    }

    private suspend fun storeRecords(
        recordsResponse: GetFilesResponse,
        ownerId: String,
    ) = supervisorScope {
        recordsResponse.items.forEach {
            launch(limitedDispatcher) { storeRecord(record = it, ownerId = ownerId) }
        }
    }

    private suspend fun storeRecord(record: Item, ownerId: String) {
        val recordItem = record.record.item
        val record = recordsRepository.getRecordByDocumentId(recordItem.documentId)
        fun getRecordEntity(id: String): RecordEntity {
            return RecordEntity(
                id = id,
                ownerId = ownerId,
                documentId = recordItem.documentId,
                filterId = recordItem.patientId,
                createdAt = recordItem.uploadDate ?: 0L,
                updatedAt = recordItem.updatedAt ?: recordItem.uploadDate ?: 0L,
                documentDate = recordItem.metadata?.documentDate,
                documentType = recordItem.documentType ?: "ot",
                isSmart = recordItem.metadata?.autoTags?.contains("1") == true,
            )
        }
        if (record != null) {
            recordsRepository.updateRecords(
                listOf(getRecordEntity(record.id))
            )
            Records.logEvent(
                EventLog(
                    params = JSONObject().also {
                        it.put("ownerId", ownerId)
                        it.put("recordId", record.id)
                        it.put("time", System.currentTimeMillis())
                    },
                    message = "Updated record for ownerId: $ownerId"
                )
            )
        } else {
            recordsRepository.createRecords(
                listOf(getRecordEntity(id = UUID.randomUUID().toString()))
            )
            Records.logEvent(
                EventLog(
                    params = JSONObject().also {
                        it.put("ownerId", ownerId)
                        it.put("recordId", recordItem.documentId)
                        it.put("time", System.currentTimeMillis())
                    },
                    message = "Stored record for ownerId: $ownerId"
                )
            )
        }
        storeThumbnail(
            recordId = recordItem.documentId,
            thumbnail = recordItem.metadata?.thumbnail,
            context = applicationContext
        )
    }

    private suspend fun storeThumbnail(recordId: String, thumbnail: String?, context: Context) {
        if (thumbnail.isNullOrEmpty()) {
            return
        }
        val localRecord = recordsRepository.getRecordByDocumentId(recordId) ?: return
        val path = downloadThumbnail(thumbnail, context = context)
        recordsRepository.updateRecords(listOf(localRecord.copy(thumbnail = path)))
        Records.logEvent(
            EventLog(
                params = JSONObject().also {
                    it.put("recordId", recordId)
                    it.put("thumbnail", thumbnail)
                    it.put("time", System.currentTimeMillis())
                },
                message = "Stored thumbnail for: $recordId, thumbnail: $thumbnail"
            )
        )
    }
}