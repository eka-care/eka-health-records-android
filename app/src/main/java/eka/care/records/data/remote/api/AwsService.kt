package eka.care.records.data.remote.api

import com.haroldadmin.cnradapter.NetworkResponse
import eka.care.records.data.remote.dto.request.FilesUploadInitRequest
import eka.care.records.data.remote.dto.response.FilesUploadInitResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
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
        @PartMap params: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part file: MultipartBody.Part
    ): Response<Void>

}