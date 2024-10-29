package eka.care.documents.ui.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.shimmer

@Composable
fun DocumentShimmer() {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        item {
            Box(
                modifier = Modifier
                    .padding(start = 16.dp, top = 16.dp)
                    .fillMaxWidth()
                    .height(48.dp)
                    .shimmer()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(50)
                    )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .width(117.dp)
                    .height(24.dp)
                    .shimmer()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(50)
                    )
            )
        }
        items(8) {
            DocumentCardShimmer()
        }
    }
}

@Composable
fun DocumentCardShimmer() {
    ListItem(
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.padding(horizontal = 16.dp),
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .shimmer()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(50)
                    )
            )
        }, headlineContent = {
            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.Start) {
                Box(
                    modifier = Modifier
                        .width(180.dp)
                        .height(16.dp)
                        .padding(bottom = 4.dp)
                        .shimmer()
                        .background(
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            RoundedCornerShape(50)
                        )
                )
                Box(
                    modifier = Modifier
                        .width(66.dp)
                        .height(16.dp)
                        .shimmer()
                        .background(
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            RoundedCornerShape(50)
                        )
                )
            }
        }, trailingContent = {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .size(24.dp)
                    .shimmer()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(50)
                    )
            )
        })
}