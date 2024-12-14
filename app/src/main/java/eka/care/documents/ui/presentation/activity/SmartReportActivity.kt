package eka.care.documents.ui.presentation.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import eka.care.documents.ui.presentation.components.SmartReportViewComponent
import eka.care.documents.ui.presentation.viewmodel.DocumentPreviewViewModel
import eka.care.documents.ui.presentation.viewmodel.RecordsViewModel

class SmartReportActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel: DocumentPreviewViewModel by viewModels()
        val recordsViewModel = RecordsViewModel(application)
        setContent {
            SmartReportViewComponent(
                viewModel = viewModel,
                recordsViewModel = recordsViewModel,
                docId = intent.getStringExtra("doc_id") ?: "",
                userId = intent.getStringExtra("user_id") ?: "",
                documentDate = intent.getStringExtra("doc_date") ?: "",
                password = intent.getStringExtra("password") ?: "",
                onClick = {
                    if (it?.action == "on_back_click") {
                        finish()
                    }
                }
            )
        }
    }
}