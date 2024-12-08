package eka.care.documents.ui.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import eka.care.documents.R
import eka.care.documents.data.utility.DocumentUtility.Companion.docTypes
import eka.care.documents.ui.touchBodyRegular
import eka.care.documents.ui.presentation.model.CTA
import eka.care.documents.ui.presentation.model.RecordModel
import eka.care.documents.ui.touchLabelRegular
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DocumentList(
    recordModel: RecordModel,
    onClick: (CTA?) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        val docType = docTypes.find { it.idNew == recordModel.documentType }
        val uploadTimestamp = recordModel.documentDate
        val uploadDate = uploadTimestamp?.times(1000)?.let { Date(it) }
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val formattedDate = uploadDate?.let { sdf.format(it) }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onClick(CTA(action = "open_deepThought"))
                },
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
                modifier = Modifier,
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = docType?.documentType.toString(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (formattedDate != null) {
                    Text(
                        text = formattedDate,
                        style = touchLabelRegular,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }else{
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
        Row {
            if(recordModel.tags?.split(",")?.contains("1") == true){
                SmartChip()
            }
            Icon(
                modifier = Modifier.clickable {
                    onClick(CTA(action = "open_options"))
                },
                imageVector = Icons.Rounded.MoreVert,
                contentDescription = ""
            )
        }
    }
}