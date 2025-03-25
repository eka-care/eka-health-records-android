package eka.care.documents.data.repository

import eka.care.documents.data.db.entity.VaultEntity

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
    suspend fun getDeletedDocuments(filterIds: List<String>? , ownerId: String): List<VaultEntity>
    suspend fun getEditedDocuments(filterIds: List<String>?, ownerId: String): List<VaultEntity>
    suspend fun updateDocumentId(documentId: String, localId: String)
    suspend fun getLocalId(docId: String): String?
    suspend fun getDocumentById(id: String) : VaultEntity?
    suspend fun removeDocument(localId: String, filterId: String?)
    suspend fun getDocumentsWithoutFilePath(ownerId: String, filterIds: List<String>?) : List<VaultEntity>
    suspend fun updateDocumentStatus(localId: String, status: String)
}