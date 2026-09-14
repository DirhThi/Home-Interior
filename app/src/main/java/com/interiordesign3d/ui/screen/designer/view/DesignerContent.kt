package com.interiordesign3d.ui.screen.designer.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.Chair
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.interiordesign3d.R
import com.interiordesign3d.common.base.BaseScreen
import com.interiordesign3d.ui.screen.designer.DrawingPhase
import com.interiordesign3d.ui.screen.designer.EditorMode
import com.interiordesign3d.ui.screen.designer.state.DesignerState
import com.interiordesign3d.ui.theme.LocalInteriorAccents
import com.interiordesign3d.ui.screen.designer.view.viewport.FilamentRoomViewport
import com.interiordesign3d.ui.screen.designer.view.viewport.WallDrawingCanvas

@Composable
fun DesignerContent(
    state: DesignerState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    BaseScreen(
        loading = state.loading,
        snackbarHostState = snackbarHostState,
        topBar = { DesignerTopBar(state) },
        bottomBar = {
            if (state.editorMode == EditorMode.DRAW_WALLS) FloorPlanToolbar(state)
        },
        floatingActionButton = { DesignerFab(state) },
        contentWindowInsets = WindowInsets(0),
    ) { modifier ->
        when (state.editorMode) {
            EditorMode.DRAW_WALLS -> PlanEditor(state, modifier)
            EditorMode.DESIGN -> RoomDesignView(state, modifier)
        }
    }

    if (state.showAddFurnitureSheet) {
        AddFurnitureSheet(
            onAdd = state::onAddFurniture,
            onDismiss = state::onDismissAddFurniture,
        )
    }

    if (state.showSurfaceSheet) {
        SurfaceSheet(state = state, onDismiss = state::onDismissSurfaceSheet)
    }
}

@Composable
private fun DesignerFab(state: DesignerState) {
    when (state.editorMode) {
        // Hidden while an opening is selected: it would float over that panel's controls.
        EditorMode.DRAW_WALLS -> AnimatedVisibility(
            visible = state.hasRooms && state.selectedOpening == null,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut(),
        ) {
            ExtendedFloatingActionButton(
                modifier = Modifier.navigationBarsPadding(),
                onClick = state::onEnterDesign,
                icon = { Icon(Icons.Outlined.Chair, null) },
                text = { Text(stringResource(R.string.design_room)) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            )
        }

        // Hidden while an item is selected: it would float over the control panel.
        EditorMode.DESIGN -> AnimatedVisibility(
            visible = state.selectedItem == null && state.selectedOpening == null,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut(),
        ) {
            FloatingActionButton(
                modifier = Modifier.navigationBarsPadding(),
                onClick = state::onShowAddFurniture,
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onTertiary,
            ) {
                Icon(Icons.Outlined.AddCircle, stringResource(R.string.add_furniture))
            }
        }
    }
}

@Composable
private fun PlanEditor(state: DesignerState, modifier: Modifier) {
    Box(modifier.fillMaxSize()) {
    WallDrawingCanvas(
        floorPlan = state.floorPlan,
        drawingPhase = state.drawingPhase,
        currentPath = state.currentPath,
        gridSizeCm = if (state.snapEnabled) 10f else 0f,
        placementTool = state.placementTool,
        onAddNewPoint = state::onAddNewPoint,
        onSnapToNode = state::onSnapToNode,
        onClosePath = state::onClosePath,
        onMoveNode = state::onMoveNode,
        onStartFromNode = state::onStartFromNode,
        onStartNewPoint = state::onStartNewPoint,
        onPlaceOpening = state::onPlaceOpening,
        onMoveOpening = state::onMoveOpening,
        onResizeOpening = state::onResizeOpening,
        onRemoveOpening = state::onRemoveOpening,
        onTapOpening = { state.onSelectOpening(it) },
        placedFurniture = state.placedFurniture,
        onMoveFurnitureInPlan = state::onMoveFurniture,
        onTapFurniture = state::onSelectFurniture,
        modifier = Modifier.fillMaxSize(),
    )

    AnimatedVisibility(
        visible = state.selectedOpening != null,
        enter = slideInVertically { it },
        exit = slideOutVertically { it },
        modifier = Modifier.align(Alignment.BottomCenter),
    ) {
        state.selectedOpening?.let { OpeningControlPanel(opening = it, state = state) }
    }
    }
}

@Composable
private fun RoomDesignView(state: DesignerState, modifier: Modifier) {
    Box(modifier.fillMaxSize()) {
        FilamentRoomViewport(
            floorPlan = state.floorPlan,
            roomPolygons = state.roomPolygons,
            placedFurniture = state.placedFurniture,
            roomHeight = state.roomHeightCm,
            wallModel = state.wallPreset.model,
            wallColorHex = state.wallColorHex,
            wallTileM = state.wallPreset.tileM,
            floorModel = state.floorPreset.model,
            floorColorHex = state.floorPreset.colorHex,
            floorTileM = state.floorPreset.tileM,
            shadows = state.shadowsOn,
            autoHideWalls = state.autoHideWalls,
            backgroundColor = LocalInteriorAccents.current.viewportBackground,
            onDropOpening = state::onDropOpening,
            onSelectFurniture = state::onSelectFurniture,
            onSelectOpening = { state.onSelectOpening(it) },
            onMoveFurniture = state::onMoveFurniture,
            modifier = Modifier.fillMaxSize(),
        )

        AnimatedVisibility(
            visible = state.selectedItem != null || state.selectedOpening != null,
            enter = slideInVertically { it },
            exit = slideOutVertically { it },
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            state.selectedItem?.let { FurnitureControlPanel(item = it, state = state) }
                ?: state.selectedOpening?.let { OpeningControlPanel(opening = it, state = state) }
        }
    }
}
