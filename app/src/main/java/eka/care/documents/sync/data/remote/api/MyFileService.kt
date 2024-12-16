package eka.care.documents.sync.data.remote.api

import com.haroldadmin.cnradapter.NetworkResponse
import eka.care.documents.sync.data.remote.dto.request.UpdateFileDetailsRequest
import eka.care.documents.sync.data.remote.dto.response.Document
import eka.care.documents.sync.data.remote.dto.response.MyFileResponse
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming
import retrofit2.http.Url

interface MyFileService {

    // for secret Locker
    @GET("api/v2/docs")
    suspend fun getMyFiles(
        @Query("document_type") documentType: String = "",
        @Query("sort") sort: String = "",
        @Query("enc") enc: Boolean? = null,
        @Header("Eka-Key-Id") ekaKeyId: String? = null,
        @Query("offset") offset: String? = ""
    ): NetworkResponse<MyFileResponse, NetworkResponse.NetworkError>

    @PATCH("api/d/v1/docs/{doc_id}")
    suspend fun updateFileDetails(
        @Path("doc_id") docId: String,
        @Query("oid") oid: String,
        @Body updateFileDetailsRequest: UpdateFileDetailsRequest,
    ): NetworkResponse<Unit, NetworkResponse.ServerError<Unit>>

    @Streaming
    @GET
    suspend fun downloadFile(@Url url: String?): NetworkResponse<ResponseBody, ResponseBody>

    @GET("api/v1/docs/{doc_id}")
    suspend fun getDocument(
        @Path("doc_id") docId: String,
        @Query("oid") oid: String,
    ): NetworkResponse<Document, NetworkResponse.NetworkError>

    @DELETE("api/v1/docs/{doc_id}")
    suspend fun deleteDocument(
        @Path("doc_id") docId: String,
        @Query("oid") oid: String,
    ): NetworkResponse<Unit, NetworkResponse.ServerError<Unit>>
}