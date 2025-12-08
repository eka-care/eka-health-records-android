package eka.care.records.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "record_tags_table",
    primaryKeys = ["document_id", "tag"],
    foreignKeys = [
        ForeignKey(
            entity = RecordEntity::class,
            parentColumns = ["document_id"],
            childColumns = ["document_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["document_id"]),
        Index(value = ["tag"])
    ]
)
data class TagEntity(
    @ColumnInfo(name = "document_id") val documentId: String,
    @ColumnInfo(name = "tag") val tag: String,
    @ColumnInfo(name = "type") val tagType: TagType = TagType.DOCUMENT_TAG
)

enum class TagType {
    DOCUMENT_TAG,
    SEARCH_TAG
}