package eka.care.documents.ui.presentation.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import eka.care.documents.R
import eka.care.documents.ui.presentation.screens.DocumentSortEnum
import eka.care.documents.ui.presentation.viewmodel.RecordsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DocumentsHeader(
    onClickFilter: () -> Unit
) {
    Column(
        modifier = Modifier.animateContentSize(
            animationSpec = tween(300)
        )
    ) {
        DocumentSearch(
            modifier = Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp),
            onClickFilter = onClickFilter
        )
    }
}

@Composable
fun DocumentsSort(
    onClickSort: () -> Unit, viewModel: RecordsViewModel
) {
    val sortBy = viewModel.sortBy.value
    var documentViewType by remember { mutableStateOf(DocumentViewType.ListView) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(androidx.compose.material.MaterialTheme.colors.surface)
            .padding(start = 16.dp, end = 16.dp, top = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(modifier = Modifier
            .background(androidx.compose.material.MaterialTheme.colors.surface)
            .clickable {
                onClickSort()
            }
            .padding(6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start) {
            Text(
                text = if (sortBy == DocumentSortEnum.UPLOAD_DATE) stringResource(id = R.string.upload_date) else stringResource(
                    id = R.string.document_date
                ), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(8.dp))
            Image(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "",
                modifier = Modifier.rotate(90f),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
            )
        }
        IconButton(
            modifier = Modifier.size(40.dp),
            onClick = {
                documentViewType = if (documentViewType == DocumentViewType.GridView) {
                    DocumentViewType.ListView
                } else {
                    DocumentViewType.GridView
                }
                viewModel.documentViewType = documentViewType
            }
        ) {
            Image(
                painter = painterResource(
                    id = if (documentViewType == DocumentViewType.GridView) {
                        R.drawable.ic_grid_view
                    } else {
                        R.drawable.ic_list
                    }
                ),
                contentDescription = if (documentViewType == DocumentViewType.GridView) "list" else "grid",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

enum class DocumentBottomSheetType {
    DocumentUpload, DocumentOptions, DocumentSort, EnterFileDetails
}

enum class DocumentViewType {
    ListView, GridView
}

fun formatLocalDateToCustomFormat(date: Date): String? {
    val formatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    return formatter.format(date)
}

fun timestampToLong(timestamp: String, format: String = "EEE, dd MMM, yyyy"): Long {
    val dateFormat = SimpleDateFormat(format, Locale.getDefault())
    val date = dateFormat.parse(timestamp) ?: throw IllegalArgumentException("Invalid date format")
    return date.time / 1000
}
