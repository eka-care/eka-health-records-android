package eka.care.documents.network

import kotlinx.coroutines.runBlocking

class OkHttpSetup : IOkHttpSetup {
    override fun getDefaultHeaders(url: String): Map<String, String> {
        val headers = mapOf("auth" to "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1dWlkIjoiOTk0NWFiMDktYWQ4OS00MjU3LTg5MmMtYWZhZTAzMjk1M2RmIiwib2lkIjoiMTY2MzU2ODcwMjUwNTI3IiwiZm4iOiJEciBHcCBHZW5lcmFsIHBoeXNpY2lhbiIsImdlbiI6Ik0iLCJpcy1wIjp0cnVlLCJkb2IiOiIxOTg0LTExLTE5IiwibW9iIjoiKzkxMSoqKioqKjIxMiIsInR5cGUiOjIsImRvYy1pZCI6IjE2NjM1Njg3MDI1MDUyNyIsImlzcyI6ImEiLCJpcy1kIjp0cnVlLCJwIjoiUEFTU19fNDk5OSIsInBwIjp7ImMiOiIyIiwiZSI6IjE3MzkyOTg2MDAiLCJ0IjoiMCJ9LCJpYXQiOjE3MzIxMDYwNTcsImV4cCI6MTczMjEwOTY1N30.-90lJNSMm7N3MOLeQjD3i0tYAbtyLPt8S5ez1CIB4sw",
            "flavour" to "android", "version" to "223", "client-id" to "androiddoc");
        return headers
    }

    override fun refreshAuthToken(url: String): Map<String, String>? {
        return runBlocking {
            val sessionToken = refreshToken()
            if (sessionToken.isNullOrBlank()) {
                null
            } else {
                getDefaultHeaders(url)
            }
        }
    }

    override fun onSessionExpire() {
    }

    private suspend fun refreshToken(): String? {
        return ""
    }
}