package eka.care.records.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import eka.care.records.data.entity.EncounterEntity
import eka.care.records.data.entity.EncounterRecordCrossRef
import eka.care.records.data.entity.EncounterWithRecords
import eka.care.records.data.entity.RecordWithEncounters
import kotlinx.coroutines.flow.Flow

@Dao
interface EncounterRecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEncounter(encounter: EncounterEntity)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateEncounter(encounter: EncounterEntity)

    @Delete
    suspend fun deleteEncounter(encounter: EncounterEntity)

    @Query("SELECT * FROM ENCOUNTERS_TABLE WHERE BUSINESS_ID = :businessId AND OWNER_ID = :ownerId")
    fun getAllEncounters(businessId: String, ownerId: String): Flow<List<EncounterWithRecords>>

    @Query("SELECT * FROM ENCOUNTERS_TABLE WHERE OWNER_ID = :ownerId AND IS_SYNCED = 0")
    suspend fun getUnsyncedEncounters(ownerId: String): List<EncounterEntity>?

    @Query("SELECT * FROM ENCOUNTERS_TABLE WHERE OWNER_ID = :ownerId AND IS_DIRTY = 1")
    suspend fun getDirtyEncounter(ownerId: String): List<EncounterEntity>?

    @Query("SELECT MAX(UPDATED_AT) FROM ENCOUNTERS_TABLE WHERE BUSINESS_ID = :businessId AND OWNER_ID = :ownerId")
    fun getLatestEncounterUpdatedAt(businessId: String, ownerId: String): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEncounterRecordCrossRef(crossRef: EncounterRecordCrossRef)

    @Query("DELETE FROM ENCOUNTER_RECORD_RELATION WHERE ENCOUNTER_ID = :encounterId AND DOCUMENT_ID = :documentId")
    suspend fun removeEncounterRecord(encounterId: String, documentId: String)

    @Transaction
    @Query("SELECT * FROM ENCOUNTERS_TABLE WHERE ENCOUNTER_ID = :encounterId")
    suspend fun getEncounterById(encounterId: String): EncounterWithRecords?

    @Transaction
    @Query("SELECT * FROM EKA_RECORDS_TABLE WHERE document_id = :documentId")
    suspend fun getRecordWithEncounters(documentId: String): RecordWithEncounters
}
