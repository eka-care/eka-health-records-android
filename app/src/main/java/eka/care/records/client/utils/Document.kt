package eka.care.records.client.utils

import android.content.Context
import eka.care.records.client.model.EventLog
import eka.care.records.data.contract.LogInterceptor
import eka.care.records.data.remote.network.Networking
import eka.care.records.data.remote.network.OkHttpSetup

object Document {
    private var configuration: DocumentConfiguration? = null
    private var logger: LogInterceptor? = null

    fun init(
        config: DocumentConfiguration,
        defaultHeaders: Map<String, String> = emptyMap(),
        context: Context,
    ) {
        configuration = config
        if (config.authorizationToken.isEmpty()) {
            throw IllegalStateException("Records SDK not initialized with authorization token")
        }
        if (config.ekaAuthConfig == null) {
            logger?.logEvent(
                EventLog(
                    message = "EkaAuthConfig is null. Please provide EkaAuthConfig for refreshing authentication!"
                )
            )
        }
        try {
            val okHttp = OkHttpSetup(
                authorizationToken = config.authorizationToken,
                defaultHeaders = defaultHeaders,
                ekaAuthConfig = config.ekaAuthConfig
            )
            Networking.init(
                baseUrl = "https://api.eka.care/mr/",
                okHttpSetup = okHttp
            )
        } catch (_: Exception) {
            logger?.logEvent(
                EventLog(
                    message = "Failed to initialize Networking with provided configuration."
                )
            )
        }
    }
}