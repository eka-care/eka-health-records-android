package eka.care.records.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "case_record_relation",
    primaryKeys = ["case_id", "record_id"],
    foreignKeys = [
        ForeignKey(
            entity = CaseEntity::class,
            parentColumns = ["case_id"],
            childColumns = ["case_id"]
        ),
        ForeignKey(
            entity = RecordEntity::class,
            parentColumns = ["local_id"],
            childColumns = ["record_id"]
        )
    ]
)
data class CaseRecordRelationEntity(
    @ColumnInfo(name = "case_id")
    val caseId: String,
    @ColumnInfo(name = "record_id")
    val recordId: String
)