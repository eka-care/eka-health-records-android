package eka.care.records.client.utils

import androidx.annotation.Keep
import com.eka.networking.token.TokenStorage
import eka.care.records.data.remote.Environment

@Keep
data class DocumentConfiguration(
    val appId: String,
    val baseUrl: String,
    val appVersionName: String,
    val appVersionCode: Int,
    val provider: String,
    val isDebugApp: Boolean = false,
    val apiCallTimeOutInSec: Long = 30L,
    val maxAvailableStorage: Long = 1L * 1024L * 1024L * 1024L,
    val enableSearch: Boolean = false,
    val headers: Map<String, String>,
    val tokenStorage: TokenStorage,
    val environment: Environment = Environment.PROD,
)