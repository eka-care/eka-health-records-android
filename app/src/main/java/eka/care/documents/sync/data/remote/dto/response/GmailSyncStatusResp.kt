package eka.care.documents.sync.data.remote.dto.response

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import eka.care.documents.data.db.model.CTAData
import kotlinx.android.parcel.Parcelize

@Keep
data class GmailSyncedAccountsResp(
    @SerializedName("success")
    var success: Boolean? = null,
    @SerializedName("accounts")
    var accounts: List<SyncedGmailAccount>? = null,
    @SerializedName("csrf_token")
    var csrfToken: String? = null,
    @SerializedName("share")
    val gmailShare: GmailShare? = null,
    @SerializedName("cta")
    val cta: CTAData? = null,
    @SerializedName("categories")
    val allGmailRecordsCategories: List<GmailRecordsCategories>? = null,
    @SerializedName("completion_progress")
    var syncProgressPercentage: Float = 0f,
    @SerializedName("sticky_notification")
    var stickyNotifyData: StickyNotifyData? = null,
    @SerializedName("home_page_content")
    var homePageContent: GmailHomeContent? = null,
    @SerializedName("select_view")
    var gmailAccountsList: List<GmailAccountInfo>? = null,
    @SerializedName("all_accounts_sync_msg")
    var allAccountsRecordsCount: String? = null
)

@Keep
@Parcelize
data class GmailAccountInfo(
    @SerializedName("tab_title")
    var title: String? = null,
    @SerializedName("image")
    var image: String? = null,
    @SerializedName("description")
    val desc: String? = null,
    var isSelected: Boolean = false
) : Parcelable

@Keep
@Parcelize
data class GmailHomeContent(
    @SerializedName("title")
    var title: String? = null,
    @SerializedName("synced_user_count")
    var syncedCount: String? = null,
    @SerializedName("body")
    val body: String? = null,
    @SerializedName("image")
    val image: String? = null,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("content")
    val contentItems: List<GmailHomeContentItem>? = null
) : Parcelable

@Keep
@Parcelize
data class GmailHomeContentItem(
    @SerializedName("title")
    var title: String? = null,
    @SerializedName("body")
    var body: String? = null,
    @SerializedName("image")
    val image: String? = null,
) : Parcelable

@Keep
@Parcelize
data class StickyNotifyData(
    @SerializedName("title")
    var title: String? = null,
    @SerializedName("body")
    var body: String? = null,
    @SerializedName("cta")
    var cta: CTAData? = null,
    var syncProgressPercentage: Float = 0f,
    var syncStatus: String? = null,
    var recordsCount: Int = 0,
    var parcelableCTAData: CTAData? = null
) : Parcelable

@Keep
@Parcelize
data class GmailRecordsCategories(
    @SerializedName("tab_title")
    var tabTitle: String? = null,
    @SerializedName("tab_count")
    var tabCount: Int = 0,
    @SerializedName("description")
    var description: String? = null,
    @SerializedName("bgcolor")
    var bgcolor: String? = null,
    @SerializedName("docs")
    var docs: List<RecordsProfileInfo>? = null,
    @SerializedName("tab_count_color")
    var tabCountColor: String? = null
) : Parcelable

@Keep
@Parcelize
data class RecordsProfileInfo(
    @SerializedName("oid")
    var oid: String? = null,
    @SerializedName("name")
    var name: String? = null,
    @SerializedName("count")
    var count: Int = 0,
    @SerializedName("desc")
    var desc: String? = null,
    @SerializedName("is_current_profile")
    var isCurrentProfile: Boolean = false,
    @SerializedName("classified")
    var classified: Boolean? = null,
) : Parcelable

@Parcelize
@Keep
data class GmailShare(
    @SerializedName("message")
    var message: String? = null,
) : Parcelable

@Keep
@Parcelize
data class SyncedGmailAccount(
    @SerializedName("email")
    val email: String? = null,
    @SerializedName("sync_status")
    val syncStatus: String? = null,
    @SerializedName("sync_started_at")
    val syncStartedAt: Long? = null,
    @SerializedName("last_sync")
    val lastSync: Long? = null,
    @SerializedName("count")
    val recordsCount: Int = 0,
    @SerializedName("sync_progress_msg")
    val recordsCountProgressMsg: String? = null,
    @SerializedName("progress_msg")
    val progressStatusMsg: String? = null,
    @SerializedName("on_demand_timer")
    var onDemandSyncTimer: Int? = null,
    @SerializedName("last_backup")
    val lastBackup: String? = null,
    @SerializedName("categories")
    val gmailRecordsCategories: List<GmailRecordsCategories>? = null,
    var syncProgressPercentage: Float = 0f,
) : Parcelable
