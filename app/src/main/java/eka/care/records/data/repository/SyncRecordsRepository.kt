package eka.care.records.data.repository

import com.eka.network.Networking
import eka.care.records.data.remote.api.MyFileService
import eka.care.records.data.remote.dto.response.GetFilesResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class SyncRecordsRepository() {

    private val recordsService: MyFileService = Networking.create(
        MyFileService::class.java,
        "https://api.eka.care/mr/"
    )

    suspend fun getRecords(
        updatedAt: Long?,
        offset: String? = null,
        oid: String?
    ): Response<GetFilesResponse>? {
        return withContext(Dispatchers.IO) {
            try {
                val response = recordsService.getFiles(
                    updatedAt = updatedAt ?: 0L,
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