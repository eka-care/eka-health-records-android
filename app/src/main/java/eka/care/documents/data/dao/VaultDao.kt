package eka.care.documents.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import eka.care.documents.data.db.entity.VaultEntity
import eka.care.documents.data.db.model.AvailableDocTypes
import kotlinx.coroutines.flow.Flow

@Dao
interface VaultDao {
    // getting documents
    @Query("""
    SELECT * FROM vault_table 
    WHERE is_deleted = 0 
    AND owner_id = :ownerId
    AND (filter_id IN (:filterIds) OR filter_id IS NULL)
    ORDER BY created_at DESC
""")
    fun fetchDocuments(ownerId: String, filterIds: List<String>?): Flow<List<VaultEntity>>

    @Query("""
    SELECT * FROM vault_table 
    WHERE is_deleted = 0 
    AND owner_id = :ownerId 
    AND (filter_id IN (:filterIds) OR filter_id IS NULL)
    AND doc_type = :docType 
    ORDER BY created_at DESC
""")
    fun fetchDocumentsByDocType(
        ownerId: String,
        filterIds: List<String>?,
        docType: Int
    ): Flow<List<VaultEntity>>

    @Query("""
    SELECT * FROM vault_table 
    WHERE is_deleted = 0 
    AND (filter_id IN (:filterIds) OR filter_id IS NULL)
    AND (:ownerId IS NULL OR owner_id = :ownerId) 
    ORDER BY doc_date DESC
""")
    fun fetchDocumentsByDocDate(
        filterIds: List<String>?,
        ownerId: String
    ): Flow<List<VaultEntity>>

    @Query("""
    SELECT * FROM vault_table 
    WHERE is_deleted = 0 
    AND (filter_id IN (:filterIds) OR filter_id IS NULL) 
    AND (:ownerId IS NULL OR owner_id = :ownerId) 
    AND doc_type = :docType 
    ORDER BY doc_date DESC
""")
    fun fetchDocumentsByDocDateAndDocType(
        filterIds: List<String>?,
        ownerId: String,
        docType: Int
    ): Flow<List<VaultEntity>>

    @Query("SELECT * FROM vault_table WHERE doc_id=:id OR local_id=:id")
    suspend fun getDocumentById(id: String): VaultEntity

    @Query("SELECT local_id FROM vault_table WHERE doc_id = :docId")
    suspend fun getLocalId(docId: String?): String?

    // storing records and updating
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun storeDocuments(vaultEntityList: List<VaultEntity>)
    @Query("""
    UPDATE vault_table 
    SET filter_id = :filterId, 
        auto_tags = :autoTags, 
        doc_date = :documentDate, 
        tags = :tags, 
        is_analyzing = :isAnalysing, 
        hash_id = :hasId, 
        cta = :cta 
    WHERE local_id = :localId 
    AND doc_id = :docId
""")
    suspend fun storeDocument(
        localId: String,
        filterId: String?,
        docId: String,
        isAnalysing: Boolean,
        hasId: String,
        cta: String?,
        tags: String,
        autoTags: String,
        documentDate: Long?
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateDocuments(vaultEntityList: List<VaultEntity>)

    @Query("UPDATE vault_table SET thumbnail=:thumbnail WHERE doc_id=:docId")
    suspend fun setThumbnail(thumbnail: String, docId: String)

    // smart Report
    @Query("SELECT smart_report_field FROM vault_table WHERE doc_id = :documentId")
    suspend fun getSmartReport(documentId :String): String?

    @Query("""
    UPDATE vault_table 
    SET smart_report_field = :smartReport 
    WHERE doc_id = :documentId 
    AND (:filterId IS NULL OR filter_id = :filterId) 
    AND owner_id = :ownerId 
""")
    suspend fun updateSmartReport(
        filterId: String?,
        ownerId: String,
        documentId: String,
        smartReport: String
    )

    // edit

    @Query("""
    UPDATE vault_table 
    SET doc_type = :docType, doc_date = :docDate, is_edited = 1 
    WHERE local_id = :localId 
    AND (:filterId IS NULL OR filter_id = :filterId)
""")
    suspend fun editDocument(
        localId: String,
        docType: Int?,
        docDate: Long?,
        filterId: String?
    )
    @Query("""
    SELECT * FROM vault_table 
    WHERE is_edited = 1 
    AND owner_id = :ownerId 
    AND (filter_id IN (:filterIds) OR filter_id IS NULL)
""")
    suspend fun getEditedDocuments(filterIds: List<String>?, ownerId: String): List<VaultEntity>

    // delete
    @Query("""
    UPDATE vault_table 
    SET is_deleted = 1 
    WHERE local_id = :localId
""")
    suspend fun deleteDocument(localId: String)
    @Query("""
    SELECT * FROM vault_table 
    WHERE is_deleted = 1 
    AND owner_id = :ownerId 
    AND (filter_id IN (:filterIds) OR filter_id IS NULL)
""")
    suspend fun getDeletedDocuments(filterIds: List<String>?, ownerId: String): List<VaultEntity>
    @Query("""
    DELETE FROM vault_table 
    WHERE local_id = :localId 
    AND (:filterId IS NULL OR filter_id = :filterId)
""")
    suspend fun removeDocument(localId: String, filterId: String?)

    // filePath
    @Query("""
    SELECT * FROM vault_table 
    WHERE file_path IS NULL 
    AND owner_id = :ownerId 
    AND (filter_id IN (:filterIds) OR filter_id IS NULL)
""")
    fun fetchDocumentsWithoutFilePath(ownerId: String, filterIds: List<String>?): List<VaultEntity>

    @Query("UPDATE vault_table SET file_path=:filePath WHERE doc_id=:docId")
    suspend fun updateFilePath(docId: String?, filePath: String)

    // other operations
    @Query("""
    SELECT 1 FROM vault_table 
    WHERE doc_id = :documentId 
    AND (owner_id = :ownerId OR (owner_id IS NULL AND :ownerId IS NULL)) 
    LIMIT 1
""")
    suspend fun alreadyExistDocument(documentId: String, ownerId: String?): Int?

    @Query(
        """
    SELECT * FROM vault_table 
    WHERE doc_id IS NULL 
    AND is_deleted = 0 
    AND owner_id = :ownerId
    AND status IN ('WTU', 'WFN')
    AND (filter_id IN (:filterIds) OR filter_id IS NULL)
"""
    )
    suspend fun getUnSyncedDocuments(
        filterIds: List<String>?,
        ownerId: String
    ): List<VaultEntity>

    @Query("""
    SELECT * FROM vault_table 
    WHERE local_id = :localId 
    AND (:filterId IS NULL OR filter_id = :filterId)
""")
    suspend fun getDocumentData(filterId: String?, localId: String): VaultEntity

    @Query("""
    SELECT doc_type AS docType, COUNT(doc_type) AS count 
    FROM vault_table 
    WHERE is_deleted = 0 
    AND (:ownerId IS NULL OR owner_id = :ownerId) 
    AND (filter_id IN (:filterIds) OR filter_id IS NULL)
    GROUP BY doc_type
""")
    suspend fun getAvailableDocTypes(filterIds: List<String>?, ownerId: String?): List<AvailableDocTypes>

    @Query("UPDATE vault_table SET doc_id = :docId WHERE local_id = :localId")
    suspend fun updateDocumentId(docId: String, localId: String)

    @Query("UPDATE vault_table SET status = :newStatus WHERE local_id = :localId")
    suspend fun updateDocumentStatus(localId: String, newStatus: String)
}