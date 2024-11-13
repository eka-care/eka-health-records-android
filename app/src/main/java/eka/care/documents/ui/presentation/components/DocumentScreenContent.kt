package eka.care.documents.ui.presentation.components

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import eka.care.documents.R
import eka.care.documents.ui.DarwinTouchNeutral0
import eka.care.documents.ui.DarwinTouchNeutral1000
import eka.care.documents.ui.presentation.model.RecordModel
import eka.care.documents.ui.presentation.model.RecordParamsModel
import eka.care.documents.ui.presentation.screens.DocumentEmptyStateScreen
import eka.care.documents.ui.presentation.state.GetRecordsState
import eka.care.documents.ui.presentation.viewmodel.RecordsViewModel
import kotlinx.coroutines.Job
import org.json.JSONObject

@Composable
@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
fun DocumentScreenContent(
    paddingValues: PaddingValues,
    pullRefreshState: PullRefreshState,
    recordsState: GetRecordsState,
    openSheet: () -> Job,
    viewModel: RecordsViewModel,
    listState: LazyListState,
    isRefreshing: Boolean,
    paramsModel: RecordParamsModel
) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .pullRefresh(pullRefreshState)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
    ) {
        when (recordsState) {
            is GetRecordsState.Loading -> {
                DocumentShimmer()
            }

            is GetRecordsState.EmptyState -> {
                DocumentEmptyStateScreen(
                    openBottomSheet = {
                        openSheet()
                        viewModel.documentBottomSheetType =
                            DocumentBottomSheetType.DocumentUpload
                    }
                )
            }

            is GetRecordsState.Error -> {

            }

            is GetRecordsState.Success -> {
                val resp = (recordsState as? GetRecordsState.Success)?.resp ?: emptyList()
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarwinTouchNeutral0),
                    state = listState
                ) {
                    stickyHeader {
                        DocumentFilter(
                            viewModel = viewModel,
                            onClick = {
                                viewModel.getLocalRecords(
                                    oid = paramsModel.patientId,
                                    doctorId = paramsModel.doctorId,
                                    docType = it
                                )
                            }
                        )
                    }
                    stickyHeader {
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
                            DocumentGrid(
                                records = resp,
                                viewModel = viewModel,
                                onClick = { cta, model ->
                                    viewModel.cardClickData.value = model
                                    if (cta?.action == "open_deepThought") {
                                        navigate(
                                            context = context,
                                            model = model,
                                            oid = paramsModel.patientId
                                        )
                                    } else {
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
                                    viewModel.cardClickData.value = model
                                    if (cta?.action == "open_deepThought") {
                                        navigate(
                                            context = context,
                                            model = model,
                                            oid = paramsModel.patientId
                                        )
                                    } else {
                                        viewModel.localId.value = model.localId ?: ""
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

private fun navigate(context: Context, model: RecordModel, oid: String) {
    if (isOnline(context)) {
        if (model.tags?.split(",")?.contains("1") == false) {
            val params = JSONObject()
            params.put("doc_id", model.documentId)
            params.put("user_id", oid)
//            (context.applicationContext as IAmCommon).navigateTo(
//                context as Activity,
//                "doc_preview",
//                params
//            )
            return
        }
        val requestParams = RequestParams(
            documentId = model.documentId,
            userId = oid
        )
        val requestParamsJson = Gson().toJson(requestParams)
        val params = JSONObject()
        params.put("page_type", "vitals_page")
        params.put("context", requestParamsJson)

//        (context.applicationContext as IAmCommon).navigateTo(
//            context as Activity,
//            "deepthought_page",
//            params
//        )
    } else {
//        Intent(context, DocumentPreview::class.java)
//            .also {
//                it.putExtra("document_model", model)
//                it.putExtra("user_id", oid)
//                context.startActivity(it)
//            }
        return
    }
}


fun isOnline(context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val capabilities =
        connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
    if (capabilities != null) {
        if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
            return true
        } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            return true
        } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
            return true
        }
    }
    return false
}

data class RequestParams(
    @SerializedName("document_id") val documentId: String?,
    @SerializedName("user_id") val userId: String?
)