package eka.care.records.client.utils

import androidx.annotation.Keep
import com.eka.networking.token.TokenStorage

@Keep
data class DocumentConfiguration(
    val appId: String,
    val baseUrl: String,
    val appVersionName: String,
    val appVersionCode: Int,
    val isDebugApp: Boolean = false,
    val apiCallTimeOutInSec: Long = 30L,
    val headers: Map<String, String>,
    val tokenStorage: TokenStorage
)