package eka.care.records.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Update
import androidx.sqlite.db.SupportSQLiteQuery
import eka.care.records.data.entity.RecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordsDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun createRecords(records: List<RecordEntity>)

    @RawQuery(observedEntities = [RecordEntity::class])
    fun readRecords(query: SupportSQLiteQuery): Flow<List<RecordEntity>>

    @Query("SELECT * FROM EKA_RECORDS_TABLE WHERE RECORD_ID = :id")
    suspend fun getRecordByDocumentId(id: String): RecordEntity?

    @Query("SELECT MAX(UPDATED_AT) FROM EKA_RECORDS_TABLE WHERE OWNER_ID = :ownerId AND FILTER_ID = :filterId OR FILTER_ID IS NULL")
    suspend fun getLatestRecordUpdatedAt(ownerId: String, filterId: String?): Long

    @Update
    suspend fun updateRecords(records: List<RecordEntity>)

    @Delete
    suspend fun deleteRecords(records: List<RecordEntity>)
}