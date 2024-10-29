package eka.care.documents.ui.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import eka.care.documents.R
import eka.care.documents.ui.DarwinTouchPrimary
import eka.care.documents.ui.presentation.components.DocumentBottomSheetType
import eka.care.documents.ui.touchBodyRegular
import eka.care.documents.ui.touchTitle3Bold

@Composable
fun DocumentEmptyStateScreen(
    openBottomSheet: (DocumentBottomSheetType) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerLowest),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_no_files),
            contentDescription = "no_files",
            modifier = Modifier.padding(bottom = 24.dp)
        )
        Text(
            text = stringResource(id = R.string.no_medical_records_yet),
            color = MaterialTheme.colorScheme.onSurface,
            style = touchTitle3Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = stringResource(id = R.string.add_or_upload_files_to_get_started),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = touchBodyRegular,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        Button(
            onClick = { openBottomSheet(DocumentBottomSheetType.DocumentUpload) },
            colors = ButtonDefaults.buttonColors(
                containerColor = DarwinTouchPrimary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                disabledContentColor = MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = DarwinTouchPrimary
            )
        ) {
            Text(text = stringResource(id = R.string.add_files), style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimary)
        }
    }
}
