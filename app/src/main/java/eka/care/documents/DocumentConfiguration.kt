package eka.care.documents

import android.content.Context
import com.eka.network.IOkHttpSetup

data class DocumentConfiguration(
    val okHttpSetup: IOkHttpSetup,
    val host: String,
    val vitalsEnabled : Boolean
)

data class SmartReportClickData(
    val ekaId: String,
    val name: String?
)