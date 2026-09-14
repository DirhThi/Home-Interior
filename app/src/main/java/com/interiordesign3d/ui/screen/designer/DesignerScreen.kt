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

    BackHandler(enabled = state.selectedId != null) { state.onDeselect() }
}
