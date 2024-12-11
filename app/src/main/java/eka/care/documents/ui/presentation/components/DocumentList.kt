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
import eka.care.documents.ui.DarwinTouchNeutral200
import eka.care.documents.ui.DarwinTouchNeutral50
import eka.care.documents.ui.Gray200
import eka.care.documents.ui.Gray400
import eka.care.documents.ui.touchBodyRegular
import eka.care.documents.ui.presentation.model.CTA
import eka.care.documents.ui.presentation.model.RecordModel
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
            .background(
                if(recordModel.filePath?.isEmpty() == true) DarwinTouchNeutral200 else MaterialTheme.colorScheme.surface
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        val docType = docTypes.find { it.idNew == recordModel.documentType }
        val uploadTimestamp = recordModel.documentDate ?: recordModel.createdAt ?: 0L
        val uploadDate = Date(uploadTimestamp * 1000)
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val formattedDate = sdf.format(uploadDate)

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
                Text(
                    text = formattedDate,
                    style = touchBodyRegular,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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