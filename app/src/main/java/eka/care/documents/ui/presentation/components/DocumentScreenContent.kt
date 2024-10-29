package eka.care.documents.ui.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.PullRefreshState
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import eka.care.documents.R
import eka.care.documents.ui.DarwinTouchNeutral1000
import eka.care.documents.ui.presentation.state.GetRecordsState
import eka.care.documents.ui.presentation.model.RecordParamsModel
import eka.care.documents.ui.presentation.screens.DocumentEmptyStateScreen
import eka.care.documents.ui.presentation.viewmodel.RecordsViewModel
import kotlinx.coroutines.Job

@Composable
@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
fun DocumentScreenContent(
    paddingValues : PaddingValues,
    pullRefreshState: PullRefreshState,
    recordsState: GetRecordsState,
    openSheet: () -> Job,
    viewModel: RecordsViewModel,
    listState: LazyListState,
    params: RecordParamsModel,
    isRefreshing: Boolean
) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
            .background(MaterialTheme.colorScheme.surface)
    ) {
        when (recordsState) {
            is GetRecordsState.Loading -> {
                DocumentShimmer()
            }

            is GetRecordsState.EmptyState -> {
                val resp =(recordsState as? GetRecordsState.Success)?.resp ?: emptyList()
                Column {
                    if (resp.isNullOrEmpty()) {
                        DocumentEmptyStateScreen(
                            openBottomSheet = {
                                openSheet()
                                viewModel.documentBottomSheetType =
                                    DocumentBottomSheetType.DocumentUpload
                            }
                        )
                    }
                }
            }

            is GetRecordsState.Error -> {

            }

            is GetRecordsState.Success -> {
                val resp = (recordsState as? GetRecordsState.Success)?.resp ?: emptyList()
                val showFilters = remember { mutableStateOf(false) }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(androidx.compose.material.MaterialTheme.colors.surface),
                    state = listState
                ) {
                    item {
                        DocumentsHeader(
                            onClickFilter = {
                                showFilters.value = !showFilters.value
                            }
                        )
                    }
                    stickyHeader {
                        AnimatedVisibility(
                            visible = showFilters.value,
                            enter = slideInVertically(
                                initialOffsetY = {
                                    it / 2
                                }
                            ),
                            exit = slideOutVertically(targetOffsetY = {
                                it / 6
                            })
                        ) {
                            Surface(
                                modifier = Modifier.fillParentMaxWidth(),
                                color = androidx.compose.material.MaterialTheme.colors.surface
                            ) {
                                DocumentFilter(
                                    viewModel = viewModel,
                                    onClick = {
                                        viewModel.getLocalRecords(
                                            oid = params.patientId,
                                            doctorId = params.doctorId,
                                            docType = it
                                        )
                                    }
                                )
                            }
                        }
                    }
                    item {
                        DocumentsSort(
                            viewModel = viewModel,
                            onClickSort = {
                                openSheet()
                                viewModel.documentBottomSheetType =
                                    DocumentBottomSheetType.DocumentSort
                            })
                    }
                    if (viewModel.documentViewType == DocumentViewType.GridView) {
                        item {
                            DocumentGrid(records = resp,
                                viewModel = viewModel,
                                onClick = { cta ->
                                    if(cta?.action == "open_deepThought"){

                                    }else{
                                        openSheet()
                                        viewModel.documentBottomSheetType =
                                            DocumentBottomSheetType.DocumentOptions
                                    }
                                })
                        }
                    } else {
                        items(resp) { model ->
                            DocumentList(
                                recordModel = model,
                                onClick = { cta ->
                                    if(cta?.action == "open_deepThought"){

                                    }else{
                                        viewModel.localId.value = model.localId ?: ""
                                        viewModel.cardClickData.value = model
                                        openSheet()
                                        viewModel.documentBottomSheetType =
                                            DocumentBottomSheetType.DocumentOptions
                                    }
                                }
                            )
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
                FloatingActionButton(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 20.dp, bottom = 20.dp),
                    onClick = {
                        openSheet()
                        viewModel.documentBottomSheetType =
                            DocumentBottomSheetType.DocumentUpload
                    },
                    shape = RoundedCornerShape(16.dp),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Row(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_plus_brand),
                            contentDescription = "",
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(id = R.string.upload),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        PullRefreshIndicator(
            modifier = Modifier.align(Alignment.TopCenter),
            refreshing = isRefreshing,
            state = pullRefreshState,
            contentColor = DarwinTouchNeutral1000,
            scale = true
        )
    }
}