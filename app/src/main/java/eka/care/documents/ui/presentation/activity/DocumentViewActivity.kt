package eka.care.documents.ui.presentation.activity

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.util.Log
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.reader.PdfReaderManager
import eka.care.documents.R
import eka.care.documents.ui.BgWhite
import eka.care.documents.ui.presentation.components.DocumentSuccessState
import eka.care.documents.ui.presentation.components.ErrorState
import eka.care.documents.ui.presentation.components.LoadingState
import eka.care.documents.ui.presentation.components.TopAppBarSmall
import eka.care.documents.ui.presentation.components.handleFileDownload
import eka.care.documents.ui.presentation.state.DocumentPreviewState
import eka.care.documents.ui.presentation.viewmodel.DocumentPreviewViewModel

class DocumentViewActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel: DocumentPreviewViewModel by viewModels()
        val pdfManager = PdfReaderManager(this)

        try {
            val localId = intent.getStringExtra("local_id")
            val docId = intent.getStringExtra("doc_id")
            val userId = intent.getStringExtra("user_id")
            viewModel.getDocument(
                userId = userId ?: "",
                docId = docId ?: "",
                localId = localId ?: ""
            )
        } catch (_: Exception) {
        }

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
        var selectedUri by remember { mutableStateOf<Uri?>(null) }

        ModalBottomSheetLayout(sheetContent = {}) {
            Scaffold(
                topBar = {
                    TopAppBarSmall(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BgWhite),
                        title = "Document",
                        leading = R.drawable.ic_back_arrow,
                        onLeadingClick = { (context as? Activity)?.finish() },
                        trailingIcon1 = R.drawable.ic_download_regular,
                        onTrailingIcon1Click = {
                            handleFileDownload(
                                state = state,
                                context = context,
                                selectedUri = selectedUri
                            )
                        }
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
                                onUriSelected = { uri -> selectedUri = uri }
                            )
                        }
                    }
                }
            )
        }
    }
}