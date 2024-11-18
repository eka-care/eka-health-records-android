package eka.care.documents.ui.presentation.activity

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.reader.PdfReaderManager
import eka.care.doctor.features.documents.features.drive.presentation.state.DocumentPreviewState
import eka.care.documents.R
import eka.care.documents.ui.presentation.components.DocumentSuccessState
import eka.care.documents.ui.presentation.components.ErrorState
import eka.care.documents.ui.presentation.components.LoadingState
import eka.care.documents.ui.presentation.components.TopAppBarSmall
import eka.care.documents.ui.presentation.viewmodel.DocumentPreviewViewModel

class DocumentPreview : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel: DocumentPreviewViewModel by viewModels()

        val pdfManager = PdfReaderManager(this)

        try {
            val docId = intent.getStringExtra("document_id")
            val userId = intent.getStringExtra("user_id")
            viewModel.getDocument(
                userId = userId ?: "",
                docId = docId ?: ""
            )
        } catch (_: Exception) { }

        setContent {
            val state by viewModel.document.collectAsState()
            Content(state, pdfManager)
        }
    }

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    private fun Content(
        state: DocumentPreviewState,
        pdfManager: PdfReaderManager
    ) {
        val context = LocalContext.current
        ModalBottomSheetLayout(sheetContent = {}) {
            Scaffold(
                topBar = {
                    TopAppBarSmall(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White),
                        title = "Document",
                        leading = R.drawable.ic_back_arrow,
                        onLeadingClick = { (context as? Activity)?.finish() }
                    )
                },
                content = {
                    when (state) {
                        is DocumentPreviewState.Loading -> LoadingState()
                        is DocumentPreviewState.Error -> ErrorState(state.message)
                        is DocumentPreviewState.Success -> {
                            DocumentSuccessState(
                                state = state,
                                paddingValues = it,
                                pdfManager = pdfManager,
                            )
                        }
                    }
                }
            )
        }
    }

}