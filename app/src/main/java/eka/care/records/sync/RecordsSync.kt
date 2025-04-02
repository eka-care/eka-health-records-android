package eka.care.records.sync

import android.app.Application
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import eka.care.documents.sync.data.remote.dto.response.GetFilesResponse
import eka.care.documents.sync.data.repository.SyncRecordsRepository
import eka.care.documents.ui.utility.RecordsUtility.Companion.downloadThumbnail
import eka.care.records.data.entity.RecordEntity
import eka.care.records.data.repository.RecordsRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class RecordsSync(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    private val syncRepository = SyncRecordsRepository(appContext as Application)
    private val recordsRepository = RecordsRepositoryImpl(appContext as Application)

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            val ownerId = inputData.getString("ownerId") ?: return@withContext Result.failure()
            val filterIds = inputData.getStringArray("filterIds")?.toList() ?: emptyList()

            (filterIds.ifEmpty { listOf(null) }).forEach { filterId ->
                val updatedAt = recordsRepository.getLatestRecordUpdatedAt(ownerId, filterId)
                fetchRecordsFromServer(updatedAt = updatedAt, filterId = filterId, ownerId = ownerId)
            }

            Result.success()
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
                updatedAt = updatedAt.toString(),
                offset = currentOffset,
                oid = filterId
            )
            response?.body()?.let<GetFilesResponse, Unit> {
                storeRecords(
                    recordsResponse = it,
                    ownerId = ownerId,
                    context = applicationContext
                )
                currentOffset = it.nextToken
            }
        } while (!currentOffset.isNullOrEmpty())
    }

    private suspend fun storeRecords(
        recordsResponse: GetFilesResponse,
        ownerId: String,
        context: Context
    ) {
        val records = mutableListOf<RecordEntity>()
        recordsResponse.items.forEach {
            val recordItem = it.record.item
            val record = recordsRepository.getRecordByDocumentId(recordItem.documentId)
            if (record != null) {
                recordsRepository.updateRecords(
                    listOf(
                        RecordEntity(
                            id = record.id,
                            ownerId = ownerId,
                            recordId = recordItem.documentId,
                            filterId = recordItem.patientId,
                            createdAt = recordItem.uploadDate ?: 0L,
                            updatedAt = System.currentTimeMillis(),
                            documentDate = recordItem.metadata?.documentDate,
                            documentType = recordItem.documentType ?: "ot",
                        )
                    )
                )
            } else {
                records.add(
                    RecordEntity(
                        id = UUID.randomUUID().toString(),
                        ownerId = ownerId,
                        recordId = recordItem.documentId,
                        filterId = recordItem.patientId,
                        createdAt = recordItem.uploadDate ?: 0L,
                        updatedAt = System.currentTimeMillis(),
                        documentDate = recordItem.metadata?.documentDate,
                        documentType = recordItem.documentType ?: "ot",
                    )
                )
            }
        }

        recordsRepository.createRecords(records)
        storeThumbnails(recordsResponse = recordsResponse, context = context)
    }

    private suspend fun storeThumbnails(recordsResponse: GetFilesResponse?, context: Context) {
        recordsResponse?.items?.forEach {
            val path = downloadThumbnail(it.record.item.metadata?.thumbnail, context = context)
            val documentId = it.record.item.documentId
            recordsRepository.getRecordByDocumentId(documentId)?.let {
                recordsRepository.updateRecords(
                    listOf(it.copy(id = it.id, thumbnail = path))
                )
            }
        }
    }
}