package eka.care.records.client.model

import org.json.JSONObject

data class EventLog(
    val params: JSONObject = JSONObject(),
    val message: String? = null,
)