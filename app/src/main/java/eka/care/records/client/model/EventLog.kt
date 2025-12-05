package eka.care.records.client.model

import androidx.annotation.Keep

@Keep
data class EventLog(
    val params: Map<String, Any?> = emptyMap(),
    val message: String? = null,
)