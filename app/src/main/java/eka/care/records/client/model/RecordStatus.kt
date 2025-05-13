package eka.care.records.client.model

import androidx.annotation.Keep

@Keep
enum class RecordStatus(val status: Int) {
    NONE(0),
    WAITING_TO_UPLOAD(1),
    WAITING_FOR_NETWORK(2),
    SYNCING(3),
    SYNC_FAILED(4),
    SYNC_SUCCESS(5)
}