package com.interiordesign3d.ui.screen.designer.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.DoorFront
import androidx.compose.material.icons.outlined.GridOn
import androidx.compose.material.icons.outlined.Window
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalIconToggleButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.interiordesign3d.R
import com.interiordesign3d.ui.properties.CenterRow
import com.interiordesign3d.ui.properties.MinTouchTarget
import com.interiordesign3d.ui.screen.designer.DrawingPhase
import com.interiordesign3d.ui.screen.designer.PlacementTool
import com.interiordesign3d.ui.screen.designer.state.DesignerState
import com.interiordesign3d.ui.theme.LocalInteriorAccents

@Composable
fun FloorPlanToolbar(state: DesignerState, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.navigationBarsPadding().padding(horizontal = 8.dp, vertical = 6.dp)) {
            AnimatedVisibility(visible = state.drawingPhase == DrawingPhase.EDITING) {
                OpeningToolRow(state)
            }
            ActionRow(state)
        }
    }
}

/** Door / window are modes, not one-shot actions, so they stay as toggles on their own line. */
@Composable
private fun OpeningToolRow(state: DesignerState) {
    val accents = LocalInteriorAccents.current

    CenterRow(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OpeningChip(
            icon = Icons.Outlined.DoorFront,
            label = stringResource(R.string.door),
            accent = accents.door,
            checked = state.placementTool == PlacementTool.DOOR,
            onCheck = { state.onToolChange(state.placementTool.toggled(PlacementTool.DOOR)) },
        )
        OpeningChip(
            icon = Icons.Outlined.Window,
            label = stringResource(R.string.window),
            accent = accents.window,
            checked = state.placementTool == PlacementTool.WINDOW,
            onCheck = { state.onToolChange(state.placementTool.toggled(PlacementTool.WINDOW)) },
        )
        AnimatedVisibility(visible = state.placementTool != PlacementTool.NONE) {
            Text(
                stringResource(R.string.opening_hint),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun PlacementTool.toggled(target: PlacementTool) =
    if (this == target) PlacementTool.NONE else target

/** Labelled, because two bare icons on the toolbar gave no hint that they were placement modes. */
@Composable
private fun OpeningChip(
    icon: ImageVector,
    label: String,
    accent: Color,
    checked: Boolean,
    onCheck: () -> Unit,
) {
    FilterChip(
        selected = checked,
        onClick = onCheck,
        modifier = Modifier.height(MinTouchTarget),
        leadingIcon = { Icon(icon, null, Modifier.size(18.dp)) },
        label = { Text(label, style = MaterialTheme.typography.labelLarge) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = accent.copy(alpha = 0.18f),
            selectedLabelColor = accent,
            selectedLeadingIconColor = accent,
        ),
    )
}

/**
 * Icon-only Undo / Clear / Snap. Labelled buttons used to share the row with Done under
 * `weight(1f)`, which squeezed them until the text wrapped and clipped.
 */
@Composable
private fun ActionRow(state: DesignerState) {
    CenterRow(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        IconButton(
            onClick = state::onUndo,
            enabled = state.currentPath.isNotEmpty() || state.drawingPhase == DrawingPhase.CLOSED,
            modifier = Modifier.size(MinTouchTarget),
        ) {
            Icon(Icons.AutoMirrored.Outlined.Undo, stringResource(R.string.undo))
        }

        IconButton(
            onClick = state::onClear,
            enabled = state.hasRooms || state.currentPath.isNotEmpty(),
            modifier = Modifier.size(MinTouchTarget),
        ) {
            Icon(Icons.Outlined.DeleteSweep, stringResource(R.string.clear))
        }

        FilledTonalIconToggleButton(
            checked = state.snapEnabled,
            onCheckedChange = { state.onToggleSnap() },
            modifier = Modifier.size(MinTouchTarget),
        ) {
            Icon(Icons.Outlined.GridOn, stringResource(R.string.snap_to_grid), Modifier.size(20.dp))
        }

        Spacer(Modifier.weight(1f))

        AnimatedVisibility(visible = state.drawingPhase == DrawingPhase.CLOSED) {
            Button(onClick = state::onDone, modifier = Modifier.height(MinTouchTarget)) {
                Icon(Icons.Outlined.Check, null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.done))
            }
        }
    }
}
