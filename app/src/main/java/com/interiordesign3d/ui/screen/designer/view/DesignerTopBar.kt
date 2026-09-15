package com.interiordesign3d.ui.screen.designer.view

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MeetingRoom
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Wallpaper
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.interiordesign3d.R
import com.interiordesign3d.ui.screen.designer.DrawingPhase
import com.interiordesign3d.ui.screen.designer.EditorMode
import com.interiordesign3d.ui.screen.designer.state.DesignerState

@Composable
fun DesignerTopBar(state: DesignerState) {
    val outside = state.editorMode == EditorMode.EXTERIOR
    val inDesign = state.editorMode == EditorMode.DESIGN || outside
    val subtitle = when {
        outside -> R.string.hint_exterior
        inDesign -> R.string.hint_design
        state.drawingPhase == DrawingPhase.PLACING -> R.string.hint_placing
        state.drawingPhase == DrawingPhase.CLOSED -> R.string.hint_closed
        else -> R.string.hint_editing
    }

    TopAppBar(
        title = {
            Column {
                Text(
                    stringResource(
                        when {
                            outside -> R.string.exterior_mode
                            inDesign -> R.string.design_mode
                            else -> R.string.floor_plan
                        }
                    ),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    stringResource(subtitle),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = state::onBack) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, stringResource(R.string.back))
            }
        },
        actions = {
            if (inDesign) {
                IconButton(onClick = state::onToggleExterior) {
                    Icon(
                        if (outside) Icons.Outlined.MeetingRoom else Icons.Outlined.Home,
                        stringResource(if (outside) R.string.go_inside else R.string.go_outside),
                    )
                }
                IconButton(onClick = state::onEditFloorPlan) {
                    Icon(Icons.Outlined.EditNote, stringResource(R.string.edit_floor_plan))
                }
                IconButton(onClick = state::onShowSurfaceSheet) {
                    Icon(Icons.Outlined.Wallpaper, stringResource(R.string.surfaces))
                }
                IconButton(onClick = state::onSave) {
                    Icon(Icons.Outlined.Save, stringResource(R.string.save))
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
        ),
    )
}
