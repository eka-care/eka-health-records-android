package eka.care.documents.ui.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import eka.care.documents.R
import eka.care.documents.ui.DarwinTouchNeutral1000
import eka.care.documents.ui.presentation.components.DocumentBottomSheetItem
import eka.care.documents.ui.presentation.components.DocumentBottomSheetsTitle
import eka.care.documents.ui.presentation.model.BottomSheetTitleModel
import eka.care.documents.ui.presentation.model.CTA
import eka.care.documents.ui.presentation.model.DocumentBottomSheetItemModel
import eka.care.documents.ui.utility.RecordsAction

@Composable
fun DocumentUploadBottomSheet(onClick: (CTA?) -> Unit) {
    val uploadOptionsItems = arrayOf(
        DocumentBottomSheetItemModel(
            itemName = stringResource(id = R.string.take_a_photo),
            itemNameColor = MaterialTheme.colorScheme.onSurface,
            isRecommended = false,
            itemType = CTA(action = RecordsAction.ACTION_TAKE_PHOTO),
            leadingIcon = painterResource(id = R.drawable.ic_camera_filled),
            leadingIconTint = MaterialTheme.colorScheme.onSurfaceVariant,
            trailingIcon = rememberVectorPainter(image = Icons.Filled.PlayArrow)
        ),
        DocumentBottomSheetItemModel(
            itemName = stringResource(id = R.string.scan_a_document),
            itemNameColor = MaterialTheme.colorScheme.onSurface,
            isRecommended = false,
            itemType = CTA(action = RecordsAction.ACTION_SCAN_A_DOCUMENT),
            leadingIcon = painterResource(id = R.drawable.ic_camera_filled),
            leadingIconTint = MaterialTheme.colorScheme.onSurfaceVariant,
            trailingIcon = rememberVectorPainter(image = Icons.Filled.PlayArrow)
        ),
        DocumentBottomSheetItemModel(
            itemName = stringResource(id = R.string.choose_from_gallery),
            itemNameColor = MaterialTheme.colorScheme.onSurface,
            isRecommended = false,
            itemType = CTA(action = RecordsAction.ACTION_CHOOSE_FROM_GALLERY),
            leadingIcon = painterResource(id = R.drawable.ic_pic_gallery),
            leadingIconTint = MaterialTheme.colorScheme.onSurfaceVariant,
            trailingIcon = rememberVectorPainter(image = Icons.Filled.PlayArrow)
        ),
        DocumentBottomSheetItemModel(
            itemName = stringResource(id = R.string.upload_pdf_file),
            itemNameColor = MaterialTheme.colorScheme.onSurface,
            isRecommended = false,
            itemType = CTA(action = RecordsAction.ACTION_UPLOAD_PDF),
            leadingIcon = painterResource(id = R.drawable.ic_pdf_box),
            leadingIconTint = MaterialTheme.colorScheme.onSurfaceVariant,
            trailingIcon = rememberVectorPainter(image = Icons.Filled.PlayArrow)
        )
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerLowest, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
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
        Column(
            modifier = Modifier.padding(top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                DocumentBottomSheetsTitle(
                    bottomSheetTitleModel = BottomSheetTitleModel(
                        bottomSheetTitleText = stringResource(
                            id = R.string.add_medical_records
                        )
                    ),
                    onCloseClick = {
                        onClick(CTA(action = "Close_bottom_sheet"))
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                LazyColumn {
                    itemsIndexed(uploadOptionsItems) { index, item ->
                        DocumentBottomSheetItem(bottomSheetItemModel = item) {
                            onClick(item.itemType)
                        }
                    }
                }
            }
        }
    }
}