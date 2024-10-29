package eka.care.documents.data.repository

import eka.care.documents.data.db.database.DocumentDatabase
import eka.care.documents.data.db.entity.VaultEntity
import eka.care.documents.data.db.model.AvailableDocTypes
import eka.care.documents.data.repository.VaultRepository

class VaultRepositoryImpl(private val database: DocumentDatabase) : VaultRepository {
    override suspend fun storeDocuments(vaultEntityList: List<VaultEntity>) {
        database.vaultDao().storeDocuments(vaultEntityList)
        return
    }

    override suspend fun setThumbnail(thumbnail: String, documentId: String?) {
        database.vaultDao().setThumbnail(thumbnail = thumbnail, docId = documentId)
        return
    }

    override suspend fun deleteDocument(oid: String, localId: String) {
        database.vaultDao().deleteDocument(oid = oid, localId = localId)
        return
    }

    override suspend fun editDocument(
        localId: String,
        docType: Int?,
        oid: String?,
        docDate: Long,
        tags: String,
        isAbhaLinked: Boolean
    ) {
        database.vaultDao().editDocument(
            localId = localId,
            docType = docType,
            oid = oid,
            docDate = docDate,
            tags = tags,
            isAbhaLinked = isAbhaLinked
        )
        return
    }

    override suspend fun storeDocument(
        localId: String,
        oid: String?,
        tags: String,
        isAbhaLinked: Boolean,
        docId: String,
        isAnalysing: Boolean,
        hasId: String,
        cta: String?
    ) {
        database.vaultDao().storeDocument(
            localId = localId,
            oid = oid,
            tags = tags,
            isAbhaLinked = isAbhaLinked,
            docId = docId,
            isAnalysing = isAnalysing,
            hasId = hasId,
            cta = cta
        )
    }

    override suspend fun getUnsyncedDocuments(oid: String, doctorId: String): List<VaultEntity> {
        val resp = database.vaultDao().getUnsyncedDocuments(
            oid = oid,
            doctorId = doctorId
        )
        return resp
    }

    override suspend fun getDeletedDocuments(oid: String, doctorId: String): List<VaultEntity> {
        return database.vaultDao().getDeletedDocuments(oid = oid, doctorId = doctorId)
    }

    override suspend fun getEditedDocuments(oid: String, doctorId: String): List<VaultEntity> {
        return database.vaultDao().getEditedDocuments(oid = oid, doctorId = doctorId)
    }

    override suspend fun fetchDocumentData(oid: String, localId: String): VaultEntity {
        return database.vaultDao().getDocumentData(oid = oid, localId = localId)
    }

    override suspend fun getAvailableDocTypes(oid: String, doctorId: String): List<AvailableDocTypes> {
        return database.vaultDao().getAvailableDocTypes(oid = oid, doctorId = doctorId)
    }

    override suspend fun fetchDocuments(
        oid: String,
        docType: Int,
        doctorId: String
    ): List<VaultEntity> {
        if (docType == -1)
            return database.vaultDao().fetchDocuments(oid = oid, doctorId = doctorId)

        return database.vaultDao().fetchDocumentsByDocType(oid = oid, docType = docType, doctorId = doctorId)
    }

    override suspend fun fetchDocumentsByDocDate(oid: String, docType: Int, doctorId: String): List<VaultEntity> {

        if (docType == -1)
            return database.vaultDao().fetchDocumentsByDocDate(oid)

        return database.vaultDao().fetchDocumentsByDocType(oid, docType, doctorId = doctorId)
    }

    override suspend fun updateDocumentId(documentId: String?, localId: String) {
        database.vaultDao().updateDocumentId(documentId, localId)
    }

    override suspend fun getLocalIdBySource(source: Int, oid: String): List<String> {

        return database.vaultDao().getLocalIdBySource(oid, source)
    }

    override suspend fun getLocalId(docId: String): String {
        return database.vaultDao().getLocalId(docId)
    }

    override suspend fun getDocumentByDocId(docId: String  ) : VaultEntity {
        return database.vaultDao().getDocumentByDocId(docId)
    }
}