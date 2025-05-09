package eka.care.records.client.model

sealed class EventLog {
    data class Error(
        val message: String,
        val code: EventCode?
    ) : EventLog()

    data class Info(
        val message: String,
        val code: EventCode? = null
    ) : EventLog()

    data class Warning(
        val message: String,
        val warningCode: Int = 0
    ) : EventLog()
}

sealed class EventCode {
    data object Sync : EventCode()
    data object Upload : EventCode()
    data object Read : EventCode()
    data class Error(val code: Int? = null) : EventCode()
}