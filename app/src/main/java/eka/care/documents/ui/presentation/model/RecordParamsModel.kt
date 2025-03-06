package eka.care.documents.ui.presentation.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class RecordParamsModel(
    val filterId : String,
    val ownerId : String,
    val name : String?,
    val uuid : String,
    val gender : String?,
    val age : Int?,
    val links : String?
):Parcelable