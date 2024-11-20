package eka.care.documents.ui.presentation.activity

import android.app.Application
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import eka.care.documents.ui.presentation.model.RecordParamsModel
import eka.care.documents.ui.presentation.screens.DocumentScreen
import eka.care.documents.ui.presentation.viewmodel.RecordsViewModel

class DocumentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = Color.White.toArgb()

        setContent {
            DocumentScreen(
                params = RecordParamsModel(
                    patientId = "173106247826612",
                    doctorId = "166356870250527",
                    name = "Manish Kumar",
                    uuid = "274b6c26-fa2d-4bde-9cd3-0cdd7750edb2",
                    age = 0,
                    gender = "Male"
                )
            )
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