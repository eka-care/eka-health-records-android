package eka.care.records.data.remote.network

interface EkaAuthConfig {
    suspend fun refreshToken(): String
    fun sessionExpired()
}