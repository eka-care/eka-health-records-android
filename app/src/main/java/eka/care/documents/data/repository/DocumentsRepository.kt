package eka.care.documents.data.repository

import eka.care.documents.data.db.entity.VaultEntity
import eka.care.documents.data.db.model.AvailableDocTypes
import kotlinx.coroutines.flow.Flow

interface DocumentsRepository {
    fun fetchDocuments(ownerId: String?, filterId: String?, docType: Int): Flow<List<VaultEntity>>
    suspend fun storeDocuments(vaultEntityList: List<VaultEntity>)
    suspend fun deleteDocument(filterId: String?, localId: String)
    suspend fun editDocument(
        localId: String,
        docType: Int?,
        docDate: Long?,
        filterId: String?,
    )
    suspend fun getAvailableDocTypes(filterId: String?, ownerId: String?): List<AvailableDocTypes>
    suspend fun alreadyExistDocument(documentId: String, ownerId : String?): Int?
}