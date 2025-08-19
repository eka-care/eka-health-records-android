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
import eka.care.records.data.entity.FileEntity
import eka.care.records.data.entity.RecordEntity
import eka.care.records.data.entity.RecordStatus
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

//    @Query("SELECT * FROM EKA_RECORDS_TABLE WHERE BUSINESS_ID = :businessId AND IS_ARCHIVED = 1")
//    suspend fun getDeletedRecords(businessId: String): List<RecordEntity>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecordFile(recordFile: FileEntity): Long

    @Query("SELECT * FROM FILES_TABLE WHERE DOCUMENT_ID = :documentId")
    suspend fun getRecordFile(documentId: String): List<FileEntity>?
}