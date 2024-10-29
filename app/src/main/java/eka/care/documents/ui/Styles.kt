package eka.care.documents.ui

import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp
import eka.care.documents.R

private const val DefaultIncludeFontPadding = false

val ekaFontFamily = FontFamily(
    Font(R.font.lato_light, FontWeight.Light),
    Font(R.font.lato, FontWeight.Normal),
    Font(R.font.lato_medium, FontWeight.Medium),
    Font(R.font.lato_semi_bold, FontWeight.SemiBold),
    Font(R.font.lato_bold, FontWeight.Bold)
)

private val DefaultPlatformTextStyle = PlatformTextStyle(
    includeFontPadding = DefaultIncludeFontPadding
)

val DefaultLineHeightStyle = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None
)

fun defaultPlatformTextStyle(): PlatformTextStyle? = DefaultPlatformTextStyle

val DefaultTextStyle = TextStyle.Default.copy(
    platformStyle = defaultPlatformTextStyle(),
    lineHeightStyle = DefaultLineHeightStyle,
)

val touchBodyBold = DefaultTextStyle.copy(
    fontFamily = ekaFontFamily,
    fontWeight = FontWeight.Bold,
    fontStyle = FontStyle.Normal,
    fontSize = 16.sp,
    lineHeight = 24.sp,
)
val touchLabelBold = DefaultTextStyle.copy(
    fontFamily = ekaFontFamily,
    fontWeight = FontWeight.Bold,
    fontStyle = FontStyle.Normal,
    fontSize = 12.sp,
    lineHeight = 16.sp,
)

val touchBodyRegular = DefaultTextStyle.copy(
    fontFamily = ekaFontFamily,
    fontWeight = FontWeight.Normal,
    fontStyle = FontStyle.Normal,
    fontSize = 16.sp,
    lineHeight = 24.sp,
)

val touchHeadlineBold = DefaultTextStyle.copy(
    fontFamily = ekaFontFamily,
    fontWeight = FontWeight.Bold,
    fontStyle = FontStyle.Normal,
    fontSize = 16.sp,
    lineHeight = 20.sp,
)

val touchTitle3Bold = DefaultTextStyle.copy(
    fontFamily = ekaFontFamily,
    fontWeight = FontWeight.Bold,
    fontStyle = FontStyle.Normal,
    fontSize = 20.sp,
    lineHeight = 28.sp,
)

val touchLabelRegular = DefaultTextStyle.copy(
    fontFamily = ekaFontFamily,
    fontWeight = FontWeight.Normal,
    fontStyle = FontStyle.Normal,
    fontSize = 12.sp,
    lineHeight = 16.sp,
)

val touchFootnoteBold = DefaultTextStyle.copy(
    fontFamily = ekaFontFamily,
    fontWeight = FontWeight.Bold,
    fontStyle = FontStyle.Normal,
    fontSize = 13.sp,
    lineHeight = 20.sp,
)