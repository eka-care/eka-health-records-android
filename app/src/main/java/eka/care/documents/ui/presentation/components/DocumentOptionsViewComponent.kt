package eka.care.documents.ui.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import eka.care.documents.R
import eka.care.documents.ui.presentation.model.BottomSheetTitleModel
import eka.care.documents.ui.presentation.model.CTA
import eka.care.documents.ui.presentation.model.DocumentBottomSheetItemModel
import eka.care.documents.ui.utility.RecordsAction

@Composable
fun DocumentOptionsViewComponent(onClick: (CTA) -> Unit) {

    val documentOptionsItems = arrayOf(
        DocumentBottomSheetItemModel(
            itemName = stringResource(id = R.string.edit_doc_details),
            itemNameColor = MaterialTheme.colorScheme.onSurface,
            isRecommended = false,
            itemType = CTA(action = RecordsAction.ACTION_EDIT_DOCUMENT),
            leadingIcon = rememberVectorPainter(image = Icons.Outlined.Edit),
            leadingIconTint = MaterialTheme.colorScheme.onSurfaceVariant,
            trailingIcon = rememberVectorPainter(image = Icons.Filled.PlayArrow)
        ),
        DocumentBottomSheetItemModel(
            itemName = stringResource(id = R.string.share_document),
            itemNameColor = MaterialTheme.colorScheme.onSurface,
            isRecommended = false,
            itemType = CTA(action = RecordsAction.ACTION_SHARE_DOCUMENT),
            leadingIcon = rememberVectorPainter(image = Icons.Outlined.Share),
            leadingIconTint = MaterialTheme.colorScheme.onSurfaceVariant,
            trailingIcon = rememberVectorPainter(image = Icons.Filled.PlayArrow)
        ),
        DocumentBottomSheetItemModel(
            itemName = stringResource(id = R.string.delete_record),
            itemNameColor = MaterialTheme.colorScheme.error,
            isRecommended = false,
            itemType = CTA(action = RecordsAction.ACTION_DELETE_RECORD),
            leadingIcon = painterResource(id = R.drawable.ic_delete_account),
            leadingIconTint = MaterialTheme.colorScheme.error,
            trailingIcon = rememberVectorPainter(image = Icons.Filled.PlayArrow)
        )
    )

    Column(modifier = Modifier
        .fillMaxWidth()
    ) {
        DocumentBottomSheetsTitle(
            bottomSheetTitleModel = BottomSheetTitleModel(bottomSheetTitleText = stringResource(id = R.string.choose_an_option)),
            onCloseClick = {
                onClick(CTA(action = "Close_bottom_sheet"))
            }
        )
        Spacer(modifier = Modifier.width(8.dp))
        LazyColumn {
            itemsIndexed(documentOptionsItems) { index,item->
                DocumentBottomSheetItem(bottomSheetItemModel = item) {
                    onClick(item.itemType)
                }
            }
        }
    }
}