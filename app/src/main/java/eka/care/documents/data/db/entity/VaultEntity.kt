package eka.care.documents.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vault_table")
data class VaultEntity(
    @PrimaryKey @ColumnInfo(name = "local_id") var localId: String,
    @ColumnInfo(name = "doc_id") var documentId: String?,
    @ColumnInfo(name = "uuid") var uuid: String?,
    @ColumnInfo(name = "owner_id") var ownerId: String? = null, // doctorId new
    @ColumnInfo(name = "filter_id") var filterId: String? = null, // app_oid, patient_id
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
    @ColumnInfo(name = "auto_tags") var autoTags : String?,
    @ColumnInfo(name = "cta") var cta: String?,
    @ColumnInfo(name = "hash_id") var hashId: String?,
    @ColumnInfo(name = "is_analyzing") var isAnalyzing: Boolean,
    @ColumnInfo(name = "smart_report_field") var smartReportField : String? = null,
    @ColumnInfo(name = "status") var status : String? = null
)