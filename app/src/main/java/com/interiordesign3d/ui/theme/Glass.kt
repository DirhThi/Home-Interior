package com.interiordesign3d.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalDensity
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.isRenderEffectSupported
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.interiordesign3d.ui.theme.InteriorColors as C

/**
 * Chrome floats over the content as smoked glass: a tint, a rim lit from the top, and a soft shadow.
 *
 * There is deliberately **no backdrop blur**. Two reasons, both structural: the 3D viewport is a
 * `SurfaceView` composited on its own layer, so nothing drawn above it can sample its pixels; and
 * `Modifier.blur` blurs the node's own content rather than what sits behind it. Tint plus rim carries
 * the same read, costs one draw call, and works all the way down to minSdk 26.
 */
@Immutable
data class GlassTokens(
    /** Chips, tab bar, icon buttons. */
    val fill: Color,
    /** Panes carrying text and controls, which need more body to stay legible over a 3D scene. */
    val fillStrong: Color,
    /** Rim where the light catches the top edge. */
    val rimHigh: Color,
    /** Same rim around the middle, where the light has fallen off. */
    val rimLow: Color,
    /** A wash across the top of the pane, so it reads as glass over a flat backdrop too. */
    val sheen: Color,
    val shadow: Color,
    val content: Color,
    val contentMuted: Color,
    /** Filled pill behind a selected item. */
    val accent: Color,
    val onAccent: Color,
)

/** Dark chrome for the designer, warmed slightly to sit with the viewport's clay-brown ground. */
val StudioGlass = GlassTokens(
    fill         = Color(0xBF1D1815),
    fillStrong   = Color(0xF0181413),
    rimHigh      = Color(0x99FFFFFF),
    rimLow       = Color(0x0FFFFFFF),
    sheen        = Color(0x1AFFFFFF),
    shadow       = Color(0x99000000),
    content      = Color(0xFFF3EFEC),
    contentMuted = Color(0xFFA79E98),
    accent       = C.Moss80,
    onAccent     = C.Moss20,
)

/**
 * Light chrome. The rim inverts here: a lit white edge is invisible on a bone ground, so light mode
 * gets a soft dark hairline all round and leans on the shadow to lift the pane instead.
 */
val DayGlass = GlassTokens(
    fill         = Color(0xDEFFFFFF),
    fillStrong   = Color(0xF7FFFFFF),
    rimHigh      = Color(0x33000000),
    rimLow       = Color(0x12000000),
    sheen        = Color(0x99FFFFFF),
    shadow       = Color(0x33000000),
    content      = C.OnBone,
    contentMuted = C.NeutralVar30,
    accent       = C.Moss40,
    onAccent     = Color.White,
)

val LocalGlass = staticCompositionLocalOf { StudioGlass }

/**
 * What a glass pane may blur, when anything can be blurred at all.
 *
 * Only Compose content can be recorded into a backdrop layer, so a screen supplies one only where
 * that holds. Over the Filament viewport it stays null and [glass] falls back to tint and rim — the
 * viewport is a `SurfaceView` on its own compositor layer and its pixels never reach this pass.
 */
val LocalGlassBackdrop = staticCompositionLocalOf<Backdrop?> { null }

/**
 * Paints a glass pane behind the content.
 *
 * [strong] picks the heavier tint — use it for anything with readable text on it, since over an
 * arbitrary 3D scene the light tint alone will not hold contrast.
 */
@Composable
fun Modifier.glass(
    shape: Shape,
    tokens: GlassTokens,
    strong: Boolean = false,
    elevation: Dp = 10.dp,
    blurRadius: Dp = 24.dp,
): Modifier {
    val backdrop = LocalGlassBackdrop.current
    val radiusPx = with(LocalDensity.current) { blurRadius.toPx() }

    // Without RenderEffect the backdrop would be drawn sharp behind the pane, which reads worse than
    // no blur at all — so the fallback takes the whole path, not just the blur step.
    return if (backdrop != null && isRenderEffectSupported()) {
        this
            .shadow(elevation, shape, ambientColor = tokens.shadow, spotColor = tokens.shadow)
            // drawBackdrop does not clip its content the way shadow(clip = true) does, so the tab
            // indicator escaped past the pill's rounded end.
            .clip(shape)
            .drawBackdrop(
                backdrop = backdrop,
                shape = { shape },
                effects = { blur(radiusPx) },
                highlight = { null },
                shadow = { null },
                onDrawSurface = { drawGlassSurface(shape, tokens, strong) },
            )
    } else {
        this.glassTinted(shape, tokens, strong, elevation)
    }
}

/** Tint, sheen and rim with nothing sampled behind them. */
private fun Modifier.glassTinted(
    shape: Shape,
    tokens: GlassTokens,
    strong: Boolean,
    elevation: Dp,
): Modifier = this
    .shadow(elevation, shape, ambientColor = tokens.shadow, spotColor = tokens.shadow)
    .drawWithContent {
        drawGlassSurface(shape, tokens, strong)
        drawContent()
    }

/**
 * Tint, sheen and rim, painted over whatever is already there.
 *
 * Shared by both paths so the two never drift: on the blurred path it lands on top of the blurred
 * backdrop, on the fallback path on top of nothing.
 */
internal fun DrawScope.drawGlassSurface(shape: Shape, tokens: GlassTokens, strong: Boolean) {
    val outline = shape.createOutline(size, layoutDirection, this)
    val rimWidth = 1.25.dp.toPx()

    drawOutline(outline, if (strong) tokens.fillStrong else tokens.fill)
    drawOutline(
        outline,
        Brush.verticalGradient(0f to tokens.sheen, 0.55f to Color.Transparent),
    )
    drawOutline(
        outline,
        Brush.verticalGradient(
            0f to tokens.rimHigh,
            0.5f to tokens.rimLow,
            1f to tokens.rimHigh.copy(alpha = tokens.rimHigh.alpha * 0.55f),
        ),
        style = Stroke(rimWidth),
    )
}
