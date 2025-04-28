package eka.care.records.data.contract

interface LogInterceptor {
    /**
     * Log the message with the given tag.
     * @param tag The tag to use for the log message.
     * @param message The message to log.
     */
    fun logInfo(tag: String, message: String)

    /**
     * Log the error with the given tag.
     * @param tag The tag to use for the log message.
     * @param error The error to log.
     */
    fun logError(tag: String, error: Throwable)

    /**
     * Log the debug message with the given tag.
     * @param tag The tag to use for the log message.
     * @param message The message to log.
     */
    fun logDebug(tag: String, message: String)
}