package eka.care.records.data.repository

import android.content.Context
import androidx.sqlite.db.SupportSQLiteQueryBuilder
import com.google.gson.Gson
import eka.care.records.client.model.DocumentTypeCount
import eka.care.records.client.model.RecordModel
import eka.care.records.client.model.RecordStatus
import eka.care.records.client.model.SortOrder
import eka.care.records.client.repository.RecordsRepository
import eka.care.records.client.utils.Logger
import eka.care.records.client.utils.RecordsUtility
import eka.care.records.client.utils.RecordsUtility.Companion.getMimeType
import eka.care.records.client.utils.RecordsUtility.Companion.md5
import eka.care.records.data.core.FileStorageManagerImpl
import eka.care.records.data.db.RecordsDatabase
import eka.care.records.data.entity.RecordEntity
import eka.care.records.data.entity.RecordFile
import eka.care.records.data.remote.dto.request.FileType
import eka.care.records.data.remote.dto.request.UpdateFileDetailsRequest
import eka.care.records.data.utility.isNetworkAvailable
import id.zelory.compressor.Compressor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import java.io.File
import java.util.UUID

internal class RecordsRepositoryImpl(private val context: Context) : RecordsRepository {
    private var dao = RecordsDatabase.getInstance(context).recordsDao()
    private val myFileRepository = MyFileRepository()
    private val fileStorageManager = FileStorageManagerImpl(context)
    private val awsRepository = AwsRepository()
    private var syncJob: Job? = null

    fun startAutoSync(ownerId: String, filterIds: List<String>) {
        syncJob?.cancel()
        syncJob = CoroutineScope(Dispatchers.IO).launch {
            dao.getDirtyRecords(ownerId, filterIds)?.also {
                syncUpdatedRecordsToServer(it)
            }
            dao.getDeletedRecords(ownerId, filterIds)?.also {
                syncDeletedRecordsToServer(it)
            }
        }
    }

    private suspend fun syncUpdatedRecordsToServer(dirtyRecords: List<RecordEntity>) =
        supervisorScope {
            Logger.i("Syncing dirty records to server: $dirtyRecords")
            dirtyRecords.forEach { record ->
                launch {
                    val documentId = record.documentId
                    if (documentId != null) {
                        if(isNetworkAvailable(context = context)) {
                            dao.updateRecords(listOf(record.copy(status = RecordStatus.SYNCING)))
                        } else {
                            dao.updateRecords(listOf(record.copy(status = RecordStatus.WAITING_FOR_NETWORK)))
                            return@launch
                        }
                        val result = myFileRepository.updateFileDetails(
                            documentId = documentId,
                            oid = record.filterId,
                            request = UpdateFileDetailsRequest(
                                filterId = record.filterId,
                                documentType = record.documentType,
                                documentDate = record.documentDate?.toString(),
                            )
                        )
                        result?.let {
                            if (it != 200) {
                                Logger.e("Error updating record: Response code: $it")
                                dao.updateRecords(listOf(record.copy(status = RecordStatus.SYNC_FAILED)))
                            } else {
                                dao.updateRecords(
                                    listOf(
                                        record.copy(
                                            status = RecordStatus.SYNC_SUCCESS,
                                            isDirty = false
                                        )
                                    )
                                )
                            }
                        }
                    } else {
                        uploadRecord(id = record.id)
                    }
                }
            }
        }

    private suspend fun syncDeletedRecordsToServer(deletedRecords: List<RecordEntity>) =
        supervisorScope {
            Logger.i("Syncing deleted records to server: $deletedRecords")
            deletedRecords.forEach { record ->
                launch {
                    record.documentId?.let {
                        val result = myFileRepository.deleteDocument(it, record.filterId)
                        if (result in (200..299)) {
                            dao.deleteRecord(record)
                        }
                    }
                }
            }
        }

    override suspend fun createRecords(records: List<RecordEntity>) {
        records.forEach {
            dao.insertRecordWithFiles(it, emptyList())
        }
    }

