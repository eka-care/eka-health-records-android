package eka.care.documents

import com.eka.network.IOkHttpSetup

data class DocumentConfiguration(
    val okHttpSetup: IOkHttpSetup,
    val host: String
)
