package com.interiordesign3d.ui.properties

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Android's minimum comfortable touch target. */
val MinTouchTarget = 48.dp

fun Modifier.rounded(radius: Dp) = clip(RoundedCornerShape(radius))

/** Clickable with no ripple — for swatches and canvas chrome where the ripple reads as a glitch. */
@Composable
fun Modifier.onClickNotRipple(enabled: Boolean = true, onClick: () -> Unit): Modifier {
    val interaction = remember { MutableInteractionSource() }
    return clickable(
        interactionSource = interaction,
        indication = null,
        enabled = enabled,
        onClick = onClick,
    )
}

/** Keeps a control tappable at 48dp even when it is drawn smaller. */
fun Modifier.touchTarget(size: Dp = MinTouchTarget) = defaultMinSize(size, size)
