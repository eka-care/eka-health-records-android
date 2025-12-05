package eka.care.records.sync

import android.content.Context
import androidx.lifecycle.Observer
import androidx.work.CoroutineWorker
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import eka.care.records.client.model.EventLog
import eka.care.records.client.utils.Records
import eka.care.records.client.utils.RecordsUtility.Companion.downloadThumbnail
import eka.care.records.client.utils.RecordsUtility.Companion.getWorkerTag
import eka.care.records.data.entity.CaseStatus
import eka.care.records.data.entity.CaseUiState
import eka.care.records.data.entity.RecordEntity
import eka.care.records.data.remote.dto.response.CaseItem
import eka.care.records.data.remote.dto.response.Item
import eka.care.records.data.repository.EncountersRepository
import eka.care.records.data.repository.RecordsRepositoryImpl
import eka.care.records.data.repository.SyncRecordsRepository
import eka.care.records.data.utility.LoggerConstant.Companion.BUSINESS_ID
import eka.care.records.data.utility.LoggerConstant.Companion.DOCUMENT_ID
import eka.care.records.data.utility.LoggerConstant.Companion.OWNER_ID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext

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
            setProgress(workDataOf("syncing" to true))
            val businessId =
                inputData.getString("businessId") ?: return@withContext Result.failure()
            val ownerIds = inputData.getStringArray("ownerIds")?.toList() ?: emptyList()
            Records.logEvent(
                EventLog(
                    params = mutableMapOf<String, Any?>().also { param ->
                        param.put(BUSINESS_ID, businessId)
                        param.put(OWNER_ID, ownerIds.joinToString(","))
                    },
                    "Starting sync for businessId: $businessId, ownerIds: $ownerIds"
                )
            )
            recordsRepository.syncLocal(
                businessId = businessId
            )
            fetchCases(
                businessId = businessId,
                ownerIds = ownerIds
            )
            fetchRecords(
                businessId = businessId,
                ownerIds = ownerIds
            )
            recordsRepository.startAutoSync(businessId = businessId)
            setProgress(workDataOf("syncing" to false))
            Result.success()
        }
    }

    private suspend fun fetchRecords(businessId: String, ownerIds: List<String>) {
        ownerIds.forEach { ownerId ->
            val updatedAt = recordsRepository.getLatestRecordUpdatedAt(businessId, ownerId)
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
            launch(dbDispatcher) { storeRecord(item = it, businessId = businessId) }
        }
    }

    private suspend fun storeRecord(item: Item, businessId: String) {
        val recordItem = item.record.item
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
            recordItem.metadata?.tags?.forEach {
                recordsRepository.addTag(
                    recordId = record.documentId,
                    tag = it
                )
            }
            Records.logEvent(
                EventLog(
                    params = mutableMapOf<String, Any?>().also { param ->
                        param.put(BUSINESS_ID, businessId)
                        param.put(OWNER_ID, recordItem.patientId)
                        param.put(DOCUMENT_ID, record.documentId)
                    },
                    message = "Updated record for businessId: $businessId"
                )
            )
        } else {
            recordsRepository.createRecords(listOf(recordToStore))
            recordItem.metadata?.tags?.forEach {
                recordsRepository.addTag(
                    recordId = recordToStore.documentId,
                    tag = it
                )
            }
            Records.logEvent(
                EventLog(
                    params = mutableMapOf<String, Any?>().also { param ->
                        param.put(DOCUMENT_ID, id)
                        param.put(BUSINESS_ID, businessId)
                        param.put(OWNER_ID, recordItem.patientId)
                    },
                    message = "Stored record for businessId: $businessId"
                )
            )
        }
        item.record.item.cases?.forEach { caseId ->
            recordsRepository.assignRecordToCase(
                caseId = caseId,
                recordId = recordToStore.documentId
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
            response.cases?.let {
                storeCases(cases = it, ownerId = ownerId, businessId = businessId)
            }
            currentOffset = response.nextToken
        } while (!currentOffset.isNullOrEmpty())
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
        if (case.status == "D") {
            caseRecord?.encounter?.let {
                recordsRepository.deleteCase(it)
            }
            return
        }
        if (caseRecord != null) {
            recordsRepository.updateCase(
                caseId = caseRecord.encounter.encounterId,
                name = case.itemDetails?.displayName ?: "Unknown Case",
                type = case.itemDetails?.type ?: "unknown",
                status = CaseStatus.NONE,
                uiStatus = CaseUiState.NONE
            )
        } else {
            recordsRepository.createCase(
                caseId = case.id,
                ownerId = ownerId,
                businessId = businessId,
                name = case.itemDetails?.displayName ?: "Unknown Case",
                type = case.itemDetails?.type ?: "unknown",
                createdAt = case.itemDetails?.createdAt,
                updatedAt = case.updatedAt,
                status = CaseStatus.NONE,
                uiStatus = CaseUiState.NONE
            )
        }
    }
}

fun WorkManager.recordSyncFlow(businessId: String): Flow<WorkInfo?> = callbackFlow {
    val liveData = getWorkInfosByTagLiveData(getWorkerTag(businessId))

    val observer = Observer<List<WorkInfo>> { workInfos ->
        val activeWork = workInfos.firstOrNull()
        trySend(activeWork)
    }

    liveData.observeForever(observer)

    awaitClose {
        liveData.removeObserver(observer)
    }
}