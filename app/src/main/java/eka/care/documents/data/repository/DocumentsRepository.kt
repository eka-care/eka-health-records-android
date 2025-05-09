package eka.care.documents.data.repository

import eka.care.documents.data.db.entity.VaultEntity
import eka.care.documents.data.db.model.AvailableDocTypes
import kotlinx.coroutines.flow.Flow

interface DocumentsRepository {
    fun fetchDocuments(
        ownerId: String,
        filterIds: List<String>?,
        docType: Int
    ): Flow<List<VaultEntity>>

    fun fetchDocumentsByDocDate(
        filterIds: List<String>?,
        docType: Int,
        ownerId: String
    ): Flow<List<VaultEntity>>

    suspend fun storeDocuments(vaultEntityList: List<VaultEntity>)
    suspend fun deleteDocument(localId: String)
    suspend fun editDocument(
        localId: String,
        docType: Int?,
        docDate: Long?,
        filterId: String?,
    )

    suspend fun getAvailableDocTypes(
        filterIds: List<String>?,
        ownerId: String?
    ): List<AvailableDocTypes>

    suspend fun alreadyExistDocument(documentId: String, ownerId: String?): Int?
}