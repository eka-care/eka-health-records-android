package eka.care.documents.sync.data.remote.api

import com.haroldadmin.cnradapter.NetworkResponse
import eka.care.documents.sync.data.remote.dto.response.FilesUploadInitResponse
import eka.care.documents.sync.data.remote.dto.request.FilesUploadInitRequest
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.PartMap
import retrofit2.http.Query
import retrofit2.http.Url

interface AwsService {

    @POST("api/v1/docs")
    suspend fun filesUploadInit(
        @Body request: FilesUploadInitRequest,
        @Query("p_oid") filterId: String?
    ): NetworkResponse<FilesUploadInitResponse, FilesUploadInitResponse>

    @POST
    @Multipart
    suspend fun uploadFile(
        @Url url: String,
        @PartMap params: Map<String, String>,
        @Part file: MultipartBody.Part
    ): Response<Void>

}