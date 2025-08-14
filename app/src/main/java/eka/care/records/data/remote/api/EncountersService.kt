package eka.care.records.data.remote.api

import com.haroldadmin.cnradapter.NetworkResponse
import eka.care.records.data.remote.dto.request.CaseRequest
import eka.care.records.data.remote.dto.response.CreateCaseResponse
import eka.care.records.data.remote.dto.response.DeleteCaseResponse
import eka.care.records.data.remote.dto.response.ListEncounterResponse
import eka.care.records.data.remote.dto.response.UpdateCaseResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface EncountersService {

    @POST("api/v1/cases")
    suspend fun createCase(
        @Header("X-Pt-Id") patientId: String,
        @Body caseRequest: CaseRequest
    ): NetworkResponse<CreateCaseResponse, CreateCaseResponse>

    @PATCH("api/v1/cases/{id}")
    suspend fun updateCaseDetails(
        @Header("X-Pt-Id") patientId: String,
        @Path("id") caseId: String,
        @Body caseRequest: CaseRequest
    ): NetworkResponse<UpdateCaseResponse?, UpdateCaseResponse>

    @GET("api/v1/cases")
    suspend fun listEncounters(
        @Header("X-Pt-Id") patientId: String?,
        @Query("u_at__gt") updatedAt: Long,
        @Query("offset") offset: String?
    ): NetworkResponse<ListEncounterResponse, ListEncounterResponse>

    @DELETE("api/v1/cases/{id}")
    suspend fun deleteEncounter(
        @Header("X-Pt-Id") patientId: String,
        @Path("id") caseId: String
    ): NetworkResponse<DeleteCaseResponse?, DeleteCaseResponse>
}