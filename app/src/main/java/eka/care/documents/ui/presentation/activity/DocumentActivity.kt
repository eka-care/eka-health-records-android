package eka.care.documents.ui.presentation.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import eka.care.documents.ui.presentation.model.RecordParamsModel
import eka.care.documents.ui.presentation.screens.DocumentScreen

class DocumentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = Color.White.toArgb()

        setContent {
            DocumentScreen(
                params = RecordParamsModel(
                    patientId = intent.getStringExtra(MedicalRecordParams.PATIENT_ID.key) ?: "",
                    doctorId = intent.getStringExtra(MedicalRecordParams.DOCTOR_ID.key) ?: "",
                    name = intent.getStringExtra(MedicalRecordParams.PATIENT_NAME.key),
                    uuid = intent.getStringExtra(MedicalRecordParams.PATIENT_UUID.key) ?: "",
                    age = intent.getIntExtra(MedicalRecordParams.PATIENT_AGE.key, -1),
                    gender = intent.getStringExtra(MedicalRecordParams.PATIENT_GENDER.key)
                )
            )
        }
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