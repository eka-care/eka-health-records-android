package eka.care.documents.sync.data.remote.api

import com.haroldadmin.cnradapter.NetworkResponse
import eka.care.documents.sync.data.remote.dto.request.UpdateFileDetailsRequest
import eka.care.documents.sync.data.remote.dto.response.Document
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming
import retrofit2.http.Url

interface MyFileService {

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