package eka.care.records.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Fts4
import androidx.room.PrimaryKey

@Entity(
    tableName = "files_table",
    foreignKeys = [
        ForeignKey(
            entity = RecordEntity::class,
            parentColumns = ["document_id"],
            childColumns = ["document_id"],
            onDelete = CASCADE
        )
    ]
)
data class FileEntity(
    @ColumnInfo(name = "file_id") @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "document_id") val documentId: String,
    @ColumnInfo(name = "file_path") val filePath: String,
    @ColumnInfo(name = "file_type") var fileType: String,
    @ColumnInfo(name = "last_used") var lastUsed: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "size_bytes") var sizeBytes: Long = 0L,
    @ColumnInfo(name = "ocr_text") var ocrText: String? = ""
)

@Fts4(contentEntity = FileEntity::class)
@Entity(tableName = "file_entity_fts")
data class FileEntityFts(
    @PrimaryKey
    @ColumnInfo(name = "rowid") val rowid: Int,
    @ColumnInfo(name = "file_id") val fileId: Long,
    @ColumnInfo(name = "document_id") val documentId: String,
    @ColumnInfo(name = "ocr_text") var ocrText: String? = "",
)