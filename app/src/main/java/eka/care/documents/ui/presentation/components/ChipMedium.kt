package eka.care.documents.ui.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import eka.care.documents.ui.DarwinTouchNeutral1000
import eka.care.documents.ui.touchLabelRegular

@Composable
fun ChipMedium(
    modifier: Modifier = Modifier,
    textModifier: Modifier = Modifier,
    border: Color,
    background: Color,
    text: String?,
    leftIcon: Int? = null,
    rightIcon: Int? = null,
    textColor: Color = DarwinTouchNeutral1000,
    textStyle: TextStyle = touchLabelRegular
) {
    Row(
        modifier = modifier
            .wrapContentWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(color = background)
            .border(1.dp, border, RoundedCornerShape(8.dp))
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        leftIcon?.let {
            Image(
                painter = painterResource(id = it),
                contentDescription = "LeftIcon",
                modifier = Modifier
                    .padding(horizontal = 2.dp)
                    .size(32.dp)
            )
        }
        Text(
            text = text ?: "",
            style = textStyle,
            color = textColor,
            modifier = textModifier.wrapContentWidth().padding(horizontal = 8.dp),
        )
        rightIcon?.let {
            Image(
                painter = painterResource(id = it),
                contentDescription = "RightIcon",
                modifier = Modifier
                    .padding(horizontal = 2.dp)
            )
        }
    }
}