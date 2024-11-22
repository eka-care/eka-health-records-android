package eka.care.documents.sync.workers

import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import coil.request.Tags
import com.google.gson.Gson
import eka.care.documents.data.db.database.DocumentDatabase
import eka.care.documents.sync.data.remote.dto.request.UpdateFileDetailsRequest
import eka.care.documents.sync.data.repository.AwsRepository
import eka.care.documents.sync.data.repository.MyFileRepository
import eka.care.documents.sync.data.repository.SyncRecordsRepository
import eka.care.documents.data.db.entity.VaultEntity
import eka.care.documents.data.db.model.CTA
import eka.care.documents.data.repository.VaultRepository
import eka.care.documents.data.repository.VaultRepositoryImpl
import eka.care.documents.data.utility.DocumentUtility.Companion.docTypes
import eka.care.documents.sync.data.remote.dto.request.FileType
import eka.care.documents.ui.utility.RecordsUtility.Companion.changeDateFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import vault.records.Records
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class SyncFileWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    private val db = DocumentDatabase.getInstance(applicationContext)
    private val vaultRepository: VaultRepository = VaultRepositoryImpl(db)
    private val awsRepository = AwsRepository()
    private val myFileRepository = MyFileRepository()
    private val recordsRepository = SyncRecordsRepository(appContext as Application)

    override suspend fun doWork(): Result = coroutineScope {
        try {
            val uuid = inputData.getString("p_uuid") ?: return@coroutineScope Result.failure()
            val oid = inputData.getString("oid") ?: return@coroutineScope Result.failure()
            val doctorId = inputData.getString("doctorId") ?: return@coroutineScope Result.failure()
            syncDocuments(oid = oid, uuid = uuid, doctorId = doctorId)
            val updatedAt = 0L
            fetchRecords(
                offset = null,
                updatedAt = updatedAt,
                uuid = uuid,
                oid = oid,
                doctorId = doctorId
            )
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private suspend fun syncDocuments(oid: String, uuid: String, doctorId: String) {
        try {
            val vaultDocuments = vaultRepository.getUnsyncedDocuments(oid = oid, doctorId = doctorId)
            val files = mutableListOf<FileType>()
            val tags = mutableListOf<String>()

            vaultDocuments.forEach { vaultEntity ->
                vaultEntity.filePath.let { path ->
                    path.forEach {
                        val file = File(it)
                        files.add(FileType(file.getMimeType().toString(), file.length()))
                    }
                }
                val tagList = vaultEntity.tags?.split(",") ?: emptyList()
//                val tagNames = Tags().getTagNamesByIds(tagList)
//                tags.addAll(tagNames)
            }

            if (files.isNotEmpty()) {
                val uploadInitResponse =
                    awsRepository.fileUploadInit(
                        files = files,
                        patientOid = oid,
                        patientUuid = uuid,
                        tags = tags
                    )
                vaultDocuments.forEachIndexed { index, vaultEntity ->
                    val batchResponse = uploadInitResponse?.batchResponse?.get(index) ?: return@forEachIndexed
                    vaultEntity.filePath.let { path ->
                        path.forEach {
                            val response = awsRepository.uploadFile(file = File(it), batch = batchResponse)
                            response?.documentId?.let { documentId ->
                                var documentDate: String? = null
                                vaultEntity.documentDate?.let {
                                    val date = Date(it * 1000)
                                    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                    documentDate = formatter.format(date)
                                }

                                val tagList = vaultEntity.tags?.split(",") ?: emptyList()
                        //        val tagNames = Tags().getTagNamesByIds(tagList)
                                val updateFileDetailsRequest = UpdateFileDetailsRequest(
                                    oid = vaultEntity.oid,
                                    documentType = docTypes.firstOrNull { it.idNew == vaultEntity.documentType }?.id,
                                    documentDate = changeDateFormat(documentDate),
                                    userTags = emptyList(),
                                    linkAbha = vaultEntity.isABHALinked
                                )

                                myFileRepository.updateFileDetails(
                                    docId = documentId,
                                    oid = oid,
                                    updateFileDetailsRequest = updateFileDetailsRequest
                                )
                                vaultRepository.updateDocumentId(documentId, vaultEntity.localId)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Handle syncing exceptions
            Log.d("TEST", e.message.toString())
        }
    }


    private suspend fun fetchRecords(
        offset: String?,
        updatedAt: Long?,
        uuid: String,
        oid: String,
        doctorId: String
    ) {
        try {
            val records = recordsRepository.getRecords(
                updatedAt = null,
                offset = offset,
                uuid = uuid
            )
            if (records?.error?.message.isNullOrEmpty()) {
                if (!records?.response?.itemsList.isNullOrEmpty()) {
                    records?.response?.let {
                        storeRecords(
                            recordsResponse = it,
                            doctorId = doctorId,
                            uuid = uuid,
                        )
                    }
                    getLocalRecords(oid = oid, doctorId = doctorId)
                }

                val newOffset = records?.response?.nextPageToken
                if (!newOffset.isNullOrEmpty()) {
                    fetchRecords(
                        offset = newOffset,
                        updatedAt = updatedAt,
                        uuid = uuid,
                        oid = oid,
                        doctorId = doctorId
                    )
                } else {
                    getLocalRecords(oid = oid, doctorId = doctorId)
                }
            } else {
                Log.d("TEST", "${records?.error?.message}")
            }
        } catch (e: Exception) {
            Log.d("TEST", "${e.message}")
        }
    }

    private suspend fun storeRecords(
        recordsResponse: Records.RecordsResponse,
        doctorId: String,
        uuid : String
    ) {
        val vaultList = mutableListOf<VaultEntity>()
        recordsResponse.itemsList?.forEach {
            val recordItem = it.record.item

            val recordCta = recordItem.metadata.cta
            val params = mutableMapOf<String, String>()
            recordCta.paramsMap.forEach { entry ->
                params[entry.key] = entry.value.stringValue
            }
            val localCta = CTA(action = recordCta.action, pageId = recordCta.pid, params = params)
            val localId = vaultRepository.getLocalId(recordItem.documentId)
            if(!localId.isNullOrEmpty()){
                vaultRepository.storeDocument(
                    localId = localId,
                    cta =  if (localCta.pageId.isNullOrEmpty()) null
                    else Gson().toJson(localCta, CTA::class.java).toString() ,
                    isAnalysing = recordItem.availableDocumentCase == Records.Record.Item.AvailableDocumentCase.IN_TRANSIT,
                    docId = recordItem.documentId,
                    hasId = it.record.hash.toString(),
                    isAbhaLinked = false,
                    oid = recordItem.patientOid,
                    tags = recordItem.metadata.tagsValueList.joinToString(","),
                    documentDate = recordItem.metadata.documentDate.seconds,
                )
            }else{
                vaultList.add(
                    VaultEntity(
                        localId = localId ?: UUID.randomUUID().toString(),
                        documentId = recordItem.documentId,
                        uuid = uuid,
                        oid = recordItem.patientOid,
                        filePath = listOf(),
                        fileType = "",
                        thumbnail = null,
                        createdAt = recordItem.uploadDate.seconds,
                        source = recordItem.source.number,
                        documentType = recordItem.documentType.number,
                        tags = recordItem.metadata.tagsValueList.joinToString(","),
                        documentDate = recordItem.metadata.documentDate.seconds,
                        hashId = it.record.hash.toString(),
                        isABHALinked = false,
                        isAnalyzing = recordItem.availableDocumentCase == Records.Record.Item.AvailableDocumentCase.IN_TRANSIT,
                        cta = if (localCta.pageId.isNullOrEmpty()) null
                        else Gson().toJson(localCta, CTA::class.java).toString(),
                        doctorId = doctorId
                    )
                )
            }
        }

        vaultRepository.storeDocuments(vaultList)
        storeThumbnails(vaultList, recordsResponse)
    }

    private suspend fun storeThumbnails(
        vaultList: List<VaultEntity>,
        recordsResponse: Records.RecordsResponse?
    ) {
        recordsResponse?.itemsList?.forEach {
            val path = downloadThumbnail(it.record.item.metadata.thumbnail)

            val documentId = it.record.item.documentId
            vaultRepository.setThumbnail(
                path, vaultList.first { it.documentId == documentId }.documentId
            )
        }
        return
    }

    private suspend fun downloadThumbnail(assetUrl: String?): String {
        val directory = ContextWrapper(applicationContext).getDir("imageDir", Context.MODE_PRIVATE)
        val childPath = "image${UUID.randomUUID()}.jpg"
        withContext(Dispatchers.IO) {
            val resp = myFileRepository.downloadFile(assetUrl)
            resp?.saveFile(File(directory, childPath))
        }

        return "${directory.path}/$childPath"
    }

    private fun ResponseBody.saveFile(destFile: File) {
        byteStream().use { inputStream ->
            destFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    }

    private fun getLocalRecords(oid: String, doctorId: String) {
        vaultRepository.fetchDocuments(oid = oid, docType = -1, doctorId = doctorId)
    }

}

fun File.getMimeType(): String? =
    MimeTypeMap.getSingleton().getMimeTypeFromExtension(this.extension)