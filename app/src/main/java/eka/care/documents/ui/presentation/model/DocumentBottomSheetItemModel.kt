package eka.care.documents.ui.presentation.model

import androidx.annotation.Keep
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter

@Keep
data class DocumentBottomSheetItemModel(
    val itemName : String,
    val itemNameColor : Color,
    val isRecommended : Boolean,
    val itemType : CTA,
    val leadingIcon : Painter,
    val trailingIcon : Painter,
    val leadingIconTint : Color
)