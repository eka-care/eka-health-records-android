package eka.care.documents.ui.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import eka.care.documents.R
import eka.care.documents.data.db.model.CTA
import eka.care.documents.sync.data.remote.dto.response.SmartReport
import eka.care.documents.sync.data.remote.dto.response.SmartReportField
import eka.care.documents.ui.BgWarning03
import eka.care.documents.ui.Border03
import eka.care.documents.ui.DarwinTouchNeutral0
import eka.care.documents.ui.Gray800
import eka.care.documents.ui.Icon01
import eka.care.documents.ui.Icon03
import eka.care.documents.ui.Purple50
import eka.care.documents.ui.Text01
import eka.care.documents.ui.Text03
import eka.care.documents.ui.Text04
import eka.care.documents.ui.TextBrand
import eka.care.documents.ui.TextError
import eka.care.documents.ui.TextSuccess
import eka.care.documents.ui.Yellow600
import eka.care.documents.ui.presentation.viewmodel.DocumentPreviewViewModel
import eka.care.documents.ui.touchBodyBold
import eka.care.documents.ui.touchCalloutBold
import eka.care.documents.ui.touchLabelBold
import eka.care.documents.ui.touchLabelRegular

@Composable
fun SmartRecordInfo(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .clickable {
                onClick()
            }
            .background(BgWarning03)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_info),
            contentDescription = "",
            colorFilter = ColorFilter.tint(Yellow600)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(id = R.string.your_original_document_is_more_reliable),
            style = touchLabelBold,
            color = Text03,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Image(
            painter = painterResource(id = R.drawable.ic_right),
            contentDescription = "",
            colorFilter = ColorFilter.tint(Icon03)
        )
    }
}

@Composable
fun SmartReportList(viewModel: DocumentPreviewViewModel) {
    val filteredList by viewModel.filteredSmartReport.collectAsState()
    LazyColumn {
      items(filteredList) { reportField ->
            SmartReportListComponent(smartReport = reportField)
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 1.dp,
                color = Purple50
            )
        }
    }
}

@Composable
fun SmartReportListComponent(smartReport: SmartReportField) {
    val resultEnum = LabParamResult.values().find { it.value == smartReport.resultId }

    val resultColor = when (resultEnum) {
        LabParamResult.NORMAL -> TextSuccess
        LabParamResult.HIGH, LabParamResult.VERY_HIGH, LabParamResult.CRITICALLY_HIGH -> TextError
        LabParamResult.LOW, LabParamResult.VERY_LOW, LabParamResult.CRITICALLY_LOW -> TextError
        else -> Gray800
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = smartReport.name ?: "",
                style = touchLabelRegular,
                color = Text01,
                modifier = Modifier.width(175.dp),
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = smartReport.range ?: "NA",
                style = touchLabelRegular,
                color = Text04,
                modifier = Modifier.width(155.dp),
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier,
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = smartReport.displayResult ?: "",
                style = touchLabelBold,
                color = resultColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = smartReport.value ?: "",
                style = touchBodyBold,
                color = Text01
            )
        }
    }
}

@Composable
fun SmartReportFilter(smartReport: SmartReport?, viewModel: DocumentPreviewViewModel) {
    val selectedFilter by viewModel.selectedFilter.collectAsState()

    LaunchedEffect(smartReport) {
        smartReport?.let {
            viewModel.initializeReports(it)
        }
    }

    val listOfFilter = listOf(
        "All Lab Vitals (${viewModel.getFilteredSmartReport(smartReport).size})",
        "Out of Range (${
            viewModel.getFilteredSmartReport(smartReport).count { field ->
                val resultEnum = LabParamResult.values().find { it.value == field.resultId }
                resultEnum != LabParamResult.NORMAL && resultEnum != LabParamResult.NO_INTERPRETATION_DONE
            }
        })"
    )

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(Purple50)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(listOfFilter.size) { index ->
            val filter = if (index == 0) Filter.ALL else Filter.OUT_OF_RANGE
            ChipMedium(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable { viewModel.updateFilter(filter, smartReport) },
                textModifier = Modifier.padding(vertical = 4.dp),
                text = listOfFilter[index],
                border = if (selectedFilter == filter) TextBrand else Border03,
                textColor = if (selectedFilter == filter) TextBrand else Text03,
                textStyle = touchCalloutBold,
                background = Color.White
            )
        }
    }
}

@Composable
fun SmartReportTopBar(onClick: (CTA?) -> Unit, documentDate: String, onDownloadClick : ()-> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(DarwinTouchNeutral0)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .padding(4.dp)
                .size(24.dp)
                .clickable {
                    onClick(CTA(action = "on_back_click"))
                },
            painter = painterResource(id = R.drawable.ic_arrow_back),
            tint = Icon01,
            contentDescription = ""
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = documentDate, style = touchBodyBold, color = Text01)
        Spacer(modifier = Modifier.weight(1f))
        IconButton(
            content = {
                Image(
                    painter = painterResource(id = R.drawable.ic_download_regular),
                    contentDescription = "TrailingIcon1",
                    modifier = Modifier.size(16.dp)
                )
            },
            onClick = onDownloadClick
        )
    }
}

enum class Filter {
    ALL,
    OUT_OF_RANGE
}
