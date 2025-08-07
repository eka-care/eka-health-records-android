package eka.care.records.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "cases_table",
    primaryKeys = ["case_id"],
    indices = [
        Index(value = ["case_id"]),
        Index(value = ["name"]),
        Index(value = ["case_type"])
    ]
)
data class CaseEntity(
    @ColumnInfo(name = "case_id")
    val caseId: String,
    @ColumnInfo(name = "owner_id")
    val ownerId: String,
    @ColumnInfo(name = "filter_id")
    val filterId: String?,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "case_type")
    val caseType: String? = null,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long
)