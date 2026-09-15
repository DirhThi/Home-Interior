package com.interiordesign3d.ui.screen.catalogue.state

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import com.interiordesign3d.common.base.BaseScreenState

@Stable
open class CatalogueState : BaseScreenState() {
    var groupIdx by mutableIntStateOf(0)

    open fun onSelectGroup(index: Int) { groupIdx = index }
    open fun onOpenItem(key: String) {}
}
