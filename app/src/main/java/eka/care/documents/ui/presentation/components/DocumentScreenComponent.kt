package eka.care.documents.ui.presentation.components

import android.content.Context
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import eka.care.documents.R
import eka.care.documents.sync.workers.SyncFileWorker
import eka.care.documents.ui.DarwinTouchNeutral1000
import eka.care.documents.ui.presentation.screens.DocumentSortEnum
import eka.care.documents.ui.presentation.viewmodel.RecordsViewModel
import eka.care.documents.ui.touchLabelBold

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
fun DocumentStatus() {
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .background(
                DarwinTouchNeutral1000
            )
            .padding(horizontal = 16.dp)
    ) {

    }
}

fun initData(
    patientUuid: String,
    filterIds: List<String>,
    ownerId: String,
    context: Context,
) {
    val inputData = Data.Builder()
        .putString("p_uuid", patientUuid)
        .putString("ownerId", ownerId)
        .putString("filterIds", filterIds.joinToString(","))
        .build()

    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    val uniqueWorkName = "syncFileWorker_${patientUuid}_$filterIds$ownerId"
    val uniqueSyncWorkRequest =
        OneTimeWorkRequestBuilder<SyncFileWorker>()
            .setInputData(inputData)
            .setConstraints(constraints)
            .build()

    WorkManager.getInstance(context)
        .enqueueUniqueWork(
            uniqueWorkName,
            ExistingWorkPolicy.KEEP,
            uniqueSyncWorkRequest
        )
}

@Composable
fun DocumentsSort(
    onClickSort: () -> Unit, viewModel: RecordsViewModel
) {
    val sortBy = viewModel.sortBy.value
    val documentViewType = viewModel.documentViewType
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(androidx.compose.material.MaterialTheme.colors.surface)
            .padding(start = 16.dp, end = 16.dp),
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
                ), style = touchLabelBold, color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(8.dp))
            Image(
                painter = painterResource(id = R.drawable.ic_back_arrow),
                contentDescription = "",
                modifier = Modifier.rotate(90f),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
            )
        }
        IconButton(
            modifier = Modifier.size(40.dp),
            onClick = {
                viewModel.documentViewType = if (documentViewType == DocumentViewType.GridView) {
                    DocumentViewType.ListView
                } else {
                    DocumentViewType.GridView
                }
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