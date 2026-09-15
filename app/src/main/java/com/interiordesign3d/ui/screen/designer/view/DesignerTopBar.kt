package com.interiordesign3d.ui.screen.designer.view

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.interiordesign3d.R
import com.interiordesign3d.data.plans.areaM2
import com.interiordesign3d.ui.properties.GlassIconButton
import com.interiordesign3d.ui.properties.GlassPane
import com.interiordesign3d.ui.screen.designer.EditorMode
import com.interiordesign3d.ui.screen.designer.PlacementTool
import com.interiordesign3d.ui.screen.designer.state.DesignerState
import com.interiordesign3d.ui.theme.LocalGlass

/**
 * Back, and nothing else. Mode switching moved to [ModeTabBar], surfaces to the action clusters, and
 * Save is gone because `observePlanForAutoSave` writes the plan 400 ms after the last edit.
 */
@Composable
fun DesignerBackButton(state: DesignerState, modifier: Modifier = Modifier) {
    GlassIconButton(
        icon = Icons.AutoMirrored.Outlined.ArrowBack,
        contentDescription = stringResource(R.string.back),
        modifier = modifier,
        onClick = state::onBack,
    )
}

/**
 * The one running readout: what to do next, or the plan's size once it has rooms.
 *
 * It absorbs what `FloorPlanCanvas` used to draw for itself. Two hint chips fought for the top of the
 * screen once the canvas went full-bleed, and the canvas's own one had no status-bar inset to give.
 */
@Composable
fun DesignerHint(state: DesignerState, modifier: Modifier = Modifier) {
    val glass = LocalGlass.current

    val rooms = state.floorPlan.rooms.size
    val remaining = 3 - state.currentPath.size
    val text = when {
        state.editorMode == EditorMode.EXTERIOR -> stringResource(R.string.hint_exterior)
        state.editorMode == EditorMode.DESIGN -> stringResource(R.string.hint_design)
        state.placementTool != PlacementTool.NONE -> stringResource(R.string.opening_hint)
        state.currentPath.size >= 3 -> stringResource(R.string.plan_hint_close)
        state.currentPath.isNotEmpty() ->
            pluralStringResource(R.plurals.plan_hint_more, remaining, remaining)
        rooms > 0 -> pluralStringResource(
            R.plurals.plan_rooms_area, rooms, rooms, state.floorPlan.areaM2()
        )
        else -> stringResource(R.string.plan_hint_empty)
    }

    GlassPane(modifier, shape = CircleShape, elevation = 6.dp) {
        Crossfade(text, label = "hint") { line ->
            Text(
                line,
                style = MaterialTheme.typography.labelMedium,
                color = glass.contentMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
            )
        }
    }
}
