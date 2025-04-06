package eka.care.records.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import eka.care.documents.sync.data.remote.dto.response.GetFilesResponse
import eka.care.documents.sync.data.remote.dto.response.Item
import eka.care.documents.sync.data.repository.MyFileRepository
import eka.care.documents.sync.data.repository.SyncRecordsRepository
import eka.care.documents.ui.utility.RecordsUtility
import eka.care.documents.ui.utility.RecordsUtility.Companion.downloadThumbnail
import eka.care.records.client.Logger
import eka.care.records.data.entity.RecordEntity
import eka.care.records.data.entity.RecordFile
import eka.care.records.data.repository.RecordsRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import java.util.UUID

class RecordsSync(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    private val syncRepository = SyncRecordsRepository(appContext.applicationContext)
    private val recordsRepository = RecordsRepositoryImpl(appContext.applicationContext)
    private val myFileRepository = MyFileRepository()
    @OptIn(ExperimentalCoroutinesApi::class)
    private val limitedDispatcher = Dispatchers.IO.limitedParallelism(5)

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            fetchRecords()
            syncLocalRecords()
            Result.success()
        }
    }

    private suspend fun fetchRecords() {
        val ownerId = inputData.getString("ownerId") ?: return

        val filterIds = inputData.getStringArray("filterIds")?.toList() ?: emptyList()
        Logger.i("Fetching records for ownerId: $ownerId, filterIds: $filterIds")

        (filterIds.ifEmpty { listOf(null) }).forEach { filterId ->
            val updatedAt = recordsRepository.getLatestRecordUpdatedAt(ownerId, filterId)
            Logger.i("Records updated till: $updatedAt")
            fetchRecordsFromServer(
                updatedAt = updatedAt,
                filterId = filterId,
                ownerId = ownerId
            )
        }
    }

    private fun syncLocalRecords() {
        // Sync documents with is_dirty flags
        // Sync documents with no details, like file path or thumbnail
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
                Logger.i("Got records for: owner: $ownerId, filterIds: $filterId, data: $it")
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
                updatedAt = System.currentTimeMillis() / 1000,
                documentDate = recordItem.metadata?.documentDate,
                documentType = recordItem.documentType ?: "ot",
            )
        }
        if (record != null) {
            recordsRepository.updateRecords(
                listOf(getRecordEntity(record.id))
            )
            Logger.i("Updated record for ownerId: $ownerId")
        } else {
            recordsRepository.createRecords(
                listOf(getRecordEntity(id = UUID.randomUUID().toString()))
            )
            Logger.i("Stored record for ownerId: $ownerId")
        }
        storeThumbnail(
            recordId = recordItem.documentId,
            thumbnail = recordItem.metadata?.thumbnail,
            context = applicationContext
        )
        updateRecordWithDetails(recordItem.documentId)
    }

    private suspend fun updateRecordWithDetails(documentId: String) {
        val record = recordsRepository.getRecordByDocumentId(documentId) ?: return

        val response = myFileRepository.getDocument(
            documentId = documentId,
            filterId = record.filterId
        )
        if (response == null) {
            return
        }

        supervisorScope {
            launch {
                val smartReportField = response.smartReport?.let {
                    Gson().toJson(it)
                }
                val updatedRecord = record.copy(
                    smartReport = smartReportField
                )
                recordsRepository.updateRecords(listOf(updatedRecord))
            }
            response.files.forEach { file ->
                launch {
                    val fileType = response.files.firstOrNull()?.fileType ?: ""
                    val filePath = RecordsUtility.downloadFile(
                        file.assetUrl,
                        context = applicationContext,
                        type = file.fileType
                    )
                    recordsRepository.insertRecordFile(
                        RecordFile(
                            localId = record.id,
                            filePath = filePath,
                            fileType = fileType
                        )
                    )
                }
            }
        }
    }

    private suspend fun storeThumbnail(recordId: String, thumbnail: String?, context: Context) {
        if (thumbnail.isNullOrEmpty()) {
            return
        }
        val localRecord = recordsRepository.getRecordByDocumentId(recordId) ?: return
        val path = downloadThumbnail(thumbnail, context = context)
        recordsRepository.updateRecords(listOf(localRecord.copy(thumbnail = path)))
        Logger.i("Stored thumbnail for: $recordId, thumbnail: $thumbnail")
    }
}