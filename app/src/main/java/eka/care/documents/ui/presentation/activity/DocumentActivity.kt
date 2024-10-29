package eka.care.documents.ui.presentation.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import eka.care.documents.ui.presentation.model.RecordParamsModel
import eka.care.documents.ui.presentation.screens.DocumentScreen
import eka.care.documents.ui.presentation.viewmodel.RecordsViewModel

class DocumentActivity : ComponentActivity() {
    private val viewModel by viewModels<RecordsViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = Color.White.toArgb()

        setContent {
            DocumentScreen(
                params = RecordParamsModel(
                    patientId = intent.getStringExtra("pid") ?: "",
                    doctorId = intent.getStringExtra("doid") ?: "",
                    name =  intent.getStringExtra("name") ?: "",
                    uuid = intent.getStringExtra("p_uuid") ?: ""
                ),
                viewModel = viewModel
            )
        }
    }
}