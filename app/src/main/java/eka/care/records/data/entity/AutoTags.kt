package eka.care.records.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.PrimaryKey

@Entity(
    tableName = "record_auto_tags",
    foreignKeys = [
        ForeignKey(
            entity = RecordEntity::class,
            parentColumns = ["local_id"],
            childColumns = ["local_id"],
            onDelete = CASCADE
        )
    ]
)
data class AutoTags(
    @ColumnInfo(name = "_id") @PrimaryKey(autoGenerate = true) val tagId: Int,
    @ColumnInfo(name = "local_id") val localId: String,
    @ColumnInfo(name = "tag") val tag: String? = null,
)