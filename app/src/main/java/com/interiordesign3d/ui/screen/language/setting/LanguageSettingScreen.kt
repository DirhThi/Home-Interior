package com.interiordesign3d.ui.screen.language.setting

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import com.interiordesign3d.ui.properties.TrackingScreen
import com.interiordesign3d.ui.screen.language.view.LanguageContent

@Composable
fun LanguageSettingScreen(viewModel: LanguageSettingViewModel) {
    TrackingScreen(screen = "ScrLanguageSetting")
    val state = viewModel.screenState

    LanguageContent(state = state, showBack = true, adPlacement = null)
    BackHandler(onBack = state::onBack)
}
