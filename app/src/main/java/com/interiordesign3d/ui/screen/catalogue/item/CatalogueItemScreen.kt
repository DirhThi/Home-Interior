package com.interiordesign3d.ui.screen.catalogue.item

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import com.interiordesign3d.ui.screen.catalogue.item.view.CatalogueItemContent

@Composable
fun CatalogueItemScreen(viewModel: CatalogueItemViewModel) {
    val state = viewModel.screenState

    CatalogueItemContent(state = state)
    BackHandler(enabled = state.showRoomPicker) { state.onDismissRoomPicker() }
}
