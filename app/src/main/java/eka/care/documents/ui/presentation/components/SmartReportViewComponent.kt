package eka.care.documents.ui.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.reader.PdfReaderManager
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import eka.care.documents.data.db.model.CTA
import eka.care.documents.ui.BorderBrand02
import eka.care.documents.ui.DarwinTouchNeutral100
import eka.care.documents.ui.presentation.state.DocumentPreviewState
import eka.care.documents.ui.presentation.state.DocumentSmartReportState
import eka.care.documents.ui.presentation.viewmodel.DocumentPreviewViewModel
import eka.care.documents.ui.presentation.viewmodel.RecordsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class)
@Composable
fun SmartReportViewComponent(
    viewModel: DocumentPreviewViewModel,
    recordsViewModel: RecordsViewModel,
    docId: String,
    userId: String,
    password : String,
    documentDate: String,
    onClick: (CTA?) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pdfManager = PdfReaderManager(context)
    val pagerState = rememberPagerState(initialPage = SmartViewTab.SMARTREPORT.ordinal)
    initData(viewModel, docId, userId)

    val state by viewModel.documentSmart.collectAsState()
    val filePathState by viewModel.document.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        SmartReportTopBar(onClick = {
            onClick(it)
        }, documentDate)
        SmartReportTabBar(pagerState = pagerState, onTabSelect = {
            scope.launch {
                pagerState.animateScrollToPage(it)
                viewModel.updateSelectedTab(SmartViewTab.values()[it])
            }
        })
        HorizontalPager(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(DarwinTouchNeutral100),
            count = 2,
            state = pagerState,
            userScrollEnabled = true,
            verticalAlignment = Alignment.Top
        ) { index ->
            when (index) {
                SmartViewTab.SMARTREPORT.ordinal -> {
                    when (state) {
                        is DocumentSmartReportState.Error -> {
                            val errorMessage = (state as DocumentSmartReportState.Error).message
                            ErrorState(message = errorMessage)
                        }

                        DocumentSmartReportState.Loading -> {
                            LoadingState()
                        }

                        is DocumentSmartReportState.Success -> {
                            val resp = (state as? DocumentSmartReportState.Success)?.data
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(BorderBrand02),
                                verticalArrangement = Arrangement.Top,
                                horizontalAlignment = Alignment.Start
                            ) {
                                SmartRecordInfo(onClick = {
                                    scope.launch {
                                        pagerState.scrollToPage(SmartViewTab.ORIGINALRECORD.ordinal)
                                    }
                                })
                                SmartReportFilter(resp?.smartReport, viewModel = viewModel)
                                SmartReportList(viewModel = viewModel)
                            }
                        }
                    }
                }

                SmartViewTab.ORIGINALRECORD.ordinal -> {
                    when (filePathState) {
                        is DocumentPreviewState.Error -> {
                            val errorMessage = (filePathState as DocumentPreviewState.Error).message
                            ErrorState(message = errorMessage)
                        }

                        DocumentPreviewState.Loading -> LoadingState()
                        is DocumentPreviewState.Success -> {
                            DocumentSuccessState(
                                state = (filePathState as? DocumentPreviewState.Success),
                                pdfManager = pdfManager,
                                paddingValues = PaddingValues(),
                                recordsViewModel = recordsViewModel,
                                password = password
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun initData(viewModel: DocumentPreviewViewModel, docId: String, userId: String) {
    viewModel.getSmartReport(docId = docId, userId = userId)
    viewModel.getDocument(
        userId = userId ?: "",
        docId = docId ?: ""
    )
}

enum class SmartViewTab(val type: String) {
    SMARTREPORT("SmartReport"),
    ORIGINALRECORD("OriginalRecord")
}

enum class LabParamResult(val value: String) {
    CRITICALLY_LOW("sm-4067860500"),
    VERY_LOW("sm-2631771970"),
    LOW("sm-1220479757"),
    BORDERLINE_LOW("sm-5279274814"),
    NORMAL("sm-8146614980"),
    ABNORMAL("sm-5379306527"),
    BORDERLINE_HIGH("sm-5279215230"),
    HIGH("sm-1420480405"),
    VERY_HIGH("sm-2631712380"),
    CRITICALLY_HIGH("sm-4067205096"),
    NO_INTERPRETATION_DONE("sm-5612225938"),
}