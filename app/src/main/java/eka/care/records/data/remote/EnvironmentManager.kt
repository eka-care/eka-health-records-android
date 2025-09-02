package eka.care.records.data.remote

object EnvironmentManager {
    private var currentEnvironment: Environment = Environment.PROD

    fun setEnvironment(env: Environment) {
        currentEnvironment = env
    }

    fun getBaseUrl(): String {
        return when (currentEnvironment) {
            Environment.STAGING -> "https://api.dev.eka.care"
            Environment.PROD -> "https://api.eka.care"
        }
    }
}

enum class Environment {
    STAGING,
    PROD
}