package eka.care.documents.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import eka.care.documents.data.db.entity.UpdatedAtEntity

@Dao
interface UpdatedAtDao {

    @Query("SELECT updated_at FROM UpdatedAtEntity WHERE filter_id = :filterId AND owner_id = :ownerId")
    suspend fun getUpdatedAtByOid(filterId: String?, ownerId :String?): String?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUpdatedAtEntity(updatedAtEntity: UpdatedAtEntity)
    @Query("UPDATE UpdatedAtEntity SET updated_at = :updatedAt WHERE filter_id = :filterId AND owner_id = :ownerId")
    suspend fun updateUpdatedAtByOid(filterId: String?, updatedAt: String, ownerId :String?)
    @Query("SELECT COUNT(*) > 0 FROM UpdatedAtEntity WHERE filter_id = :filterId AND owner_id = :ownerId")
    suspend fun doesOidExist(filterId: String?, ownerId: String?): Boolean
}