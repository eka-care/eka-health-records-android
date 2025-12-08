package eka.care.records.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Transaction
import androidx.room.Update
import androidx.sqlite.db.SupportSQLiteQuery
import eka.care.records.client.model.DocumentTypeCount
import eka.care.records.client.model.TagModel
import eka.care.records.data.entity.FileEntity
import eka.care.records.data.entity.RecordEntity
import eka.care.records.data.entity.RecordStatus
import eka.care.records.data.entity.TagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordsDao {
    @Transaction
    suspend fun insertRecordWithFiles(record: RecordEntity, files: List<FileEntity>) {
        createRecords(listOf(record))
        files.forEach { insertRecordFile(it) }
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun createRecords(records: List<RecordEntity>)

    @RawQuery(observedEntities = [RecordEntity::class])
    fun readRecords(query: SupportSQLiteQuery): Flow<List<RecordEntity>>

    @Query("SELECT * FROM EKA_RECORDS_TABLE WHERE DOCUMENT_ID = :id")
    suspend fun getRecordById(id: String): RecordEntity?

    @Query("SELECT * FROM EKA_RECORDS_TABLE WHERE DOCUMENT_ID = :id")
    suspend fun getRecordByDocumentId(id: String): RecordEntity?

    @Query("SELECT MAX(UPDATED_AT) FROM EKA_RECORDS_TABLE WHERE BUSINESS_ID = :businessId AND OWNER_ID = :ownerId")
    fun getLatestRecordUpdatedAt(businessId: String, ownerId: String?): Long?

    @RawQuery(observedEntities = [RecordEntity::class])
    fun getDocumentTypeCounts(query: SupportSQLiteQuery): Flow<List<DocumentTypeCount>>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateRecord(record: RecordEntity)

    @Delete
    suspend fun deleteRecord(record: RecordEntity)

    @Query("SELECT * FROM EKA_RECORDS_TABLE WHERE BUSINESS_ID = :businessId AND status in (:list)")
    suspend fun getRecordsByStatus(
        businessId: String,
        list: List<RecordStatus>
    ): List<RecordEntity>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecordFile(recordFile: FileEntity): Long

    @Update
    suspend fun updateRecordFiles(recordFiles: List<FileEntity>)

    @Query("UPDATE FILES_TABLE SET OCR_TEXT = :ocrText WHERE file_id = :fileId")
    suspend fun addOcrTextToFile(fileId: Long, ocrText: String)

    @Delete
    suspend fun deleteRecordFiles(recordFiles: List<FileEntity>)

    @Query("SELECT * FROM FILES_TABLE WHERE DOCUMENT_ID = :documentId")
    suspend fun getRecordFile(documentId: String): List<FileEntity>?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTag(tagEntity: TagEntity)

    @Query(
        """
        SELECT t.document_id AS documentId, t.tag AS tag
        FROM record_tags_table t
        WHERE t.document_id IN (
            SELECT r.document_id
            FROM eka_records_table r
            WHERE r.business_id = :businessId
              AND (:ownerIdsSize = 0 OR r.owner_id IN (:ownerIds))
        )
    """
    )
    fun getDocumentTagsForBusinessAndOwners(
        businessId: String,
        ownerIds: List<String>,
        ownerIdsSize: Int = ownerIds.size
    ): Flow<List<TagModel>>

    @Query(
        """
        WITH document_stats AS (
            SELECT 
                document_id,
                SUM(size_bytes) as total_size,
                MIN(last_used) as oldest_last_used
            FROM files_table
            GROUP BY document_id
        ),
        documents_to_keep AS (
            SELECT 
                ds1.document_id
            FROM document_stats ds1
            WHERE (
                SELECT COALESCE(SUM(ds2.total_size), 0)
                FROM document_stats ds2
                WHERE ds2.oldest_last_used > ds1.oldest_last_used
                   OR (ds2.oldest_last_used = ds1.oldest_last_used AND ds2.document_id >= ds1.document_id)
            ) <= :maxSizeBytes
        )
        SELECT f.*
        FROM files_table f
        WHERE f.document_id NOT IN (SELECT document_id FROM documents_to_keep)
        ORDER BY f.last_used ASC
    """
    )
    suspend fun getFilesToDeleteByMaxSize(maxSizeBytes: Long): List<FileEntity>

    @RawQuery
    suspend fun searchDocument(query: SupportSQLiteQuery): List<RecordEntity>
}