package eka.care.records.data.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Junction
import androidx.room.Relation
import eka.care.records.client.model.CaseModel

@Entity(
    tableName = "encounter_record_relation",
    primaryKeys = ["encounter_id", "document_id"],
    foreignKeys = [
        ForeignKey(
            entity = EncounterEntity::class,
            parentColumns = ["encounter_id"],
            childColumns = ["encounter_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = RecordEntity::class,
            parentColumns = ["document_id"],
            childColumns = ["document_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class EncounterRecordCrossRef(
    @ColumnInfo(name = "encounter_id")
    val encounterId: String,
    @ColumnInfo(name = "document_id")
    val documentId: String
)

data class EncounterWithRecords(
    @Embedded val encounter: EncounterEntity,
    @Relation(
        parentColumn = "encounter_id",
        entityColumn = "document_id",
        associateBy = Junction(
            value = EncounterRecordCrossRef::class,
            parentColumn = "encounter_id",
            entityColumn = "document_id"
        )
    )
    val records: List<RecordEntity>
)

data class RecordWithEncounters(
    @Embedded val record: RecordEntity,
    @Relation(
        parentColumn = "document_id",
        entityColumn = "encounter_id",
        associateBy = Junction(
            value = EncounterRecordCrossRef::class,
            parentColumn = "document_id",
            entityColumn = "encounter_id"
        )
    )
    val encounters: List<EncounterEntity>
)

fun EncounterWithRecords.toCaseModel(): CaseModel {
    return CaseModel(
        id = encounter.encounterId,
        name = encounter.name,
        type = encounter.encounterType.orEmpty(),
        createdAt = encounter.createdAt,
        updatedAt = encounter.updatedAt,
        records = records.map { it.toRecordModel() }
    )
}