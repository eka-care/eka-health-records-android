package eka.care.documents.ui.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import eka.care.documents.data.utility.DocumentUtility.Companion.docTypes
import eka.care.documents.ui.presentation.viewmodel.RecordsViewModel
import eka.care.documents.ui.touchCalloutBold

@Composable
fun DocumentFilter(
    viewModel: RecordsViewModel,
    onClick: (Int) -> Unit,
) {
    val documentType by viewModel.documentType
    val getAvailableDocTypes by viewModel.getAvailableDocTypes.collectAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LazyRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val totalCount = getAvailableDocTypes.resp?.sumOf { it.count } ?: 0
            item {
                ChipMedium(
                    modifier = Modifier.clip(RoundedCornerShape(4.dp)).clickable { onClick(-1) },
                    textModifier = Modifier.padding(vertical = 4.dp),
                    text = "All ($totalCount)",
                    border = if (documentType == -1) MaterialTheme.colorScheme.inversePrimary else MaterialTheme.colorScheme.outlineVariant,
                    textColor = if (documentType == -1) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                    textStyle = touchCalloutBold,
                    background = if (documentType == -1) MaterialTheme.colorScheme.inversePrimary else MaterialTheme.colorScheme.surfaceContainerLowest
                )
            }
            getAvailableDocTypes.resp?.forEach { availableDocType ->
                val docTypeModel = docTypes.find { it.idNew == availableDocType.docType }
                if (docTypeModel != null) {
                    item {
                        ChipMedium(
                            modifier = Modifier.clip(RoundedCornerShape(4.dp)).clickable {
                                onClick(availableDocType.docType)
                            },
                            textModifier = Modifier.padding(vertical = 4.dp),
                            text = "${docTypeModel.documentType} (${availableDocType.count})", // Use documentType here
                            border = if (documentType == availableDocType.docType) MaterialTheme.colorScheme.inversePrimary else MaterialTheme.colorScheme.outlineVariant,
                            textColor = if (documentType == availableDocType.docType) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            textStyle = touchCalloutBold,
                            background = if (documentType == availableDocType.docType) MaterialTheme.colorScheme.inversePrimary else MaterialTheme.colorScheme.surfaceContainerLowest,
                        )
                    }
                }
            }
        }
    }
}