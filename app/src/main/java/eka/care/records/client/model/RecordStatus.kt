package eka.care.records.client.model

enum class RecordStatus(val status: Int) {
    NONE(0),
    SYNCING(1),
    SYNC_FAILED(2),
    SYNC_SUCCESS(3)
}