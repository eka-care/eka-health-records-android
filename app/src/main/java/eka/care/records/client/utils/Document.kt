package eka.care.records.client.utils

import com.eka.networking.client.EkaNetwork
import com.eka.networking.client.NetworkConfig
import eka.care.records.client.model.EventLog
import eka.care.records.data.contract.LogInterceptor
import eka.care.records.data.remote.EnvironmentManager

object Document {
    private var configuration: DocumentConfiguration? = null
    private var logger: LogInterceptor? = null

    fun init(config: DocumentConfiguration) {
        configuration = config
        EnvironmentManager.setEnvironment(config.environment)
        try {
            EkaNetwork.init(
                networkConfig = NetworkConfig(
                    appId = config.appId,
                    baseUrl = config.baseUrl,
                    isDebugApp = config.isDebugApp,
                    headers = config.headers,
                    tokenStorage = config.tokenStorage,
                    appVersionName = config.appVersionName,
                    appVersionCode = config.appVersionCode
                )
            )
        } catch (_: Exception) {
            logger?.logEvent(
                EventLog(
                    message = "Failed to initialize Networking with provided configuration."
                )
            )
        }
    }

    fun getConfiguration(): DocumentConfiguration {
        if (configuration == null) {
            throw IllegalStateException("Configuration not initialized. Please call Document.init() first.")
        }
        return configuration!!
    }
}