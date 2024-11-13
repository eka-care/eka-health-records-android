package eka.care.documents.ui.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun DocumentScreenTopBar(onBackClick: () -> Unit, name : String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
            contentDescription = "",
            modifier = Modifier
                .padding(start = 8.dp, end = 16.dp)
                .clickable {
                    onBackClick()
                }
                .size(24.dp)
                .rotate(180f),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
        )
        Text(
            modifier = Modifier.weight(1f),
            text = name,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Start
        )
        AsyncImage(
            modifier = Modifier
                .padding(end = 16.dp)
                .border(1.dp, MaterialTheme.colorScheme.onSurface, RoundedCornerShape(50))
                .clip(CircleShape)
                .size(32.dp)
                .clickable(
                    onClick = {},
                    interactionSource = remember { MutableInteractionSource() },
                    indication = rememberRipple(bounded = true)
                ),
            model = "https://a.eka.care/user-avatar/",
            contentScale = ContentScale.Crop,
            contentDescription = ""
        )
    }
}