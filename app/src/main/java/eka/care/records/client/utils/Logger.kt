package eka.care.records.client.utils

import android.util.Log
import eka.care.records.client.model.EventLog
import eka.care.records.data.contract.LogInterceptor

private const val TAG = "EkaRecords"

class Logger : LogInterceptor {

    var loggingEnabled = false

    override fun logEvent(eventLog: EventLog) {
        i(eventLog.toString())
    }

    private fun i(message: String) {
        if (loggingEnabled && message.isNotEmpty()) {
            Log.i(TAG, message)
        }
    }
}