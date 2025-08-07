package eka.care.records.data.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import eka.care.records.client.model.CaseModel

data class CaseWithRecords(
    @Embedded val caseEntity: CaseEntity,
    @Relation(
        parentColumn = "case_id",
        entityColumn = "local_id",
        associateBy = Junction(
            value = CaseRecordRelationEntity::class,
            parentColumn = "case_id",
            entityColumn = "record_id"
        )
    )
    val records: List<RecordEntity>
)

fun CaseWithRecords.toCaseModel(): CaseModel {
    return CaseModel(
        id = caseEntity.caseId,
        name = caseEntity.name,
        type = caseEntity.caseType.orEmpty(),
        createdAt = caseEntity.createdAt,
        updatedAt = caseEntity.updatedAt,
        records = records.map { it.toRecordModel() }
    )
}