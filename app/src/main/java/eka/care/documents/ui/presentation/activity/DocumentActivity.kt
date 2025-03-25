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
import com.google.gson.Gson
import com.google.gson.JsonObject
import eka.care.documents.ui.presentation.components.initData
import eka.care.documents.ui.presentation.screens.DocumentScreen
import eka.care.documents.ui.presentation.screens.Mode
import eka.care.documents.ui.presentation.viewmodel.RecordsViewModel

class DocumentActivity : AppCompatActivity() {
    private lateinit var viewModel: RecordsViewModel
    private lateinit var params: JsonObject

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = Color.White.toArgb()

        val jsonString = intent.getStringExtra("params")
        if (jsonString.isNullOrEmpty()) {
            Log.e("DocumentActivity", "Params JSON is missing!")
            return
        }

        params = Gson().fromJson(jsonString, JsonObject::class.java)

        if (!params.has(MedicalRecordParams.FILTER_ID.key)) {
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
            initData(
                ownerId = params[MedicalRecordParams.OWNER_ID.key]?.asString ?: "",
                context = context,
                patientUuid = params[MedicalRecordParams.PATIENT_UUID.key]?.asString ?: "",
                filterIds = listOf()
            )
            DocumentScreen(
                param = params, onBackClick = {

                },
                mode = Mode.VIEW,
                isUploadEnabled = false
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
    FILTER_ID("filter_id"),
    OWNER_ID("owner_id"),
    PATIENT_UUID("p_uuid"),
    PATIENT_NAME("name"),
    PATIENT_GENDER("gen"),
    PATIENT_AGE("age"),
    LINKS("links")
}