package eka.care.documents.sync.data.repository

import android.app.Application
import com.eka.network.ConverterFactoryType
import com.eka.network.Networking
import eka.care.documents.sync.data.remote.api.MyDocumentsProtoService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import vault.records.Records

class SyncRecordsRepository(val app: Application) {

    private val recordsProtoService: MyDocumentsProtoService =
        Networking.create(
            MyDocumentsProtoService::class.java,
            "https://vault.eka.care/",
            ConverterFactoryType.PROTO
        )

    suspend fun getRecords(
        updatedAt: Int?,
        offset: String? = null,
        uuid : String
    ): Records.RecordsAPIResponse? {
        return withContext(Dispatchers.IO) {
            try {
                val response = recordsProtoService.getFiles(
                    updatedAt = updatedAt,
                    offset = offset,
                    uuid = uuid
                )
                if (response.isSuccessful) {
                    val ekaUat = response.headers().get("Eka-Uat")
//                    ekaUat?.let {
//                        (app as? IAmCommon)?.setValue(
//                            FILES_DB_UPDATED_AT,
//                            ekaUat.toLong()
//                        )
//                    }

                    response.body()
                } else {
                    null
                }
            } catch (ex: Exception) {
                return@withContext null
            }
        }
    }
}