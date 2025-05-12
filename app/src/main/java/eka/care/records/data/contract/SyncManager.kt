package eka.care.records.data.contract

interface SyncManager {
    /**
     * Synchronize the local dirty records with the server.
     * @return A [Result] containing the success or failure of the operation.
     */
    suspend fun syncDirtyRecords(): Result<Unit>

    /**
     * Synchronize the local deleted records with the server.
     * @return A [Result] containing the success or failure of the operation.
     */
    suspend fun syncDeletedRecords(): Result<Unit>

    /**
     * Synchronize the local records with the server.
     * @return A [Result] containing the success or failure of the operation.
     */
    suspend fun forceSyncAll(): Result<Unit>
}