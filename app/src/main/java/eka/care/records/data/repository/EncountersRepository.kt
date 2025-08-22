package eka.care.records.data.repository

import com.eka.networking.client.EkaNetwork
import com.haroldadmin.cnradapter.NetworkResponse
import eka.care.records.client.utils.Document
import eka.care.records.data.remote.api.EncountersService
import eka.care.records.data.remote.dto.request.CaseRequest
import eka.care.records.data.remote.dto.response.CreateCaseResponse
import eka.care.records.data.remote.dto.response.DeleteCaseResponse
import eka.care.records.data.remote.dto.response.ListEncounterResponse
import eka.care.records.data.remote.dto.response.UpdateCaseResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EncountersRepository {
    private val encounterService: EncountersService = EkaNetwork
        .creatorFor(
            appId = Document.getConfiguration().appId,
            service = "encounter_service"
        ).create(
            serviceUrl = "https://api.eka.care/mr/",
            serviceClass = EncountersService::class.java
        )

    suspend fun createCase(patientId: String, caseRequest: CaseRequest): CreateCaseResponse? {
        return withContext(Dispatchers.IO) {
            val response =
                when (val response = encounterService.createCase(
                    patientId = patientId,
                    caseRequest = caseRequest
                )) {
                    is NetworkResponse.Success -> response.body
                    is NetworkResponse.ServerError -> response.body
                    is NetworkResponse.NetworkError -> null
                    is NetworkResponse.UnknownError -> null
                }
            response
        }
    }

    suspend fun getCases(
        updatedAt: Long,
        offset: String? = null,
        oid: String?
    ): ListEncounterResponse? {
        return withContext(Dispatchers.IO) {
            val response =
                when (val response = encounterService.listEncounters(
                    patientId = oid,
                    updatedAt = if (offset.isNullOrEmpty()) updatedAt else null,
                    offset = offset,
                )) {
                    is NetworkResponse.Success -> response.body
                    is NetworkResponse.ServerError -> response.body
                    is NetworkResponse.NetworkError -> null
                    is NetworkResponse.UnknownError -> null
                }
            response
        }
    }

    suspend fun updateCaseDetails(
        patientId: String,
        caseId: String,
        caseRequest: CaseRequest
    ): UpdateCaseResponse? {
        return withContext(Dispatchers.IO) {
            val response =
                when (val response = encounterService.updateCaseDetails(
                    patientId = patientId,
                    caseId = caseId,
                    caseRequest = caseRequest
                )) {
                    is NetworkResponse.Success -> response.body
                    is NetworkResponse.ServerError -> response.body
                    is NetworkResponse.NetworkError -> null
                    is NetworkResponse.UnknownError -> null
                }
            response
        }
    }

    suspend fun deleteEncounter(
        patientId: String,
        caseId: String
    ): DeleteCaseResponse? {
        return withContext(Dispatchers.IO) {
            val response =
                when (val response = encounterService.deleteEncounter(
                    patientId = patientId,
                    caseId = caseId
                )) {
                    is NetworkResponse.Success -> response.body
                    is NetworkResponse.ServerError -> response.body
                    is NetworkResponse.NetworkError -> null
                    is NetworkResponse.UnknownError -> null
                }
            response
        }
    }
}