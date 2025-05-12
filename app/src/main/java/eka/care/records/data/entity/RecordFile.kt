package eka.care.records.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.PrimaryKey

@Entity(
    tableName = "eka_record_file",
    foreignKeys = [
        ForeignKey(
            entity = RecordEntity::class,
            parentColumns = ["local_id"],
            childColumns = ["local_id"],
            onDelete = CASCADE
        )
    ]
)
data class RecordFile(
    @ColumnInfo(name = "_id") @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "local_id") val localId: String,
    @ColumnInfo(name = "file_path") val filePath: String,
    @ColumnInfo(name = "file_type") var fileType: String,
)