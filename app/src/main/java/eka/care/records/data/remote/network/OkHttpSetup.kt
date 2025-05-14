package eka.care.records.data.remote.network

import kotlinx.coroutines.runBlocking

class OkHttpSetup(
    val ekaAuthConfig: EkaAuthConfig?,
    val defaultHeaders: Map<String, String> = HashMap(),
    var authorizationToken: String,
) : IOkHttpSetup {
    override fun getDefaultHeaders(url: String): Map<String, String> {
        val headers = HashMap<String, String>()
        headers.putAll(defaultHeaders)
        headers["auth"] = authorizationToken
        return headers
    }

    override fun onSessionExpire() {
        ekaAuthConfig?.sessionExpired()
    }

    override fun refreshAuthToken(url: String): Map<String, String>? {
        return runBlocking {
            val sessionToken = ekaAuthConfig?.refreshToken()
            authorizationToken = sessionToken ?: authorizationToken
            if (sessionToken.isNullOrBlank()) {
                null
            } else {
                getDefaultHeaders(url)
            }
        }
    }
}