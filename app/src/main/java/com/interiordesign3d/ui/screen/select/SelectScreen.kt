package com.interiordesign3d.ui.screen.select

import androidx.compose.runtime.Composable
import com.interiordesign3d.ui.screen.select.view.SelectContent

@Composable
fun SelectScreen(viewModel: SelectViewModel) {
    SelectContent(state = viewModel.screenState)
}
