package eka.care.records.client.model

import androidx.annotation.Keep
import org.json.JSONObject

@Keep
data class EventLog(
    val params: JSONObject = JSONObject(),
    val message: String? = null,
)