package com.interiordesign3d.ui.screen.designer

import androidx.activity.compose.BackHandler
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.interiordesign3d.common.base.CollectMessages
import com.interiordesign3d.common.base.Navigator
import com.interiordesign3d.common.base.rememberScreenViewModel
import com.interiordesign3d.ui.screen.designer.view.DesignerContent

@Composable
fun DesignerScreen(roomId: String, navigator: Navigator) {
    val viewModel = rememberScreenViewModel("designer_$roomId") {
        DesignerViewModel(it, navigator, roomId)
    }
    val snackbarHostState = remember { SnackbarHostState() }
    val state = viewModel.screenState

    CollectMessages(viewModel, snackbarHostState)
    DesignerContent(state = state, snackbarHostState = snackbarHostState)

    // Back clears whatever is selected before it leaves the screen. Stairs, balconies and walls used
    // to fall through, so Back with a stair selected dropped you out of the designer entirely.
    val hasSelection = state.selectedId != null || state.selectedOpeningId != null ||
        state.selectedStairId != null || state.selectedBalconyId != null ||
        state.selectedWall != null || state.placementTool != PlacementTool.NONE

    BackHandler(enabled = hasSelection) {
        when {
            state.selectedId != null -> state.onDeselect()
            state.selectedOpeningId != null -> state.onSelectOpening(null)
            state.selectedStairId != null -> state.onSelectStair(null)
            state.selectedBalconyId != null -> state.onSelectBalcony(null)
            state.selectedWall != null -> state.onSelectWall(null)
            else -> state.onToolChange(PlacementTool.NONE)
        }
    }
}
