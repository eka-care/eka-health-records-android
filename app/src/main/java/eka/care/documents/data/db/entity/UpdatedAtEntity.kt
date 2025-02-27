package eka.care.documents.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(primaryKeys = ["owner_id"])
data class UpdatedAtEntity(
    @ColumnInfo(name = "filter_id") var filterId: String? = null,
    @ColumnInfo(name = "owner_id") var ownerId: String,
    @ColumnInfo(name = "updated_at") var updatedAt: String? = null,
)