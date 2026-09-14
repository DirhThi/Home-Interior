package com.interiordesign3d.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.interiordesign3d.R

// Variable font: Compose derives each weight from the `wght` axis on API 26+.
val AppFont = FontFamily(
    Font(R.font.plus_jakarta_sans, FontWeight.Light),
    Font(R.font.plus_jakarta_sans, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans, FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans, FontWeight.Bold),
    Font(R.font.plus_jakarta_sans, FontWeight.ExtraBold),
)

private fun style(
    size: Int,
    line: Int,
    weight: FontWeight,
    tracking: Double = 0.0,
) = TextStyle(
    fontFamily = AppFont,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = line.sp,
    letterSpacing = tracking.sp,
)

val InteriorTypography = Typography(
    displayLarge   = style(52, 60, FontWeight.Bold, -1.0),
    displayMedium  = style(42, 50, FontWeight.Bold, -0.6),
    displaySmall   = style(34, 42, FontWeight.Bold, -0.4),

    headlineLarge  = style(30, 38, FontWeight.Bold, -0.4),
    headlineMedium = style(26, 34, FontWeight.Bold, -0.3),
    headlineSmall  = style(22, 30, FontWeight.SemiBold, -0.2),

    titleLarge     = style(20, 28, FontWeight.SemiBold, -0.2),
    titleMedium    = style(17, 24, FontWeight.SemiBold),
    titleSmall     = style(15, 20, FontWeight.SemiBold),

    bodyLarge      = style(16, 24, FontWeight.Normal),
    bodyMedium     = style(14, 20, FontWeight.Normal, 0.1),
    bodySmall      = style(12, 17, FontWeight.Normal, 0.2),

    labelLarge     = style(14, 20, FontWeight.SemiBold, 0.1),
    labelMedium    = style(12, 16, FontWeight.SemiBold, 0.3),
    labelSmall     = style(11, 15, FontWeight.Medium, 0.4),
)
