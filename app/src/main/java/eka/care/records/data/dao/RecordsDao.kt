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
import eka.care.records.data.entity.RecordEntity
import eka.care.records.data.entity.RecordFile
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordsDao {
    @Transaction
    suspend fun insertRecordWithFiles(record: RecordEntity, files: List<RecordFile>) {
        createRecords(listOf(record))
        files.forEach { insertRecordFile(it) }
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun createRecords(records: List<RecordEntity>)

    @RawQuery(observedEntities = [RecordEntity::class])
    fun readRecords(query: SupportSQLiteQuery): Flow<List<RecordEntity>>

    @Query("SELECT * FROM EKA_RECORDS_TABLE WHERE LOCAL_ID = :id")
    suspend fun getRecordById(id: String): RecordEntity?

    @Query("SELECT * FROM EKA_RECORDS_TABLE WHERE DOCUMENT_ID = :id")
    suspend fun getRecordByDocumentId(id: String): RecordEntity?

    @Query("SELECT MAX(UPDATED_AT) FROM EKA_RECORDS_TABLE WHERE OWNER_ID = :ownerId AND (FILTER_ID = :filterId OR FILTER_ID IS NULL)")
    fun getLatestRecordUpdatedAt(ownerId: String, filterId: String?): Long?

    @RawQuery(observedEntities = [RecordEntity::class])
    fun getDocumentTypeCounts(query: SupportSQLiteQuery): Flow<List<DocumentTypeCount>>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateRecords(records: List<RecordEntity>)

    @Delete
    suspend fun deleteRecord(record: RecordEntity)

    @Query("SELECT * FROM EKA_RECORDS_TABLE WHERE OWNER_ID = :ownerId AND IS_DIRTY = 1")
    suspend fun getDirtyRecords(ownerId: String): List<RecordEntity>?

    @Query("SELECT * FROM EKA_RECORDS_TABLE WHERE OWNER_ID = :ownerId AND IS_ARCHIVED = 1")
    suspend fun getDeletedRecords(ownerId: String): List<RecordEntity>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecordFile(recordFile: RecordFile): Long

    @Query("SELECT * FROM EKA_RECORD_FILE WHERE LOCAL_ID = :localId")
    suspend fun getRecordFile(localId: String): List<RecordFile>?
}