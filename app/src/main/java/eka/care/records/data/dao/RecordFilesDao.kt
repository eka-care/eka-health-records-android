package eka.care.records.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import eka.care.records.data.entity.RecordFile

@Dao
interface RecordFilesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recordFile: RecordFile): Long

    @Query("SELECT * FROM EKA_RECORD_FILE WHERE LOCAL_ID = :localId")
    suspend fun getRecordFile(localId: String): List<RecordFile>?
}