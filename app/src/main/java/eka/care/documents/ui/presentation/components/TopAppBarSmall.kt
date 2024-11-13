package eka.care.documents.ui.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.LocalMinimumTouchTargetEnforcement
import androidx.compose.material.Text
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TopAppBarSmall(
    modifier: Modifier = Modifier,
    title: String?,
    subTitle: String? = null,
    textAlign: TextAlign? = null,
    leading: Int? = null,
    leadingIconUrl: String? = null,
    onLeadingClick: () -> Unit,
    avatar: String? = null,
    trailingText: String? = null,
    trailingIcon1: Int? = null,
    trailingIconUrl: String? = null,
    trailingIcon2: Int? = null,
    trailingIcon3: Int? = null,
    onTrailingTextClick: (() -> Unit)? = null,
    onTrailingIcon1Click: (() -> Unit)? = null,
    onTrailingIcon2Click: (() -> Unit)? = null,
    onTrailingIcon3Click: (() -> Unit)? = null,
) {
    CompositionLocalProvider(LocalMinimumTouchTargetEnforcement provides false) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            leading?.let {
                IconButton(
                    content = {
                        Icon(
                            painter = painterResource(id = leading),
                            contentDescription = "",
                            tint = Color(0XFF64748B),
                        )
                    }, onClick = onLeadingClick
                )
            }
            avatar?.let {
                AsyncImage(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .clip(CircleShape)
                        .size(32.dp)
                        .clickable {
                            onLeadingClick()
                        },
                    model = it,
                    contentScale = ContentScale.Crop,
                    contentDescription = "LeftIcon"
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 16.dp),
            ) {
                Text(
                    text = title ?: "",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0XFF0F172A),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    textAlign = textAlign ?: TextAlign.Start
                )
                subTitle?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0XFF64748B),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        textAlign = textAlign ?: TextAlign.Start
                    )
                }
            }
            trailingText?.let {
                TextButton(onClick = { onTrailingTextClick?.invoke() }) {
                    Text(text = it, color = Color(0XFF6B5CE0), style = MaterialTheme.typography.labelSmall)
                }
            }
            trailingIcon1?.let {
                IconButton(
                    content = {
                        Image(
                            painter = painterResource(id = it),
                            contentDescription = "TrailingIcon1",
                        )
                    },
                    onClick = onTrailingIcon1Click ?: { }
                )
            }
            trailingIconUrl?.let {
                AsyncImage(
                    modifier = Modifier.clickable {
                        onTrailingIcon1Click?.invoke()
                    },
                    model = it,
                    contentDescription = ""
                )
            }
            trailingIcon2?.let {
                IconButton(
                    content = {
                        Image(
                            painter = painterResource(id = it),
                            contentDescription = "TrailingIcon2",
                        )
                    },
                    onClick = onTrailingIcon2Click ?: { }
                )
            }
            trailingIcon3?.let {
                IconButton(
                    content = {
                        Icon(
                            painter = painterResource(id = it),
                            contentDescription = "TrailingIcon3",
                            tint = Color(0XFF64748B),
                        )
                    },
                    onClick = onTrailingIcon3Click ?: { }
                )
            }
        }
    }
}