package eka.care.documents.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "vault_table")
data class VaultEntity(
    @PrimaryKey @ColumnInfo(name = "local_id") var localId: String,
    @ColumnInfo(name = "doc_id") var documentId: String?,
    @ColumnInfo(name = "doctor_id")  var doctorId : String,
    @ColumnInfo(name = "uuid") var uuid: String?,
    @ColumnInfo(name = "oid") var oid: String?,
    @ColumnInfo(name = "owner_id") var ownerId: String? = null,
    @ColumnInfo(name = "filter_id") var filterId: String? = null,
    @ColumnInfo(name = "file_path") var filePath: List<String>?,
    @ColumnInfo(name = "file_type") var fileType: String,
    @ColumnInfo(name = "thumbnail") var thumbnail: String?,
    @ColumnInfo(name = "created_at") var createdAt: Long,
    @ColumnInfo(name = "source") var source: Int?,
    @ColumnInfo(name = "is_edited") var isEdited: Boolean = false,
    @ColumnInfo(name = "is_deleted") var isDeleted: Boolean = false,
    @ColumnInfo(name = "doc_type") var documentType: Int?,
    @ColumnInfo(name = "doc_date") var documentDate: Long?,
    @ColumnInfo(name = "tags") var tags: String?,
    @ColumnInfo(name = "cta") var cta: String?,
    @ColumnInfo(name = "hash_id") var hashId: String?,
    @ColumnInfo(name = "is_abha_linked") var isABHALinked: Boolean = false,
    @ColumnInfo(name = "share_with_doctor") var shareWithDoctor: Boolean = false,
    @ColumnInfo(name = "is_analyzing") var isAnalyzing: Boolean = false,
    @ColumnInfo(name = "smart_report_field") var smartReportField : String? = null
)