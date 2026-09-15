package com.interiordesign3d.ui.screen.settings

import androidx.compose.runtime.Composable
import com.interiordesign3d.ui.screen.settings.view.SettingsContent

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    SettingsContent(state = viewModel.screenState)
}
