package eka.care.records.client.utils

import androidx.annotation.Keep
import com.eka.network.IOkHttpSetup

@Keep
data class DocumentConfiguration(
    val okHttpSetup: IOkHttpSetup,
    val host: String,
    val vitalsEnabled : Boolean
)