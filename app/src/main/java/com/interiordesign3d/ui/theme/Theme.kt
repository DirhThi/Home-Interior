package com.interiordesign3d.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.interiordesign3d.ui.theme.InteriorColors as C

private val LightColorScheme = lightColorScheme(
    primary                 = C.Terracotta40,
    onPrimary               = Color.White,
    primaryContainer        = C.Terracotta90,
    onPrimaryContainer      = C.Terracotta10,
    inversePrimary          = C.Terracotta80,

    secondary               = C.Sage40,
    onSecondary             = Color.White,
    secondaryContainer      = C.Sage90,
    onSecondaryContainer    = C.Sage10,

    tertiary                = C.Gold40,
    onTertiary              = Color.White,
    tertiaryContainer       = C.Gold90,
    onTertiaryContainer     = C.Gold10,

    background              = C.Neutral98,
    onBackground            = C.Neutral12,
    surface                 = C.Neutral98,
    onSurface               = C.Neutral12,
    surfaceVariant          = C.NeutralVar90,
    onSurfaceVariant        = C.NeutralVar30,
    surfaceTint             = C.Terracotta40,

    surfaceContainerLowest  = Color.White,
    surfaceContainerLow     = C.Neutral96,
    surfaceContainer        = C.Neutral94,
    surfaceContainerHigh    = C.NeutralVar92,
    surfaceContainerHighest = C.NeutralVar88,

    inverseSurface          = C.Neutral24,
    inverseOnSurface        = C.Neutral98,

    outline                 = C.Neutral50,
    outlineVariant          = C.NeutralVar80,
    scrim                   = Color.Black,

    error                   = C.Error40,
    onError                 = Color.White,
    errorContainer          = C.Error90,
    onErrorContainer        = C.Error10,
)

private val DarkColorScheme = darkColorScheme(
    primary                 = C.Terracotta80,
    onPrimary               = C.Terracotta20,
    primaryContainer        = C.Terracotta30,
    onPrimaryContainer      = C.Terracotta90,
    inversePrimary          = C.Terracotta40,

    secondary               = C.Sage80,
    onSecondary             = C.Sage20,
    secondaryContainer      = C.Sage30,
    onSecondaryContainer    = C.Sage90,

    tertiary                = C.Gold80,
    onTertiary              = C.Gold20,
    tertiaryContainer       = C.Gold30,
    onTertiaryContainer     = C.Gold90,

    background              = C.Neutral10,
    onBackground            = C.Neutral90,
    surface                 = C.Neutral10,
    onSurface               = C.Neutral90,
    surfaceVariant          = C.NeutralVar30,
    onSurfaceVariant        = C.NeutralVar80,
    surfaceTint             = C.Terracotta80,

    surfaceContainerLowest  = C.Neutral6,
    surfaceContainerLow     = C.Neutral12,
    surfaceContainer        = C.Neutral17,
    surfaceContainerHigh    = C.Neutral22,
    surfaceContainerHighest = C.Neutral27,

    inverseSurface          = C.Neutral90,
    inverseOnSurface        = C.Neutral24,

    outline                 = C.Neutral60,
    outlineVariant          = C.NeutralVar30,
    scrim                   = Color.Black,

    error                   = C.Error80,
    onError                 = C.Error20,
    errorContainer          = C.Error30,
    onErrorContainer        = C.Error90,
)

val InteriorShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small      = RoundedCornerShape(10.dp),
    medium     = RoundedCornerShape(14.dp),
    large      = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun InteriorDesignTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalInteriorAccents provides if (darkTheme) DarkAccents else LightAccents
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
            typography  = InteriorTypography,
            shapes      = InteriorShapes,
            content     = content,
        )
    }
}
