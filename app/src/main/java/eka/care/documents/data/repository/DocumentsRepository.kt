package eka.care.documents.data.repository

import eka.care.documents.data.db.entity.VaultEntity
import kotlinx.coroutines.flow.Flow

interface DocumentsRepository {
    fun fetchDocuments(ownerId: String?, filterId: String?, docType: Int): Flow<List<VaultEntity>>
    suspend fun storeDocuments(vaultEntityList: List<VaultEntity>)
    suspend fun deleteDocument(filterId: String, localId: String)
    suspend fun editDocument(
        localId: String,
        docType: Int?,
        docDate: Long?,
        filterId: String?,
        isAbhaLinked : Boolean
    )
}