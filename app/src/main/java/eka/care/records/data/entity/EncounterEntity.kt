package eka.care.records.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "encounters_table",
    indices = [
        Index(value = ["name"]),
        Index(value = ["encounter_type"])
    ]
)
data class EncounterEntity(
    @ColumnInfo(name = "encounter_id") @PrimaryKey val encounterId: String,
    @ColumnInfo(name = "business_id") val businessId: String,
    @ColumnInfo(name = "owner_id") val ownerId: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "encounter_type") val encounterType: String? = null,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "is_synced") var isSynced: Boolean = false,
    @ColumnInfo(name = "is_dirty") var isDirty: Boolean = false,
    @ColumnInfo(name = "is_archived") var isArchived: Boolean = false
)