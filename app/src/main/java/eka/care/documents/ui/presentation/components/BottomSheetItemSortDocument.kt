package eka.care.documents.ui.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import eka.care.documents.R
import eka.care.documents.ui.presentation.model.BottomSheetItemSortDocumentModel

@Composable
fun BottomSheetItemSortDocument(
    bottomSheetItemSortDocumentModel: BottomSheetItemSortDocumentModel,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent
    ) {
        ListItem(
            headlineContent = {
                Text(
                    text = bottomSheetItemSortDocumentModel.itemTitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            leadingContent = {
                if(isSelected)
                    Image(
                        painter = painterResource(id = R.drawable.ic_back_arrow),
                        contentDescription = "",
                        modifier = Modifier.rotate(90f),
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
                    )
                else
                    Image(
                        painter = ColorPainter(color = Color.Transparent),
                        modifier = Modifier
                            .width(15.dp)
                            .height(12.dp),
                        contentDescription = "",
                    )
            },
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .height(48.dp)
                .background(
                    color = if (isSelected) MaterialTheme.colorScheme.surfaceContainerHigh else Color.Transparent,
                    shape = RoundedCornerShape(32.dp)
                ),
            colors = ListItemDefaults.colors(
                containerColor = Color.Transparent,
            )
        )
    }
}