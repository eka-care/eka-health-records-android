package eka.care.documents.sync.data.repository

import android.app.Application
import android.util.Log
import com.google.protobuf.InvalidProtocolBufferException
import eka.care.documents.network.ConverterFactoryType
import eka.care.documents.network.Networking
import eka.care.documents.sync.data.remote.api.MyDocumentsProtoService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import vault.records.Records
import java.io.IOException

class SyncRecordsRepository(private val app: Application) {
    private val TAG = "SyncRecordsRepository"

    private val recordsProtoService: MyDocumentsProtoService = Networking.create(
        MyDocumentsProtoService::class.java,
        "https://vault.eka.care/",
        ConverterFactoryType.PROTO
    )

    sealed class RecordsResult {
        data class Success(val records: Records.RecordsAPIResponse) : RecordsResult()
        data class Error(val errorMessage: String, val code: Int? = null) : RecordsResult()
    }

    suspend fun getRecords(
        updatedAt: Int?,
        offset: String? = null,
        uuid: String
    ): RecordsResult = withContext(Dispatchers.IO) {
        try {
            val response = recordsProtoService.getFiles(
                updatedAt = updatedAt,
                offset = offset,
                uuid = uuid
            )

            when {
                response.isSuccessful && response.body() != null -> {
                    try {
                        // Parse the protobuf bytes into your Records.RecordsAPIResponse
                        val records = Records.RecordsAPIResponse.parseFrom(response.body()!!)
                        Log.d(TAG, "Successfully parsed records response")
                        RecordsResult.Success(records)
                    } catch (e: InvalidProtocolBufferException) {
                        Log.e(TAG, "Failed to parse protobuf response", e)
                        RecordsResult.Error("Failed to parse response: ${e.message}")
                    }
                }
                response.code() == 404 -> {
                    RecordsResult.Error("No records found", 404)
                }
                response.code() == 401 -> {
                    RecordsResult.Error("Unauthorized access", 401)
                }
                else -> {
                    RecordsResult.Error(
                        "Error fetching records: ${response.message()}",
                        response.code()
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching records", e)
            RecordsResult.Error("Error fetching records: ${e.message}")
        }
    }
}