package eka.care.documents.data.repository

import android.util.Log
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
        database.vaultDao().deleteDocument(oid = filterId, localId = localId)
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
            oid = filterId
        )
    }

    // OLD

    override suspend fun setThumbnail(thumbnail: String, documentId: String?) {
        Log.d("AYUSHI-3", thumbnail)
        database.vaultDao().setThumbnail(thumbnail = thumbnail, docId = documentId)
    }

    override suspend fun storeDocument(
        localId: String,
        oid: String?,
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
            oid = oid,
            docId = docId,
            isAnalysing = isAnalysing,
            hasId = hasId,
            cta = cta,
            tags = tags,
            autoTags = autoTags,
            documentDate = documentDate
        )
    }

    override suspend fun getUnSyncedDocuments(oid: String?, doctorId: String?): List<VaultEntity> {
        val resp = database.vaultDao().getUnSyncedDocuments(
            oid = oid,
            doctorId = doctorId
        )
        return resp
    }

    override suspend fun getDeletedDocuments(oid: String?, doctorId: String?): List<VaultEntity> {
        return database.vaultDao().getDeletedDocuments(oid = oid, doctorId = doctorId)
    }

    override suspend fun getEditedDocuments(oid: String?, doctorId: String?): List<VaultEntity> {
        return database.vaultDao().getEditedDocuments(oid = oid, doctorId = doctorId)
    }

    override suspend fun fetchDocumentData(oid: String, localId: String): VaultEntity {
        return database.vaultDao().getDocumentData(oid = oid, localId = localId)
    }

    override suspend fun getAvailableDocTypes(
        filterId: String?,
        ownerId: String?
    ): List<AvailableDocTypes> {
        return database.vaultDao().getAvailableDocTypes(filterId = filterId, ownerId = ownerId)
    }

    override fun fetchDocuments(
        oid: String,
        docType: Int,
        doctorId: String?
    ): Flow<List<VaultEntity>> {
        return if (docType == -1) {
            database.vaultDao().fetchDocuments(oid = oid, doctorId = doctorId)
        } else {
            database.vaultDao()
                .fetchDocumentsByDocType(oid = oid, docType = docType, doctorId = doctorId)
        }
    }

    override fun fetchDocumentsByDocDate(
        oid: String,
        docType: Int,
        doctorId: String?
    ): Flow<List<VaultEntity>> {
        return if (docType == -1) {
            database.vaultDao().fetchDocumentsByDocDate(oid = oid)
        } else {
            database.vaultDao()
                .fetchDocumentsByDocType(oid = oid, docType = docType, doctorId = doctorId)
        }
    }


    override suspend fun updateDocumentId(documentId: String, localId: String) {
        database.vaultDao().updateDocumentId(documentId, localId)
    }

    override suspend fun getLocalIdBySource(source: Int, oid: String): List<String> {

        return database.vaultDao().getLocalIdBySource(oid, source)
    }

    override suspend fun getLocalId(docId: String): String? {
        return database.vaultDao().getLocalId(docId)
    }

    override suspend fun getDocumentById(id: String): VaultEntity {
        return database.vaultDao().getDocumentById(id)
    }

    override suspend fun removeDocument(localId: String, oid: String?) {
        database.vaultDao().removeDocument(localId = localId, oid = oid)
        return
    }

    override suspend fun getDocumentsWithoutFilePath(
        doctorId: String?,
        patientOid: String?
    ): List<VaultEntity> {
        return database.vaultDao()
            .fetchDocumentsWithoutFilePath(doctorId = doctorId, patientoid = patientOid)
    }
}