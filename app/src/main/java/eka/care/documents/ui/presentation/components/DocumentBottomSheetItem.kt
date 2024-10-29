package eka.care.documents.ui.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import eka.care.documents.R
import eka.care.documents.ui.presentation.model.DocumentBottomSheetItemModel

@Composable
fun DocumentBottomSheetItem(
    bottomSheetItemModel: DocumentBottomSheetItemModel,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
    ) {
        ListItem(
            modifier = Modifier
                .height(40.dp),
            headlineContent = {
                Text(
                    text = bottomSheetItemModel.itemName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = bottomSheetItemModel.itemNameColor
                )
            },
            leadingContent = {
                Image(
                    painter = bottomSheetItemModel.leadingIcon,
                    contentDescription = "",
                    modifier = Modifier
                        .height(24.dp)
                        .width(24.dp),
                    colorFilter = ColorFilter.tint(bottomSheetItemModel.leadingIconTint)
                )
            },
            trailingContent = {
                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if(bottomSheetItemModel.isRecommended) {
                        RecommendedChip(text = stringResource(id = R.string.recommended))
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Icon(
                        bottomSheetItemModel.trailingIcon,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(24.dp)
                            .padding(4.dp),
                        contentDescription = "Click"
                    )
                }
            },
            colors = ListItemDefaults.colors(
                containerColor = Color.Transparent
            )
        )
    }
}

@Composable
fun RecommendedChip(text : String) {
    Box(
        modifier = Modifier
            .background(
                color = Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}