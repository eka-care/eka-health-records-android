package eka.care.documents.ui.presentation.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.reader.PdfReaderManager
import eka.care.documents.R
import eka.care.documents.ui.DarwinTouchNeutral0
import eka.care.documents.ui.DarwinTouchNeutral1000
import eka.care.documents.ui.DarwinTouchPrimaryBgLight
import eka.care.documents.ui.presentation.viewmodel.RecordsViewModel
import eka.care.documents.ui.touchLabelBold
import java.io.File

@Composable
fun CircularImageComponent(image: Int, modifier: Modifier, onClick: () -> Unit, action: String) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(50))
                .clickable {
                    onClick()
                }
                .background(DarwinTouchPrimaryBgLight)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = image),
                contentDescription = "",
                modifier = Modifier.size(32.dp)
            )
        }
        Text(
            text = action,
            style = touchLabelBold,
            color = DarwinTouchNeutral1000,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun PreviewComponent(
    pdfReaderManager: PdfReaderManager?,
    recordsViewModel: RecordsViewModel,
    onClick: () -> Unit,
    filePreviewList: List<File>,
    pdfUriString: String
) {
    Box(
        modifier = Modifier.background(DarwinTouchNeutral1000),
        contentAlignment = Alignment.BottomCenter
    ) {
        if (pdfUriString.isEmpty()) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(filePreviewList) { file ->
                    val bitmap = BitmapFactory.decodeFile(file.path)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = bitmap,
                            contentDescription = null,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                item {
                    Spacer(modifier = Modifier.padding(80.dp))
                }
            }
        } else {
            recordsViewModel.pdfSource?.let { source ->
                Column(verticalArrangement = Arrangement.Center) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        pdfReaderManager?.PdfViewer(pdfSource = source)
                    }
                    Spacer(modifier = Modifier.height(1000.dp))
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .background(DarwinTouchNeutral0)
                .wrapContentSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(48.dp)
        ) {
            CircularImageComponent(
                image = R.drawable.ic_files_selection_done_tick,
                modifier = Modifier,
                onClick = onClick,
                action = "Upload"
            )
        }
    }
}