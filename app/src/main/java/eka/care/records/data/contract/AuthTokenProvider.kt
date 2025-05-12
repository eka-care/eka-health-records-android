package eka.care.records.data.contract

interface AuthTokenProvider {
    /**
     * @return The auth token.
     */
    fun getAuthToken(): String
}