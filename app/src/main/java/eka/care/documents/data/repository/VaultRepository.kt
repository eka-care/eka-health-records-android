package eka.care.documents.data.repository

import eka.care.documents.data.db.entity.VaultEntity
import eka.care.documents.data.db.model.AvailableDocTypes
import kotlinx.coroutines.flow.Flow

interface VaultRepository: DocumentsRepository {
    // NEW
    suspend fun getSmartReport(filterId: String?, ownerId: String?, documentId: String) : String?
    suspend fun updateSmartReport(filterId: String?, ownerId: String?, documentId: String, smartReport: String)

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
    suspend fun getUnSyncedDocuments(filterId: String?, ownerId: String?): List<VaultEntity>
    suspend fun getDeletedDocuments(filterId: String? , ownerId: String?): List<VaultEntity>
    suspend fun getEditedDocuments(filterId: String?, ownerId: String?): List<VaultEntity>
    suspend fun fetchDocumentData(filterId: String, localId: String): VaultEntity
    fun fetchDocuments(filterId: String, docType: Int, ownerId: String?): Flow<List<VaultEntity>>
    suspend fun updateDocumentId(documentId: String, localId: String)
    suspend fun getLocalIdBySource(source: Int, filterId: String): List<String>
    suspend fun getLocalId(docId: String): String?
    suspend fun getDocumentById(id: String) : VaultEntity?
    suspend fun removeDocument(localId: String, filterId: String?)
    suspend fun getDocumentsWithoutFilePath(ownerId: String?, filterId: String?) : List<VaultEntity>
}