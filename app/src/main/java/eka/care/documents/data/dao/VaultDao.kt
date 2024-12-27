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
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun storeDocuments(vaultEntityList: List<VaultEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateDocuments(vaultEntityList: List<VaultEntity>)

    @Query("UPDATE vault_table SET thumbnail=:thumbnail WHERE doc_id=:docId")
    suspend fun setThumbnail(thumbnail: String, docId: String?)

    @Query("SELECT * FROM vault_table WHERE oid=:oid AND is_deleted=0 AND doctor_id= :doctorId ORDER BY created_at DESC")
    fun fetchDocuments(oid: String?, doctorId: String): Flow<List<VaultEntity>>

    @Query("SELECT * FROM vault_table WHERE oid=:oid AND doc_type =:docType AND is_deleted=0 AND doctor_id =:doctorId ORDER BY created_at DESC")
    fun fetchDocumentsByDocType(
        oid: String?,
        docType: Int,
        doctorId: String
    ): Flow<List<VaultEntity>>

    @Query("SELECT * FROM vault_table WHERE oid=:oid AND is_deleted=0 ORDER BY doc_date DESC")
    fun fetchDocumentsByDocDate(oid: String?): Flow<List<VaultEntity>>

    @Query("SELECT * FROM vault_table WHERE oid=:oid AND doc_type =:docType AND is_deleted=0 ORDER BY doc_date DESC")
    suspend fun fetchDocumentsByDocDateAndDocType(oid: String?, docType: Int): List<VaultEntity>

    @Query("UPDATE vault_table SET doc_type = :docType,  doc_date = :docDate, tags = :tags, is_edited = 1 WHERE local_id = :localId AND oid = :oid")
    suspend fun editDocument(
        localId: String,
        docType: Int?,
        docDate: Long?,
        tags: String,
        oid: String?
    )

    @Query("UPDATE vault_table SET  oid = :oid, doc_date = :documentDate ,tags = :tags, is_abha_linked = :isAbhaLinked, is_analyzing = :isAnalysing, hash_id = :hasId, cta = :cta WHERE local_id = :localId AND doc_id = :docId")
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

    @Query("UPDATE vault_table SET is_deleted=1 WHERE oid=:oid AND local_id=:localId")
    suspend fun deleteDocument(oid: String?, localId: String)

    @Query("SELECT * FROM vault_table WHERE doc_id IS NULL AND oid=:oid AND is_deleted=0 AND doctor_id = :doctorId ")
    suspend fun getUnsyncedDocuments(oid: String?, doctorId: String): List<VaultEntity>

    @Query("SELECT * FROM vault_table WHERE oid=:oid AND local_id=:localId")
    suspend fun getDocumentData(oid: String?, localId: String): VaultEntity

    @Query("SELECT doc_type as docType, count(doc_type) as count FROM vault_table WHERE oid=:oid AND is_deleted=0 AND doctor_id = :doctorId GROUP BY doc_type")
    suspend fun getAvailableDocTypes(oid: String?, doctorId: String): List<AvailableDocTypes>

    @Query("UPDATE vault_table SET doc_id=:docId WHERE local_id=:localId")
    suspend fun updateDocumentId(docId: String, localId: String)

    @Query("SELECT local_id FROM vault_table WHERE doc_id=:docId")
    suspend fun getLocalId(docId: String?): String?

    @Query("SELECT * FROM vault_table WHERE oid=:oid AND is_deleted=1 AND doctor_id =:doctorId")
    suspend fun getDeletedDocuments(oid: String?, doctorId: String?): List<VaultEntity>

    @Query("SELECT * FROM vault_table WHERE oid=:oid AND is_edited=1 AND doctor_id =:doctorId")
    suspend fun getEditedDocuments(oid: String?, doctorId: String?): List<VaultEntity>

    @Query("SELECT local_id FROM vault_table WHERE oid=:oid AND source=:source")
    suspend fun getLocalIdBySource(oid: String?, source: Int): List<String>

    @Query("SELECT * FROM vault_table WHERE doc_id=:docId OR local_id=:docId")
    suspend fun getDocumentById(docId: String): VaultEntity

    @Query("UPDATE vault_table SET file_path=:filePath WHERE doc_id=:docId")
    suspend fun updateFilePath(docId: String?, filePath: String)

    @Query("DELETE FROM vault_table WHERE oid=:oid AND local_id=:localId")
    suspend fun removeDocument(localId: String, oid: String?)

    @Query("SELECT * FROM vault_table WHERE doctor_id = :doctorId AND oid = :patientoid")
    fun fetchDocumentsWithoutFilePath(doctorId: String, patientoid : String): Flow<List<VaultEntity>>
}