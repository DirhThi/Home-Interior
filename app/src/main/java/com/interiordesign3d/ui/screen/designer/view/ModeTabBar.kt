package com.interiordesign3d.ui.screen.designer.view

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Architecture
import androidx.compose.material.icons.outlined.Chair
import androidx.compose.material.icons.outlined.Home
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.interiordesign3d.R
import com.interiordesign3d.ui.properties.GlassTab
import com.interiordesign3d.ui.properties.GlassTabBar
import com.interiordesign3d.ui.properties.GlassTabBarHeight
import com.interiordesign3d.ui.screen.designer.EditorMode
import com.interiordesign3d.ui.screen.designer.state.DesignerState

/** Height the bar occupies, so the chrome above it can leave room. */
val ModeTabBarHeight = GlassTabBarHeight

private data class ModeTab(val mode: EditorMode, val icon: ImageVector, val label: Int)

private val TABS = listOf(
    ModeTab(EditorMode.DRAW_WALLS, Icons.Outlined.Architecture, R.string.mode_plan),
    ModeTab(EditorMode.DESIGN, Icons.Outlined.Chair, R.string.mode_interior),
    ModeTab(EditorMode.EXTERIOR, Icons.Outlined.Home, R.string.mode_exterior),
)

/**
 * The designer's three editor modes. They were previously two unlabelled icon buttons in the top
 * bar, which gave no sense of where you were or what else existed.
 */
@Composable
fun ModeTabBar(state: DesignerState, modifier: Modifier = Modifier) {
    GlassTabBar(
        tabs = TABS.map { tab ->
            GlassTab(
                label = stringResource(tab.label),
                icon = tab.icon,
                // Nothing to look at inside or outside until at least one room exists.
                enabled = tab.mode == EditorMode.DRAW_WALLS || state.hasRooms,
            )
        },
        selectedIndex = TABS.indexOfFirst { it.mode == state.editorMode }.coerceAtLeast(0),
        onSelect = { state.onModeChange(TABS[it].mode) },
        modifier = modifier,
    )
}
