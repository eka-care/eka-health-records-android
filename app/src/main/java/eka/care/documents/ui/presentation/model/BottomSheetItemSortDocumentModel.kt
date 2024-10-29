package eka.care.documents.ui.presentation.model

import androidx.annotation.Keep
import eka.care.documents.ui.presentation.screens.DocumentSortEnum

@Keep
data class BottomSheetItemSortDocumentModel(
    var itemTitle : String,
    var itemLeadingIcon : Int,
    var itemType : DocumentSortEnum
)