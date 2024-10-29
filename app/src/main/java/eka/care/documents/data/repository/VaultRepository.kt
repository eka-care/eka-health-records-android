package eka.care.documents.data.repository

import eka.care.documents.data.db.entity.VaultEntity
import eka.care.documents.data.db.model.AvailableDocTypes

interface VaultRepository {
    suspend fun storeDocuments(vaultEntityList: List<VaultEntity>)
    suspend fun setThumbnail(thumbnail: String, documentId: String?)
    suspend fun deleteDocument(oid: String, localId: String)
    suspend fun editDocument(
        localId: String,
        docType: Int?,
        oid: String?,
        docDate: Long,
        tags: String,
        isAbhaLinked: Boolean
    )
    suspend fun storeDocument(
        localId: String,
        oid: String?,
        tags: String,
        isAbhaLinked: Boolean,
        docId: String,
        isAnalysing: Boolean,
        hasId: String,
        cta: String?
    )
    suspend fun getUnsyncedDocuments(oid : String, doctorId: String): List<VaultEntity>
    suspend fun getDeletedDocuments(oid: String , doctorId: String): List<VaultEntity>
    suspend fun getEditedDocuments(oid: String, doctorId: String): List<VaultEntity>
    suspend fun fetchDocumentData(oid: String, localId: String): VaultEntity
    suspend fun getAvailableDocTypes(oid: String, doctorId: String): List<AvailableDocTypes>
    suspend fun fetchDocuments(oid: String, docType: Int, doctorId: String): List<VaultEntity>
    suspend fun fetchDocumentsByDocDate(oid: String, docType: Int, doctorId: String): List<VaultEntity>
    suspend fun updateDocumentId(documentId: String?, localId: String)
    suspend fun getLocalIdBySource(source: Int, oid: String): List<String>
    suspend fun getLocalId(docId: String): String
    suspend fun getDocumentByDocId(docId: String) : VaultEntity?
}