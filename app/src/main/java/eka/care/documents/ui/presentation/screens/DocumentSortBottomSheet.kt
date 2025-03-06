package eka.care.documents.ui.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import eka.care.documents.R
import eka.care.documents.ui.DarwinTouchNeutral1000
import eka.care.documents.ui.presentation.components.BottomSheetItemSortDocument
import eka.care.documents.ui.presentation.components.DocumentBottomSheetsTitle
import eka.care.documents.ui.presentation.model.BottomSheetItemSortDocumentModel
import eka.care.documents.ui.presentation.model.BottomSheetTitleModel

@Composable
fun DocumentSortBottomSheet(
    selectedSort: DocumentSortEnum,
    onCloseClick: () -> Unit,
    onClick: (DocumentSortEnum) -> Unit
) {
    val sortBottomSheetItems = arrayOf(
        BottomSheetItemSortDocumentModel(
            itemType = DocumentSortEnum.UPLOAD_DATE,
            itemTitle = stringResource(id = R.string.upload_date),
            itemLeadingIcon = 1
        ),
        BottomSheetItemSortDocumentModel(
            itemType = DocumentSortEnum.DOCUMENT_DATE,
            itemTitle = stringResource(id = R.string.document_date),
            itemLeadingIcon = 1
        )
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .background(
                MaterialTheme.colorScheme.surfaceContainerLowest,
                RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Divider(
            modifier = Modifier
                .padding(vertical = 6.dp)
                .width(48.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    DarwinTouchNeutral1000
                )
        )
        Spacer(modifier = Modifier.height(16.dp))
        Column(
            modifier = Modifier.padding(top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = MaterialTheme.colorScheme.surfaceContainerLowest)
            ) {
                DocumentBottomSheetsTitle(
                    bottomSheetTitleModel = BottomSheetTitleModel(
                        bottomSheetTitleText = stringResource(id = R.string.sort_by)
                    ),
                    onCloseClick = {
                        onCloseClick()
                    }
                )
                LazyColumn {
                    itemsIndexed(sortBottomSheetItems) { index, item ->
                        BottomSheetItemSortDocument(
                            bottomSheetItemSortDocumentModel = item,
                            isSelected = selectedSort == item.itemType
                        ) {
                            onClick(item.itemType)
                        }
                        if (index != sortBottomSheetItems.size - 1) {
                            Spacer(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .background(color = Color.Transparent)
                            )
                        }
                    }
                }
            }
        }
    }
}

enum class DocumentSortEnum {
    UPLOAD_DATE, DOCUMENT_DATE
}