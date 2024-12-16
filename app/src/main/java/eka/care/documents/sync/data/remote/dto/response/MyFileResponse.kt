package eka.care.documents.sync.data.remote.dto.response

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import eka.care.documents.data.db.model.CTAData
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

@Keep
data class MyFileResponse(
    @SerializedName("count")
    val count: Int = 0,

    @SerializedName("documents")
    val documents: List<MyDocument?>? = null,

    @SerializedName("recent")
    val recent: List<MyDocument?>? = null,

    @SerializedName("parameters")
    val vitalParameters: List<VitalParamData>?,

    @SerializedName("parameters_v2")
    val labParameters: List<LabParameter>? = null,

    @SerializedName("banners")
    val banners: List<BannerData>?,

    @SerializedName("add_vitals_cta")
    val cta: CTAData? = null,

    @SerializedName("my_doctors")
    val myDoctors: ArrayList<DoctorInfo>? = null,

    @SerializedName("gmail_sync")
    val gmailSync: GmailSync? = null,

    @SerializedName("med_records_homepage")
    val recordsEmptyState: RecordsEmptyState? = null,

    @SerializedName("secret_vault")
    val secretVault: SecretVaultData? = null,

    @SerializedName("last_evaluated_key")
    val lastEvaluatedKey: String?,

    @SerializedName("request_id")
    val requestId: String?
)

@Parcelize
@Keep
data class RecordsEmptyState(
    @SerializedName("intro")
    val introHeader: EmptyStateIntroHeader? = null,
    @SerializedName("gmail_synced")
    val gmail_synced: Boolean = false,
    @SerializedName("show_new_screen")
    val showNewEmptyScreen: Boolean = false,
) : Parcelable

@Parcelize
@Keep
data class EmptyStateIntroHeader(
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("body")
    val body: String? = null,
    @SerializedName("image")
    val image: String? = null,
) : Parcelable

@Parcelize
@Keep
data class SecretVaultData(
    @SerializedName("enc_docs_count")
    val encryptedDocsCount: Int? = null,

    @SerializedName("curr_profile_count")
    val currProfileCount: Int? = null,

    @SerializedName("other_profile_doc")
    val otherProfileDoc: EncryptedDocument? = null,

    @SerializedName("total_doc_list")
    val encryptedDocsProfileInfoList: List<EncryptedDocsProfileInfo>? = null,

    @SerializedName("curr_profile_msg")
    val currProfileMsg: String? = null,

    @SerializedName("is_key_valid")
    val isKeyValid: Boolean = false,

    var currentProfileDoc: String? = null,
) : Parcelable

@Parcelize
@Keep
data class EncryptedDocsProfileInfo(
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("docs")
    val docsCount: Int = 0
) : Parcelable

@Parcelize
@Keep
data class GmailSync(
    @SerializedName("status")
    val status: String? = null,

    @SerializedName("sync_id")
    val id: String? = null,

    @SerializedName("sync_message")
    val msg: String? = null,

    @SerializedName("count")
    val recordsCount: Int = 0,

    @SerializedName("categories")
    val categories: List<GmailRecordsCategories>? = null,

    @SerializedName("total_count")
    val totalRecordsCount: Int = 0,

    @SerializedName("profile_doc_popup")
    val gmailRecordsSummary: GmailRecordsSummary? = null,
) : Parcelable

@Keep
data class GmailRecordsSummary(
    @SerializedName("title")
    val title: String? = null,

    @SerializedName("cta")
    val cta: CTAData? = null
) : Serializable

@Keep
data class DoctorInfo(

    @SerializedName("title")
    val title: String? = null,

    @SerializedName("oid")
    val oid: String? = null,

    @SerializedName("pic")
    val pic: String? = null,
) : Serializable

@Parcelize
@Keep
data class BannerData(

    @SerializedName("id")
    val id: String? = null,

    @SerializedName("title")
    val title: String? = null,

    @SerializedName("body")
    val body: String? = null,

    @SerializedName("category")
    val category: String? = null,

    @SerializedName("img")
    val image: String? = null,

    @SerializedName("cta")
    val cta: CTAData? = null,

    @SerializedName("lottie_id")
    val lottie: String? = null,

    @SerializedName("color")
    val color: String? = null,
) : Parcelable

