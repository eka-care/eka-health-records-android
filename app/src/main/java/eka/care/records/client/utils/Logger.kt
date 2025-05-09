package eka.care.records.client.utils

import android.util.Log
import eka.care.records.client.model.EventLog
import eka.care.records.data.contract.LogInterceptor

private const val TAG = "EkaRecords"

class Logger: LogInterceptor {

    var loggingEnabled = false

    override fun logEvent(eventLog: EventLog) {
        when (eventLog) {
            is EventLog.Error -> e(eventLog.message)
            is EventLog.Info -> i(eventLog.message)
            is EventLog.Warning -> w(eventLog.message)
        }
    }

    private fun e(message: String) {
        if (loggingEnabled && message.isNotEmpty()) {
            Log.e(TAG, message)
        }
    }

    private fun w(message: String) {
        if (loggingEnabled && message.isNotEmpty()) {
            Log.w(TAG, message)
        }
    }

    private fun i(message: String) {
        if (loggingEnabled && message.isNotEmpty()) {
            Log.i(TAG, message)
        }
    }
}