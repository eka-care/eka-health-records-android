package eka.care.records.data.repository

import android.webkit.MimeTypeMap
import com.eka.networking.client.EkaNetwork
import com.haroldadmin.cnradapter.NetworkResponse
import eka.care.records.client.utils.Document
import eka.care.records.data.remote.EnvironmentManager
import eka.care.records.data.remote.api.AwsService
import eka.care.records.data.remote.dto.request.Batch
import eka.care.records.data.remote.dto.request.FileType
import eka.care.records.data.remote.dto.request.FilesUploadInitRequest
import eka.care.records.data.remote.dto.response.AwsUploadResponse
import eka.care.records.data.remote.dto.response.BatchResponse
import eka.care.records.data.remote.dto.response.FilesUploadInitResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.util.Locale

class AwsRepository {

    private val service: AwsService = EkaNetwork
        .creatorFor(
            appId = Document.getConfiguration().appId,
            service = "aws_service"
        ).create(
            serviceUrl = "${EnvironmentManager.getBaseUrl()}/mr/",
            serviceClass = AwsService::class.java
        )

    suspend fun fileUploadInit(
        documentId: String,
        files: List<FileType>,
        isMultiFile: Boolean = false,
        isEncrypted: Boolean = false,
        patientOid: String?,
        documentType: String,
        documentDate: Long? = null,
        tags: List<String>,
        cases : List<String>? = null,
        isAbhaLinked: Boolean = false,
    ): FilesUploadInitResponse? {
        val batch = mutableListOf<Batch>()

        if (isMultiFile) {
            batch.add(
                Batch(
                    documentId = documentId,
                    files = files,
                    isEncrypted = isEncrypted,
                    sharable = false,
                    tags = tags,
                    documentType = documentType,
                    documentDate = documentDate,
                    cases = cases,
                    isAbhaLinked = isAbhaLinked
                )
            )
        } else {
            files.forEach {
                batch.add(
                    Batch(
                        documentId = documentId,
                        files = listOf(it),
                        isEncrypted = isEncrypted,
                        sharable = false,
                        tags = tags,
                        documentType = documentType,
                        documentDate = documentDate,
                        cases = cases,
                        isAbhaLinked = isAbhaLinked
                    )
                )
            }
        }
        val body = FilesUploadInitRequest(batchRequest = batch)
        return withContext(Dispatchers.IO) {
            val response =
                when (val response = service.filesUploadInit(body, patientOid)) {
                    is NetworkResponse.Success -> response.body
                    is NetworkResponse.ServerError -> response.body
                    is NetworkResponse.NetworkError -> null
                    is NetworkResponse.UnknownError -> null
                }
            response
        }
    }

    suspend fun uploadFile(
        file: File? = null,
        batch: BatchResponse,
        fileList: List<File>? = null
    ): AwsUploadResponse {
        val files = fileList ?: mutableListOf(file)
        var isResponseSuccess = false
        var message = "Unknown Error"
        return withContext(Dispatchers.IO) {
            run loop@{
                files.forEachIndexed { index, fileEntry ->
                    fileEntry ?: return@forEachIndexed
                    val requestFile = fileEntry.asRequestBody(fileEntry.getMimeType().toMediaType())
                    val fileMultipartBody: MultipartBody.Part =
                        MultipartBody.Part.createFormData("file", fileEntry.name, requestFile)
                    val params = batch.forms[index].fields.mapValues { (_, value) ->
                        value.toRequestBody("text/plain".toMediaTypeOrNull())
                    }

                    val response = service.uploadFile(
                        url = batch.forms[index].url,
                        params = params,
                        file = fileMultipartBody
                    )
                    if (response.isSuccessful) {
                        isResponseSuccess = true
                    } else {
                        isResponseSuccess = false
                        message = response.message() ?: "Failed to upload file"
                        return@loop
                    }
                }
            }
            if (isResponseSuccess) {
                AwsUploadResponse(error = false, documentId = batch.documentId, message = null)
            } else {
                AwsUploadResponse(error = true, documentId = batch.documentId, message = message)
            }
        }
    }

    private fun File.getMimeType(fallback: String = "application/pdf"): String {
        return MimeTypeMap.getFileExtensionFromUrl(toString())
            ?.run {
                MimeTypeMap.getSingleton().getMimeTypeFromExtension(lowercase(Locale.getDefault()))
            }
            ?: fallback
    }
}