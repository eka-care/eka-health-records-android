package eka.care.records.client.utils

import com.eka.network.IOkHttpSetup

data class DocumentConfiguration(
    val okHttpSetup: IOkHttpSetup,
    val host: String,
    val vitalsEnabled : Boolean
)