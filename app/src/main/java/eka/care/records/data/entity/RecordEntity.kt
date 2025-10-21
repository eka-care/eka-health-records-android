package eka.care.records.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import eka.care.records.client.model.RecordModel
import eka.care.records.client.model.RecordUiState

@Entity(
    tableName = "eka_records_table",
    primaryKeys = ["document_id"],
    indices = [
        Index(value = ["document_type"]),
    ]
)
data class RecordEntity(
    @ColumnInfo(name = "document_id") val documentId: String,
    @ColumnInfo(name = "ui_state") val uiState: RecordUiState = RecordUiState.NONE,
    @ColumnInfo(name = "status") val status: RecordStatus = RecordStatus.NONE,
    @ColumnInfo(name = "business_id") val businessId: String,
    @ColumnInfo(name = "owner_id") val ownerId: String,
    @ColumnInfo(name = "thumbnail") var thumbnail: String? = null,
    @ColumnInfo(name = "created_at") var createdAt: Long,
    @ColumnInfo(name = "updated_at") var updatedAt: Long,
    @ColumnInfo(name = "document_date") var documentDate: Long? = null,
    @ColumnInfo(name = "document_type") var documentType: String = "ot",
    @ColumnInfo(name = "document_hash") var documentHash: String? = null,
    @ColumnInfo(name = "is_abha_link") var isAbhaLink: Boolean = true,
    @ColumnInfo(name = "source") var source: String? = null,
    @ColumnInfo(name = "is_smart") var isSmart: Boolean = false,
    @ColumnInfo(name = "smart_report_field") var smartReport: String? = null,
)

enum class RecordStatus(val value: Int) {
    NONE(0),                   // Default state, no action taken
    CREATED_LOCALLY(1),        // Record created but not yet uploaded
    UPDATED_LOCALLY(2),        // Record updated locally but not yet uploaded
    SYNC_COMPLETED(3),         // Record successfully uploaded or synced
    ARCHIVED(4),               // Record marked as archived
}

fun RecordEntity.toRecordModel(): RecordModel {
    return RecordModel(
        id = documentId,
        thumbnail = thumbnail,
        status = status,
        uiState = uiState,
        createdAt = createdAt,
        updatedAt = updatedAt,
        documentDate = documentDate,
        documentType = documentType,
        isSmart = isSmart,
        smartReport = smartReport,
    )
}