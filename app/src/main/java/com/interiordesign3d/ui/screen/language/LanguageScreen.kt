package com.interiordesign3d.ui.screen.language

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import com.interiordesign3d.ui.screen.language.view.LanguageContent

@Composable
fun LanguageScreen(viewModel: LanguageViewModel) {
    val state = viewModel.screenState

    LanguageContent(state = state)
    // On first open there is nowhere behind this screen to go back to.
    BackHandler(enabled = state.firstOpen) {}
}
