package eka.care.documents.sync.data.repository

import android.app.Application
import com.eka.network.ConverterFactoryType
import com.eka.network.Networking
import eka.care.documents.Document
import eka.care.documents.sync.data.remote.api.MyFileService
import eka.care.documents.sync.data.remote.dto.response.GetFilesResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class SyncRecordsRepository(val app: Application) {

    private val recordsProtoService: MyFileService = Networking.create(
        MyFileService::class.java,
        Document.getConfiguration()?.host,
        ConverterFactoryType.PROTO
    )

    suspend fun getRecords(
        updatedAt: Long?,
        offset: String? = null,
        oid: String?
    ): Response<GetFilesResponse>? {
        return withContext(Dispatchers.IO) {
            try {
                val response = recordsProtoService.getFiles(
                    updatedAt = updatedAt.toString(),
                    offset = offset,
                    filterId = oid
                )

                if (!response.isSuccessful) {
                    return@withContext null
                }
                response
            } catch (ex: Exception) {
                return@withContext null
            }
        }
    }

}