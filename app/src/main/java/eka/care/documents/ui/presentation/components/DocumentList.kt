package eka.care.documents.ui.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import eka.care.documents.R
import eka.care.documents.data.utility.DocumentUtility.Companion.docTypes
import eka.care.documents.ui.presentation.model.CTA
import eka.care.documents.ui.presentation.model.RecordModel
import eka.care.documents.ui.touchLabelRegular
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DocumentList(
    recordModel: RecordModel,
    onClick: (CTA?, RecordModel) -> Unit,
    mode: Mode,
    selectedItems: SnapshotStateList<RecordModel>,
    onSelectedItemsChange: (List<RecordModel>) -> Unit
) {
    val isSelected = selectedItems.contains(recordModel)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isSelected && mode == Mode.SELECTION) Color.LightGray
                else MaterialTheme.colorScheme.surface
            )
            .clickable {
                if (mode == Mode.SELECTION) {
                    if (isSelected) {
                        selectedItems.remove(recordModel)
                    } else {
                        selectedItems.add(recordModel)
                    }
                    onSelectedItemsChange(selectedItems.toList())
                } else {
                    onClick(CTA(action = "open_deepThought"), recordModel)
                }
            }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        val docType = docTypes.find { it.idNew == recordModel.documentType }
        val uploadTimestamp = recordModel.documentDate
        val uploadDate = uploadTimestamp?.times(1000)?.let { Date(it) }
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val formattedDate = uploadDate?.let { sdf.format(it) }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(
                    id = docType?.icon ?: R.drawable.ic_others_new
                ),
                contentDescription = ""
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = docType?.documentType.toString(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                formattedDate?.let {
                    Text(
                        text = it,
                        style = touchLabelRegular,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        Row {
            if (recordModel.fileType.equals("pdf", ignoreCase = true)) {
                if (recordModel.autoTags?.split(",")?.contains("1") == true) {
                    SmartChip()
                }
            }
            if (mode == Mode.VIEW) {
                Icon(
                    modifier = Modifier.clickable {
                        onClick(CTA(action = "open_options"), recordModel)
                    },
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = ""
                )
            }
        }

        if (isSelected && mode == Mode.SELECTION) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(24.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = CircleShape
                    )
                    .padding(2.dp)
            )
        }
    }
}