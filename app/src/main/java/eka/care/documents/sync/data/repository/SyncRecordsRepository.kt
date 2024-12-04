package eka.care.documents.sync.data.repository

import android.app.Application
import android.util.Log
import com.eka.network.ConverterFactoryType
import com.eka.network.Networking
import eka.care.documents.Document
import eka.care.documents.sync.data.remote.api.MyDocumentsProtoService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import vault.records.Records

class SyncRecordsRepository(val app: Application) {

    private val recordsProtoService: MyDocumentsProtoService = Networking.create(
        MyDocumentsProtoService::class.java,
        Document.getConfiguration()?.host,
        ConverterFactoryType.PROTO
    )

    suspend fun getRecords(
        updatedAt: String?,
        offset: String? = null,
        uuid : String
    ):  Response<Records.RecordsAPIResponse>? {
        return withContext(Dispatchers.IO) {
            try {
                val response = recordsProtoService.getFiles(
                    updatedAt = updatedAt,
                    offset = offset,
                    uuid = uuid
                )
                if (response.isSuccessful) {
                    response
                } else {
                    null
                }
            } catch (ex: Exception) {
                return@withContext null
            }
        }
    }
}