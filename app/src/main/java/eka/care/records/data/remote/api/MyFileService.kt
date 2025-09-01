package eka.care.records.data.remote.api

import com.haroldadmin.cnradapter.NetworkResponse
import eka.care.records.data.remote.dto.request.UpdateFileDetailsRequest
import eka.care.records.data.remote.dto.response.Document
import eka.care.records.data.remote.dto.response.GetFilesResponse
import okhttp3.ResponseBody
import retrofit2.Response
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

    @GET("api/v1/docs")
    suspend fun getFiles(
        @Query("u_at__gt") updatedAt: Long,
        @Query("offset") offset: String?,
        @Header("X-Pt-Id") filterId: String?
    ): Response<GetFilesResponse>

    @PATCH("api/v1/docs/{document_id}")
    suspend fun updateFileDetails(
        @Path("document_id") documentId: String,
        @Header("X-Pt-Id") filterId: String?,
        @Body updateFileDetailsRequest: UpdateFileDetailsRequest,
    ): NetworkResponse<Unit, Unit>

    @Streaming
    @GET
    suspend fun downloadFile(@Url url: String?): NetworkResponse<ResponseBody, ResponseBody>

    @GET("api/v1/docs/{document_id}")
    suspend fun getDocument(
        @Path("document_id") documentId: String,
        @Header("X-Pt-Id") filterId: String?,
    ): NetworkResponse<Document, Document>

    @DELETE("api/v1/docs/{document_id}")
    suspend fun deleteDocument(
        @Path("document_id") documentId: String,
        @Header("X-Pt-Id") filterId: String?,
    ): NetworkResponse<Unit, Document>
}