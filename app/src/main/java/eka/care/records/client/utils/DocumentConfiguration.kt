package eka.care.records.client.utils

import androidx.annotation.Keep
import eka.care.records.data.remote.network.EkaAuthConfig

@Keep
data class DocumentConfiguration(
    val authorizationToken: String,
    val ekaAuthConfig: EkaAuthConfig? = null,
)