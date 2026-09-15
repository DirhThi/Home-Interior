package com.interiordesign3d.ui.screen.project

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.interiordesign3d.common.base.CollectMessages
import com.interiordesign3d.ui.screen.project.view.ProjectContent

@Composable
fun ProjectScreen(viewModel: ProjectViewModel) {
    val snackbarHostState = remember { SnackbarHostState() }

    CollectMessages(viewModel, snackbarHostState)
    ProjectContent(state = viewModel.screenState, snackbarHostState = snackbarHostState)
}
