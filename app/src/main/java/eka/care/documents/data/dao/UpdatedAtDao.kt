package eka.care.documents.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import eka.care.documents.data.db.entity.UpdatedAtEntity

@Dao
interface UpdatedAtDao {

    @Query("SELECT updated_at FROM UpdatedAtEntity WHERE oid = :oid AND doctor_id = :doctorId")
    suspend fun getUpdatedAtByOid(oid: String, doctorId :String): String?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUpdatedAtEntity(updatedAtEntity: UpdatedAtEntity)
    @Query("UPDATE UpdatedAtEntity SET updated_at = :updatedAt WHERE oid = :oid AND doctor_id = :doctorId")
    suspend fun updateUpdatedAtByOid(oid: String, updatedAt: String, doctorId :String)
    @Query("SELECT COUNT(*) > 0 FROM UpdatedAtEntity WHERE oid = :oid AND doctor_id = :doctorId")
    suspend fun doesOidExist(oid: String, doctorId :String): Boolean
}