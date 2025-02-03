package eka.care.documents

import android.content.Context
import com.eka.network.ConverterFactoryType
import com.eka.network.Networking
import eka.care.documents.data.db.database.DocumentDatabase
import eka.care.documents.data.db.entity.VaultEntity
import eka.care.documents.data.repository.DocumentsRepository
import eka.care.documents.data.repository.VaultRepositoryImpl
import kotlinx.coroutines.flow.Flow

object Document {
    private var configuration: DocumentConfiguration? = null
    private var db: DocumentDatabase? = null
    private var documentRepository: DocumentsRepository? = null

    fun init(context: Context, documentConfiguration: DocumentConfiguration) {
        configuration = documentConfiguration
        configuration?.let {
            Networking.init(it.host, it.okHttpSetup, converterFactoryType = ConverterFactoryType.PROTO)
        }
        db = DocumentDatabase.getInstance(context)
        db?.let {
            documentRepository = VaultRepositoryImpl(it)
        }
    }

    fun getDocuments(ownerId: String, filterId: String, docType: Int = -1): Flow<List<VaultEntity>>? {
        return documentRepository?.fetchDocuments(
            ownerId = ownerId,
            filterId = filterId,
            docType = docType
        )
    }

    fun destroy(){
        db?.clearAllTables()
    }

    fun getConfiguration() = configuration
}