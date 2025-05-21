package eka.care.records.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import eka.care.records.client.model.EventLog
import eka.care.records.client.utils.Records
import eka.care.records.client.utils.RecordsUtility.Companion.downloadThumbnail
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
    val dbDispatcher = Dispatchers.IO.limitedParallelism(8)
    @OptIn(ExperimentalCoroutinesApi::class)
    val thumbnailDispatcher = Dispatchers.IO.limitedParallelism(5)

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
                updatedAt = if (updatedAt != null) {
                    updatedAt - 1 // To get records updated after the latest
                } else {
                    null
                },
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
        val records = mutableListOf<Item>()
        var currentOffset = offset
        do {
            val response = syncRepository.getRecords(
                updatedAt = updatedAt ?: 0L,
                offset = currentOffset,
                oid = filterId
            )
            if(response?.body() == null) {
                break
            }
            response.body()?.let<GetFilesResponse, Unit> {
                records.addAll(it.items)
                currentOffset = it.nextToken
            }
        } while (!currentOffset.isNullOrEmpty())
        Records.logEvent(
            EventLog(
                params = JSONObject().also {
                    it.put("ownerId", ownerId)
                    it.put("filterId", filterId)
                    it.put("updatedAt", updatedAt)
                    it.put("time", System.currentTimeMillis())
                },
                "Got records for: owner: $ownerId, filterIds: $filterId, data size: ${records.size}"
            )
        )
        storeRecords(records = records, ownerId = ownerId)
    }

    private suspend fun storeRecords(
        records: List<Item>,
        ownerId: String
    ) = supervisorScope {
        records.forEach {
            launch(dbDispatcher) { storeRecord(record = it, ownerId = ownerId) }
        }
    }

    private suspend fun storeRecord(record: Item, ownerId: String) {
        val recordItem = record.record.item
        val record = recordsRepository.getRecordByDocumentId(recordItem.documentId)
        var recordToStore = record
        fun getRecordEntity(id: String): RecordEntity {
            recordToStore = RecordEntity(
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
            return recordToStore
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
            record = recordToStore,
            thumbnail = recordItem.metadata?.thumbnail,
            context = applicationContext
        )
    }

    private suspend fun storeThumbnail(
        record: RecordEntity?,
        thumbnail: String?,
        context: Context
    ) {
        if (thumbnail.isNullOrEmpty()) {
            return
        }
        if(record == null) {
            return
        }
        withContext(thumbnailDispatcher) {
            val path = downloadThumbnail(thumbnail, context = context)
            recordsRepository.updateRecords(listOf(record.copy(thumbnail = path)))
        }
    }
}