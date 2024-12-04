package eka.care.documents.ui.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import eka.care.documents.R
import eka.care.documents.data.utility.DocumentUtility.Companion.docTypes
import eka.care.documents.ui.DarwinTouchNeutral1000
import eka.care.documents.ui.DarwinTouchNeutral300
import eka.care.documents.ui.presentation.model.CTA
import eka.care.documents.ui.presentation.model.RecordModel
import eka.care.documents.ui.presentation.viewmodel.RecordsViewModel
import eka.care.documents.ui.touchLabelBold
import eka.care.documents.ui.touchLabelRegular
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DocumentGrid(
    records: List<RecordModel>, viewModel: RecordsViewModel, onClick: (CTA?, RecordModel) -> Unit
) {
    val screenHeight = LocalConfiguration.current.screenHeightDp
    val itemHeightDp = 160 // Set this to the approximate height of each grid item
    val gridHeight = remember(records.size) {
        val rows = (records.size + 1) / 2 // Calculate rows needed for the grid
        (rows * itemHeightDp).coerceAtMost(screenHeight) // Cap the height at screen height
    }
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(gridHeight.dp)
    ) {
        items(records) { recordModel ->
            viewModel.localId.value = recordModel.localId ?: ""
            DocumentGridItem(recordModel = recordModel, onClick = onClick, viewModel)
        }
    }
}

@Composable
fun DocumentGridItem(
    recordModel: RecordModel, onClick: (CTA?, RecordModel) -> Unit, viewModel: RecordsViewModel
) {
    val docType = docTypes.find { it.idNew == recordModel.documentType }
    val uploadTimestamp = recordModel.documentDate
    val uploadDate = uploadTimestamp?.times(1000)?.let { Date(it) }
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val formattedDate = uploadDate?.let { sdf.format(it) }
    Column(modifier = Modifier
        .padding(horizontal = 12.dp, vertical = 4.dp)
        .clickable {
            onClick(CTA(action = "open_deepThought"), recordModel)
        }
        .background(MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium)
        .height(120.dp)
        .padding(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Image(
                painter = painterResource(
                    id = docType?.icon ?: R.drawable.ic_others_new
                ), contentDescription = ""
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = docType?.documentType.toString(),
                    style = touchLabelBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.width(100.dp)
                )
                if (formattedDate != null) {
                    Text(
                        text = formattedDate,
                        style = touchLabelRegular,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(
                modifier = Modifier.clickable {
                    viewModel.localId.value = recordModel.localId ?: ""
                    viewModel.cardClickData.value = recordModel
                    onClick(CTA(action = "open_options"), recordModel)
                }, imageVector = Icons.Rounded.MoreVert, contentDescription = ""
            )
        }
        if (formattedDate == null) {
            Spacer(modifier = Modifier.height(8.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.BottomEnd) {
            AsyncImage(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color = DarwinTouchNeutral1000)
                    .graphicsLayer(alpha = 0.4f),
                model = recordModel.thumbnail,
                contentDescription = "",
                contentScale = ContentScale.FillWidth,
            )
            if (recordModel.tags?.split(",")?.contains("1") == true) {
                SmartChip()
            }
//            if(recordModel.isAnalyzing){
//                AnalysingChip()
//            }
        }
    }
}

@Composable
fun SmartChip() {
    Row(
        modifier = Modifier
            .width(70.dp)
            .height(30.dp)
            .padding(end = 4.dp, bottom = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(8.dp)
            )
            .background(color = MaterialTheme.colorScheme.surfaceVariant)
            .padding(start = 4.dp, end = 6.dp, top = 2.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Image(painter = painterResource(id = R.drawable.ic_smart_star), contentDescription = "")
        Spacer(modifier = Modifier.width(2.dp))
        Text(
            text = "Smart", style = touchLabelBold, color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun AnalysingChip() {
    val strokeWidth = 2.dp
    Row(
        modifier = Modifier
            .width(100.dp)
            .height(30.dp)
            .padding(end = 4.dp, bottom = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(8.dp)
            )
            .background(color = MaterialTheme.colorScheme.surfaceVariant)
            .padding(start = 4.dp, end = 6.dp, top = 2.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(16.dp), color = Color.LightGray, strokeWidth = strokeWidth
        )
        Spacer(modifier = Modifier.width(2.dp))
        Text(
            text = "Generating...",
            style = touchLabelBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
