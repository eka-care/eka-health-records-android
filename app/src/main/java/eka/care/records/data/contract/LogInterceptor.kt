package eka.care.records.data.contract

import eka.care.records.client.model.EventLog

interface LogInterceptor {
    /**
     * Log the event with the given event log.
     * @param eventLog The event log to log.
     */
    fun logEvent(eventLog: EventLog)
}