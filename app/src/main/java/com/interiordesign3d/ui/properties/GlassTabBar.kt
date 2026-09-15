package com.interiordesign3d.ui.properties

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.interiordesign3d.ui.theme.LocalGlass
import com.interiordesign3d.ui.theme.glass

private val TAB_HEIGHT = 54.dp
private val BAR_MAX_WIDTH = 360.dp
private const val DISABLED_ALPHA = 0.35f

/** Height the bar occupies, so content above it can leave room. */
val GlassTabBarHeight = TAB_HEIGHT + 8.dp

@Immutable
data class GlassTab(val label: String, val icon: ImageVector, val enabled: Boolean = true)

/**
 * The app's one tab bar: a floating glass pill with the current tab filled.
 *
 * Used both for the app's top-level destinations and for the designer's three editor modes — they
 * are the same gesture at different depths, and having them look the same is the point.
 *
 * Cell width comes from the bar rather than a constant: at a large font scale fixed cells clip their
 * labels, while a bar sized to its text runs off a narrow screen.
 */
@Composable
fun GlassTabBar(
    tabs: List<GlassTab>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val glass = LocalGlass.current

    BoxWithConstraints(
        modifier
            .widthIn(max = BAR_MAX_WIDTH)
            .glass(CircleShape, glass, elevation = 14.dp)
            .padding(4.dp)
            .semantics { role = Role.Tab },
    ) {
        val tabWidth = maxWidth / tabs.size
        val indicatorX by animateDpAsState(
            targetValue = tabWidth * selectedIndex.coerceIn(0, tabs.lastIndex),
            animationSpec = spring(Spring.DampingRatioLowBouncy, Spring.StiffnessMediumLow),
            label = "tabIndicator",
        )

        Box(
            Modifier
                .offset(x = indicatorX)
                .size(tabWidth, TAB_HEIGHT)
                .background(glass.accent, CircleShape)
        )

        Row {
            tabs.forEachIndexed { i, tab ->
                GlassTabItem(
                    tab = tab,
                    width = tabWidth,
                    selected = i == selectedIndex,
                    onClick = { onSelect(i) },
                )
            }
        }
    }
}

@Composable
private fun GlassTabItem(tab: GlassTab, width: Dp, selected: Boolean, onClick: () -> Unit) {
    val glass = LocalGlass.current
    val tint = when {
        !tab.enabled -> glass.content.copy(alpha = DISABLED_ALPHA)
        selected -> glass.onAccent
        else -> glass.contentMuted
    }

    Column(
        Modifier
            .size(width, TAB_HEIGHT)
            .onClickNotRipple(enabled = tab.enabled, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(9.dp))
        Icon(tab.icon, null, Modifier.size(20.dp), tint = tint)
        Spacer(Modifier.height(3.dp))
        Text(
            tab.label,
            style = MaterialTheme.typography.labelSmall,
            color = tint,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
