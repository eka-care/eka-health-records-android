package eka.care.documents.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(primaryKeys = ["oid", "doctor_id"])
data class UpdatedAtEntity(
    @ColumnInfo(name = "oid") var oid: String,
    @ColumnInfo(name = "doctor_id") var doctor_id: String,
    @ColumnInfo(name = "updated_at") var updatedAt: String = "0",
)