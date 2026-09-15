package com.interiordesign3d.ui.theme

import android.app.Activity
import android.content.ContextWrapper
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.interiordesign3d.ui.theme.InteriorColors as C

val LightColorScheme = lightColorScheme(
    primary                 = C.Moss40,
    onPrimary               = Color.White,
    primaryContainer        = C.Moss95,
    onPrimaryContainer      = C.Moss10,
    inversePrimary          = C.Moss80,

    secondary               = C.Stone40,
    onSecondary             = Color.White,
    secondaryContainer      = C.Stone90,
    onSecondaryContainer    = C.Stone10,

    tertiary                = C.Copper40,
    onTertiary              = Color.White,
    tertiaryContainer       = C.Copper90,
    onTertiaryContainer     = C.Copper10,

    background              = C.BoneGround,
    onBackground            = C.OnBone,
    surface                 = C.BoneGround,
    onSurface               = C.OnBone,
    surfaceVariant          = C.NeutralVar90,
    onSurfaceVariant        = C.NeutralVar30,
    surfaceTint             = C.Moss40,

    surfaceContainerLowest  = Color.White,
    surfaceContainerLow     = C.Bone99,
    surfaceContainer        = C.Bone97,
    surfaceContainerHigh    = C.Bone94,
    surfaceContainerHighest = C.Bone90,

    inverseSurface          = C.Ink24,
    inverseOnSurface        = C.Bone99,

    outline                 = C.Outline,
    outlineVariant          = C.NeutralVar80,
    scrim                   = Color.Black,

    error                   = C.Error40,
    onError                 = Color.White,
    errorContainer          = C.Error90,
    onErrorContainer        = C.Error10,
)

val DarkColorScheme = darkColorScheme(
    primary                 = C.Moss80,
    onPrimary               = C.Moss20,
    primaryContainer        = C.Moss30,
    onPrimaryContainer      = C.Moss90,
    inversePrimary          = C.Moss40,

    secondary               = C.Stone80,
    onSecondary             = C.Stone20,
    secondaryContainer      = C.Stone30,
    onSecondaryContainer    = C.Stone90,

    tertiary                = C.Copper80,
    onTertiary              = C.Copper20,
    tertiaryContainer       = C.Copper30,
    onTertiaryContainer     = C.Copper90,

    background              = C.Ink8,
    onBackground            = C.OnInk,
    surface                 = C.Ink8,
    onSurface               = C.OnInk,
    surfaceVariant          = C.NeutralVar30,
    onSurfaceVariant        = C.NeutralVar80,
    surfaceTint             = C.Moss80,

    surfaceContainerLowest  = C.Ink4,
    surfaceContainerLow     = C.Ink12,
    surfaceContainer        = C.Ink14,
    surfaceContainerHigh    = C.Ink18,
    surfaceContainerHighest = C.Ink22,

    inverseSurface          = C.OnInk,
    inverseOnSurface        = C.Ink24,

    outline                 = C.OutlineDark,
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
    val view = LocalView.current
    if (!view.isInEditMode) {
        // enableEdgeToEdge picks bar icons from the OS setting; the in-app switch has to win.
        SideEffect {
            var ctx = view.context
            while (ctx is ContextWrapper && ctx !is Activity) ctx = ctx.baseContext
            (ctx as? Activity)?.window?.let { window ->
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !darkTheme
                    isAppearanceLightNavigationBars = !darkTheme
                }
            }
        }
    }

    CompositionLocalProvider(
        LocalInteriorAccents provides if (darkTheme) DarkAccents else LightAccents,
        LocalGlass provides if (darkTheme) StudioGlass else DayGlass,
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
            typography  = InteriorTypography,
            shapes      = InteriorShapes,
            content     = content,
        )
    }
}
