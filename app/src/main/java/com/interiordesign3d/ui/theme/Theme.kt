package com.interiordesign3d.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─── Brand Colors ─────────────────────────────────────────────────────────────

object InteriorColors {
    // Warm neutrals
    val WarmWhite       = Color(0xFFF5F0EB)
    val ParchmentLight  = Color(0xFFEDE8E1)
    val Parchment       = Color(0xFFD9CFC4)
    val WarmGray        = Color(0xFF8B7355)

    // Primary accent — warm terracotta
    val Terracotta      = Color(0xFFB5451B)
    val TerracottaLight = Color(0xFFD4602A)
    val TerracottaDark  = Color(0xFF8A3210)

    // Secondary — sage green
    val Sage            = Color(0xFF4A7C59)
    val SageLight       = Color(0xFF6A9E78)
    val SageDark        = Color(0xFF2C5F3E)

    // Dark surfaces
    val DeepEspresso    = Color(0xFF2C1810)
    val CharcoalWarm    = Color(0xFF3A3530)
    val SlateWarm       = Color(0xFF5C5550)

    // Utility
    val Gold            = Color(0xFFC4A028)
    val ErrorRed        = Color(0xFFD94040)
    val SuccessGreen    = Color(0xFF4A7C59)
    val InfoBlue        = Color(0xFF4A6D8C)
}

// ─── Light Color Scheme ───────────────────────────────────────────────────────

private val LightColorScheme = lightColorScheme(
    primary             = InteriorColors.Terracotta,
    onPrimary           = Color.White,
    primaryContainer    = Color(0xFFFFDAD1),
    onPrimaryContainer  = InteriorColors.TerracottaDark,

    secondary           = InteriorColors.Sage,
    onSecondary         = Color.White,
    secondaryContainer  = Color(0xFFD2EDDA),
    onSecondaryContainer = InteriorColors.SageDark,

    tertiary            = InteriorColors.Gold,
    onTertiary          = Color.White,

    background          = InteriorColors.WarmWhite,
    onBackground        = InteriorColors.DeepEspresso,

    surface             = Color.White,
    onSurface           = InteriorColors.DeepEspresso,
    surfaceVariant      = InteriorColors.ParchmentLight,
    onSurfaceVariant    = InteriorColors.CharcoalWarm,

    outline             = InteriorColors.Parchment,
    outlineVariant      = InteriorColors.WarmGray,

    error               = InteriorColors.ErrorRed,
    onError             = Color.White,
)

// ─── Dark Color Scheme ────────────────────────────────────────────────────────

private val DarkColorScheme = darkColorScheme(
    primary             = InteriorColors.TerracottaLight,
    onPrimary           = InteriorColors.DeepEspresso,
    primaryContainer    = InteriorColors.TerracottaDark,
    onPrimaryContainer  = Color(0xFFFFDAD1),

    secondary           = InteriorColors.SageLight,
    onSecondary         = InteriorColors.SageDark,
    secondaryContainer  = InteriorColors.SageDark,
    onSecondaryContainer = Color(0xFFD2EDDA),

    background          = InteriorColors.DeepEspresso,
    onBackground        = InteriorColors.WarmWhite,

    surface             = InteriorColors.CharcoalWarm,
    onSurface           = InteriorColors.WarmWhite,
    surfaceVariant      = Color(0xFF4A4040),
    onSurfaceVariant    = InteriorColors.Parchment,

    outline             = InteriorColors.SlateWarm,
)

// ─── Typography ───────────────────────────────────────────────────────────────

val InteriorTypography = Typography(
    displayLarge = TextStyle(
        fontWeight = FontWeight.Light,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontWeight = FontWeight.Light,
        fontSize = 45.sp,
        lineHeight = 52.sp
    ),
    headlineLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 40.sp
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),
    headlineSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

// ─── Shapes ───────────────────────────────────────────────────────────────────

val InteriorShapes = Shapes(
    extraSmall  = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
    small       = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
    medium      = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
    large       = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
    extraLarge  = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
)

// ─── Theme ────────────────────────────────────────────────────────────────────

@Composable
fun InteriorDesignTheme(
    darkTheme: Boolean = androidx.compose.foundation.isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = InteriorTypography,
        shapes      = InteriorShapes,
        content     = content
    )
}


