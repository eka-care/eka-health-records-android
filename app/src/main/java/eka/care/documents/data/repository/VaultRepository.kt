package eka.care.documents.data.repository

import eka.care.documents.data.db.entity.VaultEntity
import eka.care.documents.data.db.model.AvailableDocTypes
import kotlinx.coroutines.flow.Flow

interface VaultRepository {
    suspend fun updateDocuments(vaultEntityList: List<VaultEntity>)
    suspend fun storeDocuments(vaultEntityList: List<VaultEntity>)
    suspend fun setThumbnail(thumbnail: String, documentId: String?)
    suspend fun deleteDocument(oid: String, localId: String)
    suspend fun editDocument(
        localId: String,
        docType: Int?,
        docDate: Long?,
        tags: String,
        patientId : String
    )
    suspend fun storeDocument(
        localId: String,
        oid: String?,
        isAbhaLinked: Boolean,
        docId: String,
        isAnalysing: Boolean,
        hasId: String,
        cta: String?,
        tags: String,
        documentDate : Long?
    )
    suspend fun getUnsyncedDocuments(oid : String, doctorId: String): List<VaultEntity>
    suspend fun getDeletedDocuments(oid: String , doctorId: String): List<VaultEntity>
    suspend fun getEditedDocuments(oid: String, doctorId: String): List<VaultEntity>
    suspend fun fetchDocumentData(oid: String, localId: String): VaultEntity
    suspend fun getAvailableDocTypes(oid: String, doctorId: String): List<AvailableDocTypes>
    fun fetchDocuments(oid: String, docType: Int, doctorId: String): Flow<List<VaultEntity>>
    fun fetchDocumentsByDocDate(oid: String, docType: Int, doctorId: String): Flow<List<VaultEntity>>
    suspend fun updateDocumentId(documentId: String, localId: String)
    suspend fun getLocalIdBySource(source: Int, oid: String): List<String>
    suspend fun getLocalId(docId: String): String?
    suspend fun getDocumentById(id: String) : VaultEntity?
    suspend fun removeDocument(localId: String, oid: String)
    suspend fun getDocumentsWithoutFilePath(doctorId: String, patientOid : String) : List<VaultEntity>
}