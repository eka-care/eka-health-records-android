package eka.care.records.data.utility

import android.content.Context
import android.content.SharedPreferences
import java.util.concurrent.atomic.AtomicLong

object TimeProvider {

    private const val PREF_NAME = "eka_time_provider"
    private const val KEY_OFFSET = "server_time_offset"

    private val offset = AtomicLong(0L)
    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        prefs = context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        offset.set(prefs?.getLong(KEY_OFFSET, 0L) ?: 0L)
    }

    // Call this with the server's Unix epoch millis whenever you get a server response.
    // Persists the offset so it survives app restarts.
    fun updateFromServerTime(serverTimeMillis: Long) {
        val newOffset = serverTimeMillis - System.currentTimeMillis()
        offset.set(newOffset)
        prefs?.edit()?.putLong(KEY_OFFSET, newOffset)?.apply()
    }

    fun nowMillis(): Long = System.currentTimeMillis() + offset.get()

    fun nowSeconds(): Long = nowMillis() / 1000
}
