package eka.care.records.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import eka.care.records.client.model.EventLog
import eka.care.records.client.utils.Records
import eka.care.records.client.utils.RecordsUtility.Companion.downloadThumbnail
import eka.care.records.data.entity.RecordEntity
import eka.care.records.data.remote.dto.response.CaseItem
import eka.care.records.data.remote.dto.response.Item
import eka.care.records.data.repository.EncountersRepository
import eka.care.records.data.repository.RecordsRepositoryImpl
import eka.care.records.data.repository.SyncRecordsRepository
import eka.care.records.data.utility.LoggerConstant.Companion.BUSINESS_ID
import eka.care.records.data.utility.LoggerConstant.Companion.DOCUMENT_ID
import eka.care.records.data.utility.LoggerConstant.Companion.OWNER_ID
import eka.care.records.data.utility.LoggerConstant.Companion.UPDATED_AT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import org.json.JSONObject

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
            val businessId =
                inputData.getString("businessId") ?: return@withContext Result.failure()
            val ownerIds = inputData.getStringArray("ownerIds")?.toList() ?: emptyList()
            Records.logEvent(
                EventLog(
                    params = JSONObject().also { param ->
                        param.put(BUSINESS_ID, businessId)
                        param.put(OWNER_ID, ownerIds.joinToString(","))
                    },
                    "Starting sync for businessId: $businessId, ownerIds: $ownerIds"
                )
            )

            fetchRecords(
                businessId = businessId,
                ownerIds = ownerIds
            )
            fetchCases(
                businessId = businessId,
                ownerIds = ownerIds
            )
            recordsRepository.startAutoSync(businessId = businessId)
            Result.success()
        }
    }

    private suspend fun fetchRecords(businessId: String, ownerIds: List<String>) {
        ownerIds.forEach { ownerId ->
            val updatedAt = recordsRepository.getLatestRecordUpdatedAt(businessId, ownerId)
            Records.logEvent(
                EventLog(
                    params = JSONObject().also { param ->
                        param.put(BUSINESS_ID, businessId)
                        param.put(OWNER_ID, ownerId)
                        param.put(UPDATED_AT, updatedAt)
                    },
                    "Records updated till: $updatedAt"
                )
            )
            fetchRecordsFromServer(
                updatedAt = updatedAt,
                businessId = businessId,
                ownerId = ownerId
            )
        }
    }

    private suspend fun fetchRecordsFromServer(
        offset: String? = null,
        updatedAt: Long? = null,
        businessId: String,
        ownerId: String
    ) {
        var currentOffset = offset
        do {
            val response = syncRepository.getRecords(
                updatedAt = updatedAt ?: 0L,
                offset = currentOffset,
                oid = ownerId
            )
            if (response?.body() == null) {
                break
            }
            response.body()?.let {
                storeRecords(records = it.items, businessId = businessId)
                currentOffset = it.nextToken
            }
        } while (!currentOffset.isNullOrEmpty())
    }

    private suspend fun storeRecords(
        records: List<Item>,
        businessId: String
    ) = supervisorScope {
        records.forEach {
            launch(dbDispatcher) { storeRecord(record = it, businessId = businessId) }
        }
    }

    private suspend fun storeRecord(record: Item, businessId: String) {
        val recordItem = record.record.item
        val record = recordsRepository.getRecordByDocumentId(recordItem.documentId)
        val recordToStore = RecordEntity(
            documentId = record?.documentId ?: recordItem.documentId,
            ownerId = recordItem.patientId ?: "",
            businessId = businessId,
            createdAt = recordItem.uploadDate ?: 0L,
            updatedAt = recordItem.updatedAt ?: recordItem.uploadDate ?: 0L,
            documentDate = recordItem.metadata?.documentDate,
            documentType = recordItem.documentType ?: "ot",
            isSmart = recordItem.metadata?.autoTags?.contains("1") == true,
        )
        if (record != null) {
            recordsRepository.updateRecord(recordToStore)
            Records.logEvent(
                EventLog(
                    params = JSONObject().also { param ->
                        param.put(BUSINESS_ID, businessId)
                        param.put(OWNER_ID, recordItem.patientId)
                        param.put(DOCUMENT_ID, record.documentId)
                    },
                    message = "Updated record for businessId: $businessId"
                )
            )
        } else {
            recordsRepository.createRecords(listOf(recordToStore))
            Records.logEvent(
                EventLog(
                    params = JSONObject().also { param ->
                        param.put(DOCUMENT_ID, id)
                        param.put(BUSINESS_ID, businessId)
                        param.put(OWNER_ID, recordItem.patientId)
                    },
                    message = "Stored record for businessId: $businessId"
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
        recordsRepository.updateRecord(record.copy(thumbnail = path))
    }

    private suspend fun fetchCases(businessId: String, ownerIds: List<String>) {
        ownerIds.forEach { ownerId ->
            val updatedAt = recordsRepository.getLatestCaseUpdatedAt(businessId, ownerId)
            Records.logEvent(
                EventLog(
                    params = JSONObject().also { param ->
                        param.put(OWNER_ID, ownerId)
                        param.put(BUSINESS_ID, businessId)
                        param.put(UPDATED_AT, updatedAt)
                    },
                    "Cases updated till: $updatedAt"
                )
            )
            fetchCasesFromServer(
                updatedAt = updatedAt,
                businessId = businessId,
                ownerId = ownerId
            )
        }
    }

    private suspend fun fetchCasesFromServer(
        offset: String? = null,
        updatedAt: Long? = null,
        businessId: String,
        ownerId: String
    ) {
        val cases = mutableListOf<CaseItem>()
        var currentOffset = offset
        do {
            val response = encountersRepository.getCases(
                updatedAt = updatedAt ?: 0L,
                offset = currentOffset,
                oid = ownerId
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
                    param.put(BUSINESS_ID, businessId)
                    param.put(UPDATED_AT, updatedAt)
                },
                "Got cases for: owner: $ownerId, businessId: $businessId, data size: ${cases.size}"
            )
        )
        storeCases(cases = cases, ownerId = ownerId, businessId = businessId)
    }

    private suspend fun storeCases(
        businessId: String,
        ownerId: String,
        cases: List<CaseItem>
    ) = supervisorScope {
        cases.forEach {
            launch(dbDispatcher) {
                storeCase(
                    case = it,
                    ownerId = ownerId,
                    businessId = businessId
                )
            }
        }
    }

    private suspend fun storeCase(case: CaseItem, ownerId: String, businessId: String) {
        val caseRecord = recordsRepository.getCaseByCaseId(case.id)
        if (caseRecord != null) {
            recordsRepository.updateCase(
                caseId = caseRecord.encounter.encounterId,
                name = case.itemDetails?.displayName ?: "Unknown Case",
                type = case.itemDetails?.type ?: "unknown",
            )
        } else {
            recordsRepository.createCase(
                caseId = case.id,
                ownerId = ownerId,
                businessId = businessId,
                name = case.itemDetails?.displayName ?: "Unknown Case",
                type = case.itemDetails?.type ?: "unknown",
                isSynced = true
            )
        }
    }
}