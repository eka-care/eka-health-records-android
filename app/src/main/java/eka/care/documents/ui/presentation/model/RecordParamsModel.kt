package eka.care.documents.ui.presentation.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class RecordParamsModel(
    val patientId : String,
    val doctorId : String,
    val name : String?,
    val uuid : String,
    val gender : String?,
    val age : Int?,
    val isFromSecretLocker : Boolean? = false,
    val password : String? = ""
):Parcelable