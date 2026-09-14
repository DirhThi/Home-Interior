package com.interiordesign3d.ui.screen.home

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.interiordesign3d.common.base.CollectMessages
import com.interiordesign3d.common.base.Navigator
import com.interiordesign3d.common.base.rememberScreenViewModel
import com.interiordesign3d.ui.screen.home.view.HomeContent

@Composable
fun HomeScreen(navigator: Navigator) {
    val viewModel = rememberScreenViewModel("home") { HomeViewModel(it, navigator) }
    val snackbarHostState = remember { SnackbarHostState() }

    CollectMessages(viewModel, snackbarHostState)
    HomeContent(state = viewModel.screenState, snackbarHostState = snackbarHostState)
}
