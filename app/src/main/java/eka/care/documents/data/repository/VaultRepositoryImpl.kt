package eka.care.documents.data.repository

import eka.care.documents.data.db.database.DocumentDatabase
import eka.care.documents.data.db.entity.VaultEntity
import eka.care.documents.data.db.model.AvailableDocTypes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class VaultRepositoryImpl(private val database: DocumentDatabase) : VaultRepository {
    override fun fetchDocuments(ownerId: String?, filterId: String?, docType: Int): Flow<List<VaultEntity>> {
        return if (docType == -1) {
            database.vaultDao().fetchDocumentsByOwnerId(ownerId = ownerId, filterId = filterId)
        } else {
            database.vaultDao().fetchDocuments(ownerId = ownerId, filterId = filterId, docType = docType)
        }
    }

    override suspend fun getSmartReport(filterId: String?, ownerId: String?, documentId: String) : String?{
        return withContext(Dispatchers.IO) {
            val result = database.vaultDao().getSmartReport(documentId = documentId)
            return@withContext result
        }
    }

    override suspend fun updateSmartReport(filterId: String?, ownerId: String?, documentId: String, smartReport: String) {
        withContext(Dispatchers.IO) {
            database.vaultDao().updateSmartReport(filterId = filterId, ownerId = ownerId, documentId = documentId, smartReport = smartReport)
        }
    }

    override suspend fun alreadyExistDocument(documentId: String, ownerId: String?): Int? {
        return withContext(Dispatchers.IO) {
            val isExist = database.vaultDao().alreadyExistDocument(documentId = documentId, ownerId = ownerId)
            return@withContext isExist
        }
    }

    // OLD

    override suspend fun updateDocuments(vaultEntityList: List<VaultEntity>) {
        database.vaultDao().updateDocuments(vaultEntityList)
    }

    override suspend fun storeDocuments(vaultEntityList: List<VaultEntity>) {
        database.vaultDao().storeDocuments(vaultEntityList)
    }

    override suspend fun deleteDocument(filterId: String?, localId: String) {
        database.vaultDao().deleteDocument(filterId = filterId, localId = localId)
        return
    }

    override suspend fun editDocument(
        localId: String,
        docType: Int?,
        docDate: Long?,
        filterId: String?
    ) {
        database.vaultDao().editDocument(
            localId = localId,
            docType = docType,
            docDate = docDate,
            filterId = filterId
        )
    }

    // OLD

    override suspend fun setThumbnail(thumbnail: String, documentId: String) {
        database.vaultDao().setThumbnail(thumbnail = thumbnail, docId = documentId)
    }

    override suspend fun storeDocument(
        localId: String,
        filterId: String?,
        docId: String,
        isAnalysing: Boolean,
        hasId: String,
        cta: String?,
        tags: String,
        autoTags : String,
        documentDate: Long?
    ) {
        database.vaultDao().storeDocument(
            localId = localId,
            filterId = filterId,
            docId = docId,
            isAnalysing = isAnalysing,
            hasId = hasId,
            cta = cta,
            tags = tags,
            autoTags = autoTags,
            documentDate = documentDate
        )
    }

    override suspend fun getUnSyncedDocuments(filterId: String?, ownerId: String?): List<VaultEntity> {
        val resp = database.vaultDao().getUnSyncedDocuments(
            filterId = filterId,
            ownerId = ownerId
        )
        return resp
    }

    override suspend fun getDeletedDocuments(filterId: String?, ownerId: String?): List<VaultEntity> {
        return database.vaultDao().getDeletedDocuments(filterId = filterId, ownerId = ownerId)
    }

    override suspend fun getEditedDocuments(filterId: String?, ownerId: String?): List<VaultEntity> {
        return database.vaultDao().getEditedDocuments(filterId = filterId, ownerId = ownerId)
    }

    override suspend fun fetchDocumentData(filterId: String, localId: String): VaultEntity {
        return database.vaultDao().getDocumentData(filterId = filterId, localId = localId)
    }

    override suspend fun getAvailableDocTypes(
        filterId: String?,
        ownerId: String?
    ): List<AvailableDocTypes> {
        return database.vaultDao().getAvailableDocTypes(filterId = filterId, ownerId = ownerId)
    }

    override fun fetchDocuments(
        filterId: String,
        docType: Int,
        ownerId : String?
    ): Flow<List<VaultEntity>> {
        return if (docType == -1) {
            database.vaultDao().fetchDocuments(filterId = filterId, ownerId = ownerId)
        } else {
            database.vaultDao()
                .fetchDocumentsByDocType(filterId = filterId, docType = docType, ownerId = ownerId)
        }
    }

    override fun fetchDocumentsByDocDate(
        filterId: String,
        docType: Int,
        ownerId: String?
    ): Flow<List<VaultEntity>> {
        return if (docType == -1) {
            database.vaultDao().fetchDocumentsByDocDate(filterId = filterId)
        } else {
            database.vaultDao()
                .fetchDocumentsByDocType(filterId = filterId, docType = docType, ownerId = ownerId)
        }
    }


    override suspend fun updateDocumentId(documentId: String, localId: String) {
        database.vaultDao().updateDocumentId(documentId, localId)
    }

    override suspend fun getLocalIdBySource(source: Int, filterId: String): List<String> {

        return database.vaultDao().getLocalIdBySource(filterId = filterId, source)
    }

    override suspend fun getLocalId(docId: String): String? {
        return database.vaultDao().getLocalId(docId)
    }

    override suspend fun getDocumentById(id: String): VaultEntity {
        return database.vaultDao().getDocumentById(id)
    }

    override suspend fun removeDocument(localId: String, filterId: String?) {
        database.vaultDao().removeDocument(localId = localId, filterId = filterId)
        return
    }

    override suspend fun getDocumentsWithoutFilePath(
        ownerId: String?,
        filterId: String?
    ): List<VaultEntity> {
        return database.vaultDao()
            .fetchDocumentsWithoutFilePath(ownerId = ownerId, filterId = filterId)
    }
}