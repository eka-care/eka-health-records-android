package eka.care.documents.sync.data.remote.api

import com.haroldadmin.cnradapter.NetworkResponse
import eka.care.documents.sync.data.remote.dto.request.UpdateFileDetailsRequest
import eka.care.documents.sync.data.remote.dto.response.Document
import eka.care.documents.sync.data.remote.dto.response.GetFilesResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming
import retrofit2.http.Url

interface MyFileService {

    @GET("api/v1/docs")
    suspend fun getFiles(
        @Query("offset") offset: String?,
        @Query("p_oid") filterId : String?
    ): Response<GetFilesResponse>

    @PATCH("api/v1/docs/{document_id}")
    suspend fun updateFileDetails(
        @Path("document_id") documentId: String,
        @Query("p_oid") filterId: String?,
        @Body updateFileDetailsRequest: UpdateFileDetailsRequest,
    ): NetworkResponse<Unit, Unit>

    @Streaming
    @GET
    suspend fun downloadFile(@Url url: String?): NetworkResponse<ResponseBody, ResponseBody>

    @GET("api/v1/docs/{document_id}")
    suspend fun getDocument(
        @Path("document_id") documentId: String,
        @Query("p_oid") filterId: String?,
    ): NetworkResponse<Document, Document>

    @DELETE("api/v1/docs/{document_id}")
    suspend fun deleteDocument(
        @Path("document_id") documentId: String,
        @Query("p_oid") filterId: String?,
    ): NetworkResponse<Unit, Unit>
}