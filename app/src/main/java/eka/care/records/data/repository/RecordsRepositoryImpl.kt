package eka.care.records.data.repository

import android.app.Application
import android.content.Context
import androidx.sqlite.db.SupportSQLiteQueryBuilder
import com.google.gson.Gson
import eka.care.records.client.model.DocumentTypeCount
import eka.care.records.client.model.RecordModel
import eka.care.records.client.model.SortOrder
import eka.care.records.client.repository.RecordsRepository
import eka.care.records.client.utils.Logger
import eka.care.records.client.utils.RecordsUtility
import eka.care.records.client.utils.ThumbnailGenerator
import eka.care.records.data.db.RecordsDatabase
import eka.care.records.data.entity.RecordEntity
import eka.care.records.data.entity.RecordFile
import id.zelory.compressor.Compressor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import java.io.File
import java.util.UUID

internal class RecordsRepositoryImpl(private val context: Context) : RecordsRepository {
    private var dao = RecordsDatabase.getInstance(context).recordsDao()
    private var filesDao = RecordsDatabase.getInstance(context).recordFilesDao()
    private val myFileRepository = MyFileRepository()
    private var syncJob: Job? = null

    private fun startAutoSync() {
        syncJob?.cancel()
        syncJob = CoroutineScope(Dispatchers.IO).launch {
            dao.observeDirtyRecords()
                .distinctUntilChanged()
                .collect { dirtyRecords ->
                    if (dirtyRecords.isNotEmpty()) {
                        syncUpdatedRecordsToServer(dirtyRecords)
                    }
                }
            dao.observeDeletedRecords()
                .distinctUntilChanged()
                .collect { deletedRecords ->
                    if (deletedRecords.isNotEmpty()) {
                        syncDeletedRecordsToServer(deletedRecords)
                    }
                }
        }
    }

    private fun syncUpdatedRecordsToServer(dirtyRecords: List<RecordEntity>) {
        Logger.i("Syncing dirty records to server: $dirtyRecords")
    }

    private fun syncDeletedRecordsToServer(deletedRecords: List<RecordEntity>) {
        Logger.i("Syncing deleted records to server: $deletedRecords")
    }

    override suspend fun createRecords(records: List<RecordEntity>) {
        dao.createRecords(records)
    }

    override suspend fun createRecords(
        files: List<File>,
        ownerId: String,
        filterId: String?,
        documentType: String
    ) = supervisorScope {
        if (files.isEmpty()) {
            Logger.e("No files to create records")
            return@supervisorScope
        }

        val time = System.currentTimeMillis() / 1000
        val id = UUID.randomUUID().toString()
        val record = RecordEntity(
            id = id,
            ownerId = ownerId,
            filterId = filterId,
            createdAt = time,
            updatedAt = time,
            documentDate = time,
        )
        dao.createRecords(listOf(record))
        val thumbnail = if (files.first().extension.lowercase() in listOf(
                "jpg",
                "jpeg",
                "png",
                "webp"
            )
        ) {
            files.first().path
        } else {
            ThumbnailGenerator.getThumbnailFromPdf(
                app = context.applicationContext as Application,
                files.first()
            )
        }
        dao.updateRecords(listOf(record.copy(thumbnail = thumbnail)))
        files.forEach { file ->
            launch {
                val compressedFile = Compressor.compress(context, file)
                val path = compressedFile.path
                val type = compressedFile.extension
                insertRecordFile(
                    RecordFile(
                        localId = record.id,
                        filePath = path,
                        fileType = type
                    )
                )
            }
        }
    }

