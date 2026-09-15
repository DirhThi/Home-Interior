package com.interiordesign3d.ui.screen.home

import androidx.compose.runtime.Composable
import com.interiordesign3d.ui.screen.home.view.HomeContent

@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    HomeContent(state = viewModel.screenState)
}
