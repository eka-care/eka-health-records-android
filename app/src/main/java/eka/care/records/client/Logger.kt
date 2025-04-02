package eka.care.records.client

import android.util.Log

private const val TAG = "EkaRecords"

object Logger {

    @JvmStatic
    var loggingEnabled = false

    @JvmStatic
    fun e(message: String) {
        if (loggingEnabled && message.isNotEmpty()) {
            Log.e(TAG, message)
        }
    }

    @JvmStatic
    fun w(message: String) {
        if (loggingEnabled && message.isNotEmpty()) {
            Log.w(TAG, message)
        }
    }

    @JvmStatic
    fun i(message: String) {
        if (loggingEnabled && message.isNotEmpty()) {
            Log.i(TAG, message)
        }
    }

    @JvmStatic
    fun d(message: String?) {
        if (loggingEnabled && message?.isNotEmpty() == true) {
            Log.d(TAG, message)
        }
    }

    @JvmStatic
    fun v(message: String) {
        if (loggingEnabled && message.isNotEmpty()) {
            Log.v(TAG, message)
        }
    }
}