    override fun readRecords(
        ownerId: String,
        filterIds: List<String>?,
        includeDeleted: Boolean,
        documentType: String?,
        sortOrder: SortOrder,
    ): Flow<List<RecordModel>> = flow {
        Logger.i("ReadRecords with ownerId: $ownerId, filterIds: $filterIds, includeDeleted: $includeDeleted, documentType: $documentType, sortOrder: $sortOrder")
        startAutoSync()
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

    override suspend fun getRecordById(id: String) = dao.getRecordById(id = id)

    override suspend fun getRecordByDocumentId(id: String) = dao.getRecordByDocumentId(id = id)

    override suspend fun getRecordDetails(id: String): RecordModel? {
        val record = getRecordById(id) ?: return null
        val documentId = record.documentId ?: return null

        val files = getRecordFile(record.id)
        if (files?.isNotEmpty() == true) {
            Logger.i("Found local files for record: $id")
            return RecordModel(
                id = record.id,
                thumbnail = record.thumbnail,
                createdAt = record.createdAt,
                updatedAt = record.updatedAt,
                documentDate = record.documentDate,
                documentType = record.documentType,
                isSmart = record.isSmart,
                smartReport = null,
                files = files.map { file ->
                    RecordModel.File(
                        id = file.id,
                        filePath = file.filePath,
                        fileType = file.fileType
                    )
                }
            )
        }

        val response = myFileRepository.getDocument(
            documentId = documentId,
            filterId = record.filterId
        )
        if (response == null) {
            Logger.e("Error fetching document details for: $documentId")
            return null
        }

        val smartReportField = response.smartReport?.let {
            Gson().toJson(it)
        }
        val updatedRecord = record.copy(
            smartReport = smartReportField
        )
        updateRecords(listOf(updatedRecord))
        response.files.forEach { file ->
            val fileType = response.files.firstOrNull()?.fileType ?: ""
            val filePath = RecordsUtility.downloadFile(
                file.assetUrl,
                context = context.applicationContext,
                type = file.fileType
            )
            Logger.i("Downloaded file: $filePath for record: $documentId")
            insertRecordFile(
                RecordFile(
                    localId = record.id,
                    filePath = filePath,
                    fileType = fileType
                )
            )
            Logger.i("Inserted file: $filePath for record: $documentId")
        }
        return RecordModel(
            id = record.id,
            thumbnail = record.thumbnail,
            createdAt = record.createdAt,
            updatedAt = record.updatedAt,
            documentDate = record.documentDate,
            documentType = record.documentType,
            isSmart = record.isSmart,
            smartReport = smartReportField,
            files = getRecordFile(record.id)?.map { file ->
                RecordModel.File(
                    id = file.id,
                    filePath = file.filePath,
                    fileType = file.fileType
                )
            } ?: emptyList()
        )
    }

    override fun getRecordTypeCounts(
        ownerId: String,
        filterIds: List<String>?
    ): Flow<List<DocumentTypeCount>> = flow {
        Logger.i("GetRecordTypeCounts with ownerId: $ownerId, filterIds: $filterIds")
        try {
            val selection = StringBuilder()
            val selectionArgs = mutableListOf<String>()

            selection.append("IS_ARCHIVED = 0 AND ")
            selection.append("OWNER_ID = ? ")
            selectionArgs.add(ownerId)

            if (!filterIds.isNullOrEmpty()) {
                val placeholders = filterIds.joinToString(",") { "?" }
                selection.append("AND (FILTER_ID IN ($placeholders) OR FILTER_ID IS NULL) ")
                selectionArgs.addAll(filterIds)
            } else {
                selection.append("AND FILTER_ID IS NULL ")
            }

            val query = SupportSQLiteQueryBuilder
                .builder("EKA_RECORDS_TABLE")
                .columns(arrayOf("document_type as documentType", "COUNT(*) as count"))
                .selection(selection.toString().trim(), selectionArgs.toTypedArray())
                .groupBy("document_type")
                .create()

            Logger.i("Query: ${query.sql}")
            Logger.i("Params: ${selectionArgs.joinToString(",")}")

            emitAll(dao.getDocumentTypeCounts(query))
        } catch (e: Exception) {
            Logger.e(e.localizedMessage ?: "Error reading records")
            emit(emptyList())
        }
    }

    override suspend fun updateRecords(records: List<RecordEntity>) {
        dao.updateRecords(records)
    }

    override suspend fun updateRecord(id: String, documentDate: Long?, documentType: String?) {
        Logger.i("UpdateRecord with id: $id, documentDate: $documentDate, documentType: $documentType")
        if (documentDate == null && documentType == null) {
            return
        }

        val record = getRecordById(id) ?: return
        val updatedRecord = record.copy(
            documentDate = documentDate ?: record.documentDate,
            documentType = documentType ?: record.documentType,
            isDirty = true
        )
        dao.updateRecords(listOf(updatedRecord))
        Logger.i("Update record: $updatedRecord")
    }

    override suspend fun deleteRecords(ids: List<String>) {
        ids.forEach { id ->
            val record = getRecordById(id)
            Logger.i("DeleteRecord with id: $id")
            record?.let {
                dao.updateRecords(
                    listOf(it.copy(isDeleted = true))
                )
            }
        }
    }

    override suspend fun getLatestRecordUpdatedAt(ownerId: String, filterId: String?): Long? {
        return dao.getLatestRecordUpdatedAt(ownerId = ownerId, filterId = filterId)
    }

    override suspend fun insertRecordFile(file: RecordFile): Long {
        return filesDao.insert(recordFile = file)
    }

    override suspend fun getRecordFile(localId: String): List<RecordFile>? {
        return filesDao.getRecordFile(localId = localId)
    }
}