package eka.care.documents.ui.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import eka.care.documents.R
import eka.care.documents.ui.touchBodyRegular
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

private val searchOptions = listOf(
    "Prescriptions",
    "Lab Reports",
    "Vaccine Certificates",
    "Insurance"
)

@Composable
fun DocumentSearch(
    modifier: Modifier = Modifier,
    onClickFilter: () -> Unit
) {
    var searchOption by remember { mutableStateOf(searchOptions[0]) }
    LaunchedEffect(key1 = Unit) {
        circularListFlow(searchOptions)
            .collect {
                searchOption = it
            }
    }
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .fillMaxWidth()
            .clickable { },
        shape = RoundedCornerShape(12.dp),
        colors = CardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
    ) {
        Row(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Search,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                contentDescription = ""
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(id = R.string.search_in_medical_records),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Start,
                style = touchBodyRegular,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            IconButton(modifier = Modifier.size(40.dp), content = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_filter),
                    contentDescription = "filter",
                    modifier = Modifier.size(24.dp)
                )
            }, onClick = {
                onClickFilter()
            })

        }
    }
}

private fun <T> circularListFlow(list: List<T>, intervalMillis: Long = 2000L): Flow<T> = flow {
    var index = 0
    while (true) {
        emit(list[index])
        delay(intervalMillis)
        index = (index + 1) % list.size
    }
}