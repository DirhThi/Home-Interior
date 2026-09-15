package com.interiordesign3d.ui.screen.designer.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.interiordesign3d.ui.screen.designer.DrawingPhase
import com.interiordesign3d.ui.screen.designer.EditorMode
import com.interiordesign3d.ui.screen.designer.state.DesignerState
import com.interiordesign3d.ui.screen.designer.view.viewport.FilamentRoomViewport
import com.interiordesign3d.ui.screen.designer.view.viewport.WallDrawingCanvas
import com.interiordesign3d.ui.theme.LocalInteriorAccents

/** Gap the bottom chrome leaves for the tab bar. */
private val PANEL_BOTTOM_GAP = ModeTabBarHeight + 10.dp

/** Clears the back button / level switcher row above it. */
private val HINT_TOP = 68.dp
private const val EXIT_MS = 130
private const val ENTER_MS = 220

/**
 * One full-bleed viewport with chrome floating over it. There is no Scaffold: top and bottom bars
 * would each claim permanent height from the thing the screen exists to show.
 *
 * Tool clusters sit at the vertical centre of each edge, not above the tab bar — that is what keeps
 * them clear of the control panels, and is why the panels no longer have to hide the primary action.
 */
@Composable
fun DesignerContent(
    state: DesignerState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (state.editorMode) {
            EditorMode.DRAW_WALLS -> PlanEditor(state)
            EditorMode.DESIGN, EditorMode.EXTERIOR -> RoomDesignView(state)
        }

        if (state.loading) {
            CircularProgressIndicator(
                Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.primary,
            )
        }

        DesignerBackButton(
            state,
            Modifier.align(Alignment.TopStart).statusBarsPadding().padding(12.dp),
        )

        // Outside there is only one storey to look at — the whole house.
        if (state.editorMode != EditorMode.EXTERIOR) {
            LevelSwitcher(
                state,
                Modifier.align(Alignment.TopEnd).statusBarsPadding().padding(12.dp),
            )
        }

        // Below the back row, so it can never be squeezed by what sits either side of it.
        DesignerHint(
            state,
            Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(horizontal = 12.dp)
                .padding(top = HINT_TOP),
        )

        ActionClusters(state)
        ControlPanels(state)

        ModeTabBar(
            state,
            Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 8.dp),
        )

        SnackbarHost(
            snackbarHostState,
            Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = PANEL_BOTTOM_GAP, start = 12.dp, end = 12.dp),
        )
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
private fun BoxScope.ActionClusters(state: DesignerState) {
    val edge = Modifier.padding(horizontal = 12.dp)

    when (state.editorMode) {
        EditorMode.DRAW_WALLS -> {
            PlanEditCluster(state, edge.align(Alignment.CenterStart))
            AnimatedVisibility(
                visible = state.drawingPhase == DrawingPhase.EDITING,
                enter = fadeIn() + scaleIn(initialScale = 0.8f),
                exit = fadeOut() + scaleOut(targetScale = 0.8f),
                modifier = edge.align(Alignment.CenterEnd),
            ) {
                PlanToolRail(state)
            }
            AnimatedVisibility(
                visible = state.drawingPhase == DrawingPhase.CLOSED,
                enter = fadeIn() + scaleIn(initialScale = 0.8f),
                exit = fadeOut() + scaleOut(targetScale = 0.8f),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = PANEL_BOTTOM_GAP),
            ) {
                DoneButton(state)
            }
        }

        EditorMode.DESIGN -> DesignActions(state, edge.align(Alignment.CenterEnd))
        EditorMode.EXTERIOR -> ExteriorActions(state, edge.align(Alignment.CenterEnd))
    }
}

@Composable
private fun BoxScope.ControlPanels(state: DesignerState) {
    val visible = when (state.editorMode) {
        EditorMode.EXTERIOR -> true
        EditorMode.DESIGN -> state.selectedItem != null || state.selectedOpening != null
        EditorMode.DRAW_WALLS -> state.selectedOpening != null || state.selectedStair != null ||
            state.selectedBalcony != null || state.selectedWall != null
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(tween(ENTER_MS)) { it } + fadeIn(tween(ENTER_MS)),
        exit = slideOutVertically(tween(EXIT_MS)) { it } + fadeOut(tween(EXIT_MS)),
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .navigationBarsPadding()
            .padding(horizontal = 10.dp)
            .padding(bottom = PANEL_BOTTOM_GAP),
    ) {
        when (state.editorMode) {
            EditorMode.EXTERIOR -> RoofControlPanel(state)
            EditorMode.DESIGN ->
                state.selectedItem?.let { FurnitureControlPanel(item = it, state = state) }
                    ?: state.selectedOpening?.let { OpeningControlPanel(opening = it, state = state) }
            EditorMode.DRAW_WALLS ->
                state.selectedOpening?.let { OpeningControlPanel(opening = it, state = state) }
                    ?: state.selectedStair?.let { StairControlPanel(stair = it, state = state) }
                    ?: state.selectedBalcony?.let { BalconyControlPanel(balcony = it, state = state) }
                    ?: state.selectedWall?.let { WallControlPanel(state) }
        }
    }
}

@Composable
private fun PlanEditor(state: DesignerState) {
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
        activeLevel = state.activeLevel,
        onPlaceStair = state::onPlaceStair,
        onMoveStair = state::onMoveStair,
        onTapStair = { state.onSelectStair(it) },
        selectedStairId = state.selectedStairId,
        onPlaceBalcony = state::onPlaceBalcony,
        onTapBalcony = { state.onSelectBalcony(it) },
        selectedBalconyId = state.selectedBalconyId,
        onTapWall = { state.onSelectWall(it) },
        selectedWall = state.selectedWall,
        placedFurniture = state.placedFurniture,
        onMoveFurnitureInPlan = state::onMoveFurniture,
        onTapFurniture = state::onSelectFurniture,
        modifier = Modifier.fillMaxSize(),
    )
}

@Composable
private fun RoomDesignView(state: DesignerState) {
    FilamentRoomViewport(
        floorPlan = state.floorPlan,
        roomPolygons = state.roomPolygons,
        placedFurniture = state.placedFurniture,
        roomHeight = state.roomHeightCm,
        stairModel = state.stairPreset.model,
        stairColorHex = state.stairPreset.colorHex,
        stairTileM = state.stairPreset.tileM,
        exterior = state.editorMode == EditorMode.EXTERIOR,
        roofModel = state.roofPreset.model,
        roofColorHex = state.roofPreset.colorHex,
        roofTileM = state.roofPreset.tileM,
        groundModel = state.groundPreset.model,
        groundColorHex = state.groundPreset.colorHex,
        groundTileM = state.groundPreset.tileM,
        shadows = state.shadowsOn,
        // Auto-hide is for looking in; from outside it would skin the house.
        autoHideWalls = state.autoHideWalls && state.editorMode != EditorMode.EXTERIOR,
        activeLevel = state.activeLevel,
        backgroundColor = LocalInteriorAccents.current.viewportBackground,
        onDropOpening = state::onDropOpening,
        onSelectFurniture = state::onSelectFurniture,
        onSelectOpening = { state.onSelectOpening(it) },
        onMoveFurniture = state::onMoveFurniture,
        modifier = Modifier.fillMaxSize(),
    )
}
