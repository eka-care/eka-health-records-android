package eka.care.documents.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import eka.care.documents.data.db.entity.VaultEntity
import eka.care.documents.data.db.model.AvailableDocTypes
import eka.care.documents.sync.data.remote.dto.response.SmartReport
import kotlinx.coroutines.flow.Flow

@Dao
interface VaultDao {
    @Query("""
    SELECT * FROM vault_table 
    WHERE is_deleted = 0 
    AND (
        (:ownerId IS NULL AND :filterId IS NULL)  
        OR (owner_id = :ownerId OR :ownerId IS NULL) 
        AND (filter_id = :filterId OR :filterId IS NULL)
    )
    ORDER BY created_at DESC
""")
    fun fetchDocumentsByOwnerId(ownerId: String?, filterId: String?): Flow<List<VaultEntity>>


    @Query("""
    SELECT * FROM vault_table 
    WHERE 
        (:ownerId IS NULL AND :filterId IS NULL OR owner_id = :ownerId OR :ownerId IS NULL) 
        AND (:filterId IS NULL OR filter_id = :filterId)
        AND doc_type = :docType 
        AND is_deleted = 0 
    ORDER BY created_at DESC
""")
    fun fetchDocuments(
        ownerId: String?,
        filterId: String?,
        docType: Int
    ): Flow<List<VaultEntity>>

    @Query("SELECT 1 FROM vault_table WHERE doc_id = :documentId AND (owner_id = :ownerId OR (:ownerId IS NULL AND owner_id IS NULL)) LIMIT 1")
    suspend fun alreadyExistDocument(documentId: String, ownerId: String?): Int?

    @Query("SELECT smart_report_field FROM vault_table WHERE doc_id = :documentId")
    suspend fun getSmartReport(documentId :String): String?

    @Query("UPDATE vault_table SET smart_report_field = :smartReport WHERE (filter_id = :filterId OR (:filterId IS NULL AND filter_id IS NULL)) AND (owner_id = :ownerId OR (:ownerId IS NULL AND owner_id IS NULL)) AND doc_id =:documentId")
    suspend fun updateSmartReport(filterId: String?, ownerId: String?, documentId: String, smartReport: String)

