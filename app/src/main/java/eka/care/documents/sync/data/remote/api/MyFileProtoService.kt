package eka.care.documents.sync.data.remote.api

import eka.care.documents.sync.data.remote.dto.response.GetFilesResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface MyDocumentsProtoService {
    @GET("api/v1/docs")
    suspend fun getFiles(
        @Query("u_at__gt") updatedAt: String?,
        @Query("offset") offset: String?,
        @Query("patient_oid") filterId : String?
    ): Response<GetFilesResponse>
}