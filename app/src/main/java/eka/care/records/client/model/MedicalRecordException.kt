package eka.care.records.client.model

sealed class MedicalRecordException : Exception() {
    object NetworkError : MedicalRecordException()
    data object DatabaseError : MedicalRecordException()
    data class ValidationError(val reason: String) : MedicalRecordException()
    data class AuthenticationError(val reason: String) : MedicalRecordException()
    data class SyncError(val recordId: String, val reason: String) : MedicalRecordException()
    data object FileStorageError : MedicalRecordException()
    data class UnexpectedError(val originalError: Throwable) : MedicalRecordException()
}