    //OLD
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun storeDocuments(vaultEntityList: List<VaultEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateDocuments(vaultEntityList: List<VaultEntity>)

    @Query("UPDATE vault_table SET thumbnail=:thumbnail WHERE doc_id=:docId")
    suspend fun setThumbnail(thumbnail: String, docId: String?)

    @Query("SELECT * FROM vault_table WHERE (filter_id = :oid OR (:oid IS NULL AND filter_id IS NULL)) AND is_deleted=0 AND (owner_id = :doctorId OR (:doctorId IS NULL AND owner_id IS NULL)) ORDER BY created_at DESC")
    fun fetchDocuments(oid: String?, doctorId: String?): Flow<List<VaultEntity>>

    @Query("SELECT * FROM vault_table WHERE (filter_id = :oid OR (:oid IS NULL AND filter_id IS NULL)) AND is_deleted=0 AND (owner_id = :doctorId OR (:doctorId IS NULL AND owner_id IS NULL)) AND doc_type =:docType ORDER BY created_at DESC")
    fun fetchDocumentsByDocType(
        oid: String?,
        docType: Int,
        doctorId: String?
    ): Flow<List<VaultEntity>>

    @Query("SELECT * FROM vault_table WHERE (filter_id = :oid OR (:oid IS NULL AND filter_id IS NULL)) AND is_deleted=0 ORDER BY doc_date DESC")
    fun fetchDocumentsByDocDate(oid: String?): Flow<List<VaultEntity>>

    @Query("SELECT * FROM vault_table WHERE oid=:oid AND doc_type =:docType AND is_deleted=0 ORDER BY doc_date DESC")
    suspend fun fetchDocumentsByDocDateAndDocType(oid: String?, docType: Int): List<VaultEntity>

    @Query("UPDATE vault_table SET doc_type = :docType,  doc_date = :docDate, is_edited = 1 WHERE local_id = :localId AND (filter_id = :oid OR (:oid IS NULL AND filter_id IS NULL))")
    suspend fun editDocument(
        localId: String,
        docType: Int?,
        docDate: Long?,
        oid: String?,
    )

    @Query("UPDATE vault_table SET filter_id = :oid, auto_tags = :autoTags, doc_date = :documentDate ,tags = :tags, is_analyzing = :isAnalysing, hash_id = :hasId, cta = :cta WHERE local_id = :localId AND doc_id = :docId")
    suspend fun storeDocument(
        localId: String,
        oid: String?,
        docId: String,
        isAnalysing: Boolean,
        hasId: String,
        cta: String?,
        tags: String,
        autoTags : String,
        documentDate: Long?
    )

    @Query("UPDATE vault_table SET is_deleted=1 WHERE (filter_id = :oid OR (:oid IS NULL AND filter_id IS NULL)) AND local_id=:localId")
    suspend fun deleteDocument(oid: String?, localId: String)

    @Query("SELECT * FROM vault_table WHERE doc_id IS NULL AND (filter_id = :oid OR (:oid IS NULL AND filter_id IS NULL)) AND is_deleted = 0 AND (owner_id = :doctorId OR (:doctorId IS NULL AND owner_id IS NULL))")
    suspend fun getUnSyncedDocuments(oid: String?, doctorId: String?): List<VaultEntity>

    @Query("SELECT * FROM vault_table WHERE (filter_id = :oid OR (:oid IS NULL AND filter_id IS NULL)) AND local_id=:localId")
    suspend fun getDocumentData(oid: String?, localId: String): VaultEntity

    @Query("SELECT doc_type as docType, count(doc_type) as count FROM vault_table WHERE (filter_id = :filterId OR (:filterId IS NULL AND filter_id IS NULL)) AND is_deleted=0 AND (owner_id = :ownerId OR (:ownerId IS NULL AND owner_id IS NULL)) GROUP BY doc_type")
    suspend fun getAvailableDocTypes(filterId: String?, ownerId: String?): List<AvailableDocTypes>

    @Query("UPDATE vault_table SET doc_id=:docId WHERE local_id=:localId")
    suspend fun updateDocumentId(docId: String, localId: String)

    @Query("SELECT local_id FROM vault_table WHERE doc_id=:docId")
    suspend fun getLocalId(docId: String?): String?

    @Query("SELECT * FROM vault_table WHERE (filter_id = :oid OR (:oid IS NULL AND filter_id IS NULL)) AND is_deleted=1 AND (owner_id = :doctorId OR (:doctorId IS NULL AND owner_id IS NULL))")
    suspend fun getDeletedDocuments(oid: String?, doctorId: String?): List<VaultEntity>

    @Query("SELECT * FROM vault_table WHERE (filter_id = :oid OR (:oid IS NULL AND filter_id IS NULL)) AND is_edited=1 AND (owner_id = :doctorId OR (:doctorId IS NULL AND owner_id IS NULL))")
    suspend fun getEditedDocuments(oid: String?, doctorId: String?): List<VaultEntity>

    @Query("SELECT local_id FROM vault_table WHERE filter_id=:oid AND source=:source")
    suspend fun getLocalIdBySource(oid: String?, source: Int): List<String>

    @Query("SELECT * FROM vault_table WHERE doc_id=:id OR local_id=:id")
    suspend fun getDocumentById(id: String): VaultEntity

    @Query("UPDATE vault_table SET file_path=:filePath WHERE doc_id=:docId")
    suspend fun updateFilePath(docId: String?, filePath: String)

    @Query("DELETE FROM vault_table WHERE filter_id=:oid AND local_id=:localId")
    suspend fun removeDocument(localId: String, oid: String?)

    @Query("SELECT * FROM vault_table WHERE (owner_id = :doctorId OR (:doctorId IS NULL AND owner_id IS NULL)) AND  (filter_id = :patientoid OR (:patientoid IS NULL AND filter_id IS NULL)) and file_path is null")
    fun fetchDocumentsWithoutFilePath(doctorId: String?, patientoid : String?): List<VaultEntity>

}