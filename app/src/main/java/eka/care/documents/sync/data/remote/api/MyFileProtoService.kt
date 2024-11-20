package eka.care.documents.sync.data.remote.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query
import vault.records.Records

interface MyDocumentsProtoService {
    @GET("api/d/v1/docs")
    suspend fun getFiles(
        @Query("u_at__gt") updatedAt: Int?,
        @Query("offset") offset: String?,
        @Query("p_uuid") uuid : String
    ): Response<Records.RecordsAPIResponse>
}