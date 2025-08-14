package eka.care.records.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import eka.care.records.client.model.EventLog
import eka.care.records.client.utils.Records
import eka.care.records.client.utils.RecordsUtility.Companion.downloadThumbnail
import eka.care.records.data.entity.RecordEntity
import eka.care.records.data.remote.dto.response.CaseItem
import eka.care.records.data.remote.dto.response.GetFilesResponse
import eka.care.records.data.remote.dto.response.Item
import eka.care.records.data.repository.EncountersRepository
import eka.care.records.data.repository.RecordsRepositoryImpl
import eka.care.records.data.repository.SyncRecordsRepository
import eka.care.records.data.utility.LoggerConstant.Companion.DOCUMENT_ID
import eka.care.records.data.utility.LoggerConstant.Companion.FILTER_ID
import eka.care.records.data.utility.LoggerConstant.Companion.OWNER_ID
import eka.care.records.data.utility.LoggerConstant.Companion.UPDATED_AT
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
    private val encountersRepository = EncountersRepository()

    @OptIn(ExperimentalCoroutinesApi::class)
    val dbDispatcher = Dispatchers.IO.limitedParallelism(8)

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            val ownerId = inputData.getString("ownerId") ?: return@withContext Result.failure()
            val filterIds = inputData.getStringArray("filterIds")?.toList() ?: emptyList()
            Records.logEvent(
                EventLog(
                    params = JSONObject().also { param ->
                        param.put(OWNER_ID, ownerId)
                        param.put(FILTER_ID, filterIds.joinToString(","))
                    },
                    "Starting sync for ownerId: $ownerId, filterIds: $filterIds"
                )
            )

            fetchRecords(
                ownerId = ownerId,
                filterIds = filterIds
            )
            fetchCases(
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
                    params = JSONObject().also { param ->
                        param.put(OWNER_ID, ownerId)
                        param.put(FILTER_ID, filterId)
                        param.put(UPDATED_AT, updatedAt)
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
        val records = mutableListOf<Item>()
        var currentOffset = offset
        do {
            val response = syncRepository.getRecords(
                updatedAt = updatedAt ?: 0L,
                offset = currentOffset,
                oid = filterId
            )
            if (response?.body() == null) {
                break
            }
            response.body()?.let<GetFilesResponse, Unit> {
                records.addAll(it.items)
                currentOffset = it.nextToken
            }
        } while (!currentOffset.isNullOrEmpty())
        Records.logEvent(
            EventLog(
                params = JSONObject().also { param ->
                    param.put(OWNER_ID, ownerId)
                    param.put(FILTER_ID, filterId)
                    param.put(UPDATED_AT, updatedAt)
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
            recordToStore = if (record?.isDirty == true || record?.isDeleted == true) {
                record
            } else {
                RecordEntity(
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
            return recordToStore
        }
        if (record != null) {
            recordsRepository.updateRecords(
                listOf(getRecordEntity(record.id))
            )
            Records.logEvent(
                EventLog(
                    params = JSONObject().also { param ->
                        param.put(OWNER_ID, ownerId)
                        param.put(FILTER_ID, recordItem.patientId)
                        param.put(DOCUMENT_ID, record.id)
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
                    params = JSONObject().also { param ->
                        param.put(OWNER_ID, ownerId)
                        param.put(FILTER_ID, recordItem.patientId)
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
        if (record == null) {
            return
        }
        val path = downloadThumbnail(thumbnail, context = context)
        recordsRepository.updateRecords(listOf(record.copy(thumbnail = path)))
    }

    private suspend fun fetchCases(ownerId: String, filterIds: List<String>) {
        (filterIds.ifEmpty { listOf(null) }).forEach { filterId ->
            val updatedAt = recordsRepository.getLatestCaseUpdatedAt(ownerId, filterId)
            Records.logEvent(
                EventLog(
                    params = JSONObject().also { param ->
                        param.put(OWNER_ID, ownerId)
                        param.put(FILTER_ID, filterId)
                        param.put(UPDATED_AT, updatedAt)
                    },
                    "Cases updated till: $updatedAt"
                )
            )
            filterId?.let {
                fetchCasesFromServer(
                    updatedAt = updatedAt,
                    filterId = it,
                    ownerId = ownerId
                )
            }
        }
    }

    private suspend fun fetchCasesFromServer(
        offset: String? = null,
        updatedAt: Long? = null,
        filterId: String,
        ownerId: String
    ) {
        val cases = mutableListOf<CaseItem>()
        var currentOffset = offset
        do {
            val response = encountersRepository.getCases(
                updatedAt = updatedAt ?: 0L,
                offset = currentOffset,
                oid = filterId
            )
            if (response == null) {
                return
            }
            response.cases?.let { cases.addAll(it) }
            currentOffset = response.nextToken
        } while (!currentOffset.isNullOrEmpty())
        Records.logEvent(
            EventLog(
                params = JSONObject().also { param ->
                    param.put(OWNER_ID, ownerId)
                    param.put(FILTER_ID, filterId)
                    param.put(UPDATED_AT, updatedAt)
                },
                "Got cases for: owner: $ownerId, filterIds: $filterId, data size: ${cases.size}"
            )
        )
        storeCases(cases = cases, ownerId = ownerId, filterId = filterId)
    }

    private suspend fun storeCases(
        cases: List<CaseItem>,
        ownerId: String,
        filterId: String
    ) = supervisorScope {
        cases.forEach {
            launch(dbDispatcher) { storeCase(case = it, ownerId = ownerId, filterId = filterId) }
        }
    }

    private suspend fun storeCase(case: CaseItem, ownerId: String, filterId: String) {
        val caseRecord = recordsRepository.getCaseByCaseId(case.id)
        if (caseRecord != null) {
            recordsRepository.updateCase(
                caseId = caseRecord.caseId,
                name = case.itemDetails?.displayName ?: "Unknown Case",
                type = case.itemDetails?.type ?: "unknown",
            )
        } else {
            recordsRepository.createCase(
                caseId = case.id,
                ownerId = ownerId,
                filterId = filterId,
                name = case.itemDetails?.displayName ?: "Unknown Case",
                type = case.itemDetails?.type ?: "unknown",
                isSynced = true
            )
        }
    }
}