@Keep
data class MyDocument(
    @SerializedName("document_id")
    val documentId: String? = null,

    @SerializedName("authorizer")
    val authorizer: String? = null,

    @SerializedName("patient_name")
    val patientName: String? = null,

    @SerializedName("document_date")
    var documentDate: String? = null,

    @SerializedName("document_type")
    val documentType: String? = null,

    @SerializedName("latest_document_type")
    val latestDocumentType: String? = null,

    @SerializedName("thumbnail")
    val thumbnail: String? = null,

    @SerializedName("doctor_image")
    val doctorImage: String? = null,

    @SerializedName("files_count")
    val filesCount: Int = 0,

    @SerializedName("files")
    val files: List<MyFilePage>? = null,

    @SerializedName("tagged_date_time")
    val taggedDateTime: String? = null,

    @SerializedName("search_meta")
    val searchMeta: SearchMeta? = null,

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("can_delete")
    val canDelete: Boolean = false,

    @SerializedName("smart_report")
    val smartReport: SmartReport? = null,

    @SerializedName("is_smart_report_available")
    val hasSmartReport: Boolean = false,

    @SerializedName("is_analyzing")
    val isAnalyzing: Boolean? = null,

    @SerializedName("post_analyzer_observations")
    val postAnalyzerObservations: String? = null,

    @SerializedName("desc")
    val desc: String? = null,  // new description field

    @SerializedName("shareable")
    val isShareable: Boolean = false,

    @SerializedName("user_tags")
    val userTags: List<String>? = null,

    @SerializedName("derived_tags")
    val derivedTags: List<String>? = null,

    @SerializedName("file_type")
    val fileType: String? = null,

    @SerializedName("shared_with")
    val sharedWith: List<String>? = null,

    @SerializedName("my_doctors")
    val myDoctors: List<DoctorInfo?>? = null,

    @SerializedName("health_id")
    val isAbhaLinked: Boolean = false,

    @SerializedName("file")
    val thumbnailFile: MyFilePage? = null,

    @SerializedName("cta")
    val cta: MyFilesCta?,

    @SerializedName("order_meds")
    val orderMeds: Boolean?,
)

@Parcelize
@Keep
data class MyFilesCta(
    @SerializedName("pid")
    val pid: String?,
    @SerializedName("params")
    val params: Map<String?, String?>
) : Parcelable

@Parcelize
@Keep
data class EncryptedDocument(
    @SerializedName("document_id")
    val documentId: String? = null,

    @SerializedName("authorizer")
    val authorizer: String? = null,

    @SerializedName("patient_name")
    val patientName: String? = null,

    @SerializedName("document_date")
    var documentDate: String? = null,

    @SerializedName("document_type")
    val documentType: String? = null,

    @SerializedName("files_count")
    val filesCount: Int = 0,

    @SerializedName("files")
    val files: List<MyFilePage>? = null,

    @SerializedName("desc")
    val desc: String? = null,  // new description field

    @SerializedName("user_tags")
    val userTags: List<String>? = null,

    @SerializedName("derived_tags")
    val derivedTags: List<String>? = null,

    @SerializedName("file_type")
    val fileType: String? = null,

    @SerializedName("file")
    val thumbnailFile: MyFilePage? = null
) : Parcelable

@Keep
data class SearchMeta(
    @SerializedName("highlight")
    val highlight: String? = null,

    @SerializedName("page")
    val page: Int = 0,

    @SerializedName("file_num")
    val fileNum: Int = 0,

    @SerializedName("document_date")
    val documentDate: String? = null,

    @SerializedName("authorizer")
    val authorizer: String? = null,
)

@Parcelize
@Keep
data class UnlinkedDocsResp(
    @SerializedName("document_ids")
    val unlinkedDocs: List<String>?,
) : Parcelable