package eka.care.documents.ui.presentation.screens

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import eka.care.documents.sync.workers.SyncFileWorker
import eka.care.documents.ui.presentation.viewmodel.RecordsViewModel

enum class Mode {
    VIEW, SELECTION
}
enum class MedicalRecordParams(val key: String) {
    FILTER_ID("filter_id"),
    OWNER_ID("owner_id"),
    PATIENT_UUID("p_uuid"),
    PATIENT_NAME("name"),
    PATIENT_GENDER("gen"),
    PATIENT_AGE("age"),
    LINKS("links")
}

fun initData(
    patientUuid: String,
    filterIds: List<String>,
    ownerId: String,
    context: Context,
    onSuccess : () -> Unit
) {
    val inputData = Data.Builder()
        .putString("p_uuid", patientUuid)
        .putString("ownerId", ownerId)
        .putString("filterIds", filterIds.joinToString(","))
        .build()

    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    val uniqueWorkName = "syncFileWorker_${patientUuid}_$filterIds$ownerId"
    val uniqueSyncWorkRequest =
        OneTimeWorkRequestBuilder<SyncFileWorker>()
            .setInputData(inputData)
            .setConstraints(constraints)
            .build()

    WorkManager.getInstance(context)
        .enqueueUniqueWork(
            uniqueWorkName,
            ExistingWorkPolicy.KEEP,
            uniqueSyncWorkRequest
        )
}