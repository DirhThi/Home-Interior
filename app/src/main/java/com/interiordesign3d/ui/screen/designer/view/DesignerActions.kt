package com.interiordesign3d.ui.screen.designer.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material.icons.outlined.Balcony
import androidx.compose.material.icons.outlined.Chair
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Construction
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.DoorFront
import androidx.compose.material.icons.outlined.GridOn
import androidx.compose.material.icons.outlined.Stairs
import androidx.compose.material.icons.outlined.Wallpaper
import androidx.compose.material.icons.outlined.Window
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.interiordesign3d.R
import com.interiordesign3d.ui.properties.GlassIconButton
import com.interiordesign3d.ui.properties.GlassPillButton
import com.interiordesign3d.ui.screen.designer.DrawingPhase
import com.interiordesign3d.ui.screen.designer.PlacementTool
import com.interiordesign3d.ui.screen.designer.state.DesignerState
import com.interiordesign3d.ui.theme.LocalInteriorAccents

private const val STAGGER_MS = 40
private const val ENTER_MS = 170

/**
 * Floating action clusters, one per editor mode. These replace `FloorPlanToolbar`, a full-width
 * opaque bar that held four chips and four buttons and took permanent height off the canvas.
 */

private data class ToolSpec(
    val tool: PlacementTool,
    val icon: ImageVector,
    val label: Int,
    val accent: Color,
)

/**
 * Door / window / stairs / balcony are placement *modes*, so they stay visible once armed — the
 * trigger wears the armed tool's own icon while collapsed, which is why four controls can fold into
 * one without hiding state.
 */
@Composable
fun PlanToolRail(state: DesignerState, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    val accents = LocalInteriorAccents.current
    val armed = state.placementTool

    val tools = listOf(
        ToolSpec(PlacementTool.DOOR, Icons.Outlined.DoorFront, R.string.door, accents.door),
        ToolSpec(PlacementTool.WINDOW, Icons.Outlined.Window, R.string.window, accents.window),
        ToolSpec(PlacementTool.STAIRS, Icons.Outlined.Stairs, R.string.stairs, MaterialTheme.colorScheme.primary),
        ToolSpec(PlacementTool.BALCONY, Icons.Outlined.Balcony, R.string.balcony, MaterialTheme.colorScheme.tertiary),
    )
    val armedSpec = tools.firstOrNull { it.tool == armed }

    Column(
        modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.End,
    ) {
        tools.forEachIndexed { i, spec ->
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(tween(ENTER_MS, delayMillis = i * STAGGER_MS)) +
                    scaleIn(tween(ENTER_MS, delayMillis = i * STAGGER_MS), initialScale = 0.6f),
                exit = fadeOut(tween(100)) + scaleOut(tween(100), targetScale = 0.6f),
            ) {
                GlassIconButton(
                    icon = spec.icon,
                    contentDescription = stringResource(spec.label),
                    selected = armed == spec.tool,
                    accent = spec.accent,
                    onClick = {
                        state.onToolChange(if (armed == spec.tool) PlacementTool.NONE else spec.tool)
                        expanded = false
                    },
                )
            }
        }

        GlassIconButton(
            icon = when {
                expanded -> Icons.Outlined.Close
                armedSpec != null -> armedSpec.icon
                else -> Icons.Outlined.Construction
            },
            contentDescription = stringResource(if (expanded) R.string.close_tools else R.string.tools),
            primary = true,
            accent = armedSpec?.accent,
            size = 56.dp,
            iconSize = 24.dp,
            onClick = { expanded = !expanded },
        )
    }
}

/** Undo / clear / snap. Secondary to placing things, so they sit opposite the tool rail. */
@Composable
fun PlanEditCluster(state: DesignerState, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        GlassIconButton(
            icon = Icons.AutoMirrored.Outlined.Undo,
            contentDescription = stringResource(R.string.undo),
            enabled = state.currentPath.isNotEmpty() || state.drawingPhase == DrawingPhase.CLOSED,
            onClick = state::onUndo,
        )
        GlassIconButton(
            icon = Icons.Outlined.DeleteSweep,
            contentDescription = stringResource(R.string.clear),
            enabled = state.hasRooms || state.currentPath.isNotEmpty(),
            onClick = state::onClear,
        )
        GlassIconButton(
            icon = Icons.Outlined.GridOn,
            contentDescription = stringResource(R.string.snap_to_grid),
            selected = state.snapEnabled,
            onClick = state::onToggleSnap,
        )
    }
}

/** Inside: add furniture is the one primary action, surfaces sits under it. */
@Composable
fun DesignActions(state: DesignerState, modifier: Modifier = Modifier) {
    Column(
        modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.End,
    ) {
        GlassIconButton(
            icon = Icons.Outlined.Wallpaper,
            contentDescription = stringResource(R.string.surfaces),
            onClick = state::onShowSurfaceSheet,
        )
        GlassIconButton(
            icon = Icons.Outlined.Chair,
            contentDescription = stringResource(R.string.add_furniture),
            primary = true,
            size = 56.dp,
            iconSize = 24.dp,
            onClick = state::onShowAddFurniture,
        )
    }
}

/** Outside: nothing to place, only finishes. */
@Composable
fun ExteriorActions(state: DesignerState, modifier: Modifier = Modifier) {
    GlassIconButton(
        icon = Icons.Outlined.Wallpaper,
        contentDescription = stringResource(R.string.surfaces),
        modifier = modifier,
        onClick = state::onShowSurfaceSheet,
    )
}

/** Only while a freshly drawn polygon is waiting to be committed. */
@Composable
fun DoneButton(state: DesignerState, modifier: Modifier = Modifier) {
    GlassPillButton(
        icon = Icons.Outlined.Check,
        label = stringResource(R.string.done),
        modifier = modifier,
        onClick = state::onDone,
    )
}
