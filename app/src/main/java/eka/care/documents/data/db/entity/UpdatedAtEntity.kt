package eka.care.documents.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(primaryKeys = ["filter_id"])
data class UpdatedAtEntity(
    @ColumnInfo(name = "filter_id") var filterId: String,
    @ColumnInfo(name = "owner_id") var ownerId: String? = null,
    @ColumnInfo(name = "updated_at") var updatedAt: String = "0",
)