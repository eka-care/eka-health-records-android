package eka.care.documents.ui.presentation.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.reader.PdfReaderManager
import com.example.reader.presentation.states.PdfSource
import eka.care.documents.ui.BgWhite
import eka.care.documents.ui.presentation.state.DocumentPreviewState
import eka.care.documents.ui.presentation.viewmodel.RecordsViewModel
import java.io.File

@Composable
fun LoadingState() {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun ErrorState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = message)
    }
}

@Composable
fun PdfPreview(uri: Uri, pdfManager: PdfReaderManager, paddingValues: PaddingValues) {
    Column(verticalArrangement = Arrangement.Center, modifier = Modifier.padding(paddingValues)) {
        Box(modifier = Modifier.fillMaxSize()) {
            pdfManager.PdfViewer(pdfSource = PdfSource.Uri(uri))
        }
        Spacer(modifier = Modifier.height(1000.dp))
    }
}

@Composable
fun ImagePreview(uri: Uri, modifier: Modifier) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset(0f, 0f)) }
    AsyncImage(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    // Update the scale based on zoom gestures.
                    scale *= zoom

                    // Limit the zoom levels within a certain range (optional).
                    scale = scale.coerceIn(0.5f, 3f)

                    // Update the offset to implement panning when zoomed.
                    offset = if (scale == 1f) Offset(0f, 0f) else offset + pan
                }
            }
            .graphicsLayer(
                scaleX = scale, scaleY = scale,
                translationX = offset.x, translationY = offset.y
            ),
        model = ImageRequest.Builder(LocalContext.current).data(uri).build(),
        contentScale = ContentScale.Fit,
        contentDescription = ""
    )
}

@Composable
fun DocumentSuccessState(
    state: DocumentPreviewState.Success?,
    paddingValues: PaddingValues,
    pdfManager: PdfReaderManager,
    recordsViewModel: RecordsViewModel,
    password: String
) {
    when (state?.data?.fileType) {
        "pdf" -> {
            val filePath = state.data.filePath.firstOrNull()
            if (filePath.isNullOrBlank()) return

            val file = if (state.data.isEncryptedFile == true) {
                val decryptedFilePath = recordsViewModel.decryptFile(
                    file = File(filePath),
                    password = "Unique.123"
                )
                File(decryptedFilePath)
            } else {
                File(filePath)
            }
            PdfPreview(
                paddingValues = paddingValues,
                uri = Uri.fromFile(file),
                pdfManager = pdfManager
            )
        }

        else -> {
            state?.data?.filePath?.let { filePaths ->
                DocumentImagePreview(
                    filePaths = filePaths,
                    isEncrypted = state.data.isEncryptedFile,
                    recordsViewModel = recordsViewModel
                )
            }
        }
    }
}

@Composable
fun DocumentImagePreview(
    filePaths: List<String>,
    isEncrypted: Boolean?,
    recordsViewModel: RecordsViewModel
) {
    var selectedUri by remember {
        mutableStateOf<Uri?>(
            Uri.parse(
                filePaths.firstOrNull()?.let { filePath ->
                    val file = if (isEncrypted == true) {
                        val decryptedFilePath = recordsViewModel.decryptFile(
                            file = File(filePath),
                            password = "Unique.123"
                        )
                        File(decryptedFilePath)
                    } else {
                        File(filePath)
                    }
                    Uri.fromFile(file).toString()
                }
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgWhite),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        selectedUri?.let {
            ImagePreview(
                uri = it,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(24.dp)
            )
        }

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(BgWhite)
                .padding(bottom = 48.dp, start = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(filePaths) { filePath ->
                val file = if (isEncrypted == true) {
                    val decryptedFilePath = recordsViewModel.decryptFile(
                        file = File(filePath),
                        password = "Unique.123"
                    )
                    File(decryptedFilePath)
                } else {
                    File(filePath)
                }
                val uri = Uri.fromFile(file)
                ImagePreview(
                    uri = uri,
                    modifier = Modifier
                        .size(48.dp)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        .clickable {
                            selectedUri = uri
                        }
                )
            }
        }
    }
}