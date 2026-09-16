package com.interiordesign3d.ui.screen.splash

import androidx.compose.runtime.Composable
import com.interiordesign3d.ui.screen.splash.view.SplashContent

@Composable
fun SplashScreen(viewModel: SplashViewModel) {
    SplashContent(state = viewModel.screenState)
}
