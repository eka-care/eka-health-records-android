package eka.care.documents.ui.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import eka.care.documents.R
import eka.care.documents.data.utility.DocumentUtility.Companion.docTypes
import eka.care.documents.ui.DarwinTouchNeutral300
import eka.care.documents.ui.touchBodyBold
import eka.care.documents.ui.touchLabelRegular
import eka.care.documents.ui.presentation.model.CTA
import eka.care.documents.ui.presentation.model.RecordModel
import eka.care.documents.ui.presentation.viewmodel.RecordsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DocumentGrid(
    records: List<RecordModel>,
    viewModel: RecordsViewModel,
    onClick: (CTA?) -> Unit
) {
    val screenHeight = LocalConfiguration.current.screenHeightDp
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(screenHeight.dp)
    ) {
        items(records) { recordModel ->
            viewModel.localId.value = recordModel.localId ?: ""
            viewModel.cardClickData.value = recordModel
            DocumentGridItem(recordModel = recordModel, onClick = onClick)
        }
    }
}

@Composable
fun DocumentGridItem(
    recordModel: RecordModel,
    onClick: (CTA?) -> Unit
) {
    val docType = docTypes.find { it.idNew == recordModel.documentType }
    val uploadDate = Date((recordModel.createdAt ?: 0L) * 1000)
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val formattedDate = sdf.format(uploadDate)
    Column(
        modifier = Modifier
            .clickable {
                onClick(CTA(action = "open_deepThought"))
            }
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .background(MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium)
            .height(120.dp)
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Image(
                painter = painterResource(
                    id = docType?.icon ?: R.drawable.ic_others_new
                ),
                contentDescription = ""
            )
            Spacer(modifier = Modifier.width(4.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = docType?.documentType.toString(),
                    style = touchBodyBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.width(100.dp)
                )
                Text(
                    text = formattedDate,
                    style = touchLabelRegular,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                modifier = Modifier.clickable {
                    onClick(CTA(action = "open_options"))
                },
                imageVector = Icons.Rounded.MoreVert,
                contentDescription = ""
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        AsyncImage(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(color = DarwinTouchNeutral300),
            model = recordModel.thumbnail,
            contentDescription = "",
            contentScale = ContentScale.FillWidth,
        )
    }
}