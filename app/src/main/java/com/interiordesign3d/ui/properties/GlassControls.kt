package com.interiordesign3d.ui.properties

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.interiordesign3d.ui.theme.GlassTokens
import com.interiordesign3d.ui.theme.LocalGlass
import com.interiordesign3d.ui.theme.glass

private const val DISABLED_ALPHA = 0.35f

/** A settle on press. Scale only, so nothing around the control moves. */
@Composable
private fun pressScale(interaction: MutableInteractionSource): Float {
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.93f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessHigh),
        label = "pressScale",
    )
    return scale
}

/** A floating glass pane. Everything the designer draws over the viewport goes through this. */
@Composable
fun GlassPane(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.extraLarge,
    strong: Boolean = false,
    elevation: Dp = 10.dp,
    tokens: GlassTokens = LocalGlass.current,
    content: @Composable () -> Unit,
) {
    Box(modifier.glass(shape, tokens, strong, elevation)) {
        CompositionLocalProvider(LocalContentColor provides tokens.content, content = content)
    }
}

/**
 * Round glass button with three weights, so a screen reads at a glance:
 * [primary] is solid accent and there is at most one per screen; [selected] is a tonal wash for an
 * armed tool or an on toggle; plain is glass. Keeping a toggle like snap-to-grid off the solid fill
 * is the point — otherwise the loudest control on the plan editor is a minor preference.
 */
@Composable
fun GlassIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    selected: Boolean = false,
    primary: Boolean = false,
    accent: Color? = null,
    size: Dp = MinTouchTarget,
    iconSize: Dp = 21.dp,
) {
    val glass = LocalGlass.current
    val interaction = remember { MutableInteractionSource() }
    val scale = pressScale(interaction)
    val fillColor = accent ?: glass.accent

    Box(
        modifier
            .size(size)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .then(
                when {
                    primary -> Modifier.shadow(10.dp, CircleShape).background(fillColor)
                    selected -> Modifier
                        .glass(CircleShape, glass, elevation = 8.dp)
                        .background(fillColor.copy(alpha = 0.24f), CircleShape)
                    else -> Modifier.glass(CircleShape, glass, elevation = 8.dp)
                }
            )
            .clickable(
                interactionSource = interaction,
                indication = LocalIndication.current,
                enabled = enabled,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            icon,
            contentDescription,
            Modifier.size(iconSize),
            tint = when {
                !enabled -> glass.content.copy(alpha = DISABLED_ALPHA)
                primary -> glass.onAccent
                selected -> fillColor
                else -> glass.content
            },
        )
    }
}

/** Icon plus label, for the one primary action a screen is allowed. */
@Composable
fun GlassPillButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    filled: Boolean = true,
) {
    val glass = LocalGlass.current
    val interaction = remember { MutableInteractionSource() }
    val scale = pressScale(interaction)
    val content = if (filled) glass.onAccent else glass.content

    Row(
        modifier
            .height(MinTouchTarget)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .then(
                if (filled) Modifier.shadow(12.dp, CircleShape).background(glass.accent)
                else Modifier.glass(CircleShape, glass, elevation = 8.dp)
            )
            .clickable(
                interactionSource = interaction,
                indication = LocalIndication.current,
                onClick = onClick,
            )
            .padding(horizontal = 18.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, Modifier.size(19.dp), tint = content)
        Spacer(Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.labelLarge, color = content)
    }
}

// ─── Controls that live *on* a glass pane ─────────────────────────────────────
//
// Glass inside glass goes muddy and flattens the hierarchy, so nothing below paints another pane.
// They are tonal: a wash of the pane's own content colour, with the accent reserved for selection.

private const val TRACK_ALPHA = 0.08f
private const val TONAL_ALPHA = 0.12f
private val PILL_HEIGHT = 44.dp

@Immutable
data class Segment(val label: String, val icon: ImageVector? = null)

/**
 * The app's one selection idiom: a recessed track with the current option filled.
 *
 * Replaces the `FilterChip` rows and `TabRow`s that were scattered through the panels and sheets —
 * three different Material components were being used to express the same "pick one of these".
 */
@Composable
fun SegmentedPills(
    segments: List<Segment>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val glass = LocalGlass.current

    Box(
        modifier
            .clip(CircleShape)
            .background(glass.content.copy(alpha = TRACK_ALPHA))
            .padding(3.dp)
    ) {
        Row(Modifier.fillMaxWidth()) {
            segments.forEachIndexed { i, segment ->
                val selected = i == selectedIndex
                Row(
                    Modifier
                        .weight(1f)
                        .height(PILL_HEIGHT)
                        .clip(CircleShape)
                        .then(if (selected) Modifier.background(glass.accent) else Modifier)
                        .clickable { onSelect(i) },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val tint = if (selected) glass.onAccent else glass.contentMuted
                    segment.icon?.let {
                        Icon(it, null, Modifier.size(17.dp), tint = tint)
                        Spacer(Modifier.width(6.dp))
                    }
                    Text(
                        segment.label,
                        style = MaterialTheme.typography.labelLarge,
                        color = tint,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

/** A standalone pill, for rows that scroll or where the options are not a fixed small set. */
@Composable
fun ChoiceChip(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    accent: Color? = null,
    onClick: () -> Unit,
) {
    val glass = LocalGlass.current
    val fill = accent ?: glass.accent
    val tint = if (selected) glass.onAccent else glass.content

    Row(
        modifier
            .height(MinTouchTarget)
            .clip(CircleShape)
            .background(if (selected) fill else glass.content.copy(alpha = TONAL_ALPHA))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icon?.let {
            Icon(it, null, Modifier.size(17.dp), tint = tint)
            Spacer(Modifier.width(6.dp))
        }
        Text(label, style = MaterialTheme.typography.labelLarge, color = tint, maxLines = 1)
    }
}

/** A number that opens its editor — "Pitch 30°". Was a bare `TextButton`, which read as a link. */
@Composable
fun ValueChip(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) =
    ChoiceChip(label = label, selected = false, onClick = onClick, modifier = modifier)

/** Close / delete inside a panel. [danger] is the only place the error colour appears. */
@Composable
fun PanelIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    danger: Boolean = false,
) {
    val glass = LocalGlass.current
    val tint = if (danger) MaterialTheme.colorScheme.error else glass.content

    Box(
        modifier
            .size(MinTouchTarget)
            .clip(CircleShape)
            .background(tint.copy(alpha = TONAL_ALPHA))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription, Modifier.size(19.dp), tint = tint)
    }
}
