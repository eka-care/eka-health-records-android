package eka.care.documents.data.repository

import eka.care.documents.data.db.entity.VaultEntity
import eka.care.documents.data.db.model.AvailableDocTypes
import kotlinx.coroutines.flow.Flow

interface VaultRepository: DocumentsRepository {
    // NEW
    suspend fun getSmartReport(filterId: String?, ownerId: String?, documentId: String) : String?
    suspend fun updateSmartReport(filterId: String?, ownerId: String, documentId: String, smartReport: String)

    // OLD
    suspend fun updateDocuments(vaultEntityList: List<VaultEntity>)
    suspend fun setThumbnail(thumbnail: String, documentId: String)
    suspend fun storeDocument(
        localId: String,
        filterId: String?,
        docId: String,
        isAnalysing: Boolean,
        hasId: String,
        cta: String?,
        tags: String,
        autoTags : String,
        documentDate : Long?
    )
    suspend fun getUnSyncedDocuments(filterIds: List<String>?, ownerId: String): List<VaultEntity>
    suspend fun getDeletedDocuments(ownerId: String): List<VaultEntity>
    suspend fun getEditedDocuments(ownerId: String): List<VaultEntity>
    suspend fun updateDocumentId(documentId: String, localId: String)
    suspend fun getLocalId(docId: String): String?
    suspend fun getDocumentById(id: String) : VaultEntity?
    suspend fun removeDocument(localId: String, filterId: String?)
    suspend fun getDocumentsWithoutFilePath(ownerId: String) : List<VaultEntity>
}