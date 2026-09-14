package com.interiordesign3d.ui.screen.designer.view

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.interiordesign3d.ui.theme.LocalInteriorAccents
import com.interiordesign3d.ui.screen.designer.DrawingPhase
import com.interiordesign3d.ui.screen.designer.PlacementTool
import com.interiordesign3d.ui.screen.designer.state.DesignerState

@Composable
fun FloorPlanToolbar(state: DesignerState, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 3.dp,
        shadowElevation = 6.dp,
    ) {
        Column(Modifier.navigationBarsPadding()) {
            AnimatedVisibility(visible = state.drawingPhase == DrawingPhase.EDITING) {
                OpeningToolRow(state)
            }
            ActionRow(state)
        }
    }
}

@Composable
private fun OpeningToolRow(state: DesignerState) {
    val accents = LocalInteriorAccents.current

    Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp)) {
        CenterRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OpeningChip(
                label = stringResource(R.string.door),
                icon = Icons.Outlined.DoorFront,
                accent = accents.door,
                selected = state.placementTool == PlacementTool.DOOR,
                onClick = { state.onToolChange(state.placementTool.toggled(PlacementTool.DOOR)) },
            )
            OpeningChip(
                label = stringResource(R.string.window),
                icon = Icons.Outlined.Window,
                accent = accents.window,
                selected = state.placementTool == PlacementTool.WINDOW,
                onClick = { state.onToolChange(state.placementTool.toggled(PlacementTool.WINDOW)) },
            )
        }
        AnimatedVisibility(visible = state.placementTool != PlacementTool.NONE) {
            Text(
                stringResource(R.string.opening_hint),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    }
}

private fun PlacementTool.toggled(target: PlacementTool) =
    if (this == target) PlacementTool.NONE else target

@Composable
private fun OpeningChip(
    label: String,
    icon: ImageVector,
    accent: Color,
    selected: Boolean,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        modifier = Modifier.height(MinTouchTarget),
        label = { Text(label, style = MaterialTheme.typography.labelLarge) },
        leadingIcon = {
            Icon(icon, null, Modifier.size(18.dp), tint = if (selected) accent else MaterialTheme.colorScheme.onSurfaceVariant)
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = accent.copy(alpha = 0.16f),
            selectedLabelColor = accent,
        ),
    )
}

@Composable
private fun ActionRow(state: DesignerState) {
    CenterRow(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AnimatedVisibility(visible = state.drawingPhase == DrawingPhase.CLOSED) {
            Button(
                onClick = state::onDone,
                modifier = Modifier.height(MinTouchTarget),
            ) {
                Icon(Icons.Outlined.Check, null, Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.done))
            }
        }

        OutlinedButton(
            onClick = state::onUndo,
            enabled = state.currentPath.isNotEmpty() || state.drawingPhase == DrawingPhase.CLOSED,
            modifier = Modifier.weight(1f).height(MinTouchTarget),
        ) {
            Icon(Icons.AutoMirrored.Outlined.Undo, null, Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(stringResource(R.string.undo))
        }

        OutlinedButton(
            onClick = state::onClear,
            enabled = state.hasRooms || state.currentPath.isNotEmpty(),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error,
            ),
            modifier = Modifier.weight(1f).height(MinTouchTarget),
        ) {
            Icon(Icons.Outlined.DeleteSweep, null, Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(stringResource(R.string.clear))
        }

        FilterChip(
            selected = state.snapEnabled,
            onClick = state::onToggleSnap,
            modifier = Modifier.height(MinTouchTarget),
            label = { Text(stringResource(R.string.snap_to_grid)) },
            leadingIcon = { Icon(Icons.Outlined.GridOn, null, Modifier.size(16.dp)) },
        )
    }
}
