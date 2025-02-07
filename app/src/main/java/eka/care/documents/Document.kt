package eka.care.documents

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.Composable
import com.eka.network.ConverterFactoryType
import com.eka.network.Networking
import com.google.gson.Gson
import eka.care.documents.data.db.database.DocumentDatabase
import eka.care.documents.data.db.entity.VaultEntity
import eka.care.documents.data.db.model.AvailableDocTypes
import eka.care.documents.data.repository.DocumentsRepository
import eka.care.documents.data.repository.VaultRepositoryImpl
import eka.care.documents.ui.presentation.activity.DocumentViewActivity
import eka.care.documents.ui.presentation.activity.SmartReportActivity
import eka.care.documents.ui.presentation.components.DocumentFilter
import eka.care.documents.ui.presentation.model.RecordModel
import eka.care.documents.ui.utility.RecordsUtility
import kotlinx.coroutines.flow.Flow
import eka.care.documents.ui.presentation.model.CTA

object Document {
    private var configuration: DocumentConfiguration? = null
    private var db: DocumentDatabase? = null
    private var documentRepository: DocumentsRepository? = null

    fun init(context: Context, documentConfiguration: DocumentConfiguration) {
        configuration = documentConfiguration
        configuration?.let {
            Networking.init(
                it.host,
                it.okHttpSetup,
                converterFactoryType = ConverterFactoryType.PROTO
            )
        }
        db = DocumentDatabase.getInstance(context)
        db?.let {
            documentRepository = VaultRepositoryImpl(it)
        }
    }

    fun getDocuments(
        ownerId: String,
        filterId: String?,
        docType: Int = -1
    ): Flow<List<VaultEntity>>? {
        return documentRepository?.fetchDocuments(
            ownerId = ownerId,
            filterId = filterId,
            docType = docType
        )
    }

    suspend fun storeDocuments(vaultEntityList: List<VaultEntity>) {
        documentRepository?.storeDocuments(vaultEntityList)
    }

    suspend fun deleteDocument(filterId: String, localId: String) {
        documentRepository?.deleteDocument(filterId= filterId, localId = localId)
    }

    suspend fun editDocument(
        localId: String,
        docType: Int?,
        docDate: Long?,
        filterId: String?,
    ) {
        documentRepository?.editDocument(
            localId = localId,
            docType = docType,
            docDate = docDate,
            filterId= filterId
        )
    }

    suspend fun getAvailableDocTypes(filterId: String, ownerId: String): List<AvailableDocTypes>?{
       return documentRepository?.getAvailableDocTypes(filterId = filterId, ownerId = ownerId)
    }

    fun view(context: Context, model: RecordModel, oid: String){
        if (model.tags?.split(",")?.contains("1") == true) {
            val date = RecordsUtility.convertLongToDateString(model.documentDate ?: model.createdAt)
            Intent(context, SmartReportActivity::class.java)
                .also {
                    it.putExtra("doc_id", model.documentId)
                    it.putExtra("local_id", model.localId)
                    it.putExtra("doctor_id", model.doctorId)
                    it.putExtra("user_id", oid)
                    it.putExtra("doc_date", date)
                    context.startActivity(it)
                }
            return
        } else {
            Intent(context, DocumentViewActivity::class.java).also {
                it.putExtra("local_id", model.localId)
                it.putExtra("doc_id", model.documentId)
                it.putExtra("user_id", oid)
                context.startActivity(it)
            }
            return
        }
    }


    fun destroy() {
        db?.clearAllTables()
    }
    suspend fun getRecordById(id: String?): RecordModel? {
        if(id.isNullOrEmpty()) return null
        val vaultEntity = db?.vaultDao()?.getDocumentById(id)
        return vaultEntity?.toRecordModel()
    }

    fun getConfiguration() = configuration
}
fun VaultEntity.toRecordModel(): RecordModel {
    return RecordModel(
        localId = this.localId,
        documentId = this.documentId,
        doctorId = this.doctorId,
        documentType = this.documentType,
        documentDate = this.documentDate,
        createdAt = this.createdAt,
        thumbnail = this.thumbnail,
        filePath = this.filePath,
        fileType = this.fileType,
        cta = Gson().fromJson(this.cta, CTA::class.java),
        tags = this.tags,
        source = this.source,
        isAnalyzing = this.isAnalyzing
    )
}