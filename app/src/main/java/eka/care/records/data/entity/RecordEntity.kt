package eka.care.records.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "eka_records_table",
    indices = [
        Index(value = ["owner_id"]),
        Index(value = ["document_type"]),
        Index(value = ["is_dirty"]),
        Index(value = ["is_archived"])
    ]
)
data class RecordEntity(
    @ColumnInfo(name = "local_id") @PrimaryKey val id: String,
    @ColumnInfo(name = "record_id") val recordId: String? = null,
    @ColumnInfo(name = "owner_id") var ownerId: String,
    @ColumnInfo(name = "filter_id") var filterId: String? = null,
    @ColumnInfo(name = "thumbnail") var thumbnail: String? = null,
    @ColumnInfo(name = "created_at") var createdAt: Long,
    @ColumnInfo(name = "updated_at") var updatedAt: Long,
    @ColumnInfo(name = "document_date") var documentDate: Long? = null,
    @ColumnInfo(name = "document_type") var documentType: String = "ot",
    @ColumnInfo(name = "document_hash") var documentHash: String? = null,
    @ColumnInfo(name = "is_dirty") var isDirty: Boolean = false,
    @ColumnInfo(name = "is_archived") var isDeleted: Boolean = false,
)