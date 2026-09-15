package com.interiordesign3d.ui.screen.catalogue

import android.app.Application
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.interiordesign3d.common.base.BaseViewModel
import com.interiordesign3d.ui.navigation.Dest
import com.interiordesign3d.ui.screen.catalogue.state.CatalogueState

class CatalogueViewModel(
    app: Application,
    backStack: NavBackStack<NavKey>,
) : BaseViewModel(app, backStack) {

    val screenState: CatalogueState = object : CatalogueState() {
        override fun onOpenItem(key: String) = navigateTo(Dest.ScrCatalogueItem(key))
    }
}