    override suspend fun createRecord(
        files: List<File>,
        ownerId: String,
        filterId: String?,
        documentType: String,
        documentDate: Long?,
        tags: List<String>
    ) = supervisorScope {
        if (files.isEmpty()) {
            Logger.e("No files to create records")
            return@supervisorScope
        }

        val time = System.currentTimeMillis() / 1000
        val id = UUID.randomUUID().toString()
        val thumbnail = if (files.first().extension.lowercase() in listOf(
                "jpg",
                "jpeg",
                "png",
                "webp"
            )
        ) {
            files.first().path
        } else {
            fileStorageManager.generateThumbnail(
                filePath = files.first().path
            )
        }
        val record = RecordEntity(
            id = id,
            ownerId = ownerId,
            filterId = filterId,
            thumbnail = thumbnail,
            documentType = documentType,
            createdAt = time,
            updatedAt = time,
            documentDate = documentDate ?: time,
            documentHash = files.first().md5(),
            isDirty = true,
            status = RecordStatus.WAITING_TO_UPLOAD
        )
        dao.insertRecordWithFiles(
            record = record,
            files = files.map { file ->
                val compressedFile =
                    if (file.extension.lowercase() == "pdf") file else Compressor.compress(
                        context,
                        file
                    )
                val path = compressedFile.path
                val type = compressedFile.extension
                RecordFile(
                    localId = record.id,
                    filePath = path,
                    fileType = type
                )
            }
        )
    }

    override fun readRecords(
        ownerId: String,
        filterIds: List<String>?,
        includeDeleted: Boolean,
        documentType: String?,
        sortOrder: SortOrder,
    ): Flow<List<RecordModel>> = flow {
        Logger.i("ReadRecords with ownerId: $ownerId, filterIds: $filterIds, includeDeleted: $includeDeleted, documentType: $documentType, sortOrder: $sortOrder")
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
                        thumbnail = it.thumbnail ?: dao.getRecordFile(it.id)
                            ?.firstOrNull()?.filePath,
                        status = it.status,
                        createdAt = it.createdAt,
                        updatedAt = it.updatedAt,
                        documentType = it.documentType,
                        documentDate = it.documentDate,
                        isSmart = it.isSmart,
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
        val record = getRecordById(id)
        if (record == null) {
            Logger.e("Error fetching record details for: $id")
            return null
        }
        val documentId = record.documentId
        if (documentId == null) {
            Logger.e("Error fetching documentId for record: $id")
            return null
        }

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
                    RecordModel.RecordFile(
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
                RecordModel.RecordFile(
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
                .columns(arrayOf("document_type as documentType", "COUNT(local_id) as count"))
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
        return dao.insertRecordFile(recordFile = file)
    }

    override suspend fun getRecordFile(localId: String): List<RecordFile>? {
        return dao.getRecordFile(localId = localId)
    }

    private suspend fun uploadRecord(id: String) = supervisorScope {
        val record = getRecordById(id)

        if (record == null) {
            Logger.e("Upload error: No document found for documentId: $id")
            return@supervisorScope
        }

        if(isNetworkAvailable(context = context)) {
            dao.updateRecords(listOf(record.copy(status = RecordStatus.SYNCING)))
        } else {
            dao.updateRecords(listOf(record.copy(status = RecordStatus.WAITING_FOR_NETWORK)))
            return@supervisorScope
        }

        val files = dao.getRecordFile(id)?.map { File(it.filePath) }
        if (files.isNullOrEmpty()) {
            Logger.e("Upload error: No file for the given documentId: $id")
            return@supervisorScope
        }

        val fileContentList =
            files.map { FileType(contentType = it.getMimeType() ?: "", fileSize = it.length()) }
        val uploadInitResponse =
            awsRepository.fileUploadInit(
                files = fileContentList,
                patientOid = record.filterId,
                isMultiFile = files.size > 1,
                tags = emptyList(), // TODO add tags from the user tags table
                documentType = record.documentType
            )
        if (uploadInitResponse?.error == true) {
            Logger.e("Upload initialization error: ${uploadInitResponse.message}")
            dao.updateRecords(listOf(record.copy(status = RecordStatus.SYNC_FAILED)))
            return@supervisorScope
        }
        val batchResponse = uploadInitResponse?.batchResponse?.firstOrNull()
        if (batchResponse == null) {
            Logger.e("Batch response is null")
            dao.updateRecords(listOf(record.copy(status = RecordStatus.SYNC_FAILED)))
            return@supervisorScope
        }
        val uploadResponse =
            awsRepository.uploadFile(batch = batchResponse, fileList = files)
        if (uploadResponse?.error == true) {
            Logger.e("Upload error: ${uploadResponse.message}")
            dao.updateRecords(listOf(record.copy(status = RecordStatus.SYNC_FAILED)))
            return@supervisorScope
        }
        if (uploadResponse == null) {
            Logger.e("Upload response is null")
            dao.updateRecords(listOf(record.copy(status = RecordStatus.SYNC_FAILED)))
            return@supervisorScope
        }
        dao.updateRecords(
            listOf(
                record.copy(
                    status = RecordStatus.SYNC_SUCCESS,
                    documentId = uploadResponse.documentId,
                    isDirty = false
                )
            )
        )
    }
}