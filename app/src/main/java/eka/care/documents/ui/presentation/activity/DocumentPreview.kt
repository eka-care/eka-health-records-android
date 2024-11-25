package eka.care.documents.ui.presentation.activity

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.reader.PdfReaderManager
import eka.care.documents.ui.presentation.state.DocumentPreviewState
import eka.care.documents.R
import eka.care.documents.ui.presentation.components.DocumentSuccessState
import eka.care.documents.ui.presentation.components.ErrorState
import eka.care.documents.ui.presentation.components.LoadingState
import eka.care.documents.ui.presentation.components.TopAppBarSmall
import eka.care.documents.ui.presentation.state.DocumentState
import eka.care.documents.ui.presentation.viewmodel.DocumentPreviewViewModel

class DocumentPreview : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel: DocumentPreviewViewModel by viewModels()

        val pdfManager = PdfReaderManager(this)

        try {
            val localId = intent.getStringExtra("local_id")
            val oid = intent.getStringExtra("user_id")
            viewModel.getDocument(
                oid = oid ?: "",
                localId = localId ?: ""
            )
        } catch (_: Exception) {
        }

        setContent {
            val documentState by viewModel.documentState.collectAsState()

            when (documentState) {
                is DocumentState.Loading -> {
                    // Show loading indicator
                    CircularProgressIndicator()
                }
                is DocumentState.Success -> {
                    val data = documentState as DocumentState.Success
                    Content(state = Pair(data.filePath, data.fileType ?: ""), pdfManager = pdfManager)
                }
                is DocumentState.Error -> {
                    val error = documentState as DocumentState.Error
                    Text("Error: ${error.message}")
                }
            }
        }
    }

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    private fun Content(
        state: Pair<List<String>?, String>,
        pdfManager: PdfReaderManager
    ) {
        if (state.first != null && state.second != null) {
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
                        DocumentSuccessState(
                            state = state,
                            paddingValues = it,
                            pdfManager = pdfManager,
                        )
                    }
                )
            }
        } else {
            Text("No document to display")
        }
    }
}