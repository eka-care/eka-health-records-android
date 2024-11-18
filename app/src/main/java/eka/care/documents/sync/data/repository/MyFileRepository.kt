package eka.care.documents.sync.data.repository

import com.eka.network.Networking
import com.haroldadmin.cnradapter.NetworkResponse
import eka.care.documents.sync.data.remote.api.MyFileService
import eka.care.documents.sync.data.remote.dto.request.UpdateFileDetailsRequest
import eka.care.documents.sync.data.remote.dto.response.Document
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody

class MyFileRepository {

    private val myFileService: MyFileService =
        Networking.create(MyFileService::class.java, "https://vault.eka.care/")

    suspend fun updateFileDetails(
        docId: String,
        oid: String,
        updateFileDetailsRequest: UpdateFileDetailsRequest,
    ): Int? {
        return withContext(Dispatchers.IO) {
            val errorCode = when (
                val response =
                    myFileService.updateFileDetails(
                        docId,
                        updateFileDetailsRequest = updateFileDetailsRequest,
                        oid = oid
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

    suspend fun getDocument(docId: String, userId: String): Document? {
        return withContext(Dispatchers.IO) {
            val myDocument =
                when (val response = myFileService.getDocument(docId = docId, oid = userId)) {
                    is NetworkResponse.Success -> response.body
                    is NetworkResponse.ServerError -> null // handleServerError(response.code)
                    is NetworkResponse.NetworkError -> null //handleNetworkError(response.error)
                    is NetworkResponse.UnknownError -> null //handleUnknownError(response.error)
                }
            myDocument
        }
    }

    suspend fun deleteDocument(docId: String, oid: String): Int? {
        return withContext(Dispatchers.IO) {
            val errorCode =
                when (val response = myFileService.deleteDocument(docId = docId, oid = oid)) {
                    is NetworkResponse.Success -> response.code
                    is NetworkResponse.ServerError -> response.code // handleServerError(response.code)
                    is NetworkResponse.NetworkError -> null //handleNetworkError(response.error)
                    is NetworkResponse.UnknownError -> response.code //handleUnknownError(response.error)
                }
            errorCode
        }
    }
}