package eka.care.records.data.repository

import android.webkit.MimeTypeMap
import com.eka.network.Networking
import com.haroldadmin.cnradapter.NetworkResponse
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
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.util.Locale

class AwsRepository {

    private val service: AwsService =
        Networking.create(AwsService::class.java, "https://api.eka.care/mr/")

    suspend fun fileUploadInit(
        files: List<FileType>,
        isMultiFile: Boolean = false,
        isEncrypted: Boolean = false,
        patientOid: String?,
        documentType: String,
        documentDate: Long? = null,
        tags: List<String>
    ): FilesUploadInitResponse? {
        val batch = mutableListOf<Batch>()

        if (isMultiFile) {
            batch.add(
                Batch(
                    files = files,
                    isEncrypted = isEncrypted,
                    sharable = false,
                    tags = tags,
                    documentType = documentType,
                    documentDate = documentDate
                )
            )
        } else {
            files.forEach {
                batch.add(
                    Batch(
                        files = listOf(it),
                        isEncrypted = isEncrypted,
                        sharable = false,
                        tags = tags,
                        documentType = documentType,
                        documentDate = documentDate
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
    ): AwsUploadResponse? {
        val files = fileList ?: mutableListOf(file)
        var isResponseSuccess = false
        return withContext(Dispatchers.IO) {
            run loop@{
                files.forEachIndexed { index, fileEntry ->
                    fileEntry ?: return@forEachIndexed
                    val requestFile = fileEntry.asRequestBody(fileEntry.getMimeType().toMediaType())
                    val fileMultipartBody: MultipartBody.Part =
                        MultipartBody.Part.createFormData("file", fileEntry.name, requestFile)
                    val response = service.uploadFile(
                        url = batch.forms[index].url,
                        params = batch.forms[index].fields,
                        file = fileMultipartBody
                    )
                    if (response.isSuccessful) {
                        isResponseSuccess = true
                    } else {
                        isResponseSuccess = false
                        return@loop
                    }
                }
            }
            if (isResponseSuccess) {
                AwsUploadResponse(error = false, documentId = batch.documentId, message = null)
            } else {
                null
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