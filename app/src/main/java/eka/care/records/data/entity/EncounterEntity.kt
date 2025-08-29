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
    @ColumnInfo(name = "ui_state") val uiState: CaseUiState = CaseUiState.NONE,
    @ColumnInfo(name = "status") val status: CaseStatus = CaseStatus.NONE,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "encounter_type") val encounterType: String? = null,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
)

enum class CaseStatus(val value: Int) {
    NONE(0),                   // Default state, no action taken
    CREATED_LOCALLY(1),        // Record created but not yet uploaded
    UPDATED_LOCALLY(2),        // Record updated locally but not yet uploaded
    SYNC_COMPLETED(3),         // Record successfully uploaded or synced
    ARCHIVED(4),               // Record marked as archived
}

enum class CaseUiState(val status: Int) {
    NONE(0),
    WAITING_TO_UPLOAD(1),
    WAITING_FOR_NETWORK(2),
    SYNCING(3),
    SYNC_FAILED(4),
    SYNC_SUCCESS(5)
}