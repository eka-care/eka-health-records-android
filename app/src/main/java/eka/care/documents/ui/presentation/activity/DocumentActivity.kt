package eka.care.documents.ui.presentation.activity

import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import eka.care.documents.ui.presentation.model.RecordParamsModel
import eka.care.documents.ui.presentation.screens.DocumentScreen
import eka.care.documents.ui.presentation.screens.initData
import eka.care.documents.ui.presentation.viewmodel.RecordsViewModel

class DocumentActivity : AppCompatActivity() {
    private lateinit var viewModel: RecordsViewModel
    private lateinit var params: RecordParamsModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = Color.White.toArgb()

        params =  RecordParamsModel(
            patientId = intent.getStringExtra(MedicalRecordParams.PATIENT_ID.key) ?: "",
            doctorId = intent.getStringExtra(MedicalRecordParams.DOCTOR_ID.key) ?: "",
            name = intent.getStringExtra(MedicalRecordParams.PATIENT_NAME.key),
            uuid = intent.getStringExtra(MedicalRecordParams.PATIENT_UUID.key) ?: "",
            age = intent.getIntExtra(MedicalRecordParams.PATIENT_AGE.key, -1),
            gender = intent.getStringExtra(MedicalRecordParams.PATIENT_GENDER.key)
        )

        if (params.patientId.isEmpty()) {
            Log.e("DocumentActivity", "Patient ID is missing!")
            return
        }

        val application = applicationContext as Application
        viewModel = ViewModelProvider(
            this,
            RecordsViewModelFactory(application)
        ).get(RecordsViewModel::class.java)

        setContent {
            val context = this@DocumentActivity
            Log.d("AYUSHI-3", "called")
            initData(
                oid = params.patientId,
                doctorId = params.doctorId,
                viewModel = viewModel,
                context = context,
                patientUuid = params.uuid,
                syncDoc = false
            )
            DocumentScreen(params = params, viewModel = viewModel)
        }
    }
}

class RecordsViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecordsViewModel::class.java)) {
            return RecordsViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

enum class MedicalRecordParams(val key: String) {
    PATIENT_ID("pid"),
    DOCTOR_ID("doid"),
    PATIENT_UUID("p_uuid"),
    PATIENT_NAME("name"),
    PATIENT_GENDER("gen"),
    PATIENT_AGE("age")
}