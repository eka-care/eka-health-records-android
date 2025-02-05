package eka.care.documents.ui.presentation.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import eka.care.documents.ui.presentation.components.SmartReportViewComponent
import eka.care.documents.ui.presentation.viewmodel.DocumentPreviewViewModel

class SmartReportActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel: DocumentPreviewViewModel by viewModels()
        setContent {
            SmartReportViewComponent(
                viewModel = viewModel,
                docId = intent.getStringExtra("doc_id") ?: "",
                userId = intent.getStringExtra("user_id") ?: "",
                doctorId = intent.getStringExtra("doctor_id") ?: "",
                documentDate = intent.getStringExtra("doc_date") ?: "",
                localId = intent.getStringExtra("local_id") ?: "",
                onClick = {
                    if (it?.action == "on_back_click") {
                        finish()
                    }
                }
            )
        }
    }
}