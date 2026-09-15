package com.interiordesign3d.ui.screen.catalogue

import androidx.compose.runtime.Composable
import com.interiordesign3d.ui.screen.catalogue.view.CatalogueContent

@Composable
fun CatalogueScreen(viewModel: CatalogueViewModel) {
    CatalogueContent(state = viewModel.screenState)
}
