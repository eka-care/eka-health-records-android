package eka.care.records.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "cases",
    primaryKeys = ["case_id"],
    indices = [
        Index(value = ["caseId"]),
        Index(value = ["name"]),
        Index(value = ["caseType"])
    ]
)
data class CaseEntity(
    @ColumnInfo(name = "case_id")
    val caseId: String,
    @ColumnInfo(name = "filter_id")
    val filterId: String,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "case_type")
    val caseType: String? = null,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long
)