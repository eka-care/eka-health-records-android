package eka.care.records.data.repository

import com.haroldadmin.cnradapter.NetworkResponse
import eka.care.records.data.remote.api.MyFileService
import eka.care.records.data.remote.dto.request.UpdateFileDetailsRequest
import eka.care.records.data.remote.dto.response.Document
import eka.care.records.data.remote.network.Networking
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody

class MyFileRepository {

    private val myFileService: MyFileService by lazy {
        Networking.create(MyFileService::class.java, "https://api.eka.care/mr/")
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

    suspend fun deleteDocument(documentId: String, filterId: String?): Int? {
        return withContext(Dispatchers.IO) {
            val errorCode =
                when (val response =
                    myFileService.deleteDocument(documentId = documentId, filterId = filterId)) {
                    is NetworkResponse.Success -> response.code
                    is NetworkResponse.ServerError -> response.code // handleServerError(response.code)
                    is NetworkResponse.NetworkError -> null //handleNetworkError(response.error)
                    is NetworkResponse.UnknownError -> response.code //handleUnknownError(response.error)
                }
            errorCode
        }
    }
}