package eka.care.documents

import android.app.Application
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.eka.network.ConverterFactoryType
import com.eka.network.Networking
import com.google.gson.Gson
import eka.care.documents.data.db.database.DocumentDatabase
import eka.care.documents.data.db.entity.VaultEntity
import eka.care.documents.data.db.model.AvailableDocTypes
import eka.care.documents.data.repository.DocumentsRepository
import eka.care.documents.data.repository.VaultRepositoryImpl
import eka.care.documents.sync.data.repository.SyncRecordsRepository
import eka.care.documents.sync.workers.SyncFileWorker
import eka.care.documents.ui.presentation.activity.DocumentViewActivity
import eka.care.documents.ui.presentation.activity.SmartReportActivity
import eka.care.documents.ui.presentation.components.FileSharing
import eka.care.documents.ui.presentation.model.CTA
import eka.care.documents.ui.presentation.model.RecordModel
import eka.care.documents.ui.presentation.screens.DocumentSortEnum
import eka.care.documents.ui.utility.RecordsUtility
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking

object Document {
    private var appContext: Context? = null
    private var configuration: DocumentConfiguration? = null
    private var db: DocumentDatabase? = null
    private var documentRepository: DocumentsRepository? = null
    private lateinit var recordsRepository: SyncRecordsRepository
    private var vitalTrendsNavigation: ((SmartReportClickData) -> Unit)? = null

    fun init(context: Context, documentConfiguration: DocumentConfiguration) {
        appContext = context.applicationContext
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
        if (appContext is Application) {
            recordsRepository = SyncRecordsRepository(appContext as Application)
        } else {
            Log.e("Document", "Context is not an Application, cannot initialize SyncRecordsRepository")
        }
    }

    fun setVitalTrendsNavigation(callback: (SmartReportClickData) -> Unit) {
        vitalTrendsNavigation = callback
    }

    fun navigateToVitalTrends(data: SmartReportClickData) {
        vitalTrendsNavigation?.invoke(data)
    }

    fun getContext(): Context? {
        return appContext
    }

    fun initSyncingData(context: Context, ownerId : String?, filterIds: List<String>?, patientUuid : String){
        val inputData = Data.Builder()
            .putString("p_uuid", patientUuid)
            .putString("ownerId", ownerId)
            .putString("filterIds", filterIds?.joinToString(","))
            .build()
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val uniqueWorkName = "syncFileWorker_${patientUuid}_$filterIds$ownerId"
        val uniqueSyncWorkRequest =
            OneTimeWorkRequestBuilder<SyncFileWorker>()
                .setInputData(inputData)
                .setConstraints(constraints)
                .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                uniqueWorkName,
                ExistingWorkPolicy.KEEP,
                uniqueSyncWorkRequest
            )
    }

    suspend fun alreadyExistDocument(documentId : String, ownerId : String?) : Int?{
       return documentRepository?.alreadyExistDocument(documentId = documentId, ownerId = ownerId)
    }

    fun downloadFileFromTheAssetUrl(url : String?, context: Context, type: String?) : String{
        var filePath = ""
        runBlocking {
            filePath = RecordsUtility.downloadFile(url, context, type ?: "")
        }
        return filePath
    }

    fun downloadThumbNailFromAssetUrl(url : String?, context: Context) : String{
        var thumbnail = ""
        runBlocking {
            thumbnail = RecordsUtility.downloadThumbnail(url, context)
        }
        return thumbnail
    }

    fun getDocuments(
        ownerId: String,
        filterIds: List<String>?,
        docType: Int = -1,
        sortBy : DocumentSortEnum
    ): Flow<List<VaultEntity>>? {
        return if (sortBy == DocumentSortEnum.UPLOAD_DATE) {
            documentRepository?.fetchDocuments(
                ownerId = ownerId,
                filterIds = filterIds,
                docType = docType
            )
        } else {
            documentRepository?.fetchDocumentsByDocDate(
                ownerId = ownerId,
                filterIds = filterIds,
                docType = docType
            )
        }
    }

    suspend fun storeDocuments(vaultEntityList: List<VaultEntity>) {
        documentRepository?.storeDocuments(vaultEntityList)
    }

    suspend fun deleteDocument(localId: String) {
        documentRepository?.deleteDocument(localId = localId)
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

    suspend fun getAvailableDocTypes(filterIds: List<String>?, ownerId: String?): List<AvailableDocTypes>?{
       return documentRepository?.getAvailableDocTypes(filterIds = filterIds, ownerId = ownerId)
    }

    fun view(context: Context, model: RecordModel, filterId: String?){
        if (model.autoTags?.split(",")?.contains("1") == true) {
            val date = RecordsUtility.convertLongToDateString(model.documentDate ?: model.createdAt)
            Intent(context, SmartReportActivity::class.java)
                .also {
                    it.putExtra("doc_id", model.documentId)
                    it.putExtra("local_id", model.localId)
                    it.putExtra("doctor_id", model.ownerId)
                    it.putExtra("user_id", filterId)
                    it.putExtra("doc_date", date)
                    context.startActivity(it)
                }
            return
        } else {
            Intent(context, DocumentViewActivity::class.java).also {
                it.putExtra("local_id", model.localId)
                it.putExtra("doc_id", model.documentId)
                it.putExtra("user_id", filterId)
                context.startActivity(it)
            }
            return
        }
    }


    fun destroy() {
        db?.clearAllTables()
    }
    suspend fun getDocumentById(id: String?): RecordModel? {
        if(id.isNullOrEmpty()) return null
        val vaultEntity = db?.vaultDao()?.getDocumentById(id)
        return vaultEntity?.toRecordModel()
    }

    fun getConfiguration() = configuration

    fun shareFiles(context: Context, filePaths : List<String>){
        FileSharing().shareFiles(context = context, filePaths = filePaths)
    }
}
fun VaultEntity.toRecordModel(): RecordModel {
    return RecordModel(
        localId = this.localId,
        documentId = this.documentId,
        ownerId = this.ownerId,
        documentType = this.documentType,
        documentDate = this.documentDate,
        createdAt = this.createdAt,
        thumbnail = this.thumbnail,
        filePath = this.filePath,
        fileType = this.fileType,
        cta = Gson().fromJson(this.cta, CTA::class.java),
        tags = this.tags,
        source = this.source,
        autoTags = this.autoTags,
        isAnalyzing = this.isAnalyzing
    )
}