package eka.care.records.data.repository

import com.eka.networking.client.EkaNetwork
import com.haroldadmin.cnradapter.NetworkResponse
import eka.care.records.client.logger.logRecordSyncEvent
import eka.care.records.data.remote.EnvironmentManager
import eka.care.records.data.remote.api.MyFileService
import eka.care.records.data.remote.dto.request.UpdateFileDetailsRequest
import eka.care.records.data.remote.dto.response.Document
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody

class MyFileRepository {

    private val myFileService: MyFileService by lazy {
        EkaNetwork
            .creatorFor(
                appId = eka.care.records.client.utils.Document.getConfiguration().appId,
                service = "files_service"
            ).create(
                serviceUrl = "${EnvironmentManager.getBaseUrl()}/mr/",
                serviceClass = MyFileService::class.java
            )
    }

    suspend fun updateFileDetails(
        documentId: String,
        oid: String?,
        request: UpdateFileDetailsRequest,
    ): Int? {
        return withContext(Dispatchers.IO) {
            val errorCode = when (
                val response =
                    myFileService.updateFileDetails(
                        documentId = documentId,
                        updateFileDetailsRequest = request,
                        filterId = oid
                    )) {
                is NetworkResponse.Success -> response.code
                is NetworkResponse.ServerError -> response.code // handleServerError(response.code)
                is NetworkResponse.NetworkError -> null //handleNetworkError(response.error)
                is NetworkResponse.UnknownError -> response.code //handleUnknownError(response.error)
            }
            errorCode
        }
    }

    suspend fun downloadFile(url: String?): ResponseBody? {
        return withContext(Dispatchers.IO) {
            val response = when (val response = myFileService.downloadFile(url)) {
                is NetworkResponse.Success -> response.body
                is NetworkResponse.ServerError -> null // handleServerError(response.code)
                is NetworkResponse.NetworkError -> null //handleNetworkError(response.error)
                is NetworkResponse.UnknownError -> null //handleUnknownError(response.error)
            }
            response
        }
    }

    suspend fun getDocument(documentId: String, filterId: String?): Document? {
        return withContext(Dispatchers.IO) {
            val myDocument =
                when (val response =
                    myFileService.getDocument(documentId = documentId, filterId = filterId)) {
                    is NetworkResponse.Success -> response.body
                    is NetworkResponse.ServerError -> null // handleServerError(response.code)
                    is NetworkResponse.NetworkError -> null //handleNetworkError(response.error)
                    is NetworkResponse.UnknownError -> null //handleUnknownError(response.error)
                }
            myDocument
        }
    }

    suspend fun deleteDocument(documentId: String, filterId: String?, businessId: String): Int? {
        return withContext(Dispatchers.IO) {
            try {
                val errorCode =
                    when (val response =
                        myFileService.deleteDocument(
                            documentId = documentId,
                            filterId = filterId
                        )) {
                        is NetworkResponse.Success -> {
                            logRecordSyncEvent(
                                dId = documentId,
                                bId = businessId,
                                oId = filterId ?: "",
                                msg = "Delete success: $documentId"
                            )
                            response.code
                        }

                        is NetworkResponse.ServerError -> {
                            logRecordSyncEvent(
                                dId = documentId,
                                bId = businessId,
                                oId = filterId ?: "",
                                msg = "Delete Error: $documentId, ${response.response}"
                            )
                            response.code
                        } // handleServerError(response.code)
                        is NetworkResponse.NetworkError -> {
                            logRecordSyncEvent(
                                dId = documentId,
                                bId = businessId,
                                oId = filterId ?: "",
                                msg = "Delete Error: $documentId, ${response.error.toString()}"
                            )
                            null
                        } //handleNetworkError(response.error)
                        is NetworkResponse.UnknownError -> {
                            logRecordSyncEvent(
                                dId = documentId,
                                bId = businessId,
                                oId = filterId ?: "",
                                msg = "Delete Error: $documentId, ${response.response}"
                            )
                            response.code
                        } //handleUnknownError(response.error)
                    }
                errorCode
            } catch (ex: Exception) {
                logRecordSyncEvent(
                    dId = documentId,
                    bId = businessId,
                    oId = filterId ?: "",
                    msg = "Delete error: ${ex.message.toString()}"
                )
                null
            }
        }
    }
}