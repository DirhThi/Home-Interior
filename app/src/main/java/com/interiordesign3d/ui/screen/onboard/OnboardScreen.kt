package com.interiordesign3d.ui.screen.onboard

import androidx.compose.runtime.Composable
import com.interiordesign3d.ui.screen.onboard.view.OnboardContent

@Composable
fun OnboardScreen(viewModel: OnboardViewModel) {
    OnboardContent(state = viewModel.screenState)
}
